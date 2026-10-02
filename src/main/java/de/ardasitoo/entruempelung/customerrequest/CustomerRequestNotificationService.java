package de.ardasitoo.entruempelung.customerrequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CustomerRequestNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRequestNotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String recipient;
    private final String sender;

    public CustomerRequestNotificationService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.notifications.email.enabled:false}") boolean enabled,
            @Value("${app.notifications.email.to:}") String recipient,
            @Value("${app.notifications.email.from:noreply@example.com}") String sender
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.recipient = recipient;
        this.sender = sender;
    }

    public void notifyAbout(CustomerRequest customerRequest) {
        if (!enabled) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || recipient.isBlank()) {
            logger.warn("Email notification is enabled, but mail sender or recipient is missing.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(recipient);
        message.setSubject("Neue Entruempelungsanfrage");
        message.setText(buildMessage(customerRequest));

        try {
            mailSender.send(message);
        } catch (RuntimeException exception) {
            logger.warn("Could not send customer request notification email.", exception);
        }
    }

    private String buildMessage(CustomerRequest customerRequest) {
        return """
                Es wurde eine neue Entruempelungsanfrage gespeichert.

                Name: %s %s
                E-Mail: %s
                Telefon: %s
                Adresse: %s
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
                customerRequest.getServiceType(),
                customerRequest.getPreferredDate() == null ? "Nicht angegeben" : customerRequest.getPreferredDate(),
                valueOrFallback(customerRequest.getMessage())
        );
    }

    private String valueOrFallback(String value) {
        return value == null || value.isBlank() ? "Nicht angegeben" : value;
    }
}
