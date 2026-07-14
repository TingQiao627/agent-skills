package com.example.auth.repository;

import com.example.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会话数据访问层
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    Optional<Session> findByRefreshTokenJti(String refreshTokenJti);
    
    List<Session> findByUserIdAndStatus(Long userId, Session.SessionStatus status);
    
    @Modifying
    @Query("UPDATE Session s SET s.status = 'REVOKED' WHERE s.user.id = :userId")
    void revokeAllByUserId(Long userId);
}