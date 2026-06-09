package com.yx.fridgebutler.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public final class JwtUtil {

    /** JWT 签名密钥（从配置文件读取） */
    @Value("${jwt.secret}")
    private String secret;

    /** 普通登录 Token 过期时间（毫秒） */
    @Getter
    @Value("${jwt.expiration}")
    private Long expiration;

    /** 记住我登录 Token 过期时间（毫秒，默认 30 天） */
    @Getter
    @Value("${jwt.remember-me-expiration:2592000000}")
    private Long rememberMeExpiration;

    /** 解析后的 HMAC SHA 签名密钥 */
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET 长度必须至少 32 字节（推荐 64 字节以上），当前长度: " + keyBytes.length
            );
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取 JWT 签名密钥
     *
     * @return HMAC SHA 密钥对象
     */
    private SecretKey getSigningKey() {
        return this.key;
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
        return generateToken(username, userId, roleName, false, true);
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
        return generateToken(username, userId, roleName, rememberMe, true);
    }

    /**
     * 生成 JWT Token（完整参数）
     *
     * @param username   用户名
     * @param userId     用户ID
     * @param roleName   角色名称
     * @param rememberMe 是否为记住我登录
     * @param activated  用户是否已激活
     * @return 生成的 JWT Token 字符串
     */
    public String generateToken(String username, Long userId, String roleName, boolean rememberMe, boolean activated) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleName", roleName);
        claims.put("rememberMe", rememberMe);
        claims.put("activated", activated);

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
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中提取角色名称
     *
     * @param token JWT Token 字符串
     * @return Token 中存储的角色名称
     */
    public String extractRoleName(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roleName", String.class);
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
            log.error("JWT Token 验证异常", e);
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
        return claims.get("rememberMe", Boolean.class);
    }

    /**
     * 从 Token 中提取激活状态
     *
     * @param token JWT Token 字符串
     * @return 用户是否已激活，是返回 true，否则返回 false
     */
    public Boolean extractActivated(String token) {
        Claims claims = extractAllClaims(token);
        Boolean activated = claims.get("activated", Boolean.class);
        return activated != null ? activated : true;
    }
}
