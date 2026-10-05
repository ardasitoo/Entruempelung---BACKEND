package de.ardasitoo.entruempelung.admin;

import de.ardasitoo.entruempelung.config.AdminAccessService;
import de.ardasitoo.entruempelung.customerrequest.CustomerRequestNotificationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminEmailTestController {

    private final AdminAccessService adminAccessService;
    private final CustomerRequestNotificationService notificationService;

    public AdminEmailTestController(
            AdminAccessService adminAccessService,
            CustomerRequestNotificationService notificationService
    ) {
        this.adminAccessService = adminAccessService;
        this.notificationService = notificationService;
    }

    @PostMapping("/test-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendTestEmail(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            HttpServletResponse response
    ) {
        if (!adminAccessService.isAllowed(authorizationHeader)) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Admin\"");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin access required");
        }

        notificationService.sendTestEmail();
    }
}
