# F6 忘记密码实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development

**Goal:** 实现忘记密码功能，支持身份验证、密码重置、多渠道通知。

**Architecture:** 三步流程：身份验证 → 设置新密码 → 通知确认，支持手机/邮箱验证。

**Tech Stack:** Spring Boot, Redis, 短信服务, 邮件服务

---

## 全局约束

- 重置链接有效期: 30分钟
- 验证码有效期: 5分钟
- 重置次数限制: 同一账号每天最多 5 次
- 新密码要求: 与旧密码不同，强度符合规范

---

## Task 1: 身份验证服务

**Files:**
- Create: `src/main/java/com/example/login/service/PasswordResetService.java`
- Create: `src/main/java/com/example/login/service/impl/PasswordResetServiceImpl.java`

**Steps:**

- [ ] 实现身份验证流程
```java
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    private final UserMapper userMapper;
    private final CaptchaService captchaService;
    private final StringRedisTemplate redisTemplate;
    
    private static final String RESET_TOKEN_KEY = "password:reset:";
    
    @Override
    public ResetTokenResponse sendResetCode(SendResetCodeRequest request) {
        // 1. 验证用户是否存在
        User user = userMapper.selectByAccount(request.getAccount());
        if (user == null) {
            throw new AuthException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 2. 检查重置频率
        checkResetLimit(request.getAccount());
        
        // 3. 生成重置 Token
        String resetToken = UUID.randomUUID().toString();
        String code = captchaService.generateSmsCode(user.getPhone());
        
        // 4. 存储 Token 和验证码
        redisTemplate.opsForValue().set(
            RESET_TOKEN_KEY + resetToken, 
            JSON.toJSONString(new ResetInfo(user.getId(), code, request.getVerifyMethod())),
            30, TimeUnit.MINUTES);
        
        // 5. 发送验证码
        if ("phone".equals(request.getVerifyMethod())) {
            smsService.sendResetCode(user.getPhone(), code);
        } else {
            emailService.sendResetCode(user.getEmail(), code);
        }
        
        return new ResetTokenResponse(resetToken, 
            maskPhoneOrEmail(user.getPhone()));
    }
    
    @Override
    public boolean verifyResetCode(String resetToken, String code) {
        String infoJson = redisTemplate.opsForValue()
            .get(RESET_TOKEN_KEY + resetToken);
        if (infoJson == null) {
            throw new AuthException(ErrorCode.RESET_TOKEN_EXPIRED);
        }
        
        ResetInfo resetInfo = JSON.parseObject(infoJson, ResetInfo.class);
        return code.equals(resetInfo.getCode());
    }
}
```

**Verification:** `mvn test -Dtest=PasswordResetServiceTest`

---

## Task 2: 密码重置控制器

**Files:**
- Create: `src/main/java/com/example/login/controller/PasswordController.java`

**Steps:**

- [ ] 实现重置密码接口
```java
@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {
    private final PasswordResetService passwordResetService;
    private final UserService userService;
    
    @PostMapping("/reset/send-code")
    public Result<ResetTokenResponse> sendResetCode(
            @Valid @RequestBody SendResetCodeRequest request) {
        ResetTokenResponse response = passwordResetService.sendResetCode(request);
        return Result.success(response);
    }
    
    @PostMapping("/reset/verify")
    public Result<Boolean> verifyResetCode(
            @Valid @RequestBody VerifyResetCodeRequest request) {
        boolean valid = passwordResetService.verifyResetCode(
            request.getResetToken(), request.getCode());
        return Result.success(valid);
    }
    
    @PostMapping("/reset/confirm")
    public Result<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        // 1. 验证重置 Token 和验证码
        if (!passwordResetService.verifyResetCode(
                request.getResetToken(), request.getCode())) {
            throw new AuthException(ErrorCode.INVALID_VERIFY_CODE);
        }
        
        // 2. 获取用户信息
        Long userId = passwordResetService.getUserIdByResetToken(
            request.getResetToken());
        
        // 3. 解密并验证新密码
        String newPassword = encryptionService.decrypt(request.getNewPassword());
        validatePasswordStrength(newPassword);
        
        // 4. 更新密码
        userService.updatePassword(userId, newPassword);
        
        // 5. 清除重置 Token
        passwordResetService.invalidateResetToken(request.getResetToken());
        
        // 6. 发送通知
        notificationService.sendPasswordChangedNotice(userId);
        
        return Result.success();
    }
}
```

**Verification:** `mvn test -Dtest=PasswordControllerTest`

---

## Task 3: 安全检查

**Files:**
- Modify: `src/main/java/com/example/login/service/impl/PasswordResetServiceImpl.java`

**Steps:**

- [ ] 实现安全检查逻辑
```java
private void checkResetLimit(String account) {
    String key = "password:reset:limit:" + account;
    Long count = redisTemplate.opsForValue().increment(key);
    if (count == 1) {
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }
    if (count > 5) {
        throw new AuthException(ErrorCode.RESET_LIMIT_EXCEEDED);
    }
}

private void validatePasswordStrength(String password) {
    if (password.length() < 8 || password.length() > 20) {
        throw new AuthException(ErrorCode.PASSWORD_LENGTH_INVALID);
    }
    
    if (!Pattern.matches(".*[A-Z].*", password) ||
        !Pattern.matches(".*[a-z].*", password) ||
        !Pattern.matches(".*\\d.*", password)) {
        throw new AuthException(ErrorCode.PASSWORD_STRENGTH_WEAK);
    }
}
```

**Verification:** `mvn test -Dtest=PasswordResetServiceTest`

---

## Task 4: 通知服务

**Files:**
- Create: `src/main/java/com/example/login/service/NotificationService.java`

**Steps:**

- [ ] 实现多渠道通知
```java
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final SmsService smsService;
    private final EmailService emailService;
    
    @Override
    public void sendPasswordChangedNotice(Long userId) {
        User user = userMapper.selectById(userId);
        
        // 短信通知
        if (user.getPhone() != null) {
            smsService.sendPasswordChangedNotice(user.getPhone());
        }
        
        // 邮件通知
        if (user.getEmail() != null) {
            emailService.sendPasswordChangedNotice(user.getEmail());
        }
    }
}
```

**Verification:** `mvn test -Dtest=NotificationServiceTest`

---

## 验证清单

- [ ] 身份验证流程正确
- [ ] 验证码发送与验证正常
- [ ] 密码重置功能正常
- [ ] 重置限制生效
- [ ] 通知发送成功
- [ ] 单元测试覆盖率 ≥ 80%

---

**创建时间:** 2026-07-14