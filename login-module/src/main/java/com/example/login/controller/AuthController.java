package com.example.login.controller;

import com.example.login.dto.LoginRequest;
import com.example.login.dto.LoginResponse;
import com.example.login.dto.SmsLoginRequest;
import com.example.login.service.AuthService;
import com.example.login.service.security.LoginSecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * F1 账号密码登录 + F2 手机验证码登录 + F4 安全防护
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final LoginSecurityService loginSecurityService;
    
    /**
     * F1: 账号密码登录
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * F2: 手机验证码登录
     */
    @PostMapping("/sms-login")
    public ResponseEntity<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        LoginResponse response = authService.smsLogin(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * F4: 获取图形验证码
     */
    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha() {
        LoginSecurityService.CaptchaResult result = loginSecurityService.generateCaptcha();
        return ResponseEntity.ok(new CaptchaResponse(result.captchaKey(), result.captchaCode()));
    }
    
    /**
     * F5: 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
    
    /**
     * F5: 全局登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
    
    public record CaptchaResponse(String captchaKey, String captchaCode) {}
}