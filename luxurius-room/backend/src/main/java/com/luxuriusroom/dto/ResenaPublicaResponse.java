package com.luxuriusroom.dto;

import com.luxuriusroom.model.Resena;

import java.time.LocalDateTime;

public record ResenaPublicaResponse(Long id, String usuario, Integer calificacion, String comentario,
                                    LocalDateTime creadoEn) {
    public static ResenaPublicaResponse desde(Resena resena) {
        return new ResenaPublicaResponse(resena.getId(), resena.getUsuario().getNombre(),
            resena.getCalificacion(), resena.getComentario(), resena.getCreadoEn());
    }
}