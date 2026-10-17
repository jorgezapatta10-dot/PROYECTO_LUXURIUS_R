package com.luxuriusroom.dto;

import java.time.LocalDate;
import java.util.List;

public record AlojamientoDisponibilidadResponse(Long alojamientoId, List<HabitacionDisponibilidad> habitaciones) {
    public record HabitacionDisponibilidad(Long id, String nombre, Integer unidadesTotales,
                                           List<PeriodoNoDisponible> periodos) {
    }

    public record PeriodoNoDisponible(LocalDate fechaInicio, LocalDate fechaFin, Integer unidades, String tipo) {
    }
}