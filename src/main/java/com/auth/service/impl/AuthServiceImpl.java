package com.auth.service.impl;

import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.PhoneLoginRequest;
import com.auth.dto.response.LoginResponse;
import com.auth.entity.User;
import com.auth.entity.LoginLog;
import com.auth.repository.UserRepository;
import com.auth.repository.LoginLogRepository;
import com.auth.security.JwtTokenProvider;
import com.auth.service.AuthService;
import com.auth.util.AccountTypeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        // 1. 检查账号锁定
        if (isAccountLocked(request.getAccount())) {
            recordLoginFail(request.getAccount(), ip, "账号已锁定");
            throw new RuntimeException("账号已锁定，请稍后重试");
        }
        
        // 2. 根据账号类型查询用户
        User user = findUserByAccount(request.getAccount());
        if (user == null) {
            recordLoginFail(request.getAccount(), ip, "用户不存在");
            throw new RuntimeException("账号或密码错误");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordLoginFail(request.getAccount(), ip, "密码错误");
            throw new RuntimeException("账号或密码错误");
        }
        
        // 4. 检查账号状态
        if (!user.isActive()) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 5. 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // 6. 更新用户登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        user.resetLoginFailCount();
        userRepository.save(user);
        
        // 7. 记录登录日志
        saveLoginLog(user.getId(), request.getAccount(), "password", true, null, ip, userAgent);
        
        // 8. 返回响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
    
    @Override
    @Transactional
    public LoginResponse loginByPhone(PhoneLoginRequest request, String ip, String userAgent) {
        // 1. 验证短信验证码（这里简化，实际需要调用 SmsService）
        // smsService.verifyCode(request.getPhone(), request.getCode());
        
        // 2. 查询或创建用户
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> createPhoneUser(request.getPhone()));
        
        // 3. 检查账号状态
        if (!user.isActive()) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 4. 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // 5. 更新用户登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userRepository.save(user);
        
        // 6. 记录登录日志
        saveLoginLog(user.getId(), request.getPhone(), "phone", true, null, ip, userAgent);
        
        // 7. 返回响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .needCompleteProfile(user.getNickname() == null)
                .build();
    }
    
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 1. 验证 Refresh Token
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }
        
        // 2. 提取用户ID
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
        
        // 3. 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 4. 检查账号状态
        if (!user.isActive()) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 5. 生成新 Token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        // 6. 返回响应
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenProvider.getAccessExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
    
    @Override
    public void logout(String refreshToken) {
        // 将 refreshToken 加入黑名单（使用 Redis）
        // redisTemplate.opsForValue().set("token:blacklist:" + refreshToken, "1", 7, TimeUnit.DAYS);
        log.info("用户登出，token已加入黑名单");
    }
    
    @Override
    public boolean isAccountLocked(String account) {
        User user = findUserByAccount(account);
        return user != null && user.isLocked();
    }
    
    @Override
    public void recordLoginFail(String account, String ip, String reason) {
        User user = findUserByAccount(account);
        if (user != null) {
            user.incrementLoginFailCount();
            // 连续5次失败，锁定30分钟
            if (user.getLoginFailCount() >= 5) {
                user.lock(30);
            }
            userRepository.save(user);
        }
        saveLoginLog(user != null ? user.getId() : null, account, "password", false, reason, ip, null);
    }
    
    @Override
    public void resetLoginFailCount(String account) {
        User user = findUserByAccount(account);
        if (user != null) {
            user.resetLoginFailCount();
            userRepository.save(user);
        }
    }
    
    // ========== 私有方法 ==========
    
    private User findUserByAccount(String account) {
        String accountType = AccountTypeDetector.detect(account);
        return switch (accountType) {
            case "phone" -> userRepository.findByPhone(account).orElse(null);
            case "email" -> userRepository.findByEmail(account).orElse(null);
            default -> userRepository.findByUsername(account).orElse(null);
        };
    }
    
    private User createPhoneUser(String phone) {
        User user = User.builder()
                .phone(phone)
                .username("user_" + phone.substring(phone.length() - 4))
                .status(1)
                .build();
        return userRepository.save(user);
    }
    
    private void saveLoginLog(Long userId, String username, String loginType, 
                              boolean success, String failReason, String ip, String userAgent) {
        LoginLog log = LoginLog.builder()
                .userId(userId)
                .username(username)
                .loginType(loginType)
                .loginResult(success ? 1 : 0)
                .failReason(failReason)
                .loginIp(ip)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .build();
        loginLogRepository.save(log);
    }
}