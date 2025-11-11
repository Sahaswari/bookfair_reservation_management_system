package com.bookfair.stall_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bookfair.stall_service.entity.StallSize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Stall responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StallDTO {
    private UUID id;
    private UUID eventId;
    private String eventName;
    private String stallCode;
    private StallSize sizeCategory;
    private BigDecimal price;
    private Float locationX;
    private Float locationY;
    private Boolean isReserved;
    private UUID reservedBy;
    private String reservedByName; // Cached from user snapshot
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
