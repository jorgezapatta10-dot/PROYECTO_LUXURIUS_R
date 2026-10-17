package com.luxuriusroom.service;

import com.luxuriusroom.dto.ReservaRequest;
import com.luxuriusroom.exception.BadRequestException;
import com.luxuriusroom.exception.ConflictException;
import com.luxuriusroom.exception.ForbiddenException;
import com.luxuriusroom.model.*;
import com.luxuriusroom.repository.BloqueoDisponibilidadRepository;
import com.luxuriusroom.repository.HabitacionRepository;
import com.luxuriusroom.repository.ReservaNocheRepository;
import com.luxuriusroom.repository.ReservaRepository;
import com.luxuriusroom.repository.ResenaRepository;
import com.luxuriusroom.repository.TarifaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {
    @Mock private ReservaRepository reservaRepository;
    @Mock private ResenaRepository resenaRepository;
    @Mock private ReservaNocheRepository reservaNocheRepository;
    @Mock private HabitacionRepository habitacionRepository;
    @Mock private TarifaRepository tarifaRepository;
    @Mock private BloqueoDisponibilidadRepository bloqueoDisponibilidadRepository;
    @Mock private AuditoriaService auditoriaService;
    @InjectMocks private ReservaService reservaService;

    private Usuario huesped;
    private Usuario anfitrion;
    private Habitacion habitacion;
    private Alojamiento alojamiento;

    @BeforeEach
    void preparar() {
        huesped = new Usuario();
        huesped.setId(4L);
        huesped.setRol(RolUsuario.HUESPED);
        anfitrion = new Usuario();
        anfitrion.setId(9L);
        anfitrion.setRol(RolUsuario.ANFITRION);
        alojamiento = new Alojamiento();
        alojamiento.setId(3L);
        alojamiento.setNombre("Casa de prueba");
        alojamiento.setCiudad("Bogota");
        alojamiento.setEstado(EstadoAlojamiento.ACTIVO);
        alojamiento.setPoliticaCancelacion(PoliticaCancelacion.MODERADA);
        alojamiento.setAnfitrion(anfitrion);
        habitacion = new Habitacion();
        habitacion.setId(12L);
        habitacion.setAlojamiento(alojamiento);
        habitacion.setNombre("Doble");
        habitacion.setActiva(true);
        habitacion.setCapacidadMaxima(2);
        habitacion.setUnidadesTotales(2);
        habitacion.setPrecioBaseNoche(new BigDecimal("100.00"));
        habitacion.setCargoLimpieza(new BigDecimal("25.00"));
    }

    @Test
    void crearCalculaNochesTarifasCargoYGuardaPrecioPorNoche() {
        LocalDate entrada = LocalDate.now().plusDays(20);
        LocalDate salida = entrada.plusDays(2);
        ReservaRequest request = request(entrada, salida, 2, 1, true);
        Tarifa tarifa = new Tarifa();
        tarifa.setFechaInicio(entrada);
        tarifa.setFechaFin(entrada);
        tarifa.setPrecioNoche(new BigDecimal("150.00"));
        when(habitacionRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(habitacion));
        when(tarifaRepository.findByHabitacion(habitacion)).thenReturn(List.of(tarifa));
        when(reservaRepository.findByHabitacion(habitacion)).thenReturn(List.of());
        when(bloqueoDisponibilidadRepository.findByHabitacion(habitacion)).thenReturn(List.of());
        when(reservaRepository.findByCodigo(any())).thenReturn(Optional.empty());
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reserva = invocation.getArgument(0);
            reserva.setId(44L);
            return reserva;
        });

        var result = reservaService.crear(huesped, request);

        assertEquals(2, result.noches());
        assertEquals(new BigDecimal("250.00"), result.subtotal());
        assertEquals(new BigDecimal("25.00"), result.cargosAdicionales());
        assertEquals(new BigDecimal("275.00"), result.total());
        assertEquals(EstadoReserva.PENDIENTE_CONFIRMACION, result.estadoReserva());
        assertEquals(EstadoPago.PENDIENTE_EN_SITIO, result.estadoPago());
        assertEquals(11, result.codigo().length());
        ArgumentCaptor<List<ReservaNoche>> captor = ArgumentCaptor.forClass(List.class);
        verify(reservaNocheRepository).saveAll(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getFirst().getPrecioAplicado());
        assertEquals(new BigDecimal("100.00"), captor.getValue().getLast().getPrecioAplicado());
    }

    @Test
    void cotizacionMuestraTotalVigenteSinPersistirReserva() {
        LocalDate entrada = LocalDate.now().plusDays(18);
        when(habitacionRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(habitacion));
        when(tarifaRepository.findByHabitacion(habitacion)).thenReturn(List.of());
        when(reservaRepository.findByHabitacion(habitacion)).thenReturn(List.of());
        when(bloqueoDisponibilidadRepository.findByHabitacion(habitacion)).thenReturn(List.of());

        var result = reservaService.cotizar(huesped, request(entrada, entrada.plusDays(2), 2, 1, false));

        assertEquals(2, result.noches());
        assertEquals(new BigDecimal("200.00"), result.subtotal());
        assertEquals(new BigDecimal("25.00"), result.cargosAdicionales());
        assertEquals(new BigDecimal("225.00"), result.total());
        assertEquals(2, result.detalleNoches().size());
        verify(reservaRepository, never()).save(any(Reserva.class));
        verify(reservaNocheRepository, never()).saveAll(any());
    }

    @Test
    void salidaYEntradaEnLaMismaFechaNoSeConsideranSolapadas() {
        LocalDate entrada = LocalDate.now().plusDays(20);
        LocalDate salida = entrada.plusDays(1);
        Reserva existente = reserva(entrada.minusDays(1), entrada, 1, EstadoReserva.CONFIRMADA);
        prepararCreacion(List.of(existente), List.of());

        var result = reservaService.crear(huesped, request(entrada, salida, 1, 1, true));

        assertEquals(1, result.noches());
    }

    @Test
    void unidadesBloqueadasSeRestanDeDisponibilidad() {
        LocalDate entrada = LocalDate.now().plusDays(20);
        BloqueoDisponibilidad bloqueo = new BloqueoDisponibilidad();
        bloqueo.setFechaInicio(entrada);
        bloqueo.setFechaFin(entrada);
        bloqueo.setUnidadesBloqueadas(1);
        when(habitacionRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(habitacion));
        when(reservaRepository.findByHabitacion(habitacion)).thenReturn(List.of());
        when(bloqueoDisponibilidadRepository.findByHabitacion(habitacion)).thenReturn(List.of(bloqueo));

        assertThrows(ConflictException.class,
            () -> reservaService.crear(huesped, request(entrada, entrada.plusDays(1), 2, 2, true)));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void rechazaEntradaEnPasadoCapacidadInsuficienteYUnidadesMayoresAAdultos() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        assertThrows(BadRequestException.class,
            () -> reservaService.crear(huesped, request(ayer, ayer.plusDays(1), 1, 1, true)));

        LocalDate futura = LocalDate.now().plusDays(10);
        when(habitacionRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(habitacion));
        habitacion.setCapacidadMaxima(2);
        assertThrows(BadRequestException.class,
            () -> reservaService.crear(huesped, request(futura, futura.plusDays(1), 2, 1, 1, true)));
        assertThrows(BadRequestException.class,
            () -> reservaService.crear(huesped, request(futura, futura.plusDays(1), 1, 2, true)));
        assertThrows(BadRequestException.class,
            () -> reservaService.crear(huesped, request(futura, futura.plusDays(1), 17, 1, true)));
        assertThrows(BadRequestException.class,
            () -> reservaService.crear(huesped, request(futura, futura.plusDays(1), 1, 1, false)));
    }

    @Test
    void soloHuespedPuedeCrearReserva() {
        LocalDate entrada = LocalDate.now().plusDays(10);
        assertThrows(ForbiddenException.class,
            () -> reservaService.crear(anfitrion, request(entrada, entrada.plusDays(1), 1, 1, true)));
        verify(habitacionRepository, never()).findByIdForUpdate(12L);
    }

    @Test
    void huespedPuedeCancelarAntesDeEntradaYSeMarcaFueraDelPlazoGratuito() {
        LocalDate entrada = LocalDate.now().plusDays(2);
        Reserva reserva = reserva(entrada, entrada.plusDays(2), 1, EstadoReserva.CONFIRMADA);
        reserva.setUsuario(huesped);
        when(reservaRepository.findByCodigo("LR-ABCDEFG2")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(reserva)).thenReturn(reserva);

        assertEquals(true, reservaService.cancelar("LR-ABCDEFG2", huesped).cancelacionTardia());
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstadoReserva());
    }

    @Test
    void rechazaTransicionCancelarUnaReservaCompletada() {
        LocalDate entrada = LocalDate.now().minusDays(3);
        Reserva reserva = reserva(entrada, entrada.plusDays(2), 1, EstadoReserva.COMPLETADA);
        reserva.setUsuario(huesped);
        when(reservaRepository.findByCodigo("LR-ABCDEFG2")).thenReturn(Optional.of(reserva));

        assertThrows(ConflictException.class, () -> reservaService.cancelar("LR-ABCDEFG2", huesped));
    }

    @Test
    void anfitrionConfirmaReservaPropiaYSeAudita() {
        LocalDate entrada = LocalDate.now().plusDays(4);
        Reserva reserva = reserva(entrada, entrada.plusDays(2), 1, EstadoReserva.PENDIENTE_CONFIRMACION);
        when(reservaRepository.findByCodigo("LR-ABCDEFG2")).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(reserva)).thenReturn(reserva);

        var result = reservaService.confirmar("LR-ABCDEFG2", anfitrion);

        assertEquals(EstadoReserva.CONFIRMADA, result.estadoReserva());
        verify(auditoriaService).guardar(anfitrion, "CONFIRMAR_RESERVA", "reserva", null, "LR-ABCDEFG2");
    }

    @Test
    void anfitrionNoPuedeGestionarReservasAjenasNiCompletarAntesDeLaEntrada() {
        LocalDate entrada = LocalDate.now().plusDays(4);
        Reserva ajena = reserva(entrada, entrada.plusDays(1), 1, EstadoReserva.PENDIENTE_CONFIRMACION);
        Usuario otroAnfitrion = new Usuario();
        otroAnfitrion.setId(77L);
        alojamiento.setAnfitrion(otroAnfitrion);
        when(reservaRepository.findByCodigo("LR-ABCDEFG2")).thenReturn(Optional.of(ajena));

        assertThrows(ForbiddenException.class, () -> reservaService.confirmar("LR-ABCDEFG2", anfitrion));
        verify(reservaRepository, never()).save(any(Reserva.class));

        alojamiento.setAnfitrion(anfitrion);
        Reserva futuraConfirmada = reserva(entrada, entrada.plusDays(1), 1, EstadoReserva.CONFIRMADA);
        when(reservaRepository.findByCodigo("LR-ABCDEFG3")).thenReturn(Optional.of(futuraConfirmada));
        assertThrows(ConflictException.class, () -> reservaService.completar("LR-ABCDEFG3", anfitrion));
    }

    @Test
    void pagoSoloSeRegistraParaReservaConfirmadaOCompletada() {
        LocalDate entrada = LocalDate.now().plusDays(2);
        Reserva reserva = reserva(entrada, entrada.plusDays(1), 1, EstadoReserva.PENDIENTE_CONFIRMACION);
        when(reservaRepository.findByCodigo("LR-ABCDEFG2")).thenReturn(Optional.of(reserva));
        assertThrows(ConflictException.class, () -> reservaService.registrarPagoEnSitio("LR-ABCDEFG2", anfitrion));

        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        var result = reservaService.registrarPagoEnSitio("LR-ABCDEFG2", anfitrion);
        assertEquals(EstadoPago.PAGADO_EN_SITIO, result.estadoPago());
    }

    @Test
    void listaDelHuespedIndicaSiLaReservaYaTieneResena() {
        Reserva completada = reserva(LocalDate.now().minusDays(4), LocalDate.now().minusDays(2), 1,
            EstadoReserva.COMPLETADA);
        when(reservaRepository.findByUsuario(huesped)).thenReturn(List.of(completada));
        when(resenaRepository.existsByReserva(completada)).thenReturn(true);

        var result = reservaService.listarUsuario(huesped);

        assertEquals(true, result.getFirst().resenaCreada());
    }

    private void prepararCreacion(List<Reserva> reservas, List<BloqueoDisponibilidad> bloqueos) {
        when(habitacionRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(habitacion));
        when(tarifaRepository.findByHabitacion(habitacion)).thenReturn(List.of());
        when(reservaRepository.findByHabitacion(habitacion)).thenReturn(reservas);
        when(bloqueoDisponibilidadRepository.findByHabitacion(habitacion)).thenReturn(bloqueos);
        when(reservaRepository.findByCodigo(any())).thenReturn(Optional.empty());
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reserva = invocation.getArgument(0);
            reserva.setId(55L);
            return reserva;
        });
    }

    private ReservaRequest request(LocalDate entrada, LocalDate salida, int adultos, int unidades, boolean acepto) {
        return request(entrada, salida, adultos, 0, unidades, acepto);
    }

    private ReservaRequest request(LocalDate entrada, LocalDate salida, int adultos, int ninos, int unidades, boolean acepto) {
        return new ReservaRequest(12L, entrada, salida, adultos, ninos, unidades, acepto);
    }

    private Reserva reserva(LocalDate entrada, LocalDate salida, int unidades, EstadoReserva estado) {
        Reserva reserva = new Reserva();
        reserva.setUsuario(huesped);
        reserva.setHabitacion(habitacion);
        reserva.setFechaEntrada(entrada);
        reserva.setFechaSalida(salida);
        reserva.setUnidades(unidades);
        reserva.setEstadoReserva(estado);
        reserva.setCodigo("LR-ABCDEFG2");
        return reserva;
    }
}