package com.luxuriusroom.config;

import com.luxuriusroom.model.RolUsuario;
import com.luxuriusroom.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void tokenHs256IncluyeIdentidadRolYExpiracion() throws Exception {
        JwtService service = new JwtService(SECRET, 120);
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setEmail("anfitrion@example.test");
        usuario.setRol(RolUsuario.ANFITRION);

        var claims = service.parseToken(service.generateToken(usuario));

        assertEquals("42", claims.getSubject());
        assertEquals("anfitrion@example.test", claims.getStringClaim("email"));
        assertEquals("ANFITRION", claims.getStringClaim("rol"));
        assertTrue(claims.getExpirationTime().after(java.util.Date.from(Instant.now())));
    }

    @Test
    void conversorDeJwtAsignaAutoridadDelRol() {
        Instant now = Instant.now();
        Jwt token = Jwt.withTokenValue("test-token")
            .header("alg", "HS256")
            .subject("42")
            .claim("rol", "ANFITRION")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(120))
            .build();

        var authentication = new SecurityConfig().jwtAuthenticationConverter().convert(token);

        assertEquals("42", authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ANFITRION")));
    }
}