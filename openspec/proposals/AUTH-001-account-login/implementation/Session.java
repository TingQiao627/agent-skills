package com.example.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 会话实体类
 * 管理 Refresh Token 和会话状态
 * 
 * 规格参考：OPSX.md F5 会话管理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_token", columnList = "refresh_token_jti", unique = true)
})
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Refresh Token 唯一标识
     */
    @Column(name = "refresh_token_jti", unique = true, nullable = false, length = 64)
    private String refreshTokenJti;
    
    /**
     * 设备指纹
     */
    @Column(length = 128)
    private String deviceFingerprint;
    
    /**
     * 设备信息（User-Agent）
     */
    @Column(length = 500)
    private String deviceInfo;
    
    /**
     * 登录IP
     */
    @Column(length = 45)
    private String ipAddress;
    
    /**
     * 地理位置（基于IP解析）
     */
    @Column(length = 100)
    private String location;
    
    /**
     * 会话状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
    
    /**
     * 登录时间
     */
    @Column(nullable = false)
    private LocalDateTime loginTime;
    
    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;
    
    /**
     * 过期时间
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * 会话状态枚举
     */
    public enum SessionStatus {
        ACTIVE,     // 活跃
        EXPIRED,    // 已过期
        REVOKED     // 已撤销
    }
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = SessionStatus.ACTIVE;
        }
        loginTime = LocalDateTime.now();
        lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * 检查会话是否有效
     */
    public boolean isValid() {
        return status == SessionStatus.ACTIVE 
            && expiresAt.isAfter(LocalDateTime.now());
    }
    
    /**
     * 撤销会话
     */
    public void revoke() {
        this.status = SessionStatus.REVOKED;
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateLastActive() {
        this.lastActiveTime = LocalDateTime.now();
    }
}