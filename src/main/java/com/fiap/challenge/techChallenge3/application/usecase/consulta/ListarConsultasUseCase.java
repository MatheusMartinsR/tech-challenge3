package com.fiap.challenge.techChallenge3.application.usecase.consulta;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaRepository;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.domain.model.Role;
import com.fiap.challenge.techChallenge3.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lista consultas de acordo com o perfil do usuário autenticado:
 * MEDICO/ENFERMEIRO veem todo o histórico, PACIENTE vê apenas as próprias.
 */
@Component
public class ListarConsultasUseCase {

    private final ConsultaRepository consultaRepository;

    public ListarConsultasUseCase(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    @Transactional(readOnly = true)
    public List<Consulta> execute(User usuarioAutenticado) {
        return usuarioAutenticado.getRole() == Role.PACIENTE
                ? consultaRepository.findByPacienteId(usuarioAutenticado.getId())
                : consultaRepository.findAll();
    }
}
