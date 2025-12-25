package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByRequestId(String requestId);
    Optional<Payment> findByTransId(String transId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findByUserIdAndStatus(Long userId, Payment.PaymentStatus status);
    List<Payment> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
}

