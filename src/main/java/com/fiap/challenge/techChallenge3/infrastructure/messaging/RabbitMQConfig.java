package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do broker RabbitMQ do Serviço de Agendamento: a exchange onde os
 * eventos de consulta são publicados, e a infraestrutura de publicação.
 *
 * <p>Deliberadamente não declara nenhuma fila: o Serviço de Agendamento publica
 * na exchange topic "consultas.exchange" (routing keys "consulta.criada" e
 * "consulta.editada") sem saber quem consome nem quantos consumidores existem.
 * A fila do Serviço de Notificações — e a decisão de dead-letter dela — é
 * responsabilidade de quem consome, em {@link NotificacaoRabbitConfig}.</p>
 */
@Configuration
public class RabbitMQConfig {

    public static final String CONSULTAS_EXCHANGE = "consultas.exchange";
    public static final String ROUTING_KEY_CONSULTA_CRIADA = "consulta.criada";
    public static final String ROUTING_KEY_CONSULTA_EDITADA = "consulta.editada";

    @Bean
    public TopicExchange consultasExchange() {
        return new TopicExchange(CONSULTAS_EXCHANGE);
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
