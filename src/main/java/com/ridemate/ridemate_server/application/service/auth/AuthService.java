package com.ridemate.ridemate_server.application.service.auth;

import com.ridemate.ridemate_server.application.dto.auth.*;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    OtpResponse initiateRegistration(SendOtpRequest request);
    AuthResponse completeRegistration(CompleteRegistrationRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    OtpResponse sendOtp(SendOtpRequest request);
    OtpResponse verifyOtp(VerifyOtpRequest request);
    AuthResponse socialLogin(SocialLoginRequest request);
    void logout(HttpServletRequest request);
}
