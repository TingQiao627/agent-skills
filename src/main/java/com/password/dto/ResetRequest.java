package com.password.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 密码重置请求DTO
 */
public class ResetRequest {

    @NotBlank(message = "邮箱或手机号不能为空")
    private String contact; // 可以是邮箱或手机号

    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$|^1[3-9]\\d{9}$", 
             message = "请输入有效的邮箱地址或手机号")
    private String emailOrPhone;

    @Size(min = 8, max = 20, message = "密码长度必须在8-20个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
             message = "密码必须包含大小写字母和数字")
    private String newPassword;

    private String token; // 重置令牌

    // 构造函数
    public ResetRequest() {}

    public ResetRequest(String emailOrPhone, String newPassword) {
        this.emailOrPhone = emailOrPhone;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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