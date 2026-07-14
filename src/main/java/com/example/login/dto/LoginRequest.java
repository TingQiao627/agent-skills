package com.example.login.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @NotBlank(message = "登录标识不能为空")
    private String loginField;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String captchaKey;
    
    private String captchaCode;
    
    private Boolean rememberMe;
    
    private String deviceType;
    
    private String deviceName;
    
    private String userAgent;
}