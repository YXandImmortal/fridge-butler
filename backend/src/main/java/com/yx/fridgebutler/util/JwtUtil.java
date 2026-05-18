package com.yx.fridgebutler.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * <p>
 * 提供 JWT Token 的生成、解析和验证功能。
 * 支持普通登录和记住我两种过期时间策略。
 * </p>
 */
@Component
public final class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Getter
    @Value("${jwt.expiration}")
    private Long expiration;

    @Getter
    @Value("${jwt.remember-me-expiration:2592000000}")
    private Long rememberMeExpiration;

    /**
     * 获取 JWT 签名密钥
     *
     * @return HMAC SHA 密钥对象
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token（普通登录，使用默认过期时间）
     *
     * @param username 用户名
     * @param userId   用户ID
     * @param roleName 角色名称
     * @return 生成的 JWT Token 字符串
     */
    public String generateToken(String username, Long userId, String roleName) {
        return generateToken(username, userId, roleName, false);
    }

    /**
     * 生成 JWT Token
     *
     * @param username   用户名
     * @param userId     用户ID
     * @param roleName   角色名称
     * @param rememberMe 是否为记住我登录，true 则使用更长的过期时间
     * @return 生成的 JWT Token 字符串
     */
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

    /**
     * 解析 Token 获取所有 Claims
     *
     * @param token JWT Token 字符串
     * @return Token 中包含的所有声明信息
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取用户名
     *
     * @param token JWT Token 字符串
     * @return Token 中存储的用户名
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT Token 字符串
     * @return Token 中存储的用户ID
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return (Long) claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中提取角色名称
     *
     * @param token JWT Token 字符串
     * @return Token 中存储的角色名称
     */
    public String extractRoleName(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("roleName", String.class);
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT Token 字符串
     * @return 已过期返回 true，未过期返回 false
     */
    public boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * 验证 Token 的有效性
     *
     * @param token JWT Token 字符串
     * @return 验证通过返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中提取记住我标识
     *
     * @param token JWT Token 字符串
     * @return 是否为记住我登录，是返回 true，否则返回 false
     */
    public Boolean extractRememberMe(String token) {
        Claims claims = extractAllClaims(token);
        return (Boolean) claims.get("rememberMe", Boolean.class);
    }
}
