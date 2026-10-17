package com.luxuriusroom.dto;

import com.luxuriusroom.model.Tarifa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaResponse(Long id, Long habitacionId, LocalDate fechaInicio, LocalDate fechaFin,
                             BigDecimal precioNoche, String descripcion) {
    public static TarifaResponse desde(Tarifa tarifa) {
        return new TarifaResponse(tarifa.getId(), tarifa.getHabitacion().getId(), tarifa.getFechaInicio(),
            tarifa.getFechaFin(), tarifa.getPrecioNoche(), tarifa.getDescripcion());
    }
}