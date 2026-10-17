package com.luxuriusroom.dto;

import com.luxuriusroom.model.EstadoAlojamiento;
import com.luxuriusroom.model.PoliticaCancelacion;
import com.luxuriusroom.model.TipoAlojamiento;

import java.math.BigDecimal;
import java.util.List;

public record AlojamientoAnfitrionResponse(
    Long id,
    String nombre,
    TipoAlojamiento tipo,
    String descripcion,
    String ciudad,
    String pais,
    String direccion,
    String reglas,
    PoliticaCancelacion politicaCancelacion,
    EstadoAlojamiento estado,
    BigDecimal calificacionPromedio,
    List<HabitacionAnfitrionResponse> habitaciones,
    List<String> imagenes,
    List<String> servicios
) {
}