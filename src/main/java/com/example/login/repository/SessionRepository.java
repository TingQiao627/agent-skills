package com.example.login.repository;

import com.example.login.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    Optional<Session> findByRefreshToken(String refreshToken);
    
    List<Session> findByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM Session s WHERE s.userId = :userId")
    void deleteByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM Session s WHERE s.refreshToken = :refreshToken")
    void deleteByRefreshToken(String refreshToken);
    
    @Modifying
    @Query("DELETE FROM Session s WHERE s.userId = :userId AND s.refreshToken != :currentToken")
    void deleteOtherSessions(Long userId, String currentToken);
    
    @Modifying
    @Query("DELETE FROM Session s WHERE s.expireTime < :now")
    void deleteExpiredSessions(LocalDateTime now);
    
    boolean existsByUserIdAndDeviceIp(Long userId, String deviceIp);
}