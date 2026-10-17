package com.luxuriusroom.repository;

import com.luxuriusroom.model.Habitacion;
import com.luxuriusroom.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    List<Tarifa> findByHabitacion(Habitacion habitacion);
}
