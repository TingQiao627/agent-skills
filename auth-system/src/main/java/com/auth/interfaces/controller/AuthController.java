package com.auth.interfaces.controller;

import com.auth.application.dto.LoginRequest;
import com.auth.application.dto.LoginResponse;
import com.auth.application.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, 
                                HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return authService.login(request, ip);
    }
    
    @PostMapping("/logout")
    public void logout() {
        // JWT无状态登出，客户端删除Token即可
    }
    
    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody String refreshToken) {
        // Token刷新逻辑
        return null;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}