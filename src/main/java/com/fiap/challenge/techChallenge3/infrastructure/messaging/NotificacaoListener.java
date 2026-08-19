package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaEditadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa o Serviço de Notificações: consome os eventos de consulta publicados
 * pelo Serviço de Agendamento e "notifica" os envolvidos (aqui, simulado via log).
 */
@Component
public class NotificacaoListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoListener.class);

    private final AtomicInteger notificacoesEnviadas = new AtomicInteger(0);

    @RabbitListener(queues = RabbitMQConfig.NOTIFICACAO_QUEUE)
    public void receberEventoConsulta(Object evento) {
        if (evento instanceof ConsultaCriadaEvent criada) {
            log.info("Notificando paciente {} e médico {}: consulta {} agendada para {}",
                    criada.pacienteId(), criada.medicoId(), criada.consultaId(), criada.dataHora());
        } else if (evento instanceof ConsultaEditadaEvent editada) {
            log.info("Notificando paciente {} e médico {}: consulta {} atualizada (status {})",
                    editada.pacienteId(), editada.medicoId(), editada.consultaId(), editada.status());
        } else {
            log.warn("Evento de consulta desconhecido recebido: {}", evento);
        }
        notificacoesEnviadas.incrementAndGet();
    }

    public int getNotificacoesEnviadas() {
        return notificacoesEnviadas.get();
    }
}
