package com.luxuriusroom.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaRequest(
    @NotNull LocalDate fechaInicio,
    @NotNull LocalDate fechaFin,
    @NotNull @DecimalMin("0.00") BigDecimal precioNoche,
    @Size(max = 255) String descripcion
) {
}