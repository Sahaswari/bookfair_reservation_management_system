package com.bookfair.stall_service.controller;

import com.bookfair.stall_service.dto.ApiResponse;
import com.bookfair.stall_service.dto.CreateEventRequest;
import com.bookfair.stall_service.dto.EventDTO;
import com.bookfair.stall_service.entity.EventStatus;
import com.bookfair.stall_service.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Event operations
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;

    /**
     * Create a new event
     * POST /api/events
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        try {
            EventDTO event = eventService.createEvent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Event created successfully", event));
        } catch (Exception e) {
            log.error("Error creating event", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all events
     * GET /api/events
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventDTO>>> getAllEvents() {
        try {
            List<EventDTO> events = eventService.getAllEvents();
            return ResponseEntity.ok(ApiResponse.success("Events fetched successfully", events));
        } catch (Exception e) {
            log.error("Error fetching events", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get event by ID
     * GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable UUID id) {
        try {
            EventDTO event = eventService.getEventById(id);
            return ResponseEntity.ok(ApiResponse.success("Event fetched successfully", event));
        } catch (Exception e) {
            log.error("Error fetching event", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get events by year
     * GET /api/events/year/{year}
     */
    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventsByYear(@PathVariable Integer year) {
        try {
            List<EventDTO> events = eventService.getEventsByYear(year);
            return ResponseEntity.ok(ApiResponse.success("Events fetched successfully", events));
        } catch (Exception e) {
            log.error("Error fetching events by year", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get events by status
     * GET /api/events/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EventDTO>>> getEventsByStatus(@PathVariable EventStatus status) {
        try {
            List<EventDTO> events = eventService.getEventsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("Events fetched successfully", events));
        } catch (Exception e) {
            log.error("Error fetching events by status", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update event
     * PUT /api/events/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEventRequest request) {
        try {
            EventDTO event = eventService.updateEvent(id, request);
            return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
        } catch (Exception e) {
            log.error("Error updating event", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update event status
     * PATCH /api/events/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EventDTO>> updateEventStatus(
            @PathVariable UUID id,
            @RequestParam EventStatus status) {
        try {
            EventDTO event = eventService.updateEventStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Event status updated successfully", event));
        } catch (Exception e) {
            log.error("Error updating event status", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete event
     * DELETE /api/events/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable UUID id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting event", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
