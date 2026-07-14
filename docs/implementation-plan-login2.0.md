# 账号登录 2.0 - 实施计划

> **生成时间：** 2026-07-14
> **当前阶段：** plan（实施计划）
> **技能应用：** writing-plans

---

## 一、项目目标

完成账号登录 2.0 系统的六大核心功能模块开发与验证：
- F1 账号密码登录（多字段支持、加密传输、记住密码、自动登录）
- F2 手机号验证码登录（短信频率限制、自动注册）
- F3 第三方 OAuth 登录（微信/支付宝/企业微信 + 账号绑定管理）
- F4 安全防护（登录锁定、图形验证码、防暴力破解、CSRF、异地提醒）
- F5 会话管理（JWT 双 Token、静默刷新、全局登出）
- F6 忘记密码（身份验证 → 重置 → 通知）

---

## 二、成功标准

1. 所有 6 个功能模块通过单元测试和集成测试
2. 安全审计无高危漏洞
3. API 文档完整且通过验证
4. 端到端登录流程可正常使用

---

## 三、已验证证据

| 证据项 | 来源 |
|-------|------|
| 6 大功能模块详细规格 | docs/requirements-clarification-login2.0.md |
| 技术栈确认 | Spring Boot 3.2.0 + Spring Security 6 + JWT + PostgreSQL + Redis |
| 所有待澄清项推荐方案 | 需求澄清文档各模块章节 |
| 数据库实体设计 | User、OAuthBinding、LoginLog、PasswordResetToken |

---

## 四、影响范围

| 层级 | 影响内容 |
|-----|---------|
| 后端认证模块 | 新增登录、验证码、OAuth、会话管理相关 Controller/Service |
| 数据库 | 新增用户表、OAuth绑定表、登录日志表、密码重置表 |
| Redis | 新增会话缓存、限流计数器、Token 黑名单 |
| 外部依赖 | 短信服务、微信/支付宝 OAuth API |

---

## 五、风险与缓解

| 风险 | 缓解措施 |
|-----|---------|
| OAuth 账号绑定冲突处理逻辑复杂 | 严格设计绑定流程，采用推荐方案：提示用户登录已有账号后进行绑定 |
| 异地登录判定依赖 IP 库准确性 | 配置降级策略，IP 库不可用时跳过异地检测 |
| 短信服务商稳定性 | 配置备用通道和降级策略 |

---

## 六、回滚策略

1. 功能模块按 Feature Toggle 隔离，可独立回滚
2. 数据库迁移使用 Flyway/Liquibase 版本化管理
3. Redis Key 采用命名空间隔离，便于清理

---

## 七、验证方案

- **单元测试：** `mvn test` 执行所有单元测试通过
- **集成测试：** 覆盖登录、验证码、OAuth、会话刷新核心路径
- **安全扫描：** 检查 SQL 注入、XSS、CSRF 漏洞
- **API 文档：** 与实际接口一致

---

## 八、实施步骤

### Step 1: 搭建基础设施与数据库设计
**优先级：** 高
**状态：** 待执行

**任务内容：**
- 创建 User 表（id, username, phone, email, password, status）
- 创建 OAuthBinding 表（id, user_id, platform, openid, unionid）
- 创建 LoginLog 表（id, user_id, ip, location, device, status, created_at）
- 创建 PasswordResetToken 表（id, user_id, token, expire_at, used）
- 配置 Redis 连接池和命名空间
- 创建 Entity 类和 Repository 接口

**验收标准：**
- 数据库表创建成功，索引配置正确
- Entity 类字段与数据库表一致
- Redis 连接测试通过

---

### Step 2: 实现 F5 会话管理模块
**优先级：** 高
**状态：** 待执行

**任务内容：**
- JWT 双 Token 生成与验证（Access Token 30 分钟，Refresh Token 7 天）
- 静默刷新接口（Token 过期前 5 分钟或返回 401 时触发）
- 全局登出实现（Refresh Token 加入 Redis 黑名单）
- Token 黑名单管理

**技术要点：**
- Access Token 存储：sub（用户ID）、roles、exp、iat
- Refresh Token 存储：tokenId、userId、expireAt
- 黑名单 Key：`token:blacklist:{tokenId}`

