package com.auth.application.service;

import com.auth.application.dto.LoginResponse;
import com.auth.application.dto.SmsLoginRequest;
import com.auth.domain.entity.User;
import com.auth.domain.repository.UserRepository;
import com.auth.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SmsAuthService {
    private final UserRepository userRepository;
    private final SmsCodeService smsCodeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public LoginResponse smsLogin(SmsLoginRequest request, String ip) {
        // 验证短信验证码
        if (!smsCodeService.verifyCode(request.getPhone(), request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        // 查找或创建用户（自动注册）
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> createNewUser(request.getPhone()));
        
        // 生成Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // 更新最后登录信息
        user.setLastLoginIp(ip);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200L)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .build())
                .build();
    }
    
    private User createNewUser(String phone) {
        String randomUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);
        User user = User.builder()
                .username(randomUsername)
                .phone(phone)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .loginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }
}