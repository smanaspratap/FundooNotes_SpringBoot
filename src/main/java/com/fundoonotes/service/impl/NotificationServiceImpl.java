package com.fundoonotes.service.impl;

import com.fundoonotes.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Simulated notification service.
 * Logs notification actions for development/testing.
 *
 * In production, replace with real implementations:
 * - Email via SMTP / SendGrid / AWS SES
 * - Push via Firebase Cloud Messaging
 * - In-app via WebSocket or notification table
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendWelcomeNotification(Long userId, String email, String firstName) {
        log.info("[NOTIFICATION] Welcome notification prepared for {} (userId={}, email={})",
                firstName, userId, email);
    }

    @Override
    public void sendPasswordResetNotification(String email) {
        log.info("[NOTIFICATION] Password reset notification prepared for email={}", email);
    }

    @Override
    public void processReminderNotification(Long reminderId, Long noteId, Long userId,
                                            LocalDateTime reminderTime) {
        log.info("[NOTIFICATION] Reminder notification prepared: reminderId={}, noteId={}, " +
                "userId={}, reminderTime={}", reminderId, noteId, userId, reminderTime);
    }

    @Override
    public void logAuditEvent(Long noteId, Long userId, String action, LocalDateTime timestamp) {
        log.info("[AUDIT] Note activity recorded: noteId={}, userId={}, action={}, time={}",
                noteId, userId, action, timestamp);
    }
}
