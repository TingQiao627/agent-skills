package com.example.auth.controller;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 * 处理登录、登出、Token 刷新等请求
 * 
 * 规格参考：OPSX.md F1 账号密码登录接口
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 账号密码登录
     * 
     * POST /api/auth/login/password
     * Request:
     *   identifier: string      # 用户名/邮箱/手机号
     *   password: string        # RSA加密后的密码
     *   remember_me: boolean    # 是否记住密码
     *   captcha_token: string   # 图形验证码token（安全策略触发时必填）
     * 
     * Response:
     *   access_token: string
     *   refresh_token: string
     *   expires_in: number
     *   user_info: object
     */
    @PostMapping("/login/password")
    public ResponseEntity<LoginResponse> loginWithPassword(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Login attempt for identifier: {}", request.getIdentifier());
        
        String ipAddress = getClientIp(httpRequest);
        String deviceFingerprint = request.getDeviceFingerprint();
        
        LoginResponse response = authService.loginWithPassword(
            request.getIdentifier(),
            request.getPassword(),
            request.getRememberMe(),
            request.getCaptchaToken(),
            ipAddress,
            deviceFingerprint
        );
        
        log.info("Login successful for user: {}", response.getUserInfo().getUsername());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Token 刷新
     * 
     * POST /api/auth/token/refresh
     * Request:
     *   refresh_token: string
     * 
     * Response:
     *   access_token: string
     *   refresh_token: string
     *   expires_in: number
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken,
            @CookieValue(value = "refresh_token", required = false) String cookieRefreshToken,
            HttpServletRequest httpRequest) {
        
        String token = refreshToken != null ? refreshToken : cookieRefreshToken;
        
        if (token == null) {
            return ResponseEntity.badRequest().build();
        }
        
        String deviceFingerprint = httpRequest.getHeader("X-Device-Fingerprint");
        LoginResponse response = authService.refreshToken(token, deviceFingerprint);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 登出
     * 
     * POST /api/auth/logout
     * Request:
     *   global: boolean  # 是否全局登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        boolean global = request != null && request.isGlobal();
        
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token, global);
        }
        
        log.info("Logout successful, global: {}", global);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 登出请求 DTO
     */
    public static class LogoutRequest {
        private boolean global;
        
        public boolean isGlobal() {
            return global;
        }
        
        public void setGlobal(boolean global) {
            this.global = global;
        }
    }
}