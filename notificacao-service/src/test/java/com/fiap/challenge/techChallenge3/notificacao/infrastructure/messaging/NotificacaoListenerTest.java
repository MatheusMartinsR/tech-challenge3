package com.fiap.challenge.techChallenge3.notificacao.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEditadaEvent;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import com.fiap.challenge.techChallenge3.notificacao.application.usecase.EnviarLembreteConsultaUseCase;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.Destinatario;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.TipoNotificacao;
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
    private static final Destinatario MARIA = new Destinatario("Maria", "maria@paciente.com");

    @Mock
    private EnviarLembreteConsultaUseCase enviarLembreteConsultaUseCase;

    private NotificacaoListener notificacaoListener;

    @BeforeEach
    void setUp() {
        notificacaoListener = new NotificacaoListener(enviarLembreteConsultaUseCase);
    }

    private static ConsultaCriadaEvent eventoCriada() {
        return new ConsultaCriadaEvent(1L, 10L, "Maria", "maria@paciente.com", 20L, DATA_HORA);
    }

    private static ConsultaEditadaEvent eventoEditada(StatusConsulta status) {
        return new ConsultaEditadaEvent(1L, 10L, "Maria", "maria@paciente.com", 20L, DATA_HORA, status);
    }

    @Test
    void deveProcessarEventoDeConsultaCriada() {
        notificacaoListener.receberConsultaCriada(eventoCriada());

        // O destinatário sai do próprio payload — nenhuma consulta a banco de outro serviço.
        verify(enviarLembreteConsultaUseCase).execute(1L, 10L, MARIA, DATA_HORA, TipoNotificacao.CONSULTA_CRIADA);
        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(1);
    }

    @Test
    void deveProcessarEventoDeConsultaEditada() {
        notificacaoListener.receberConsultaEditada(eventoEditada(StatusConsulta.REALIZADA));

        verify(enviarLembreteConsultaUseCase).execute(1L, 10L, MARIA, DATA_HORA, TipoNotificacao.CONSULTA_EDITADA);
        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(1);
    }

    @Test
    void deveIncrementarContadorParaCadaEventoRecebido() {
        notificacaoListener.receberConsultaCriada(eventoCriada());
        notificacaoListener.receberConsultaEditada(eventoEditada(StatusConsulta.CANCELADA));

        assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(2);
    }

    @Test
    void deveIgnorarEventoDesconhecidoSemContabilizar() {
        notificacaoListener.receberEventoDesconhecido("payload inesperado");

        verifyNoInteractions(enviarLembreteConsultaUseCase);
        assertThat(notificacaoListener.getNotificacoesEnviadas()).isZero();
    }
}
