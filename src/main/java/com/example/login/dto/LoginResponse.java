package com.example.login.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String accessToken;
    
    private String refreshToken;
    
    private Long accessExpireTime;
    
    private Long refreshExpireTime;
    
    private UserInfo userInfo;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String phone;
        private String email;
    }
}