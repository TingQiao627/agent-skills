package com.security.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 图形验证码服务
 * 自实现验证码生成与校验
 */
@Service
public class CaptchaService {
    
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final long EXPIRE_MINUTES = 5;
    
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    
    private final Random random = new Random();
    
    // 验证码字符集（去除容易混淆的字符）
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 生成验证码
     * @return 包含验证码ID和Base64图片的CaptchaResult对象
     */
    public CaptchaResult generateCaptcha() {
        // 生成验证码文本
        String code = generateCode();
        
        // 生成验证码ID
        String captchaId = UUID.randomUUID().toString();
        
        // 存储到Redis
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, code.toUpperCase(), EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 生成图片
        BufferedImage image = createImage(code);
        String base64Image = imageToBase64(image);
        
        return new CaptchaResult(captchaId, base64Image);
    }
    
    /**
     * 验证验证码
     * @param captchaId 验证码ID
     * @param inputCode 用户输入的验证码
     * @return 是否验证成功
     */
    public boolean validateCaptcha(String captchaId, String inputCode) {
        if (captchaId == null || inputCode == null) {
            return false;
        }
        
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        // 验证成功后删除验证码（一次性使用）
        if (storedCode != null) {
            redisTemplate.delete(key);
            return storedCode.equalsIgnoreCase(inputCode.trim());
        }
        
        return false;
    }
    
    /**
     * 生成随机验证码文本
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }
    
    /**
     * 创建验证码图片
     */
    private BufferedImage createImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        drawInterferenceLines(g);
        
        // 绘制验证码字符
        drawCode(g, code);
        
        // 绘制干扰点
        drawNoise(g);
        
        g.dispose();
        return image;
    }
    
    /**
     * 绘制验证码字符
     */
    private void drawCode(Graphics2D g, String code) {
        Font font = new Font("Arial", Font.BOLD, 28);
        g.setFont(font);
        
        int x = 10;
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            
            // 随机旋转角度
            double angle = (random.nextDouble() - 0.5) * 0.4;
            g.rotate(angle, x + 12, HEIGHT / 2);
            
            g.drawString(String.valueOf(code.charAt(i)), x, HEIGHT - 10);
            
            g.rotate(-angle, x + 12, HEIGHT / 2);
            x += 28;
        }
    }
    
    /**
     * 绘制干扰线
     */
    private void drawInterferenceLines(Graphics2D g) {
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), 
                       random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
    }
    
    /**
     * 绘制干扰点
     */
    private void drawNoise(Graphics2D g) {
        for (int i = 0; i < 40; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }
    }
    
    /**
     * 图片转Base64
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64Utils.encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to base64", e);
        }
    }
    
    /**
     * 验证码结果对象
     */
    public static class CaptchaResult {
        private final String captchaId;
        private final String captchaImage;
        
        public CaptchaResult(String captchaId, String captchaImage) {
            this.captchaId = captchaId;
            this.captchaImage = captchaImage;
        }
        
        public String getCaptchaId() {
            return captchaId;
        }
        
        public String getCaptchaImage() {
            return captchaImage;
        }
    }
}