package de.ardasitoo.entruempelung.customerrequest;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface CustomerRequestRepository extends JpaRepository<CustomerRequest, Long> {
    @Modifying
    @Query("delete from CustomerRequest r where r.status = :status and r.rejectedAt <= :cutoff and r.retentionProtected = false")
    int deleteExpiredRejected(@Param("status") CustomerRequestStatus status, @Param("cutoff") LocalDateTime cutoff);
}
