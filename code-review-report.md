# 代码评审报告 - T3 账号登录系统

**评审时间**: 2026-07-14  
**评审范围**: src/account-login 模块  
**评审标准**: code-review-skill (FastAPI/Python)

---

## 📋 总体评估

| 维度 | 评分 | 说明 |
|------|------|------|
| 安全性 | ⚠️ 中等 | 核心防护已实现，但存在若干安全隐患 |
| 代码质量 | ✅ 良好 | 结构清晰，分层合理，符合 FastAPI 最佳实践 |
| 功能完整性 | ⚠️ 部分完成 | F1/F4/F5 已实现，F2/F3/F6 待完善 |
| 可维护性 | ✅ 良好 | 模块化设计，注释充分 |
| 测试覆盖 | ❌ 缺失 | 无单元测试文件 |

---

## 🔴 严重问题 (Critical)

### C1. 硬编码密钥安全风险
**文件**: `config.py:22-23`
```python
SECRET_KEY: str = "your-secret-key-change-in-production"
JWT_SECRET: str = "jwt-secret-key-change-in-production"
```
**问题**: 生产环境默认值泄露风险  
**修复建议**:
```python
SECRET_KEY: str = Field(default_factory=lambda: os.environ.get("SECRET_KEY", ""))
JWT_SECRET: str = Field(default_factory=lambda: os.environ.get("JWT_SECRET", ""))
```

### C2. JWT Token 黑名单存储于内存
**文件**: `jwt_service.py:89`
```python
self._blacklist: Dict[str, datetime] = {}
```
**问题**: 进程重启后黑名单丢失，无法保证安全一致性  
**影响**: 全局登出、Token 失效机制不可靠  
**修复建议**: 使用 Redis 持久化存储黑名单

### C3. 登录失败锁定存储于内存
**文件**: `security_middleware.py:45-48`
```python
self._ip_failures: Dict[str, List[datetime]] = {}
self._account_failures: Dict[str, List[datetime]] = {}
self._ip_locked: Dict[str, datetime] = {}
self._account_locked: Dict[str, datetime] = {}
```
**问题**: 进程重启后锁定记录丢失，攻击者可绕过限制  
**修复建议**: 迁移至 Redis 存储，支持分布式部署

### C4. 缺少 CSRF 保护实现
**文件**: `auth_controller.py`  
**问题**: 需求 F4 要求 CSRF 防护，但代码中未实现  
**修复建议**: 集成 `fastapi-csrf-protect` 或实现 SameSite Cookie 策略

---

## 🟠 重要问题 (Major)

### M1. sys.path 修改违反最佳实践
**文件**: `auth_controller.py:6-7`, `user_repository.py:6-7`
```python
import sys
sys.path.insert(0, '..')
```
**问题**: 动态修改模块路径，违反 Python 导入规范  
**修复建议**: 使用相对导入或配置 PYTHONPATH

### M2. 密码历史记录存储不安全
**文件**: `user_repository.py:26`
```python
self._password_history: Dict[int, List[str]] = {}
```
**问题**: 密码历史明文存储于内存，存在泄露风险  
**修复建议**: 仅存储哈希值，且应持久化到数据库

### M3. 用户数据无持久化
**文件**: `user_repository.py:20-21`
```python
self._users: Dict[int, User] = {}
self._users_by_username: Dict[str, int] = {}
```
**问题**: 所有用户数据仅存内存，重启后丢失  
**修复建议**: 接入真实数据库（PostgreSQL/MySQL）

### M4. 缺少 Refresh Token Rotation
**文件**: `jwt_service.py`  
**问题**: 刷新 Token 时未实现 Token Rotation（安全最佳实践）  
**影响**: Token 泄露后有效期较长，风险较大  
**修复建议**: 刷新时生成新的 Refresh Token，旧 Token 立即失效

### M5. 缺少请求日志审计
**文件**: `main.py`, `security_middleware.py`  
**问题**: 登录成功/失败、锁定事件无持久化日志  
**影响**: 安全事件无法追溯  
**修复建议**: 集成结构化日志系统（如 structlog）+ 审计日志表

---

## 🟡 一般问题 (Minor)

### m1. 验证码实现缺失
**文件**: `models/user.py:69`
```python
captcha: Optional[str] = Field(None, description="图形验证码")
```
**问题**: 字段存在但无验证逻辑  
**修复建议**: 实现验证码生成/校验服务

### m2. 密码强度规则不完整
**文件**: `models/user.py:51-61`  
**问题**: 未要求特殊字符，符合行业最高标准应包含  
**修复建议**: 添加 `if not re.search(r'[!@#$%^&*(),.?":{}|<>]', v)`

