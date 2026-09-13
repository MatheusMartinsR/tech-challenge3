package com.fiap.challenge.techChallenge3.common.event;

/**
 * Nomes de exchange e routing key dos eventos de consulta.
 *
 * <p>Fazem parte do contrato tanto quanto o formato do payload: o publicador envia
 * para {@link #EXCHANGE} com uma destas routing keys, e cada consumidor liga a
 * própria fila à mesma exchange. Ficam aqui, e não na configuração de um dos
 * serviços, para que nenhum dos dois precise importar classes do outro.</p>
 *
 * <p>A exchange é declarada pelos dois lados. Declarar uma exchange que já existe,
 * com os mesmos atributos, é idempotente no RabbitMQ — assim nenhum serviço depende
 * de o outro ter subido primeiro.</p>
 */
public final class ConsultaEventos {

    public static final String EXCHANGE = "consultas.exchange";
    public static final String ROUTING_KEY_CONSULTA_CRIADA = "consulta.criada";
    public static final String ROUTING_KEY_CONSULTA_EDITADA = "consulta.editada";

    /** Pacote dos eventos, liberado na desserialização JSON dos dois lados. */
    public static final String PACOTE_EVENTOS = "com.fiap.challenge.techChallenge3.common.event";

    private ConsultaEventos() {
    }
}
