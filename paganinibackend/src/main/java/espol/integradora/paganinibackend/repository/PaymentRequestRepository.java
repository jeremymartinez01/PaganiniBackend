package espol.integradora.paganinibackend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import espol.integradora.paganinibackend.model.PaymentRequest.Status;
import espol.integradora.paganinibackend.model.User;
import espol.integradora.paganinibackend.model.PaymentRequest;
import jakarta.persistence.LockModeType;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentRequest p where p.id = :id")
    Optional<PaymentRequest> lockById(@Param("id") Integer id);

    // fallback cuando el QR NO trae pid
    Optional<PaymentRequest> findTopByRequesterAndAmountAndStatusOrderByCreatedAtDesc(
            User requester, BigDecimal amount, Status status
    );
    List<PaymentRequest> findByRequesterAndStatusOrderByCreatedAtDesc(User requester, Status status);

}