package com.fiap.challenge.techChallenge3.agendamento.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEditadaEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que os eventos de consulta são corretamente serializados/deserializados
 * em JSON (via Jackson) pelo conversor usado no producer/consumer do RabbitMQ.
 */
class ConsultaEventMessageConverterTest {

    // Mesmo bean usado em produção (RabbitMQConfig), garantindo que o teste valide
    // exatamente o conversor configurado para o producer/consumer reais.
    private final Jackson2JsonMessageConverter converter =
            (Jackson2JsonMessageConverter) new RabbitMQConfig().jsonMessageConverter();

    @Test
    void deveSerializarEDeserializarConsultaCriadaEventComoJson() {
        ConsultaCriadaEvent event = new ConsultaCriadaEvent(
                1L, 10L, "Maria", "maria@paciente.com", 20L, LocalDateTime.now().withNano(0).plusDays(1));

        Message message = converter.toMessage(event, new MessageProperties());

        assertThat(new String(message.getBody(), StandardCharsets.UTF_8))
                .contains("\"consultaId\":1")
                .contains("\"pacienteEmail\":\"maria@paciente.com\"");

        Object recovered = converter.fromMessage(message);

        assertThat(recovered).isInstanceOf(ConsultaCriadaEvent.class);
        assertThat(recovered).isEqualTo(event);
    }

    @Test
    void deveSerializarEDeserializarConsultaEditadaEventComoJson() {
        ConsultaEditadaEvent event = new ConsultaEditadaEvent(
                1L, 10L, "Maria", "maria@paciente.com", 20L,
                LocalDateTime.now().withNano(0).plusDays(1), StatusConsulta.REALIZADA);

        Message message = converter.toMessage(event, new MessageProperties());

        assertThat(new String(message.getBody(), StandardCharsets.UTF_8)).contains("\"status\":\"REALIZADA\"");

        Object recovered = converter.fromMessage(message);

        assertThat(recovered).isInstanceOf(ConsultaEditadaEvent.class);
        assertThat(recovered).isEqualTo(event);
    }
}
