package com.ridemate.ridemate_server.application.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud.name:${CLOUDINARY_CLOUD_NAME:}}")
    private String cloudName;

    @Value("${cloudinary.api.key:${CLOUDINARY_API_KEY:}}")
    private String apiKey;

    @Value("${cloudinary.api.secret:${CLOUDINARY_API_SECRET:}}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Load from .env file first (most reliable)
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        String finalCloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
        String finalApiKey = dotenv.get("CLOUDINARY_API_KEY");
        String finalApiSecret = dotenv.get("CLOUDINARY_API_SECRET");
        
        // Also check system environment variables (for production)
        if (finalCloudName == null || finalCloudName.isEmpty()) {
            finalCloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        }
        if (finalApiKey == null || finalApiKey.isEmpty()) {
            finalApiKey = System.getenv("CLOUDINARY_API_KEY");
        }
        if (finalApiSecret == null || finalApiSecret.isEmpty()) {
            finalApiSecret = System.getenv("CLOUDINARY_API_SECRET");
        }
        
        // Fallback to @Value if still empty (from application.properties)
        if ((finalCloudName == null || finalCloudName.isEmpty()) && cloudName != null && !cloudName.isEmpty()) {
            finalCloudName = cloudName;
        }
        if ((finalApiKey == null || finalApiKey.isEmpty()) && apiKey != null && !apiKey.isEmpty()) {
            finalApiKey = apiKey;
        }
        if ((finalApiSecret == null || finalApiSecret.isEmpty()) && apiSecret != null && !apiSecret.isEmpty()) {
            finalApiSecret = apiSecret;
        }

        System.out.println("🔧 Cloudinary Config Loading:");
        System.out.println("   - Cloud Name: " + (finalCloudName != null && !finalCloudName.isEmpty() ? finalCloudName : "null"));
        System.out.println("   - API Key: " + (finalApiKey != null && !finalApiKey.isEmpty() ? finalApiKey.substring(0, Math.min(4, finalApiKey.length())) + "***" : "null"));
        System.out.println("   - API Secret: " + (finalApiSecret != null && !finalApiSecret.isEmpty() ? finalApiSecret.substring(0, Math.min(4, finalApiSecret.length())) + "***" : "null"));
        
        if (finalCloudName == null || finalCloudName.isEmpty() || 
            finalApiKey == null || finalApiKey.isEmpty() || 
            finalApiSecret == null || finalApiSecret.isEmpty()) {
            System.err.println("⚠️ WARNING: Cloudinary credentials are missing! Uploads will fail.");
            System.err.println("   Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET in .env file");
        } else {
            System.out.println("✅ Cloudinary credentials loaded successfully");
        }
        
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", finalCloudName);
        config.put("api_key", finalApiKey);
        config.put("api_secret", finalApiSecret);
        
        return new Cloudinary(config);
    }
}

