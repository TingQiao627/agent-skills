package com.example.auth.security;

import org.springframework.stereotype.Component;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import lombok.extern.slf4j.Slf4j;

/**
 * Argon2 密码编码器
 * 
 * 规格参考：DECISIONS.md D2
 * Argon2 是当前最安全的密码哈希算法（Password Hashing Competition 冠军）
 */
@Slf4j
@Component
public class Argon2PasswordEncoder implements AuthPasswordEncoder {
    
    private final Argon2 argon2;
    
    /**
     * Argon2 配置参数（可配置化）
     * 这些参数应根据硬件性能调整
     */
    private static final int SALT_LENGTH = 16;      // 盐值长度（字节）
    private static final int HASH_LENGTH = 32;      // 哈希长度（字节）
    private static final int ITERATIONS = 10;       // 迭代次数
    private static final int MEMORY = 65536;        // 内存成本（KB）
    private static final int PARALLELISM = 1;       // 并行度
    
    public Argon2PasswordEncoder() {
        this.argon2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id,  // 推荐 ARGON2id 变体
            SALT_LENGTH,
            HASH_LENGTH
        );
    }
    
    @Override
    public String encode(CharSequence rawPassword) {
        try {
            String hash = argon2.hash(
                ITERATIONS,
                MEMORY,
                PARALLELISM,
                rawPassword.toString().toCharArray()
            );
            log.debug("Password encoded successfully using Argon2");
            return hash;
        } catch (Exception e) {
            log.error("Failed to encode password with Argon2", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            boolean matches = argon2.verify(
                encodedPassword,
                rawPassword.toString().toCharArray()
            );
            log.debug("Password verification result: {}", matches);
            return matches;
        } catch (Exception e) {
            log.error("Failed to verify password with Argon2", e);
            return false;
        }
    }
}