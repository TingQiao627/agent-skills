package com.example.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务
 * F2 手机验证码登录 - 短信发送与验证
 */
@Service
@RequiredArgsConstructor
public class SmsService {
    
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int MAX_SEND_COUNT_PER_DAY = 10;
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 发送验证码
     * F2: 频率限制 - 每天最多10条
     */
    public void sendSmsCode(String phone) {
        // 检查发送频率
        String countKey = "sms:count:" + phone + ":" + System.currentTimeMillis() / 86400000;
        Long count = redisTemplate.opsForValue().increment(countKey);
        if (count != null && count > MAX_SEND_COUNT_PER_DAY) {
            throw new RuntimeException("短信发送次数超限");
        }
        redisTemplate.expire(countKey, 1, TimeUnit.DAYS);
        
        // 生成6位验证码
        String code = generateCode();
        
        // 存储验证码
        String codeKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 实际发送短信（TODO: 接入短信服务商）
        // smsClient.send(phone, "验证码：" + code);
    }
    
    /**
     * 验证短信验证码
     */
    public void validateSmsCode(String phone, String code) {
        String codeKey = SMS_CODE_PREFIX + phone;
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (savedCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        
        if (!savedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        
        // 验证成功后删除验证码
        redisTemplate.delete(codeKey);
    }
    
    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}