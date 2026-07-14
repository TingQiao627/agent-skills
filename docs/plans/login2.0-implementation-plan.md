# 账号登录 2.0 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建完整的账号登录 2.0 系统，支持账号密码、手机验证码、第三方 OAuth 三种登录方式，并提供安全防护、会话管理和密码找回功能。

**Architecture:** 采用 Spring Boot 3.x 微服务架构，前后端分离，JWT 双 Token 认证机制，Redis 缓存会话和验证码，MySQL 存储用户数据，消息队列处理异步通知。

**Tech Stack:** 
- 后端: Spring Boot 3.2, Spring Security 6, JWT (jjwt 0.12), MyBatis-Plus 3.5, Redis 7, MySQL 8.0
- 前端: Vue 3 + TypeScript + Vite
- 安全: BCrypt, AES-256, RSA-2048
- 第三方 SDK: 微信开放平台 SDK, 支付宝开放平台 SDK

---

## 全局约束

- Java 版本: JDK 17+
- Spring Boot 版本: 3.2.x
- 数据库: MySQL 8.0+, Redis 7.0+
- 命名规范: RESTful API, 驼峰命名, 包名小写
- 安全要求: 所有密码使用 BCrypt 加密, 敏感数据传输使用 RSA/AES 加密
- 日志规范: 使用 SLF4J + Logback, 敏感信息脱敏
- 测试覆盖: 核心业务逻辑单元测试覆盖率 ≥ 80%

---

## 子系统计划

由于系统规模较大且各子系统相对独立，按照模块化设计原则，分解为以下子计划：

| 子计划 | 文档路径 | 主要功能 | 优先级 |
|--------|---------|---------|--------|
| F1 账号密码登录 | `docs/plans/f1-account-login-plan.md` | 账号密码登录、加密传输、记住密码、自动登录 | P0 |
| F2 手机号验证码登录 | `docs/plans/f2-phone-login-plan.md` | 手机验证码登录、短信频率限制、自动注册 | P0 |
| F3 第三方 OAuth 登录 | `docs/plans/f3-oauth-login-plan.md` | 微信/支付宝/企业微信登录、账号绑定 | P1 |
| F4 安全防护 | `docs/plans/f4-security-plan.md` | 登录锁定、图形验证码、防暴力破解、CSRF、异地提醒 | P0 |
| F5 会话管理 | `docs/plans/f5-session-plan.md` | JWT 双 Token、静默刷新、全局登出 | P0 |
| F6 忘记密码 | `docs/plans/f6-password-reset-plan.md` | 身份验证、重置密码、通知 | P1 |

---

## 文件结构总览

```
src/main/java/com/example/login/
├── config/                          # 配置类
│   ├── SecurityConfig.java         # Spring Security 配置
│   ├── RedisConfig.java            # Redis 配置
│   ├── JwtConfig.java              # JWT 配置
│   └── OAuthConfig.java            # 第三方登录配置
├── controller/                      # 控制器层
│   ├── AuthController.java         # 认证控制器
│   ├── OAuthController.java        # 第三方登录控制器
│   ├── CaptchaController.java      # 验证码控制器
│   └── PasswordController.java     # 密码管理控制器
├── service/                         # 服务层
│   ├── AuthService.java            # 认证服务接口
│   ├── impl/
│   │   ├── AuthServiceImpl.java    # 认证服务实现
│   │   ├── OAuthServiceImpl.java   # 第三方登录实现
│   │   ├── CaptchaServiceImpl.java # 验证码服务实现
│   │   └── SessionServiceImpl.java # 会话服务实现
│   └── SmsService.java             # 短信服务接口
├── security/                        # 安全模块
│   ├── JwtTokenProvider.java       # JWT Token 提供者
│   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│   ├── LoginAttemptService.java    # 登录尝试服务
│   └── EncryptionService.java      # 加密服务
├── entity/                          # 实体类
│   ├── User.java                   # 用户实体
│   ├── UserOAuth.java              # 第三方账号绑定
│   ├── LoginLog.java               # 登录日志
│   └── Session.java                # 会话实体
├── dto/                             # 数据传输对象
│   ├── request/
│   │   ├── LoginRequest.java       # 登录请求
│   │   ├── PhoneLoginRequest.java  # 手机号登录请求
│   │   ├── OAuthRequest.java       # OAuth 登录请求
│   │   └── ResetPasswordRequest.java # 重置密码请求
│   └── response/
│   │   ├── LoginResponse.java      # 登录响应
│   │   └── UserInfoResponse.java   # 用户信息响应
├── mapper/                          # MyBatis Mapper
│   ├── UserMapper.java
│   ├── UserOAuthMapper.java
│   └── LoginLogMapper.java
├── exception/                       # 异常处理
│   ├── GlobalExceptionHandler.java
│   ├── AuthException.java
│   └── ErrorCode.java
└── util/                            # 工具类
    ├── IpUtils.java                # IP 工具
    ├── DeviceUtils.java            # 设备识别
    └── ValidationUtils.java        # 校验工具

src/main/resources/
├── application.yml                 # 主配置文件
├── application-dev.yml             # 开发环境配置
├── application-prod.yml            # 生产环境配置
└── mapper/                         # MyBatis XML
    ├── UserMapper.xml
    └── LoginLogMapper.xml

src/test/java/com/example/login/
├── controller/
│   ├── AuthControllerTest.java
│   └── OAuthControllerTest.java
├── service/
│   ├── AuthServiceTest.java
│   └── CaptchaServiceTest.java
└── security/
    └── JwtTokenProviderTest.java
```

