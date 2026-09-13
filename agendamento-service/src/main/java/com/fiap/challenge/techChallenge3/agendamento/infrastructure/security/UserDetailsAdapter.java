package com.fiap.challenge.techChallenge3.agendamento.infrastructure.security;

import com.fiap.challenge.techChallenge3.agendamento.domain.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapta o {@link User} de domínio para o contrato {@link UserDetails} exigido
 * pelo Spring Security, mantendo o modelo de domínio livre dessa dependência.
 */
public class UserDetailsAdapter implements UserDetails {

    @Getter
    private final User user;

    public UserDetailsAdapter(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getSenha();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
