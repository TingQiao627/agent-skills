package com.sms.controller;

import com.sms.dto.SendCodeRequest;
import com.sms.dto.VerifyCodeRequest;
import com.sms.service.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信验证码控制器
 */
@RestController
@RequestMapping("/api/sms")
public class SmsController {
    
    @Autowired
    private SmsService smsService;
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(
            @Valid @RequestBody SendCodeRequest request,
            HttpServletRequest httpRequest) {
        
        String ip = getClientIp(httpRequest);
        String result = smsService.sendCode(request.getPhone(), ip);
        
        Map<String, Object> response = new HashMap<>();
        if (result.contains("成功")) {
            response.put("success", true);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 验证验证码
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @Valid @RequestBody VerifyCodeRequest request) {
        
        Long userId = smsService.verifyCode(request.getPhone(), request.getCode());
        
        Map<String, Object> response = new HashMap<>();
        if (userId != null) {
            response.put("success", true);
            response.put("message", "验证成功");
            response.put("userId", userId);
            // TODO: 生成JWT token
            // response.put("token", jwtService.generateToken(userId));
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "验证码错误或已过期");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多IP情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}