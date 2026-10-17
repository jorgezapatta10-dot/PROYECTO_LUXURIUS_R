package com.luxuriusroom.service;

import com.luxuriusroom.dto.PasswordChangeRequest;
import com.luxuriusroom.dto.PerfilRequest;
import com.luxuriusroom.exception.BadRequestException;
import com.luxuriusroom.exception.ResourceNotFoundException;
import com.luxuriusroom.model.Reserva;
import com.luxuriusroom.model.Usuario;
import com.luxuriusroom.repository.ReservaRepository;
import com.luxuriusroom.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("No hay usuario autenticado");
        }
        Long id = Long.parseLong(authentication.getName());
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public Usuario obtenerPerfil(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public Usuario actualizarPerfil(Long id, PerfilRequest request) {
        Usuario usuario = obtenerPerfil(id);
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setTelefono(request.telefono());
        return usuarioRepository.save(usuario);
    }

    public Usuario cambiarPassword(Long id, PasswordChangeRequest request) {
        Usuario usuario = obtenerPerfil(id);
        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual no coincide");
        }
        usuario.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
        return usuarioRepository.save(usuario);
    }

    public List<Reserva> reservasUsuario(Long id) {
        Usuario usuario = obtenerPerfil(id);
        return reservaRepository.findByUsuario(usuario);
    }
}
