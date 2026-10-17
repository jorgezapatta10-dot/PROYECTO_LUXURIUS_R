package com.luxuriusroom.dto;

import jakarta.validation.constraints.NotBlank;

public record PerfilRequest(
    @NotBlank String nombre,
    @NotBlank String apellido,
    String telefono
) {
}
