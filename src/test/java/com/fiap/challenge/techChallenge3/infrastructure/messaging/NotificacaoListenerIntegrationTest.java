package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.application.port.out.NotificacaoRepository;
import com.fiap.challenge.techChallenge3.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.domain.model.Role;
import com.fiap.challenge.techChallenge3.domain.model.StatusNotificacao;
import com.fiap.challenge.techChallenge3.domain.model.TipoNotificacao;
import com.fiap.challenge.techChallenge3.domain.model.User;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaCriadaEvent;
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
 */
@SpringBootTest
@Transactional
class NotificacaoListenerIntegrationTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 15, 14, 30);

    @Autowired
    private NotificacaoListener notificacaoListener;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Test
    void deveRegistrarNotificacaoEnviadaQuandoPacienteExiste() {
        User paciente = userRepository.save(User.builder()
                .nome("Maria").email("maria@paciente.com").senha("hash").role(Role.PACIENTE).build());

        notificacaoListener.receberConsultaCriada(
                new ConsultaCriadaEvent(1L, paciente.getId(), 20L, DATA_HORA));

        List<Notificacao> notificacoes = notificacaoRepository.findByPacienteId(paciente.getId());
        assertThat(notificacoes).hasSize(1);
        assertThat(notificacoes.get(0).getStatus()).isEqualTo(StatusNotificacao.ENVIADA);
        assertThat(notificacoes.get(0).getDestinatario()).isEqualTo("maria@paciente.com");
        assertThat(notificacoes.get(0).getTipo()).isEqualTo(TipoNotificacao.CONSULTA_CRIADA);
        assertThat(notificacoes.get(0).getMensagem()).contains("Maria", "15/10/2026");
    }

    @Test
    void deveRegistrarFalhaQuandoPacienteNaoExiste() {
        long pacienteInexistente = 999_999L;

        notificacaoListener.receberConsultaCriada(
                new ConsultaCriadaEvent(1L, pacienteInexistente, 20L, DATA_HORA));

        List<Notificacao> notificacoes = notificacaoRepository.findByPacienteId(pacienteInexistente);
        assertThat(notificacoes).hasSize(1);
        assertThat(notificacoes.get(0).getStatus()).isEqualTo(StatusNotificacao.FALHA);
        assertThat(notificacoes.get(0).getDestinatario()).isNull();
    }
}
