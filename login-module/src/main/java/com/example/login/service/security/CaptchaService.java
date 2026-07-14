package com.example.login.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务
 * F4 安全防护 - 防暴力破解
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 生成验证码
     */
    public String generate(String captchaKey) {
        String code = generateCode();
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaKey, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return code;
    }
    
    /**
     * 验证验证码
     */
    public void validate(String captchaKey, String code) {
        String savedCode = redisTemplate.opsForValue().get(CAPTCHA_PREFIX + captchaKey);
        
        if (savedCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        
        if (!savedCode.equalsIgnoreCase(code)) {
            throw new RuntimeException("验证码错误");
        }
        
        redisTemplate.delete(CAPTCHA_PREFIX + captchaKey);
    }
    
    /**
     * 生成验证码图片
     */
    public BufferedImage generateImage(String code) {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // 干扰线
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        
        // 验证码文本
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.BLACK);
        g.drawString(code, 20, 28);
        
        g.dispose();
        return image;
    }
    
    private String generateCode() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}