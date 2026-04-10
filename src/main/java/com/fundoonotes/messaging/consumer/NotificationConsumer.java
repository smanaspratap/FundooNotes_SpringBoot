package com.fundoonotes.messaging.consumer;

import com.fundoonotes.config.RabbitMQConfig;
import com.fundoonotes.dto.event.PasswordResetEvent;
import com.fundoonotes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes password reset notification events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.USER_NOTIFICATION_QUEUE)
    public void handlePasswordReset(PasswordResetEvent event) {
        log.debug("Received password reset event for email={}", event.getEmail());
        notificationService.sendPasswordResetNotification(event.getEmail());
    }
}
