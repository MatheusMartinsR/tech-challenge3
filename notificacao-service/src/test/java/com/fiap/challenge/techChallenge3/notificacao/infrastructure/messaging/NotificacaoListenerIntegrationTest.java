package com.fiap.challenge.techChallenge3.notificacao.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.notificacao.application.port.out.NotificacaoRepository;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.StatusNotificacao;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.TipoNotificacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida o Serviço de Notificações de ponta a ponta a partir do consumidor, com os
 * adapters e o banco reais — sem depender de um broker RabbitMQ, invocando o listener
 * diretamente. Cobre o registro na tabela de notificações nos dois desfechos.
 *
 * <p>Repare que não há nenhum usuário sendo criado: todo o dado necessário chega no
 * evento. Este serviço não tem acesso à tabela de usuários do Serviço de Agendamento.</p>
 */
@SpringBootTest
@Transactional
class NotificacaoListenerIntegrationTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 15, 14, 30);

    @Autowired
    private NotificacaoListener notificacaoListener;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Test
    void deveRegistrarNotificacaoEnviadaQuandoEventoTrazContatoDoPaciente() {
        long pacienteId = 10L;

        notificacaoListener.receberConsultaCriada(new ConsultaCriadaEvent(
                1L, pacienteId, "Maria", "maria@paciente.com", 20L, DATA_HORA));

        List<Notificacao> notificacoes = notificacaoRepository.findByPacienteId(pacienteId);
        assertThat(notificacoes).hasSize(1);
        assertThat(notificacoes.get(0).getStatus()).isEqualTo(StatusNotificacao.ENVIADA);
        assertThat(notificacoes.get(0).getDestinatario()).isEqualTo("maria@paciente.com");
        assertThat(notificacoes.get(0).getTipo()).isEqualTo(TipoNotificacao.CONSULTA_CRIADA);
        assertThat(notificacoes.get(0).getMensagem()).contains("Maria", "15/10/2026");
    }

    @Test
    void deveRegistrarFalhaQuandoEventoNaoTrazContatoDoPaciente() {
        long pacienteSemContato = 999_999L;

        notificacaoListener.receberConsultaCriada(new ConsultaCriadaEvent(
                1L, pacienteSemContato, null, null, 20L, DATA_HORA));

        List<Notificacao> notificacoes = notificacaoRepository.findByPacienteId(pacienteSemContato);
        assertThat(notificacoes).hasSize(1);
        assertThat(notificacoes.get(0).getStatus()).isEqualTo(StatusNotificacao.FALHA);
        assertThat(notificacoes.get(0).getDestinatario()).isNull();
    }
}
