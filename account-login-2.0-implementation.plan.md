# 账号登录 2.0 实施计划

## 📋 项目概述

实现完整的账号登录 2.0 系统，包含账号密码登录、手机验证码登录、第三方 OAuth 登录、安全防护、会话管理、忘记密码等六大核心功能模块。

**技术栈：**
- 后端：Spring Boot 3.2 + Spring Security 6 + JWT (JJWT 0.12.3)
- 数据库：PostgreSQL + Spring Data JPA
- 缓存：Redis
- 消息队列：RabbitMQ/Kafka（异步通知）

---

## ✅ 成功标准

| 标准 | 描述 |
|------|------|
| SC1 | 用户可通过账号密码登录，支持记住密码和自动登录功能 |
| SC2 | 用户可通过手机验证码登录，首次登录自动注册 |
| SC3 | 用户可通过微信/支付宝/企业微信第三方 OAuth 登录并管理账号绑定 |
| SC4 | 系统具备安全防护能力：登录锁定、图形验证码、防暴力破解、CSRF 防护、异地登录提醒 |
| SC5 | 会话管理支持 JWT 双 Token 机制，实现静默刷新和全局登出 |
| SC6 | 忘记密码流程完整：身份验证 → 密码重置 → 通知用户 |

---

## 📝 约束条件

- 采用 Spring Boot 3.2 + Spring Security 6 + JWT 技术栈
- 数据库使用 PostgreSQL，缓存使用 Redis
- 必须支持多字段登录（用户名/邮箱/手机号）
- 密码传输必须加密（RSA/AES）
- 短信验证码必须有频率限制（60秒间隔，每日上限）
- OAuth 登录需支持账号绑定管理（绑定/解绑）
- JWT Access Token 有效期 ≤ 2小时，Refresh Token 有效期 ≤ 7天

---

## 🎯 技术决策

| 决策 | 说明 |
|------|------|
| D1 | 采用 RSA 非对称加密传输密码（前端公钥加密，后端私钥解密） |
| D2 | 短信验证码 6 位数字，有效期 5 分钟，同一手机号 60 秒内不可重复发送 |
| D3 | JWT 双 Token 方案：Access Token 2 小时，Refresh Token 7 天 |
| D4 | OAuth 登录优先级：企业微信 > 微信 > 支付宝 |
| D5 | 登录锁定策略：连续失败 5 次，锁定 30 分钟 |

---

## 📊 实施步骤

### 步骤 1: F1 账号密码登录模块

**功能范围：**
- 多字段登录支持（用户名/邮箱/手机号）
- 密码 RSA 加密传输
- 记住密码功能（加密 Cookie 存储）
- 自动登录机制（Refresh Token）

**核心文件：**
```
src/main/java/com/auth/
├── controller/AuthController.java
├── service/AuthService.java
├── service/PasswordEncoder.java
├── dto/LoginRequest.java
├── dto/LoginResponse.java
└── entity/LoginAttempt.java
```

**数据库表：**
```sql
CREATE TABLE user_accounts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_accounts(id),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    success BOOLEAN DEFAULT false
);
```

**API 接口：**
- `POST /api/auth/login` - 登录
- `POST /api/auth/logout` - 登出
- `GET /api/auth/me` - 获取当前用户信息

---

### 步骤 2: F2 手机验证码登录模块

**功能范围：**
- 手机号验证码登录
- 短信发送频率限制（60秒间隔，每日上限 5 次）
- 首次登录自动注册
- 验证码有效期 5 分钟

**核心文件：**
```
src/main/java/com/sms/
├── controller/SmsController.java
├── service/SmsService.java
├── service/SmsRateLimitService.java
├── dto/SendCodeRequest.java
├── dto/VerifyCodeRequest.java
└── entity/SmsVerificationCode.java
```

**数据库表：**
```sql
CREATE TABLE sms_verification_codes (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sms_send_limits (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    send_date DATE NOT NULL,
    send_count INT DEFAULT 0,
    UNIQUE(phone, send_date)
);
```

