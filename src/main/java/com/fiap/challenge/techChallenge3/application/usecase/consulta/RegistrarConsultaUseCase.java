package com.fiap.challenge.techChallenge3.application.usecase.consulta;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaEventPublisherPort;
import com.fiap.challenge.techChallenge3.application.port.out.ConsultaRepository;
import com.fiap.challenge.techChallenge3.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.domain.exception.ResourceNotFoundException;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.domain.model.StatusConsulta;
import com.fiap.challenge.techChallenge3.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Registra uma nova consulta (executado por um ENFERMEIRO) e publica o
 * evento correspondente para o Serviço de Notificações.
 */
@Component
public class RegistrarConsultaUseCase {

    private final ConsultaRepository consultaRepository;
    private final UserRepository userRepository;
    private final ConsultaEventPublisherPort eventPublisher;

    public RegistrarConsultaUseCase(ConsultaRepository consultaRepository,
                                     UserRepository userRepository,
                                     ConsultaEventPublisherPort eventPublisher) {
        this.consultaRepository = consultaRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Consulta execute(Long pacienteId, Long medicoId, LocalDateTime dataHora, String observacoes) {
        User paciente = buscarUsuario(pacienteId);
        User medico = buscarUsuario(medicoId);

        Consulta consulta = Consulta.builder()
                .paciente(paciente)
                .medico(medico)
                .dataHora(dataHora)
                .observacoes(observacoes)
                .status(StatusConsulta.AGENDADA)
                .build();

        consulta = consultaRepository.save(consulta);
        eventPublisher.publicarConsultaCriada(consulta);

        return consulta;
    }

    private User buscarUsuario(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }
}