---

## 数据库设计

### 用户表 (user)
```sql
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) COMMENT '密码(BCrypt)',
  `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常, 2-锁定',
  `locked_until` DATETIME COMMENT '锁定截止时间',
  `login_fail_count` INT DEFAULT 0 COMMENT '连续登录失败次数',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `last_login_device` VARCHAR(100) COMMENT '最后登录设备',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_phone` (`phone`),
  INDEX `idx_username` (`username`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 第三方账号绑定表 (user_oauth)
```sql
CREATE TABLE `user_oauth` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `oauth_type` VARCHAR(20) NOT NULL COMMENT '类型: wechat/alipay/wework',
  `oauth_id` VARCHAR(100) NOT NULL COMMENT '第三方用户ID',
  `oauth_name` VARCHAR(100) COMMENT '第三方用户名',
  `oauth_avatar` VARCHAR(255) COMMENT '第三方头像',
  `bind_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  UNIQUE KEY `uk_oauth_type_id` (`oauth_type`, `oauth_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方账号绑定表';
```

### 登录日志表 (login_log)
```sql
CREATE TABLE `login_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT COMMENT '用户ID',
  `username` VARCHAR(50) COMMENT '登录账号',
  `login_type` VARCHAR(20) COMMENT '登录类型: password/phone/oauth',
  `login_result` TINYINT COMMENT '结果: 0-失败, 1-成功',
  `fail_reason` VARCHAR(100) COMMENT '失败原因',
  `login_ip` VARCHAR(50) COMMENT '登录IP',
  `login_location` VARCHAR(100) COMMENT '登录地点',
  `device_info` VARCHAR(255) COMMENT '设备信息',
  `user_agent` VARCHAR(500) COMMENT '浏览器UA',
  `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';
```

---

## API 接口设计

### 认证相关接口

| 方法 | 路径 | 描述 | 认证 |
|-----|------|-----|------|
| POST | `/api/auth/login` | 账号密码登录 | 否 |
| POST | `/api/auth/login/phone` | 手机验证码登录 | 否 |
| POST | `/api/auth/login/oauth/{type}` | 第三方登录 | 否 |
| POST | `/api/auth/logout` | 退出登录 | 是 |
| POST | `/api/auth/refresh` | 刷新 Token | 是 |
| GET | `/api/auth/captcha` | 获取图形验证码 | 否 |
| POST | `/api/auth/sms/send` | 发送短信验证码 | 否 |

### 用户管理接口

| 方法 | 路径 | 描述 | 认证 |
|-----|------|-----|------|
| GET | `/api/user/info` | 获取当前用户信息 | 是 |
| PUT | `/api/user/password` | 修改密码 | 是 |
| POST | `/api/user/password/reset` | 重置密码 | 否 |
| POST | `/api/user/oauth/bind` | 绑定第三方账号 | 是 |
| DELETE | `/api/user/oauth/unbind/{type}` | 解绑第三方账号 | 是 |

---

## 执行顺序建议

### 阶段一: 基础架构 (Week 1)
1. 执行 F5 会话管理计划 - 搭建 JWT 双 Token 基础设施
2. 执行 F4 安全防护计划 - 实现安全中间件

### 阶段二: 核心登录功能 (Week 2-3)
3. 执行 F1 账号密码登录计划
4. 执行 F2 手机号验证码登录计划

### 阶段三: 扩展功能 (Week 4)
5. 执行 F3 第三方 OAuth 登录计划
6. 执行 F6 忘记密码计划

---

## 验证清单

- [ ] 所有接口单元测试通过
- [ ] 集成测试覆盖核心流程
- [ ] 安全测试: SQL 注入、XSS、CSRF、暴力破解防护验证
- [ ] 性能测试: 登录接口 QPS ≥ 1000, 响应时间 < 200ms
- [ ] 兼容性测试: 主流浏览器、移动端验证

---

**文档版本:** v1.0  
**创建时间:** 2026-07-14  
**最后更新:** 2026-07-14