package com.bookfair.notification_service.service;

import com.bookfair.notification_service.exception.NotificationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@bookfair.com}")
    private String fromEmail;

    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send an email notification
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param body    Email body (HTML supported)
     * @throws NotificationException if email sending fails
     */
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping email to: {}", to);
            return;
        }

        try {
            log.info("Sending email to: {} with subject: {}", to, subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true indicates HTML

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new NotificationException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send a simple text email
     *
     * @param to      Recipient email address
     * @param subject Email subject
     * @param body    Email body (plain text)
     * @throws NotificationException if email sending fails
     */
    public void sendTextEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping text email to: {}", to);
            return;
        }

        try {
            log.info("Sending text email to: {} with subject: {}", to, subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // false indicates plain text

            mailSender.send(message);
            log.info("Text email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send text email to: {}", to, e);
            throw new NotificationException("Failed to send text email: " + e.getMessage(), e);
        }
    }
}
