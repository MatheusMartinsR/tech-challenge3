package com.fiap.challenge.techChallenge3.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEventos;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitConfig {

    public static final String NOTIFICATION_QUEUE = "consultas.notification.queue";
    public static final String NOTIFICATION_DLX = "consultas.notification.dlx";
    public static final String NOTIFICATION_DLQ = "consultas.notification.queue.dlq";

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(ConsultaEventos.EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLX)
                .build();
    }

    @Bean
    public Binding appointmentCreatedBinding(Queue notificationQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(notificationQueue).to(appointmentExchange)
                .with(ConsultaEventos.APPOINTMENT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding appointmentUpdatedBinding(Queue notificationQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(notificationQueue).to(appointmentExchange)
                .with(ConsultaEventos.APPOINTMENT_UPDATED_ROUTING_KEY);
    }

    @Bean
    public FanoutExchange notificationDeadLetterExchange() {
        return new FanoutExchange(NOTIFICATION_DLX, true, false);
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding notificationDeadLetterBinding(Queue notificationDeadLetterQueue,
                                                  FanoutExchange notificationDeadLetterExchange) {
        return BindingBuilder.bind(notificationDeadLetterQueue).to(notificationDeadLetterExchange);
    }

    @Bean
    public MessageRecoverer notificationMessageRecoverer() {
        return new RejectAndDontRequeueRecoverer();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper, ConsultaEventos.EVENT_PACKAGE);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
