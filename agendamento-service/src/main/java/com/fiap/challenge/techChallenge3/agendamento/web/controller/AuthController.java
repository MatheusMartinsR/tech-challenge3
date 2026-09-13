package com.fiap.challenge.techChallenge3.agendamento.web.controller;

import com.fiap.challenge.techChallenge3.agendamento.application.usecase.auth.AuthResult;
import com.fiap.challenge.techChallenge3.agendamento.application.usecase.auth.LoginUseCase;
import com.fiap.challenge.techChallenge3.agendamento.application.usecase.auth.RegisterUserUseCase;
import com.fiap.challenge.techChallenge3.agendamento.web.dto.auth.AuthResponse;
import com.fiap.challenge.techChallenge3.agendamento.web.dto.auth.LoginRequest;
import com.fiap.challenge.techChallenge3.agendamento.web.dto.auth.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUseCase loginUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = registerUserUseCase.execute(request.nome(), request.email(), request.senha(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(result.token(), result.tokenType()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginUseCase.execute(request.email(), request.senha());
        return ResponseEntity.ok(new AuthResponse(result.token(), result.tokenType()));
    }
}
