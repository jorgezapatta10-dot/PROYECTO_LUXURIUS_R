package com.luxuriusroom.repository;

import com.luxuriusroom.model.Alojamiento;
import com.luxuriusroom.model.EstadoAlojamiento;
import com.luxuriusroom.model.TipoAlojamiento;
import com.luxuriusroom.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AlojamientoRepository extends JpaRepository<Alojamiento, Long> {
    Page<Alojamiento> findByEstado(EstadoAlojamiento estado, Pageable pageable);
    Page<Alojamiento> findByEstadoAndCiudadIgnoreCase(EstadoAlojamiento estado, String ciudad, Pageable pageable);
        @Query(value = """
                select a from Alojamiento a
                where a.estado = :estado
                    and (:ciudad is null or lower(a.ciudad) = lower(:ciudad))
                    and (:tipo is null or a.tipo = :tipo)
                    and (:calificacionMinima is null or a.calificacionPromedio >= :calificacionMinima)
                    and (
                        (:precioMinimo is null and :precioMaximo is null and :capacidad is null)
                        or exists (
                            select h.id from Habitacion h
                            where h.alojamiento = a and h.activa = true
                                and (:precioMinimo is null or h.precioBaseNoche >= :precioMinimo)
                                and (:precioMaximo is null or h.precioBaseNoche <= :precioMaximo)
                                and (:capacidad is null or h.capacidadMaxima >= :capacidad)
                        )
                    )
                      and (:wifi = 0 or exists (
                        select rel.alojamiento from AlojamientoServicio rel
                        where rel.alojamiento = a and rel.servicio.codigo = 'WIFI'
                    ))
                      and (:estacionamiento = 0 or exists (
                        select rel.alojamiento from AlojamientoServicio rel
                        where rel.alojamiento = a and rel.servicio.codigo = 'ESTACIONAMIENTO'
                    ))
                      and (:desayuno = 0 or exists (
                        select rel.alojamiento from AlojamientoServicio rel
                        where rel.alojamiento = a and rel.servicio.codigo = 'DESAYUNO'
                    ))
                      and (:aireAcondicionado = 0 or exists (
                        select rel.alojamiento from AlojamientoServicio rel
                        where rel.alojamiento = a and rel.servicio.codigo = 'AIRE_ACONDICIONADO'
                    ))
                """,
                countQuery = """
                select count(a) from Alojamiento a
                where a.estado = :estado
                    and (:ciudad is null or lower(a.ciudad) = lower(:ciudad))
                    and (:tipo is null or a.tipo = :tipo)
                    and (:calificacionMinima is null or a.calificacionPromedio >= :calificacionMinima)
                    and (
                        (:precioMinimo is null and :precioMaximo is null and :capacidad is null)
                        or exists (
                            select h.id from Habitacion h
                            where h.alojamiento = a and h.activa = true
                                and (:precioMinimo is null or h.precioBaseNoche >= :precioMinimo)
                                and (:precioMaximo is null or h.precioBaseNoche <= :precioMaximo)
                                and (:capacidad is null or h.capacidadMaxima >= :capacidad)
                        )
                    )
                      and (:wifi = 0 or exists (
                        select rel.alojamiento from AlojamientoServicio rel
                        where rel.alojamiento = a and rel.servicio.codigo = 'WIFI'
                    ))
                      and (:estacionamiento = 0 or exists (
                        select rel.alojamiento from AlojamientoServicio rel
                        where rel.alojamiento = a and rel.servicio.codigo = 'ESTACIONAMIENTO'
                    ))
                      and (:desayuno = 0 or exists (
                        select rel.alojamiento from AlojamientoServicio rel
                        where rel.alojamiento = a and rel.servicio.codigo = 'DESAYUNO'
                    ))
                      and (:aireAcondicionado = 0 or exists (
                        select rel.alojamiento from AlojamientoServicio rel
                        where rel.alojamiento = a and rel.servicio.codigo = 'AIRE_ACONDICIONADO'
                    ))
                """)
        Page<Alojamiento> buscarPublicos(
                @Param("estado") EstadoAlojamiento estado,
                @Param("ciudad") String ciudad,
                @Param("tipo") TipoAlojamiento tipo,
                @Param("precioMinimo") BigDecimal precioMinimo,
                @Param("precioMaximo") BigDecimal precioMaximo,
                @Param("calificacionMinima") BigDecimal calificacionMinima,
                @Param("capacidad") Integer capacidad,
                @Param("wifi") int wifi,
                @Param("estacionamiento") int estacionamiento,
                @Param("desayuno") int desayuno,
                @Param("aireAcondicionado") int aireAcondicionado,
                Pageable pageable);
    List<Alojamiento> findByAnfitrionAndEstado(Usuario anfitrion, EstadoAlojamiento estado);
    List<Alojamiento> findByAnfitrion(Usuario anfitrion);
}
