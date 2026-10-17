package com.luxuriusroom.controller;

import com.luxuriusroom.service.AlojamientoService;
import com.luxuriusroom.dto.AlojamientoDisponibilidadResponse;
import com.luxuriusroom.dto.HabitacionResponse;
import com.luxuriusroom.model.TipoAlojamiento;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class AlojamientoController {
    private final AlojamientoService alojamientoService;

    public AlojamientoController(AlojamientoService alojamientoService) {
        this.alojamientoService = alojamientoService;
    }

    @GetMapping("/alojamientos")
    public Object listar(@RequestParam(required = false) String ciudad,
                         @RequestParam(required = false) TipoAlojamiento tipo,
                         @RequestParam(required = false) BigDecimal precioMinimo,
                         @RequestParam(required = false) BigDecimal precioMaximo,
                         @RequestParam(required = false) BigDecimal calificacionMinima,
                         @RequestParam(required = false) Integer capacidad,
                         @RequestParam(defaultValue = "false") boolean wifi,
                         @RequestParam(defaultValue = "false") boolean estacionamiento,
                         @RequestParam(defaultValue = "false") boolean desayuno,
                         @RequestParam(defaultValue = "false") boolean aireAcondicionado,
                         @RequestParam(required = false, defaultValue = "0") Integer pagina,
                         @RequestParam(required = false, defaultValue = "12") Integer tamano) {
        return alojamientoService.listarAlojamientos(ciudad, tipo, precioMinimo, precioMaximo,
            calificacionMinima, capacidad, wifi, estacionamiento, desayuno, aireAcondicionado, pagina, tamano);
    }

    @GetMapping("/alojamientos/{id}")
    public Object detalle(@PathVariable Long id) {
        return alojamientoService.detalleCompleto(id);
    }

    @GetMapping("/alojamientos/{id}/habitaciones")
    public java.util.List<HabitacionResponse> habitaciones(@PathVariable Long id) {
        return alojamientoService.habitacionesAlojamiento(id);
    }

    @GetMapping("/alojamientos/{id}/disponibilidad")
    public AlojamientoDisponibilidadResponse disponibilidad(@PathVariable Long id) {
        return alojamientoService.disponibilidadAlojamiento(id);
    }

    @GetMapping("/alojamientos/{id}/resenas")
    public Object resenas(@PathVariable Long id) {
        return alojamientoService.resenasPublicas(id);
    }
}
