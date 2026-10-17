package com.luxuriusroom.service;

import com.luxuriusroom.dto.ReporteResenaRequest;
import com.luxuriusroom.dto.ResenaRequest;
import com.luxuriusroom.dto.ResenaResponse;
import com.luxuriusroom.exception.BadRequestException;
import com.luxuriusroom.exception.ConflictException;
import com.luxuriusroom.exception.ForbiddenException;
import com.luxuriusroom.exception.ResourceNotFoundException;
import com.luxuriusroom.model.*;
import com.luxuriusroom.repository.AlojamientoRepository;
import com.luxuriusroom.repository.ResenaRepository;
import com.luxuriusroom.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResenaService {
    private final ResenaRepository resenaRepository;
    private final ReservaRepository reservaRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final AuditoriaService auditoriaService;

    public ResenaService(ResenaRepository resenaRepository, ReservaRepository reservaRepository,
                         AlojamientoRepository alojamientoRepository, AuditoriaService auditoriaService) {
        this.resenaRepository = resenaRepository;
        this.reservaRepository = reservaRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ResenaResponse crear(Usuario usuario, ResenaRequest request) {
        if (usuario.getRol() != RolUsuario.HUESPED) {
            throw new ForbiddenException("Solo un huesped puede crear resenas");
        }
        Reserva reserva = reservaRepository.findByCodigoForUpdate(request.codigoReserva())
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        if (!reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Solo puedes resenar tus reservas");
        }
        if (reserva.getEstadoReserva() != EstadoReserva.COMPLETADA) {
            throw new BadRequestException("Solo puedes reseñar reservas completadas");
        }
        if (resenaRepository.findByReserva(reserva).isPresent()) {
            throw new ConflictException("Ya existe una reseña para esta reserva");
        }
        Resena resena = new Resena();
        resena.setReserva(reserva);
        resena.setUsuario(usuario);
        resena.setAlojamiento(reserva.getHabitacion().getAlojamiento());
        resena.setCalificacion(request.calificacion());
        resena.setComentario(request.comentario());
        resena.setEstado(EstadoResena.VISIBLE);
        resena.setReportada(false);
        resena.setCreadoEn(LocalDateTime.now());
        Resena guardada = resenaRepository.save(resena);
        recalcularPromedio(guardada.getAlojamiento());
        auditoriaService.guardar(usuario, "CREAR_RESENA", "resena", guardada.getId(), guardada.getCalificacion().toString());
        return ResenaResponse.desde(guardada);
    }

    @Transactional(readOnly = true)
    public List<ResenaResponse> listarAnfitrion(Usuario anfitrion) {
        exigirRol(anfitrion, RolUsuario.ANFITRION, "Solo un anfitrion puede consultar estas resenas");
        return resenaRepository.findByAnfitrionId(anfitrion.getId()).stream().map(ResenaResponse::desde).toList();
    }

    @Transactional
    public ResenaResponse reportar(Long id, Usuario anfitrion, ReporteResenaRequest request) {
        exigirRol(anfitrion, RolUsuario.ANFITRION, "Solo un anfitrion puede reportar resenas");
        Resena resena = obtener(id);
        if (!resena.getAlojamiento().getAnfitrion().getId().equals(anfitrion.getId())) {
            throw new ForbiddenException("No puede reportar resenas de alojamientos ajenos");
        }
        if (Boolean.TRUE.equals(resena.getReportada())) {
            throw new ConflictException("La resena ya fue reportada");
        }
        resena.setReportada(true);
        resena.setMotivoReporte(request.motivo());
        Resena guardada = resenaRepository.save(resena);
        auditoriaService.guardar(anfitrion, "REPORTAR_RESENA", "resena", guardada.getId(), request.motivo());
        return ResenaResponse.desde(guardada);
    }

    @Transactional(readOnly = true)
    public List<ResenaResponse> listarReportadas() {
        return resenaRepository.findByReportadaTrueOrderByCreadoEnDesc().stream().map(ResenaResponse::desde).toList();
    }

    @Transactional
    public ResenaResponse ocultar(Long id, Usuario administrador) {
        exigirRol(administrador, RolUsuario.ADMINISTRADOR, "Solo un administrador puede moderar resenas");
        return moderar(id, administrador, EstadoResena.OCULTA, "OCULTAR_RESENA");
    }

    @Transactional
    public ResenaResponse restaurar(Long id, Usuario administrador) {
        exigirRol(administrador, RolUsuario.ADMINISTRADOR, "Solo un administrador puede moderar resenas");
        return moderar(id, administrador, EstadoResena.VISIBLE, "RESTAURAR_RESENA");
    }

    private ResenaResponse moderar(Long id, Usuario administrador, EstadoResena estado, String accion) {
        Resena resena = obtener(id);
        if (!Boolean.TRUE.equals(resena.getReportada())) {
            throw new ConflictException("Solo se pueden moderar resenas reportadas");
        }
        resena.setEstado(estado);
        Resena guardada = resenaRepository.save(resena);
        recalcularPromedio(guardada.getAlojamiento());
        auditoriaService.guardar(administrador, accion, "resena", guardada.getId(), estado.name());
        return ResenaResponse.desde(guardada);
    }

    private void recalcularPromedio(Alojamiento alojamiento) {
        List<Resena> visibles = resenaRepository.findByAlojamientoAndEstadoOrderByCreadoEnDesc(
            alojamiento, EstadoResena.VISIBLE);
        BigDecimal promedio = visibles.isEmpty() ? null : BigDecimal.valueOf(visibles.stream()
            .mapToInt(Resena::getCalificacion).sum())
            .divide(BigDecimal.valueOf(visibles.size()), 1, RoundingMode.HALF_UP);
        alojamiento.setCalificacionPromedio(promedio);
        alojamientoRepository.save(alojamiento);
    }

    private Resena obtener(Long id) {
        return resenaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resena no encontrada"));
    }

    private void exigirRol(Usuario usuario, RolUsuario rol, String mensaje) {
        if (usuario.getRol() != rol) throw new ForbiddenException(mensaje);
    }
}
