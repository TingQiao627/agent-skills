package com.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 手机验证码登录请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneLoginRequest {
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    private String code;
    
    /**
     * 设备标识
     */
    private String deviceId;
}