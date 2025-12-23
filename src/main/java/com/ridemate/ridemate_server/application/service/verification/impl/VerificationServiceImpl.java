package com.ridemate.ridemate_server.application.service.verification.impl;

import com.ridemate.ridemate_server.application.dto.verification.VerificationResponse;
import com.ridemate.ridemate_server.application.dto.verification.VerificationStatusResponse;
import com.ridemate.ridemate_server.application.service.cloudinary.CloudinaryService;
import com.ridemate.ridemate_server.application.service.face.FaceEmbeddingService;
import com.ridemate.ridemate_server.application.service.verification.VerificationService;
import com.ridemate.ridemate_server.domain.entity.TemporaryVerification;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.TemporaryVerificationRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TemporaryVerificationRepository tempVerificationRepository;

    @Autowired
    private FaceEmbeddingService faceEmbeddingService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Value("${face.similarity.threshold:0.65}")
    private float similarityThreshold;

    @Override
    @Transactional
    public VerificationResponse verifyIdCard(String phoneNumber, MultipartFile idCardImage) throws Exception {
        log.info("Processing ID card verification for phone: {}", phoneNumber);

        // Validate image contains a face
        boolean faceDetected = faceEmbeddingService.detectFace(idCardImage);
        if (!faceDetected) {
            log.warn("No face detected in ID card image for phone: {}", phoneNumber);
            return VerificationResponse.builder()
                    .status(User.VerificationStatus.REJECTED)
                    .message("Không phát hiện khuôn mặt trong ảnh căn cước. Vui lòng chụp lại.")
                    .verified(false)
                    .build();
        }

        // Generate face embedding from ID card
        float[] idCardEmbedding = faceEmbeddingService.generateEmbedding(idCardImage);
        
        // Upload ID card image to Cloudinary
        String idCardImageUrl = cloudinaryService.uploadImage(idCardImage, "id_cards");
        log.info("ID card image uploaded to: {}", idCardImageUrl);

        // Find or create user by phone number
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        
        if (user == null) {
            // Create temporary user record for verification
            user = User.builder()
                    .phoneNumber(phoneNumber)
                    .fullName("") // Will be filled during registration
                    .userType(User.UserType.PASSENGER)
                    .authProvider(User.AuthProvider.LOCAL)
                    .verificationStatus(User.VerificationStatus.PENDING)
                    .isActive(false) // Not active until registration complete
                    .build();
        }

        // Store ID card data
        user.setIdCardImageUrl(idCardImageUrl);
        user.setIdCardFaceEmbedding(faceEmbeddingService.embeddingToString(idCardEmbedding)); // Lưu embedding từ CCCD
        user.setVerificationStatus(User.VerificationStatus.PENDING);
        
        userRepository.save(user);
        log.info("ID card verification data saved for phone: {}", phoneNumber);

        return VerificationResponse.builder()
                .status(User.VerificationStatus.PENDING)
                .message("Ảnh căn cước đã được tải lên thành công. Vui lòng tiếp tục xác thực khuôn mặt.")
                .idCardImageUrl(idCardImageUrl)
                .verified(false)
                .build();
    }

    @Override
    @Transactional
    public VerificationResponse verifyLiveness(String phoneNumber, MultipartFile selfieImage) throws Exception {
        log.info("Processing liveness verification for phone: {}", phoneNumber);

        // Find user
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new Exception("Không tìm thấy thông tin căn cước. Vui lòng tải lên ảnh căn cước trước."));

        // Check if ID card was uploaded
        if (user.getIdCardFaceEmbedding() == null || user.getIdCardFaceEmbedding().isEmpty()) {
            throw new Exception("Chưa có ảnh căn cước. Vui lòng tải lên ảnh căn cước trước.");
        }

        // Validate selfie contains a face
        boolean faceDetected = faceEmbeddingService.detectFace(selfieImage);
        if (!faceDetected) {
            log.warn("No face detected in selfie for phone: {}", phoneNumber);
            return VerificationResponse.builder()
                    .status(User.VerificationStatus.REJECTED)
                    .message("Không phát hiện khuôn mặt trong ảnh selfie. Vui lòng thử lại.")
                    .verified(false)
                    .build();
        }

        // Generate embedding from selfie
        float[] selfieEmbedding = faceEmbeddingService.generateEmbedding(selfieImage);
        
        // Get ID card embedding (từ ảnh CCCD)
        float[] idCardEmbedding = faceEmbeddingService.stringToEmbedding(user.getIdCardFaceEmbedding());

        // Compare faces (so sánh CCCD với selfie)
        float similarityScore = faceEmbeddingService.compareFaces(idCardEmbedding, selfieEmbedding);
        log.info("Face similarity score for phone {}: {}", phoneNumber, similarityScore);

        // Check if similarity meets threshold
        boolean verified = similarityScore >= similarityThreshold;

        if (verified) {
            user.setVerificationStatus(User.VerificationStatus.VERIFIED);
            user.setVerificationDate(LocalDateTime.now());
            user.setVerificationSimilarityScore(similarityScore);
            user.setFaceEmbedding(faceEmbeddingService.embeddingToString(selfieEmbedding)); // Lưu embedding từ selfie
            userRepository.save(user);
            
            log.info("Liveness verification successful for phone: {}", phoneNumber);
            return VerificationResponse.builder()
                    .status(User.VerificationStatus.VERIFIED)
                    .message("Xác thực khuôn mặt thành công!")
                    .similarityScore(similarityScore)
                    .verified(true)
                    .build();
        } else {
            user.setVerificationStatus(User.VerificationStatus.REJECTED);
            user.setVerificationSimilarityScore(similarityScore);
            userRepository.save(user);
            
            log.warn("Liveness verification failed for phone: {} (score: {})", phoneNumber, similarityScore);
            return VerificationResponse.builder()
                    .status(User.VerificationStatus.REJECTED)
                    .message(String.format("Khuôn mặt không khớp với ảnh căn cước (độ tương đồng: %.2f%%). Vui lòng thử lại.", similarityScore * 100))
                    .similarityScore(similarityScore)
                    .verified(false)
                    .build();
        }
    }

    @Override
    public VerificationStatusResponse getVerificationStatus(String phoneNumber) {
        log.info("Getting verification status for phone: {}", phoneNumber);
        
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        
        if (user == null) {
            return VerificationStatusResponse.builder()
                    .verificationStatus(User.VerificationStatus.UNVERIFIED)
                    .idCardUploaded(false)
                    .livenessChecked(false)
                    .verified(false)
                    .build();
        }

        boolean idCardUploaded = user.getIdCardImageUrl() != null && !user.getIdCardImageUrl().isEmpty();
        boolean livenessChecked = user.getVerificationDate() != null;
        boolean verified = user.getVerificationStatus() == User.VerificationStatus.VERIFIED;

        return VerificationStatusResponse.builder()
                .verificationStatus(user.getVerificationStatus())
                .idCardUploaded(idCardUploaded)
                .livenessChecked(livenessChecked)
                .verified(verified)
                .verificationDate(user.getVerificationDate())
                .similarityScore(user.getVerificationSimilarityScore())
                .build();
    }
    
    @Override
    @Transactional
    public VerificationResponse verifyIdCardWithTempId(String tempId, MultipartFile idCardImage) throws Exception {
        log.info("Processing ID card verification with tempId: {}", tempId);
        
        // Validate image contains a face
        boolean faceDetected = faceEmbeddingService.detectFace(idCardImage);
        if (!faceDetected) {
            log.warn("No face detected in ID card image for tempId: {}", tempId);
            return VerificationResponse.builder()
                    .status(User.VerificationStatus.REJECTED)
                    .message("Không phát hiện khuôn mặt trong ảnh căn cước. Vui lòng chụp lại.")
                    .verified(false)
                    .build();
        }
        
        // Generate face embedding from ID card (KHÔNG lưu ảnh gốc)
        float[] idCardEmbedding = faceEmbeddingService.generateEmbedding(idCardImage);
        log.info("Face embedding extracted from ID card for tempId: {}", tempId);
        
        // Create temporary verification record (CHỈ lưu embedding)
        TemporaryVerification tempVerification = TemporaryVerification.builder()
                .tempId(tempId)
                .idCardFaceEmbedding(faceEmbeddingService.embeddingToString(idCardEmbedding))
                .livenessVerified(false)
                .build();
        
        tempVerificationRepository.save(tempVerification);
        log.info("Temporary verification created for tempId: {} (embedding only, no image stored)", tempId);
        
        return VerificationResponse.builder()
                .status(User.VerificationStatus.PENDING)
                .message("Khuôn mặt từ căn cước đã được xác thực. Vui lòng tiếp tục xác thực liveness.")
                .verified(false)
                .build();
    }
    
    @Override
    @Transactional
    public VerificationResponse verifyLivenessWithTempId(String tempId, MultipartFile selfieImage) throws Exception {
        log.info("Processing liveness verification with tempId: {}", tempId);
        
        // Find temporary verification
        TemporaryVerification tempVerification = tempVerificationRepository.findByTempId(tempId)
                .orElseThrow(() -> new Exception("Không tìm thấy thông tin căn cước. Vui lòng tải lên ảnh căn cước trước."));
        
        // Check if ID card was uploaded
        if (tempVerification.getIdCardFaceEmbedding() == null || tempVerification.getIdCardFaceEmbedding().isEmpty()) {
            throw new Exception("Chưa có ảnh căn cước. Vui lòng tải lên ảnh căn cước trước.");
        }
        
        // Validate selfie contains a face
        boolean faceDetected = faceEmbeddingService.detectFace(selfieImage);
        if (!faceDetected) {
            log.warn("No face detected in selfie for tempId: {}", tempId);
            return VerificationResponse.builder()
                    .status(User.VerificationStatus.REJECTED)
                    .message("Không phát hiện khuôn mặt trong ảnh selfie. Vui lòng thử lại.")
                    .verified(false)
                    .build();
        }
        
        // Generate embedding from selfie
        float[] selfieEmbedding = faceEmbeddingService.generateEmbedding(selfieImage);
        
        // Get ID card embedding
        float[] idCardEmbedding = faceEmbeddingService.stringToEmbedding(tempVerification.getIdCardFaceEmbedding());
        
        // Compare faces
        float similarityScore = faceEmbeddingService.compareFaces(idCardEmbedding, selfieEmbedding);
        log.info("Face similarity score for tempId {}: {}", tempId, similarityScore);
        
        // Check if similarity meets threshold
        boolean verified = similarityScore >= similarityThreshold;
        
        if (verified) {
            tempVerification.setLivenessVerified(true);
            tempVerification.setSimilarityScore(similarityScore);
            tempVerification.setSelfieFaceEmbedding(faceEmbeddingService.embeddingToString(selfieEmbedding));  // Save selfie embedding
            tempVerificationRepository.save(tempVerification);
            
            log.info("Liveness verification successful for tempId: {}", tempId);
            return VerificationResponse.builder()
                    .status(User.VerificationStatus.VERIFIED)
                    .message("Xác thực khuôn mặt thành công!")
                    .similarityScore(similarityScore)
                    .verified(true)
                    .build();
        } else {
            tempVerification.setSimilarityScore(similarityScore);
            tempVerificationRepository.save(tempVerification);
            
            log.warn("Liveness verification failed for tempId: {} (score: {})", tempId, similarityScore);
            return VerificationResponse.builder()
                    .status(User.VerificationStatus.REJECTED)
                    .message(String.format("Khuôn mặt không khớp với ảnh căn cước (độ tương đồng: %.2f%%). Vui lòng thử lại.", similarityScore * 100))
                    .similarityScore(similarityScore)
                    .verified(false)
                    .build();
        }
    }
    
    @Override
    @Transactional
    public void linkTempVerificationToUser(String tempId, String phoneNumber) throws Exception {
        log.info("Linking temporary verification {} to user {}", tempId, phoneNumber);
        
        // Find temporary verification
        TemporaryVerification tempVerification = tempVerificationRepository.findByTempId(tempId)
                .orElseThrow(() -> new Exception("Không tìm thấy dữ liệu xác thực tạm thời."));
        
        // Check if liveness was verified
        if (!Boolean.TRUE.equals(tempVerification.getLivenessVerified())) {
            throw new Exception("Chưa hoàn thành xác thực khuôn mặt.");
        }
        
        // Find or create user
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        
        if (user == null) {
            // Create new user record
            user = User.builder()
                    .phoneNumber(phoneNumber)
                    .fullName("") // Will be filled during registration
                    .userType(User.UserType.PASSENGER)
                    .authProvider(User.AuthProvider.LOCAL)
                    .verificationStatus(User.VerificationStatus.VERIFIED)
                    .isActive(false) // Not active until registration complete
                    .build();
        }
        
        // Transfer verification data to user (CHỈ embedding, KHÔNG có ảnh)
        user.setIdCardFaceEmbedding(tempVerification.getIdCardFaceEmbedding());  // CCCD embedding
        user.setFaceEmbedding(tempVerification.getSelfieFaceEmbedding());  // Selfie embedding (for future face recognition)
        user.setFaceIdData(tempVerification.getSelfieFaceEmbedding());  // Also save to faceIdData (avatar/profile)
        user.setVerificationStatus(User.VerificationStatus.VERIFIED);
        user.setVerificationDate(LocalDateTime.now());
        user.setVerificationSimilarityScore(tempVerification.getSimilarityScore());
        
        log.info("Transferred embeddings to user: CCCD={}, Selfie={}", 
                tempVerification.getIdCardFaceEmbedding() != null ? "✓" : "✗",
                tempVerification.getSelfieFaceEmbedding() != null ? "✓" : "✗");
        
        userRepository.save(user);
        
        // Delete temporary verification data
        tempVerificationRepository.delete(tempVerification);
        
        log.info("Successfully linked temp verification to user: {}", phoneNumber);
    }
}
