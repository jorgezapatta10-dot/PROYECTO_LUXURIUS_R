package com.luxuriusroom.service;

import com.luxuriusroom.model.*;
import com.luxuriusroom.repository.AlojamientoRepository;
import com.luxuriusroom.repository.ReservaRepository;
import com.luxuriusroom.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AlojamientoRepository alojamientoRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private AuditoriaService auditoriaService;
    @InjectMocks private AdminService adminService;

    @Test
    void estadisticasCuentaReservasCreadasEnElMesActual() {
        for (EstadoAlojamiento estado : EstadoAlojamiento.values()) {
            when(alojamientoRepository.findByEstado(org.mockito.ArgumentMatchers.eq(estado), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), Pageable.unpaged(), 2));
        }
        for (EstadoReserva estado : EstadoReserva.values()) {
            when(reservaRepository.findByEstadoReserva(estado)).thenReturn(List.of());
        }
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        when(reservaRepository.countByCreadoEnGreaterThanEqualAndCreadoEnLessThan(
            inicio.atStartOfDay(), inicio.plusMonths(1).atStartOfDay())).thenReturn(6L);

        Map<String, Object> result = adminService.estadisticas();

        assertEquals(6L, result.get("reservasDelMes"));
        assertEquals(2L, ((Map<?, ?>) result.get("alojamientosPorEstado")).get("ACTIVO"));
    }

    @Test
    void cambiosAdministrativosEscribenAuditoriaYDevuelvenDto() {
        Usuario admin = new Usuario();
        admin.setId(90L);
        admin.setRol(RolUsuario.ADMINISTRADOR);
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setEmail("persona@example.test");
        usuario.setRol(RolUsuario.HUESPED);
        usuario.setActivo(true);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        var result = adminService.cambiarEstadoUsuario(5L, false, admin);

        assertEquals(false, result.activo());
        verify(auditoriaService).guardar(admin, "DESACTIVAR_USUARIO", "usuario", 5L, "persona@example.test");
    }

    @Test
    void moderarAlojamientoDevuelveDtoYRegistraLaAccion() {
        Usuario admin = new Usuario();
        admin.setId(90L);
        Usuario anfitrion = new Usuario();
        anfitrion.setEmail("anfitrion@example.test");
        Alojamiento alojamiento = new Alojamiento();
        alojamiento.setId(7L);
        alojamiento.setNombre("Casa central");
        alojamiento.setTipo(TipoAlojamiento.CASA);
        alojamiento.setCiudad("Bogota");
        alojamiento.setPais("Colombia");
        alojamiento.setAnfitrion(anfitrion);
        when(alojamientoRepository.findById(7L)).thenReturn(Optional.of(alojamiento));
        when(alojamientoRepository.save(alojamiento)).thenReturn(alojamiento);

        var result = adminService.cambiarEstadoAlojamiento(7L, EstadoAlojamiento.ACTIVO, admin);

        assertEquals(EstadoAlojamiento.ACTIVO, result.estado());
        assertEquals("anfitrion@example.test", result.emailAnfitrion());
        verify(auditoriaService).guardar(admin, "APROBAR_ALOJAMIENTO", "alojamiento", 7L, "ACTIVO");
    }
}