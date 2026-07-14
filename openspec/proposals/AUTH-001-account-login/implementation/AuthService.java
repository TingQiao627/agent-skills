package com.example.auth.service;

import com.example.auth.dto.LoginResponse;
import com.example.auth.entity.User;
import com.example.auth.entity.Session;
import com.example.auth.entity.SecurityLog;
import com.example.auth.repository.UserRepository;
import com.example.auth.repository.SessionRepository;
import com.example.auth.repository.SecurityLogRepository;
import com.example.auth.security.JwtTokenProvider;
import com.example.auth.security.AuthPasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 * 
 * 规格参考：OPSX.md F1 账号密码登录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SecurityLogRepository securityLogRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthPasswordEncoder passwordEncoder;
    
    /**
     * 登录锁定阈值（可配置）
     */
    private static final int LOGIN_LOCK_THRESHOLD = 5;
    
    /**
     * 账号密码登录
     */
    @Transactional
    public LoginResponse loginWithPassword(
            String identifier,
            String encryptedPassword,
            Boolean rememberMe,
            String captchaToken,
            String ipAddress,
            String deviceFingerprint) {
        
        // 1. 根据登录标识查找用户
        User user = findUserByIdentifier(identifier);
        
        if (user == null) {
            log.warn("Login failed: user not found for identifier: {}", identifier);
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 2. 检查账号是否锁定
        if (user.isAccountLocked()) {
            log.warn("Account locked for user: {}", user.getId());
            throw new RuntimeException("账号已被锁定，请稍后再试");
        }
        
        // 3. 解密密码（实际项目中应使用 RSA 解密）
        String password = encryptedPassword; // 简化，实际需解密
        
        // 4. 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            handleLoginFailure(user, ipAddress);
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 5. 登录成功
        return handleLoginSuccess(user, ipAddress, deviceFingerprint, rememberMe);
    }
    
    /**
     * Token 刷新
     */
    @Transactional
    public LoginResponse refreshToken(String refreshToken, String deviceFingerprint) {
        // 1. 验证 Refresh Token
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String jti = jwtTokenProvider.getJtiFromToken(refreshToken);
        
        // 2. 查找会话
        Session session = sessionRepository.findByRefreshTokenJti(jti)
            .orElseThrow(() -> new RuntimeException("无效的会话"));
        
        if (!session.isValid()) {
            throw new RuntimeException("会话已过期或已撤销");
        }
        
        // 3. 检查设备指纹（可选）
        if (deviceFingerprint != null && 
            !deviceFingerprint.equals(session.getDeviceFingerprint())) {
            log.warn("Device fingerprint mismatch for session: {}", jti);
            // 可选：记录安全日志
        }
        
        // 4. 生成新的 Access Token
        User user = session.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user, deviceFingerprint);
        
        // 5. 更新会话活跃时间
        session.updateLastActive();
        
        return LoginResponse.builder()
            .accessToken(newAccessToken)
            .expiresIn(jwtTokenProvider.getAccessTokenExpireSeconds())
            .userInfo(buildUserInfo(user))
            .build();
    }
    
    /**
     * 登出
     */
    @Transactional
    public void logout(String accessToken, boolean global) {
        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
        
        if (global) {
            // 全局登出：撤销所有会话
            sessionRepository.revokeAllByUserId(userId);
            log.info("Global logout for user: {}", userId);
        } else {
            // 单设备登出：仅撤销当前会话
            String jti = jwtTokenProvider.getJtiFromToken(accessToken);
            sessionRepository.findByRefreshTokenJti(jti)
                .ifPresent(Session::revoke);
            log.info("Single logout for user: {}", userId);
        }
    }
    
    /**
     * 根据登录标识查找用户
     */
    private User findUserByIdentifier(String identifier) {
        // 尝试作为用户名查找
        if (identifier.matches("^[a-zA-Z0-9_]{3,20}$")) {
            return userRepository.findByUsername(identifier).orElse(null);
        }
        
        // 尝试作为邮箱查找
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier).orElse(null);
        }
        
        // 尝试作为手机号查找
        if (identifier.matches("^\\+?\\d{10,15}$")) {
            return userRepository.findByPhone(identifier).orElse(null);
        }
        
        return null;
    }
    
    /**
     * 处理登录失败
     */
    private void handleLoginFailure(User user, String ipAddress) {
        user.incrementLoginFailCount();
        
        // 检查是否需要锁定
        if (user.getLoginFailCount() >= LOGIN_LOCK_THRESHOLD) {
            user.setStatus(User.UserStatus.LOCKED);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            log.warn("Account locked for user: {} after {} failed attempts", 
                user.getId(), user.getLoginFailCount());
        }
        
        userRepository.save(user);
        
        // 记录安全日志
        logSecurityEvent(user, SecurityLog.ActionType.LOGIN, ipAddress, 
            SecurityLog.RiskLevel.MEDIUM, "Login failed");
    }
    
    /**
     * 处理登录成功
     */
    private LoginResponse handleLoginSuccess(
            User user, 
            String ipAddress, 
            String deviceFingerprint,
            Boolean rememberMe) {
        
        // 1. 重置失败计数
        user.resetLoginFailCount();
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userRepository.save(user);
        
        // 2. 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(user, deviceFingerprint);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), deviceFingerprint);
        
        // 3. 创建会话
        Session session = Session.builder()
            .user(user)
            .refreshTokenJti(jwtTokenProvider.getJtiFromToken(refreshToken))
            .deviceFingerprint(deviceFingerprint)
            .ipAddress(ipAddress)
            .expiresAt(LocalDateTime.now().plusSeconds(
                jwtTokenProvider.getRefreshTokenExpireSeconds()))
            .build();
        sessionRepository.save(session);
        
        // 4. 记录安全日志
        logSecurityEvent(user, SecurityLog.ActionType.LOGIN, ipAddress, 
            SecurityLog.RiskLevel.LOW, "Login successful");
        
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(rememberMe ? refreshToken : null)
            .expiresIn(jwtTokenProvider.getAccessTokenExpireSeconds())
            .userInfo(buildUserInfo(user))
            .build();
    }
    
    /**
     * 记录安全日志
     */
    private void logSecurityEvent(User user, SecurityLog.ActionType action, 
            String ip, SecurityLog.RiskLevel riskLevel, String details) {
        SecurityLog log = SecurityLog.builder()
            .user(user)
            .action(action)
            .ip(ip)
            .riskLevel(riskLevel)
            .details(details)
            .build();
        securityLogRepository.save(log);
    }
    
    /**
     * 构建用户信息 DTO
     */
    private LoginResponse.UserInfo buildUserInfo(User user) {
        return LoginResponse.UserInfo.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .phone(user.getPhone())
            .build();
    }
}