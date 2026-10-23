package de.ardasitoo.entruempelung.customerrequest;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class CustomerRequestRetentionService {
    private final CustomerRequestRepository repository;
    public CustomerRequestRetentionService(CustomerRequestRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 86400000, initialDelay = 60000)
    @Transactional
    public void deleteExpiredRequests() {
        deleteExpiredAt(LocalDateTime.now(ZoneOffset.UTC));
    }

    @Transactional
    public int deleteExpiredAt(LocalDateTime now) {
        int deleted = repository.deleteExpiredRejected(CustomerRequestStatus.REJECTED, now.minusMonths(6));
        if (deleted > 0) LoggerFactory.getLogger(getClass()).info("Deleted {} expired rejected requests.", deleted);
        return deleted;
    }
}
