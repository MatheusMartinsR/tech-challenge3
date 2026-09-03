package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do broker RabbitMQ do Serviço de Notificações: a fila que este
 * serviço consome e sua infraestrutura de mensagens mortas (<i>dead letter</i>).
 *
 * <p>O Serviço de Notificações é dono de {@link #NOTIFICACAO_QUEUE} — o Serviço
 * de Agendamento (ver {@link RabbitMQConfig}) só publica na exchange
 * "consultas.exchange" e não conhece esta fila. O binding abaixo ainda injeta o
 * bean {@code TopicExchange} de {@link RabbitMQConfig} porque os dois hoje
 * vivem no mesmo módulo/contexto Spring; quando forem separados em serviços de
 * fato, essa dependência vira apenas o nome da exchange combinado por
 * convenção entre os times, sem acoplar as duas classes de configuração.</p>
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
                .with(RabbitMQConfig.ROUTING_KEY_CONSULTA_CRIADA);
    }

    @Bean
    public Binding bindingConsultaEditada(Queue notificacaoQueue, TopicExchange consultasExchange) {
        return BindingBuilder.bind(notificacaoQueue).to(consultasExchange)
                .with(RabbitMQConfig.ROUTING_KEY_CONSULTA_EDITADA);
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
}
