package com.luxuriusroom.dto;

import com.luxuriusroom.model.PoliticaCancelacion;
import com.luxuriusroom.model.TipoAlojamiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlojamientoRequest(
    @NotBlank String nombre,
    @NotNull TipoAlojamiento tipo,
    String descripcion,
    @NotBlank String ciudad,
    @NotBlank String pais,
    @NotBlank String direccion,
    String reglas,
    @NotNull PoliticaCancelacion politicaCancelacion
) {
}
