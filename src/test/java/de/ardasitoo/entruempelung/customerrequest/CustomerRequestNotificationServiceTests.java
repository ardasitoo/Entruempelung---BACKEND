package de.ardasitoo.entruempelung.customerrequest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CustomerRequestNotificationServiceTests {
    private final RestClient.Builder builder = RestClient.builder().baseUrl("https://api.resend.com");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    private CustomerRequestNotificationService service(boolean enabled, String key) {
        return new CustomerRequestNotificationService(builder.build(), key, enabled,
                "owner@example.com", "NEXRUM <anfragen@example.com>");
    }

    @Test
    void sendsCustomerDetailsAndReplyAddress() {
        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.to[0]").value("owner@example.com"))
                .andExpect(jsonPath("$.reply_to").value("customer@example.com"))
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Postleitzahl: 10115")))
                .andRespond(withSuccess("{\"id\":\"email-id\"}", MediaType.APPLICATION_JSON));
        CustomerRequest request = new CustomerRequest();
        request.setEmail("customer@example.com");
        request.setPostalCode("10115");
        service(true, "test-key").notifyAbout(request);
        server.verify();
    }

    @Test
    void rejectsFailedTestButDoesNotFailCustomerNotification() {
        server.expect(requestTo("https://api.resend.com/emails"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));
        assertThrows(IllegalStateException.class, () -> service(true, "test-key").sendTestEmail());
        server.verify();
        server.reset();
        server.expect(requestTo("https://api.resend.com/emails"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertDoesNotThrow(() -> service(true, "test-key").notifyAbout(new CustomerRequest()));
        server.verify();
    }

    @Test
    void disabledOrMissingKeyDoesNotContactResend() {
        service(false, "").notifyAbout(new CustomerRequest());
        assertThrows(IllegalStateException.class, () -> service(false, "").sendTestEmail());
        assertThrows(IllegalStateException.class, () -> service(true, "").sendTestEmail());
        server.verify();
    }
}
