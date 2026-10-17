package com.luxuriusroom.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas", indexes = {
    @Index(name = "idx_reservas_habitacion_fechas", columnList = "habitacion_id, fecha_entrada, fecha_salida"),
    @Index(name = "idx_reservas_usuario", columnList = "usuario_id")
})
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(nullable = false)
    private Integer adultos;

    @Column(nullable = false)
    private Integer ninos;

    @Column(nullable = false)
    private Integer unidades;

    @Column(nullable = false)
    private Integer noches;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "cargos_adicionales", nullable = false, precision = 10, scale = 2)
    private BigDecimal cargosAdicionales;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reserva", nullable = false)
    private EstadoReserva estadoReserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE_EN_SITIO;

    @Column(name = "acepto_condiciones", nullable = false)
    private Boolean aceptoCondiciones;

    @Column(name = "cancelacion_tardia", nullable = false)
    private Boolean cancelacionTardia = false;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public Reserva() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
    public Integer getAdultos() { return adultos; }
    public void setAdultos(Integer adultos) { this.adultos = adultos; }
    public Integer getNinos() { return ninos; }
    public void setNinos(Integer ninos) { this.ninos = ninos; }
    public Integer getUnidades() { return unidades; }
    public void setUnidades(Integer unidades) { this.unidades = unidades; }
    public Integer getNoches() { return noches; }
    public void setNoches(Integer noches) { this.noches = noches; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getCargosAdicionales() { return cargosAdicionales; }
    public void setCargosAdicionales(BigDecimal cargosAdicionales) { this.cargosAdicionales = cargosAdicionales; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public EstadoReserva getEstadoReserva() { return estadoReserva; }
    public void setEstadoReserva(EstadoReserva estadoReserva) { this.estadoReserva = estadoReserva; }
    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }
    public Boolean getAceptoCondiciones() { return aceptoCondiciones; }
    public void setAceptoCondiciones(Boolean aceptoCondiciones) { this.aceptoCondiciones = aceptoCondiciones; }
    public Boolean getCancelacionTardia() { return cancelacionTardia; }
    public void setCancelacionTardia(Boolean cancelacionTardia) { this.cancelacionTardia = cancelacionTardia; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
