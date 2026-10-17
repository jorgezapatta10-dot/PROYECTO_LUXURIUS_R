package com.luxuriusroom.dto;

import com.luxuriusroom.model.EstadoAlojamiento;
import com.luxuriusroom.model.TipoAlojamiento;

public record AlojamientoAdminResponse(Long id, String nombre, TipoAlojamiento tipo, String ciudad, String pais,
                                       String emailAnfitrion, EstadoAlojamiento estado) {
}