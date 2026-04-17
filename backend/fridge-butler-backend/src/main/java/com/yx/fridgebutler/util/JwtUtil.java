package com.yx.fridgebutler.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public final class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.remember-me-expiration:2592000000}")
    private Long rememberMeExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Long userId, String roleName) {
        return generateToken(username, userId, roleName, false);
    }

    public String generateToken(String username, Long userId, String roleName, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleName", roleName);
        claims.put("rememberMe", rememberMe);

        Date now = new Date();
        Long expireTime = rememberMe ? rememberMeExpiration : expiration;
        Date expiryDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return (Long) claims.get("userId", Long.class);
    }

    public String extractRoleName(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("roleName", String.class);
    }

    public boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean extractRememberMe(String token) {
        Claims claims = extractAllClaims(token);
        return (Boolean) claims.get("rememberMe", Boolean.class);
    }
}
