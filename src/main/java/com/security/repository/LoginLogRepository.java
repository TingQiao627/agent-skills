package com.security.repository;

import com.security.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志数据访问层
 */
@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    
    /**
     * 根据用户ID查询登录日志
     */
    List<LoginLog> findByUserIdOrderByLoginTimeDesc(Long userId);
    
    /**
     * 查询用户最近的登录日志
     */
    List<LoginLog> findTop10ByUserIdOrderByLoginTimeDesc(Long userId);
    
    /**
     * 统计指定时间段内用户的失败登录次数
     */
    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.userId = :userId " +
           "AND l.status = 'FAILED' AND l.loginTime >= :startTime")
    int countFailedLogins(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
    
    /**
     * 查询用户最近的成功登录记录（用于异地登录检测）
     */
    @Query("SELECT l FROM LoginLog l WHERE l.userId = :userId " +
           "AND l.status = 'SUCCESS' ORDER BY l.loginTime DESC LIMIT 1")
    LoginLog findLatestSuccessLogin(@Param("userId") Long userId);
    
    /**
     * 查询用户指定IP的登录记录
     */
    List<LoginLog> findByUserIdAndIp(Long userId, String ip);
    
    /**
     * 查询指定时间段内的所有登录日志
     */
    List<LoginLog> findByLoginTimeBetween(LocalDateTime start, LocalDateTime end);
}