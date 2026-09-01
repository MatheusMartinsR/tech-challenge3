package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.application.usecase.notificacao.EnviarLembreteConsultaUseCase;
import com.fiap.challenge.techChallenge3.domain.model.StatusConsulta;
import com.fiap.challenge.techChallenge3.domain.model.TipoNotificacao;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaEditadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Testa o consumidor do Serviço de Notificações isoladamente: garante que cada tipo de
 * evento é traduzido na chamada correta ao caso de uso, sem depender de broker real.
 */
@ExtendWith(MockitoExtension.class)
class NotificacaoListenerTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 15, 14, 30);

    @Mock
    private EnviarLembreteConsultaUseCase enviarLembreteConsultaUseCase;

    private NotificacaoListener notificacaoListener;

    @BeforeEach
    void setUp() {
        notificacaoListener = new NotificacaoListener(enviarLembreteConsultaUseCase);
    }

    @Test
    void deveProcessarEventoDeConsultaCriada() {
        notificacaoListener.receberEventoConsulta(new ConsultaCriadaEvent(1L, 10L, 20L, DATA_HORA));

        verify(enviarLembreteConsultaUseCase).execute(1L, 10L, DATA_HORA, TipoNotificacao.CONSULTA_CRIADA);
        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(1);
    }

    @Test
    void deveProcessarEventoDeConsultaEditada() {
        notificacaoListener.receberEventoConsulta(
                new ConsultaEditadaEvent(1L, 10L, 20L, DATA_HORA, StatusConsulta.REALIZADA));

        verify(enviarLembreteConsultaUseCase).execute(1L, 10L, DATA_HORA, TipoNotificacao.CONSULTA_EDITADA);
        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(1);
    }

    @Test
    void deveIncrementarContadorParaCadaEventoRecebido() {
        notificacaoListener.receberEventoConsulta(new ConsultaCriadaEvent(1L, 10L, 20L, DATA_HORA));
        notificacaoListener.receberEventoConsulta(
                new ConsultaEditadaEvent(1L, 10L, 20L, DATA_HORA, StatusConsulta.CANCELADA));

        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(2);
    }

    @Test
    void deveIgnorarEventoDesconhecidoSemContabilizar() {
        notificacaoListener.receberEventoConsulta("payload inesperado");

        verifyNoInteractions(enviarLembreteConsultaUseCase);
        assertThat(notificacaoListener.getNotificacoesEnviadas()).isZero();
    }
}
