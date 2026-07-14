package com.oauth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * OAuth账号绑定实体
 * 用于存储用户与第三方OAuth账号的绑定关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "oauth_binding", 
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_provider_user", 
                              columnNames = {"provider", "provider_user_id"}),
            @UniqueConstraint(name = "uk_user_provider", 
                              columnNames = {"user_id", "provider"})
        })
public class OAuthBinding {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 系统用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * OAuth提供商 (wechat, alipay, wecom等)
     */
    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    /**
     * 第三方平台用户唯一标识
     */
    @Column(name = "provider_user_id", nullable = false, length = 128)
    private String providerUserId;

    /**
     * 第三方平台的访问令牌(可选，用于后续API调用)
     */
    @Column(name = "access_token", length = 512)
    private String accessToken;

    /**
     * 刷新令牌(可选)
     */
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    /**
     * 绑定时间
     */
    @Column(name = "bind_time", nullable = false)
    private LocalDateTime bindTime;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 绑定状态 (0-已解绑, 1-已绑定)
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 插入前自动填充
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (bindTime == null) {
            bindTime = LocalDateTime.now();
        }
    }

    /**
     * 更新前自动填充
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}