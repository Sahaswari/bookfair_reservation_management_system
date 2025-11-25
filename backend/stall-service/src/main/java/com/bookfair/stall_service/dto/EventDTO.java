package com.bookfair.stall_service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bookfair.stall_service.entity.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Event responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private UUID id;
    private Integer year;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long totalStalls;
    private Long availableStalls;
}
