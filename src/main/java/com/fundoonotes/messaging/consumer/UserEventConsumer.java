package com.fundoonotes.messaging.consumer;

import com.fundoonotes.config.RabbitMQConfig;
import com.fundoonotes.dto.event.UserRegistrationEvent;
import com.fundoonotes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes user registration events and delegates to NotificationService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTRATION_QUEUE)
    public void handleUserRegistration(UserRegistrationEvent event) {
        log.debug("Received user registration event for userId={}", event.getUserId());
        notificationService.sendWelcomeNotification(
                event.getUserId(), event.getEmail(), event.getFirstName());
    }
}
