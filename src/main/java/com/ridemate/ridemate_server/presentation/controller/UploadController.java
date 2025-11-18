package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.upload.UploadResponse;
import com.ridemate.ridemate_server.application.service.cloudinary.CloudinaryService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/upload")
@Tag(name = "Upload", description = "File upload endpoints")
public class UploadController {

    private static final String UPLOAD_FOLDER = "images";

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/image")
    @Operation(
            summary = "Upload image to Cloudinary",
            description = "Upload an image file to Cloudinary. Files are stored in the 'images' folder. " +
                    "Supported file types: JPEG, PNG, GIF, etc."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Image uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file or file is empty"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Upload failed"
            )
    })
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "File cannot be null or empty"));
        }

        try {
            String url = cloudinaryService.uploadImage(file, UPLOAD_FOLDER);
            
            UploadResponse response = UploadResponse.builder()
                    .url(url)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .folder(UPLOAD_FOLDER)
                    .build();

            log.info("Image uploaded successfully to folder: {}, size: {} bytes", UPLOAD_FOLDER, file.getSize());
            
            return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
        } catch (Exception e) {
            log.error("Failed to upload image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to upload image: " + e.getMessage()));
        }
    }
}

