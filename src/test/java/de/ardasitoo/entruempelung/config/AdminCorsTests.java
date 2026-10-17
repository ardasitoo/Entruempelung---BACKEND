package de.ardasitoo.entruempelung.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.FilterChainProxy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.cors.allowed-origins=https://entruempelung-frontend.onrender.com")
class AdminCorsTests {
    private static final String ORIGIN = "https://entruempelung-frontend.onrender.com";
    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy securityFilterChain;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilterChain).build();
    }

    @Test
    void loginPreflightAllowsAuthorizationHeader() throws Exception {
        mvc().perform(options("/api/customer-requests")
                .header("Origin", ORIGIN)
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Headers", "authorization"));
    }

    @Test
    void unauthorizedResponseIsReadableByFrontend() throws Exception {
        mvc().perform(get("/api/customer-requests").header("Origin", ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    @Test
    void failedLoginResponseIsReadableByFrontend() throws Exception {
        mvc().perform(get("/api/customer-requests").header("Origin", ORIGIN)
                .header("Authorization", "Basic bm9uZXhpc3RlbnQ6aW52YWxpZA=="))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    @Test
    void untrustedOriginRemainsBlocked() throws Exception {
        mvc().perform(get("/api/customer-requests").header("Origin", "https://untrusted.example"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
