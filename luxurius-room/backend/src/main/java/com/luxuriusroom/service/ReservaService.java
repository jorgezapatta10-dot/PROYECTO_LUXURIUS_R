package com.luxuriusroom.service;

import com.luxuriusroom.dto.ReservaRequest;
import com.luxuriusroom.dto.ReservaResponse;
import com.luxuriusroom.dto.ReservaCotizacionResponse;
import com.luxuriusroom.exception.BadRequestException;
import com.luxuriusroom.exception.ConflictException;
import com.luxuriusroom.exception.ForbiddenException;
import com.luxuriusroom.exception.ResourceNotFoundException;
import com.luxuriusroom.model.*;
import com.luxuriusroom.repository.BloqueoDisponibilidadRepository;
import com.luxuriusroom.repository.HabitacionRepository;
import com.luxuriusroom.repository.ReservaNocheRepository;
import com.luxuriusroom.repository.ReservaRepository;
import com.luxuriusroom.repository.ResenaRepository;
import com.luxuriusroom.repository.TarifaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservaService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODIGO_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final ReservaRepository reservaRepository;
    private final ResenaRepository resenaRepository;
    private final ReservaNocheRepository reservaNocheRepository;
    private final HabitacionRepository habitacionRepository;
    private final TarifaRepository tarifaRepository;
    private final BloqueoDisponibilidadRepository bloqueoDisponibilidadRepository;
    private final AuditoriaService auditoriaService;

    public ReservaService(ReservaRepository reservaRepository, ResenaRepository resenaRepository,
                          ReservaNocheRepository reservaNocheRepository,
                          HabitacionRepository habitacionRepository, TarifaRepository tarifaRepository,
                          BloqueoDisponibilidadRepository bloqueoDisponibilidadRepository,
                          AuditoriaService auditoriaService) {
        this.reservaRepository = reservaRepository;
        this.resenaRepository = resenaRepository;
        this.reservaNocheRepository = reservaNocheRepository;
        this.habitacionRepository = habitacionRepository;
        this.tarifaRepository = tarifaRepository;
        this.bloqueoDisponibilidadRepository = bloqueoDisponibilidadRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ReservaResponse crear(Usuario usuario, ReservaRequest request) {
        if (usuario.getRol() != RolUsuario.HUESPED) {
            throw new ForbiddenException("Solo un huesped puede crear reservas");
        }
        validarSolicitud(request, true);

        Habitacion habitacion = habitacionRepository.findByIdForUpdate(request.habitacionId())
            .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));
        validarHabitacionReservable(habitacion, request);
        CalculoReserva calculo = calcularPrecio(habitacion, request);
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setHabitacion(habitacion);
        reserva.setFechaEntrada(request.fechaEntrada());
        reserva.setFechaSalida(request.fechaSalida());
        reserva.setAdultos(request.adultos());
        reserva.setNinos(request.ninos());
        reserva.setUnidades(request.unidades());
        reserva.setNoches(calculo.noches());
        reserva.setSubtotal(calculo.subtotal());
        reserva.setCargosAdicionales(calculo.cargosAdicionales());
        reserva.setTotal(calculo.total());
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE_CONFIRMACION);
        reserva.setEstadoPago(EstadoPago.PENDIENTE_EN_SITIO);
        reserva.setAceptoCondiciones(true);
        reserva.setCancelacionTardia(false);
        reserva.setCreadoEn(LocalDateTime.now());
        reserva.setActualizadoEn(LocalDateTime.now());
        reserva.setCodigo(generarCodigoReserva());

        Reserva guardada = reservaRepository.save(reserva);
        for (ReservaNoche reservaNoche : calculo.nochesReservadas()) {
            reservaNoche.setReserva(guardada);
        }
        reservaNocheRepository.saveAll(calculo.nochesReservadas());
        auditoriaService.guardar(usuario, "CREAR_RESERVA", "reserva", guardada.getId(),
            "Reserva creada para habitacion " + habitacion.getId());
        return ReservaResponse.desde(guardada);
    }

    @Transactional
    public ReservaCotizacionResponse cotizar(Usuario usuario, ReservaRequest request) {
        if (usuario.getRol() != RolUsuario.HUESPED) {
            throw new ForbiddenException("Solo un huesped puede cotizar una reserva");
        }
        validarSolicitud(request, false);
        Habitacion habitacion = habitacionRepository.findByIdForUpdate(request.habitacionId())
            .orElseThrow(() -> new ResourceNotFoundException("Habitacion no encontrada"));
        validarHabitacionReservable(habitacion, request);
        CalculoReserva calculo = calcularPrecio(habitacion, request);
        List<ReservaCotizacionResponse.NocheCotizada> detalle = calculo.nochesReservadas().stream()
            .map(noche -> new ReservaCotizacionResponse.NocheCotizada(noche.getFecha(), noche.getPrecioAplicado(),
                noche.getPrecioAplicado().multiply(BigDecimal.valueOf(request.unidades())).setScale(2, RoundingMode.HALF_UP)))
            .toList();
        return new ReservaCotizacionResponse(calculo.noches(), detalle, calculo.subtotal(),
            calculo.cargosAdicionales(), calculo.total());
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(String codigo, Usuario usuario) {
        Reserva reserva = buscar(codigo);
        verificarAcceso(reserva, usuario);
        return ReservaResponse.desde(reserva);
    }

    @Transactional
    public ReservaResponse cancelar(String codigo, Usuario usuario) {
        Reserva reserva = buscar(codigo);
        boolean esHuesped = reserva.getUsuario().getId().equals(usuario.getId())
            && usuario.getRol() == RolUsuario.HUESPED;
        boolean esAnfitrion = reserva.getHabitacion().getAlojamiento().getAnfitrion().getId().equals(usuario.getId())
            && usuario.getRol() == RolUsuario.ANFITRION;
        if (!esHuesped && !esAnfitrion) {
            throw new ForbiddenException("No puede cancelar esta reserva");
        }
        if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE_CONFIRMACION
                && reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw new ConflictException("La reserva no se puede cancelar desde su estado actual");
        }
        if (esHuesped && !LocalDate.now().isBefore(reserva.getFechaEntrada())) {
            throw new ConflictException("Solo puede cancelar antes de la fecha de entrada");
        }

        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), reserva.getFechaEntrada());
        long plazoGratuito = switch (reserva.getHabitacion().getAlojamiento().getPoliticaCancelacion()) {
            case FLEXIBLE -> 1;
            case MODERADA -> 5;
            case ESTRICTA -> 14;
        };
        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        reserva.setCancelacionTardia(diasRestantes < plazoGratuito);
        reserva.setActualizadoEn(LocalDateTime.now());
        Reserva guardada = reservaRepository.save(reserva);
        auditoriaService.guardar(usuario, "CANCELAR_RESERVA", "reserva", guardada.getId(),
            "Reserva cancelada; tardia=" + guardada.getCancelacionTardia());
        return ReservaResponse.desde(guardada);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarUsuario(Usuario usuario) {
        return reservaRepository.findByUsuario(usuario).stream()
            .map(reserva -> ReservaResponse.desde(reserva, resenaRepository.existsByReserva(reserva))).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarAnfitrion(Usuario anfitrion) {
        validarRolAnfitrion(anfitrion);
        return reservaRepository.findByAnfitrionId(anfitrion.getId()).stream().map(ReservaResponse::desde).toList();
    }

    @Transactional
    public ReservaResponse confirmar(String codigo, Usuario anfitrion) {
        Reserva reserva = buscarReservaPropia(codigo, anfitrion);
        exigirEstado(reserva, EstadoReserva.PENDIENTE_CONFIRMACION);
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        return guardarCambioAnfitrion(reserva, anfitrion, "CONFIRMAR_RESERVA");
    }

    @Transactional
    public ReservaResponse completar(String codigo, Usuario anfitrion) {
        return cambiarEstadoFinalAnfitrion(codigo, anfitrion, EstadoReserva.COMPLETADA);
    }

    @Transactional
    public ReservaResponse marcarNoPresentado(String codigo, Usuario anfitrion) {
        return cambiarEstadoFinalAnfitrion(codigo, anfitrion, EstadoReserva.NO_PRESENTADO);
    }

    @Transactional
    public ReservaResponse registrarPagoEnSitio(String codigo, Usuario anfitrion) {
        Reserva reserva = buscarReservaPropia(codigo, anfitrion);
        if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA
                && reserva.getEstadoReserva() != EstadoReserva.COMPLETADA) {
            throw new ConflictException("Solo se puede registrar el pago de una reserva confirmada o completada");
        }
        if (reserva.getEstadoPago() != EstadoPago.PENDIENTE_EN_SITIO) {
            throw new ConflictException("La reserva ya tiene el pago registrado");
        }
        reserva.setEstadoPago(EstadoPago.PAGADO_EN_SITIO);
        return guardarCambioAnfitrion(reserva, anfitrion, "REGISTRAR_PAGO_EN_SITIO");
    }

    private ReservaResponse cambiarEstadoFinalAnfitrion(String codigo, Usuario anfitrion, EstadoReserva estado) {
        Reserva reserva = buscarReservaPropia(codigo, anfitrion);
        exigirEstado(reserva, EstadoReserva.CONFIRMADA);
        if (LocalDate.now().isBefore(reserva.getFechaEntrada())) {
            throw new ConflictException("La reserva solo puede completarse desde el dia de entrada en adelante");
        }
        reserva.setEstadoReserva(estado);
        return guardarCambioAnfitrion(reserva, anfitrion,
            estado == EstadoReserva.COMPLETADA ? "COMPLETAR_RESERVA" : "MARCAR_NO_PRESENTADO");
    }

    private Reserva buscarReservaPropia(String codigo, Usuario anfitrion) {
        validarRolAnfitrion(anfitrion);
        Reserva reserva = buscar(codigo);
        if (!reserva.getHabitacion().getAlojamiento().getAnfitrion().getId().equals(anfitrion.getId())) {
            throw new ForbiddenException("No puede gestionar reservas de un alojamiento ajeno");
        }
        return reserva;
    }

    private void validarRolAnfitrion(Usuario anfitrion) {
        if (anfitrion.getRol() != RolUsuario.ANFITRION) {
            throw new ForbiddenException("Solo un anfitrion puede gestionar estas reservas");
        }
    }

    private void exigirEstado(Reserva reserva, EstadoReserva esperado) {
        if (reserva.getEstadoReserva() != esperado) {
            throw new ConflictException("La transicion de estado de la reserva no esta permitida");
        }
    }

    private ReservaResponse guardarCambioAnfitrion(Reserva reserva, Usuario anfitrion, String accion) {
        reserva.setActualizadoEn(LocalDateTime.now());
        Reserva guardada = reservaRepository.save(reserva);
        auditoriaService.guardar(anfitrion, accion, "reserva", guardada.getId(), guardada.getCodigo());
        return ReservaResponse.desde(guardada);
    }

    private void validarSolicitud(ReservaRequest request, boolean exigirCondiciones) {
        if (request.fechaEntrada().isBefore(LocalDate.now())) {
            throw new BadRequestException("La fecha de entrada no puede estar en el pasado");
        }
        if (!request.fechaSalida().isAfter(request.fechaEntrada())) {
            throw new BadRequestException("La fecha de salida debe ser posterior a la de entrada");
        }
        if (request.adultos() < 1 || request.ninos() < 0 || request.adultos() + request.ninos() > 16) {
            throw new BadRequestException("La reserva debe tener entre 1 y 16 huespedes y al menos un adulto");
        }
        if (request.unidades() < 1 || request.unidades() > request.adultos()) {
            throw new BadRequestException("Las unidades deben ser al menos 1 y no superar la cantidad de adultos");
        }
        if (exigirCondiciones && !Boolean.TRUE.equals(request.aceptoCondiciones())) {
            throw new BadRequestException("Debe aceptar las condiciones");
        }
        try {
            Math.toIntExact(ChronoUnit.DAYS.between(request.fechaEntrada(), request.fechaSalida()));
        } catch (ArithmeticException exception) {
            throw new BadRequestException("El rango de fechas es demasiado amplio");
        }
    }

    private void validarCapacidad(Habitacion habitacion, ReservaRequest request) {
        int huespedes = request.adultos() + request.ninos();
        if (huespedes > habitacion.getCapacidadMaxima() * request.unidades()) {
            throw new BadRequestException("La capacidad de las unidades seleccionadas no es suficiente");
        }
    }

    private void validarHabitacionReservable(Habitacion habitacion, ReservaRequest request) {
        if (!Boolean.TRUE.equals(habitacion.getActiva())
                || habitacion.getAlojamiento().getEstado() != EstadoAlojamiento.ACTIVO) {
            throw new ConflictException("La habitacion no esta disponible para reservas");
        }
        validarCapacidad(habitacion, request);
        if (request.unidades() > habitacion.getUnidadesTotales()) {
            throw new BadRequestException("Las unidades solicitadas superan las unidades de esta habitacion");
        }
        validarDisponibilidad(habitacion, request.fechaEntrada(), request.fechaSalida(), request.unidades());
    }

    private CalculoReserva calcularPrecio(Habitacion habitacion, ReservaRequest request) {
        int noches = Math.toIntExact(ChronoUnit.DAYS.between(request.fechaEntrada(), request.fechaSalida()));
        List<Tarifa> tarifas = tarifaRepository.findByHabitacion(habitacion);
        List<ReservaNoche> nochesReservadas = new ArrayList<>(noches);
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LocalDate fecha = request.fechaEntrada(); fecha.isBefore(request.fechaSalida()); fecha = fecha.plusDays(1)) {
            BigDecimal precioNoche = precioVigente(habitacion, tarifas, fecha);
            subtotal = subtotal.add(precioNoche.multiply(BigDecimal.valueOf(request.unidades())));
            ReservaNoche reservaNoche = new ReservaNoche();
            reservaNoche.setFecha(fecha);
            reservaNoche.setPrecioAplicado(precioNoche);
            nochesReservadas.add(reservaNoche);
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal cargosAdicionales = habitacion.getCargoLimpieza()
            .multiply(BigDecimal.valueOf(request.unidades())).setScale(2, RoundingMode.HALF_UP);
        return new CalculoReserva(noches, nochesReservadas, subtotal, cargosAdicionales,
            subtotal.add(cargosAdicionales).setScale(2, RoundingMode.HALF_UP));
    }

    private void validarDisponibilidad(Habitacion habitacion, LocalDate entrada, LocalDate salida, int unidades) {
        List<Reserva> reservas = reservaRepository.findByHabitacion(habitacion);
        List<BloqueoDisponibilidad> bloqueos = bloqueoDisponibilidadRepository.findByHabitacion(habitacion);
        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            LocalDate fechaNoche = noche;
            int unidadesReservadas = reservas.stream()
                .filter(reserva -> reserva.getEstadoReserva() == EstadoReserva.PENDIENTE_CONFIRMACION
                    || reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA)
                .filter(reserva -> !fechaNoche.isBefore(reserva.getFechaEntrada()) && fechaNoche.isBefore(reserva.getFechaSalida()))
                .mapToInt(Reserva::getUnidades)
                .sum();
            int unidadesBloqueadas = bloqueos.stream()
                .filter(bloqueo -> !fechaNoche.isBefore(bloqueo.getFechaInicio()) && !fechaNoche.isAfter(bloqueo.getFechaFin()))
                .mapToInt(BloqueoDisponibilidad::getUnidadesBloqueadas)
                .sum();
            if (habitacion.getUnidadesTotales() - unidadesReservadas - unidadesBloqueadas < unidades) {
                throw new ConflictException("No hay suficientes unidades disponibles para todas las noches solicitadas");
            }
        }
    }

    private BigDecimal precioVigente(Habitacion habitacion, List<Tarifa> tarifas, LocalDate fecha) {
        return tarifas.stream()
            .filter(tarifa -> !fecha.isBefore(tarifa.getFechaInicio()) && !fecha.isAfter(tarifa.getFechaFin()))
            .map(Tarifa::getPrecioNoche)
            .findFirst()
            .orElse(habitacion.getPrecioBaseNoche())
            .setScale(2, RoundingMode.HALF_UP);
    }

    private Reserva buscar(String codigo) {
        return reservaRepository.findByCodigo(codigo)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    private void verificarAcceso(Reserva reserva, Usuario usuario) {
        boolean esHuesped = usuario.getRol() == RolUsuario.HUESPED
            && reserva.getUsuario().getId().equals(usuario.getId());
        boolean esAnfitrion = usuario.getRol() == RolUsuario.ANFITRION
            && reserva.getHabitacion().getAlojamiento().getAnfitrion().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRol() == RolUsuario.ADMINISTRADOR;
        if (!esHuesped && !esAnfitrion && !esAdmin) {
            throw new ForbiddenException("No puede consultar esta reserva");
        }
    }

    private String generarCodigoReserva() {
        for (int intento = 0; intento < 10; intento++) {
            StringBuilder codigo = new StringBuilder("LR-");
            for (int indice = 0; indice < 8; indice++) {
                codigo.append(CODIGO_CHARS.charAt(RANDOM.nextInt(CODIGO_CHARS.length())));
            }
            if (reservaRepository.findByCodigo(codigo.toString()).isEmpty()) {
                return codigo.toString();
            }
        }
        throw new ConflictException("No se pudo generar un codigo de reserva unico; intente nuevamente");
    }

    private record CalculoReserva(int noches, List<ReservaNoche> nochesReservadas,
                                  BigDecimal subtotal, BigDecimal cargosAdicionales, BigDecimal total) {
    }
}
