package com.oauth.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * OAuth 用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthUserInfo {
    
    private String platform;
    
    private String oauthId;
    
    private String oauthName;
    
    private String oauthAvatar;
    
    private Long userId;
    
    private Boolean isNewUser;
}