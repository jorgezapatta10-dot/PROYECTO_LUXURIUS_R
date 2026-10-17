package com.luxuriusroom.dto;

import com.luxuriusroom.model.BloqueoDisponibilidad;

import java.time.LocalDate;

public record BloqueoResponse(Long id, Long habitacionId, LocalDate fechaInicio, LocalDate fechaFin,
                              Integer unidadesBloqueadas, String motivo) {
    public static BloqueoResponse desde(BloqueoDisponibilidad bloqueo) {
        return new BloqueoResponse(bloqueo.getId(), bloqueo.getHabitacion().getId(), bloqueo.getFechaInicio(),
            bloqueo.getFechaFin(), bloqueo.getUnidadesBloqueadas(), bloqueo.getMotivo());
    }
}