**API 接口：**
- `POST /api/sms/send` - 发送验证码
- `POST /api/sms/verify` - 验证码登录

---

### 步骤 3: F3 第三方 OAuth 登录模块

**功能范围：**
- 微信 OAuth 2.0 登录
- 支付宝 OAuth 2.0 登录
- 企业微信 OAuth 2.0 登录
- 账号绑定管理（绑定/解绑）

**核心文件：**
```
src/main/java/com/oauth/
├── controller/OAuthController.java
├── service/
│   ├── WechatOAuthService.java
│   ├── AlipayOAuthService.java
│   └── WorkWechatOAuthService.java
├── dto/OAuthCallbackRequest.java
└── entity/UserOAuthBinding.java
```

**数据库表：**
```sql
CREATE TABLE user_oauth_bindings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_accounts(id),
    oauth_provider VARCHAR(20) NOT NULL,
    oauth_user_id VARCHAR(100) NOT NULL,
    oauth_union_id VARCHAR(100),
    bind_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(oauth_provider, oauth_user_id)
);
```

**API 接口：**
- `GET /api/oauth/{provider}/authorize` - 获取授权 URL
- `POST /api/oauth/{provider}/callback` - OAuth 回调
- `GET /api/oauth/bindings` - 获取绑定列表
- `DELETE /api/oauth/bindings/{id}` - 解除绑定

---

### 步骤 4: F4 安全防护模块

**功能范围：**
- 登录锁定机制（连续失败 5 次，锁定 30 分钟）
- 图形验证码生成与验证
- 防暴力破解（IP 频率限制）
- CSRF Token 防护
- 异地登录提醒

**核心文件：**
```
src/main/java/com/security/
├── config/SecurityConfig.java
├── service/
│   ├── LoginLockService.java
│   ├── CaptchaService.java
│   ├── BruteForceProtectionService.java
│   └── AnomalyLoginDetectionService.java
├── filter/CsrfTokenFilter.java
└── entity/LoginLock.java
```

**数据库表：**
```sql
CREATE TABLE login_locks (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(100) NOT NULL,
    lock_type VARCHAR(20) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_login_locations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_accounts(id),
    ip_address VARCHAR(45) NOT NULL,
    city VARCHAR(100),
    country VARCHAR(100),
    first_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**配置项：**
```yaml
security:
  login:
    max_attempts: 5
    lock_duration: 30m
  captcha:
    length: 6
    expire: 5m
  brute_force:
    max_requests_per_minute: 10
  anomaly:
    enabled: true
    alert_channels: [email, sms]
```

---

### 步骤 5: F5 会话管理模块

**功能范围：**
- JWT 双 Token 机制（Access Token + Refresh Token）
- 静默刷新（前端无感知）
- 全局登出（单点登出所有设备）
- Token 黑名单机制

**核心文件：**
```
src/main/java/com/session/
├── service/
│   ├── JwtService.java
│   ├── TokenRefreshService.java
│   └── SessionManagementService.java
├── filter/JwtAuthenticationFilter.java
└── entity/UserSession.java
```

**数据库表：**
```sql
CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_accounts(id),
    session_id VARCHAR(100) UNIQUE NOT NULL,
    access_token_hash VARCHAR(255),
    refresh_token_hash VARCHAR(255),
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE token_blacklist (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);
```

**API 接口：**
- `POST /api/auth/refresh` - 刷新 Token
- `POST /api/auth/logout-all` - 全局登出
- `GET /api/auth/sessions` - 获取会话列表

---

### 步骤 6: F6 忘记密码模块

**功能范围：**
- 身份验证（手机/邮箱验证）
- 密码重置令牌生成
- 密码重置流程
- 重置成功通知

**核心文件：**
```
src/main/java/com/password/
├── controller/PasswordResetController.java
├── service/PasswordResetService.java
├── dto/
│   ├── ResetRequest.java
│   ├── VerifyIdentityRequest.java
│   └── ResetPasswordRequest.java
└── entity/PasswordResetToken.java
```

**数据库表：**
```sql
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES user_accounts(id),
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    verification_method VARCHAR(20) NOT NULL,
    verified BOOLEAN DEFAULT false,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**API 接口：**
