package com.tradingbot.loriview.repository;

import com.tradingbot.loriview.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(p.amountKes),0) FROM Payment p " +
            "WHERE p.status = 'SUCCESS'")
    Optional<BigDecimal> sumSuccessfulPayments();

    @Query("SELECT COALESCE(SUM(p.amountKes),0) FROM Payment p " +
            "WHERE p.status = 'SUCCESS' AND p.createdAt >= :from")
    Optional<BigDecimal> sumPaymentsSince(
            @Param("from") ZonedDateTime from
    );

    List<Payment> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Payment> findByCheckoutRequestId(String checkoutRequestId);
}