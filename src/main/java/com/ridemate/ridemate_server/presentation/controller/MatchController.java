package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.match.BookRideRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.dto.match.UpdateMatchStatusRequest;
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

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept a ride", description = "Driver accepts a waiting ride. Driver must have an APPROVED vehicle.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ride accepted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ride not available or User not a driver"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Match not found")
    })
    public ResponseEntity<ApiResponse<MatchResponse>> acceptRide(
            @PathVariable Long id,
            @AuthenticationPrincipal Long driverId) {
        
        MatchResponse response = matchService.acceptRide(id, driverId);
        return ResponseEntity.ok(ApiResponse.success("Ride accepted successfully", response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update match status", description = "Update the status of a ride (e.g., IN_PROGRESS, COMPLETED, CANCELLED)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status or transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized to update this match")
    })
    public ResponseEntity<ApiResponse<MatchResponse>> updateMatchStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMatchStatusRequest request,
            @AuthenticationPrincipal Long userId) {
        
        MatchResponse response = matchService.updateMatchStatus(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Match status updated successfully", response));
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

    