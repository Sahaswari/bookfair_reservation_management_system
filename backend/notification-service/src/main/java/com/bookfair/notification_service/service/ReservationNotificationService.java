package com.bookfair.notification_service.service;

import com.bookfair.notification_service.dto.NotificationRequest;
import com.bookfair.notification_service.entity.NotificationChannel;
import com.bookfair.notification_service.entity.ReservationSnapshot;
import com.bookfair.notification_service.entity.UserSnapshot;
import com.bookfair.notification_service.repository.ReservationSnapshotRepository;
import com.bookfair.notification_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling reservation-related notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationNotificationService {

    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final ReservationSnapshotRepository reservationSnapshotRepository;
    private final UserSnapshotRepository userSnapshotRepository;

    /**
     * Send notification when reservation is created
     */
    public void sendReservationCreatedNotification(UUID reservationId) {
        log.info("Sending reservation created notification for reservation: {}", reservationId);

        ReservationSnapshot reservation = reservationSnapshotRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        UserSnapshot user = userSnapshotRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + reservation.getUserId()));

        Map<String, Object> variables = buildReservationVariables(reservation, user);
        String message = templateService.renderTemplate("RESERVATION_CREATED", variables);

        NotificationRequest request = NotificationRequest.builder()
                .userId(reservation.getUserId())
                .reservationId(reservationId)
                .channel(NotificationChannel.EMAIL)
                .templateCode("RESERVATION_CREATED")
                .subject("Reservation Created Successfully")
                .message(message)
                .metadata(variables)
                .build();

        notificationService.createNotification(request);
    }

    /**
     * Send notification when reservation is confirmed
     */
    public void sendReservationConfirmedNotification(UUID reservationId) {
        log.info("Sending reservation confirmed notification for reservation: {}", reservationId);

        ReservationSnapshot reservation = reservationSnapshotRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        UserSnapshot user = userSnapshotRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + reservation.getUserId()));

        Map<String, Object> variables = buildReservationVariables(reservation, user);
        String message = templateService.renderTemplate("RESERVATION_CONFIRMED", variables);

        NotificationRequest request = NotificationRequest.builder()
                .userId(reservation.getUserId())
                .reservationId(reservationId)
                .channel(NotificationChannel.EMAIL)
                .templateCode("RESERVATION_CONFIRMED")
                .subject("Reservation Confirmed!")
                .message(message)
                .metadata(variables)
                .build();

        notificationService.createNotification(request);
    }

    /**
     * Send notification when reservation is cancelled
     */
    public void sendReservationCancelledNotification(UUID reservationId) {
        log.info("Sending reservation cancelled notification for reservation: {}", reservationId);

        ReservationSnapshot reservation = reservationSnapshotRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        UserSnapshot user = userSnapshotRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + reservation.getUserId()));

        Map<String, Object> variables = buildReservationVariables(reservation, user);
        String message = templateService.renderTemplate("RESERVATION_CANCELLED", variables);

        NotificationRequest request = NotificationRequest.builder()
                .userId(reservation.getUserId())
                .reservationId(reservationId)
                .channel(NotificationChannel.EMAIL)
                .templateCode("RESERVATION_CANCELLED")
                .subject("Reservation Cancelled")
                .message(message)
                .metadata(variables)
                .build();

        notificationService.createNotification(request);
    }

    /**
     * Build template variables from reservation and user data
     */
    private Map<String, Object> buildReservationVariables(ReservationSnapshot reservation, UserSnapshot user) {
        Map<String, Object> variables = new HashMap<>();

        // User details
        variables.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
        variables.put("lastName", user.getLastName() != null ? user.getLastName() : "");
        variables.put("email", user.getEmail() != null ? user.getEmail() : "");

        // Reservation details
        variables.put("confirmationCode", reservation.getConfirmationCode() != null ? reservation.getConfirmationCode() : "N/A");
        variables.put("reservationDate", reservation.getReservationDate() != null ? reservation.getReservationDate().toString() : "N/A");
        variables.put("status", reservation.getStatus() != null ? reservation.getStatus() : "N/A");

        // Stall details
        variables.put("stallCode", reservation.getStallCode() != null ? reservation.getStallCode() : "N/A");
        variables.put("sizeCategory", reservation.getSizeCategory() != null ? reservation.getSizeCategory() : "N/A");
        variables.put("price", reservation.getPrice() != null ? reservation.getPrice().toString() : "0.00");
        variables.put("locationX", reservation.getLocationX() != null ? reservation.getLocationX().toString() : "N/A");
        variables.put("locationY", reservation.getLocationY() != null ? reservation.getLocationY().toString() : "N/A");

        return variables;
    }
}
