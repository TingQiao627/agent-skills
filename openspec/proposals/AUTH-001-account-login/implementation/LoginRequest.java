package com.example.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求 DTO
 * 
 * 规格参考：OPSX.md F1 接口规格
 */
@Data
public class LoginRequest {
    
    /**
     * 登录标识
     * 用户名/邮箱/手机号
     */
    @NotBlank(message = "登录标识不能为空")
    private String identifier;
    
    /**
     * 密码（RSA加密后）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 1, max = 512, message = "密码长度不合法")
    private String password;
    
    /**
     * 是否记住密码
     */
    private Boolean rememberMe = false;
    
    /**
     * 图形验证码 Token（安全策略触发时必填）
     */
    private String captchaToken;
    
    /**
     * 设备指纹
     */
    private String deviceFingerprint;
}