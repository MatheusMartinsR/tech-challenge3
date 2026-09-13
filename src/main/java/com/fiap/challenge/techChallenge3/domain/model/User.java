package com.fiap.challenge.techChallenge3.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuário da aplicação (médico, enfermeiro ou paciente).
 *
 * <p>Modelo de domínio puro: não conhece JPA, Spring Security ou qualquer
 * outro detalhe de infraestrutura.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;
    private String nome;
    private String email;
    private String senha;
    private Role role;
}
