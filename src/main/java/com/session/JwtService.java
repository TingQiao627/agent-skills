package com.session;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT服务类
 * 负责生成、解析和验证Access Token和Refresh Token
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    @Autowired
    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Access Token
     *
     * @param userId 用户ID
     * @return Access Token字符串
     */
    public String generateAccessToken(String userId) {
        return generateAccessToken(userId, new HashMap<>());
    }

    /**
     * 生成Access Token（带额外claims）
     *
     * @param userId 用户ID
     * @param claims 额外的声明信息
     * @return Access Token字符串
     */
    public String generateAccessToken(String userId, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成Refresh Token
     *
     * @param userId 用户ID
     * @return Refresh Token字符串
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成Token对（Access Token + Refresh Token）
     *
     * @param userId 用户ID
     * @return 包含Access Token和Refresh Token的Map
     */
    public Map<String, String> generateTokenPair(String userId) {
        return generateTokenPair(userId, new HashMap<>());
    }

    /**
     * 生成Token对（带额外claims）
     *
     * @param userId 用户ID
     * @param claims 额外的声明信息
     * @return 包含Access Token和Refresh Token的Map
     */
    public Map<String, String> generateTokenPair(String userId, Map<String, Object> claims) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", generateAccessToken(userId, claims));
        tokens.put("refreshToken", generateRefreshToken(userId));
        return tokens;
    }

    /**
     * 解析Token获取Claims
     *
     * @param token JWT Token
     * @return Claims对象
     * @throws JwtException 解析失败时抛出
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public String extractUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (JwtException e) {
            logger.debug("Failed to extract userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取Token ID（JTI）
     *
     * @param token JWT Token
     * @return Token ID
     */
    public String extractTokenId(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getId();
        } catch (JwtException e) {
            logger.debug("Failed to extract token ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取Token的过期时间
     *
     * @param token JWT Token
     * @return 过期时间（Date）
     */
    public Date getExpirationDate(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration();
        } catch (JwtException e) {
            logger.debug("Failed to get expiration date: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查Token是否即将过期（用于静默刷新）
     *
     * @param token JWT Token
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Date expiration = getExpirationDate(token);
            if (expiration == null) {
                return true;
            }
            long timeLeft = expiration.getTime() - System.currentTimeMillis();
            return timeLeft <= jwtProperties.getRefreshBeforeExpiration();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 检查Token是否为Refresh Token
     *
     * @param token JWT Token
     * @return 是否为Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 获取Access Token的剩余有效时间（毫秒）
     *
     * @param token Access Token
     * @return 剩余有效时间
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDate(token);
            if (expiration == null) {
                return 0;
            }
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }
}