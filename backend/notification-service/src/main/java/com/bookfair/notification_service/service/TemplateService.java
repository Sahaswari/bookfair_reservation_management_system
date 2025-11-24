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
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Reservation Created</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fa;">
                    <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 20px 0;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;">📚 Book Fair Reservation</h1>
                                            <p style="color: #f0f0f0; margin: 10px 0 0 0; font-size: 16px;">Reservation Created Successfully!</p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="color: #333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                                Dear <strong>{{firstName}} {{lastName}}</strong>,
                                            </p>
                                            <p style="color: #555; font-size: 15px; line-height: 1.6; margin: 0 0 30px 0;">
                                                Great news! Your stall reservation has been created successfully. Please review the details below and complete your payment to confirm your reservation.
                                            </p>
                                            
                                            <!-- Reservation Details Card -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%); border-radius: 8px; border-left: 4px solid #667eea; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 25px;">
                                                        <h3 style="color: #667eea; margin: 0 0 20px 0; font-size: 18px;">📋 Reservation Details</h3>
                                                        <table width="100%" cellpadding="8" cellspacing="0">
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Confirmation Code:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{confirmationCode}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Stall Code:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{stallCode}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Size Category:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{sizeCategory}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Location:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">X: {{locationX}}, Y: {{locationY}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0; border-top: 2px solid #e0e0e0;">Amount:</td>
                                                                <td style="color: #667eea; font-size: 18px; font-weight: 700; padding: 8px 0; text-align: right; border-top: 2px solid #e0e0e0;">${{price}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Reservation Date:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{reservationDate}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Status:</td>
                                                                <td style="padding: 8px 0; text-align: right;">
                                                                    <span style="background-color: #ffc107; color: #fff; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600;">{{status}}</span>
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Contact Information -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f8f9fa; border-radius: 8px; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 20px;">
                                                        <h3 style="color: #333; margin: 0 0 15px 0; font-size: 16px;">👤 Your Information</h3>
                                                        <p style="color: #555; font-size: 14px; margin: 5px 0;"><strong>Name:</strong> {{firstName}} {{lastName}}</p>
                                                        <p style="color: #555; font-size: 14px; margin: 5px 0;"><strong>Email:</strong> {{email}}</p>
                                                        <p style="color: #555; font-size: 14px; margin: 5px 0;"><strong>Role:</strong> {{userRole}}</p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- QR Code Section -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="margin: 30px 0;">
                                                <tr>
                                                    <td align="center" style="padding: 20px; background-color: #f8f9fa; border-radius: 8px;">
                                                        <h3 style="color: #333; margin: 0 0 15px 0; font-size: 16px;">📱 Your QR Code</h3>
                                                        <img src="{{qrCodeUrl}}" alt="QR Code" style="max-width: 200px; height: auto; border: 3px solid #667eea; border-radius: 8px; padding: 10px; background-color: white;"/>
                                                        <p style="color: #666; font-size: 13px; margin: 15px 0 0 0;">Present this QR code at the venue</p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Action Button -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="margin: 30px 0;">
                                                <tr>
                                                    <td align="center">
                                                        <a href="#" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; text-decoration: none; padding: 15px 40px; border-radius: 25px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);">
                                                            Complete Payment
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Important Notice -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #fff3cd; border-left: 4px solid #ffc107; border-radius: 8px; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 15px;">
                                                        <p style="color: #856404; font-size: 14px; margin: 0; line-height: 1.6;">
                                                            <strong>⚠️ Important:</strong> Please complete your payment within 24 hours to confirm your reservation. Unpaid reservations will be automatically cancelled.
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="color: #555; font-size: 15px; line-height: 1.6; margin: 30px 0 0 0;">
                                                Thank you for choosing our Book Fair! If you have any questions, please don't hesitate to contact us.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                            <p style="color: #999; font-size: 13px; margin: 0 0 10px 0;">
                                                This is an automated message. Please do not reply to this email.
                                            </p>
                                            <p style="color: #999; font-size: 13px; margin: 0;">
                                                © 2025 Book Fair Reservation System. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;
    }

    private String buildReservationConfirmedTemplate() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Reservation Confirmed</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fa;">
                    <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 20px 0;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;">🎉 Reservation Confirmed!</h1>
                                            <p style="color: #f0f0f0; margin: 10px 0 0 0; font-size: 16px;">Your stall is ready for the event!</p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="color: #333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                                Dear <strong>{{firstName}} {{lastName}}</strong>,
                                            </p>
                                            <p style="color: #555; font-size: 15px; line-height: 1.6; margin: 0 0 30px 0;">
                                                Congratulations! Your payment has been received and your stall reservation is now <strong style="color: #11998e;">CONFIRMED</strong>. We're excited to have you at the Book Fair!
                                            </p>
                                            
                                            <!-- Success Badge -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="margin: 20px 0;">
                                                <tr>
                                                    <td align="center" style="padding: 20px;">
                                                        <div style="display: inline-block; background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; padding: 12px 30px; border-radius: 25px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(17, 153, 142, 0.3);">
                                                            ✓ CONFIRMED
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Reservation Details Card -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #11998e15 0%, #38ef7d15 100%); border-radius: 8px; border-left: 4px solid #11998e; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 25px;">
                                                        <h3 style="color: #11998e; margin: 0 0 20px 0; font-size: 18px;">📋 Confirmed Reservation Details</h3>
                                                        <table width="100%" cellpadding="8" cellspacing="0">
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Confirmation Code:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{confirmationCode}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Stall Code:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{stallCode}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Size Category:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{sizeCategory}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Location:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">X: {{locationX}}, Y: {{locationY}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Event Date:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{reservationDate}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0; border-top: 2px solid #e0e0e0;">Amount Paid:</td>
                                                                <td style="color: #11998e; font-size: 18px; font-weight: 700; padding: 8px 0; text-align: right; border-top: 2px solid #e0e0e0;">${{price}}</td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- QR Code Section -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="margin: 30px 0;">
                                                <tr>
                                                    <td align="center" style="padding: 20px; background-color: #f8f9fa; border-radius: 8px;">
                                                        <h3 style="color: #333; margin: 0 0 15px 0; font-size: 16px;">📱 Your Entry Pass QR Code</h3>
                                                        <img src="{{qrCodeUrl}}" alt="QR Code" style="max-width: 200px; height: auto; border: 3px solid #11998e; border-radius: 8px; padding: 10px; background-color: white;"/>
                                                        <p style="color: #666; font-size: 13px; margin: 15px 0 0 0;">Save this QR code - You'll need it at the venue entrance!</p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Instructions -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #e8f5e9; border-left: 4px solid #11998e; border-radius: 8px; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 20px;">
                                                        <h3 style="color: #11998e; margin: 0 0 15px 0; font-size: 16px;">📌 What's Next?</h3>
                                                        <ul style="color: #555; font-size: 14px; line-height: 1.8; margin: 0; padding-left: 20px;">
                                                            <li>Save this email and your QR code</li>
                                                            <li>Arrive 30 minutes before the event starts</li>
                                                            <li>Present your QR code at the registration desk</li>
                                                            <li>Bring your merchandise and display materials</li>
                                                            <li>Check in at Stall {{stallCode}} (Location: X={{locationX}}, Y={{locationY}})</li>
                                                        </ul>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="color: #555; font-size: 15px; line-height: 1.6; margin: 30px 0 0 0;">
                                                We look forward to seeing you at the Book Fair! If you have any questions, please contact our support team.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                            <p style="color: #999; font-size: 13px; margin: 0 0 10px 0;">
                                                This is an automated message. Please do not reply to this email.
                                            </p>
                                            <p style="color: #999; font-size: 13px; margin: 0;">
                                                © 2025 Book Fair Reservation System. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;
    }

    private String buildReservationCancelledTemplate() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Reservation Cancelled</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fa;">
                    <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f4f7fa; padding: 20px 0;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%); padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;">Reservation Cancelled</h1>
                                            <p style="color: #f0f0f0; margin: 10px 0 0 0; font-size: 16px;">Your reservation has been cancelled</p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="color: #333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                                Dear <strong>{{firstName}} {{lastName}}</strong>,
                                            </p>
                                            <p style="color: #555; font-size: 15px; line-height: 1.6; margin: 0 0 30px 0;">
                                                This email confirms that your stall reservation has been cancelled. Below are the details of the cancelled reservation.
                                            </p>
                                            
                                            <!-- Cancelled Details Card -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #eb334915 0%, #f45c4315 100%); border-radius: 8px; border-left: 4px solid #eb3349; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 25px;">
                                                        <h3 style="color: #eb3349; margin: 0 0 20px 0; font-size: 18px;">📋 Cancelled Reservation Details</h3>
                                                        <table width="100%" cellpadding="8" cellspacing="0">
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Confirmation Code:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{confirmationCode}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Stall Code:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{stallCode}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Size Category:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{sizeCategory}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Original Amount:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">${{price}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Cancellation Date:</td>
                                                                <td style="color: #333; font-size: 14px; font-weight: 600; padding: 8px 0; text-align: right;">{{reservationDate}}</td>
                                                            </tr>
                                                            <tr>
                                                                <td style="color: #666; font-size: 14px; padding: 8px 0;">Status:</td>
                                                                <td style="padding: 8px 0; text-align: right;">
                                                                    <span style="background-color: #eb3349; color: #fff; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600;">CANCELLED</span>
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Refund Information -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #fff3cd; border-left: 4px solid #ffc107; border-radius: 8px; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 20px;">
                                                        <h3 style="color: #856404; margin: 0 0 15px 0; font-size: 16px;">💰 Refund Information</h3>
                                                        <p style="color: #856404; font-size: 14px; line-height: 1.6; margin: 0;">
                                                            If you made a payment for this reservation, your refund will be processed within 5-7 business days to your original payment method. You will receive a separate email confirmation once the refund is processed.
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Action Section -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f8f9fa; border-radius: 8px; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 20px;">
                                                        <h3 style="color: #333; margin: 0 0 15px 0; font-size: 16px;">🔄 Make a New Reservation</h3>
                                                        <p style="color: #555; font-size: 14px; line-height: 1.6; margin: 0 0 15px 0;">
                                                            We'd love to have you at the Book Fair! You can make a new reservation anytime through our platform.
                                                        </p>
                                                        <table width="100%" cellpadding="0" cellspacing="0">
                                                            <tr>
                                                                <td align="center">
                                                                    <a href="#" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; text-decoration: none; padding: 12px 30px; border-radius: 25px; font-size: 14px; font-weight: 600;">
                                                                        Browse Available Stalls
                                                                    </a>
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Support Information -->
                                            <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #e3f2fd; border-left: 4px solid #2196f3; border-radius: 8px; margin: 20px 0;">
                                                <tr>
                                                    <td style="padding: 20px;">
                                                        <h3 style="color: #1976d2; margin: 0 0 15px 0; font-size: 16px;">❓ Need Help?</h3>
                                                        <p style="color: #1565c0; font-size: 14px; line-height: 1.6; margin: 0;">
                                                            If this cancellation was made by mistake or if you have any questions, please contact our support team immediately. We're here to help!
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="color: #555; font-size: 15px; line-height: 1.6; margin: 30px 0 0 0;">
                                                We hope to see you at our future events. Thank you for your interest in the Book Fair!
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                            <p style="color: #999; font-size: 13px; margin: 0 0 10px 0;">
                                                This is an automated message. Please do not reply to this email.
                                            </p>
                                            <p style="color: #999; font-size: 13px; margin: 0;">
                                                © 2025 Book Fair Reservation System. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
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
