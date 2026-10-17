package com.luxuriusroom.dto;

import com.luxuriusroom.model.EstadoResena;
import com.luxuriusroom.model.Resena;

import java.time.LocalDateTime;

public record ResenaResponse(Long id, Long alojamientoId, String alojamiento, String codigoReserva,
                             String usuario, Integer calificacion, String comentario, EstadoResena estado,
                             Boolean reportada, String motivoReporte, LocalDateTime creadoEn) {
    public static ResenaResponse desde(Resena resena) {
        return new ResenaResponse(resena.getId(), resena.getAlojamiento().getId(), resena.getAlojamiento().getNombre(),
            resena.getReserva().getCodigo(), resena.getUsuario().getNombre(), resena.getCalificacion(),
            resena.getComentario(), resena.getEstado(), resena.getReportada(), resena.getMotivoReporte(),
            resena.getCreadoEn());
    }
}