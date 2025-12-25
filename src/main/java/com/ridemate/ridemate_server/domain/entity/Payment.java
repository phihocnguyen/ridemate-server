package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(nullable = false, length = 200)
    private String requestId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(length = 500)
    private String orderInfo; // Description of the payment

    @Column(length = 100)
    private String partnerCode;

    @Column(length = 200)
    private String transId;

    @Column(length = 50)
    private String payType;

    @Column(length = 50)
    private String responseTime;

    @Column(length = 1000)
    private String extraData; // Additional data

    @Column(length = 1000)
    private String signature;

    @Column(length = 1000)
    private String paymentUrl;

    @Column
    private LocalDateTime paidAt; // When payment was completed

    @Column(length = 1000)
    private String errorMessage; // Error message if payment failed

    @Column(length = 100)
    private String referenceId; // Reference to related entity (e.g., match_id, booking_id)

    @Column(length = 50)
    private String referenceType; // Type of reference (MATCH, BOOKING, MEMBERSHIP, etc.)

    public enum PaymentStatus {
        PENDING,        // Payment initiated, waiting for user to pay
        PROCESSING,     // Payment is being processed
        SUCCESS,        // Payment completed successfully
        FAILED,         // Payment failed
        CANCELLED,      // Payment was cancelled
        EXPIRED         // Payment expired (not paid in time)
    }

    public enum PaymentMethod {
        STRIPE,         // Stripe payment
        BANK_TRANSFER,  // Bank transfer
        CREDIT_CARD,    // Credit card
        CASH            // Cash payment
    }

    public void markAsSuccess(String transId, String payType, String responseTime) {
        this.status = PaymentStatus.SUCCESS;
        this.transId = transId;
        this.payType = payType;
        this.responseTime = responseTime;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = PaymentStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }

    public void markAsExpired() {
        this.status = PaymentStatus.EXPIRED;
    }
}

