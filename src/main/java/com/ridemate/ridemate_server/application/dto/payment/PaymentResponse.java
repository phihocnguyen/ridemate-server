package com.ridemate.ridemate_server.application.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment response")
public class PaymentResponse {

    @Schema(description = "Payment ID")
    private Long paymentId;

    @Schema(description = "Order ID")
    private String orderId;

    @Schema(description = "Request ID")
    private String requestId;

    @Schema(description = "Payment amount")
    private BigDecimal amount;

    @Schema(description = "Payment status")
    private String status;

    @Schema(description = "Payment URL for redirect")
    private String paymentUrl;

    @Schema(description = "Transaction ID from payment provider")
    private String transId;

    @Schema(description = "Order info/description")
    private String orderInfo;

    @Schema(description = "Reference ID (e.g., membership ID, match ID)")
    private String referenceId;

    @Schema(description = "Reference type (MEMBERSHIP, MATCH, etc.)")
    private String referenceType;

    @Schema(description = "Payment method")
    private String paymentMethod;

    @Schema(description = "When payment was completed")
    private LocalDateTime paidAt;

    @Schema(description = "When payment was created")
    private LocalDateTime createdAt;

    @Schema(description = "Message")
    private String message;

    @Schema(description = "Error code")
    private Integer errorCode;
}

