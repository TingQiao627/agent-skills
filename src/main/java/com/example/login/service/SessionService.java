package com.example.login.service;

import com.example.login.dto.LoginResponse;
import com.example.login.entity.Session;
import com.example.login.entity.User;
import com.example.login.repository.SessionRepository;
import com.example.login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    
    private final SessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public Session createSession(User user, String deviceType, String deviceName, 
                                  String deviceIp, String userAgent) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        LocalDateTime expireTime = LocalDateTime.now()
                .plusMillis(jwtUtil.getRefreshTokenExpireTime());
        
        Session session = Session.builder()
                .userId(user.getId())
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .deviceType(deviceType)
                .deviceName(deviceName)
                .deviceIp(deviceIp)
                .userAgent(userAgent)
                .expireTime(expireTime)
                .build();
        
        return sessionRepository.save(session);
    }
    
    public Optional<Session> findByRefreshToken(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken);
    }
    
    @Transactional
    public LoginResponse refreshAccessToken(String refreshToken) {
        Session session = findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("无效的刷新令牌"));
        
        if (session.getExpireTime().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            throw new RuntimeException("刷新令牌已过期");
        }
        
        String newAccessToken = jwtUtil.generateAccessToken(session.getUserId(), null);
        session.setAccessToken(newAccessToken);
        sessionRepository.save(session);
        
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .accessExpireTime(System.currentTimeMillis() + jwtUtil.getAccessTokenExpireTime())
                .refreshExpireTime(System.currentTimeMillis() + jwtUtil.getRefreshTokenExpireTime())
                .build();
    }
    
    @Transactional
    public void invalidateSession(String refreshToken) {
        sessionRepository.deleteByRefreshToken(refreshToken);
    }
    
    @Transactional
    public void invalidateAllSessions(Long userId) {
        sessionRepository.deleteByUserId(userId);
    }
    
    @Transactional
    public void invalidateOtherSessions(Long userId, String currentRefreshToken) {
        sessionRepository.deleteOtherSessions(userId, currentRefreshToken);
    }
    
    public List<Session> getSessionsByUserId(Long userId) {
        return sessionRepository.findByUserId(userId);
    }
    
    @Transactional
    public void cleanExpiredSessions() {
        sessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }
}