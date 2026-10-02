package de.ardasitoo.entruempelung.customerrequest;

import de.ardasitoo.entruempelung.config.AdminAccessService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customer-requests")
public class CustomerRequestController {

    private final CustomerRequestRepository customerRequestRepository;
    private final CustomerRequestNotificationService notificationService;
    private final AdminAccessService adminAccessService;

    public CustomerRequestController(
            CustomerRequestRepository customerRequestRepository,
            CustomerRequestNotificationService notificationService,
            AdminAccessService adminAccessService
    ) {
        this.customerRequestRepository = customerRequestRepository;
        this.notificationService = notificationService;
        this.adminAccessService = adminAccessService;
    }

    @GetMapping
    public List<CustomerRequest> findAll(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            HttpServletResponse response
    ) {
        if (!adminAccessService.isAllowed(authorizationHeader)) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Customer Requests\"");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin access required");
        }

        return customerRequestRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerRequest create(@Valid @RequestBody CustomerRequest customerRequest) {
        customerRequest.setId(null);
        customerRequest.setStatus(CustomerRequestStatus.NEW);
        CustomerRequest savedRequest = customerRequestRepository.save(customerRequest);
        notificationService.notifyAbout(savedRequest);
        return savedRequest;
    }
}
