package com.luxuriusroom.dto;

import com.luxuriusroom.model.RolUsuario;
import com.luxuriusroom.model.Usuario;

public record UsuarioResponse(Long id, String nombre, String apellido, String email, String telefono, RolUsuario rol) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getApellido(),
            usuario.getEmail(), usuario.getTelefono(), usuario.getRol());
    }
}