package com.bookfair.stall_service.service;

import com.bookfair.stall_service.dto.CreateStallRequest;
import com.bookfair.stall_service.dto.StallDTO;
import com.bookfair.stall_service.entity.Event;
import com.bookfair.stall_service.entity.Stall;
import com.bookfair.stall_service.entity.StallSize;
import com.bookfair.stall_service.entity.UserSnapshot;
import com.bookfair.stall_service.repository.EventRepository;
import com.bookfair.stall_service.repository.StallRepository;
import com.bookfair.stall_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Stall operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StallService {

    private final StallRepository stallRepository;
    private final EventRepository eventRepository;
    private final UserSnapshotRepository userSnapshotRepository;

    /**
     * Create a new stall
     */
    public StallDTO createStall(CreateStallRequest request) {
        log.info("Creating new stall: {}", request.getStallCode());
        
        // Check if stall code already exists
        if (stallRepository.existsByStallCode(request.getStallCode())) {
            throw new RuntimeException("Stall with code '" + request.getStallCode() + "' already exists");
        }

        // Verify event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + request.getEventId()));

        Stall stall = new Stall();
        stall.setEvent(event);
        stall.setStallCode(request.getStallCode());
        stall.setSizeCategory(request.getSizeCategory());
        stall.setPrice(request.getPrice());
        stall.setLocationX(request.getLocationX());
        stall.setLocationY(request.getLocationY());
        stall.setIsReserved(false);

        Stall savedStall = stallRepository.save(stall);
        log.info("Stall created successfully with ID: {}", savedStall.getId());

        return convertToDTO(savedStall);
    }

    /**
     * Get all stalls
     */
    @Transactional(readOnly = true)
    public List<StallDTO> getAllStalls() {
        log.info("Fetching all stalls");
        return stallRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get stall by ID
     */
    @Transactional(readOnly = true)
    public StallDTO getStallById(UUID stallId) {
        log.info("Fetching stall with ID: {}", stallId);
        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found with ID: " + stallId));
        return convertToDTO(stall);
    }

    /**
     * Update an existing stall
     */
    public StallDTO updateStall(UUID id, CreateStallRequest request) {
        log.info("Updating stall with ID: {}", id);

        // Find existing stall
        Stall stall = stallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stall not found with ID: " + id));

        // Check if stall code is being changed to one that already exists
        log.info("Updating stall ID: {}, Current code: '{}', New code: '{}'", 
                 id, stall.getStallCode(), request.getStallCode());
        
        // Only validate if the stall code is actually changing
        if (!stall.getStallCode().equals(request.getStallCode())) {
            log.info("Stall code IS changing from '{}' to '{}', checking for duplicates...", 
                     stall.getStallCode(), request.getStallCode());
            // Check if the new code already exists for a DIFFERENT stall
            if (stallRepository.existsByStallCode(request.getStallCode())) {
                log.error("Cannot change code to '{}' - already exists", request.getStallCode());
                throw new RuntimeException("Stall with code '" + request.getStallCode() + "' already exists");
            }
            log.info("Code change validated - no duplicates found");
        } else {
            log.info("Stall code NOT changing (still '{}'), skipping duplicate check", stall.getStallCode());
        }

        // Verify event exists (if event is being changed)
        if (!stall.getEvent().getId().equals(request.getEventId())) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event not found with ID: " + request.getEventId()));
            stall.setEvent(event);
        }

        // Update stall fields
        stall.setStallCode(request.getStallCode());
        stall.setSizeCategory(request.getSizeCategory());
        stall.setPrice(request.getPrice());
        stall.setLocationX(request.getLocationX());
        stall.setLocationY(request.getLocationY());

        Stall updatedStall = stallRepository.save(stall);
        log.info("Stall updated successfully: {}", updatedStall.getStallCode());

        return convertToDTO(updatedStall);
    }

    /**
     * Get stalls by event ID
     */
    @Transactional(readOnly = true)
    public List<StallDTO> getStallsByEventId(UUID eventId) {
        log.info("Fetching stalls for event: {}", eventId);
        return stallRepository.findByEventId(eventId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available stalls by event ID
     */
    @Transactional(readOnly = true)
    public List<StallDTO> getAvailableStallsByEventId(UUID eventId) {
        log.info("Fetching available stalls for event: {}", eventId);
        return stallRepository.findAvailableStallsByEventId(eventId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available stalls by event ID and size
     */
    @Transactional(readOnly = true)
    public List<StallDTO> getAvailableStallsByEventIdAndSize(UUID eventId, StallSize size) {
        log.info("Fetching available {} stalls for event: {}", size, eventId);
        return stallRepository.findAvailableStallsByEventIdAndSize(eventId, size).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Reserve a stall
     */
    public StallDTO reserveStall(UUID stallId, UUID vendorId) {
        log.info("Reserving stall {} for vendor {}", stallId, vendorId);
        
        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found with ID: " + stallId));

        if (stall.getIsReserved()) {
            throw new RuntimeException("Stall is already reserved");
        }

        stall.reserve(vendorId);
        Stall updatedStall = stallRepository.save(stall);
        
        log.info("Stall reserved successfully");
        return convertToDTO(updatedStall);
    }

    /**
     * Unreserve a stall
     */
    public StallDTO unreserveStall(UUID stallId) {
        log.info("Unreserving stall {}", stallId);
        
        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found with ID: " + stallId));

        if (!stall.getIsReserved()) {
            throw new RuntimeException("Stall is not reserved");
        }

        stall.unreserve();
        Stall updatedStall = stallRepository.save(stall);
        
        log.info("Stall unreserved successfully");
        return convertToDTO(updatedStall);
    }

    /**
     * Get stalls reserved by a vendor
     */
    @Transactional(readOnly = true)
    public List<StallDTO> getStallsByVendor(UUID vendorId) {
        log.info("Fetching stalls for vendor: {}", vendorId);
        return stallRepository.findByReservedBy(vendorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete stall
     */
    public void deleteStall(UUID stallId) {
        log.info("Deleting stall with ID: {}", stallId);
        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found with ID: " + stallId));
        
        if (stall.getIsReserved()) {
            throw new RuntimeException("Cannot delete a reserved stall");
        }
        
        stallRepository.delete(stall);
        log.info("Stall deleted successfully");
    }

    /**
     * Convert Stall entity to DTO
     */
    private StallDTO convertToDTO(Stall stall) {
        StallDTO dto = new StallDTO();
        dto.setId(stall.getId());
        dto.setEventId(stall.getEvent().getId());
        dto.setEventName(stall.getEvent().getName());
        dto.setStallCode(stall.getStallCode());
        dto.setSizeCategory(stall.getSizeCategory());
        dto.setPrice(stall.getPrice());
        dto.setLocationX(stall.getLocationX());
        dto.setLocationY(stall.getLocationY());
        dto.setIsReserved(stall.getIsReserved());
        dto.setReservedBy(stall.getReservedBy());
        dto.setCreatedAt(stall.getCreatedAt());
        dto.setUpdatedAt(stall.getUpdatedAt());
        
        // Get vendor name from user snapshot if available
        if (stall.getReservedBy() != null) {
            userSnapshotRepository.findById(stall.getReservedBy())
                    .ifPresent(user -> dto.setReservedByName(user.getFirstName() + " " + user.getLastName()));
        }
        
        return dto;
    }
}
