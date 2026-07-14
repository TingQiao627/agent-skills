package com.example.login.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, unique = true, length = 500)
    private String refreshToken;
    
    @Column(nullable = false)
    private String accessToken;
    
    @Column(length = 50)
    private String deviceType;
    
    @Column(length = 200)
    private String deviceName;
    
    @Column(length = 100)
    private String deviceIp;
    
    @Column(length = 255)
    private String userAgent;
    
    @Column(nullable = false)
    private LocalDateTime expireTime;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}