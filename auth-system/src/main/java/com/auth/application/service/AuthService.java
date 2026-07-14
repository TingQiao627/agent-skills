package com.auth.application.service;

import com.auth.application.dto.LoginRequest;
import com.auth.application.dto.LoginResponse;
import com.auth.domain.entity.User;
import com.auth.domain.repository.UserRepository;
import com.auth.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    
    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        // 查找用户（支持用户名/邮箱/手机号）
        User user = findUserByAccount(request.getAccount())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查账号锁定
        if (loginAttemptService.isLocked(user)) {
            throw new RuntimeException("账号已锁定，请稍后再试");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailedAttempt(user);
            throw new RuntimeException("密码错误");
        }
        
        // 登录成功，重置失败次数
        loginAttemptService.resetAttempts(user);
        
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
    
    private Optional<User> findUserByAccount(String account) {
        if (account.contains("@")) {
            return userRepository.findByEmail(account);
        } else if (account.matches("\\d{11}")) {
            return userRepository.findByPhone(account);
        } else {
            return userRepository.findByUsername(account);
        }
    }
}