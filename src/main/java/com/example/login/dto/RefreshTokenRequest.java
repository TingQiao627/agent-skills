package com.example.login.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
    
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}