- `POST /api/password/reset-request` - 发起重置请求
- `POST /api/password/verify-identity` - 身份验证
- `POST /api/password/reset` - 执行密码重置

---

### 步骤 7: 集成测试与验证

**测试范围：**
- 单元测试覆盖率 ≥ 80%
- 集成测试覆盖核心登录流程（6 种场景）
- 安全测试：SQL 注入、XSS、CSRF、暴力破解防护
- 性能测试：并发登录、Token 刷新性能

**测试文件：**
```
src/test/java/com/auth/
├── AuthControllerIntegrationTest.java
├── AuthServiceTest.java
├── SmsLoginTest.java
├── OAuthFlowTest.java
├── SecurityTest.java
└── SessionManagementTest.java
```

**验证清单：**
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 安全测试通过
- [ ] API 文档生成（OpenAPI）
- [ ] 数据库迁移脚本验证
- [ ] 性能基准测试通过

---

## ⚠️ 风险管理

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| OAuth 集成复杂度高 | 高 | 提前申请第三方平台审核，准备沙箱环境测试 |
| 短信服务成本和稳定性 | 中 | 准备备用服务商（阿里云/腾讯云/云片），实现降级切换 |
| JWT Token 刷新并发安全性 | 中 | 使用 Redis 分布式锁保证原子性 |
| 异地登录判断准确性 | 中 | 接入专业 IP 库服务，设置合理阈值 |
| 记住密码客户端安全性 | 低 | Cookie 加密存储，设置合理过期时间 |

---

## 🔄 回滚策略

- **模块级回滚**：各模块独立，可按模块禁用或回滚
- **数据库回滚**：迁移脚本版本化，提供降级脚本
- **配置回滚**：OAuth 功能通过配置开关动态启用/禁用
- **版本回滚**：使用 Git 版本控制，支持快速回退到稳定版本

---

## 📦 影响范围

**新增模块：**
- `src/main/java/com/auth/**` - 认证核心
- `src/main/java/com/sms/**` - 短信服务
- `src/main/java/com/oauth/**` - OAuth 集成
- `src/main/java/com/security/**` - 安全防护
- `src/main/java/com/session/**` - 会话管理
- `src/main/java/com/password/**` - 密码重置

**修改文件：**
- `pom.xml` - 新增依赖
- `src/main/resources/application.yml` - 配置更新
- `src/main/resources/db/migration/**` - 数据库迁移

---

## 📚 参考资料

- Spring Security 官方文档：https://docs.spring.io/spring-security/reference/
- JWT 最佳实践：https://datatracker.ietf.org/doc/html/rfc8725
- OAuth 2.0 规范：https://oauth.net/2/
- 微信开放平台文档：https://developers.weixin.qq.com/
- 支付宝开放平台文档：https://opendocs.alipay.com/

---

## 📅 时间估算

| 模块 | 开发工时 | 测试工时 | 总计 |
|------|----------|----------|------|
| F1 账号密码登录 | 3 天 | 1 天 | 4 天 |
| F2 手机验证码登录 | 2 天 | 1 天 | 3 天 |
| F3 第三方 OAuth 登录 | 4 天 | 2 天 | 6 天 |
| F4 安全防护 | 3 天 | 1.5 天 | 4.5 天 |
| F5 会话管理 | 2 天 | 1 天 | 3 天 |
| F6 忘记密码 | 1.5 天 | 0.5 天 | 2 天 |
| 集成测试与验证 | - | 2 天 | 2 天 |
| **总计** | **15.5 天** | **8 天** | **23.5 天** |

---

*计划创建时间：2026-07-14*  
*计划状态：待执行*