package com.luxuriusroom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxuriusroom.model.Alojamiento;
import com.luxuriusroom.model.EstadoAlojamiento;
import com.luxuriusroom.model.ImagenAlojamiento;
import com.luxuriusroom.model.Habitacion;
import com.luxuriusroom.model.Resena;
import com.luxuriusroom.model.Reserva;
import com.luxuriusroom.model.EstadoReserva;
import com.luxuriusroom.model.BloqueoDisponibilidad;
import com.luxuriusroom.model.Tarifa;
import com.luxuriusroom.model.TipoAlojamiento;
import com.luxuriusroom.model.Usuario;
import com.luxuriusroom.dto.BloqueoRequest;
import com.luxuriusroom.dto.TarifaRequest;
import com.luxuriusroom.exception.ConflictException;
import com.luxuriusroom.exception.ForbiddenException;
import com.luxuriusroom.exception.ResourceNotFoundException;
import com.luxuriusroom.repository.AlojamientoRepository;
import com.luxuriusroom.repository.AlojamientoServicioRepository;
import com.luxuriusroom.repository.BloqueoDisponibilidadRepository;
import com.luxuriusroom.repository.HabitacionRepository;
import com.luxuriusroom.repository.ImagenAlojamientoRepository;
import com.luxuriusroom.repository.ResenaRepository;
import com.luxuriusroom.repository.ReservaRepository;
import com.luxuriusroom.repository.ServicioRepository;
import com.luxuriusroom.repository.TarifaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AlojamientoServiceTest {
    @Mock private AlojamientoRepository alojamientoRepository;
    @Mock private HabitacionRepository habitacionRepository;
    @Mock private TarifaRepository tarifaRepository;
    @Mock private BloqueoDisponibilidadRepository bloqueoDisponibilidadRepository;
    @Mock private ImagenAlojamientoRepository imagenAlojamientoRepository;
    @Mock private AlojamientoServicioRepository alojamientoServicioRepository;
    @Mock private ServicioRepository servicioRepository;
    @Mock private ResenaRepository resenaRepository;
    @Mock private ReservaRepository reservaRepository;

    @InjectMocks private AlojamientoService alojamientoService;

    @Test
    void listarAlojamientosFiltraAntesDePaginarYDevuelveImagenPrincipal() {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(17L);
        alojamiento.setNombre("Casa del Rio");
        alojamiento.setCiudad("Bogota");
        alojamiento.setPais("Colombia");
        alojamiento.setTipo(TipoAlojamiento.CASA);

        ImagenAlojamiento imagen = new ImagenAlojamiento();
        imagen.setUrl("https://example.test/casa.jpg");
        imagen.setEsPrincipal(true);

        when(alojamientoRepository.buscarPublicos(
            eq(EstadoAlojamiento.ACTIVO), eq("Bogota"), eq(null), eq(null), eq(null), eq(null),
            eq(null), eq(0), eq(0), eq(0), eq(0), eq(PageRequest.of(1, 2))))
            .thenReturn(new PageImpl<>(List.of(alojamiento), PageRequest.of(1, 2), 3));
        when(imagenAlojamientoRepository.findByAlojamientoOrderByOrdenAsc(alojamiento))
            .thenReturn(List.of(imagen));

        var result = alojamientoService.listarAlojamientos(" Bogota ", 1, 2);

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertEquals("https://example.test/casa.jpg", result.getContent().getFirst().imagenPrincipal());
        verify(alojamientoRepository, never()).findByEstado(eq(EstadoAlojamiento.ACTIVO), eq(PageRequest.of(1, 2)));
    }

    @Test
    void detallePublicoNoExponeAlojamientosNoActivos() {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(18L);
        alojamiento.setEstado(EstadoAlojamiento.SUSPENDIDO);
        when(alojamientoRepository.findById(18L)).thenReturn(Optional.of(alojamiento));

        assertThrows(ResourceNotFoundException.class, () -> alojamientoService.detalleCompleto(18L));
        verify(habitacionRepository, never()).findByAlojamiento(alojamiento);
    }

    @Test
    void detalleAdmiteHabitacionSinCamasYResenaSinComentario() {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(19L);
        alojamiento.setEstado(EstadoAlojamiento.ACTIVO);
        Habitacion habitacion = new Habitacion();
        habitacion.setId(7L);
        habitacion.setNombre("Habitacion sin camas declaradas");
        habitacion.setCapacidadMaxima(2);
        habitacion.setCamas(null);
        habitacion.setUnidadesTotales(1);
        habitacion.setPrecioBaseNoche(new java.math.BigDecimal("100.00"));
        habitacion.setCargoLimpieza(null);
        habitacion.setActiva(true);
        Resena resena = new Resena();
        resena.setId(2L);
        resena.setCalificacion(4);
        resena.setComentario(null);
        com.luxuriusroom.model.Usuario usuario = new com.luxuriusroom.model.Usuario();
        usuario.setNombre("Eva");
        resena.setUsuario(usuario);
        when(alojamientoRepository.findById(19L)).thenReturn(Optional.of(alojamiento));
        when(habitacionRepository.findByAlojamiento(alojamiento)).thenReturn(List.of(habitacion));
        when(alojamientoServicioRepository.findByAlojamiento(alojamiento)).thenReturn(List.of());
        when(imagenAlojamientoRepository.findByAlojamientoOrderByOrdenAsc(alojamiento)).thenReturn(List.of());
        when(resenaRepository.findByAlojamientoAndEstadoOrderByCreadoEnDesc(alojamiento,
            com.luxuriusroom.model.EstadoResena.VISIBLE)).thenReturn(List.of(resena));

        var result = alojamientoService.detalleCompleto(19L);

        var habitaciones = (List<java.util.Map<String, Object>>) result.get("habitaciones");
        var resenas = (List<java.util.Map<String, Object>>) result.get("resenas");
        assertEquals(null, habitaciones.getFirst().get("camas"));
        assertEquals(null, habitaciones.getFirst().get("cargoLimpieza"));
        assertEquals(null, resenas.getFirst().get("comentario"));
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> new ObjectMapper().writeValueAsString(result));
    }

    @Test
    void habitacionesPublicasSeMapeanADtoSinEntidadJPA() {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(21L);
        alojamiento.setEstado(EstadoAlojamiento.ACTIVO);
        Habitacion habitacion = new Habitacion();
        habitacion.setId(9L);
        habitacion.setNombre("Habitacion");
        habitacion.setCamas(null);
        habitacion.setActiva(true);
        when(alojamientoRepository.findById(21L)).thenReturn(Optional.of(alojamiento));
        when(habitacionRepository.findByAlojamiento(alojamiento)).thenReturn(List.of(habitacion));

        var result = alojamientoService.habitacionesAlojamiento(21L);

        assertEquals(1, result.size());
        assertEquals(9L, result.getFirst().id());
        assertEquals(null, result.getFirst().camas());
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> new ObjectMapper().writeValueAsString(result));
    }

    @Test
    void resenasPublicasNoRevelanElCodigoDeReserva() throws Exception {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(25L);
        alojamiento.setEstado(EstadoAlojamiento.ACTIVO);
        Usuario usuario = new Usuario();
        usuario.setNombre("Eva");
        Resena resena = new Resena();
        resena.setId(26L);
        resena.setUsuario(usuario);
        resena.setCalificacion(5);
        resena.setComentario("Muy buena estancia");
        Reserva reserva = new Reserva();
        reserva.setCodigo("LR-SECRETO1");
        resena.setReserva(reserva);
        when(alojamientoRepository.findById(25L)).thenReturn(Optional.of(alojamiento));
        when(resenaRepository.findByAlojamientoAndEstadoOrderByCreadoEnDesc(alojamiento,
            com.luxuriusroom.model.EstadoResena.VISIBLE)).thenReturn(List.of(resena));

        var response = alojamientoService.resenasPublicas(25L);
        String json = new ObjectMapper().writeValueAsString(response);

        assertEquals(1, response.size());
        assertFalse(json.contains("LR-SECRETO1"));
        assertFalse(json.contains("codigoReserva"));
    }

    @Test
    void disponibilidadIncluyeReservasActivasYBloqueosConSusUnidades() {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(20L);
        alojamiento.setEstado(EstadoAlojamiento.ACTIVO);
        Habitacion habitacion = new Habitacion();
        habitacion.setId(8L);
        habitacion.setNombre("Doble");
        habitacion.setActiva(true);
        habitacion.setUnidadesTotales(3);
        LocalDate inicio = LocalDate.now().plusDays(5);
        Reserva reserva = new Reserva();
        reserva.setFechaEntrada(inicio);
        reserva.setFechaSalida(inicio.plusDays(2));
        reserva.setUnidades(1);
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        BloqueoDisponibilidad bloqueo = new BloqueoDisponibilidad();
        bloqueo.setFechaInicio(inicio.plusDays(3));
        bloqueo.setFechaFin(inicio.plusDays(4));
        bloqueo.setUnidadesBloqueadas(2);
        when(alojamientoRepository.findById(20L)).thenReturn(Optional.of(alojamiento));
        when(habitacionRepository.findByAlojamiento(alojamiento)).thenReturn(List.of(habitacion));
        when(reservaRepository.findByHabitacion(habitacion)).thenReturn(List.of(reserva));
        when(bloqueoDisponibilidadRepository.findByHabitacion(habitacion)).thenReturn(List.of(bloqueo));

        var result = alojamientoService.disponibilidadAlojamiento(20L);

        assertEquals(2, result.habitaciones().getFirst().periodos().size());
        assertEquals(inicio.plusDays(1), result.habitaciones().getFirst().periodos().getFirst().fechaFin());
        assertEquals("RESERVADA", result.habitaciones().getFirst().periodos().getFirst().tipo());
        assertEquals("BLOQUEADA", result.habitaciones().getFirst().periodos().getLast().tipo());
        assertEquals(2, result.habitaciones().getFirst().periodos().getLast().unidades());
        assertEquals(3, result.habitaciones().getFirst().unidadesTotales());
    }

    @Test
    void creaTarifaConRangoIndicadoYRechazaSolapamientoInclusivo() {
        Usuario anfitrion = new Usuario();
        anfitrion.setId(5L);
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(30L);
        alojamiento.setAnfitrion(anfitrion);
        Habitacion habitacion = new Habitacion();
        habitacion.setId(31L);
        habitacion.setAlojamiento(alojamiento);
        LocalDate inicio = LocalDate.now().plusDays(10);
        TarifaRequest request = new TarifaRequest(inicio, inicio.plusDays(2),
            new java.math.BigDecimal("150.00"), "Temporada alta");
        when(habitacionRepository.findByIdForUpdate(31L)).thenReturn(Optional.of(habitacion));
        when(tarifaRepository.findByHabitacion(habitacion)).thenReturn(List.of());
        when(tarifaRepository.save(any(Tarifa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = alojamientoService.crearTarifa(31L, anfitrion, request);

        assertEquals(inicio, created.fechaInicio());
        assertEquals(inicio.plusDays(2), created.fechaFin());
        assertEquals(new java.math.BigDecimal("150.00"), created.precioNoche());

        Tarifa existente = new Tarifa();
        existente.setFechaInicio(inicio.minusDays(3));
        existente.setFechaFin(inicio);
        when(tarifaRepository.findByHabitacion(habitacion)).thenReturn(List.of(existente));
        assertThrows(ConflictException.class, () -> alojamientoService.crearTarifa(31L, anfitrion, request));
        verify(tarifaRepository, times(1)).save(any(Tarifa.class));
    }

    @Test
    void bloqueosRespetanReservasYSoloElPropietarioPuedeCrearlos() {
        Usuario anfitrion = new Usuario();
        anfitrion.setId(8L);
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(40L);
        alojamiento.setAnfitrion(anfitrion);
        Habitacion habitacion = new Habitacion();
        habitacion.setId(41L);
        habitacion.setAlojamiento(alojamiento);
        habitacion.setUnidadesTotales(2);
        LocalDate fecha = LocalDate.now().plusDays(12);
        Reserva reserva = new Reserva();
        reserva.setFechaEntrada(fecha);
        reserva.setFechaSalida(fecha.plusDays(1));
        reserva.setUnidades(1);
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        when(habitacionRepository.findByIdForUpdate(41L)).thenReturn(Optional.of(habitacion));
        when(reservaRepository.findByHabitacion(habitacion)).thenReturn(List.of(reserva));
        when(bloqueoDisponibilidadRepository.findByHabitacion(habitacion)).thenReturn(List.of());

        BloqueoRequest bloqueoQueExcedeInventario = new BloqueoRequest(fecha, fecha, 2, "Mantenimiento");
        assertThrows(ConflictException.class,
            () -> alojamientoService.crearBloqueo(41L, anfitrion, bloqueoQueExcedeInventario));
        verify(bloqueoDisponibilidadRepository, never()).save(any(BloqueoDisponibilidad.class));

        Usuario otroAnfitrion = new Usuario();
        otroAnfitrion.setId(9L);
        BloqueoRequest bloqueoPermitido = new BloqueoRequest(fecha, fecha, 1, "Mantenimiento");
        assertThrows(ForbiddenException.class,
            () -> alojamientoService.crearBloqueo(41L, otroAnfitrion, bloqueoPermitido));
    }
}