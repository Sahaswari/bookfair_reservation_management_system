package com.bookfair.stall_service.service;

import com.bookfair.stall_service.dto.CreateEventRequest;
import com.bookfair.stall_service.dto.EventDTO;
import com.bookfair.stall_service.entity.Event;
import com.bookfair.stall_service.entity.EventStatus;
import com.bookfair.stall_service.repository.EventRepository;
import com.bookfair.stall_service.repository.StallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Event operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final StallRepository stallRepository;

    /**
     * Create a new event
     */
    public EventDTO createEvent(CreateEventRequest request) {
        log.info("Creating new event: {}", request.getName());
        
        // Check if event name already exists
        if (eventRepository.existsByName(request.getName())) {
            throw new RuntimeException("Event with name '" + request.getName() + "' already exists");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }

        Event event = new Event();
        event.setYear(request.getYear());
        event.setName(request.getName());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setStatus(request.getStatus() != null ? request.getStatus() : EventStatus.UPCOMING);

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with ID: {}", savedEvent.getId());

        return convertToDTO(savedEvent);
    }

    /**
     * Get all events
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getAllEvents() {
        log.info("Fetching all events");
        return eventRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get event by ID
     */
    @Transactional(readOnly = true)
    public EventDTO getEventById(UUID eventId) {
        log.info("Fetching event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
        return convertToDTO(event);
    }

    /**
     * Get events by year
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsByYear(Integer year) {
        log.info("Fetching events for year: {}", year);
        return eventRepository.findByYear(year).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get events by status
     */
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsByStatus(EventStatus status) {
        log.info("Fetching events with status: {}", status);
        return eventRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update event
     */
    public EventDTO updateEvent(UUID eventId, CreateEventRequest request) {
        log.info("Updating event with ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
        
        // Check if event name is being changed and if new name already exists
        if (!event.getName().equals(request.getName()) && 
            eventRepository.existsByName(request.getName())) {
            throw new RuntimeException("Event with name '" + request.getName() + "' already exists");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date cannot be before start date");
        }

        // Update event fields
        event.setYear(request.getYear());
        event.setName(request.getName());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully with ID: {}", updatedEvent.getId());

        return convertToDTO(updatedEvent);
    }

    /**
     * Update event status
     */
    public EventDTO updateEventStatus(UUID eventId, EventStatus status) {
        log.info("Updating event {} status to {}", eventId, status);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
        
        event.setStatus(status);
        Event updatedEvent = eventRepository.save(event);
        
        return convertToDTO(updatedEvent);
    }

    /**
     * Delete event
     */
    public void deleteEvent(UUID eventId) {
        log.info("Deleting event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
        
        // Check if event has reserved stalls
        long reservedStalls = stallRepository.findReservedStallsByEventId(eventId).size();
        if (reservedStalls > 0) {
            throw new RuntimeException("Cannot delete event with reserved stalls");
        }
        
        eventRepository.delete(event);
        log.info("Event deleted successfully");
    }

    /**
     * Convert Event entity to DTO
     */
    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setYear(event.getYear());
        dto.setName(event.getName());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setLocation(event.getLocation());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        
        // Count stalls
        long totalStalls = stallRepository.findByEventId(event.getId()).size();
        long availableStalls = stallRepository.countAvailableStallsByEventId(event.getId());
        dto.setTotalStalls(totalStalls);
        dto.setAvailableStalls(availableStalls);
        
        return dto;
    }
}
