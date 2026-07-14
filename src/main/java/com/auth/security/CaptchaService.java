package com.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    
    /**
     * 生成验证码
     */
    public CaptchaResult generateCaptcha() {
        String captchaKey = UUID.randomUUID().toString();
        String captchaCode = generateRandomCode(4);
        
        // 存储到Redis
        redisTemplate.opsForValue().set(
            "captcha:" + captchaKey, 
            captchaCode.toLowerCase(), 
            CAPTCHA_EXPIRE_MINUTES, 
            TimeUnit.MINUTES
        );
        
        // 生成图片
        BufferedImage image = generateImage(captchaCode);
        
        return new CaptchaResult(captchaKey, captchaCode, image);
    }
    
    /**
     * 验证验证码
     */
    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        
        String key = "captcha:" + captchaKey;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode != null && storedCode.equalsIgnoreCase(captchaCode.trim())) {
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查验证码是否需要（失败1次后显示）
     */
    public boolean isCaptchaRequired(String account) {
        String key = "login:fail:account:" + account;
        String count = redisTemplate.opsForValue().get(key);
        return count != null && Integer.parseInt(count) >= 1;
    }
    
    // ========== 私有方法 ==========
    
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    private BufferedImage generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 干扰线
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), 
                       random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        
        // 验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.BLACK);
        g.drawString(code, 20, 30);
        
        g.dispose();
        return image;
    }
    
    /**
     * 验证码结果
     */
    public record CaptchaResult(String captchaKey, String captchaCode, BufferedImage image) {}
}