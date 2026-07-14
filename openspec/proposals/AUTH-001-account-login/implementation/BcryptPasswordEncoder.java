package com.example.auth.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Bcrypt 密码编码器（备选方案）
 * 
 * 规格参考：DECISIONS.md D2
 * Bcrypt 生态成熟，多数框架原生支持
 */
@Slf4j
@Component
public class BcryptPasswordEncoder implements AuthPasswordEncoder {
    
    private final BCryptPasswordEncoder encoder;
    
    /**
     * bcrypt cost 因素（默认12）
     * 值越大越安全，但计算时间越长
     */
    private static final int COST = 12;
    
    public BcryptPasswordEncoder() {
        this.encoder = new BCryptPasswordEncoder(COST);
    }
    
    @Override
    public String encode(CharSequence rawPassword) {
        try {
            String hash = encoder.encode(rawPassword);
            log.debug("Password encoded successfully using bcrypt");
            return hash;
        } catch (Exception e) {
            log.error("Failed to encode password with bcrypt", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            boolean matches = encoder.matches(rawPassword, encodedPassword);
            log.debug("Password verification result: {}", matches);
            return matches;
        } catch (Exception e) {
            log.error("Failed to verify password with bcrypt", e);
            return false;
        }
    }
}