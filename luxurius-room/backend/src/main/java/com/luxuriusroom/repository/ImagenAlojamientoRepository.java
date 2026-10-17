package com.luxuriusroom.repository;

import com.luxuriusroom.model.Alojamiento;
import com.luxuriusroom.model.ImagenAlojamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenAlojamientoRepository extends JpaRepository<ImagenAlojamiento, Long> {
    List<ImagenAlojamiento> findByAlojamientoOrderByOrdenAsc(Alojamiento alojamiento);
}
