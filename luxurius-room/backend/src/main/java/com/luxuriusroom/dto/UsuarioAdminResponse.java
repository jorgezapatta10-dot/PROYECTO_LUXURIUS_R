package com.luxuriusroom.dto;

import com.luxuriusroom.model.RolUsuario;
import com.luxuriusroom.model.Usuario;

public record UsuarioAdminResponse(Long id, String nombre, String apellido, String email, String telefono,
                                   RolUsuario rol, boolean activo) {
    public static UsuarioAdminResponse desde(Usuario usuario) {
        return new UsuarioAdminResponse(usuario.getId(), usuario.getNombre(), usuario.getApellido(),
            usuario.getEmail(), usuario.getTelefono(), usuario.getRol(), usuario.isActivo());
    }
}