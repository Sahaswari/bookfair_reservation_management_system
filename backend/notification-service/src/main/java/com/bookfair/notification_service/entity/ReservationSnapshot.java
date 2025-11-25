package com.bookfair.notification_service.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ReservationSnapshot entity
 * Stores a snapshot of reservation data received from Kafka events
 * Used for notification generation and cross-service data synchronization
 */
@Entity
@Table(name = "reservation_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSnapshot implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "reservation_id", nullable = false, unique = true)
    private UUID reservationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "stall_id", nullable = false)
    private UUID stallId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @Column(name = "status", length = 20)
    private String status; // PENDING, CONFIRMED, CANCELLED

    @Column(name = "confirmation_code", length = 50)
    private String confirmationCode;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    // User snapshot fields
    @Column(name = "user_first_name", length = 100)
    private String userFirstName;

    @Column(name = "user_last_name", length = 100)
    private String userLastName;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "user_status", length = 50)
    private String userStatus;

    // Stall snapshot fields
    @Column(name = "stall_code", length = 50)
    private String stallCode;

    @Column(name = "size_category", length = 50)
    private String sizeCategory;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "location_x")
    private Float locationX;

    @Column(name = "location_y")
    private Float locationY;

    @Column(name = "updated_at", nullable = false, updatable = true)
    private Instant updatedAt;
}
