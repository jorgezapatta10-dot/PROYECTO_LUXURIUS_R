package com.luxuriusroom.repository;

import com.luxuriusroom.model.Alojamiento;
import com.luxuriusroom.model.AlojamientoServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlojamientoServicioRepository extends JpaRepository<AlojamientoServicio, Long> {
    List<AlojamientoServicio> findByAlojamiento(Alojamiento alojamiento);
    void deleteByAlojamiento(Alojamiento alojamiento);
}
