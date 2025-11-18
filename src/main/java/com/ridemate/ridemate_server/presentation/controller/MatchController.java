package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.match.BookRideRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.service.match.MatchService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Ride booking and management")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping("/book")
    @Operation(summary = "Book a new ride", description = "Passenger requests a ride")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ride booked successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<MatchResponse>> bookRide(
            @Valid @RequestBody BookRideRequest request,
            @AuthenticationPrincipal Long userId) {
        
        MatchResponse response = matchService.bookRide(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ride booked successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match details", description = "Get details of a specific match")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<MatchResponse>> getMatchById(@PathVariable Long id) {
        MatchResponse response = matchService.getMatchById(id);
        return ResponseEntity.ok(ApiResponse.success("Match details retrieved", response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get ride history", description = "Get history of rides for current user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getHistory(@AuthenticationPrincipal Long userId) {
        List<MatchResponse> response = matchService.getMyHistory(userId);
        return ResponseEntity.ok(ApiResponse.success("History retrieved", response));
    }
}