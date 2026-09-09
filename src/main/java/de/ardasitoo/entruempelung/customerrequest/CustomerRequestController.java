package de.ardasitoo.entruempelung.customerrequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer-requests")
public class CustomerRequestController {

    private final CustomerRequestRepository customerRequestRepository;

    public CustomerRequestController(CustomerRequestRepository customerRequestRepository) {
        this.customerRequestRepository = customerRequestRepository;
    }

    @GetMapping
    public List<CustomerRequest> findAll() {
        return customerRequestRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerRequest create(@Valid @RequestBody CustomerRequest customerRequest) {
        customerRequest.setId(null);
        customerRequest.setStatus(CustomerRequestStatus.NEW);
        return customerRequestRepository.save(customerRequest);
    }
}
