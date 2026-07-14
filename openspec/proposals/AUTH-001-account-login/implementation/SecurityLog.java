package com.example.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 安全日志实体
 * 记录登录、绑定、解绑等安全相关操作
 * 
 * 规格参考：OPSX.md F4.5 异地登录提醒
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "security_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class SecurityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;
    
    /**
     * 操作IP
     */
    @Column(length = 45)
    private String ip;
    
    /**
     * 地理位置
     */
    @Column(length = 100)
    private String location;
    
    /**
     * 设备指纹
     */
    @Column(length = 128)
    private String deviceFingerprint;
    
    /**
     * User-Agent
     */
    @Column(length = 500)
    private String userAgent;
    
    /**
     * 风险等级
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.LOW;
    
    /**
     * 操作详情（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String details;
    
    /**
     * 操作时间
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * 操作类型枚举
     */
    public enum ActionType {
        LOGIN,          // 登录
        LOGOUT,         // 登出
        BIND,           // 绑定第三方账号
        UNBIND,         // 解绑第三方账号
        PASSWORD_RESET, // 密码重置
        LOCK_ACCOUNT,   // 账号锁定
        UNLOCK_ACCOUNT  // 账号解锁
    }
    
    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW,      // 低风险
        MEDIUM,   // 中风险
        HIGH      // 高风险
    }
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}