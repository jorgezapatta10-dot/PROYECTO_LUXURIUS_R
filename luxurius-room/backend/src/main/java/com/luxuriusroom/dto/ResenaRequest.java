package com.luxuriusroom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResenaRequest(
    @NotBlank String codigoReserva,
    @NotNull @Min(1) @Max(5) Integer calificacion,
    @Size(max = 1000) String comentario
) {
}