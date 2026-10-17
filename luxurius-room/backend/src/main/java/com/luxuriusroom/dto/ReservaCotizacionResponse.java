package com.luxuriusroom.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReservaCotizacionResponse(
    Integer noches,
    List<NocheCotizada> detalleNoches,
    BigDecimal subtotal,
    BigDecimal cargosAdicionales,
    BigDecimal total
) {
    public record NocheCotizada(LocalDate fecha, BigDecimal precioPorUnidad, BigDecimal importe) {
    }
}