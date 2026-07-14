package com.auth.repository;

import com.auth.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 登录日志 Repository
 */
@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
}