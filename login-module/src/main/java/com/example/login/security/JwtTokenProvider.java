package com.example.login.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT双Token管理
 * F5 会话管理 - Access Token + Refresh Token
 */
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.access-secret}")
    private String accessSecret;
    
    @Value("${jwt.refresh-secret}")
    private String refreshSecret;
    
    @Value("${jwt.access-expiration:7200000}")
    private long accessExpiration; // 2小时
    
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration; // 7天
    
    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, accessSecret, accessExpiration);
    }
    
    public String generateRefreshToken(Long userId, String username) {
        return generateToken(userId, username, refreshSecret, refreshExpiration);
    }
    
    private String generateToken(Long userId, String username, String secret, long expiration) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean validateAccessToken(String token) {
        return validateToken(token, accessSecret);
    }
    
    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshSecret);
    }
    
    private boolean validateToken(String token, String secret) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Long getUserIdFromToken(String token, boolean isAccessToken) {
        String secret = isAccessToken ? accessSecret : refreshSecret;
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }
}