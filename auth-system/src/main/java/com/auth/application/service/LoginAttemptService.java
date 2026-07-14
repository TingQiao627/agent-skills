package com.auth.application.service;

import com.auth.domain.entity.User;
import com.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final UserRepository userRepository;
    
    @Value("${security.login.max-attempts:5}")
    private Integer maxAttempts;
    
    @Value("${security.login.lock-duration:900000}")
    private Long lockDuration;
    
    public boolean isLocked(User user) {
        if (user.getLockedUntil() == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(user.getLockedUntil());
    }
    
    public void recordFailedAttempt(User user) {
        user.setLoginAttempts(user.getLoginAttempts() + 1);
        
        if (user.getLoginAttempts() >= maxAttempts) {
            user.setLockedUntil(LocalDateTime.now().plusSeconds(lockDuration / 1000));
        }
        
        userRepository.save(user);
    }
    
    public void resetAttempts(User user) {
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
}