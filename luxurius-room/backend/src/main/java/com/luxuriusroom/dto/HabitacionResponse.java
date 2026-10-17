package com.luxuriusroom.dto;

import com.luxuriusroom.model.Habitacion;

import java.math.BigDecimal;

public record HabitacionResponse(
    Long id,
    String nombre,
    String descripcion,
    Integer capacidadMaxima,
    String camas,
    Integer unidadesTotales,
    BigDecimal precioBaseNoche,
    BigDecimal cargoLimpieza,
    Boolean activa
) {
    public static HabitacionResponse desde(Habitacion habitacion) {
        return new HabitacionResponse(habitacion.getId(), habitacion.getNombre(), habitacion.getDescripcion(),
            habitacion.getCapacidadMaxima(), habitacion.getCamas(), habitacion.getUnidadesTotales(),
            habitacion.getPrecioBaseNoche(), habitacion.getCargoLimpieza(), habitacion.getActiva());
    }
}