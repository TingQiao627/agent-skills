package com.password.repository;

import com.password.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 密码重置令牌Repository
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * 根据令牌查找
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * 根据用户ID查找
     */
    Optional<PasswordResetToken> findByUserId(Long userId);

    /**
     * 查找用户的有效令牌
     */
    Optional<PasswordResetToken> findByUserIdAndUsedFalseAndExpireTimeAfter(Long userId, LocalDateTime now);

    /**
     * 删除过期的令牌
     */
    void deleteByExpireTimeBefore(LocalDateTime expireTime);

    /**
     * 标记用户的所有令牌为已使用
     */
    void markAllAsUsedByUserId(Long userId);
}