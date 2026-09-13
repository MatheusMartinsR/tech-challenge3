package com.fiap.challenge.techChallenge3.notificacao.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEditadaEvent;
import com.fiap.challenge.techChallenge3.notificacao.application.usecase.EnviarLembreteConsultaUseCase;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.Destinatario;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.TipoNotificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ponto de entrada do Serviço de Notificações: consome os eventos de consulta
 * publicados pelo Serviço de Agendamento e delega o envio do lembrete ao
 * {@link EnviarLembreteConsultaUseCase}.
 *
 * <p>A fila recebe mais de um tipo de evento, por isso a assinatura usa
 * {@code @RabbitListener} na classe com um {@code @RabbitHandler} por tipo: é o que
 * permite ao conversor JSON resolver o payload pelo cabeçalho {@code __TypeId__}.
 * Um único método recebendo {@code Object} não é desserializado — a mensagem bruta
 * chega ao listener e nenhum evento é reconhecido.</p>
 */
@Component
@RabbitListener(queues = NotificacaoRabbitConfig.NOTIFICACAO_QUEUE)
public class NotificacaoListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoListener.class);

    private final EnviarLembreteConsultaUseCase enviarLembreteConsultaUseCase;

    private final AtomicInteger notificacoesEnviadas = new AtomicInteger(0);

    public NotificacaoListener(EnviarLembreteConsultaUseCase enviarLembreteConsultaUseCase) {
        this.enviarLembreteConsultaUseCase = enviarLembreteConsultaUseCase;
    }

    @RabbitHandler
    public void receberConsultaCriada(ConsultaCriadaEvent evento) {
        processar(evento.consultaId(), evento.pacienteId(),
                new Destinatario(evento.pacienteNome(), evento.pacienteEmail()),
                evento.dataHora(), TipoNotificacao.CONSULTA_CRIADA);
    }

    @RabbitHandler
    public void receberConsultaEditada(ConsultaEditadaEvent evento) {
        processar(evento.consultaId(), evento.pacienteId(),
                new Destinatario(evento.pacienteNome(), evento.pacienteEmail()),
                evento.dataHora(), TipoNotificacao.CONSULTA_EDITADA);
    }

    @RabbitHandler(isDefault = true)
    public void receberEventoDesconhecido(Object evento) {
        log.warn("Evento de consulta desconhecido recebido: {}", evento);
    }

    private void processar(Long consultaId, Long pacienteId, Destinatario destinatario,
                            LocalDateTime dataHora, TipoNotificacao tipo) {
        enviarLembreteConsultaUseCase.execute(consultaId, pacienteId, destinatario, dataHora, tipo);
        notificacoesEnviadas.incrementAndGet();
    }

    public int getNotificacoesEnviadas() {
        return notificacoesEnviadas.get();
    }
}
