# AUTH-001 实施任务清单

## 状态说明
- [ ] 待开始
- [x] 已完成
- [-] 进行中

## M1: 账号密码登录 (3天)

### 任务列表

#### T1.1 核心实体设计
- [ ] User 实体（支持用户名/邮箱/手机号登录）
- [ ] Session 实体（会话管理）
- [ ] SecurityLog 实体（安全审计）
- [ ] OAuthBinding 实体（第三方绑定）

#### T1.2 密码加密服务
- [ ] PasswordEncoder 接口
- [ ] Argon2PasswordEncoder 实现
- [ ] BcryptPasswordEncoder 实现（备选）
- [ ] RSA 加解密工具（前端传输）

#### T1.3 JWT Token 服务
- [ ] JwtTokenProvider 核心类
- [ ] Access Token 生成/验证
- [ ] Refresh Token 生成/验证
- [ ] Token 刷新机制

#### T1.4 登录控制器
- [ ] POST /api/auth/login/password
- [ ] 多字段登录标识识别
- [ ] 记住密码 Token 生成
- [ ] 登录失败计数

---

## M2: 安全防护（部分） (2天)

- [ ] IP 限流拦截器
- [ ] 图形验证码服务
- [ ] 登录锁定策略
- [ ] CSRF Token 生成/验证

---

## M3: 会话管理 (2天)

- [ ] POST /api/auth/token/refresh
- [ ] POST /api/auth/logout
- [ ] GET /api/auth/sessions
- [ ] DELETE /api/auth/sessions/{id}

---

## M4-M9: 后续里程碑

详见 OPSX.md 实施计划部分