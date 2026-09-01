package com.fiap.challenge.techChallenge3.infrastructure.persistence;

import com.fiap.challenge.techChallenge3.application.port.out.NotificacaoRepository;
import com.fiap.challenge.techChallenge3.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.domain.model.StatusNotificacao;
import com.fiap.challenge.techChallenge3.domain.model.TipoNotificacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercita a persistência de notificações contra um PostgreSQL real (Testcontainers),
 * a mesma imagem usada no docker-compose. Cobre o que o H2 dos demais testes não
 * garante: tipos, enums e a coluna de destinatário anulável no banco de produção.
 *
 * <p>É automaticamente ignorado (não falha) em ambientes sem Docker disponível,
 * graças a {@code @EnabledIfDockerAvailable}.</p>
 */
@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
class NotificacaoPersistenceIntegrationTest {

    private static final LocalDateTime DATA_ENVIO = LocalDateTime.of(2026, 10, 15, 14, 30);

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    private Notificacao novaNotificacao(Long pacienteId, String destinatario, StatusNotificacao status) {
        return Notificacao.builder()
                .consultaId(1L)
                .pacienteId(pacienteId)
                .destinatario(destinatario)
                .tipo(TipoNotificacao.CONSULTA_CRIADA)
                .mensagem("Olá, Maria! Sua consulta foi agendada para 15/10/2026 às 14:30.")
                .status(status)
                .dataEnvio(DATA_ENVIO)
                .build();
    }

    @Test
    void devePersistirERecuperarNotificacaoEnviada() {
        Notificacao salva = notificacaoRepository.save(
                novaNotificacao(10L, "maria@paciente.com", StatusNotificacao.ENVIADA));

        assertThat(salva.getId()).isNotNull();

        List<Notificacao> encontradas = notificacaoRepository.findByPacienteId(10L);
        assertThat(encontradas).hasSize(1);
        assertThat(encontradas.get(0).getDestinatario()).isEqualTo("maria@paciente.com");
        assertThat(encontradas.get(0).getStatus()).isEqualTo(StatusNotificacao.ENVIADA);
        assertThat(encontradas.get(0).getTipo()).isEqualTo(TipoNotificacao.CONSULTA_CRIADA);
        assertThat(encontradas.get(0).getDataEnvio()).isEqualTo(DATA_ENVIO);
        assertThat(encontradas.get(0).getMensagem()).contains("Olá", "às");
    }

    @Test
    void devePersistirNotificacaoDeFalhaComDestinatarioNulo() {
        notificacaoRepository.save(novaNotificacao(20L, null, StatusNotificacao.FALHA));

        List<Notificacao> encontradas = notificacaoRepository.findByPacienteId(20L);
        assertThat(encontradas).hasSize(1);
        assertThat(encontradas.get(0).getDestinatario()).isNull();
        assertThat(encontradas.get(0).getStatus()).isEqualTo(StatusNotificacao.FALHA);
    }

    @Test
    void deveFiltrarNotificacoesPorPaciente() {
        notificacaoRepository.save(novaNotificacao(30L, "a@paciente.com", StatusNotificacao.ENVIADA));
        notificacaoRepository.save(novaNotificacao(31L, "b@paciente.com", StatusNotificacao.ENVIADA));

        assertThat(notificacaoRepository.findByPacienteId(30L)).hasSize(1);
        assertThat(notificacaoRepository.findAll()).extracting(Notificacao::getPacienteId).contains(30L, 31L);
    }
}
