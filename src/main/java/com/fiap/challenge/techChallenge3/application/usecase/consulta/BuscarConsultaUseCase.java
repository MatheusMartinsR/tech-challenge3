package com.fiap.challenge.techChallenge3.application.usecase.consulta;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaRepository;
import com.fiap.challenge.techChallenge3.domain.exception.AcessoNegadoException;
import com.fiap.challenge.techChallenge3.domain.exception.ResourceNotFoundException;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.domain.model.Role;
import com.fiap.challenge.techChallenge3.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Busca uma consulta por id, garantindo que um PACIENTE só acesse as
 * próprias consultas.
 */
@Component
public class BuscarConsultaUseCase {

    private final ConsultaRepository consultaRepository;

    public BuscarConsultaUseCase(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    @Transactional(readOnly = true)
    public Consulta execute(Long id, User usuarioAutenticado) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada: " + id));

        boolean pacienteTentandoAcessarConsultaDeOutrem = usuarioAutenticado.getRole() == Role.PACIENTE
                && !consulta.getPaciente().getId().equals(usuarioAutenticado.getId());

        if (pacienteTentandoAcessarConsultaDeOutrem) {
            throw new AcessoNegadoException("Paciente só pode visualizar as próprias consultas");
        }

        return consulta;
    }
}
