package com.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 登录尝试服务 - 防暴力破解
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final int MAX_ATTEMPTS_PER_ACCOUNT = 5;
    private static final int MAX_ATTEMPTS_PER_IP = 10;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);
    
    /**
     * 记录登录失败（账号维度）
     */
    public void recordFailByAccount(String account) {
        String key = "login:fail:account:" + account;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOCK_DURATION);
        
        if (count >= MAX_ATTEMPTS_PER_ACCOUNT) {
            log.warn("账号 {} 登录失败次数达到 {} 次，已锁定", account, count);
        }
    }
    
    /**
     * 记录登录失败（IP维度）
     */
    public void recordFailByIp(String ip) {
        String key = "login:fail:ip:" + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOCK_DURATION);
        
        if (count >= MAX_ATTEMPTS_PER_IP) {
            log.warn("IP {} 登录失败次数达到 {} 次，已锁定", ip, count);
        }
    }
    
    /**
     * 检查账号是否锁定
     */
    public boolean isAccountLocked(String account) {
        String key = "login:fail:account:" + account;
        String count = redisTemplate.opsForValue().get(key);
        return count != null && Integer.parseInt(count) >= MAX_ATTEMPTS_PER_ACCOUNT;
    }
    
    /**
     * 检查IP是否锁定
     */
    public boolean isIpLocked(String ip) {
        String key = "login:fail:ip:" + ip;
        String count = redisTemplate.opsForValue().get(key);
        return count != null && Integer.parseInt(count) >= MAX_ATTEMPTS_PER_IP;
    }
    
    /**
     * 重置账号失败计数
     */
    public void resetAccountFailCount(String account) {
        String key = "login:fail:account:" + account;
        redisTemplate.delete(key);
    }
    
    /**
     * 获取账号剩余尝试次数
     */
    public int getRemainingAttempts(String account) {
        String key = "login:fail:account:" + account;
        String count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            return MAX_ATTEMPTS_PER_ACCOUNT;
        }
        return Math.max(0, MAX_ATTEMPTS_PER_ACCOUNT - Integer.parseInt(count));
    }
}