package com.luxuriusroom.controller;

import com.luxuriusroom.model.EstadoAlojamiento;
import com.luxuriusroom.service.AdminService;
import com.luxuriusroom.service.ResenaService;
import com.luxuriusroom.service.UsuarioService;
import com.luxuriusroom.dto.ReporteResenaRequest;
import com.luxuriusroom.dto.ResenaResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final ResenaService resenaService;
    private final UsuarioService usuarioService;

    public AdminController(AdminService adminService, ResenaService resenaService, UsuarioService usuarioService) {
        this.adminService = adminService;
        this.resenaService = resenaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        return ResponseEntity.ok(adminService.listarUsuarios());
    }

    @PatchMapping("/usuarios/{id}/activo")
    public ResponseEntity<?> cambiarEstadoUsuario(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(adminService.cambiarEstadoUsuario(id, activo, usuarioService.obtenerUsuarioActual()));
    }

    @GetMapping("/alojamientos")
    public ResponseEntity<?> listarAlojamientos() {
        return ResponseEntity.ok(adminService.listarAlojamientos());
    }

    @PatchMapping("/alojamientos/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.cambiarEstadoAlojamiento(id, EstadoAlojamiento.ACTIVO,
            usuarioService.obtenerUsuarioActual()));
    }

    @PatchMapping("/alojamientos/{id}/suspender")
    public ResponseEntity<?> suspender(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.cambiarEstadoAlojamiento(id, EstadoAlojamiento.SUSPENDIDO,
            usuarioService.obtenerUsuarioActual()));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<?> estadisticas() {
        return ResponseEntity.ok(adminService.estadisticas());
    }

    @GetMapping("/reservas")
    public ResponseEntity<?> reservas() {
        return ResponseEntity.ok(adminService.listarReservas());
    }

    @GetMapping("/resenas/reportadas")
    public ResponseEntity<?> resenasReportadas() {
        return ResponseEntity.ok(resenaService.listarReportadas());
    }

    @PatchMapping("/resenas/{id}/ocultar")
    public ResponseEntity<ResenaResponse> ocultarResena(@PathVariable Long id) {
        return ResponseEntity.ok(resenaService.ocultar(id, usuarioService.obtenerUsuarioActual()));
    }

    @PatchMapping("/resenas/{id}/restaurar")
    public ResponseEntity<ResenaResponse> restaurarResena(@PathVariable Long id) {
        return ResponseEntity.ok(resenaService.restaurar(id, usuarioService.obtenerUsuarioActual()));
    }
}
