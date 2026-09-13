package com.fiap.challenge.techChallenge3.agendamento.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEventos;
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
 * <p>Deliberadamente não declara nenhuma fila. Este serviço publica na exchange
 * e não sabe quem consome, quantos consumidores existem, nem se existe algum —
 * cada consumidor declara e liga a própria fila.</p>
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange consultasExchange() {
        return new TopicExchange(ConsultaEventos.EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        // O segundo argumento define os pacotes "trusted" para deserialização (por padrão,
        // apenas java.util/java.lang são aceitos), liberando os eventos do contrato.
        return new Jackson2JsonMessageConverter(objectMapper, ConsultaEventos.PACOTE_EVENTOS);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
