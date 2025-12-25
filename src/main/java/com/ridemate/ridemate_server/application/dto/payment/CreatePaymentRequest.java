package com.ridemate.ridemate_server.application.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create payment request")
public class CreatePaymentRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Payment amount", example = "50000")
    private BigDecimal amount;

    @NotNull(message = "Order info is required")
    @Schema(description = "Order description", example = "Thanh toan cho chuyen di")
    private String orderInfo;

    @Schema(description = "Reference ID (e.g., match_id, booking_id)", example = "123")
    private String referenceId;

    @Schema(description = "Reference type (MATCH, BOOKING, MEMBERSHIP, etc.)", example = "MATCH")
    private String referenceType;

    @Schema(description = "Return URL after payment", example = "https://yourapp.com/payment/return")
    private String returnUrl;

    @Schema(description = "Notify URL for IPN callback", example = "https://yourapp.com/api/payments/callback")
    private String notifyUrl;

    // userId is set automatically from JWT authentication, not from request body
    @Schema(description = "User ID making the payment (set automatically from authentication)")
    private Long userId;
}

