package com.luxuriusroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record HabitacionRequest(
    @NotBlank String nombre,
    String descripcion,
    @NotNull Integer capacidadMaxima,
    String camas,
    @NotNull Integer unidadesTotales,
    @NotNull BigDecimal precioBaseNoche,
    BigDecimal cargoLimpieza,
    Boolean activa
) {
}
