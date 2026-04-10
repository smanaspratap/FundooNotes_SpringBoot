package com.fundoonotes.service;

import java.time.LocalDateTime;

/**
 * Abstraction for notification delivery.
 * Current implementation simulates (logs) notifications.
 * Future versions can swap in real email/push/in-app delivery
 * without touching consumers or other services.
 */
public interface NotificationService {

    void sendWelcomeNotification(Long userId, String email, String firstName);

    void sendPasswordResetNotification(String email);

    void processReminderNotification(Long reminderId, Long noteId, Long userId, LocalDateTime reminderTime);

    void logAuditEvent(Long noteId, Long userId, String action, LocalDateTime timestamp);
}