### m3. 缺少 Token 过期时间校验边界处理
**文件**: `jwt_service.py:180-185`  
**问题**: `should_refresh_token` 方法未处理 Token 已失效情况  
**修复建议**: 添加 try-catch 捕获 jwt.ExpiredSignatureError

### m4. 用户模型缺少索引优化字段
**文件**: `models/user.py`  
**问题**: 缺少 `last_login_at`、`login_count` 等审计字段  
**修复建议**: 添加登录审计字段

### m5. 错误消息国际化缺失
**文件**: 各控制器和服务文件  
**问题**: 所有错误消息硬编码中文  
**修复建议**: 使用 i18n 框架支持多语言

---

## ✅ 优秀实践

1. **分层架构清晰**: Controllers → Services → Models 分离良好
2. **密码哈希使用 bcrypt**: `password_service.py:28` 采用安全算法
3. **双 Token 机制**: Access + Refresh Token 设计合理
4. **输入验证完善**: Pydantic 模型验证覆盖所有字段
5. **限流逻辑完备**: IP 级和账号级双重限流保护
6. **配置外部化**: 使用 pydantic-settings 支持环境变量

---

## 🧪 测试覆盖缺失

**状态**: 无测试文件  
**建议测试清单**:

| 模块 | 测试类型 | 优先级 |
|------|----------|--------|
| `jwt_service.py` | 单元测试（Token 生成/验证/刷新） | P0 |
| `password_service.py` | 单元测试（哈希/验证） | P0 |
| `auth_controller.py` | 集成测试（登录流程） | P0 |
| `security_middleware.py` | 单元测试（锁定逻辑） | P1 |
| `user_repository.py` | 单元测试（CRUD） | P1 |

---

## 📊 功能完整性检查

| 功能需求 | 实现状态 | 代码位置 |
|----------|----------|----------|
| F1 账号密码登录 | ✅ 已实现 | `auth_controller.py:30-70` |
| F1 多字段支持 | ✅ 已实现 | `user_repository.py:45-60` |
| F1 加密传输 | ⚠️ 需 HTTPS | 部署层配置 |
| F1 记住密码 | ✅ 已实现 | `jwt_service.py:70-80` |
| F1 自动登录 | ⚠️ 前端依赖 | Token 有效期内自动刷新 |
| F2 手机验证码登录 | ❌ 未实现 | - |
| F3 OAuth 登录 | ❌ 未实现 | - |
| F4 登录锁定 | ✅ 已实现 | `security_middleware.py:45-90` |
| F4 图形验证码 | ⚠️ 框架存在 | 需补充生成/校验逻辑 |
| F4 防暴力破解 | ✅ 已实现 | 双重限流机制 |
| F4 CSRF | ❌ 未实现 | - |
| F4 异地提醒 | ❌ 未实现 | - |
| F5 JWT 双 Token | ✅ 已实现 | `jwt_service.py:40-85` |
| F5 静默刷新 | ✅ 已实现 | `jwt_service.py:175-185` |
| F5 全局登出 | ⚠️ 部分实现 | `auth_controller.py:115-125` (无实际清黑名单) |
| F6 忘记密码 | ❌ 未实现 | - |

---

## 📝 修复优先级建议

### P0 - 立即修复
1. 移除硬编码密钥，强制环境变量配置
2. 实现 CSRF 保护
3. 补充核心模块单元测试

### P1 - 短期修复
1. 迁移内存存储至 Redis（黑名单、锁定记录）
2. 实现 Refresh Token Rotation
3. 添加审计日志

### P2 - 中期优化
1. 实现 F2/F3/F6 功能
2. 接入真实数据库
3. 国际化错误消息

---

## 🔧 建议架构改进

```
当前架构（内存模式）:
┌─────────────────┐
│   FastAPI App   │
└────────┬────────┘
         │
    ┌────▼────┐
    │ Memory  │ ← 进程重启数据丢失
    │ Storage │
    └─────────┘

建议架构（生产就绪）:
┌─────────────────┐
│   FastAPI App   │
└────────┬────────┘
         │
    ┌────▼────┐      ┌──────────┐
    │  Redis  │◄────►│PostgreSQL│
    │(Session)│      │(Persist) │
    └─────────┘      └──────────┘
```

---

## ✍️ 评审结论

本代码库实现了 T3 账号登录系统的核心功能（账号密码登录、安全防护、会话管理），代码质量良好，架构设计合理。但存在若干安全隐患需要修复，建议在投入生产前完成 P0/P1 级修复，并补充自动化测试。

**评审结果**: ⚠️ **需要修改后合并**  
**建议下一步**: 
1. 修复 C1-C4 严重问题
2. 补充 jwt_service 和 password_service 单元测试
3. 重新评审后合并