**验收标准：**
- Token 生成/验证单元测试通过
- 静默刷新逻辑正确
- 全局登出后 Token 立即失效

---

### Step 3: 实现 F1 账号密码登录
**优先级：** 高
**状态：** 待执行

**任务内容：**
- 多字段登录（用户名/手机号/邮箱），系统自动识别账号类型
- 密码加密传输：前端 RSA 加密，后端 BCrypt 存储
- 记住密码：客户端安全存储（30 天）
- 自动登录：基于 Refresh Token 的免登录机制
- 密码强度规则：至少 8 位，包含大小写字母 + 数字

**API 接口：**
- `POST /api/auth/login` - 统一登录接口
- `GET /api/auth/public-key` - 获取 RSA 公钥

**验收标准：**
- 三种账号类型登录成功
- 密码加密传输验证通过
- 记住密码/自动登录功能正常

---

### Step 4: 实现 F2 手机号验证码登录
**优先级：** 高
**状态：** 待执行

**任务内容：**
- 短信发送限流：60 秒/次，每天最多 10 条
- 验证码生成与校验（5 分钟有效期）
- 自动注册：验证通过后创建账号，默认用户名 `user_{phone}`
- 手机号格式校验（优先支持大陆 11 位）

**Redis Key 设计：**
- 限流：`sms:limit:{phone}`
- 验证码：`sms:code:{phone}`

**验收标准：**
- 短信限流生效
- 验证码校验逻辑正确
- 自动注册流程完整

---

### Step 5: 实现 F3 第三方 OAuth 登录
**优先级：** 高
**状态：** 待执行

**任务内容：**
- 微信/支付宝/企业微信 OAuth 授权
- 账号绑定管理（每个平台绑定 1 个）
- 绑定冲突处理：提示用户登录已有账号后进行绑定
- 仅存储 openid/unionid，不存储敏感 token

**API 接口：**
- `GET /api/oauth/{platform}/authorize` - 获取授权 URL
- `GET /api/oauth/{platform}/callback` - 授权回调
- `POST /api/oauth/bind` - 绑定第三方账号
- `DELETE /api/oauth/unbind/{platform}` - 解绑

**验收标准：**
- 三平台 OAuth 流程完整
- 绑定/解绑功能正常
- 冲突处理逻辑正确

---

### Step 6: 实现 F4 安全防护
**优先级：** 高
**状态：** 待执行

**任务内容：**
- 登录锁定：连续 5 次失败锁定 30 分钟，支持短信验证解锁
- 图形验证码：首次不显示，失败 1 次后显示
- IP/账号双重限流
- CSRF Token 校验（每个请求刷新）
- 异地登录提醒：短信 + 邮件双重通知

**Redis Key 设计：**
- 登录失败：`login:fail:{account/IP}`
- CSRF Token：`csrf:{sessionId}`

**验收标准：**
- 登录锁定机制生效
- 图形验证码触发正确
- 异地提醒发送成功

---

### Step 7: 实现 F6 忘记密码
**优先级：** 中
**状态：** 待执行

**任务内容：**
- 身份验证：手机/邮箱验证码
- 重置链接生成（15 分钟有效期）
- 密码历史检查：不与最近 3 次相同
- 密码变更通知：短信 + 邮件
- 重置成功后强制重新登录所有设备

**验收标准：**
- 身份验证流程完整
- 重置链接一次性使用
- 密码历史检查生效

---

## 九、依赖关系

```
Step 1 (基础设施) ──┬──> Step 2 (会话管理) ──> Step 3 (账号密码登录)
                    │                         │
                    │                         ├──> Step 4 (验证码登录)
                    │                         │
                    │                         ├──> Step 5 (OAuth 登录)
                    │                         │
                    │                         └──> Step 6 (安全防护)
                    │
                    └──> Step 7 (忘记密码)
```

---

## 十、技术栈确认

| 组件 | 版本 |
|-----|------|
| Spring Boot | 3.2.0 |
| Spring Security | 6.x |
| JWT (JJWT) | 0.12.3 |
| PostgreSQL | 主库 |
| Redis | 缓存/会话 |
| RabbitMQ/Kafka | 可选，异步通知 |

---

**文档版本：** v1.0
**最后更新：** 2026-07-14