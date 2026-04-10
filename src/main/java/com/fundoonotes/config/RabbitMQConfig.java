package com.fundoonotes.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Fundoo Notes event-driven architecture.
 * Defines a single topic exchange with multiple queues for different event types.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "fundoo.exchange";

    public static final String USER_REGISTRATION_QUEUE = "user.registration.queue";
    public static final String USER_NOTIFICATION_QUEUE = "user.notification.queue";
    public static final String REMINDER_QUEUE = "reminder.queue";
    public static final String AUDIT_QUEUE = "audit.queue";

    public static final String ROUTING_USER_REGISTERED = "user.registered";
    public static final String ROUTING_PASSWORD_RESET = "user.password.reset";
    public static final String ROUTING_REMINDER_CREATED = "reminder.created";
    public static final String ROUTING_NOTE_ACTIVITY = "note.activity";

    @Bean
    public TopicExchange fundooExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue userRegistrationQueue() {
        return QueueBuilder.durable(USER_REGISTRATION_QUEUE).build();
    }

    @Bean
    public Queue userNotificationQueue() {
        return QueueBuilder.durable(USER_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue reminderQueue() {
        return QueueBuilder.durable(REMINDER_QUEUE).build();
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public Binding userRegistrationBinding(Queue userRegistrationQueue, TopicExchange fundooExchange) {
        return BindingBuilder.bind(userRegistrationQueue).to(fundooExchange).with(ROUTING_USER_REGISTERED);
    }

    @Bean
    public Binding userNotificationBinding(Queue userNotificationQueue, TopicExchange fundooExchange) {
        return BindingBuilder.bind(userNotificationQueue).to(fundooExchange).with(ROUTING_PASSWORD_RESET);
    }

    @Bean
    public Binding reminderBinding(Queue reminderQueue, TopicExchange fundooExchange) {
        return BindingBuilder.bind(reminderQueue).to(fundooExchange).with(ROUTING_REMINDER_CREATED);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange fundooExchange) {
        return BindingBuilder.bind(auditQueue).to(fundooExchange).with(ROUTING_NOTE_ACTIVITY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
