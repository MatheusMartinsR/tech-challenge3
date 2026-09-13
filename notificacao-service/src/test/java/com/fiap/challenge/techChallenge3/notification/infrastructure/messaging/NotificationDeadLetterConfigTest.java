package com.fiap.challenge.techChallenge3.notification.infrastructure.messaging;

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

@SpringBootTest
class NotificationDeadLetterConfigTest {

    @Autowired
    @Qualifier("notificationQueue")
    private Queue notificationQueue;

    @Autowired
    @Qualifier("notificationDeadLetterQueue")
    private Queue notificationDeadLetterQueue;

    @Autowired
    private FanoutExchange notificationDeadLetterExchange;

    @Autowired
    @Qualifier("notificationDeadLetterBinding")
    private Binding notificationDeadLetterBinding;

    @Autowired
    private MessageRecoverer messageRecoverer;

    @Test
    void mainQueuePointsToTheDeadLetterExchange() {
        assertThat(notificationQueue.getArguments())
                .containsEntry("x-dead-letter-exchange", NotificationRabbitConfig.NOTIFICATION_DLX);
        assertThat(notificationQueue.isDurable()).isTrue();
    }

    @Test
    void deadLetterQueueIsDurableAndBoundToTheFanoutExchange() {
        assertThat(notificationDeadLetterQueue.getName()).isEqualTo(NotificationRabbitConfig.NOTIFICATION_DLQ);
        assertThat(notificationDeadLetterQueue.isDurable()).isTrue();
        assertThat(notificationDeadLetterQueue.getArguments()).doesNotContainKey("x-dead-letter-exchange");

        assertThat(notificationDeadLetterExchange.getName()).isEqualTo(NotificationRabbitConfig.NOTIFICATION_DLX);
        assertThat(notificationDeadLetterBinding.getExchange()).isEqualTo(NotificationRabbitConfig.NOTIFICATION_DLX);
        assertThat(notificationDeadLetterBinding.getDestination()).isEqualTo(NotificationRabbitConfig.NOTIFICATION_DLQ);
    }

    @Test
    void rejectsWithoutRequeueWhenRetriesAreExhausted() {
        assertThat(messageRecoverer).isInstanceOf(RejectAndDontRequeueRecoverer.class);
    }
}
