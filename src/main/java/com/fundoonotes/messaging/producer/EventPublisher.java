package com.fundoonotes.messaging.producer;

import com.fundoonotes.config.RabbitMQConfig;
import com.fundoonotes.dto.event.NoteActivityEvent;
import com.fundoonotes.dto.event.PasswordResetEvent;
import com.fundoonotes.dto.event.ReminderEvent;
import com.fundoonotes.dto.event.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events to RabbitMQ exchanges.
 * All event publishing is fire-and-forget from the caller's perspective.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistration(UserRegistrationEvent event) {
        log.info("Publishing user registration event for userId={}", event.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_USER_REGISTERED, event);
    }

    public void publishPasswordReset(PasswordResetEvent event) {
        log.info("Publishing password reset event for email={}", event.getEmail());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_PASSWORD_RESET, event);
    }

    public void publishReminderCreated(ReminderEvent event) {
        log.info("Publishing reminder created event for reminderId={}", event.getReminderId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_REMINDER_CREATED, event);
    }

    public void publishNoteActivity(NoteActivityEvent event) {
        log.debug("Publishing note activity event: noteId={}, action={}",
                event.getNoteId(), event.getAction());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_NOTE_ACTIVITY, event);
    }
}
