package com.fiap.challenge.techChallenge3.notificacao.infrastructure.messaging;

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

/**
 * Configuração do broker RabbitMQ do Serviço de Notificações: a fila que este
 * serviço consome, sua ligação com a exchange do contrato e sua infraestrutura de
 * mensagens mortas (<i>dead letter</i>).
 *
 * <p>Este serviço é dono de tudo o que declara aqui. A exchange
 * {@link ConsultaEventos#EXCHANGE} é declarada dos dois lados de propósito:
 * redeclarar uma exchange existente com os mesmos atributos é idempotente no
 * RabbitMQ, e assim nenhum dos dois serviços depende de o outro ter subido antes —
 * nem de conhecer classes do outro.</p>
 *
 * <p>Quando o consumo de um evento falha e as tentativas configuradas em
 * {@code spring.rabbitmq.listener.simple.retry.*} se esgotam, a mensagem não pode
 * voltar para a fila principal (reprocessamento infinito) nem ser descartada
 * (perda silenciosa do lembrete). Ela é rejeitada e o próprio broker a republica
 * na exchange apontada pelo argumento {@code x-dead-letter-exchange} da fila
 * principal, de onde chega à {@link #NOTIFICACAO_DLQ} para inspeção posterior.</p>
 *
 * <p>A exchange de dead letter é do tipo <i>fanout</i> de propósito: ao republicar, o
 * RabbitMQ preserva a routing key original ({@code consulta.criada} ou {@code consulta.editada}),
 * então uma exchange <i>topic</i> exigiria um binding por routing key para não
 * descartar metade das mensagens mortas.</p>
 */
@Configuration
public class NotificacaoRabbitConfig {

    public static final String NOTIFICACAO_QUEUE = "consultas.notificacao.queue";
    public static final String NOTIFICACAO_DLX = "consultas.notificacao.dlx";
    public static final String NOTIFICACAO_DLQ = "consultas.notificacao.queue.dlq";

    @Bean
    public TopicExchange consultasExchange() {
        return new TopicExchange(ConsultaEventos.EXCHANGE);
    }

    @Bean
    public Queue notificacaoQueue() {
        // O argumento x-dead-letter-exchange precisa ser declarado aqui: ele é imutável
        // depois que a fila é criada.
        return QueueBuilder.durable(NOTIFICACAO_QUEUE)
                .deadLetterExchange(NOTIFICACAO_DLX)
                .build();
    }

    @Bean
    public Binding bindingConsultaCriada(Queue notificacaoQueue, TopicExchange consultasExchange) {
        return BindingBuilder.bind(notificacaoQueue).to(consultasExchange)
                .with(ConsultaEventos.ROUTING_KEY_CONSULTA_CRIADA);
    }

    @Bean
    public Binding bindingConsultaEditada(Queue notificacaoQueue, TopicExchange consultasExchange) {
        return BindingBuilder.bind(notificacaoQueue).to(consultasExchange)
                .with(ConsultaEventos.ROUTING_KEY_CONSULTA_EDITADA);
    }

    @Bean
    public FanoutExchange notificacaoDeadLetterExchange() {
        return new FanoutExchange(NOTIFICACAO_DLX, true, false);
    }

    @Bean
    public Queue notificacaoDeadLetterQueue() {
        // Sem x-dead-letter-exchange próprio: uma mensagem que já morreu não deve
        // morrer de novo e sair daqui.
        return QueueBuilder.durable(NOTIFICACAO_DLQ).build();
    }

    @Bean
    public Binding bindingNotificacaoDeadLetter(Queue notificacaoDeadLetterQueue,
                                                 FanoutExchange notificacaoDeadLetterExchange) {
        return BindingBuilder.bind(notificacaoDeadLetterQueue).to(notificacaoDeadLetterExchange);
    }

    /**
     * Define o que fazer com a mensagem depois da última tentativa fracassada.
     *
     * <p>Sem este bean o Spring apenas registra um aviso e confirma (ack) a mensagem,
     * que some da fila sem passar pela DLQ — o dead lettering do RabbitMQ só é
     * acionado por rejeição. O {@link RejectAndDontRequeueRecoverer} lança
     * {@code AmqpRejectAndDontRequeueException}, que faz o container rejeitar a
     * mensagem sem devolvê-la à fila.</p>
     */
    @Bean
    public MessageRecoverer notificacaoMessageRecoverer() {
        return new RejectAndDontRequeueRecoverer();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        // Libera o pacote do contrato na desserialização; por padrão só java.util/java.lang
        // são aceitos e nenhum evento seria reconhecido.
        return new Jackson2JsonMessageConverter(objectMapper, ConsultaEventos.PACOTE_EVENTOS);
    }

    /**
     * Usado para ler a dead letter queue (inspeção e testes). O envio de eventos é
     * responsabilidade do Serviço de Agendamento — este serviço só consome.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
