package com.example.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * F1 账号密码登录 - 支持多字段登录
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "登录账号不能为空")
    private String account; // 可为用户名、手机号或邮箱
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String captchaKey;
    
    private String captchaCode;
    
    private Boolean rememberMe = false;
}