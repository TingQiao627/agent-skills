# F2 手机号验证码登录实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development

**Goal:** 实现手机号验证码登录功能，支持短信频率限制、自动注册、验证码有效期管理。

**Architecture:** 使用 Redis 存储验证码，消息队列异步发送短信，支持自动注册新用户。

**Tech Stack:** Spring Boot, Redis, 阿里云短信 SDK/腾讯云短信 SDK

---

## 全局约束

- 验证码长度: 6位数字
- 验证码有效期: 5分钟
- 发送频率: 同一手机号 60秒内只能发送 1 次
- 日发送上限: 同一手机号每天最多 10 次
- IP 限制: 同一 IP 每小时最多发送 20 次

---

## Task 1: 验证码服务

**Files:**
- Create: `src/main/java/com/example/login/service/CaptchaService.java`
- Create: `src/main/java/com/example/login/service/impl/CaptchaServiceImpl.java`
- Test: `src/test/java/com/example/login/service/CaptchaServiceTest.java`

**Steps:**

- [ ] 创建验证码服务接口
```java
public interface CaptchaService {
    String generateSmsCode(String phone);
    boolean validateSmsCode(String phone, String code);
    void sendSmsCode(String phone) throws SmsException;
    void checkSendLimit(String phone, String ip);
}
```

- [ ] 实现验证码生成与 Redis 存储
```java
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {
    private final StringRedisTemplate redisTemplate;
    private final SmsService smsService;
    
    private static final String SMS_CODE_KEY = "sms:code:";
    private static final String SMS_COUNT_KEY = "sms:count:";
    private static final int CODE_EXPIRY_MINUTES = 5;
    
    @Override
    public String generateSmsCode(String phone) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        String key = SMS_CODE_KEY + phone;
        redisTemplate.opsForValue().set(key, code, 
            CODE_EXPIRY_MINUTES, TimeUnit.MINUTES);
        return code;
    }
    
    @Override
    public void sendSmsCode(String phone) {
        checkSendLimit(phone, IpUtils.getClientIp());
        
        String code = generateSmsCode(phone);
        smsService.sendVerificationCode(phone, code);
        
        // 记录发送次数
        String countKey = SMS_COUNT_KEY + phone + ":" + LocalDate.now();
        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, 1, TimeUnit.DAYS);
    }
    
    @Override
    public boolean validateSmsCode(String phone, String code) {
        String key = SMS_CODE_KEY + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (code.equals(storedCode)) {
            redisTemplate.delete(key); // 一次性使用
            return true;
        }
        return false;
    }
}
```

**Verification:** `mvn test -Dtest=CaptchaServiceTest`

---

## Task 2: 短信发送服务

**Files:**
- Create: `src/main/java/com/example/login/service/SmsService.java`
- Create: `src/main/java/com/example/login/service/impl/AliyunSmsServiceImpl.java`
- Test: `src/test/java/com/example/login/service/SmsServiceTest.java`

**Steps:**

- [ ] 实现阿里云短信服务
```java
@Service
@RequiredArgsConstructor
public class AliyunSmsServiceImpl implements SmsService {
    @Value("${sms.aliyun.access-key-id}")
    private String accessKeyId;
    
    @Value("${sms.aliyun.access-key-secret}")
    private String accessKeySecret;
    
    @Value("${sms.aliyun.sign-name}")
    private String signName;
    
    @Value("${sms.aliyun.template-code}")
    private String templateCode;
    
    @Override
    public void sendVerificationCode(String phone, String code) {
        DefaultProfile profile = DefaultProfile.getProfile(
            "cn-hangzhou", accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);
        
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", 
            "{\"code\":\"" + code + "\"}");
        
        try {
            CommonResponse response = client.getCommonResponse(request);
            // 解析响应验证发送结果
        } catch (Exception e) {
            throw new SmsException("短信发送失败", e);
        }
    }
}
```

**Verification:** `mvn test -Dtest=SmsServiceTest`

---

## Task 3: 手机号登录服务

**Files:**
- Modify: `src/main/java/com/example/login/service/AuthService.java`
- Modify: `src/main/java/com/example/login/service/impl/AuthServiceImpl.java`

**Steps:**

- [ ] 添加手机号登录 DTO
```java
@Data
public class PhoneLoginRequest {
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String code;
    
    private Boolean autoRegister = true;
}
```

- [ ] 实现手机号登录逻辑（支持自动注册）
```java
@Override
public LoginResponse loginByPhone(PhoneLoginRequest request) {
    // 1. 验证短信验证码
    if (!captchaService.validateSmsCode(request.getPhone(), request.getCode())) {
        throw new AuthException(ErrorCode.INVALID_SMS_CODE);
    }
    
    // 2. 查询用户
    User user = userMapper.selectByPhone(request.getPhone());
    
    // 3. 自动注册
    if (user == null) {
        if (!request.getAutoRegister()) {
            throw new AuthException(ErrorCode.USER_NOT_FOUND);
        }
        user = registerUserByPhone(request.getPhone());
    }
    
    // 4. 检查用户状态
    if (user.getStatus() == UserStatus.LOCKED) {
        throw new AuthException(ErrorCode.ACCOUNT_LOCKED);
    }
    
    // 5. 生成 Token
    UserDetails userDetails = createUserDetails(user);
    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, false);
    
    return new LoginResponse(accessToken, refreshToken, 
        jwtTokenProvider.getAccessTokenExpiry(), buildUserInfo(user));
}

private User registerUserByPhone(String phone) {
    User user = new User();
    user.setPhone(phone);
    user.setNickname("用户" + phone.substring(7));
    user.setStatus(UserStatus.NORMAL);
    userMapper.insert(user);
    return user;
}
```

**Verification:** `mvn test -Dtest=AuthServiceTest`

---

## Task 4: 控制器接口

**Files:**
- Modify: `src/main/java/com/example/login/controller/AuthController.java`

**Steps:**

- [ ] 添加手机号登录接口
```java
@PostMapping("/login/phone")
public Result<LoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
    LoginResponse response = authService.loginByPhone(request);
    return Result.success(response);
}

@PostMapping("/sms/send")
@RateLimit(key = "sms", period = 60, count = 1)
public Result<Void> sendSmsCode(@Valid @RequestBody SendSmsRequest request) {
    captchaService.sendSmsCode(request.getPhone());
    return Result.success();
}
```

**Verification:** `mvn test -Dtest=AuthControllerTest`

---

## Task 5: 发送频率限制

**Files:**
- Create: `src/main/java/com/example/login/annotation/RateLimit.java`
- Create: `src/main/java/com/example/login/aspect/RateLimitAspect.java`

**Steps:**

- [ ] 实现基于 Redis 的频率限制注解
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String key();
    int period(); // 秒
    int count();
}

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final StringRedisTemplate redisTemplate;
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) {
        String key = "rate:" + rateLimit.key() + ":" + getTargetKey(point);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, rateLimit.period(), TimeUnit.SECONDS);
        }
        if (count > rateLimit.count()) {
            throw new RateLimitException("操作过于频繁");
        }
        return point.proceed();
    }
}
```

**Verification:** `mvn test -Dtest=RateLimitAspectTest`

---

## 验证清单

- [ ] 验证码生成与验证正确
- [ ] 短信发送频率限制生效
- [ ] 自动注册功能正常
- [ ] 手机号登录流程完整
- [ ] 单元测试覆盖率 ≥ 80%

---

**创建时间:** 2026-07-14