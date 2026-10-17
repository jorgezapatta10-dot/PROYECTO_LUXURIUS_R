package com.luxuriusroom.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BloqueoRequest(
    @NotNull LocalDate fechaInicio,
    @NotNull LocalDate fechaFin,
    @NotNull @Positive Integer unidadesBloqueadas,
    @Size(max = 255) String motivo
) {
}