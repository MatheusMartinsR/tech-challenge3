package com.fiap.challenge.techChallenge3.notificacao.infrastructure.envio;

import com.fiap.challenge.techChallenge3.notificacao.application.port.out.EnvioLembretePort;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.Notificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Envio simulado do lembrete: registra a mensagem em log, no lugar de um provedor
 * real de e-mail ou SMS. Substituível sem impacto no caso de uso, por conta da porta
 * {@link EnvioLembretePort}.
 */
@Component
public class LogEnvioLembreteAdapter implements EnvioLembretePort {

    private static final Logger log = LoggerFactory.getLogger(LogEnvioLembreteAdapter.class);

    @Override
    public void enviar(Notificacao notificacao) {
        log.info("[LEMBRETE] para={} | consulta={} | {}",
                notificacao.getDestinatario(), notificacao.getConsultaId(), notificacao.getMensagem());
    }
}
