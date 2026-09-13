package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.domain.model.StatusConsulta;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaEditadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa o comportamento do listener (Serviço de Notificações) diretamente,
 * sem depender de um broker RabbitMQ real.
 */
class NotificacaoListenerTest {

    private NotificacaoListener notificacaoListener;

    @BeforeEach
    void setUp() {
        notificacaoListener = new NotificacaoListener();
    }

    @Test
    void deveProcessarEventoDeConsultaCriada() {
        ConsultaCriadaEvent event = new ConsultaCriadaEvent(1L, 10L, 20L, LocalDateTime.now().plusDays(1));

        notificacaoListener.receberEventoConsulta(event);

        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(1);
    }

    @Test
    void deveProcessarEventoDeConsultaEditada() {
        ConsultaEditadaEvent event = new ConsultaEditadaEvent(
                1L, 10L, 20L, LocalDateTime.now().plusDays(1), StatusConsulta.REALIZADA);

        notificacaoListener.receberEventoConsulta(event);

        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(1);
    }

    @Test
    void deveIncrementarContadorParaCadaEventoRecebido() {
        notificacaoListener.receberEventoConsulta(new ConsultaCriadaEvent(1L, 10L, 20L, LocalDateTime.now().plusDays(1)));
        notificacaoListener.receberEventoConsulta(new ConsultaEditadaEvent(
                1L, 10L, 20L, LocalDateTime.now().plusDays(1), StatusConsulta.CANCELADA));

        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(2);
    }
}
