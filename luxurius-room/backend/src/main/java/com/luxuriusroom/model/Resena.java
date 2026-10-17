package com.luxuriusroom.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resenas", uniqueConstraints = {
    @UniqueConstraint(name = "uk_resena_reserva", columnNames = "reserva_id")
})
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alojamiento_id", nullable = false)
    private Alojamiento alojamiento;

    @Column(nullable = false)
    private Integer calificacion;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoResena estado = EstadoResena.VISIBLE;

    @Column(nullable = false)
    private Boolean reportada = false;

    @Column(name = "motivo_reporte")
    private String motivoReporte;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public Resena() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Alojamiento getAlojamiento() { return alojamiento; }
    public void setAlojamiento(Alojamiento alojamiento) { this.alojamiento = alojamiento; }
    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public EstadoResena getEstado() { return estado; }
    public void setEstado(EstadoResena estado) { this.estado = estado; }
    public Boolean getReportada() { return reportada; }
    public void setReportada(Boolean reportada) { this.reportada = reportada; }
    public String getMotivoReporte() { return motivoReporte; }
    public void setMotivoReporte(String motivoReporte) { this.motivoReporte = motivoReporte; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
