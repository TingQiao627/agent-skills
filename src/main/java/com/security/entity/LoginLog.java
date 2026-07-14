package com.security.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 登录日志实体
 * 记录用户每次登录的详细信息
 */
@Entity
@Table(name = "login_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_login_time", columnList = "login_time"),
    @Index(name = "idx_status", columnList = "status")
})
public class LoginLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;
    
    @Column(name = "ip", length = 45, nullable = false)
    private String ip;
    
    @Column(name = "location", length = 100)
    private String location;
    
    @Column(name = "device", length = 255)
    private String device;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status; // SUCCESS, FAILED, LOCKED
    
    @Column(name = "failure_reason", length = 255)
    private String failureReason;
    
    // 默认构造函数
    public LoginLog() {}
    
    // 构造函数
    public LoginLog(Long userId, String ip, String status) {
        this.userId = userId;
        this.ip = ip;
        this.status = status;
        this.loginTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getDevice() {
        return device;
    }
    
    public void setDevice(String device) {
        this.device = device;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}