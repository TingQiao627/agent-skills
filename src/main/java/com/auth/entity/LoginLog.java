package com.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 登录日志实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "login_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_login_time", columnList = "login_time")
})
public class LoginLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username", length = 50)
    private String username;
    
    /**
     * 登录类型: password/phone/oauth
     */
    @Column(name = "login_type", length = 20)
    private String loginType;
    
    /**
     * 结果: 0-失败, 1-成功
     */
    @Column(name = "login_result")
    private Integer loginResult;
    
    @Column(name = "fail_reason", length = 100)
    private String failReason;
    
    @Column(name = "login_ip", length = 50)
    private String loginIp;
    
    @Column(name = "login_location", length = 100)
    private String loginLocation;
    
    @Column(name = "device_info", length = 255)
    private String deviceInfo;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;
    
    @PrePersist
    protected void onCreate() {
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
    }
}