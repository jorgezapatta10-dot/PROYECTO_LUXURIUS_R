package com.luxuriusroom.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxuriusroom.model.RolUsuario;
import com.luxuriusroom.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioResponseTest {
    @Test
    void respuestaPublicaNoSerializaHashDeContrasena() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(8L);
        usuario.setNombre("Eva");
        usuario.setApellido("Lopez");
        usuario.setEmail("eva@example.test");
        usuario.setPasswordHash("hash-privado");
        usuario.setRol(RolUsuario.HUESPED);

        String json = new ObjectMapper().writeValueAsString(UsuarioResponse.desde(usuario));

        assertTrue(json.contains("eva@example.test"));
        assertFalse(json.contains("passwordHash"));
        assertFalse(json.contains("hash-privado"));
    }

    @Test
    void entidadUsuarioTampocoSerializaHash() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setEmail("eva@example.test");
        usuario.setPasswordHash("hash-privado");

        String json = new ObjectMapper().writeValueAsString(usuario);

        assertFalse(json.contains("passwordHash"));
        assertFalse(json.contains("hash-privado"));
    }
}