package de.ardasitoo.entruempelung.config;

import de.ardasitoo.entruempelung.customerrequest.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RetentionTests {
    @Autowired CustomerRequestRepository repository;
    @Autowired CustomerRequestRetentionService retention;

    private CustomerRequest request(CustomerRequestStatus status, LocalDateTime rejectedAt) {
        CustomerRequest r = new CustomerRequest();
        r.setFirstName("Retention"); r.setLastName("Test"); r.setEmail("test@example.com");
        r.setAddress("Test 1"); r.setPostalCode("10115"); r.setServiceType("Keller");
        r.setStatus(status);
        ReflectionTestUtils.setField(r, "rejectedAt", rejectedAt);
        return r;
    }

    @Test
    void deletesOnlyExpiredUncontractedRejectionsIncludingBoundary() {
        LocalDateTime now = LocalDateTime.of(2027, 4, 24, 12, 0);
        LocalDateTime cutoff = now.minusMonths(6);
        var expired = repository.save(request(CustomerRequestStatus.REJECTED, cutoff.minusSeconds(1)));
        var boundary = repository.save(request(CustomerRequestStatus.REJECTED, cutoff));
        var recent = repository.save(request(CustomerRequestStatus.REJECTED, cutoff.plusSeconds(1)));
        var legacy = repository.save(request(CustomerRequestStatus.REJECTED, null));
        var open = repository.save(request(CustomerRequestStatus.CONTACTED, cutoff.minusMonths(1)));
        var accepted = request(CustomerRequestStatus.ACCEPTED, null);
        accepted.setStatus(CustomerRequestStatus.REJECTED);
        ReflectionTestUtils.setField(accepted, "rejectedAt", cutoff.minusMonths(1));
        accepted = repository.save(accepted);
        var completed = repository.save(request(CustomerRequestStatus.COMPLETED, cutoff.minusMonths(1)));
        retention.deleteExpiredAt(now);
        assertFalse(repository.existsById(expired.getId()));
        assertFalse(repository.existsById(boundary.getId()));
        for (var kept : new CustomerRequest[]{recent, legacy, open, accepted, completed}) {
            assertTrue(repository.existsById(kept.getId()));
        }
    }

    @Test
    void repeatedRejectionDoesNotExtendDeadlineAndReopeningClearsIt() {
        LocalDateTime original = LocalDateTime.of(2026, 1, 1, 12, 0);
        var r = request(CustomerRequestStatus.REJECTED, original);
        r.setStatus(CustomerRequestStatus.REJECTED);
        assertEquals(original, ReflectionTestUtils.getField(r, "rejectedAt"));
        r.setStatus(CustomerRequestStatus.CONTACTED);
        assertNull(ReflectionTestUtils.getField(r, "rejectedAt"));
        r.setStatus(CustomerRequestStatus.REJECTED);
        assertNotEquals(original, ReflectionTestUtils.getField(r, "rejectedAt"));
    }
}
