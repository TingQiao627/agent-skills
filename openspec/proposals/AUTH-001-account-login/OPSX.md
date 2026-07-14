---
id: AUTH-001
title: T2 账号登录系统
status: draft
created: 2026-07-14
updated: 2026-07-14
proposer: AI Assistant
scope: authentication
impact: high
priority: P0
---

# OpenSpec 提案: 账号登录系统

## 1. 概述 (Overview)

### 1.1 业务目标
构建一套完整的账号登录系统，支持多种登录方式，保障安全性，提供良好的用户体验。系统需满足以下核心需求：
- 支持账号密码、手机验证码、第三方 OAuth 登录
- 提供完善的安全防护机制
- 实现可靠的会话管理
- 支持密码找回流程

### 1.2 影响范围
- **用户模块**: 新增/修改用户认证相关接口
- **安全模块**: 新增防护策略、验证码服务
- **会话模块**: 实现双 Token 机制
- **第三方集成**: 微信、支付宝、企业微信 OAuth 接入

---

## 2. 功能需求 (Functional Requirements)

### F1: 账号密码登录

#### 功能描述
支持用户使用账号+密码进行登录，提供增强的安全性和用户体验功能。

#### 详细规格

**F1.1 多字段支持**
- 支持用户名、邮箱、手机号三种登录标识
- 后端统一识别类型，前端提供单一输入框或选择器
- 验证规则：
  - 用户名: 3-20字符，字母数字下划线
  - 邮箱: 标准邮箱格式验证
  - 手机号: 11位数字，支持国际区号

**F1.2 加密传输**
- 前端使用 RSA 公钥加密密码
- 后端私钥解密验证
- 密码不落库明文，使用 bcrypt/argon2 加密存储
- 传输层强制 HTTPS

**F1.3 记住密码**
- 客户端本地存储加密 token（非明文密码）
- Token 有效期 30 天
- 用户可主动撤销记住状态

**F1.4 自动登录**
- 基于记住密码功能，检测有效 token 自动完成登录
- 首次访问时检查，无感知刷新会话
- 异地登录触发二次验证（见 F4）

#### 接口规格

```yaml
POST /api/auth/login/password
Request:
  identifier: string      # 用户名/邮箱/手机号
  password: string        # RSA加密后的密码
  remember_me: boolean    # 是否记住密码
  captcha_token: string   # 图形验证码token（安全策略触发时必填）

Response:
  access_token: string
  refresh_token: string
  expires_in: number
  user_info: object
```

---

### F2: 手机号验证码登录

#### 功能描述
支持用户通过手机号+验证码快速登录，自动识别新用户并注册。

#### 详细规格

**F2.1 短信发送频率限制**
- 同一手机号：
  - 60秒内只能发送 1 次
  - 每小时最多 5 次
  - 每天最多 10 次
- 超限返回友好提示，不暴露具体限制规则
- 支持配置化调整限制参数

**F2.2 验证码规格**
- 6位纯数字
- 有效期 5 分钟
- 验证成功后立即失效
- 同一手机号最多 3 次尝试机会

**F2.3 自动注册**
- 验证码验证通过后，检查手机号是否已注册
- 未注册则自动创建账号（最小化信息收集）
- 已注册则直接登录
- 返回 `is_new_user` 标识供前端引导完善信息

#### 接口规格

```yaml
POST /api/auth/sms/send
Request:
  phone: string           # 手机号（含国际区号）
  captcha_token: string   # 图形验证码（防刷保护）

Response:
  success: boolean
  retry_after: number     # 下次可发送时间戳

---

POST /api/auth/login/sms
Request:
  phone: string
  code: string            # 6位验证码

Response:
  access_token: string
  refresh_token: string
  expires_in: number
  user_info: object
  is_new_user: boolean
```

---

### F3: 第三方 OAuth 登录

#### 功能描述
支持微信、支付宝、企业微信第三方账号登录，并管理账号绑定关系。

#### 详细规格

