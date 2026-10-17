package com.luxuriusroom.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "alojamiento_servicio")
@IdClass(AlojamientoServicioId.class)
public class AlojamientoServicio {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alojamiento_id", nullable = false)
    private Alojamiento alojamiento;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    public AlojamientoServicio() {
    }

    public Alojamiento getAlojamiento() { return alojamiento; }
    public void setAlojamiento(Alojamiento alojamiento) { this.alojamiento = alojamiento; }
    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }
}

class AlojamientoServicioId implements Serializable {
    private Long alojamiento;
    private Long servicio;

    public AlojamientoServicioId() {}

    public Long getAlojamiento() { return alojamiento; }
    public void setAlojamiento(Long alojamiento) { this.alojamiento = alojamiento; }
    public Long getServicio() { return servicio; }
    public void setServicio(Long servicio) { this.servicio = servicio; }

    @Override
    public boolean equals(Object o) { return o instanceof AlojamientoServicioId id && java.util.Objects.equals(alojamiento, id.alojamiento) && java.util.Objects.equals(servicio, id.servicio); }
    @Override
    public int hashCode() { return java.util.Objects.hash(alojamiento, servicio); }
}
