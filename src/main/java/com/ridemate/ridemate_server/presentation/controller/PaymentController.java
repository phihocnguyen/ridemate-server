package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.payment.CreatePaymentRequest;
import com.ridemate.ridemate_server.application.dto.payment.PaymentResponse;
import com.ridemate.ridemate_server.application.service.payment.StripePaymentService;
import com.ridemate.ridemate_server.domain.entity.UserMembership;
import com.ridemate.ridemate_server.domain.repository.PaymentRepository;
import com.ridemate.ridemate_server.domain.repository.UserMembershipRepository;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final ObjectProvider<StripePaymentService> stripePaymentServiceProvider;
    
    private StripePaymentService getStripePaymentService() {
        return stripePaymentServiceProvider.getIfAvailable(() -> {
            throw new IllegalStateException("Stripe payment service is not available. Please configure payment.provider=stripe");
        });
    }

    @PostMapping("/create")
    @Operation(summary = "Create payment", description = "Create a new payment request")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "User ID is required. Please ensure you are authenticated."));
        }
        request.setUserId(userId);
        
        PaymentResponse response = getStripePaymentService().createPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment created successfully", response));
    }

    @PostMapping("/stripe/confirm")
    @Operation(summary = "Confirm Stripe payment", description = "Confirm Stripe checkout session")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmStripePayment(
            @RequestParam String sessionId) {
        PaymentResponse response = getStripePaymentService().confirmPayment(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed", response));
    }

    @GetMapping("/stripe/config")
    @Operation(summary = "Get Stripe config", description = "Get Stripe publishable key for frontend")
    public ResponseEntity<ApiResponse<Map<String, String>>> getStripeConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("publishableKey", getStripePaymentService().getPublishableKey());
        return ResponseEntity.ok(ApiResponse.success("Stripe config retrieved", config));
    }

    @GetMapping("/query/{orderId}")
    @Operation(summary = "Query payment", description = "Query payment status by order ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> queryPayment(@PathVariable String orderId) {
        PaymentResponse response = getStripePaymentService().queryPayment(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment queried successfully", response));
    }

    @GetMapping("/my-payments")
    @Operation(summary = "Get my payments", description = "Get all payments for current user")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "User ID is required. Please ensure you are authenticated."));
        }
        
        List<PaymentResponse> payments = paymentRepository.findByUserId(userId).stream()
                .map(payment -> PaymentResponse.builder()
                        .paymentId(payment.getId())
                        .orderId(payment.getOrderId())
                        .requestId(payment.getRequestId())
                        .amount(payment.getAmount())
                        .status(payment.getStatus().toString())
                        .transId(payment.getTransId())
                        .paymentUrl(payment.getPaymentUrl())
                        .orderInfo(payment.getOrderInfo())
                        .referenceId(payment.getReferenceId())
                        .referenceType(payment.getReferenceType())
                        .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : null)
                        .paidAt(payment.getPaidAt())
                        .createdAt(payment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    @GetMapping("/my-membership")
    @Operation(summary = "Get my current membership", description = "Get active membership for current user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyMembership(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "User ID is required. Please ensure you are authenticated."));
        }
        
        UserMembership activeMembership = userMembershipRepository
                .findByUserIdAndStatusAndEndDateAfter(userId, UserMembership.MembershipStatus.ACTIVE, 
                    java.time.LocalDateTime.now())
                .orElse(null);
        
        Map<String, Object> result = new HashMap<>();
        if (activeMembership != null && activeMembership.isActive()) {
            result.put("id", activeMembership.getId());
            result.put("membershipId", activeMembership.getMembershipId());
            result.put("membershipName", activeMembership.getMembershipName());
            result.put("startDate", activeMembership.getStartDate());
            result.put("endDate", activeMembership.getEndDate());
            result.put("status", activeMembership.getStatus().toString());
            result.put("isActive", true);
        } else {
            result.put("isActive", false);
            result.put("message", "Bạn chưa có gói hội viên đang hoạt động");
        }
        
        return ResponseEntity.ok(ApiResponse.success("Membership retrieved successfully", result));
    }
}