**F3.1 支持的 OAuth 平台**
1. **微信开放平台**（移动应用、网站应用）
2. **支付宝**（生活号、小程序）
3. **企业微信**（企业内部应用）

**F3.2 OAuth 流程**
1. 前端获取第三方授权码
2. 后端使用授权码换取 access_token
3. 获取第三方用户信息
4. 检查是否已绑定账号
   - 已绑定：直接登录
   - 未绑定：
     - 已登录用户：绑定当前账号
     - 未登录用户：创建新账号或引导绑定已有账号

**F3.3 账号绑定管理**
- 支持一个账号绑定多个第三方账号（不同平台）
- 同一平台只能绑定一个第三方账号
- 提供解绑接口（需二次验证）
- 绑定/解绑操作记录审计日志

#### 接口规格

```yaml
POST /api/auth/oauth/{platform}/authorize
Request:
  code: string            # 第三方授权码
  state: string           # 防 CSRF state 参数

Response:
  access_token: string
  refresh_token: string
  expires_in: number
  user_info: object
  is_new_binding: boolean
  bind_action: 'login' | 'bind' | 'choose'

---

GET /api/auth/oauth/bindings
Response:
  bindings: [
    {
      platform: string    # wechat/alipay/wework
      platform_user_id: string
      nickname: string
      avatar: string
      bind_time: datetime
    }
  ]

---

DELETE /api/auth/oauth/bindings/{platform}
Request:
  verify_code: string     # 短信/邮箱验证码

Response:
  success: boolean
```

---

### F4: 安全防护

#### 功能描述
提供多层次安全防护机制，防止暴力破解、自动化攻击，保障账号安全。

#### 详细规格

**F4.1 登录锁定策略**
- 错误次数阈值：连续 5 次密码错误
- 锁定时长：15 分钟（可配置）
- 解锁方式：
  - 等待自动解锁
  - 短信/邮箱验证解锁
  - 管理员手动解锁
- 锁定状态：记录 IP、设备指纹

**F4.2 图形验证码**
- 触发条件：
  - 连续 3 次登录失败
  - 短信验证码发送前（防刷）
  - 频繁访问检测
- 验证码类型：
  - 数字字母组合（4-6位）
  - 滑动验证
  - 点选验证
- 有效期：2 分钟
- 失败次数限制：单验证码最多 3 次尝试

**F4.3 防暴力破解**
- IP 限流：同一 IP 每分钟最多 20 次登录请求
- 分布式限流：使用 Redis 计数
- 异常行为检测：
  - 凌晨时段高频尝试
  - 短时间多账号尝试
  - 自动标记可疑 IP

**F4.4 CSRF 防护**
- 所有状态变更操作验证 CSRF Token
- Token 生成：随机 32 字节 + 签名
- Token 存储：Cookie + Header 双重验证
- Token 刷新：登录后重新生成

**F4.5 异地登录提醒**
- 检测登录 IP 地理位置
- 新地区首次登录：
  - 发送短信/邮件通知
  - 记录登录设备、时间、地点
- 用户可查看历史登录记录
- 支持一键踢出异常会话（见 F5）

#### 数据结构

```yaml
SecurityLog:
  user_id: string
  action: string          # login/logout/bind/unbind
  ip: string
  location: string        # 地理位置
  device: string          # 设备指纹
  user_agent: string
  timestamp: datetime
  risk_level: 'low' | 'medium' | 'high'
```

---

### F5: 会话管理

#### 功能描述
实现 JWT 双 Token 机制，支持静默刷新和全局登出。

#### 详细规格

**F5.1 JWT 双 Token 机制**
- **Access Token**：
  - 有效期：2 小时
  - 用途：API 访问凭证
  - 存储：客户端内存/localStorage
- **Refresh Token**：
  - 有效期：7 天
  - 用途：刷新 Access Token
  - 存储：客户端 httpOnly Cookie + 数据库记录

**F5.2 Token 结构**

