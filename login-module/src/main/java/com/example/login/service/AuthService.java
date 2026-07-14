package com.example.login.service;

import com.example.login.dto.LoginRequest;
import com.example.login.dto.LoginResponse;
import com.example.login.dto.SmsLoginRequest;
import com.example.login.entity.User;
import com.example.login.repository.UserRepository;
import com.example.login.security.JwtTokenProvider;
import com.example.login.service.security.LoginSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 * F1 账号密码登录 + F2 手机验证码登录
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginSecurityService loginSecurityService;
    private final SmsService smsService;
    
    /**
     * 账号密码登录
     * F1: 多字段支持、加密传输、记住密码
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();
        
        // F4: 检查登录锁定状态
        if (loginSecurityService.isAccountLocked(account)) {
            throw new LoginLockedException("账号已被锁定，请稍后重试");
        }
        
        // F4: 验证图形验证码
        if (request.getCaptchaKey() != null) {
            loginSecurityService.validateCaptcha(request.getCaptchaKey(), request.getCaptchaCode());
        }
        
        // 多字段登录：支持用户名、手机号、邮箱
        User user = userRepository.findByUsername(account)
                .or(() -> userRepository.findByPhone(account))
                .or(() -> userRepository.findByEmail(account))
                .orElseThrow(() -> new LoginFailedException("账号或密码错误"));
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginSecurityService.recordLoginFailure(account);
            throw new LoginFailedException("账号或密码错误");
        }
        
        // 检查账号是否启用
        if (!user.getEnabled()) {
            throw new LoginFailedException("账号已被禁用");
        }
        
        // 重置登录失败次数
        loginSecurityService.resetLoginFailure(account);
        
        // F5: 生成JWT双Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
        
        return buildLoginResponse(user, accessToken, refreshToken, request.getRememberMe());
    }
    
    /**
     * 手机验证码登录
     * F2: 短信验证码、自动注册
     */
    @Transactional
    public LoginResponse smsLogin(SmsLoginRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        
        // 验证短信验证码
        smsService.validateSmsCode(phone, code);
        
        // 自动注册：手机号不存在则自动创建账号
        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> autoRegisterUser(phone));
        
        // 生成Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
        
        return buildLoginResponse(user, accessToken, refreshToken, false);
    }
    
    /**
     * 自动注册用户
     * F2: 验证码登录自动注册
     */
    private User autoRegisterUser(String phone) {
        User user = new User();
        user.setUsername("user_" + phone.substring(7));
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode("DEFAULT_PASSWORD_" + System.currentTimeMillis()));
        user.setEnabled(true);
        return userRepository.save(user);
    }
    
    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken, boolean rememberMe) {
        long expiresIn = rememberMe ? 604800000L : 7200000L;
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}