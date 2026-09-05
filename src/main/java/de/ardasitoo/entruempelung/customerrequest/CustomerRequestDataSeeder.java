package de.ardasitoo.entruempelung.customerrequest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CustomerRequestDataSeeder implements CommandLineRunner {

    private final CustomerRequestRepository customerRequestRepository;

    public CustomerRequestDataSeeder(CustomerRequestRepository customerRequestRepository) {
        this.customerRequestRepository = customerRequestRepository;
    }

    @Override
    public void run(String... args) {
        if (customerRequestRepository.count() > 0) {
            return;
        }

        CustomerRequest basementClearance = new CustomerRequest();
        basementClearance.setFirstName("Max");
        basementClearance.setLastName("Mustermann");
        basementClearance.setEmail("max.mustermann@example.com");
        basementClearance.setPhone("+49 170 1234567");
        basementClearance.setAddress("Musterstrasse 12, 12345 Berlin");
        basementClearance.setServiceType("Kellerentruempelung");
        basementClearance.setMessage("Der Keller soll komplett geleert und besenrein uebergeben werden.");
        basementClearance.setPreferredDate(LocalDate.now().plusDays(7));
        basementClearance.setStatus(CustomerRequestStatus.NEW);

        CustomerRequest apartmentClearance = new CustomerRequest();
        apartmentClearance.setFirstName("Aylin");
        apartmentClearance.setLastName("Demir");
        apartmentClearance.setEmail("aylin.demir@example.com");
        apartmentClearance.setPhone("+49 160 7654321");
        apartmentClearance.setAddress("Hauptstrasse 48, 50667 Koeln");
        apartmentClearance.setServiceType("Wohnungsaufloesung");
        apartmentClearance.setMessage("Eine Zwei-Zimmer-Wohnung soll nach einem Umzug entruempelt werden.");
        apartmentClearance.setPreferredDate(LocalDate.now().plusDays(14));
        apartmentClearance.setStatus(CustomerRequestStatus.CONTACTED);

        CustomerRequest garageClearance = new CustomerRequest();
        garageClearance.setFirstName("Jonas");
        garageClearance.setLastName("Schneider");
        garageClearance.setEmail("jonas.schneider@example.com");
        garageClearance.setAddress("Industrieweg 5, 44135 Dortmund");
        garageClearance.setServiceType("Garagenentruempelung");
        garageClearance.setMessage("Alte Moebel, Kartons und Werkzeuge sollen fachgerecht entsorgt werden.");
        garageClearance.setPreferredDate(LocalDate.now().plusDays(21));
        garageClearance.setStatus(CustomerRequestStatus.OFFER_SENT);

        customerRequestRepository.saveAll(List.of(
                basementClearance,
                apartmentClearance,
                garageClearance
        ));
    }
}
