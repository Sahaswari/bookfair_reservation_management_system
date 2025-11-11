package com.bookfair.stall_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookfair.stall_service.entity.Event;
import com.bookfair.stall_service.entity.EventStatus;

/**
 * Repository for Event entity
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    
    // Find events by year
    List<Event> findByYear(Integer year);
    
    // Find events by status
    List<Event> findByStatus(EventStatus status);
    
    // Find event by name
    Optional<Event> findByName(String name);
    
    // Find upcoming and ongoing events
    List<Event> findByStatusIn(List<EventStatus> statuses);
    
    // Check if event exists by name
    boolean existsByName(String name);
}
