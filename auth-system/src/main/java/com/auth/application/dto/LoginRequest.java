package com.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "账号不能为空")
    private String account;  // 支持用户名/邮箱/手机号
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private boolean rememberMe;
    private String captchaToken;
    private String captchaCode;
}