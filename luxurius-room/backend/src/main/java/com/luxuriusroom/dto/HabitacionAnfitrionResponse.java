package com.luxuriusroom.dto;

import com.luxuriusroom.model.Habitacion;

import java.math.BigDecimal;
import java.util.List;

public record HabitacionAnfitrionResponse(Long id, String nombre, String descripcion, Integer capacidadMaxima,
                                          String camas, Integer unidadesTotales, BigDecimal precioBaseNoche,
                                          BigDecimal cargoLimpieza, Boolean activa, List<TarifaResponse> tarifas,
                                          List<BloqueoResponse> bloqueos) {
    public static HabitacionAnfitrionResponse desde(Habitacion habitacion, List<TarifaResponse> tarifas,
                                                    List<BloqueoResponse> bloqueos) {
        return new HabitacionAnfitrionResponse(habitacion.getId(), habitacion.getNombre(), habitacion.getDescripcion(),
            habitacion.getCapacidadMaxima(), habitacion.getCamas(), habitacion.getUnidadesTotales(),
            habitacion.getPrecioBaseNoche(), habitacion.getCargoLimpieza(), habitacion.getActiva(), tarifas, bloqueos);
    }
}