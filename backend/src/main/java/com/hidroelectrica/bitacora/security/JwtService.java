package com.hidroelectrica.bitacora.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
        @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret,
        @Value("${jwt.expiration-ms:28800000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .claims(extraClaims)
            .subject(username)
            .issuedAt(new Date(now))
            .expiration(new Date(now + expirationMs))
            .signWith(key)
            .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
