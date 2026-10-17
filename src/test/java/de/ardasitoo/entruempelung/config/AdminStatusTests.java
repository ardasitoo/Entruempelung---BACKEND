package de.ardasitoo.entruempelung.config;

import de.ardasitoo.entruempelung.customerrequest.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.FilterChainProxy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.admin.username=status-test", "app.admin.password=test-password", "app.notifications.email.enabled=false"})
class AdminStatusTests {
    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy securityFilterChain;
    @Autowired CustomerRequestRepository repository;
    private String authorization() {
        return "Basic " + Base64.getEncoder().encodeToString("status-test:test-password".getBytes(StandardCharsets.UTF_8));
    }
    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilterChain).build();
    }
    private CustomerRequest createRequest() {
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Test"); request.setLastName("Kunde");
        request.setEmail("test@example.com"); request.setAddress("Teststraße 1");
        request.setPostalCode("10115"); request.setServiceType("Entrümpelung");
        return repository.save(request);
    }
    @Test
    void adminCanPersistStatusWithoutChangingCustomerDetails() throws Exception {
        CustomerRequest request = createRequest();
        mvc().perform(patch("/api/customer-requests/" + request.getId() + "/status")
                .header("Authorization", authorization()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\",\"firstName\":\"Changed\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        CustomerRequest saved = repository.findById(request.getId()).orElseThrow();
        assertEquals(CustomerRequestStatus.COMPLETED, saved.getStatus());
        assertEquals("Test", saved.getFirstName());
    }
    @Test
    void anonymousAndInvalidCredentialsCannotChangeStatus() throws Exception {
        CustomerRequest request = createRequest();
        String url = "/api/customer-requests/" + request.getId() + "/status";
        mvc().perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isUnauthorized());
        mvc().perform(patch(url).header("Authorization", "Basic aW52YWxpZDppbnZhbGlk")
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isUnauthorized());
        assertEquals(CustomerRequestStatus.NEW, repository.findById(request.getId()).orElseThrow().getStatus());
    }
    @Test
    void rejectsNullAndUnknownStatuses() throws Exception {
        CustomerRequest request = createRequest();
        String url = "/api/customer-requests/" + request.getId() + "/status";
        for (String body : new String[]{"{}", "{\"status\":null}", "{\"status\":\"INVALID\"}"}) {
            mvc().perform(patch(url).header("Authorization", authorization())
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
        assertEquals(CustomerRequestStatus.NEW, repository.findById(request.getId()).orElseThrow().getStatus());
    }
    @Test
    void missingRequestReturnsNotFound() throws Exception {
        mvc().perform(patch("/api/customer-requests/9223372036854775807/status")
                .header("Authorization", authorization()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CONTACTED\"}"))
                .andExpect(status().isNotFound());
    }
}
