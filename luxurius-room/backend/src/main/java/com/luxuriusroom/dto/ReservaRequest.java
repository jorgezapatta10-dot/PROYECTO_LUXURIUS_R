package com.luxuriusroom.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservaRequest(
    @NotNull Long habitacionId,
    @NotNull LocalDate fechaEntrada,
    @NotNull LocalDate fechaSalida,
    @NotNull Integer adultos,
    @NotNull Integer ninos,
    @NotNull Integer unidades,
    Boolean aceptoCondiciones
) {
}
