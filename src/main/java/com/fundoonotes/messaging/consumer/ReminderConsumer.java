package com.fundoonotes.messaging.consumer;

import com.fundoonotes.config.RabbitMQConfig;
import com.fundoonotes.dto.event.ReminderEvent;
import com.fundoonotes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes reminder events and delegates notification processing.
 * Current: simulates notification preparation.
 * Future: triggers scheduled email/push/in-app delivery.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.REMINDER_QUEUE)
    public void handleReminderCreated(ReminderEvent event) {
        log.debug("Received reminder event: reminderId={}", event.getReminderId());
        notificationService.processReminderNotification(
                event.getReminderId(), event.getNoteId(),
                event.getUserId(), event.getReminderTime());
    }
}
