package com.luxuriusroom.controller;

import com.luxuriusroom.dto.AlojamientoRequest;
import com.luxuriusroom.dto.HabitacionRequest;
import com.luxuriusroom.dto.TarifaRequest;
import com.luxuriusroom.dto.BloqueoRequest;
import com.luxuriusroom.dto.ImagenRequest;
import com.luxuriusroom.dto.ReporteResenaRequest;
import com.luxuriusroom.service.AlojamientoService;
import com.luxuriusroom.service.ReservaService;
import com.luxuriusroom.service.ResenaService;
import com.luxuriusroom.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anfitrion")
public class AnfitrionController {
    private final AlojamientoService alojamientoService;
    private final ReservaService reservaService;
    private final ResenaService resenaService;
    private final UsuarioService usuarioService;

    public AnfitrionController(AlojamientoService alojamientoService, ReservaService reservaService,
                               ResenaService resenaService, UsuarioService usuarioService) {
        this.alojamientoService = alojamientoService;
        this.reservaService = reservaService;
        this.resenaService = resenaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/alojamientos")
    public ResponseEntity<?> crearAlojamiento(@Valid @RequestBody AlojamientoRequest request) {
        return ResponseEntity.ok(alojamientoService.crearAlojamiento(usuarioService.obtenerUsuarioActual(), request));
    }

    @GetMapping("/alojamientos")
    public ResponseEntity<?> misAlojamientos() {
        return ResponseEntity.ok(alojamientoService.alojamientosDeAnfitrion(usuarioService.obtenerUsuarioActual()));
    }

    @GetMapping("/alojamientos/{id}")
    public ResponseEntity<?> obtenerAlojamiento(@PathVariable Long id) {
        return ResponseEntity.ok(alojamientoService.obtenerAlojamientoAnfitrion(
            id, usuarioService.obtenerUsuarioActual()));
    }

    @PutMapping("/alojamientos/{id}")
    public ResponseEntity<?> editarAlojamiento(@PathVariable Long id, @Valid @RequestBody AlojamientoRequest request) {
        return ResponseEntity.ok(alojamientoService.editarAlojamiento(id, usuarioService.obtenerUsuarioActual(), request));
    }

    @PostMapping("/alojamientos/{id}/habitaciones")
    public ResponseEntity<?> crearHabitacion(@PathVariable Long id, @Valid @RequestBody HabitacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alojamientoService.crearHabitacion(
            id, usuarioService.obtenerUsuarioActual(), request));
    }

    @PutMapping("/alojamientos/{id}/habitaciones/{habitacionId}")
    public ResponseEntity<?> editarHabitacion(@PathVariable Long id, @PathVariable Long habitacionId,
                                               @Valid @RequestBody HabitacionRequest request) {
        return ResponseEntity.ok(alojamientoService.editarHabitacion(id, habitacionId,
            usuarioService.obtenerUsuarioActual(), request));
    }

    @PutMapping("/alojamientos/{id}/servicios")
    public ResponseEntity<Void> asignarServicios(@PathVariable Long id, @RequestBody java.util.List<String> codigos) {
        alojamientoService.asignarServicios(id, usuarioService.obtenerUsuarioActual(), codigos);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/alojamientos/{id}/imagenes")
    public ResponseEntity<?> agregarImagen(@PathVariable Long id, @Valid @RequestBody ImagenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("url",
            alojamientoService.guardarImagen(id, usuarioService.obtenerUsuarioActual(), request)));
    }

    @PostMapping("/habitaciones/{id}/tarifas")
    public ResponseEntity<?> crearTarifa(@PathVariable Long id, @Valid @RequestBody TarifaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alojamientoService.crearTarifa(
            id, usuarioService.obtenerUsuarioActual(), request));
    }

    @PostMapping("/habitaciones/{id}/bloqueos")
    public ResponseEntity<?> crearBloqueo(@PathVariable Long id, @Valid @RequestBody BloqueoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alojamientoService.crearBloqueo(
            id, usuarioService.obtenerUsuarioActual(), request));
    }

    @DeleteMapping("/bloqueos/{id}")
    public ResponseEntity<Void> eliminarBloqueo(@PathVariable Long id) {
        alojamientoService.eliminarBloqueo(id, usuarioService.obtenerUsuarioActual());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservas")
    public ResponseEntity<?> reservas() {
        return ResponseEntity.ok(reservaService.listarAnfitrion(usuarioService.obtenerUsuarioActual()));
    }

    @PatchMapping("/reservas/{codigo}/confirmar")
    public ResponseEntity<?> confirmarReserva(@PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.confirmar(codigo, usuarioService.obtenerUsuarioActual()));
    }

    @PatchMapping("/reservas/{codigo}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.cancelar(codigo, usuarioService.obtenerUsuarioActual()));
    }

    @PatchMapping("/reservas/{codigo}/completar")
    public ResponseEntity<?> completarReserva(@PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.completar(codigo, usuarioService.obtenerUsuarioActual()));
    }

    @PatchMapping("/reservas/{codigo}/no-presentado")
    public ResponseEntity<?> marcarNoPresentado(@PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.marcarNoPresentado(codigo, usuarioService.obtenerUsuarioActual()));
    }

    @PatchMapping("/reservas/{codigo}/pago")
    public ResponseEntity<?> registrarPago(@PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.registrarPagoEnSitio(codigo, usuarioService.obtenerUsuarioActual()));
    }

    @GetMapping("/resenas")
    public ResponseEntity<?> resenas() {
        return ResponseEntity.ok(resenaService.listarAnfitrion(usuarioService.obtenerUsuarioActual()));
    }

    @PostMapping("/resenas/{id}/reportar")
    public ResponseEntity<?> reportarResena(@PathVariable Long id, @Valid @RequestBody ReporteResenaRequest request) {
        return ResponseEntity.ok(resenaService.reportar(id, usuarioService.obtenerUsuarioActual(), request));
    }
}
