package com.luxuriusroom.service;

import com.luxuriusroom.config.JwtService;
import com.luxuriusroom.dto.LoginRequest;
import com.luxuriusroom.dto.RegistroRequest;
import com.luxuriusroom.exception.BadRequestException;
import com.luxuriusroom.exception.ConflictException;
import com.luxuriusroom.exception.InvalidCredentialsException;
import com.luxuriusroom.model.RolUsuario;
import com.luxuriusroom.model.Usuario;
import com.luxuriusroom.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService authService;

    @Test
    void registroNormalizaEmailYGuardaPasswordHasheada() {
        var request = new RegistroRequest("Ana", "Perez", " ANA@EXAMPLE.TEST ", "Clave1234", "3001234567", RolUsuario.HUESPED);
        when(usuarioRepository.findByEmailIgnoreCase("ana@example.test")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Clave1234")).thenReturn("hash-seguro");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario result = authService.registrar(request);

        assertEquals("ana@example.test", result.getEmail());
        assertEquals("hash-seguro", result.getPasswordHash());
        assertEquals(RolUsuario.HUESPED, result.getRol());
    }

    @Test
    void registroPublicoRechazaAdministradorAntesDeGuardar() {
        var request = new RegistroRequest("Admin", "Demo", "admin@example.test", "Clave1234", null, RolUsuario.ADMINISTRADOR);

        assertThrows(BadRequestException.class, () -> authService.registrar(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registroRechazaEmailDuplicadoSinDistinguirMayusculas() {
        var request = new RegistroRequest("Ana", "Perez", "ANA@example.test", "Clave1234", null, RolUsuario.ANFITRION);
        when(usuarioRepository.findByEmailIgnoreCase("ana@example.test")).thenReturn(Optional.of(new Usuario()));

        assertThrows(ConflictException.class, () -> authService.registrar(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void loginEmiteTokenYRechazaCredencialesInvalidas() {
        Usuario usuario = new Usuario();
        usuario.setId(23L);
        usuario.setNombre("Ana");
        usuario.setEmail("ana@example.test");
        usuario.setPasswordHash("hash-seguro");
        usuario.setRol(RolUsuario.ANFITRION);
        usuario.setActivo(true);
        when(usuarioRepository.findByEmailIgnoreCase("ana@example.test")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave1234", "hash-seguro")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("jwt-firmado");

        var response = authService.login(new LoginRequest("ana@example.test", "Clave1234"));

        assertEquals("jwt-firmado", response.token());
        assertEquals(RolUsuario.ANFITRION, response.usuario().rol());
        when(passwordEncoder.matches("incorrecta", "hash-seguro")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class,
            () -> authService.login(new LoginRequest("ana@example.test", "incorrecta")));

        usuario.setActivo(false);
        assertThrows(InvalidCredentialsException.class,
            () -> authService.login(new LoginRequest("ana@example.test", "Clave1234")));
    }
}