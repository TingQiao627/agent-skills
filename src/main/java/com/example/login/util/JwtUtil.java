package com.example.login.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:login2.0-default-secret-key-must-be-changed}")
    private String secret;
    
    @Value("${jwt.access-token-expire:7200000}")
    private Long accessTokenExpire;
    
    @Value("${jwt.refresh-token-expire:604800000}")
    private Long refreshTokenExpire;
    
    private Key key;
    
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "access");
        return createToken(claims, accessTokenExpire);
    }
    
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return createToken(claims, refreshTokenExpire);
    }
    
    private String createToken(Map<String, Object> claims, Long expireTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            throw new RuntimeException("Token已过期");
        } catch (JwtException e) {
            log.error("Invalid token: {}", e.getMessage());
            throw new RuntimeException("无效的Token");
        }
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public Long getAccessTokenExpireTime() {
        return accessTokenExpire;
    }
    
    public Long getRefreshTokenExpireTime() {
        return refreshTokenExpire;
    }
}