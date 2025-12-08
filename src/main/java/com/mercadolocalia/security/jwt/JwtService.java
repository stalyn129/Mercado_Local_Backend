package com.mercadolocalia.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercadolocalia.entities.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationMs;

    // ============================================================
    // 🔐 OBTENER CLAVE DE FIRMA
    // ============================================================
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ============================================================
    // 🔐 GENERAR TOKEN CON CLAIMS PERSONALIZADOS
    // ============================================================
    public String generarToken(Usuario usuario) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().getNombreRol());
        claims.put("idUsuario", usuario.getIdUsuario());

        return crearToken(claims, usuario.getCorreo());
    }

    private String crearToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ============================================================
    // 🔍 OBTENER CLAIMS Y CAMPOS
    // ============================================================
    private Claims obtenerTodosLosClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String obtenerCorreoDesdeToken(String token) {
        return obtenerTodosLosClaims(token).getSubject();
    }

    public <T> T extraerValor(String token, String clave, Class<T> tipo) {
        Object value = obtenerTodosLosClaims(token).get(clave);
        return tipo.cast(value);
    }

    // ============================================================
    // 🔐 VALIDAR TOKEN
    // ============================================================
    public boolean validarToken(String token, Usuario usuario) {
        final String correo = obtenerCorreoDesdeToken(token);
        return correo.equals(usuario.getCorreo()) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {
        return obtenerTodosLosClaims(token)
                .getExpiration()
                .before(new Date());
    }
}
