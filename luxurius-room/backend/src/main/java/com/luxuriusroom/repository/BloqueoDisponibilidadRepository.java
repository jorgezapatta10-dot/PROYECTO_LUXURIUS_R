package com.luxuriusroom.repository;

import com.luxuriusroom.model.BloqueoDisponibilidad;
import com.luxuriusroom.model.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloqueoDisponibilidadRepository extends JpaRepository<BloqueoDisponibilidad, Long> {
    List<BloqueoDisponibilidad> findByHabitacion(Habitacion habitacion);
}
