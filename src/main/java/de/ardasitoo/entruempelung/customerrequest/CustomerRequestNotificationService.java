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
            logger.info("Email notification is disabled. Customer request {} was saved without email.", customerRequest.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(recipient);
        message.setSubject("Neue Entruempelungsanfrage");
        message.setText(buildMessage(customerRequest));

        try {
            send(message);
        } catch (RuntimeException exception) {
            logger.warn("Customer request {} was saved, but email notification failed.", customerRequest.getId());
        }
    }

    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(recipient);
        message.setSubject("Test: Entruempelung Anfrage-Mail");
        message.setText("""
                Das ist eine Test-Mail vom Entruempelung-Backend.

                Wenn diese Mail angekommen ist, funktioniert der SMTP-Versand ueber Render.
                """);

        send(message);
    }

    private void send(SimpleMailMessage message) {
        if (!enabled) {
            throw new IllegalStateException("Email notifications are disabled.");
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("JavaMailSender is not available. Check SPRING_MAIL_* variables.");
        }

        if (recipient.isBlank()) {
            throw new IllegalStateException("Email recipient is missing. Check EMAIL_NOTIFICATIONS_TO.");
        }

        try {
            mailSender.send(message);
            logger.info("Email notification sent to {}.", recipient);
        } catch (RuntimeException exception) {
            logger.error("Could not send email notification to {}.", recipient, exception);
            throw exception;
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
