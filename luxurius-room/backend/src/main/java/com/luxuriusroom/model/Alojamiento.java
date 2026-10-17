package com.luxuriusroom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alojamientos", indexes = {
    @Index(name = "idx_alojamientos_ciudad_estado", columnList = "ciudad, estado")
})
public class Alojamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anfitrion_id", nullable = false)
    private Usuario anfitrion;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlojamiento tipo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank
    @Column(nullable = false)
    private String ciudad;

    @NotBlank
    @Column(nullable = false)
    private String pais;

    @NotBlank
    @Column(nullable = false)
    private String direccion;

    @Column(columnDefinition = "TEXT")
    private String reglas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PoliticaCancelacion politicaCancelacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAlojamiento estado;

    @Column(name = "calificacion_promedio", precision = 2, scale = 1)
    private BigDecimal calificacionPromedio;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public Alojamiento() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getAnfitrion() { return anfitrion; }
    public void setAnfitrion(Usuario anfitrion) { this.anfitrion = anfitrion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoAlojamiento getTipo() { return tipo; }
    public void setTipo(TipoAlojamiento tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getReglas() { return reglas; }
    public void setReglas(String reglas) { this.reglas = reglas; }
    public PoliticaCancelacion getPoliticaCancelacion() { return politicaCancelacion; }
    public void setPoliticaCancelacion(PoliticaCancelacion politicaCancelacion) { this.politicaCancelacion = politicaCancelacion; }
    public EstadoAlojamiento getEstado() { return estado; }
    public void setEstado(EstadoAlojamiento estado) { this.estado = estado; }
    public BigDecimal getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(BigDecimal calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
