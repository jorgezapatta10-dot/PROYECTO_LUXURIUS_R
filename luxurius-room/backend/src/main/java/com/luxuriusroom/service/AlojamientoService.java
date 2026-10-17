package com.luxuriusroom.service;

import com.luxuriusroom.dto.AlojamientoRequest;
import com.luxuriusroom.dto.AlojamientoAnfitrionResponse;
import com.luxuriusroom.dto.AlojamientoListadoResponse;
import com.luxuriusroom.dto.AlojamientoDisponibilidadResponse;
import com.luxuriusroom.dto.HabitacionResponse;
import com.luxuriusroom.dto.HabitacionAnfitrionResponse;
import com.luxuriusroom.dto.HabitacionRequest;
import com.luxuriusroom.dto.TarifaRequest;
import com.luxuriusroom.dto.TarifaResponse;
import com.luxuriusroom.dto.BloqueoRequest;
import com.luxuriusroom.dto.BloqueoResponse;
import com.luxuriusroom.dto.ImagenRequest;
import com.luxuriusroom.dto.ResenaPublicaResponse;
import com.luxuriusroom.exception.BadRequestException;
import com.luxuriusroom.exception.ConflictException;
import com.luxuriusroom.exception.ForbiddenException;
import com.luxuriusroom.exception.ResourceNotFoundException;
import com.luxuriusroom.model.*;
import com.luxuriusroom.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlojamientoService {
    private final AlojamientoRepository alojamientoRepository;
    private final HabitacionRepository habitacionRepository;
    private final TarifaRepository tarifaRepository;
    private final BloqueoDisponibilidadRepository bloqueoDisponibilidadRepository;
    private final ImagenAlojamientoRepository imagenAlojamientoRepository;
    private final AlojamientoServicioRepository alojamientoServicioRepository;
    private final ServicioRepository servicioRepository;
    private final ResenaRepository resenaRepository;
    private final ReservaRepository reservaRepository;

    public AlojamientoService(AlojamientoRepository alojamientoRepository,
                              HabitacionRepository habitacionRepository,
                              TarifaRepository tarifaRepository,
                              BloqueoDisponibilidadRepository bloqueoDisponibilidadRepository,
                              ImagenAlojamientoRepository imagenAlojamientoRepository,
                              AlojamientoServicioRepository alojamientoServicioRepository,
                              ServicioRepository servicioRepository,
                              ResenaRepository resenaRepository,
                              ReservaRepository reservaRepository) {
        this.alojamientoRepository = alojamientoRepository;
        this.habitacionRepository = habitacionRepository;
        this.tarifaRepository = tarifaRepository;
        this.bloqueoDisponibilidadRepository = bloqueoDisponibilidadRepository;
        this.imagenAlojamientoRepository = imagenAlojamientoRepository;
        this.alojamientoServicioRepository = alojamientoServicioRepository;
        this.servicioRepository = servicioRepository;
        this.resenaRepository = resenaRepository;
        this.reservaRepository = reservaRepository;
    }

    public Page<AlojamientoListadoResponse> listarAlojamientos(String ciudad, Integer pagina, Integer tamano) {
        return listarAlojamientos(ciudad, null, null, null, null, null, false, false, false, false, pagina, tamano);
    }

    public Page<AlojamientoListadoResponse> listarAlojamientos(String ciudad, TipoAlojamiento tipo,
            BigDecimal precioMinimo, BigDecimal precioMaximo, BigDecimal calificacionMinima,
            Integer capacidad, boolean wifi, boolean estacionamiento, boolean desayuno,
            boolean aireAcondicionado, Integer pagina, Integer tamano) {
        if (precioMinimo != null && precioMaximo != null && precioMinimo.compareTo(precioMaximo) > 0) {
            throw new BadRequestException("El precio minimo no puede superar el precio maximo");
        }
        if (capacidad != null && capacidad < 1) {
            throw new BadRequestException("La capacidad debe ser al menos 1");
        }
        if (calificacionMinima != null && (calificacionMinima.signum() < 0 || calificacionMinima.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new BadRequestException("La calificacion minima debe estar entre 0 y 5");
        }
        Pageable pageable = PageRequest.of(pagina == null ? 0 : pagina, tamano == null ? 12 : tamano);
        Page<Alojamiento> page = alojamientoRepository.buscarPublicos(
            EstadoAlojamiento.ACTIVO,
            ciudad == null || ciudad.isBlank() ? null : ciudad.trim(),
            tipo,
            precioMinimo,
            precioMaximo,
            calificacionMinima,
            capacidad,
            wifi ? 1 : 0,
            estacionamiento ? 1 : 0,
            desayuno ? 1 : 0,
            aireAcondicionado ? 1 : 0,
            pageable);
        return page.map(alojamiento -> new AlojamientoListadoResponse(
            alojamiento.getId(),
            alojamiento.getNombre(),
            alojamiento.getCiudad(),
            alojamiento.getPais(),
            alojamiento.getTipo(),
            alojamiento.getCalificacionPromedio(),
            imagenAlojamientoRepository.findByAlojamientoOrderByOrdenAsc(alojamiento).stream()
                .filter(ImagenAlojamiento::getEsPrincipal)
                .findFirst()
                .map(ImagenAlojamiento::getUrl)
                .orElse(null)
        ));
    }

    public Alojamiento obtenerDetalle(Long id) {
        return alojamientoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alojamiento no encontrado"));
    }

    private Alojamiento obtenerAlojamientoActivo(Long id) {
        Alojamiento alojamiento = obtenerDetalle(id);
        if (alojamiento.getEstado() != EstadoAlojamiento.ACTIVO) {
            throw new ResourceNotFoundException("Alojamiento no encontrado");
        }
        return alojamiento;
    }

    public List<HabitacionResponse> habitacionesAlojamiento(Long id) {
        Alojamiento alojamiento = obtenerAlojamientoActivo(id);
        return habitacionRepository.findByAlojamiento(alojamiento).stream()
            .filter(habitacion -> Boolean.TRUE.equals(habitacion.getActiva()))
            .map(HabitacionResponse::desde)
            .toList();
    }

    public AlojamientoDisponibilidadResponse disponibilidadAlojamiento(Long id) {
        Alojamiento alojamiento = obtenerAlojamientoActivo(id);
        List<AlojamientoDisponibilidadResponse.HabitacionDisponibilidad> disponibilidad = habitacionRepository
            .findByAlojamiento(alojamiento).stream()
            .filter(habitacion -> Boolean.TRUE.equals(habitacion.getActiva()))
            .map(habitacion -> {
                List<AlojamientoDisponibilidadResponse.PeriodoNoDisponible> periodos = new ArrayList<>();
                reservaRepository.findByHabitacion(habitacion).stream()
                    .filter(reserva -> reserva.getEstadoReserva() == EstadoReserva.PENDIENTE_CONFIRMACION
                        || reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA)
                    .map(reserva -> new AlojamientoDisponibilidadResponse.PeriodoNoDisponible(
                        reserva.getFechaEntrada(), reserva.getFechaSalida().minusDays(1), reserva.getUnidades(), "RESERVADA"))
                    .forEach(periodos::add);
                bloqueoDisponibilidadRepository.findByHabitacion(habitacion).stream()
                    .map(bloqueo -> new AlojamientoDisponibilidadResponse.PeriodoNoDisponible(
                        bloqueo.getFechaInicio(), bloqueo.getFechaFin(), bloqueo.getUnidadesBloqueadas(), "BLOQUEADA"))
                    .forEach(periodos::add);
                periodos.sort(java.util.Comparator.comparing(AlojamientoDisponibilidadResponse.PeriodoNoDisponible::fechaInicio)
                    .thenComparing(AlojamientoDisponibilidadResponse.PeriodoNoDisponible::tipo));
                return new AlojamientoDisponibilidadResponse.HabitacionDisponibilidad(
                    habitacion.getId(), habitacion.getNombre(), habitacion.getUnidadesTotales(), periodos);
            })
            .toList();
        return new AlojamientoDisponibilidadResponse(alojamiento.getId(), disponibilidad);
    }

    @Transactional(readOnly = true)
    public List<ResenaPublicaResponse> resenasPublicas(Long id) {
        Alojamiento alojamiento = obtenerAlojamientoActivo(id);
        return resenaRepository.findByAlojamientoAndEstadoOrderByCreadoEnDesc(alojamiento, EstadoResena.VISIBLE)
            .stream().map(ResenaPublicaResponse::desde).toList();
    }

    @Transactional
    public AlojamientoAnfitrionResponse crearAlojamiento(Usuario anfitrion, AlojamientoRequest request) {
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setAnfitrion(anfitrion);
        alojamiento.setNombre(request.nombre());
        alojamiento.setTipo(request.tipo());
        alojamiento.setDescripcion(request.descripcion());
        alojamiento.setCiudad(request.ciudad());
        alojamiento.setPais(request.pais());
        alojamiento.setDireccion(request.direccion());
        alojamiento.setReglas(request.reglas());
        alojamiento.setPoliticaCancelacion(request.politicaCancelacion());
        alojamiento.setEstado(EstadoAlojamiento.PENDIENTE_APROBACION);
        alojamiento.setCreadoEn(LocalDateTime.now());
        return respuestaAnfitrion(alojamientoRepository.save(alojamiento));
    }

    @Transactional
    public AlojamientoAnfitrionResponse editarAlojamiento(Long id, Usuario anfitrion, AlojamientoRequest request) {
        Alojamiento alojamiento = obtenerDetalle(id);
        if (!alojamiento.getAnfitrion().getId().equals(anfitrion.getId())) {
            throw new ForbiddenException("No puede modificar un alojamiento ajeno");
        }
        alojamiento.setNombre(request.nombre());
        alojamiento.setTipo(request.tipo());
        alojamiento.setDescripcion(request.descripcion());
        alojamiento.setCiudad(request.ciudad());
        alojamiento.setPais(request.pais());
        alojamiento.setDireccion(request.direccion());
        alojamiento.setReglas(request.reglas());
        alojamiento.setPoliticaCancelacion(request.politicaCancelacion());
        return respuestaAnfitrion(alojamientoRepository.save(alojamiento));
    }

    @Transactional(readOnly = true)
    public List<AlojamientoAnfitrionResponse> alojamientosDeAnfitrion(Usuario anfitrion) {
        return alojamientoRepository.findByAnfitrion(anfitrion).stream().map(this::respuestaAnfitrion).toList();
    }

    @Transactional(readOnly = true)
    public AlojamientoAnfitrionResponse obtenerAlojamientoAnfitrion(Long id, Usuario anfitrion) {
        Alojamiento alojamiento = obtenerDetalle(id);
        verificarPropiedad(alojamiento, anfitrion);
        return respuestaAnfitrion(alojamiento);
    }

    @Transactional
    public HabitacionResponse crearHabitacion(Long alojamientoId, Usuario anfitrion, HabitacionRequest request) {
        Alojamiento alojamiento = obtenerDetalle(alojamientoId);
        verificarPropiedad(alojamiento, anfitrion);
        Habitacion habitacion = new Habitacion();
        habitacion.setAlojamiento(alojamiento);
        habitacion.setNombre(request.nombre());
        habitacion.setDescripcion(request.descripcion());
        habitacion.setCapacidadMaxima(request.capacidadMaxima());
        habitacion.setCamas(request.camas());
        habitacion.setUnidadesTotales(request.unidadesTotales());
        habitacion.setPrecioBaseNoche(request.precioBaseNoche());
        habitacion.setCargoLimpieza(request.cargoLimpieza() == null ? BigDecimal.ZERO : request.cargoLimpieza());
        habitacion.setActiva(request.activa() == null || request.activa());
        return HabitacionResponse.desde(habitacionRepository.save(habitacion));
    }

    @Transactional
    public HabitacionResponse editarHabitacion(Long alojamientoId, Long habitacionId, Usuario anfitrion,
                                               HabitacionRequest request) {
        Habitacion habitacion = habitacionRepository.findByIdForUpdate(habitacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada"));
        if (!habitacion.getAlojamiento().getId().equals(alojamientoId)) {
            throw new ResourceNotFoundException("Habitación no encontrada");
        }
        verificarPropiedad(habitacion.getAlojamiento(), anfitrion);
        habitacion.setNombre(request.nombre());
        habitacion.setDescripcion(request.descripcion());
        habitacion.setCapacidadMaxima(request.capacidadMaxima());
        habitacion.setCamas(request.camas());
        habitacion.setUnidadesTotales(request.unidadesTotales());
        habitacion.setPrecioBaseNoche(request.precioBaseNoche());
        habitacion.setCargoLimpieza(request.cargoLimpieza() == null ? BigDecimal.ZERO : request.cargoLimpieza());
        habitacion.setActiva(request.activa() == null || request.activa());
        return HabitacionResponse.desde(habitacionRepository.save(habitacion));
    }

    @Transactional
    public TarifaResponse crearTarifa(Long habitacionId, Usuario anfitrion, TarifaRequest request) {
        Habitacion habitacion = habitacionRepository.findByIdForUpdate(habitacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada"));
        verificarPropiedad(habitacion.getAlojamiento(), anfitrion);
        validarRango(request.fechaInicio(), request.fechaFin());
        boolean seSolapa = tarifaRepository.findByHabitacion(habitacion).stream()
            .anyMatch(tarifa -> !tarifa.getFechaFin().isBefore(request.fechaInicio())
                && !tarifa.getFechaInicio().isAfter(request.fechaFin()));
        if (seSolapa) {
            throw new ConflictException("El rango de tarifa se solapa con otra tarifa de esta habitación");
        }
        Tarifa tarifa = new Tarifa();
        tarifa.setHabitacion(habitacion);
        tarifa.setFechaInicio(request.fechaInicio());
        tarifa.setFechaFin(request.fechaFin());
        tarifa.setPrecioNoche(request.precioNoche());
        tarifa.setDescripcion(request.descripcion());
        return TarifaResponse.desde(tarifaRepository.save(tarifa));
    }

    @Transactional
    public BloqueoResponse crearBloqueo(Long habitacionId, Usuario anfitrion, BloqueoRequest request) {
        Habitacion habitacion = habitacionRepository.findByIdForUpdate(habitacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada"));
        verificarPropiedad(habitacion.getAlojamiento(), anfitrion);
        validarRango(request.fechaInicio(), request.fechaFin());
        if (request.unidadesBloqueadas() > habitacion.getUnidadesTotales()) {
            throw new BadRequestException("Las unidades bloqueadas superan el inventario de la habitación");
        }

        List<Reserva> reservas = reservaRepository.findByHabitacion(habitacion);
        List<BloqueoDisponibilidad> bloqueos = bloqueoDisponibilidadRepository.findByHabitacion(habitacion);
        for (LocalDate fecha = request.fechaInicio(); !fecha.isAfter(request.fechaFin()); fecha = fecha.plusDays(1)) {
            LocalDate noche = fecha;
            int unidadesReservadas = reservas.stream()
                .filter(reserva -> reserva.getEstadoReserva() == EstadoReserva.PENDIENTE_CONFIRMACION
                    || reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA)
                .filter(reserva -> !noche.isBefore(reserva.getFechaEntrada()) && noche.isBefore(reserva.getFechaSalida()))
                .mapToInt(Reserva::getUnidades)
                .sum();
            int unidadesBloqueadas = bloqueos.stream()
                .filter(bloqueo -> !noche.isBefore(bloqueo.getFechaInicio()) && !noche.isAfter(bloqueo.getFechaFin()))
                .mapToInt(BloqueoDisponibilidad::getUnidadesBloqueadas)
                .sum();
            if (unidadesReservadas + unidadesBloqueadas + request.unidadesBloqueadas() > habitacion.getUnidadesTotales()) {
                throw new ConflictException("No se pueden bloquear unidades ya reservadas o bloqueadas");
            }
        }

        BloqueoDisponibilidad bloqueo = new BloqueoDisponibilidad();
        bloqueo.setHabitacion(habitacion);
        bloqueo.setFechaInicio(request.fechaInicio());
        bloqueo.setFechaFin(request.fechaFin());
        bloqueo.setUnidadesBloqueadas(request.unidadesBloqueadas());
        bloqueo.setMotivo(request.motivo());
        return BloqueoResponse.desde(bloqueoDisponibilidadRepository.save(bloqueo));
    }

    @Transactional
    public void eliminarBloqueo(Long bloqueoId, Usuario anfitrion) {
        BloqueoDisponibilidad bloqueo = bloqueoDisponibilidadRepository.findById(bloqueoId)
            .orElseThrow(() -> new ResourceNotFoundException("Bloqueo no encontrado"));
        Habitacion habitacion = habitacionRepository.findByIdForUpdate(bloqueo.getHabitacion().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Habitación no encontrada"));
        verificarPropiedad(habitacion.getAlojamiento(), anfitrion);
        bloqueoDisponibilidadRepository.delete(bloqueo);
    }

    private void validarRango(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null || fechaFin.isBefore(fechaInicio)) {
            throw new BadRequestException("La fecha fin debe ser igual o posterior a la fecha inicio");
        }
    }

    private void verificarPropiedad(Alojamiento alojamiento, Usuario anfitrion) {
        if (anfitrion == null || alojamiento.getAnfitrion() == null
                || !alojamiento.getAnfitrion().getId().equals(anfitrion.getId())) {
            throw new ForbiddenException("No puede modificar recursos de un alojamiento ajeno");
        }
    }

    @Transactional
    public void asignarServicios(Long alojamientoId, Usuario anfitrion, List<String> codigos) {
        Alojamiento alojamiento = obtenerDetalle(alojamientoId);
        verificarPropiedad(alojamiento, anfitrion);
        alojamientoServicioRepository.deleteByAlojamiento(alojamiento);
        for (String codigo : codigos) {
            Servicio servicio = servicioRepository.findByCodigo(codigo)
                .orElseGet(() -> {
                    Servicio nuevo = new Servicio();
                    nuevo.setCodigo(codigo);
                    nuevo.setNombre(codigo);
                    return servicioRepository.save(nuevo);
                });
            AlojamientoServicio rel = new AlojamientoServicio();
            rel.setAlojamiento(alojamiento);
            rel.setServicio(servicio);
            alojamientoServicioRepository.save(rel);
        }
    }

    @Transactional
    public String guardarImagen(Long alojamientoId, Usuario anfitrion, ImagenRequest request) {
        Alojamiento alojamiento = obtenerDetalle(alojamientoId);
        verificarPropiedad(alojamiento, anfitrion);
        ImagenAlojamiento imagen = new ImagenAlojamiento();
        imagen.setAlojamiento(alojamiento);
        imagen.setUrl(request.url());
        imagen.setOrden(imagenAlojamientoRepository.findByAlojamientoOrderByOrdenAsc(alojamiento).size());
        imagen.setEsPrincipal(false);
        return imagenAlojamientoRepository.save(imagen).getUrl();
    }

    private AlojamientoAnfitrionResponse respuestaAnfitrion(Alojamiento alojamiento) {
        List<HabitacionAnfitrionResponse> habitaciones = habitacionRepository.findByAlojamiento(alojamiento).stream()
            .map(habitacion -> HabitacionAnfitrionResponse.desde(habitacion,
                tarifaRepository.findByHabitacion(habitacion).stream().map(TarifaResponse::desde).toList(),
                bloqueoDisponibilidadRepository.findByHabitacion(habitacion).stream().map(BloqueoResponse::desde).toList()))
            .toList();
        List<String> imagenes = imagenAlojamientoRepository.findByAlojamientoOrderByOrdenAsc(alojamiento).stream()
            .map(ImagenAlojamiento::getUrl).toList();
        List<String> servicios = alojamientoServicioRepository.findByAlojamiento(alojamiento).stream()
            .map(rel -> rel.getServicio().getCodigo()).toList();
        return new AlojamientoAnfitrionResponse(alojamiento.getId(), alojamiento.getNombre(), alojamiento.getTipo(),
            alojamiento.getDescripcion(), alojamiento.getCiudad(), alojamiento.getPais(), alojamiento.getDireccion(),
            alojamiento.getReglas(), alojamiento.getPoliticaCancelacion(), alojamiento.getEstado(),
            alojamiento.getCalificacionPromedio(), habitaciones, imagenes, servicios);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detalleCompleto(Long id) {
        Alojamiento alojamiento = obtenerAlojamientoActivo(id);
        List<Habitacion> habitaciones = habitacionRepository.findByAlojamiento(alojamiento);
        List<Map<String, Object>> servicios = alojamientoServicioRepository.findByAlojamiento(alojamiento).stream()
            .map(rel -> {
                Map<String, Object> servicio = new HashMap<>();
                servicio.put("id", rel.getServicio().getId());
                servicio.put("codigo", rel.getServicio().getCodigo());
                servicio.put("nombre", rel.getServicio().getNombre());
                return servicio;
            })
            .toList();
        List<String> imagenes = imagenAlojamientoRepository.findByAlojamientoOrderByOrdenAsc(alojamiento).stream()
            .map(ImagenAlojamiento::getUrl)
            .toList();
        List<Map<String, Object>> reseñas = resenaRepository.findByAlojamientoAndEstadoOrderByCreadoEnDesc(alojamiento, EstadoResena.VISIBLE).stream()
            .map(r -> {
                Map<String, Object> resena = new HashMap<>();
                resena.put("id", r.getId());
                resena.put("usuario", r.getUsuario().getNombre());
                resena.put("calificacion", r.getCalificacion());
                resena.put("comentario", r.getComentario());
                return resena;
            })
            .toList();

        Map<String, Object> detalle = new HashMap<>();
        detalle.put("id", alojamiento.getId());
        detalle.put("nombre", alojamiento.getNombre());
        detalle.put("ciudad", alojamiento.getCiudad());
        detalle.put("pais", alojamiento.getPais());
        detalle.put("descripcion", alojamiento.getDescripcion());
        detalle.put("direccion", alojamiento.getDireccion());
        detalle.put("reglas", alojamiento.getReglas());
        detalle.put("tipo", alojamiento.getTipo());
        detalle.put("politicaCancelacion", alojamiento.getPoliticaCancelacion());
        detalle.put("estado", alojamiento.getEstado());
        detalle.put("calificacionPromedio", alojamiento.getCalificacionPromedio());
        detalle.put("servicios", servicios);
        detalle.put("imagenes", imagenes);
        detalle.put("habitaciones", habitaciones.stream().filter(h -> Boolean.TRUE.equals(h.getActiva())).map(h -> {
            Map<String, Object> habitacion = new HashMap<>();
            habitacion.put("id", h.getId());
            habitacion.put("nombre", h.getNombre());
            habitacion.put("capacidadMaxima", h.getCapacidadMaxima());
            habitacion.put("camas", h.getCamas());
            habitacion.put("unidadesTotales", h.getUnidadesTotales());
            habitacion.put("precioBaseNoche", h.getPrecioBaseNoche());
            habitacion.put("cargoLimpieza", h.getCargoLimpieza());
            return habitacion;
        })
            .toList());
        detalle.put("resenas", reseñas);
        return detalle;
    }
}
