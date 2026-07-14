package com.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    
    @NotBlank(message = "登录字段不能为空")
    private String login_field;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private Boolean remember_me = false;
    
    private String captcha_key;
    
    private String captcha_code;
    
    // Constructors
    public LoginRequest() {}
    
    public LoginRequest(String login_field, String password) {
        this.login_field = login_field;
        this.password = password;
    }
    
    // Getters and Setters
    public String getLogin_field() {
        return login_field;
    }
    
    public void setLogin_field(String login_field) {
        this.login_field = login_field;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Boolean getRemember_me() {
        return remember_me;
    }
    
    public void setRemember_me(Boolean remember_me) {
        this.remember_me = remember_me;
    }
    
    public String getCaptcha_key() {
        return captcha_key;
    }
    
    public void setCaptcha_key(String captcha_key) {
        this.captcha_key = captcha_key;
    }
    
    public String getCaptcha_code() {
        return captcha_code;
    }
    
    public void setCaptcha_code(String captcha_code) {
        this.captcha_code = captcha_code;
    }
}