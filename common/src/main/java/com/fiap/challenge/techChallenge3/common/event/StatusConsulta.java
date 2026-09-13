package com.fiap.challenge.techChallenge3.common.event;

/**
 * Situação de uma consulta.
 *
 * <p>Vive no contrato, e não no domínio do Serviço de Agendamento, porque faz parte
 * do payload de {@link ConsultaEditadaEvent} — o vocabulário publicado precisa ser
 * conhecido pelos dois lados. Acrescentar um valor aqui é mudança de contrato.</p>
 */
public enum StatusConsulta {
    AGENDADA,
    REALIZADA,
    CANCELADA
}
