package com.luxuriusroom.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "bloqueos_disponibilidad")
public class BloqueoDisponibilidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "unidades_bloqueadas", nullable = false)
    private Integer unidadesBloqueadas;

    @Column(length = 255)
    private String motivo;

    public BloqueoDisponibilidad() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Integer getUnidadesBloqueadas() { return unidadesBloqueadas; }
    public void setUnidadesBloqueadas(Integer unidadesBloqueadas) { this.unidadesBloqueadas = unidadesBloqueadas; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
