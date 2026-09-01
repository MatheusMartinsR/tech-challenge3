package com.fiap.challenge.techChallenge3.infrastructure.notificacao;

import com.fiap.challenge.techChallenge3.application.port.out.DadosDestinatarioPort;
import com.fiap.challenge.techChallenge3.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.domain.model.Destinatario;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Obtém os dados de contato do paciente a partir do repositório de usuários.
 *
 * <p>Solução provisória: os eventos de consulta publicados hoje carregam apenas
 * identificadores. Caso o payload passe a trazer nome e e-mail do paciente, este
 * adapter deixa de ser necessário e o destinatário passa a vir direto do evento,
 * sem alteração no caso de uso nem nos testes.</p>
 */
@Component
public class UserDadosDestinatarioAdapter implements DadosDestinatarioPort {

    private final UserRepository userRepository;

    public UserDadosDestinatarioAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Destinatario> buscarPor(Long pacienteId) {
        return userRepository.findById(pacienteId)
                .map(user -> new Destinatario(user.getNome(), user.getEmail()));
    }
}
