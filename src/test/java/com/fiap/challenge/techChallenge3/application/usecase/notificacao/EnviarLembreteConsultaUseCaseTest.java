package com.fiap.challenge.techChallenge3.application.usecase.notificacao;

import com.fiap.challenge.techChallenge3.application.port.out.DadosDestinatarioPort;
import com.fiap.challenge.techChallenge3.application.port.out.EnvioLembretePort;
import com.fiap.challenge.techChallenge3.application.port.out.NotificacaoRepository;
import com.fiap.challenge.techChallenge3.domain.model.Destinatario;
import com.fiap.challenge.techChallenge3.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.domain.model.StatusNotificacao;
import com.fiap.challenge.techChallenge3.domain.model.TipoNotificacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnviarLembreteConsultaUseCaseTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 15, 14, 30);

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private DadosDestinatarioPort dadosDestinatarioPort;

    @Mock
    private EnvioLembretePort envioLembretePort;

    private EnviarLembreteConsultaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EnviarLembreteConsultaUseCase(notificacaoRepository, dadosDestinatarioPort, envioLembretePort);
    }

    private void comDestinatario() {
        when(dadosDestinatarioPort.buscarPor(10L))
                .thenReturn(Optional.of(new Destinatario("Maria", "maria@paciente.com")));
        when(notificacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Notificacao capturarNotificacaoSalva() {
        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void deveEnviarLembreteERegistrarNotificacaoComoEnviada() {
        comDestinatario();

        useCase.execute(1L, 10L, DATA_HORA, TipoNotificacao.CONSULTA_CRIADA);

        verify(envioLembretePort).enviar(any(Notificacao.class));

        Notificacao salva = capturarNotificacaoSalva();
        assertThat(salva.getStatus()).isEqualTo(StatusNotificacao.ENVIADA);
        assertThat(salva.getConsultaId()).isEqualTo(1L);
        assertThat(salva.getPacienteId()).isEqualTo(10L);
        assertThat(salva.getDestinatario()).isEqualTo("maria@paciente.com");
        assertThat(salva.getTipo()).isEqualTo(TipoNotificacao.CONSULTA_CRIADA);
        assertThat(salva.getDataEnvio()).isNotNull();
    }

    @Test
    void deveMontarMensagemDeAgendamentoComNomeEDataFormatada() {
        comDestinatario();

        useCase.execute(1L, 10L, DATA_HORA, TipoNotificacao.CONSULTA_CRIADA);

        assertThat(capturarNotificacaoSalva().getMensagem())
                .isEqualTo("Olá, Maria! Sua consulta foi agendada para 15/10/2026 às 14:30.");
    }

    @Test
    void deveMontarMensagemDeRemarcacaoQuandoConsultaEditada() {
        comDestinatario();

        useCase.execute(1L, 10L, DATA_HORA, TipoNotificacao.CONSULTA_EDITADA);

        assertThat(capturarNotificacaoSalva().getMensagem())
                .isEqualTo("Olá, Maria! Sua consulta foi remarcada para 15/10/2026 às 14:30.");
    }

    @Test
    void deveRegistrarFalhaSemEnviarQuandoDestinatarioNaoEncontrado() {
        when(dadosDestinatarioPort.buscarPor(99L)).thenReturn(Optional.empty());
        when(notificacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(1L, 99L, DATA_HORA, TipoNotificacao.CONSULTA_CRIADA);

        verify(envioLembretePort, never()).enviar(any());

        Notificacao salva = capturarNotificacaoSalva();
        assertThat(salva.getStatus()).isEqualTo(StatusNotificacao.FALHA);
        assertThat(salva.getDestinatario()).isNull();
    }

    @Test
    void deveRegistrarFalhaEPropagarExcecaoQuandoEnvioFalha() {
        when(dadosDestinatarioPort.buscarPor(10L))
                .thenReturn(Optional.of(new Destinatario("Maria", "maria@paciente.com")));
        doThrow(new IllegalStateException("provedor indisponível"))
                .when(envioLembretePort).enviar(any(Notificacao.class));

        assertThatThrownBy(() -> useCase.execute(1L, 10L, DATA_HORA, TipoNotificacao.CONSULTA_CRIADA))
                .isInstanceOf(IllegalStateException.class);

        assertThat(capturarNotificacaoSalva().getStatus()).isEqualTo(StatusNotificacao.FALHA);
    }
}
