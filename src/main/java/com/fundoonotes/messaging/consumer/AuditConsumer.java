package com.fundoonotes.messaging.consumer;

import com.fundoonotes.config.RabbitMQConfig;
import com.fundoonotes.dto.event.NoteActivityEvent;
import com.fundoonotes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes note activity audit events for tracking state changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void handleNoteActivity(NoteActivityEvent event) {
        log.debug("Received note activity event: noteId={}, action={}",
                event.getNoteId(), event.getAction());
        notificationService.logAuditEvent(
                event.getNoteId(), event.getUserId(),
                event.getAction(), event.getTimestamp());
    }
}
