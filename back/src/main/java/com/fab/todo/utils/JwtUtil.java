package com.fab.todo.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 工具类
 * 提供 token 生成、解析、校验功能
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret 长度不足，至少需要 256 bits (32 bytes)");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成 token */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 解析 token，返回 Claims */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 token 中提取用户 ID */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /** 从 token 中提取用户名 */
    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    /** 从 token 中提取角色 */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /** 校验 token 是否有效 */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 从 token 中提取签发时间（毫秒时间戳） */
    public long getIssuedAt(String token) {
        Date issuedAt = parseToken(token).getIssuedAt();
        return issuedAt != null ? issuedAt.getTime() : 0L;
    }
}
