package com.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送验证码请求DTO
 */
public class SendCodeRequest {
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private String captchaKey;
    
    private String captchaCode;
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getCaptchaKey() {
        return captchaKey;
    }
    
    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }
    
    public String getCaptchaCode() {
        return captchaCode;
    }
    
    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}