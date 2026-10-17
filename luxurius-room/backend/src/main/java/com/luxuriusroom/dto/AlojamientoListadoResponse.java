package com.luxuriusroom.dto;

import com.luxuriusroom.model.TipoAlojamiento;

import java.math.BigDecimal;

public record AlojamientoListadoResponse(
    Long id,
    String nombre,
    String ciudad,
    String pais,
    TipoAlojamiento tipo,
    BigDecimal calificacionPromedio,
    String imagenPrincipal
) {
}