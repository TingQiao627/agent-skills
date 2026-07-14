package com.example.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import com.example.auth.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token 提供者
 * 实现 Access Token 和 Refresh Token 双 Token 机制
 * 
 * 规格参考：OPSX.md F5 会话管理
 */
@Slf4j
@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    
    /**
     * 可配置参数（来自 DECISIONS.md）
     */
    private static final long ACCESS_TOKEN_EXPIRE_MINUTES = 120;  // 2小时
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;       // 7天
    private static final long REFRESH_WINDOW_MINUTES = 5;          // 5分钟刷新窗口
    
    public JwtTokenProvider() {
        // 生产环境应从配置文件读取密钥
        String secret = "your-256-bit-secret-key-must-be-at-least-32-characters-long!";
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成 Access Token
     * 
     * @param user 用户实体
     * @param deviceFingerprint 设备指纹
     * @return JWT Token 字符串
     */
    public String generateAccessToken(User user, String deviceFingerprint) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("roles", Collections.emptyList()); // 后续从角色服务获取
        claims.put("permissions", Collections.emptyList());
        claims.put("device_fingerprint", deviceFingerprint);
        
        String jti = UUID.randomUUID().toString();
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId().toString())
            .setId(jti)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 
                TimeUnit.MINUTES.toMillis(ACCESS_TOKEN_EXPIRE_MINUTES)))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    /**
     * 生成 Refresh Token
     * 
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     * @return JWT Token 字符串
     */
    public String generateRefreshToken(Long userId, String deviceFingerprint) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("device_fingerprint", deviceFingerprint);
        
        String jti = UUID.randomUUID().toString();
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userId.toString())
            .setId(jti)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 
                TimeUnit.DAYS.toMillis(REFRESH_TOKEN_EXPIRE_DAYS)))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    /**
     * 验证 Token 并获取 Claims
     * 
     * @param token JWT Token
     * @return Claims 对象
     */
    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            throw new RuntimeException("Token 已过期", e);
        } catch (JwtException e) {
            log.error("Invalid token: {}", e.getMessage());
            throw new RuntimeException("无效的 Token", e);
        }
    }
    
    /**
     * 从 Token 中提取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateAndGetClaims(token);
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * 从 Token 中提取 JTI（唯一标识）
     */
    public String getJtiFromToken(String token) {
        Claims claims = validateAndGetClaims(token);
        return claims.getId();
    }
    
    /**
     * 检查 Token 是否即将过期（用于静默刷新）
     * 
     * @param token JWT Token
     * @param minutesThreshold 提前多少分钟判定为即将过期
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, int minutesThreshold) {
        try {
            Claims claims = validateAndGetClaims(token);
            Date expiration = claims.getExpiration();
            long timeLeft = expiration.getTime() - System.currentTimeMillis();
            return timeLeft < TimeUnit.MINUTES.toMillis(minutesThreshold);
        } catch (Exception e) {
            return true; // 无效或过期的 Token 视为即将过期
        }
    }
    
    /**
     * 获取 Access Token 过期时间（秒）
     */
    public long getAccessTokenExpireSeconds() {
        return TimeUnit.MINUTES.toSeconds(ACCESS_TOKEN_EXPIRE_MINUTES);
    }
    
    /**
     * 获取 Refresh Token 过期时间（秒）
     */
    public long getRefreshTokenExpireSeconds() {
        return TimeUnit.DAYS.toSeconds(REFRESH_TOKEN_EXPIRE_DAYS);
    }
    
    /**
     * 获取刷新窗口期（秒）
     */
    public long getRefreshWindowSeconds() {
        return TimeUnit.MINUTES.toSeconds(REFRESH_WINDOW_MINUTES);
    }
}