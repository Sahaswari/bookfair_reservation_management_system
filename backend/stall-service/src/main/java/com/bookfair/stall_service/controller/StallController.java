package com.bookfair.stall_service.controller;

import com.bookfair.stall_service.dto.ApiResponse;
import com.bookfair.stall_service.dto.CreateStallRequest;
import com.bookfair.stall_service.dto.StallDTO;
import com.bookfair.stall_service.entity.StallSize;
import com.bookfair.stall_service.service.StallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Stall operations
 */
@RestController
@RequestMapping("/api/stalls")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StallController {

    private final StallService stallService;

    /**
     * Create a new stall
     * POST /api/stalls
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StallDTO>> createStall(@Valid @RequestBody CreateStallRequest request) {
        try {
            StallDTO stall = stallService.createStall(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Stall created successfully", stall));
        } catch (Exception e) {
            log.error("Error creating stall", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all stalls
     * GET /api/stalls
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StallDTO>>> getAllStalls() {
        try {
            List<StallDTO> stalls = stallService.getAllStalls();
            return ResponseEntity.ok(ApiResponse.success("Stalls fetched successfully", stalls));
        } catch (Exception e) {
            log.error("Error fetching stalls", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get stall by ID
     * GET /api/stalls/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StallDTO>> getStallById(@PathVariable UUID id) {
        try {
            StallDTO stall = stallService.getStallById(id);
            return ResponseEntity.ok(ApiResponse.success("Stall fetched successfully", stall));
        } catch (Exception e) {
            log.error("Error fetching stall", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get stalls by event ID
     * GET /api/stalls/event/{eventId}
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<StallDTO>>> getStallsByEventId(@PathVariable UUID eventId) {
        try {
            List<StallDTO> stalls = stallService.getStallsByEventId(eventId);
            return ResponseEntity.ok(ApiResponse.success("Stalls fetched successfully", stalls));
        } catch (Exception e) {
            log.error("Error fetching stalls by event", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get available stalls by event ID
     * GET /api/stalls/event/{eventId}/available
     */
    @GetMapping("/event/{eventId}/available")
    public ResponseEntity<ApiResponse<List<StallDTO>>> getAvailableStallsByEventId(@PathVariable UUID eventId) {
        try {
            List<StallDTO> stalls = stallService.getAvailableStallsByEventId(eventId);
            return ResponseEntity.ok(ApiResponse.success("Available stalls fetched successfully", stalls));
        } catch (Exception e) {
            log.error("Error fetching available stalls", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get available stalls by event ID and size
     * GET /api/stalls/event/{eventId}/available/size/{size}
     */
    @GetMapping("/event/{eventId}/available/size/{size}")
    public ResponseEntity<ApiResponse<List<StallDTO>>> getAvailableStallsByEventIdAndSize(
            @PathVariable UUID eventId,
            @PathVariable StallSize size) {
        try {
            List<StallDTO> stalls = stallService.getAvailableStallsByEventIdAndSize(eventId, size);
            return ResponseEntity.ok(ApiResponse.success("Available stalls fetched successfully", stalls));
        } catch (Exception e) {
            log.error("Error fetching available stalls by size", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Reserve a stall
     * POST /api/stalls/{id}/reserve
     */
    @PostMapping("/{id}/reserve")
    public ResponseEntity<ApiResponse<StallDTO>> reserveStall(
            @PathVariable UUID id,
            @RequestParam UUID vendorId) {
        try {
            StallDTO stall = stallService.reserveStall(id, vendorId);
            return ResponseEntity.ok(ApiResponse.success("Stall reserved successfully", stall));
        } catch (Exception e) {
            log.error("Error reserving stall", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Unreserve a stall
     * POST /api/stalls/{id}/unreserve
     */
    @PostMapping("/{id}/unreserve")
    public ResponseEntity<ApiResponse<StallDTO>> unreserveStall(@PathVariable UUID id) {
        try {
            StallDTO stall = stallService.unreserveStall(id);
            return ResponseEntity.ok(ApiResponse.success("Stall unreserved successfully", stall));
        } catch (Exception e) {
            log.error("Error unreserving stall", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update stall
     * PUT /api/stalls/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StallDTO>> updateStall(
            @PathVariable UUID id,
            @Valid @RequestBody CreateStallRequest request) {
        try {
            StallDTO stall = stallService.updateStall(id, request);
            return ResponseEntity.ok(ApiResponse.success("Stall updated successfully", stall));
        } catch (Exception e) {
            log.error("Error updating stall", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get stalls by vendor
     * GET /api/stalls/vendor/{vendorId}
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<StallDTO>>> getStallsByVendor(@PathVariable UUID vendorId) {
        try {
            List<StallDTO> stalls = stallService.getStallsByVendor(vendorId);
            return ResponseEntity.ok(ApiResponse.success("Vendor stalls fetched successfully", stalls));
        } catch (Exception e) {
            log.error("Error fetching vendor stalls", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete stall
     * DELETE /api/stalls/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStall(@PathVariable UUID id) {
        try {
            stallService.deleteStall(id);
            return ResponseEntity.ok(ApiResponse.success("Stall deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting stall", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
