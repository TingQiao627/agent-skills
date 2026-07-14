package com.example.login.service;

import com.example.login.entity.User;
import com.example.login.repository.UserRepository;
import com.example.login.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;
    
    public Optional<User> findByLoginField(String loginField) {
        return userRepository.findByLoginField(loginField);
    }
    
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    @Transactional
    public User createPasswordUser(String username, String password, String phone, String email) {
        User user = User.builder()
                .username(username)
                .password(passwordUtil.encode(password))
                .phone(phone)
                .email(email)
                .loginType(User.LoginType.PASSWORD)
                .status(User.UserStatus.NORMAL)
                .build();
        return userRepository.save(user);
    }
    
    @Transactional
    public User createPhoneUser(String phone) {
        User user = User.builder()
                .phone(phone)
                .password(passwordUtil.encode(java.util.UUID.randomUUID().toString()))
                .loginType(User.LoginType.PHONE)
                .status(User.UserStatus.NORMAL)
                .build();
        return userRepository.save(user);
    }
    
    public boolean checkPassword(User user, String rawPassword) {
        return passwordUtil.matches(rawPassword, user.getPassword());
    }
    
    @Transactional
    public void updateLoginSuccess(User user, String loginIp) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userRepository.save(user);
    }
    
    @Transactional
    public void incrementLoginFailCount(User user) {
        int failCount = user.getLoginFailCount() + 1;
        user.setLoginFailCount(failCount);
        
        if (failCount >= 5) {
            user.setStatus(User.UserStatus.LOCKED);
            user.setLockTime(LocalDateTime.now());
            log.warn("User {} is locked due to too many failed login attempts", user.getId());
        }
        
        userRepository.save(user);
    }
    
    @Transactional
    public void unlockUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(User.UserStatus.NORMAL);
            user.setLoginFailCount(0);
            user.setLockTime(null);
            userRepository.save(user);
        });
    }
    
    public boolean isUserLocked(User user) {
        return user.getStatus() == User.UserStatus.LOCKED;
    }
    
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}