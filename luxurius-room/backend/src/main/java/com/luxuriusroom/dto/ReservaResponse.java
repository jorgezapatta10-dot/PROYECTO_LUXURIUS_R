package com.luxuriusroom.dto;

import com.luxuriusroom.model.EstadoPago;
import com.luxuriusroom.model.EstadoReserva;
import com.luxuriusroom.model.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservaResponse(
    Long id,
    String codigo,
    String alojamiento,
    String ciudad,
    String habitacion,
    LocalDate fechaEntrada,
    LocalDate fechaSalida,
    Integer adultos,
    Integer ninos,
    Integer unidades,
    Integer noches,
    BigDecimal subtotal,
    BigDecimal cargosAdicionales,
    BigDecimal total,
    EstadoReserva estadoReserva,
    EstadoPago estadoPago,
    Boolean cancelacionTardia,
    Boolean resenaCreada
) {
    public static ReservaResponse desde(Reserva reserva) {
        return desde(reserva, false);
    }

    public static ReservaResponse desde(Reserva reserva, boolean resenaCreada) {
        var alojamiento = reserva.getHabitacion().getAlojamiento();
        return new ReservaResponse(
            reserva.getId(), reserva.getCodigo(), alojamiento.getNombre(), alojamiento.getCiudad(),
            reserva.getHabitacion().getNombre(), reserva.getFechaEntrada(), reserva.getFechaSalida(),
            reserva.getAdultos(), reserva.getNinos(), reserva.getUnidades(), reserva.getNoches(),
            reserva.getSubtotal(), reserva.getCargosAdicionales(), reserva.getTotal(),
            reserva.getEstadoReserva(), reserva.getEstadoPago(), reserva.getCancelacionTardia(), resenaCreada);
    }
}