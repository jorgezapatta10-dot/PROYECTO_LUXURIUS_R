package com.luxuriusroom.repository;

import com.luxuriusroom.model.Alojamiento;
import com.luxuriusroom.model.Resena;
import com.luxuriusroom.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    Optional<Resena> findByReserva(Reserva reserva);
    boolean existsByReserva(Reserva reserva);
    List<Resena> findByAlojamientoAndEstadoOrderByCreadoEnDesc(Alojamiento alojamiento, com.luxuriusroom.model.EstadoResena estado);
    List<Resena> findByReportadaTrueOrderByCreadoEnDesc();

    @Query("select r from Resena r where r.alojamiento.anfitrion.id = :anfitrionId order by r.creadoEn desc")
    List<Resena> findByAnfitrionId(@Param("anfitrionId") Long anfitrionId);
}
