package com.auth.controller;

import com.auth.security.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CaptchaController {
    
    private final CaptchaService captchaService;
    
    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    public ResponseEntity<Map<String, String>> getCaptcha() throws Exception {
        CaptchaService.CaptchaResult result = captchaService.generateCaptcha();
        
        // 图片转Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(result.image(), "png", baos);
        String imageBase64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        
        Map<String, String> response = new HashMap<>();
        response.put("captchaKey", result.captchaKey());
        response.put("captchaImage", "data:image/png;base64," + imageBase64);
        
        return ResponseEntity.ok(response);
    }
}