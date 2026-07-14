package com.sms.service.impl;

import com.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 阿里云短信服务实现
 */
@Service
public class AliyunSmsServiceImpl implements SmsService {
    
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_RATE_LIMIT_PREFIX = "sms:rate:";
    private static final String SMS_IP_LIMIT_PREFIX = "sms:ip:";
    
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int PHONE_RATE_LIMIT_SECONDS = 60;
    private static final int IP_RATE_LIMIT_PER_HOUR = 10;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private final SecureRandom random = new SecureRandom();
    
    @Override
    public String sendCode(String phone, String ip) {
        // 检查手机号频率限制
        String rateLimitKey = SMS_RATE_LIMIT_PREFIX + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            return "发送频繁，请稍后再试";
        }
        
        // 检查IP频率限制
        String ipLimitKey = SMS_IP_LIMIT_PREFIX + ip;
        String ipCount = redisTemplate.opsForValue().get(ipLimitKey);
        if (ipCount != null && Integer.parseInt(ipCount) >= IP_RATE_LIMIT_PER_HOUR) {
            return "发送次数超限，请稍后再试";
        }
        
        // 生成6位验证码
        String code = generateCode();
        
        // 存储验证码，有效期5分钟
        String codeKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 设置手机号频率限制，60秒
        redisTemplate.opsForValue().set(rateLimitKey, "1", PHONE_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        
        // IP计数器增加
        if (ipCount == null) {
            redisTemplate.opsForValue().set(ipLimitKey, "1", 1, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(ipLimitKey);
        }
        
        // TODO: 调用阿里云短信API发送验证码
        // 实际项目中需要注入阿里云短信客户端并调用
        // aliyunSmsClient.send(phone, code);
        
        return "验证码发送成功";
    }
    
    @Override
    public Long verifyCode(String phone, String code) {
        String codeKey = SMS_CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        
        if (storedCode == null) {
            return null;
        }
        
        if (!storedCode.equals(code)) {
            return null;
        }
        
        // 验证成功后删除验证码
        redisTemplate.delete(codeKey);
        
        // TODO: 自动注册用户逻辑
        // 实际项目中需要查询用户，不存在则创建
        // User user = userService.findByPhone(phone);
        // if (user == null) {
        //     user = userService.registerByPhone(phone);
        // }
        // return user.getId();
        
        return 1L; // 模拟用户ID
    }
    
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}