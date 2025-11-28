package com.ridemate.ridemate_server.application.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to submit a report")
public class CreateReportRequest {

    @Schema(description = "ID of the match related to this report (optional)", example = "1")
    private Long matchId;

    @Schema(description = "ID of the user being reported (optional)", example = "2")
    private Long reportedUserId;

    @NotBlank(message = "Title is required")
    @Schema(example = "Tài xế lái xe quá nhanh")
    private String title;

    @NotBlank(message = "Description is required")
    @Schema(example = "Tài xế liên tục vượt đèn đỏ và đi quá tốc độ cho phép.")
    private String description;

    @NotNull(message = "Category is required")
    @Schema(example = "SAFETY", allowableValues = {"SAFETY", "BEHAVIOR", "LOST_ITEM", "PAYMENT", "APP_ISSUE", "OTHER"})
    private String category;

    @Schema(description = "URL of evidence image (optional)", example = "https://res.cloudinary.com/...")
    private String evidenceUrl;
}