package com.luxuriusroom.controller;

import com.luxuriusroom.dto.ReservaRequest;
import com.luxuriusroom.dto.ReservaCotizacionResponse;
import com.luxuriusroom.dto.ReservaResponse;
import com.luxuriusroom.service.ReservaService;
import com.luxuriusroom.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservaController {
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    public ReservaController(ReservaService reservaService, UsuarioService usuarioService) {
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/reservas/cotizacion")
    public ResponseEntity<ReservaCotizacionResponse> cotizar(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.ok(reservaService.cotizar(usuarioService.obtenerUsuarioActual(), request));
    }

    @PostMapping("/reservas")
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reservaService.crear(usuarioService.obtenerUsuarioActual(), request));
    }

    @GetMapping("/reservas/{codigo}")
    public ResponseEntity<ReservaResponse> detalle(@PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.obtener(codigo, usuarioService.obtenerUsuarioActual()));
    }

    @PatchMapping("/reservas/{codigo}/cancelar")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.cancelar(codigo, usuarioService.obtenerUsuarioActual()));
    }

}
