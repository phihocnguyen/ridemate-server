package com.ridemate.ridemate_server.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideMate API")
                        .version("1.0.0")
                        .description("RideMate Server - Ride Sharing Application API\n\n" +
                                "**PUBLIC Endpoints** (No token required):\n" +
                                "- POST /auth/register/initiate\n" +
                                "- POST /auth/verify-otp\n" +
                                "- POST /auth/register/complete\n" +
                                "- POST /auth/login\n" +
                                "- POST /auth/social-login\n" +
                                "- POST /auth/refresh-token\n\n" +
                                "**PROTECTED Endpoints** (Token required):\n" +
                                "Click 'Authorize' button at top and paste JWT accessToken from login/register response")
                        .contact(new Contact()
                                .name("RideMate Team")
                                .email("support@ridemate.com")
                                .url("https://ridemate.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token from /auth/login or /auth/register/complete response")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
