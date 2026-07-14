package com.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 账号密码登录请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    /**
     * 登录账号（用户名/手机号/邮箱）
     */
    @NotBlank(message = "账号不能为空")
    private String account;
    
    /**
     * 密码（RSA加密后）
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 图形验证码（失败后必填）
     */
    private String captchaKey;
    
    private String captchaCode;
    
    /**
     * 记住密码
     */
    @Builder.Default
    private Boolean rememberMe = false;
    
    /**
     * 设备标识
     */
    private String deviceId;
}