package com.bookfair.reservation_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reservation Entity
 * Represents a stall reservation made by a vendor
 */
@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "stall_id", nullable = false)
    private UUID stallId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "confirmation_code", length = 255)
    private String confirmationCode;

    @Column(name = "qr_code_url", length = 255)
    private String qrCodeUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships with snapshot tables
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserSnapshot userSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stall_id", insertable = false, updatable = false)
    private StallSnapshot stallSnapshot;

    /**
     * Helper method to set status to PENDING
     */
    public void setPending() {
        this.status = ReservationStatus.PENDING;
    }

    /**
     * Helper method to confirm reservation
     */
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    /**
     * Helper method to cancel reservation
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
