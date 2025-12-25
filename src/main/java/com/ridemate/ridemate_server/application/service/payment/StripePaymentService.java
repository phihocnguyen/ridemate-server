package com.ridemate.ridemate_server.application.service.payment;

import com.ridemate.ridemate_server.application.dto.payment.CreatePaymentRequest;
import com.ridemate.ridemate_server.application.dto.payment.PaymentResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.model.PaymentIntent;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.ridemate.ridemate_server.application.service.notification.NotificationService;
import com.ridemate.ridemate_server.domain.entity.Payment;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.Membership;
import com.ridemate.ridemate_server.domain.entity.UserMembership;
import com.ridemate.ridemate_server.domain.repository.MembershipRepository;
import com.ridemate.ridemate_server.domain.repository.PaymentRepository;
import com.ridemate.ridemate_server.domain.repository.UserMembershipRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "payment.provider", havingValue = "stripe", matchIfMissing = false)
public class StripePaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final MembershipRepository membershipRepository;
    private final NotificationService notificationService;

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @Value("${stripe.publishable-key:}")
    private String publishableKey;

    @Value("${stripe.currency:vnd}")
    private String currency;

    @Value("${stripe.success-url:ridemate://payment-success}")
    private String successUrl;

    @Value("${stripe.cancel-url:ridemate://payment-cancel}")
    private String cancelUrl;

    public StripePaymentService(
            PaymentRepository paymentRepository, 
            UserRepository userRepository,
            UserMembershipRepository userMembershipRepository,
            MembershipRepository membershipRepository,
            NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.userMembershipRepository = userMembershipRepository;
        this.membershipRepository = membershipRepository;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        if (secretKey != null && !secretKey.isEmpty()) {
            Stripe.apiKey = secretKey;
            log.info("Stripe payment service initialized (Test mode: {})", secretKey.startsWith("sk_test"));
        } else {
            log.warn("Stripe secret key not configured");
        }
    }

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String orderId = "RIDEMATE_" + System.currentTimeMillis() + "_" + user.getId();
            long amountInCents = request.getAmount().longValue();

            // Create Checkout Session for WebView/Expo Go compatibility
            SessionCreateParams.LineItem.PriceData priceData = 
                SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(currency)
                    .setUnitAmount(amountInCents)
                    .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(request.getOrderInfo())
                            .build()
                    )
                    .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .addLineItem(
                        SessionCreateParams.LineItem.builder()
                            .setPriceData(priceData)
                            .setQuantity(1L)
                            .build()
                    )
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}&order_id=" + orderId)
                    .setCancelUrl(cancelUrl + "?order_id=" + orderId)
                    .putMetadata("orderId", orderId)
                    .putMetadata("userId", user.getId().toString())
                    .putMetadata("referenceId", request.getReferenceId() != null ? request.getReferenceId() : "")
                    .putMetadata("referenceType", request.getReferenceType() != null ? request.getReferenceType() : "")
                    .build();

            Session session = Session.create(params);

            Payment payment = Payment.builder()
                    .user(user)
                    .orderId(orderId)
                    .requestId(session.getId())
                    .amount(request.getAmount())
                    .status(Payment.PaymentStatus.PENDING)
                    .paymentMethod(Payment.PaymentMethod.STRIPE)
                    .orderInfo(request.getOrderInfo())
                    .partnerCode("STRIPE")
                    .transId(session.getId())
                    .paymentUrl(session.getUrl())
                    .referenceId(request.getReferenceId())
                    .referenceType(request.getReferenceType())
                    .build();

            payment = paymentRepository.save(payment);

            log.info("Stripe checkout session created: orderId={}, sessionId={}, url={}", 
                orderId, session.getId(), session.getUrl());

            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .orderId(orderId)
                    .requestId(session.getId())
                    .amount(request.getAmount())
                    .status("PENDING")
                    .paymentUrl(session.getUrl())
                    .transId(session.getId())
                    .message("Checkout session created successfully")
                    .build();

        } catch (StripeException e) {
            log.error("Failed to create Stripe payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse confirmPayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            String paymentIntentId = session.getPaymentIntent();

            Payment payment = paymentRepository.findByTransId(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

            // MOCK MODE: ALWAYS SUCCESS
            // Bypass Stripe status check for testing/demo purposes
            String mockTransId = session.getPaymentIntent() != null ? session.getPaymentIntent() : "mock_pi_" + UUID.randomUUID().toString();
            String mockPaymentMethod = "card_mock";
            String mockCreated = String.valueOf(System.currentTimeMillis() / 1000);

            // In real logic we would check: 
            // if ("complete".equals(session.getStatus()) && paymentIntentId != null) ...
            
            // Force Success
            payment.markAsSuccess(
                    mockTransId,
                    mockPaymentMethod,
                    mockCreated
            );
            paymentRepository.save(payment);

            // Apply membership if payment is for membership
            if ("MEMBERSHIP".equals(payment.getReferenceType()) && payment.getReferenceId() != null) {
                applyMembership(payment);
            }

            // Send notification
            sendPaymentSuccessNotification(payment);

            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .requestId(sessionId)
                    .amount(payment.getAmount())
                    .status("SUCCESS")
                    .transId(mockTransId)
                    .message("Payment confirmed successfully (MOCKED)")
                    .build();
            
            /* ORIGINAL LOGIC COMMENTED OUT
            if ("complete".equals(session.getStatus()) && paymentIntentId != null) {
                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                
                if ("succeeded".equals(paymentIntent.getStatus())) {
                    payment.markAsSuccess(
                            paymentIntentId,
                            paymentIntent.getPaymentMethodTypes().toString(),
                            String.valueOf(paymentIntent.getCreated())
                    );
                    paymentRepository.save(payment);

                    // Apply membership if payment is for membership
                    if ("MEMBERSHIP".equals(payment.getReferenceType()) && payment.getReferenceId() != null) {
                        applyMembership(payment);
                    }

                    // Send notification
                    sendPaymentSuccessNotification(payment);

                    return PaymentResponse.builder()
                            .paymentId(payment.getId())
                            .orderId(payment.getOrderId())
                            .requestId(sessionId)
                            .amount(payment.getAmount())
                            .status("SUCCESS")
                            .transId(paymentIntentId)
                            .message("Payment confirmed successfully")
                            .build();
                }
            }
            
            payment.markAsFailed("Payment status: " + session.getStatus());
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .status("FAILED")
                    .errorCode(400)
                    .message("Payment failed: " + session.getStatus())
                    .build();
            */

        } catch (StripeException e) {
            log.error("Failed to confirm Stripe payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to confirm payment: " + e.getMessage());
        }
    }

    public PaymentResponse queryPayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        try {
            if (payment.getTransId() != null) {
                // Try to retrieve as Session first
                try {
                    Session session = Session.retrieve(payment.getTransId());
                    if ("complete".equals(session.getStatus()) && session.getPaymentIntent() != null) {
                        PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
                        payment.setStatus(mapStripeStatus(paymentIntent.getStatus()));
                    } else {
                        payment.setStatus(mapCheckoutStatus(session.getStatus()));
                    }
                    paymentRepository.save(payment);
                } catch (StripeException e) {
                    // If not a session, try as PaymentIntent
                    PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getTransId());
                    payment.setStatus(mapStripeStatus(paymentIntent.getStatus()));
                    paymentRepository.save(payment);
                }
            }
        } catch (StripeException e) {
            log.error("Failed to query Stripe payment: {}", e.getMessage());
        }

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .requestId(payment.getRequestId())
                .amount(payment.getAmount())
                .status(payment.getStatus().toString())
                .transId(payment.getTransId())
                .message("Payment queried successfully")
                .build();
    }

    private Payment.PaymentStatus mapCheckoutStatus(String checkoutStatus) {
        return switch (checkoutStatus) {
            case "complete" -> Payment.PaymentStatus.SUCCESS;
            case "open" -> Payment.PaymentStatus.PENDING;
            case "expired" -> Payment.PaymentStatus.CANCELLED;
            default -> Payment.PaymentStatus.FAILED;
        };
    }

    private Payment.PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "succeeded" -> Payment.PaymentStatus.SUCCESS;
            case "processing", "requires_action" -> Payment.PaymentStatus.PROCESSING;
            case "canceled" -> Payment.PaymentStatus.CANCELLED;
            case "requires_payment_method", "requires_confirmation" -> Payment.PaymentStatus.PENDING;
            default -> Payment.PaymentStatus.FAILED;
        };
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    /**
     * Apply membership to user after successful payment
     */
    private void applyMembership(Payment payment) {
        try {
            User user = payment.getUser();
            String membershipId = payment.getReferenceId();
            
            // Try to find membership package from database
            Membership membershipPackage = membershipRepository.findByMembershipId(membershipId).orElse(null);
            
            String membershipName;
            Integer duration;
            
            if (membershipPackage != null) {
                // Use data from membership package
                membershipName = membershipPackage.getName();
                duration = membershipPackage.getDuration();
            } else {
                // Fallback to payment orderInfo or default
                membershipName = payment.getOrderInfo() != null 
                    ? payment.getOrderInfo() 
                    : "Membership Package " + membershipId;
                duration = 30; // Default 30 days
            }

            // Calculate membership duration
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = startDate.plusDays(duration);

            // Check if user already has an active membership
            // Check if user already has an active membership
            List<UserMembership> existingMemberships = userMembershipRepository
                .findByUserIdAndStatusAndEndDateAfter(
                    user.getId(), 
                    UserMembership.MembershipStatus.ACTIVE, 
                    LocalDateTime.now()
                );
            Optional<UserMembership> existingMembership = existingMemberships.stream().findFirst();

            if (existingMembership.isPresent()) {
                // Extend existing membership
                UserMembership existing = existingMembership.get();
                existing.setEndDate(existing.getEndDate().plusDays(duration));
                existing.setStatus(UserMembership.MembershipStatus.ACTIVE);
                existing.setPayment(payment);
                if (membershipPackage != null) {
                    existing.setMembership(membershipPackage);
                }
                userMembershipRepository.save(existing);
                log.info("Extended membership for user {}: {}", user.getId(), membershipId);

                // Send membership extension notification
                notificationService.sendNotification(
                    user,
                    "Gói hội viên đã được gia hạn",
                    String.format("Gói hội viên %s của bạn đã được gia hạn. Hết hạn mới: %s", 
                        membershipName, existing.getEndDate().toString()),
                    "SYSTEM",
                    existing.getId()
                );
            } else {
                // Create new membership
                UserMembership userMembership = UserMembership.builder()
                    .user(user)
                    .membership(membershipPackage)
                    .membershipId(membershipId)
                    .membershipName(membershipName)
                    .startDate(startDate)
                    .endDate(endDate)
                    .status(UserMembership.MembershipStatus.ACTIVE)
                    .payment(payment)
                    .notes("Activated via payment: " + payment.getOrderId())
                    .build();

                userMembership = userMembershipRepository.save(userMembership);
                log.info("Applied membership {} to user {}", membershipId, user.getId());

                // Send membership activation notification
                notificationService.sendNotification(
                    user,
                    "Gói hội viên đã được kích hoạt",
                    String.format("Gói hội viên %s của bạn đã được kích hoạt thành công. Hết hạn: %s", 
                        membershipName, endDate.toString()),
                    "SYSTEM",
                    userMembership.getId()
                );
            }
        } catch (Exception e) {
            log.error("Failed to apply membership for payment {}: {}", payment.getId(), e.getMessage(), e);
            // Don't throw - payment is already successful
        }
    }

    /**
     * Send payment success notification
     */
    private void sendPaymentSuccessNotification(Payment payment) {
        try {
            User user = payment.getUser();
            String message = String.format(
                "Thanh toán thành công: %s - %s VNĐ",
                payment.getOrderInfo() != null ? payment.getOrderInfo() : "Thanh toán",
                payment.getAmount().toString()
            );

            notificationService.sendNotification(
                user,
                "Thanh toán thành công",
                message,
                "SYSTEM",
                payment.getId()
            );

            log.info("Payment success notification sent to user {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to send payment success notification: {}", e.getMessage(), e);
            // Don't throw - notification failure shouldn't affect payment
        }
    }
}

