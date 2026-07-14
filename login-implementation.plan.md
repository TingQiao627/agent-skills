# 登录功能 2.0 实施计划

## 项目概述
实现完整的登录功能系统，包括账号密码登录、手机验证码登录、第三方OAuth登录、安全防护、会话管理等核心功能。

## 技术栈
- 后端：Spring Boot 3.x + Spring Security + JWT
- 数据库：PostgreSQL/MySQL
- 缓存：Redis
- 消息队列：RabbitMQ/Kafka (用于异步通知)
- 前端：React/Vue (可选)

## 模块划分

### 模块 1: 核心认证模块 (F1)
**任务**: 账号密码登录
- 多字段支持（用户名/邮箱/手机号）
- 密码加密传输（BCrypt + RSA）
- 记住密码功能
- 自动登录功能
- 密码强度校验

**产出文件**:
- `src/main/java/com/auth/controller/AuthController.java`
- `src/main/java/com/auth/service/AuthService.java`
- `src/main/java/com/auth/dto/LoginRequest.java`
- `src/main/java/com/auth/dto/LoginResponse.java`
- `src/main/java/com/auth/entity/User.java`
- `src/main/java/com/auth/repository/UserRepository.java`

### 模块 2: 手机验证码登录 (F2)
**任务**: 手机号验证码登录
- 短信发送频率限制（同IP/同手机号）
- 验证码有效期管理
- 自动注册逻辑
- 短信服务商集成（阿里云/腾讯云）

**产出文件**:
- `src/main/java/com/sms/controller/SmsController.java`
- `src/main/java/com/sms/service/SmsService.java`
- `src/main/java/com/sms/service/impl/AliyunSmsServiceImpl.java`
- `src/main/java/com/sms/dto/SendCodeRequest.java`
- `src/main/java/com/sms/dto/VerifyCodeRequest.java`

### 模块 3: 第三方OAuth登录 (F3)
**任务**: 微信/支付宝/企业微信 OAuth 登录
- OAuth2.0 集成
- 用户绑定管理
- 第三方账号解绑
- 多平台统一认证

**产出文件**:
- `src/main/java/com/oauth/controller/OAuthController.java`
- `src/main/java/com/oauth/service/OAuthService.java`
- `src/main/java/com/oauth/service/impl/WechatOAuthServiceImpl.java`
- `src/main/java/com/oauth/service/impl/AlipayOAuthServiceImpl.java`
- `src/main/java/com/oauth/entity/OAuthBinding.java`
- `src/main/java/com/oauth/repository/OAuthBindingRepository.java`

### 模块 4: 安全防护模块 (F4)
**任务**: 登录安全防护
- 登录锁定机制（连续失败N次）
- 图形验证码
- 防暴力破解
- CSRF防护
- 异地登录提醒

**产出文件**:
- `src/main/java/com/security/service/LockService.java`
- `src/main/java/com/security/service/CaptchaService.java`
- `src/main/java/com/security/service/SecurityMonitorService.java`
- `src/main/java/com/security/entity/LoginLog.java`
- `src/main/java/com/security/repository/LoginLogRepository.java`

### 模块 5: 会话管理 (F5)
**任务**: JWT双Token机制
- Access Token + Refresh Token
- 静默刷新机制
- 全局登出
- Token黑名单

**产出文件**:
- `src/main/java/com/session/service/JwtService.java`
- `src/main/java/com/session/service/SessionManager.java`
- `src/main/java/com/session/service/TokenBlacklistService.java`
- `src/main/java/com/session/filter/JwtAuthenticationFilter.java`

### 模块 6: 密码找回 (F6)
**任务**: 忘记密码流程
- 身份验证（邮箱/手机验证）
- 密码重置链接
- 重置成功通知

**产出文件**:
- `src/main/java/com/password/controller/PasswordResetController.java`
- `src/main/java/com/password/service/PasswordResetService.java`
- `src/main/java/com/password/dto/ResetRequest.java`
- `src/main/java/com/password/entity/PasswordResetToken.java`

### 模块 7: 辅助功能 (F7)
**任务**: 辅助功能
- 密码明文切换
- Enter键提交
- 登录后重定向
- 注册入口

**产出文件**:
- 前端组件（可选）
- API文档增强

### 模块 8: 基础设施
**任务**: 项目基础设施
- Spring Boot项目初始化
- 数据库设计
- 配置文件
- 安全配置

**产出文件**:
- `pom.xml` / `build.gradle`
- `application.yml`
- `schema.sql`
- `SpringSecurityConfig.java`
- `WebMvcConfig.java`

## 执行策略
采用并行派发模式，将独立模块分配给不同子代理执行：
1. 基础设施模块先执行（依赖基础）
2. 其他模块可并行执行
3. 最后集成测试

## 验证标准
- 每个模块编译通过
- 单元测试覆盖率 > 80%
- API接口可正常调用
- 安全测试通过（SQL注入、XSS等）