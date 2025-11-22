package com.bookfair.reservation_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StallSnapshot Entity
 * Caches stall information from Stall Service
 */
@Entity
@Table(name = "stall_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StallSnapshot {

    @Id
    @Column(name = "stall_id")
    private UUID stallId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "stall_code", length = 10)
    private String stallCode;

    @Column(name = "size_category", length = 20)
    private String sizeCategory;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "location_x")
    private Float locationX;

    @Column(name = "location_y")
    private Float locationY;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
