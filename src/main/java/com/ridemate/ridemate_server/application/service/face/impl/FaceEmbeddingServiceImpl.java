package com.ridemate.ridemate_server.application.service.face.impl;

import com.ridemate.ridemate_server.application.service.face.FaceEmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;

@Service
@Slf4j
public class FaceEmbeddingServiceImpl implements FaceEmbeddingService {

    @Value("${face.service.url:http://localhost:5000}")
    private String faceServiceUrl;

    private final RestTemplate restTemplate;

    public FaceEmbeddingServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public float[] generateEmbedding(MultipartFile image) throws Exception {
        try {
            log.info("Generating face embedding for image: {}", image.getOriginalFilename());
            
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", image.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call Python face recognition service
            String url = faceServiceUrl + "/extract-embedding";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object embeddingObj = response.getBody().get("embedding");
                
                if (embeddingObj instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Number> embeddingList = (java.util.List<Number>) embeddingObj;
                    float[] embedding = new float[embeddingList.size()];
                    for (int i = 0; i < embeddingList.size(); i++) {
                        embedding[i] = embeddingList.get(i).floatValue();
                    }
                    log.info("Successfully generated embedding with {} dimensions", embedding.length);
                    return embedding;
                }
            }
            
            throw new Exception("Failed to generate face embedding from image");
        } catch (Exception e) {
            log.error("Error generating face embedding: {}", e.getMessage());
            throw new Exception("Face embedding generation failed: " + e.getMessage());
        }
    }

    @Override
    public float compareFaces(float[] embedding1, float[] embedding2) {
        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        // Calculate cosine similarity
        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }

        float similarity = dotProduct / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
        
        // Convert to 0-1 range (cosine similarity is -1 to 1)
        similarity = (similarity + 1) / 2;
        
        log.info("Face similarity score: {}", similarity);
        return similarity;
    }

    @Override
    public boolean detectFace(MultipartFile image) throws Exception {
        try {
            log.info("Detecting face in image: {}", image.getOriginalFilename());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", image.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = faceServiceUrl + "/detect-face";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object faceDetected = response.getBody().get("face_detected");
                boolean detected = faceDetected != null && (Boolean) faceDetected;
                log.info("Face detection result: {}", detected);
                return detected;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error detecting face: {}", e.getMessage());
            throw new Exception("Face detection failed: " + e.getMessage());
        }
    }

    @Override
    public String embeddingToString(float[] embedding) {
        // Convert to PostgreSQL vector format: [0.1,0.2,0.3,...]
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public float[] stringToEmbedding(String embeddingStr) {
        // Parse PostgreSQL vector format: [0.1,0.2,0.3,...]
        if (embeddingStr == null || embeddingStr.isEmpty()) {
            return null;
        }
        
        String cleaned = embeddingStr.replace("[", "").replace("]", "");
        String[] parts = cleaned.split(",");
        float[] embedding = new float[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Float.parseFloat(parts[i].trim());
        }
        
        return embedding;
    }
}
