package com.ridemate.ridemate_server.application.service.auth;

import com.ridemate.ridemate_server.application.dto.auth.SocialLoginRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class SocialAuthValidator {

    private static final String GOOGLE_TOKENINFO_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=";
    private static final String FACEBOOK_DEBUG_TOKEN_URL = "https://graph.facebook.com/debug_token?input_token=";

    private final RestTemplate restTemplate;

    public SocialAuthValidator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GoogleTokenInfo validateGoogleToken(String token) {
        try {
            String url = GOOGLE_TOKENINFO_URL + token;
            GoogleTokenInfo tokenInfo = restTemplate.getForObject(url, GoogleTokenInfo.class);
            
            if (tokenInfo != null && tokenInfo.getExpires_in() > 0) {
                log.info("Google token validated for email: {}", tokenInfo.getEmail());
                return tokenInfo;
            } else {
                log.warn("Google token is invalid or expired");
                return null;
            }
        } catch (Exception e) {
            log.error("Error validating Google token: {}", e.getMessage());
            return null;
        }
    }

    public FacebookTokenInfo validateFacebookToken(String token, String appAccessToken) {
        try {
            String url = FACEBOOK_DEBUG_TOKEN_URL + token + "&access_token=" + appAccessToken;
            FacebookDebugResponse response = restTemplate.getForObject(url, FacebookDebugResponse.class);
            
            if (response != null && response.getData() != null && response.getData().isValid()) {
                log.info("Facebook token validated for user ID: {}", response.getData().getUser_id());
                return response.getData();
            } else {
                log.warn("Facebook token is invalid");
                return null;
            }
        } catch (Exception e) {
            log.error("Error validating Facebook token: {}", e.getMessage());
            return null;
        }
    }

    public SocialUserInfo extractUserInfo(SocialLoginRequest request) {
        if ("GOOGLE".equalsIgnoreCase(request.getProvider())) {
            GoogleTokenInfo tokenInfo = validateGoogleToken(request.getToken());
            if (tokenInfo != null) {
                return SocialUserInfo.builder()
                        .provider("GOOGLE")
                        .providerId(tokenInfo.getSub())
                        .email(tokenInfo.getEmail())
                        .name(tokenInfo.getName())
                        .profilePictureUrl(tokenInfo.getPicture())
                        .build();
            }
        } else if ("FACEBOOK".equalsIgnoreCase(request.getProvider())) {
            
            String appAccessToken = request.getAppAccessToken();
            FacebookTokenInfo tokenInfo = validateFacebookToken(request.getToken(), appAccessToken);
            if (tokenInfo != null) {
                return SocialUserInfo.builder()
                        .provider("FACEBOOK")
                        .providerId(tokenInfo.getUser_id())
                        .email(tokenInfo.getEmail())
                        .name(tokenInfo.getName())
                        .profilePictureUrl(tokenInfo.getPicture() != null ? 
                                tokenInfo.getPicture().getData().getUrl() : null)
                        .build();
            }
        }
        
        throw new IllegalArgumentException("Invalid provider or token");
    }

    @Data
    public static class GoogleTokenInfo {
        private String iss;
        private String azp;
        private String aud;
        private String sub;
        private String email;
        private boolean email_verified;
        private String name;
        private String picture;
        private String given_name;
        private String family_name;
        private long iat;
        private long exp;
        private int expires_in;
    }

    @Data
    public static class FacebookDebugResponse {
        private FacebookTokenInfo data;
    }

    @Data
    public static class FacebookTokenInfo {
        private String app_id;
        private String type;
        private String application;
        private long data_access_expires_at;
        private long expires_at;
        private boolean is_valid;
        private String[] scopes;
        private String user_id;
        private String email;
        private String name;
        private FacebookPictureData picture;

        public boolean isValid() {
            return is_valid;
        }
    }

    @Data
    public static class FacebookPictureData {
        private FacebookPicture data;
    }

    @Data
    public static class FacebookPicture {
        private int height;
        private String url;
        private int width;
    }

    @Data
    public static class SocialUserInfo {
        private String provider;
        private String providerId;
        private String email;
        private String name;
        private String profilePictureUrl;

        public static SocialUserInfoBuilder builder() {
            return new SocialUserInfoBuilder();
        }

        public static class SocialUserInfoBuilder {
            private String provider;
            private String providerId;
            private String email;
            private String name;
            private String profilePictureUrl;

            public SocialUserInfoBuilder provider(String provider) {
                this.provider = provider;
                return this;
            }

            public SocialUserInfoBuilder providerId(String providerId) {
                this.providerId = providerId;
                return this;
            }

            public SocialUserInfoBuilder email(String email) {
                this.email = email;
                return this;
            }

            public SocialUserInfoBuilder name(String name) {
                this.name = name;
                return this;
            }

            public SocialUserInfoBuilder profilePictureUrl(String profilePictureUrl) {
                this.profilePictureUrl = profilePictureUrl;
                return this;
            }

            public SocialUserInfo build() {
                SocialUserInfo info = new SocialUserInfo();
                info.provider = this.provider;
                info.providerId = this.providerId;
                info.email = this.email;
                info.name = this.name;
                info.profilePictureUrl = this.profilePictureUrl;
                return info;
            }
        }
    }
}
