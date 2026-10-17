package com.luxuriusroom.repository;

import com.luxuriusroom.model.Alojamiento;
import com.luxuriusroom.model.Habitacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    List<Habitacion> findByAlojamiento(Alojamiento alojamiento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Habitacion h where h.id = :id")
    Optional<Habitacion> findByIdForUpdate(Long id);
}
