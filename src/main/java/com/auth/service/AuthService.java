package com.auth.service;

import com.auth.dto.LoginRequest;
import com.auth.dto.LoginResponse;
import com.auth.entity.User;
import com.auth.entity.UserStatus;
import com.auth.repository.UserRepository;
import com.auth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 根据登录字段查找用户（支持用户名/邮箱/手机号）
        Optional<User> userOptional = findUserByLoginField(request.getLogin_field());
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOptional.get();
        
        // 2. 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("用户账号已被禁用或锁定");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 4. 生成Token（根据是否记住密码设置不同的过期时间）
        String accessToken;
        String refreshToken;
        Long expiresIn;
        
        if (Boolean.TRUE.equals(request.getRemember_me())) {
            // 记住密码：使用延长过期时间的令牌
            accessToken = jwtTokenProvider.generateRememberMeToken(user.getId(), user.getUsername());
            refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
            expiresIn = jwtTokenProvider.getRememberMeExpirationInSeconds();
        } else {
            // 普通登录
            accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
            refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
            expiresIn = jwtTokenProvider.getAccessTokenExpirationInSeconds();
        }
        
        // 5. 构建响应
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone()
        );
        
        logger.info("用户登录成功: {}", user.getUsername());
        
        return new LoginResponse(accessToken, refreshToken, expiresIn, userInfo);
    }
    
    /**
     * 根据登录字段查找用户
     * 支持用户名、邮箱、手机号三种登录方式
     */
    private Optional<User> findUserByLoginField(String loginField) {
        // 尝试按用户名查询
        Optional<User> user = userRepository.findByUsername(loginField);
        if (user.isPresent()) {
            return user;
        }
        
        // 尝试按邮箱查询
        user = userRepository.findByEmail(loginField);
        if (user.isPresent()) {
            return user;
        }
        
        // 尝试按手机号查询
        return userRepository.findByPhone(loginField);
    }
    
    /**
     * 密码加密
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}