package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do broker RabbitMQ para o domínio de consultas.
 *
 * Exchange do tipo topic "consultas.exchange" com uma fila dedicada às
 * notificações, ligada pelas routing keys "consulta.criada" e "consulta.editada".
 */
@Configuration
public class RabbitMQConfig {

    public static final String CONSULTAS_EXCHANGE = "consultas.exchange";
    public static final String NOTIFICACAO_QUEUE = "consultas.notificacao.queue";
    public static final String ROUTING_KEY_CONSULTA_CRIADA = "consulta.criada";
    public static final String ROUTING_KEY_CONSULTA_EDITADA = "consulta.editada";

    @Bean
    public TopicExchange consultasExchange() {
        return new TopicExchange(CONSULTAS_EXCHANGE);
    }

    @Bean
    public Queue notificacaoQueue() {
        // O argumento x-dead-letter-exchange precisa ser declarado aqui: ele é imutável
        // depois que a fila é criada. O destino das mensagens mortas está em
        // NotificacaoRabbitConfig.
        return QueueBuilder.durable(NOTIFICACAO_QUEUE)
                .deadLetterExchange(NotificacaoRabbitConfig.NOTIFICACAO_DLX)
                .build();
    }

    @Bean
    public Binding bindingConsultaCriada(Queue notificacaoQueue, TopicExchange consultasExchange) {
        return BindingBuilder.bind(notificacaoQueue).to(consultasExchange).with(ROUTING_KEY_CONSULTA_CRIADA);
    }

    @Bean
    public Binding bindingConsultaEditada(Queue notificacaoQueue, TopicExchange consultasExchange) {
        return BindingBuilder.bind(notificacaoQueue).to(consultasExchange).with(ROUTING_KEY_CONSULTA_EDITADA);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        // O segundo argumento define os pacotes "trusted" para deserialização (por padrão,
        // apenas java.util/java.lang são aceitos), liberando os eventos do domínio de consultas.
        return new Jackson2JsonMessageConverter(objectMapper, "com.fiap.challenge.techChallenge3.infrastructure.messaging.event");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
