package com.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录锁定服务
 * 实现连续失败5次锁定30分钟的机制
 * 使用Redis存储失败计数和锁定状态
 */
@Service
public class LockService {
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 30;
    
    // Redis key前缀
    private static final String FAILED_ATTEMPTS_KEY = "login:failed:";
    private static final String LOCKED_KEY = "login:locked:";
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 记录登录失败
     * @param userId 用户ID
     * @return 当前失败次数
     */
    public int recordFailedAttempt(Long userId) {
        String key = FAILED_ATTEMPTS_KEY + userId;
        
        // 增加失败计数
        Long attempts = redisTemplate.opsForValue().increment(key);
        
        // 如果是第一次失败，设置过期时间（30分钟）
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
        
        // 达到最大失败次数，锁定账户
        if (attempts != null && attempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(userId);
        }
        
        return attempts != null ? attempts.intValue() : 0;
    }
    
    /**
     * 锁定账户
     * @param userId 用户ID
     */
    public void lockAccount(Long userId) {
        String key = LOCKED_KEY + userId;
        redisTemplate.opsForValue().set(key, "1", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * 检查账户是否被锁定
     * @param userId 用户ID
     * @return 是否锁定
     */
    public boolean isAccountLocked(Long userId) {
        String key = LOCKED_KEY + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 解锁账户（登录成功后调用）
     * @param userId 用户ID
     */
    public void unlockAccount(Long userId) {
        // 清除失败计数
        redisTemplate.delete(FAILED_ATTEMPTS_KEY + userId);
        // 清除锁定状态
        redisTemplate.delete(LOCKED_KEY + userId);
    }
    
    /**
     * 获取剩余锁定时间（分钟）
     * @param userId 用户ID
     * @return 剩余时间，如果未锁定返回0
     */
    public long getRemainingLockTime(Long userId) {
        String key = LOCKED_KEY + userId;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
    
    /**
     * 获取当前失败次数
     * @param userId 用户ID
     * @return 失败次数
     */
    public int getFailedAttempts(Long userId) {
        String key = FAILED_ATTEMPTS_KEY + userId;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }
    
    /**
     * 检查是否需要验证码（失败3次后需要验证码）
     * @param userId 用户ID
     * @return 是否需要验证码
     */
    public boolean needCaptcha(Long userId) {
        return getFailedAttempts(userId) >= 3;
    }
}