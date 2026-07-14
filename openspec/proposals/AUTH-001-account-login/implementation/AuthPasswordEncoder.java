package com.example.auth.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码编码器接口
 * 
 * 规格参考：DECISIONS.md D2 - 优先使用 argon2，备选 bcrypt
 */
public interface AuthPasswordEncoder extends PasswordEncoder {
    
    /**
     * 检查密码是否匹配
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    @Override
    boolean matches(CharSequence rawPassword, String encodedPassword);
    
    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    @Override
    String encode(CharSequence rawPassword);
}