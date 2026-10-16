package de.ardasitoo.entruempelung.customerrequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CustomerRequestNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRequestNotificationService.class);

    private final RestClient client;
    private final String apiKey;
    private final boolean enabled;
    private final String recipient;
    private final String sender;

    @Autowired
    public CustomerRequestNotificationService(
            @Value("${app.notifications.resend.api-key:}") String apiKey,
            @Value("${app.notifications.email.enabled:false}") boolean enabled,
            @Value("${app.notifications.email.to:}") String recipient,
            @Value("${app.notifications.email.from:}") String sender
    ) {
        this(createClient(), apiKey, enabled, recipient, sender);
    }

    CustomerRequestNotificationService(RestClient client, String apiKey, boolean enabled,
                                       String recipient, String sender) {
        this.client = client;
        this.apiKey = apiKey;
        this.enabled = enabled;
        this.recipient = recipient;
        this.sender = sender;
    }

    private static RestClient createClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder().baseUrl("https://api.resend.com")
                .requestFactory(factory).build();
    }

    public void notifyAbout(CustomerRequest customerRequest) {
        if (!enabled) {
            logger.info("Email notification is disabled. Customer request {} was saved without email.", customerRequest.getId());
            return;
        }

        try {
            send("Neue NEXRUM Entrümpelungsanfrage", buildMessage(customerRequest),
                    customerRequest.getEmail());
        } catch (RuntimeException exception) {
            logger.warn("Customer request {} was saved, but email notification failed ({}).",
                    customerRequest.getId(), exception.getClass().getSimpleName());
        }
    }

    public void sendTestEmail() {
        send("Test: NEXRUM Anfrage-Mail",
                "Das ist eine Test-Mail vom NEXRUM-Backend über Resend.", null);
    }

    private void send(String subject, String text, String replyTo) {
        if (!enabled) {
            throw new IllegalStateException("Email notifications are disabled.");
        }
        if (apiKey.isBlank() || recipient.isBlank() || sender.isBlank()) {
            logger.error("Resend configuration incomplete. Check RESEND_API_KEY, "
                    + "EMAIL_NOTIFICATIONS_TO and EMAIL_NOTIFICATIONS_FROM.");
            throw new IllegalStateException("Resend configuration incomplete.");
        }

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("from", sender);
        payload.put("to", List.of(recipient));
        payload.put("subject", subject);
        payload.put("text", text);
        if (replyTo != null && !replyTo.isBlank()) {
            payload.put("reply_to", replyTo);
        }
        try {
            client.post().uri("/emails")
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload).retrieve().toBodilessEntity();
            logger.info("Resend accepted email notification for delivery.");
        } catch (RestClientResponseException exception) {
            // Do not log response bodies: they may contain personal data.
            logger.error("Resend rejected email (HTTP {}). Check API key, verified sender "
                    + "and recipient restrictions in the Resend dashboard.", exception.getStatusCode().value());
            throw new IllegalStateException("Resend rejected email (HTTP "
                    + exception.getStatusCode().value() + ").");
        }
    }

    private String buildMessage(CustomerRequest customerRequest) {
        return """
                Es wurde eine neue Entruempelungsanfrage gespeichert.

                Name: %s %s
                E-Mail: %s
                Telefon: %s
                Adresse: %s
                Postleitzahl: %s
                Leistung: %s
                Wunschtermin: %s

                Nachricht:
                %s
                """.formatted(
                customerRequest.getFirstName(),
                customerRequest.getLastName(),
                customerRequest.getEmail(),
                valueOrFallback(customerRequest.getPhone()),
                customerRequest.getAddress(),
                customerRequest.getPostalCode(),
                customerRequest.getServiceType(),
                customerRequest.getPreferredDate() == null ? "Nicht angegeben" : customerRequest.getPreferredDate(),
                valueOrFallback(customerRequest.getMessage())
        );
    }

    private String valueOrFallback(String value) {
        return value == null || value.isBlank() ? "Nicht angegeben" : value;
    }
}
