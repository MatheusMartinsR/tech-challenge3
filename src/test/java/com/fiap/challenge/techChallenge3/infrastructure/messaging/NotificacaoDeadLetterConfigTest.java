package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confere a declaração da infraestrutura de mensagens mortas sem depender de um
 * broker. O ponto sensível é o argumento {@code x-dead-letter-exchange} da fila
 * principal: sem ele o RabbitMQ descarta a mensagem rejeitada em vez de republicá-la,
 * e a falha não aparece em nenhum teste que só observe o consumidor.
 */
@SpringBootTest
class NotificacaoDeadLetterConfigTest {

    @Autowired
    @Qualifier("notificacaoQueue")
    private Queue notificacaoQueue;

    @Autowired
    @Qualifier("notificacaoDeadLetterQueue")
    private Queue notificacaoDeadLetterQueue;

    @Autowired
    private FanoutExchange notificacaoDeadLetterExchange;

    @Autowired
    @Qualifier("bindingNotificacaoDeadLetter")
    private Binding bindingNotificacaoDeadLetter;

    @Autowired
    private MessageRecoverer messageRecoverer;

    @Test
    void filaPrincipalDeveApontarParaAExchangeDeMensagensMortas() {
        assertThat(notificacaoQueue.getArguments())
                .containsEntry("x-dead-letter-exchange", NotificacaoRabbitConfig.NOTIFICACAO_DLX);
        assertThat(notificacaoQueue.isDurable()).isTrue();
    }

    @Test
    void deadLetterQueueDeveSerDuravelELigadaAExchangeFanout() {
        assertThat(notificacaoDeadLetterQueue.getName()).isEqualTo(NotificacaoRabbitConfig.NOTIFICACAO_DLQ);
        assertThat(notificacaoDeadLetterQueue.isDurable()).isTrue();
        // Uma mensagem que já morreu não pode morrer de novo e escapar da DLQ.
        assertThat(notificacaoDeadLetterQueue.getArguments()).doesNotContainKey("x-dead-letter-exchange");

        assertThat(notificacaoDeadLetterExchange.getName()).isEqualTo(NotificacaoRabbitConfig.NOTIFICACAO_DLX);
        assertThat(bindingNotificacaoDeadLetter.getExchange()).isEqualTo(NotificacaoRabbitConfig.NOTIFICACAO_DLX);
        assertThat(bindingNotificacaoDeadLetter.getDestination()).isEqualTo(NotificacaoRabbitConfig.NOTIFICACAO_DLQ);
    }

    @Test
    void deveRejeitarSemReenfileirarQuandoAsTentativasSeEsgotam() {
        // Sem este recoverer o Spring apenas confirmaria (ack) a mensagem e ela sumiria
        // sem passar pela DLQ.
        assertThat(messageRecoverer).isInstanceOf(RejectAndDontRequeueRecoverer.class);
    }
}
