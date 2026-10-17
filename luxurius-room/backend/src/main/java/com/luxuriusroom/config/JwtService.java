package com.luxuriusroom.config;

import com.luxuriusroom.model.Usuario;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final String secret;
    private final long expiracionMinutos;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.secret = secret;
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generateToken(Usuario usuario) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(expiracionMinutos * 60)))
                .jwtID(UUID.randomUUID().toString())
                .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo firmar el token JWT", e);
        }
    }

    public JWTClaimsSet parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(secret.getBytes(StandardCharsets.UTF_8)))) {
                throw new SecurityException("Token inválido");
            }
            if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                throw new SecurityException("Token expirado");
            }
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException | JOSEException e) {
            throw new SecurityException("Token inválido", e);
        }
    }
}
