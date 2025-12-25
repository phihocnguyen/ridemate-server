package com.ridemate.ridemate_server.application.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Check if cloudinary is properly configured
        if (cloudinary == null) {
            log.error("❌ Cloudinary bean is null! Check CloudinaryConfig.");
            throw new IllegalStateException("Cloudinary service is not properly configured");
        }

        log.info("📤 Starting Cloudinary upload - folder: {}, size: {} bytes", folder, file.getSize());

        // Start with simple params to avoid signature issues
        Map<String, Object> params = new HashMap<>();
        params.put("folder", folder);
        params.put("resource_type", "image");
        params.put("overwrite", true);

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String secureUrl = (String) uploadResult.get("secure_url");
            
            log.info("✅ Cloudinary upload successful: {}", secureUrl);
            return secureUrl;
        } catch (Exception e) {
            log.error("❌ Cloudinary upload failed: {}", e.getMessage(), e);
            
            // Check if it's a signature error
            if (e.getMessage() != null && e.getMessage().contains("Invalid Signature")) {
                log.error("   🔐 Signature error detected!");
                log.error("   Please verify:");
                log.error("   1. CLOUDINARY_API_SECRET in .env matches Cloudinary dashboard");
                log.error("   2. No extra spaces or special characters in credentials");
                log.error("   3. API Secret is the full secret key (not truncated)");
            }
            
            // Check if it's a network/connection error
            if (e.getMessage() != null && (e.getMessage().contains("timeout") || e.getMessage().contains("connection"))) {
                log.error("   🌐 Network error - check internet connection");
            }
            
            // Re-throw exception instead of returning mock URL
            throw new IOException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        // Extract public_id from URL
        String publicId = extractPublicId(imageUrl);
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    private String extractPublicId(String imageUrl) {
        try {
            // Cloudinary URL format: https://res.cloudinary.com/{cloud_name}/image/upload/{version}/{public_id}.{format}
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                // Remove version if present (v1234567890/)
                if (path.contains("/v")) {
                    path = path.substring(path.indexOf("/", 1) + 1);
                }
                // Remove file extension
                int lastDot = path.lastIndexOf(".");
                if (lastDot > 0) {
                    path = path.substring(0, lastDot);
                }
                return path;
            }
        } catch (Exception e) {
            // Log error but don't throw
        }
        return null;
    }
}

