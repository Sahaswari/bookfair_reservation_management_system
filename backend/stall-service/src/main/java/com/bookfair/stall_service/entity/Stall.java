package com.bookfair.stall_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stall Entity
 * Represents a stall at a book fair event
 */
@Entity
@Table(name = "stalls", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"stall_code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stall {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Foreign key to Event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "stall_code", nullable = false, length = 10, unique = true)
    private String stallCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "size_category", nullable = false, length = 20)
    private StallSize sizeCategory;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "location_x")
    private Float locationX;

    @Column(name = "location_y")
    private Float locationY;

    @Column(name = "is_reserved", nullable = false)
    private Boolean isReserved = false;

    // UUID reference to the vendor who reserved (from auth-service)
    @Column(name = "reserved_by")
    private UUID reservedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void reserve(UUID vendorId) {
        this.isReserved = true;
        this.reservedBy = vendorId;
    }

    public void unreserve() {
        this.isReserved = false;
        this.reservedBy = null;
    }
}
