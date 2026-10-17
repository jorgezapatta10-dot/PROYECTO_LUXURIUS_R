package com.luxuriusroom.repository;

import com.luxuriusroom.model.EstadoReserva;
import com.luxuriusroom.model.Habitacion;
import com.luxuriusroom.model.Reserva;
import com.luxuriusroom.model.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    Optional<Reserva> findByCodigo(String codigo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.codigo = :codigo")
    Optional<Reserva> findByCodigoForUpdate(@Param("codigo") String codigo);
    List<Reserva> findByUsuario(Usuario usuario);
    List<Reserva> findByHabitacion(Habitacion habitacion);
    List<Reserva> findByEstadoReserva(EstadoReserva estadoReserva);
    long countByCreadoEnGreaterThanEqualAndCreadoEnLessThan(LocalDateTime desde, LocalDateTime hasta);

    @Query("select r from Reserva r where r.habitacion.alojamiento.anfitrion.id = :anfitrionId order by r.creadoEn desc")
    List<Reserva> findByAnfitrionId(@Param("anfitrionId") Long anfitrionId);
}
