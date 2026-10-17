package com.luxuriusroom.dto;

import com.luxuriusroom.model.Reserva;

public record ReservaAdminResponse(ReservaResponse reserva, String nombreHuesped, String emailHuesped) {
    public static ReservaAdminResponse desde(Reserva reserva) {
        return new ReservaAdminResponse(ReservaResponse.desde(reserva), reserva.getUsuario().getNombre(),
            reserva.getUsuario().getEmail());
    }
}