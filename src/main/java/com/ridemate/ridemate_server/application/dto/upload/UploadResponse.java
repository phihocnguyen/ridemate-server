package com.ridemate.ridemate_server.application.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "File upload response")
public class UploadResponse {

    @Schema(description = "Uploaded file URL", example = "https://res.cloudinary.com/...")
    private String url;

    @Schema(description = "File type", example = "image/jpeg")
    private String fileType;

    @Schema(description = "File size in bytes", example = "1024000")
    private Long fileSize;

    @Schema(description = "Upload folder", example = "ridemate/vehicles/documents")
    private String folder;
}

