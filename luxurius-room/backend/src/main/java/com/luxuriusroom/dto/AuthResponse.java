package com.luxuriusroom.dto;

import com.luxuriusroom.model.RolUsuario;

public record AuthResponse(
    String token,
    UsuarioBasico usuario
) {
    public record UsuarioBasico(Long id, String nombre, String email, RolUsuario rol) {}
}
