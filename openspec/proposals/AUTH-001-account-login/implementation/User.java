package com.example.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 支持用户名/邮箱/手机号三种登录标识
 * 
 * 规格参考：OPSX.md F1.1 多字段支持
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_phone", columnList = "phone", unique = true)
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名
     * 验证规则: 3-20字符，字母数字下划线
     */
    @Column(unique = true, length = 20)
    private String username;
    
    /**
     * 邮箱
     * 标准邮箱格式验证
     */
    @Column(unique = true, length = 100)
    private String email;
    
    /**
     * 手机号
     * 11位数字，支持国际区号
     */
    @Column(unique = true, length = 20)
    private String phone;
    
    /**
     * 密码（bcrypt/argon2 加密）
     */
    @Column(nullable = false, length = 255)
    private String passwordHash;
    
    /**
     * 用户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    /**
     * 连续登录失败次数
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer loginFailCount = 0;
    
    /**
     * 账号锁定到期时间
     */
    private LocalDateTime lockedUntil;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 最后登录IP
     */
    @Column(length = 45)
    private String lastLoginIp;
    
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE,       // 正常
        LOCKED,       // 锁定
        DISABLED,     // 禁用
        PENDING       // 待激活
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查账号是否锁定
     */
    public boolean isAccountLocked() {
        if (status != UserStatus.LOCKED) {
            return false;
        }
        if (lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
            // 锁定已过期，自动解锁
            return false;
        }
        return true;
    }
    
    /**
     * 重置登录失败计数
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.lockedUntil = null;
        if (this.status == UserStatus.LOCKED) {
            this.status = UserStatus.ACTIVE;
        }
    }
    
    /**
     * 增加登录失败计数
     */
    public void incrementLoginFailCount() {
        this.loginFailCount++;
    }
}