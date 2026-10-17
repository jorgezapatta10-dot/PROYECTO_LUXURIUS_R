package com.luxuriusroom.config;

import com.luxuriusroom.controller.AuthController;
import com.luxuriusroom.dto.AuthResponse;
import com.luxuriusroom.dto.LoginRequest;
import com.luxuriusroom.model.RolUsuario;
import com.luxuriusroom.model.Usuario;
import com.luxuriusroom.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = AuthController.class, properties = {
    "app.jwt.secret=0123456789abcdef0123456789abcdef",
    "app.cors.origenes=http://localhost:5500"
})
@Import(SecurityConfig.class)
class SecurityConfigTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Autowired private MockMvc mockMvc;
    @MockBean private AuthService authService;

    @Test
    void rutaProtegidaSinTokenDevuelve401Json() throws Exception {
        mockMvc.perform(get("/api/anfitrion/alojamientos"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.estado").value(401))
            .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void soloGetDeAlojamientosEsPublico() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/alojamientos"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    void registroResponde201SinSerializarElHash() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(21L);
        usuario.setNombre("Huesped");
        usuario.setApellido("Prueba");
        usuario.setEmail("huesped@example.test");
        usuario.setPasswordHash("hash-privado");
        usuario.setRol(RolUsuario.HUESPED);
        when(authService.registrar(any())).thenReturn(usuario);

        mockMvc.perform(post("/api/auth/registro")
                .contentType("application/json")
                .content("{\"nombre\":\"Huesped\",\"apellido\":\"Prueba\",\"email\":\"huesped@example.test\",\"password\":\"Clave1234\",\"rol\":\"HUESPED\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("huesped@example.test"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void loginRespondeConTokenYUsuarioBasico() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(
            new AuthResponse("jwt-prueba", new AuthResponse.UsuarioBasico(21L, "Huesped", "huesped@example.test", RolUsuario.HUESPED)));

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"huesped@example.test\",\"password\":\"Clave1234\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-prueba"))
            .andExpect(jsonPath("$.usuario.rol").value("HUESPED"));
    }

    @Test
    void rolHuespedNoPuedeEntrarAlAreaDeAnfitrion() throws Exception {
        mockMvc.perform(get("/api/anfitrion/alojamientos")
                .header("Authorization", "Bearer " + token(RolUsuario.HUESPED)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.estado").value(403))
            .andExpect(jsonPath("$.error").value("Acceso prohibido"));
    }

    @Test
    void rolAnfitrionNoPuedeEntrarAlAreaDeAdministrador() throws Exception {
        mockMvc.perform(get("/api/admin/estadisticas")
                .header("Authorization", "Bearer " + token(RolUsuario.ANFITRION)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.estado").value(403));
    }

    private String token(RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(16L);
        usuario.setEmail("usuario@example.test");
        usuario.setRol(rol);
        return new JwtService(SECRET, 15).generateToken(usuario);
    }
}