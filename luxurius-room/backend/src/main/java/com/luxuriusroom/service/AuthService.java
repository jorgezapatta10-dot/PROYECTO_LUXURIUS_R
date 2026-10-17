package com.luxuriusroom.service;

import com.luxuriusroom.config.JwtService;
import com.luxuriusroom.dto.AuthResponse;
import com.luxuriusroom.dto.LoginRequest;
import com.luxuriusroom.dto.RegistroRequest;
import com.luxuriusroom.exception.BadRequestException;
import com.luxuriusroom.exception.ConflictException;
import com.luxuriusroom.exception.InvalidCredentialsException;
import com.luxuriusroom.model.RolUsuario;
import com.luxuriusroom.model.Usuario;
import com.luxuriusroom.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Usuario registrar(RegistroRequest request) {
        if (request.rol() != RolUsuario.HUESPED && request.rol() != RolUsuario.ANFITRION) {
            throw new BadRequestException("Solo se permite crear usuario HUESPED o ANFITRION");
        }
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ConflictException("El correo ya existe");
        }
        if (!request.password().matches("(?=.*[A-Za-z])(?=.*\\d).*")) {
            throw new BadRequestException("La contraseña debe tener al menos 8 caracteres, letras y números");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setTelefono(request.telefono());
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(InvalidCredentialsException::new);

        if (!usuario.isActivo() || !passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token, new AuthResponse.UsuarioBasico(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol()));
    }
}
