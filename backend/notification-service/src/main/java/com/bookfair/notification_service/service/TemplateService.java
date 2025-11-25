package com.bookfair.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for managing notification templates
 */
@Service
@Slf4j
public class TemplateService {

    /**
     * Render a template with the given variables
     *
     * @param templateCode Template identifier
     * @param variables    Variables to replace in template
     * @return Rendered message
     */
    public String renderTemplate(String templateCode, Map<String, Object> variables) {
        log.debug("Rendering template: {} with variables: {}", templateCode, variables);

        String template = getTemplate(templateCode);
        if (template == null) {
            log.warn("Template not found: {}, returning empty string", templateCode);
            return "";
        }

        // Simple variable replacement
        String rendered = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            rendered = rendered.replace(placeholder, value);
        }

        log.debug("Template rendered successfully");
        return rendered;
    }

    /**
     * Get template by code
     *
     * @param templateCode Template identifier
     * @return Template string
     */
    private String getTemplate(String templateCode) {
        // Template repository (can be moved to database or external file)
        return switch (templateCode) {
            case "RESERVATION_CREATED" -> buildReservationCreatedTemplate();
            case "RESERVATION_CONFIRMED" -> buildReservationConfirmedTemplate();
            case "RESERVATION_CANCELLED" -> buildReservationCancelledTemplate();
            case "PAYMENT_REMINDER" -> buildPaymentReminderTemplate();
            case "EVENT_REMINDER" -> buildEventReminderTemplate();
            default -> null;
        };
    }

    private String buildReservationCreatedTemplate() {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #2c3e50;">Reservation Created Successfully</h2>
                        <p>Dear {{firstName}} {{lastName}},</p>
                        <p>Your reservation has been created successfully!</p>
                        <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Reservation Details:</strong></p>
                            <ul style="list-style: none; padding: 0;">
                                <li><strong>Confirmation Code:</strong> {{confirmationCode}}</li>
                                <li><strong>Stall Code:</strong> {{stallCode}}</li>
                                <li><strong>Size:</strong> {{sizeCategory}}</li>
                                <li><strong>Price:</strong> ${{price}}</li>
                                <li><strong>Reservation Date:</strong> {{reservationDate}}</li>
                                <li><strong>Status:</strong> {{status}}</li>
                            </ul>
                        </div>
                        <p>Please complete your payment to confirm your reservation.</p>
                        <p>Thank you for choosing our Book Fair!</p>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                        <p style="font-size: 12px; color: #666;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </body>
                </html>
                """;
    }

    private String buildReservationConfirmedTemplate() {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #27ae60;">Reservation Confirmed! 🎉</h2>
                        <p>Dear {{firstName}} {{lastName}},</p>
                        <p>Great news! Your reservation has been confirmed.</p>
                        <div style="background-color: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Confirmed Reservation:</strong></p>
                            <ul style="list-style: none; padding: 0;">
                                <li><strong>Confirmation Code:</strong> {{confirmationCode}}</li>
                                <li><strong>Stall Code:</strong> {{stallCode}}</li>
                                <li><strong>Size:</strong> {{sizeCategory}}</li>
                                <li><strong>Event Date:</strong> {{reservationDate}}</li>
                            </ul>
                        </div>
                        <p>Please present your confirmation code at the venue on the event day.</p>
                        <p>We look forward to seeing you at the Book Fair!</p>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                        <p style="font-size: 12px; color: #666;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </body>
                </html>
                """;
    }

    private String buildReservationCancelledTemplate() {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #e74c3c;">Reservation Cancelled</h2>
                        <p>Dear {{firstName}} {{lastName}},</p>
                        <p>Your reservation has been cancelled.</p>
                        <div style="background-color: #ffebee; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Cancelled Reservation:</strong></p>
                            <ul style="list-style: none; padding: 0;">
                                <li><strong>Confirmation Code:</strong> {{confirmationCode}}</li>
                                <li><strong>Stall Code:</strong> {{stallCode}}</li>
                                <li><strong>Cancellation Date:</strong> {{reservationDate}}</li>
                            </ul>
                        </div>
                        <p>If this was a mistake, please contact our support team.</p>
                        <p>We hope to see you at future events!</p>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                        <p style="font-size: 12px; color: #666;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </body>
                </html>
                """;
    }

    private String buildPaymentReminderTemplate() {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #f39c12;">Payment Reminder</h2>
                        <p>Dear {{firstName}} {{lastName}},</p>
                        <p>This is a reminder to complete your payment for reservation <strong>{{confirmationCode}}</strong>.</p>
                        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Payment Details:</strong></p>
                            <ul style="list-style: none; padding: 0;">
                                <li><strong>Amount Due:</strong> ${{price}}</li>
                                <li><strong>Stall Code:</strong> {{stallCode}}</li>
                                <li><strong>Due Date:</strong> {{dueDate}}</li>
                            </ul>
                        </div>
                        <p>Please complete your payment to secure your reservation.</p>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                        <p style="font-size: 12px; color: #666;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </body>
                </html>
                """;
    }

    private String buildEventReminderTemplate() {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #3498db;">Event Reminder</h2>
                        <p>Dear {{firstName}} {{lastName}},</p>
                        <p>This is a reminder that the Book Fair event is coming up soon!</p>
                        <div style="background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Event Details:</strong></p>
                            <ul style="list-style: none; padding: 0;">
                                <li><strong>Date:</strong> {{eventDate}}</li>
                                <li><strong>Your Stall:</strong> {{stallCode}}</li>
                                <li><strong>Confirmation Code:</strong> {{confirmationCode}}</li>
                            </ul>
                        </div>
                        <p>Please arrive early to set up your stall. Don't forget your confirmation code!</p>
                        <p>We're excited to see you there!</p>
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                        <p style="font-size: 12px; color: #666;">
                            This is an automated message. Please do not reply to this email.
                        </p>
                    </div>
                </body>
                </html>
                """;
    }
}
