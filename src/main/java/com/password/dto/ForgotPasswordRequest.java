package com.password.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 忘记密码请求DTO
 */
public class ForgotPasswordRequest {

    @NotBlank(message = "邮箱或手机号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$|^1[3-9]\\d{9}$", 
             message = "请输入有效的邮箱地址或手机号")
    private String emailOrPhone;

    // 构造函数
    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    // Getters and Setters
    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    /**
     * 判断是否为邮箱
     */
    public boolean isEmail() {
        return emailOrPhone != null && emailOrPhone.contains("@");
    }

    /**
     * 判断是否为手机号
     */
    public boolean isPhone() {
        return emailOrPhone != null && emailOrPhone.matches("^1[3-9]\\d{9}$");
    }
}