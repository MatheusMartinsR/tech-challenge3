package com.fiap.challenge.techChallenge3.application.usecase.notificacao;

import com.fiap.challenge.techChallenge3.application.port.out.DadosDestinatarioPort;
import com.fiap.challenge.techChallenge3.application.port.out.EnvioLembretePort;
import com.fiap.challenge.techChallenge3.application.port.out.NotificacaoRepository;
import com.fiap.challenge.techChallenge3.domain.model.Destinatario;
import com.fiap.challenge.techChallenge3.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.domain.model.StatusNotificacao;
import com.fiap.challenge.techChallenge3.domain.model.TipoNotificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Envia ao paciente o lembrete de uma consulta criada ou editada e registra o
 * resultado da tentativa na tabela de notificações.
 *
 * <p>Não é anotado com {@code @Transactional} de propósito: cada {@code save} roda em
 * sua própria transação, de modo que o registro de uma falha permaneça gravado mesmo
 * quando a exceção é propagada para o consumidor da fila.</p>
 */
@Component
public class EnviarLembreteConsultaUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnviarLembreteConsultaUseCase.class);

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final NotificacaoRepository notificacaoRepository;
    private final DadosDestinatarioPort dadosDestinatarioPort;
    private final EnvioLembretePort envioLembretePort;

    public EnviarLembreteConsultaUseCase(NotificacaoRepository notificacaoRepository,
                                          DadosDestinatarioPort dadosDestinatarioPort,
                                          EnvioLembretePort envioLembretePort) {
        this.notificacaoRepository = notificacaoRepository;
        this.dadosDestinatarioPort = dadosDestinatarioPort;
        this.envioLembretePort = envioLembretePort;
    }

    public Notificacao execute(Long consultaId, Long pacienteId, LocalDateTime dataHora, TipoNotificacao tipo) {
        Optional<Destinatario> destinatario = dadosDestinatarioPort.buscarPor(pacienteId);

        if (destinatario.isEmpty()) {
            // Falha permanente: reprocessar a mensagem não mudaria o resultado, então
            // registramos e encerramos sem propagar exceção, evitando reenfileiramento inútil.
            log.warn("Lembrete da consulta {} não enviado: paciente {} não encontrado", consultaId, pacienteId);
            return notificacaoRepository.save(
                    novaNotificacao(consultaId, pacienteId, null, tipo, StatusNotificacao.FALHA,
                            "Destinatário desconhecido para o paciente " + pacienteId));
        }

        Notificacao notificacao = novaNotificacao(consultaId, pacienteId, destinatario.get().email(), tipo,
                StatusNotificacao.ENVIADA, montarMensagem(destinatario.get(), dataHora, tipo));

        try {
            envioLembretePort.enviar(notificacao);
        } catch (RuntimeException e) {
            // Falha potencialmente transitória: grava o insucesso e propaga, para que o
            // tratamento de retry/DLQ da fila possa atuar.
            log.error("Falha ao enviar lembrete da consulta {} para o paciente {}", consultaId, pacienteId, e);
            notificacao.setStatus(StatusNotificacao.FALHA);
            notificacaoRepository.save(notificacao);
            throw e;
        }

        return notificacaoRepository.save(notificacao);
    }

    private Notificacao novaNotificacao(Long consultaId, Long pacienteId, String destinatario,
                                         TipoNotificacao tipo, StatusNotificacao status, String mensagem) {
        return Notificacao.builder()
                .consultaId(consultaId)
                .pacienteId(pacienteId)
                .destinatario(destinatario)
                .tipo(tipo)
                .mensagem(mensagem)
                .status(status)
                .dataEnvio(LocalDateTime.now())
                .build();
    }

    private String montarMensagem(Destinatario destinatario, LocalDateTime dataHora, TipoNotificacao tipo) {
        String acao = tipo == TipoNotificacao.CONSULTA_CRIADA ? "foi agendada" : "foi remarcada";
        return "Olá, %s! Sua consulta %s para %s.".formatted(
                destinatario.nome(), acao, FORMATO_DATA_HORA.format(dataHora));
    }
}
