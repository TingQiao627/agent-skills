package com.session;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性类
 * 用于从配置文件中加载JWT相关配置
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥
     */
    private String secret = "your-256-bit-secret-key-for-jwt-signing-must-be-long-enough";

    /**
     * Access Token过期时间（毫秒），默认15分钟
     */
    private long accessTokenExpiration = 15 * 60 * 1000L;

    /**
     * Refresh Token过期时间（毫秒），默认7天
     */
    private long refreshTokenExpiration = 7 * 24 * 60 * 60 * 1000L;

    /**
     * Token前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Token请求头名称
     */
    private String header = "Authorization";

    /**
     * Access Token过期前多久开始刷新（毫秒），默认5分钟
     */
    private long refreshBeforeExpiration = 5 * 60 * 1000L;

    /**
     * Refresh Token在Redis中的key前缀
     */
    private String refreshTokenKeyPrefix = "jwt:refresh:";

    /**
     * 黑名单在Redis中的key前缀
     */
    private String blacklistKeyPrefix = "jwt:blacklist:";

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public long getRefreshBeforeExpiration() {
        return refreshBeforeExpiration;
    }

    public void setRefreshBeforeExpiration(long refreshBeforeExpiration) {
        this.refreshBeforeExpiration = refreshBeforeExpiration;
    }

    public String getRefreshTokenKeyPrefix() {
        return refreshTokenKeyPrefix;
    }

    public void setRefreshTokenKeyPrefix(String refreshTokenKeyPrefix) {
        this.refreshTokenKeyPrefix = refreshTokenKeyPrefix;
    }

    public String getBlacklistKeyPrefix() {
        return blacklistKeyPrefix;
    }

    public void setBlacklistKeyPrefix(String blacklistKeyPrefix) {
        this.blacklistKeyPrefix = blacklistKeyPrefix;
    }
}