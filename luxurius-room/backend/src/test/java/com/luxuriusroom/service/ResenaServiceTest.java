package com.luxuriusroom.service;

import com.luxuriusroom.dto.ReporteResenaRequest;
import com.luxuriusroom.dto.ResenaRequest;
import com.luxuriusroom.exception.ForbiddenException;
import com.luxuriusroom.model.*;
import com.luxuriusroom.repository.AlojamientoRepository;
import com.luxuriusroom.repository.ResenaRepository;
import com.luxuriusroom.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {
    @Mock private ResenaRepository resenaRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private AlojamientoRepository alojamientoRepository;
    @Mock private AuditoriaService auditoriaService;
    @InjectMocks private ResenaService resenaService;

    private Usuario huesped;
    private Usuario anfitrion;
    private Usuario administrador;
    private Alojamiento alojamiento;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        huesped = usuario(1L, RolUsuario.HUESPED);
        anfitrion = usuario(2L, RolUsuario.ANFITRION);
        administrador = usuario(3L, RolUsuario.ADMINISTRADOR);
        alojamiento = new Alojamiento();
        alojamiento.setId(4L);
        alojamiento.setNombre("Casa verde");
        alojamiento.setAnfitrion(anfitrion);
        Habitacion habitacion = new Habitacion();
        habitacion.setAlojamiento(alojamiento);
        reserva = new Reserva();
        reserva.setCodigo("LR-ABCDEFG2");
        reserva.setUsuario(huesped);
        reserva.setHabitacion(habitacion);
        reserva.setEstadoReserva(EstadoReserva.COMPLETADA);
    }

    @Test
    void crearResenaRecalculaPromedioVisible() {
        Resena resena = resena(5, EstadoResena.VISIBLE);
        when(reservaRepository.findByCodigoForUpdate(reserva.getCodigo())).thenReturn(Optional.of(reserva));
        when(resenaRepository.findByReserva(reserva)).thenReturn(Optional.empty());
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocation -> {
            Resena guardada = invocation.getArgument(0);
            guardada.setId(10L);
            return guardada;
        });
        when(resenaRepository.findByAlojamientoAndEstadoOrderByCreadoEnDesc(alojamiento, EstadoResena.VISIBLE))
            .thenReturn(List.of(resena));

        var response = resenaService.crear(huesped, new ResenaRequest(reserva.getCodigo(), 5, "Excelente"));

        assertEquals("Casa verde", response.alojamiento());
        assertEquals(new BigDecimal("5.0"), alojamiento.getCalificacionPromedio());
        verify(alojamientoRepository).save(alojamiento);
        verify(auditoriaService).guardar(huesped, "CREAR_RESENA", "resena", 10L, "5");
    }

    @Test
    void anfitrionReportaSoloResenaDeSuAlojamiento() {
        Resena resena = resena(4, EstadoResena.VISIBLE);
        resena.setId(20L);
        when(resenaRepository.findById(20L)).thenReturn(Optional.of(resena));
        when(resenaRepository.save(resena)).thenReturn(resena);

        var response = resenaService.reportar(20L, anfitrion, new ReporteResenaRequest("Contenido inapropiado"));

        assertEquals(true, response.reportada());
        assertEquals("Contenido inapropiado", response.motivoReporte());
        verify(auditoriaService).guardar(anfitrion, "REPORTAR_RESENA", "resena", 20L, "Contenido inapropiado");
    }

    @Test
    void otroAnfitrionNoPuedeReportarLaResena() {
        Resena resena = resena(4, EstadoResena.VISIBLE);
        resena.setId(21L);
        Usuario otro = usuario(99L, RolUsuario.ANFITRION);
        when(resenaRepository.findById(21L)).thenReturn(Optional.of(resena));

        assertThrows(ForbiddenException.class,
            () -> resenaService.reportar(21L, otro, new ReporteResenaRequest("Motivo")));
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    void ocultarYRestaurarResenaReportadaRecalculanElPromedio() {
        Resena resena = resena(3, EstadoResena.VISIBLE);
        resena.setId(22L);
        resena.setReportada(true);
        when(resenaRepository.findById(22L)).thenReturn(Optional.of(resena));
        when(resenaRepository.save(resena)).thenReturn(resena);
        when(resenaRepository.findByAlojamientoAndEstadoOrderByCreadoEnDesc(alojamiento, EstadoResena.VISIBLE))
            .thenReturn(List.of());

        resenaService.ocultar(22L, administrador);

        assertEquals(EstadoResena.OCULTA, resena.getEstado());
        assertEquals(null, alojamiento.getCalificacionPromedio());

        when(resenaRepository.findByAlojamientoAndEstadoOrderByCreadoEnDesc(alojamiento, EstadoResena.VISIBLE))
            .thenReturn(List.of(resena));
        resenaService.restaurar(22L, administrador);

        assertEquals(EstadoResena.VISIBLE, resena.getEstado());
        assertEquals(new BigDecimal("3.0"), alojamiento.getCalificacionPromedio());
        verify(alojamientoRepository, org.mockito.Mockito.times(2)).save(alojamiento);
    }

    private Resena resena(int calificacion, EstadoResena estado) {
        Resena resena = new Resena();
        resena.setId(30L);
        resena.setReserva(reserva);
        resena.setUsuario(huesped);
        resena.setAlojamiento(alojamiento);
        resena.setCalificacion(calificacion);
        resena.setEstado(estado);
        resena.setReportada(false);
        return resena;
    }

    private Usuario usuario(Long id, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Usuario");
        usuario.setRol(rol);
        return usuario;
    }
}