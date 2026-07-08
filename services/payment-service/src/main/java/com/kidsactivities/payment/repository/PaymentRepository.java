package com.kidsactivities.payment.repository;

import com.kidsactivities.common.model.PaymentStatus;
import com.kidsactivities.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingIdAndStatus(Long bookingId, PaymentStatus status);
    Optional<Payment> findByIdAndUserId(Long id, Long userId);
    Optional<Payment> findByExternalReference(String externalReference);
}
