package de.ardasitoo.entruempelung.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AdminAccessService {

    private final String expectedUsername;
    private final String expectedPassword;

    public AdminAccessService(
            @Value("${app.admin.username:admin}") String expectedUsername,
            @Value("${app.admin.password:changeme}") String expectedPassword
    ) {
        this.expectedUsername = expectedUsername;
        this.expectedPassword = expectedPassword;
    }

    public boolean isAllowed(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            return false;
        }

        String encodedCredentials = authorizationHeader.substring("Basic ".length());
        String decodedCredentials;
        try {
            decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        String[] credentials = decodedCredentials.split(":", 2);

        if (credentials.length != 2) {
            return false;
        }

        return expectedUsername.equals(credentials[0]) && expectedPassword.equals(credentials[1]);
    }
}
