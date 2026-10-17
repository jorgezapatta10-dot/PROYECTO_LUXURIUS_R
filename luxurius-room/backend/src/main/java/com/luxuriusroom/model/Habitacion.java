package com.luxuriusroom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Entity
@Table(name = "habitaciones")
public class Habitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alojamiento_id", nullable = false)
    private Alojamiento alojamiento;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;

    @Column(length = 255)
    private String camas;

    @Column(name = "unidades_totales", nullable = false)
    private Integer unidadesTotales;

    @Column(name = "precio_base_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBaseNoche;

    @Column(name = "cargo_limpieza", precision = 10, scale = 2)
    private BigDecimal cargoLimpieza = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean activa = true;

    public Habitacion() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alojamiento getAlojamiento() { return alojamiento; }
    public void setAlojamiento(Alojamiento alojamiento) { this.alojamiento = alojamiento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public String getCamas() { return camas; }
    public void setCamas(String camas) { this.camas = camas; }
    public Integer getUnidadesTotales() { return unidadesTotales; }
    public void setUnidadesTotales(Integer unidadesTotales) { this.unidadesTotales = unidadesTotales; }
    public BigDecimal getPrecioBaseNoche() { return precioBaseNoche; }
    public void setPrecioBaseNoche(BigDecimal precioBaseNoche) { this.precioBaseNoche = precioBaseNoche; }
    public BigDecimal getCargoLimpieza() { return cargoLimpieza; }
    public void setCargoLimpieza(BigDecimal cargoLimpieza) { this.cargoLimpieza = cargoLimpieza; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
