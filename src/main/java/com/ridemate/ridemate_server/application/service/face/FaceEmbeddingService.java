package com.ridemate.ridemate_server.application.service.face;

import org.springframework.web.multipart.MultipartFile;

public interface FaceEmbeddingService {
    
    /**
     * Generate face embedding from an image
     * @param image The image file containing a face
     * @return 512-dimensional face embedding as float array
     * @throws Exception if no face is detected or image processing fails
     */
    float[] generateEmbedding(MultipartFile image) throws Exception;
    
    /**
     * Compare two face embeddings using cosine similarity
     * @param embedding1 First face embedding
     * @param embedding2 Second face embedding
     * @return Similarity score between 0 and 1 (higher is more similar)
     */
    float compareFaces(float[] embedding1, float[] embedding2);
    
    /**
     * Detect if an image contains a face
     * @param image The image file to check
     * @return true if a face is detected, false otherwise
     */
    boolean detectFace(MultipartFile image) throws Exception;
    
    /**
     * Convert float array embedding to string format for database storage
     * @param embedding Face embedding as float array
     * @return String representation suitable for pgvector
     */
    String embeddingToString(float[] embedding);
    
    /**
     * Convert string embedding from database to float array
     * @param embeddingStr String representation from database
     * @return Face embedding as float array
     */
    float[] stringToEmbedding(String embeddingStr);
}
