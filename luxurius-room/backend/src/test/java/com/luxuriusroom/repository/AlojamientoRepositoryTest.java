package com.luxuriusroom.repository;

import com.luxuriusroom.model.EstadoAlojamiento;
import com.luxuriusroom.model.TipoAlojamiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Sql(statements = {
    "CREATE TABLE alojamientos (id BIGINT PRIMARY KEY, anfitrion_id BIGINT, nombre VARCHAR(255), tipo VARCHAR(255), descripcion CLOB, ciudad VARCHAR(255), pais VARCHAR(255), direccion VARCHAR(255), reglas CLOB, politica_cancelacion VARCHAR(255), estado VARCHAR(255), calificacion_promedio DECIMAL(2,1), creado_en TIMESTAMP)",
    "CREATE TABLE habitaciones (id BIGINT PRIMARY KEY, alojamiento_id BIGINT, nombre VARCHAR(255), descripcion CLOB, capacidad_maxima INT, camas VARCHAR(255), unidades_totales INT, precio_base_noche DECIMAL(10,2), cargo_limpieza DECIMAL(10,2), activa TINYINT)",
    "CREATE TABLE servicios (id BIGINT PRIMARY KEY, codigo VARCHAR(255), nombre VARCHAR(255))",
    "CREATE TABLE alojamiento_servicio (alojamiento_id BIGINT, servicio_id BIGINT, PRIMARY KEY (alojamiento_id, servicio_id))"
})
class AlojamientoRepositoryTest {
    @Autowired
    private AlojamientoRepository alojamientoRepository;

    @Test
    void buscarPublicosAceptaFiltrosCombinados() {
        var result = alojamientoRepository.buscarPublicos(
            EstadoAlojamiento.ACTIVO,
            "Bogota",
            TipoAlojamiento.CASA,
            BigDecimal.valueOf(100_000),
            BigDecimal.valueOf(800_000),
            BigDecimal.valueOf(4),
            2,
            1,
            1,
            0,
            1,
            PageRequest.of(0, 10));

        assertNotNull(result);
    }
}