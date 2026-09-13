package com.fiap.challenge.techChallenge3.application.usecase.consulta;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaEventPublisherPort;
import com.fiap.challenge.techChallenge3.application.port.out.ConsultaRepository;
import com.fiap.challenge.techChallenge3.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.domain.exception.ResourceNotFoundException;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Edita uma consulta existente (executado por um MEDICO) e publica o
 * evento correspondente para o Serviço de Notificações.
 */
@Component
public class EditarConsultaUseCase {

    private final ConsultaRepository consultaRepository;
    private final UserRepository userRepository;
    private final ConsultaEventPublisherPort eventPublisher;

    public EditarConsultaUseCase(ConsultaRepository consultaRepository,
                                  UserRepository userRepository,
                                  ConsultaEventPublisherPort eventPublisher) {
        this.consultaRepository = consultaRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Consulta execute(Long consultaId, Long pacienteId, Long medicoId, LocalDateTime dataHora, String observacoes) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada: " + consultaId));

        User paciente = buscarUsuario(pacienteId);
        User medico = buscarUsuario(medicoId);

        consulta.setPaciente(paciente);
        consulta.setMedico(medico);
        consulta.setDataHora(dataHora);
        consulta.setObservacoes(observacoes);

        consulta = consultaRepository.save(consulta);
        eventPublisher.publicarConsultaEditada(consulta);

        return consulta;
    }

    private User buscarUsuario(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }
}