```yaml
Access Token Payload:
  sub: string             # 用户ID
  username: string
  roles: string[]
  permissions: string[]
  iat: number             # 签发时间
  exp: number             # 过期时间
  jti: string             # Token唯一标识
  device_fingerprint: string

Refresh Token Payload:
  sub: string
  iat: number
  exp: number
  jti: string
  device_fingerprint: string
```

**F5.3 静默刷新**
- 前端检测 Access Token 即将过期（提前 5 分钟）
- 使用 Refresh Token 无感刷新
- 刷新失败提示重新登录
- 单次刷新窗口期：5 分钟（防止并发刷新）

**F5.4 全局登出**
- 用户主动登出：撤销所有设备会话
- 强制登出：管理员操作或安全事件触发
- 登出操作：
  - 将 Refresh Token 标记为失效
  - 清除数据库会话记录
  - 通知其他设备（WebSocket 推送）

#### 接口规格

```yaml
POST /api/auth/token/refresh
Request:
  refresh_token: string   # 或从 Cookie 自动读取

Response:
  access_token: string
  refresh_token: string   # 可选：轮换新 Refresh Token
  expires_in: number

---

POST /api/auth/logout
Request:
  global: boolean         # 是否全局登出

Response:
  success: boolean

---

GET /api/auth/sessions
Response:
  sessions: [
    {
      session_id: string
      device: string
      ip: string
      location: string
      login_time: datetime
      last_active: datetime
      is_current: boolean
    }
  ]

---

DELETE /api/auth/sessions/{session_id}
Response:
  success: boolean
```

---

### F6: 忘记密码

#### 功能描述
提供安全的密码重置流程，包含身份验证和通知机制。

#### 详细规格

**F6.1 流程设计**

```
步骤1: 身份验证
  ├─ 输入账号/邮箱/手机号
  ├─ 发送验证码（短信/邮箱）
  └─ 验证通过进入步骤2

步骤2: 重置密码
  ├─ 设置新密码
  ├─ 密码强度校验
  └─ 确认重置

步骤3: 通知
  ├─ 发送密码重置成功通知
  ├─ 全局登出其他设备
  └─ 记录安全日志
```

**F6.2 密码强度要求**
- 最小长度：8 位
- 必须包含：数字 + 字母
- 建议包含：特殊字符
- 不能包含：用户名、连续字符、常见弱密码
- 检查密码泄露库（可选）

**F6.3 防护措施**
- 验证码有效期：10 分钟
- 单次验证码仅可重置一次
- 重置链接/验证码使用后立即失效
- 记录密码修改历史（防止短时间内多次修改）

#### 接口规格

```yaml
POST /api/auth/password/reset/request
Request:
  identifier: string      # 账号/邮箱/手机号

Response:
  success: boolean
  verify_method: string   # 'sms' | 'email'
  retry_after: number

---

POST /api/auth/password/reset/verify
Request:
  identifier: string
  code: string            # 验证码

Response:
  reset_token: string     # 用于重置密码的临时凭证
  expires_in: number

---

POST /api/auth/password/reset/confirm
Request:
  reset_token: string
  new_password: string    # RSA 加密

Response:
  success: boolean
```

---

## 3. 非功能需求 (Non-Functional Requirements)

### 3.1 性能要求
- 登录接口响应时间 < 500ms (P95)
- Token 验证 < 100ms
- 支持并发登录请求 1000 QPS

### 3.2 安全要求
- 密码存储使用 bcrypt (cost=12) 或 argon2
- 所有敏感操作记录审计日志
- 遵循 OWASP Top 10 防护标准
- 定期安全审计和渗透测试

### 3.3 可用性要求
- 登录服务可用性 99.9%
- 支持多活部署
- 第三方服务降级预案

### 3.4 合规要求
- 符合《网络安全法》要求
- 遵守《个人信息保护法》
- 日志保留至少 6 个月

---

## 4. 技术架构 (Technical Architecture)

