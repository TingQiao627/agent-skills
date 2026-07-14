package com.example.login.service;

import com.example.login.dto.*;
import com.example.login.entity.Session;
import com.example.login.entity.User;
import com.example.login.repository.SessionRepository;
import com.example.login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final SessionService sessionService;
    private final StringRedisTemplate redisTemplate;
    
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int DAILY_LIMIT = 10;
    
    @Transactional
    public LoginResponse login(LoginRequest request, String clientIp) {
        User user = userService.findByLoginField(request.getLoginField())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (userService.isUserLocked(user)) {
            throw new RuntimeException("账号已被锁定，请联系管理员");
        }
        
        if (!userService.checkPassword(user, request.getPassword())) {
            userService.incrementLoginFailCount(user);
            throw new RuntimeException("密码错误");
        }
        
        userService.updateLoginSuccess(user, clientIp);
        
        Session session = sessionService.createSession(user, request.getDeviceType(),
                request.getDeviceName(), clientIp, request.getUserAgent());
        
        return buildLoginResponse(user, session);
    }
    
    @Transactional
    public LoginResponse phoneLogin(PhoneLoginRequest request, String clientIp) {
        String cachedCode = redisTemplate.opsForValue().get(SMS_CODE_PREFIX + request.getPhone());
        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        User user = userService.findByPhone(request.getPhone())
                .orElseGet(() -> userService.createPhoneUser(request.getPhone()));
        
        userService.updateLoginSuccess(user, clientIp);
        redisTemplate.delete(SMS_CODE_PREFIX + request.getPhone());
        
        Session session = sessionService.createSession(user, request.getDeviceType(),
                request.getDeviceName(), clientIp, request.getUserAgent());
        
        return buildLoginResponse(user, session);
    }
    
    public void sendCode(SendCodeRequest request) {
        String limitKey = SMS_LIMIT_PREFIX + request.getPhone();
        String countStr = redisTemplate.opsForValue().get(limitKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        
        if (count >= DAILY_LIMIT) {
            throw new RuntimeException("今日发送次数已达上限");
        }
        
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        redisTemplate.opsForValue().set(SMS_CODE_PREFIX + request.getPhone(), code,
                CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        if (count == 0) {
            redisTemplate.opsForValue().set(limitKey, "1", 24, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(limitKey);
        }
        
        log.info("SMS code for {}: {}", request.getPhone(), code);
    }
    
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        return sessionService.refreshAccessToken(request.getRefreshToken());
    }
    
    @Transactional
    public void logout(String refreshToken) {
        sessionService.invalidateSession(refreshToken);
    }
    
    @Transactional
    public void logoutAll(Long userId) {
        sessionService.invalidateAllSessions(userId);
    }
    
    private LoginResponse buildLoginResponse(User user, Session session) {
        return LoginResponse.builder()
                .accessToken(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .accessExpireTime(System.currentTimeMillis() + sessionService.getJwtUtil().getAccessTokenExpireTime())
                .refreshExpireTime(System.currentTimeMillis() + sessionService.getJwtUtil().getRefreshTokenExpireTime())
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}