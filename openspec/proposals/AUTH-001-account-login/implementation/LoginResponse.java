package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 * 
 * 规格参考：OPSX.md F1 接口规格
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * Access Token
     */
    private String accessToken;
    
    /**
     * Refresh Token（可选）
     */
    private String refreshToken;
    
    /**
     * 过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    /**
     * 是否新用户（用于短信登录）
     */
    private Boolean isNewUser;
    
    /**
     * 用户信息 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String avatar;
    }
}