### 4.1 核心组件
```
┌─────────────────────────────────────────────┐
│            API Gateway                       │
│  (限流、CSRF、WAF)                          │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Auth Service                         │
│  ├─ Login Controller                         │
│  ├─ OAuth Controller                         │
│  ├─ Session Controller                       │
│  └─ Security Controller                      │
└─────────────────────────────────────────────┘
                    ↓
    ┌───────────────┼───────────────┐
    ↓               ↓               ↓
┌─────────┐   ┌─────────┐   ┌──────────┐
│ User DB │   │ Redis   │   │ SMS/Email│
│         │   │ Session │   │ Service  │
└─────────┘   └─────────┘   └──────────┘
```

### 4.2 技术选型
- **认证框架**: Spring Security / Passport.js / Authlib
- **JWT 库**: jjwt / jsonwebtoken
- **缓存**: Redis (会话、验证码、限流)
- **数据库**: PostgreSQL / MySQL (用户、日志)
- **消息队列**: RabbitMQ / Kafka (异步通知)

---

## 5. 实施计划 (Implementation Plan)

### 5.1 里程碑

| 阶段 | 内容 | 工期 | 依赖 |
|------|------|------|------|
| M1 | F1 账号密码登录 | 3天 | 无 |
| M2 | F4 安全防护（部分） | 2天 | M1 |
| M3 | F5 会话管理 | 2天 | M1 |
| M4 | F2 手机验证码登录 | 2天 | SMS 服务 |
| M5 | F3 OAuth 登录（微信） | 3天 | 微信开放平台 |
| M6 | F3 OAuth 登录（其他） | 2天 | M5 |
| M7 | F6 忘记密码 | 2天 | M1, M4 |
| M8 | F4 完善（异地提醒等） | 2天 | M1-M7 |
| M9 | 集成测试与优化 | 3天 | M1-M8 |

**总工期**: 约 21 天

### 5.2 关键路径
M1 → M2 → M3 → M5 → M8 → M9

---

## 6. 风险与应对 (Risks & Mitigations)

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|----------|
| 第三方 OAuth 接口变更 | 高 | 中 | 抽象适配层、监控告警 |
| 短信服务不稳定 | 高 | 低 | 多服务商备份、降级方案 |
| 暴力破解攻击 | 高 | 中 | 多层限流、验证码、IP 黑名单 |
| Token 泄露 | 高 | 低 | 短有效期、轮换机制、全局登出 |
| 合规审查不通过 | 高 | 低 | 提前法务审核、隐私政策完善 |

---

## 7. 验收标准 (Acceptance Criteria)

### 7.1 功能验收
- ✅ 所有接口功能正常，符合规格文档
- ✅ 三种登录方式均可正常使用
- ✅ 安全防护机制有效（防暴力破解、CSRF、验证码）
- ✅ Token 刷新和全局登出正常工作
- ✅ 忘记密码流程完整可用

### 7.2 性能验收
- ✅ 登录接口 P95 响应时间 < 500ms
- ✅ 压测通过 1000 QPS

### 7.3 安全验收
- ✅ 无 OWASP Top 10 漏洞
- ✅ 密码存储安全
- ✅ 通过渗透测试

### 7.4 用户体验
- ✅ 登录流程顺畅，无死链接
- ✅ 错误提示友好
- ✅ 响应式设计，移动端适配

---

## 8. 后续演进 (Future Evolution)

### 8.1 短期优化 (V1.1)
- 生物识别登录（指纹、Face ID）
- 设备信任管理
- 密码强度可视化

### 8.2 长期规划 (V2.0)
- 无密码登录（Magic Link、Passkey）
- 统一身份认证平台（SSO）
- 零信任架构升级

---

## 9. 附录 (Appendix)

### 9.1 参考资料
- [OAuth 2.0 规范](https://oauth.net/2/)
- [JWT 最佳实践](https://datatracker.ietf.org/doc/html/rfc8725)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

### 9.2 相关文档
- 安全架构设计文档（待创建）
- 数据库设计文档（待创建）
- API 接口文档（待创建）