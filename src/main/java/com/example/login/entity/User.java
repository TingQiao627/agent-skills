package com.example.login.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, length = 50)
    private String username;
    
    @Column(unique = true, length = 20)
    private String phone;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(length = 100)
    private String nickname;
    
    @Column(length = 255)
    private String avatar;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    private LocalDateTime lastLoginTime;
    
    private String lastLoginIp;
    
    private Integer loginFailCount;
    
    private LocalDateTime lockTime;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
        if (loginType == null) {
            loginType = LoginType.PASSWORD;
        }
        if (status == null) {
            status = UserStatus.NORMAL;
        }
        if (loginFailCount == null) {
            loginFailCount = 0;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    public enum LoginType {
        PASSWORD, PHONE, WECHAT, ALIPAY, ENTERPRISE_WECHAT
    }
    
    public enum UserStatus {
        NORMAL, LOCKED, DISABLED
    }
}