package com.luxuriusroom.service;

import com.luxuriusroom.dto.AlojamientoAdminResponse;
import com.luxuriusroom.dto.ReservaAdminResponse;
import com.luxuriusroom.dto.UsuarioAdminResponse;
import com.luxuriusroom.exception.ResourceNotFoundException;
import com.luxuriusroom.model.*;
import com.luxuriusroom.repository.AlojamientoRepository;
import com.luxuriusroom.repository.ReservaRepository;
import com.luxuriusroom.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private final UsuarioRepository usuarioRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final ReservaRepository reservaRepository;
    private final AuditoriaService auditoriaService;

    public AdminService(UsuarioRepository usuarioRepository, AlojamientoRepository alojamientoRepository,
                        ReservaRepository reservaRepository, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.reservaRepository = reservaRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<UsuarioAdminResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(UsuarioAdminResponse::desde).toList();
    }

    public UsuarioAdminResponse cambiarEstadoUsuario(Long id, boolean activo, Usuario administrador) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.setActivo(activo);
        Usuario guardado = usuarioRepository.save(usuario);
        auditoriaService.guardar(administrador, activo ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO",
            "usuario", guardado.getId(), guardado.getEmail());
        return UsuarioAdminResponse.desde(guardado);
    }

    @Transactional(readOnly = true)
    public List<AlojamientoAdminResponse> listarAlojamientos() {
        return alojamientoRepository.findAll().stream().map(alojamiento -> new AlojamientoAdminResponse(
            alojamiento.getId(), alojamiento.getNombre(), alojamiento.getTipo(), alojamiento.getCiudad(),
            alojamiento.getPais(), alojamiento.getAnfitrion().getEmail(), alojamiento.getEstado())).toList();
    }

    @Transactional
    public AlojamientoAdminResponse cambiarEstadoAlojamiento(Long id, EstadoAlojamiento estado, Usuario administrador) {
        Alojamiento alojamiento = alojamientoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado"));
        alojamiento.setEstado(estado);
        Alojamiento guardado = alojamientoRepository.save(alojamiento);
        auditoriaService.guardar(administrador, estado == EstadoAlojamiento.ACTIVO ? "APROBAR_ALOJAMIENTO" : "SUSPENDER_ALOJAMIENTO",
            "alojamiento", guardado.getId(), estado.name());
        return new AlojamientoAdminResponse(guardado.getId(), guardado.getNombre(), guardado.getTipo(),
            guardado.getCiudad(), guardado.getPais(), guardado.getAnfitrion().getEmail(), guardado.getEstado());
    }

    @Transactional(readOnly = true)
    public List<ReservaAdminResponse> listarReservas() {
        return reservaRepository.findAll().stream().map(ReservaAdminResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> estadisticas() {
        Map<String, Object> response = new HashMap<>();
        response.put("usuariosTotales", usuarioRepository.count());
        response.put("alojamientosPorEstado", Map.of(
            "PENDIENTE_APROBACION", alojamientoRepository.findByEstado(EstadoAlojamiento.PENDIENTE_APROBACION, org.springframework.data.domain.Pageable.unpaged()).getTotalElements(),
            "ACTIVO", alojamientoRepository.findByEstado(EstadoAlojamiento.ACTIVO, org.springframework.data.domain.Pageable.unpaged()).getTotalElements(),
            "SUSPENDIDO", alojamientoRepository.findByEstado(EstadoAlojamiento.SUSPENDIDO, org.springframework.data.domain.Pageable.unpaged()).getTotalElements()
        ));
        response.put("reservasPorEstado", Map.of(
            "PENDIENTE_CONFIRMACION", reservaRepository.findByEstadoReserva(EstadoReserva.PENDIENTE_CONFIRMACION).size(),
            "CONFIRMADA", reservaRepository.findByEstadoReserva(EstadoReserva.CONFIRMADA).size(),
            "CANCELADA", reservaRepository.findByEstadoReserva(EstadoReserva.CANCELADA).size(),
            "COMPLETADA", reservaRepository.findByEstadoReserva(EstadoReserva.COMPLETADA).size(),
            "NO_PRESENTADO", reservaRepository.findByEstadoReserva(EstadoReserva.NO_PRESENTADO).size()
        ));
        LocalDate primerDia = LocalDate.now().withDayOfMonth(1);
        response.put("reservasDelMes", reservaRepository.countByCreadoEnGreaterThanEqualAndCreadoEnLessThan(
            primerDia.atStartOfDay(), primerDia.plusMonths(1).atStartOfDay()));
        return response;
    }
}
