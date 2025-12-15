package com.ridemate.ridemate_server.application.service.auth.impl;

import com.ridemate.ridemate_server.application.dto.auth.*;
import com.ridemate.ridemate_server.application.security.jwt.JwtTokenProvider;
import com.ridemate.ridemate_server.application.service.auth.AuthService;
import com.ridemate.ridemate_server.application.service.auth.SocialAuthValidator;
import com.ridemate.ridemate_server.application.service.notification.OtpNotificationService;
import com.ridemate.ridemate_server.domain.entity.OTP;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.OTPRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OTPRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SocialAuthValidator socialAuthValidator;

    @Autowired
    private OtpNotificationService otpNotificationService;

    @Value("${STREAM_API_KEY}")
    private String streamApiKey;

    @Value("${STREAM_API_SECRET}")
    private String streamApiSecret;

    @Override
    @Transactional
    public OtpResponse initiateRegistration(SendOtpRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        return sendOtp(SendOtpRequest.builder()
                .phoneNumber(request.getPhoneNumber())
                .purpose("REGISTER")
                .build());
    }

    @Override
    @Transactional
    public AuthResponse completeRegistration(CompleteRegistrationRequest request) {
        OTP otp = otpRepository.findByIdentifierAndPurpose(
                request.getPhoneNumber(), 
                OTP.OTPPurpose.REGISTER
        ).orElseThrow(() -> new IllegalArgumentException("No OTP verification found for this phone number"));

        if (!otp.getIsVerified()) {
            throw new IllegalArgumentException("Phone number has not been verified with OTP");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .profilePictureUrl(request.getProfilePictureUrl())
                .faceIdData(request.getFaceIdData())
                .currentLatitude(request.getCurrentLatitude())
                .currentLongitude(request.getCurrentLongitude())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .authProvider(User.AuthProvider.LOCAL)
                .userType(User.UserType.valueOf(request.getUserType() != null ? 
                        request.getUserType() : "PASSENGER"))
                .rating(0f)
                .coins(0)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getUserType().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getPhoneNumber(), user.getUserType().toString());

        log.info("User registered successfully: {}", user.getPhoneNumber());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    @Deprecated(since = "1.1", forRemoval = true)
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .profilePictureUrl(request.getProfilePictureUrl())
                .faceIdData(request.getFaceIdData())
                .currentLatitude(request.getCurrentLatitude())
                .currentLongitude(request.getCurrentLongitude())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .authProvider(User.AuthProvider.LOCAL)
                .userType(User.UserType.valueOf(request.getUserType() != null ? 
                        request.getUserType() : "PASSENGER"))
                .rating(0f)
                .coins(0)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getUserType().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getPhoneNumber(), user.getUserType().toString());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invalid phone number or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid phone number or password");
        }

        // ===== AUTO SET DRIVER TO ONLINE ON LOGIN =====
        if (user.getUserType() == User.UserType.DRIVER) {
            // Update driver status to ONLINE
            user.setDriverStatus(User.DriverStatus.ONLINE);
            
            // Set/update location if provided in request, or use existing
            if (request.getCurrentLatitude() != null && request.getCurrentLongitude() != null) {
                user.setCurrentLatitude(request.getCurrentLatitude());
                user.setCurrentLongitude(request.getCurrentLongitude());
            } else if (user.getCurrentLatitude() == null) {
                // Default to Ho Chi Minh center if no location ever set
                user.setCurrentLatitude(10.7769);
                user.setCurrentLongitude(106.7009);
            }
            
            user.setLastLocationUpdate(java.time.LocalDateTime.now());
            log.info("Driver {} auto set to ONLINE on login at location ({}, {})", 
                    user.getId(), user.getCurrentLatitude(), user.getCurrentLongitude());
        }

        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getUserType().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getPhoneNumber(), user.getUserType().toString());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getPhoneNumber(), user.getUserType().toString());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getPhoneNumber(), user.getUserType().toString());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public OtpResponse sendOtp(SendOtpRequest request) {
        String otpCode = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        OTP.OTPPurpose purpose = OTP.OTPPurpose.valueOf(request.getPurpose());

        var existingOtp = otpRepository.findByIdentifierAndPurpose(request.getPhoneNumber(), purpose);
        
        OTP otp;
        if (existingOtp.isPresent()) {
            otp = existingOtp.get();
            otp.setOtpCode(otpCode);
            otp.setExpiryTime(expiryTime);
            otp.setIsVerified(false);
        } else {
            otp = OTP.builder()
                    .identifier(request.getPhoneNumber())
                    .otpCode(otpCode)
                    .expiryTime(expiryTime)
                    .purpose(purpose)
                    .isVerified(false)
                    .build();
        }

        otpRepository.save(otp);

        otpNotificationService.sendOtpViaSms(request.getPhoneNumber(), otpCode, request.getPurpose());
        log.info("OTP sent to {} for purpose: {}", request.getPhoneNumber(), request.getPurpose());
        
        return OtpResponse.builder()
                .success(true)
                .message("OTP sent to " + request.getPhoneNumber())
                .expiryTime(expiryTime)
                .identifier(request.getPhoneNumber())
                .build();
    }

    @Override
    @Transactional
    public OtpResponse verifyOtp(VerifyOtpRequest request) {
        OTP.OTPPurpose purpose = OTP.OTPPurpose.valueOf(request.getPurpose());

        OTP otp = otpRepository.findByIdentifierAndPurpose(request.getPhoneNumber(), purpose)
                .orElseThrow(() -> new IllegalArgumentException("No OTP found for this phone number"));

        if (otp.getIsVerified()) {
            throw new IllegalArgumentException("OTP has already been verified");
        }

        if (LocalDateTime.now().isAfter(otp.getExpiryTime())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one");
        }

        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            throw new IllegalArgumentException("Invalid OTP code");
        }

        otp.setIsVerified(true);
        otpRepository.save(otp);

        log.info("OTP verified for {} with purpose: {}", request.getPhoneNumber(), request.getPurpose());

        return OtpResponse.builder()
                .success(true)
                .message("OTP verified successfully")
                .expiryTime(otp.getExpiryTime())
                .identifier(request.getPhoneNumber())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request) {
        try {
            SocialAuthValidator.SocialUserInfo userInfo = socialAuthValidator.extractUserInfo(request);

            User user = userRepository.findByProviderId(userInfo.getProviderId())
                    .orElse(null);

            if (user == null) {
                user = User.builder()
                        .fullName(request.getFullName() != null ? request.getFullName() : userInfo.getName())
                        .phoneNumber(null)
                        .email(userInfo.getEmail())
                        .profilePictureUrl(userInfo.getProfilePictureUrl())
                        .providerId(userInfo.getProviderId())
                        .authProvider(User.AuthProvider.valueOf(userInfo.getProvider()))
                        .userType(User.UserType.valueOf(request.getUserType() != null ? 
                                request.getUserType() : "PASSENGER"))
                        .currentLatitude(request.getCurrentLatitude())
                        .currentLongitude(request.getCurrentLongitude())
                        .passwordHash(null)
                        .rating(0f)
                        .coins(0)
                        .isActive(true)
                        .build();

                user = userRepository.save(user);
                log.info("New social user created: {} via {}", user.getEmail(), userInfo.getProvider());
            } else {
                if (request.getFullName() != null) {
                    user.setFullName(request.getFullName());
                }
                if (request.getCurrentLatitude() != null) {
                    user.setCurrentLatitude(request.getCurrentLatitude());
                }
                if (request.getCurrentLongitude() != null) {
                    user.setCurrentLongitude(request.getCurrentLongitude());
                }
                user = userRepository.save(user);
                log.info("Social user login: {}", user.getEmail());
            }

            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), 
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getEmail(), 
                    user.getUserType().toString());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), 
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getEmail(), 
                    user.getUserType().toString());

            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (Exception e) {
            log.error("Social login failed: {}", e.getMessage());
            throw new IllegalArgumentException("Social login failed: " + e.getMessage());
        }
    }
    
    private String generateStreamChatToken(Long userId) {
        try {
            
            if (streamApiSecret == null || streamApiSecret.isEmpty() || "default_secret_if_missing".equals(streamApiSecret)) {
                log.warn("STREAM_API_SECRET is not set in .env file. Chat token will be invalid.");
                return null;
            }
            
            SecretKey key = Keys.hmacShaKeyFor(streamApiSecret.getBytes(StandardCharsets.UTF_8));
            
            return Jwts.builder()
                    .claim("user_id", userId.toString())
                    .signWith(key, Jwts.SIG.HS256) 
                    .compact();
        } catch (Exception e) {
            log.error("Error generating Stream Chat token", e);
            return null;
        }
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        long expiresIn = 86400;
        
        String chatToken = generateStreamChatToken(user.getId());

        AuthResponse.UserDto userDto = AuthResponse.UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType().toString())
                .coins(user.getCoins())
                .rating(user.getRating())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .chatToken(chatToken) 
                .streamApiKey(streamApiKey) 
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userDto)
                .build();
    }
}