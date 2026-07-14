package com.example.login.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录安全服务
 * F4 安全防护 - 登录锁定、图形验证码、防暴力破解
 */
@Service
@RequiredArgsConstructor
public class LoginSecurityService {
    
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final long LOCK_DURATION_MINUTES = 30;
    
    private final StringRedisTemplate redisTemplate;
    private final CaptchaService captchaService;
    
    /**
     * 检查账号是否被锁定
     */
    public boolean isAccountLocked(String account) {
        String lockKey = "login:lock:" + account;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }
    
    /**
     * 记录登录失败次数
     * F4: 防暴力破解 - 5次失败后锁定30分钟
     */
    public void recordLoginFailure(String account) {
        String failureKey = "login:failure:" + account;
        Long failures = redisTemplate.opsForValue().increment(failureKey);
        
        if (failures != null) {
            redisTemplate.expire(failureKey, 24, TimeUnit.HOURS);
            
            if (failures >= MAX_LOGIN_FAILURES) {
                lockAccount(account);
                redisTemplate.delete(failureKey);
            }
        }
    }
    
    /**
     * 锁定账号
     */
    private void lockAccount(String account) {
        String lockKey = "login:lock:" + account;
        redisTemplate.opsForValue().set(lockKey, "locked", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * 重置登录失败次数
     */
    public void resetLoginFailure(String account) {
        String failureKey = "login:failure:" + account;
        redisTemplate.delete(failureKey);
    }
    
    /**
     * 验证图形验证码
     */
    public void validateCaptcha(String captchaKey, String captchaCode) {
        captchaService.validate(captchaKey, captchaCode);
    }
    
    /**
     * 生成图形验证码
     */
    public CaptchaResult generateCaptcha() {
        String captchaKey = UUID.randomUUID().toString();
        String captchaCode = captchaService.generate(captchaKey);
        return new CaptchaResult(captchaKey, captchaCode);
    }
    
    public record CaptchaResult(String captchaKey, String captchaCode) {}
}