package com.luxuriusroom.controller;

import com.luxuriusroom.dto.PasswordChangeRequest;
import com.luxuriusroom.dto.PerfilRequest;
import com.luxuriusroom.dto.ReservaResponse;
import com.luxuriusroom.dto.UsuarioResponse;
import com.luxuriusroom.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> perfil() {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.obtenerUsuarioActual()));
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@Valid @RequestBody PerfilRequest request) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.actualizarPerfil(usuarioService.obtenerUsuarioActual().getId(), request)));
    }

    @PutMapping("/perfil/password")
    public ResponseEntity<UsuarioResponse> cambiarPassword(@Valid @RequestBody PasswordChangeRequest request) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.cambiarPassword(usuarioService.obtenerUsuarioActual().getId(), request)));
    }

    @GetMapping("/reservas")
    public ResponseEntity<?> reservas() {
        return ResponseEntity.ok(usuarioService.reservasUsuario(usuarioService.obtenerUsuarioActual().getId()).stream()
            .map(ReservaResponse::desde)
            .toList());
    }
}
