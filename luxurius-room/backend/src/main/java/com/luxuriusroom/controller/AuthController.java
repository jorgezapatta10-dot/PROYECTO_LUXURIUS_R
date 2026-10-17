package com.luxuriusroom.controller;

import com.luxuriusroom.dto.AuthResponse;
import com.luxuriusroom.dto.LoginRequest;
import com.luxuriusroom.dto.RegistroRequest;
import com.luxuriusroom.dto.UsuarioResponse;
import com.luxuriusroom.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(authService.registrar(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
