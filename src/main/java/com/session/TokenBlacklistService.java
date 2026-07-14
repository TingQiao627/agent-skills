package com.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 * 使用Redis存储被注销的Token，实现全局登出功能
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;

    @Autowired
    public TokenBlacklistService(StringRedisTemplate redisTemplate,
                                  JwtProperties jwtProperties,
                                  JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
        this.jwtService = jwtService;
    }

    /**
     * 将Token加入黑名单
     *
     * @param token JWT Token
     */
    public void addToBlacklist(String token) {
        try {
            String tokenId = jwtService.extractTokenId(token);
            if (tokenId == null) {
                logger.warn("Cannot add token to blacklist: unable to extract token ID");
                return;
            }

            String key = getBlacklistKey(tokenId);
            long remainingTime = jwtService.getTokenRemainingTime(token);

            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(key, "1", remainingTime, TimeUnit.MILLISECONDS);
                logger.info("Token added to blacklist, tokenId: {}, remaining time: {}ms", tokenId, remainingTime);
            } else {
                logger.debug("Token already expired, no need to blacklist: {}", tokenId);
            }
        } catch (Exception e) {
            logger.error("Failed to add token to blacklist: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token JWT Token
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        try {
            String tokenId = jwtService.extractTokenId(token);
            if (tokenId == null) {
                return false;
            }

            String key = getBlacklistKey(tokenId);
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            logger.error("Failed to check blacklist: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从黑名单中移除Token（通常不需要，Token会自动过期）
     *
     * @param token JWT Token
     */
    public void removeFromBlacklist(String token) {
        try {
            String tokenId = jwtService.extractTokenId(token);
            if (tokenId == null) {
                return;
            }

            String key = getBlacklistKey(tokenId);
            redisTemplate.delete(key);
            logger.info("Token removed from blacklist: {}", tokenId);
        } catch (Exception e) {
            logger.error("Failed to remove token from blacklist: {}", e.getMessage(), e);
        }
    }

    /**
     * 将用户的所有Token加入黑名单（全局登出）
     * 通过使所有关联的Refresh Token失效来实现
     *
     * @param userId 用户ID
     */
    public void blacklistAllUserTokens(String userId) {
        try {
            // 将用户的Refresh Token标记为无效
            String refreshTokenKey = jwtProperties.getRefreshTokenKeyPrefix() + userId;
            redisTemplate.delete(refreshTokenKey);
            logger.info("All tokens blacklisted for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to blacklist all tokens for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 构建黑名单Key
     *
     * @param tokenId Token ID
     * @return Redis Key
     */
    private String getBlacklistKey(String tokenId) {
        return jwtProperties.getBlacklistKeyPrefix() + tokenId;
    }
}