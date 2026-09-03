package com.fiap.challenge.techChallenge3.notificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do Serviço de Notificações.
 *
 * <p>Processo independente do Serviço de Agendamento: sobe seu próprio contexto
 * Spring, com seu próprio banco, e se comunica com o outro serviço apenas pelos
 * eventos que consome do RabbitMQ.</p>
 */
@SpringBootApplication
public class NotificacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacaoApplication.class, args);
    }
}
