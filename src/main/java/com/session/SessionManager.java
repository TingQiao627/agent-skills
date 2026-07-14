package com.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理服务
 * 负责管理用户会话、Token刷新、会话状态维护
 */
@Service
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public SessionManager(JwtService jwtService,
                          JwtProperties jwtProperties,
                          TokenBlacklistService tokenBlacklistService,
                          StringRedisTemplate redisTemplate) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建新会话（登录）
     *
     * @param userId 用户ID
     * @return Token对（Access Token + Refresh Token）
     */
    public Map<String, String> createSession(String userId) {
        return createSession(userId, new HashMap<>());
    }

    /**
     * 创建新会话（带额外claims）
     *
     * @param userId 用户ID
     * @param claims 额外的声明信息
     * @return Token对（Access Token + Refresh Token）
     */
    public Map<String, String> createSession(String userId, Map<String, Object> claims) {
        Map<String, String> tokens = jwtService.generateTokenPair(userId, claims);
        String refreshToken = tokens.get("refreshToken");

        // 将Refresh Token存储到Redis
        storeRefreshToken(userId, refreshToken);

        logger.info("Session created for user: {}", userId);
        return tokens;
    }

    /**
     * 刷新Access Token（使用Refresh Token）
     *
     * @param refreshToken Refresh Token
     * @return 新的Token对，如果刷新失败返回null
     */
    public Map<String, String> refreshAccessToken(String refreshToken) {
        // 1. 验证Refresh Token格式和签名
        if (!jwtService.validateToken(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            logger.warn("Invalid refresh token");
            return null;
        }

        // 2. 提取用户ID
        String userId = jwtService.extractUserId(refreshToken);
        if (userId == null) {
            logger.warn("Cannot extract userId from refresh token");
            return null;
        }

        // 3. 检查Refresh Token是否在Redis中且有效
        if (!isRefreshTokenValid(userId, refreshToken)) {
            logger.warn("Refresh token not found or invalid in storage for user: {}", userId);
            return null;
        }

        // 4. 检查Token是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            logger.warn("Refresh token is blacklisted for user: {}", userId);
            return null;
        }

        // 5. 生成新的Token对
        Map<String, String> newTokens = jwtService.generateTokenPair(userId);

        // 6. 更新Redis中的Refresh Token
        storeRefreshToken(userId, newTokens.get("refreshToken"));

        logger.info("Access token refreshed for user: {}", userId);
        return newTokens;
    }

    /**
     * 静默刷新 - 在Access Token即将过期时自动刷新
     * 只有在Token即将过期且Refresh Token有效时才刷新
     *
     * @param accessToken 当前Access Token
     * @param refreshToken Refresh Token
     * @return 新的Token对，如果不需要刷新或刷新失败返回null
     */
    public Map<String, String> silentRefresh(String accessToken, String refreshToken) {
        // 检查Access Token是否即将过期
        if (!jwtService.isTokenExpiringSoon(accessToken)) {
            return null;
        }

        // 检查Access Token是否仍然有效（只是即将过期）
        if (!jwtService.validateToken(accessToken)) {
            return null;
        }

        // 检查Access Token是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(accessToken)) {
            return null;
        }

        // 使用Refresh Token获取新Token
        return refreshAccessToken(refreshToken);
    }

    /**
     * 登出（销毁会话）
     *
     * @param accessToken Access Token
     */
    public void logout(String accessToken) {
        try {
            String userId = jwtService.extractUserId(accessToken);
            if (userId == null) {
                return;
            }

            // 将当前Access Token加入黑名单
            tokenBlacklistService.addToBlacklist(accessToken);

            // 删除Redis中的Refresh Token
            removeRefreshToken(userId);

            logger.info("User logged out: {}", userId);
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 全局登出（使所有Token失效）
     *
     * @param userId 用户ID
     */
    public void globalLogout(String userId) {
        // 将用户的所有Token加入黑名单
        tokenBlacklistService.blacklistAllUserTokens(userId);

        // 删除用户的Refresh Token
        removeRefreshToken(userId);

        logger.info("Global logout for user: {}", userId);
    }

    /**
     * 检查会话是否有效
     *
     * @param accessToken Access Token
     * @return 是否有效
     */
    public boolean isSessionValid(String accessToken) {
        if (!jwtService.validateToken(accessToken)) {
            return false;
        }

        // 检查Token是否在黑名单中
        return !tokenBlacklistService.isBlacklisted(accessToken);
    }

    /**
     * 获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public String getUserId(String token) {
        return jwtService.extractUserId(token);
    }

    /**
     * 存储Refresh Token到Redis
     *
     * @param userId 用户ID
     * @param refreshToken Refresh Token
     */
    private void storeRefreshToken(String userId, String refreshToken) {
        String key = jwtProperties.getRefreshTokenKeyPrefix() + userId;
        long expiration = jwtProperties.getRefreshTokenExpiration();
        redisTemplate.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
        logger.debug("Refresh token stored for user: {}", userId);
    }

    /**
     * 验证Refresh Token是否有效（在Redis中存在且匹配）
     *
     * @param userId 用户ID
     * @param refreshToken Refresh Token
     * @return 是否有效
     */
    private boolean isRefreshTokenValid(String userId, String refreshToken) {
        String key = jwtProperties.getRefreshTokenKeyPrefix() + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken);
    }

    /**
     * 从Redis删除Refresh Token
     *
     * @param userId 用户ID
     */
    private void removeRefreshToken(String userId) {
        String key = jwtProperties.getRefreshTokenKeyPrefix() + userId;
        redisTemplate.delete(key);
        logger.debug("Refresh token removed for user: {}", userId);
    }

    /**
     * 强制刷新会话（强制生成新的Token对）
     *
     * @param userId 用户ID
     * @return 新的Token对
     */
    public Map<String, String> forceRefresh(String userId) {
        return createSession(userId);
    }
}