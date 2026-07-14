package com.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SmsCodeService {
    private final StringRedisTemplate redisTemplate;
    
    @Value("${security.sms.code-expiration:300000}")
    private Long codeExpiration;
    
    @Value("${security.sms.daily-limit:10}")
    private Integer dailyLimit;
    
    private static final String CODE_PREFIX = "sms:code:";
    private static final String DAILY_PREFIX = "sms:daily:";
    
    public void sendCode(String phone, String ip) {
        // 检查每日发送限制
        String dailyKey = DAILY_PREFIX + phone + ":" + getToday();
        Long count = redisTemplate.opsForValue().increment(dailyKey);
        
        if (count != null && count == 1) {
            redisTemplate.expire(dailyKey, 1, TimeUnit.DAYS);
        }
        
        if (count != null && count > dailyLimit) {
            throw new RuntimeException("今日发送次数已达上限");
        }
        
        // 生成6位验证码
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        
        // 存储到Redis
        String key = CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, codeExpiration, TimeUnit.MILLISECONDS);
        
        // TODO: 调用短信服务商API发送短信
        // smsSender.send(phone, code);
    }
    
    public boolean verifyCode(String phone, String code) {
        String key = CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }
    
    private String getToday() {
        return java.time.LocalDate.now().toString();
    }
}