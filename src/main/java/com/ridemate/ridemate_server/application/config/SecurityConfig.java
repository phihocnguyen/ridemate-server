package com.ridemate.ridemate_server.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridemate.ridemate_server.application.security.jwt.JwtAuthenticationFilter;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import com.ridemate.ridemate_server.presentation.exception.ExceptionHandlerFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            private final ObjectMapper objectMapper = new ObjectMapper();

            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                                 AuthenticationException authException) throws IOException {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());

                ApiResponse<Object> errorResponse = ApiResponse.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Authentication failed. Please provide valid credentials."
                );

                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            }
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            private final ObjectMapper objectMapper = new ObjectMapper();

            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                               AccessDeniedException accessDeniedException) throws IOException {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpStatus.FORBIDDEN.value());

                ApiResponse<Object> errorResponse = ApiResponse.error(
                        HttpStatus.FORBIDDEN.value(),
                        "Access denied. You don't have permission to access this resource."
                );

                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(
                                "/auth/register/**",
                                "/auth/login",
                                "/auth/refresh-token",
                                "/auth/send-otp",
                                "/auth/verify-otp",
                                "/auth/social-login"
                        ).permitAll()
                        
                        // Verification endpoints - public for registration flow
                        .requestMatchers(
                                "/verification/id-card",
                                "/verification/liveness",
                                "/verification/liveness/verify-phase",
                                "/verification/status/**",
                                "/verification/id-card-temp",
                                "/verification/liveness-temp",
                                "/verification/link-temp"
                        ).permitAll()

                        // Logout requires authentication
                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers("/upload/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Admin endpoints - protected by @PreAuthorize
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(new ExceptionHandlerFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
