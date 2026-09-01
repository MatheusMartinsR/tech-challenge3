package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.application.usecase.notificacao.EnviarLembreteConsultaUseCase;
import com.fiap.challenge.techChallenge3.domain.model.TipoNotificacao;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaEditadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ponto de entrada do Serviço de Notificações: consome os eventos de consulta
 * publicados pelo Serviço de Agendamento e delega o envio do lembrete ao
 * {@link EnviarLembreteConsultaUseCase}.
 */
@Component
public class NotificacaoListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoListener.class);

    private final EnviarLembreteConsultaUseCase enviarLembreteConsultaUseCase;

    private final AtomicInteger notificacoesEnviadas = new AtomicInteger(0);

    public NotificacaoListener(EnviarLembreteConsultaUseCase enviarLembreteConsultaUseCase) {
        this.enviarLembreteConsultaUseCase = enviarLembreteConsultaUseCase;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICACAO_QUEUE)
    public void receberEventoConsulta(Object evento) {
        if (evento instanceof ConsultaCriadaEvent criada) {
            enviarLembreteConsultaUseCase.execute(
                    criada.consultaId(), criada.pacienteId(), criada.dataHora(), TipoNotificacao.CONSULTA_CRIADA);
        } else if (evento instanceof ConsultaEditadaEvent editada) {
            enviarLembreteConsultaUseCase.execute(
                    editada.consultaId(), editada.pacienteId(), editada.dataHora(), TipoNotificacao.CONSULTA_EDITADA);
        } else {
            log.warn("Evento de consulta desconhecido recebido: {}", evento);
            return;
        }
        notificacoesEnviadas.incrementAndGet();
    }

    public int getNotificacoesEnviadas() {
        return notificacoesEnviadas.get();
    }
}
