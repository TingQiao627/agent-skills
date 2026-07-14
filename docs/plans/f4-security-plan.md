# F4 安全防护实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development

**Goal:** 实现登录安全防护，包括登录锁定、图形验证码、防暴力破解、CSRF 防护、异地登录提醒。

**Architecture:** 基于 Spring Security + Redis 实现多层防护，图形验证码使用 Google Kaptcha，异地登录基于 IP 地理位置库。

**Tech Stack:** Spring Security 6, Redis, Google Kaptcha, IP2Region

---

## 全局约束

- 登录失败锁定: 连续失败 5 次，锁定 30 分钟
- 图形验证码触发: 连续失败 3 次后必须验证
- 密码强度: 8-20位，包含大小写字母和数字
- CSRF Token 有效期: 30分钟
- 异地登录判定: 城市/省份变化即触发

---

## Task 1: 登录尝试限制服务

**Files:**
- Create: `src/main/java/com/example/login/security/LoginAttemptService.java`

**Steps:**

- [ ] 实现登录失败计数与锁定
```java
@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final StringRedisTemplate redisTemplate;
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_MINUTES = 30;
    
    private static final String ATTEMPT_KEY = "login:attempt:";
    private static final String LOCK_KEY = "login:lock:";
    
    public void loginFailed(String key) {
        String attemptCountKey = ATTEMPT_KEY + key;
        Long attempts = redisTemplate.opsForValue().increment(attemptCountKey);
        redisTemplate.expire(attemptCountKey, 1, TimeUnit.HOURS);
        
        if (attempts >= MAX_ATTEMPTS) {
            lockAccount(key);
        }
    }
    
    public void loginSucceeded(String key) {
        redisTemplate.delete(ATTEMPT_KEY + key);
        redisTemplate.delete(LOCK_KEY + key);
    }
    
    public boolean isBlocked(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOCK_KEY + key));
    }
    
    private void lockAccount(String key) {
        redisTemplate.opsForValue().set(LOCK_KEY + key, 
            String.valueOf(System.currentTimeMillis()), 
            LOCK_TIME_MINUTES, TimeUnit.MINUTES);
    }
}
```

**Verification:** `mvn test -Dtest=LoginAttemptServiceTest`

---

## Task 2: 图形验证码

**Files:**
- Create: `src/main/java/com/example/login/controller/CaptchaController.java`
- Create: `src/main/java/com/example/login/service/impl/ImageCaptchaServiceImpl.java`

**Steps:**

- [ ] 生成图形验证码
```java
@RestController
@RequestMapping("/api/auth/captcha")
@RequiredArgsConstructor
public class CaptchaController {
    private final Producer captchaProducer;
    private final StringRedisTemplate redisTemplate;
    
    @GetMapping
    public void getCaptcha(HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        String capText = captchaProducer.createText();
        String captchaKey = UUID.randomUUID().toString();
        
        // 存储到 Redis，5分钟有效
        redisTemplate.opsForValue().set(
            "captcha:" + captchaKey, capText, 5, TimeUnit.MINUTES);
        
        BufferedImage bi = captchaProducer.createImage(capText);
        
        // 返回 captchaKey 和图片
        response.setHeader("X-Captcha-Key", captchaKey);
        response.setContentType("image/jpeg");
        ImageIO.write(bi, "jpg", response.getOutputStream());
    }
    
    @PostMapping("/verify")
    public Result<Boolean> verifyCaptcha(@RequestBody CaptchaRequest request) {
        String storedCode = redisTemplate.opsForValue()
            .get("captcha:" + request.getCaptchaKey());
        boolean valid = request.getCode().equalsIgnoreCase(storedCode);
        
        if (valid) {
            redisTemplate.delete("captcha:" + request.getCaptchaKey());
        }
        
        return Result.success(valid);
    }
}
```

**Verification:** `mvn test -Dtest=CaptchaControllerTest`

---

## Task 3: CSRF 防护

**Files:**
- Modify: `src/main/java/com/example/login/config/SecurityConfig.java`

**Steps:**

- [ ] 配置 CSRF 防护
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .sessionAuthenticationStrategy(sessionFixationProtectionStrategy())
        );
    
    return http.build();
}

@Component
public class CsrfTokenGeneratorFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain filterChain) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
        }
        filterChain.doFilter(request, response);
    }
}
```

**Verification:** 集成测试验证 CSRF Token

---

## Task 4: 异地登录提醒

**Files:**
- Create: `src/main/java/com/example/login/service/LoginLocationService.java`
- Create: `src/main/java/com/example/login/listener/LoginEventListener.java`

**Steps:**

- [ ] IP 地理位置解析
```java
@Service
@RequiredArgsConstructor
public class LoginLocationService {
    private final Ip2RegionClient ip2RegionClient;
    
    public LoginLocation getLocation(String ip) {
        String region = ip2RegionClient.search(ip);
        // 解析省份、城市
        return LoginLocation.fromRegion(region);
    }
    
    public boolean isUnusualLogin(Long userId, String currentIp) {
        User user = userMapper.selectById(userId);
        String lastIp = user.getLastLoginIp();
        
        if (lastIp == null) return false;
        
        LoginLocation lastLocation = getLocation(lastIp);
        LoginLocation currentLocation = getLocation(currentIp);
        
        // 判断城市是否变化
        return !Objects.equals(lastLocation.getCity(), currentLocation.getCity());
    }
}

@Component
@RequiredArgsConstructor
public class LoginEventListener {
    private final LoginLocationService locationService;
    private final NotificationService notificationService;
    
    @EventListener
    public void onLoginSuccess(LoginSuccessEvent event) {
        if (locationService.isUnusualLogin(event.getUserId(), event.getIp())) {
            // 发送异地登录提醒
            notificationService.sendUnusualLoginAlert(
                event.getUserId(), 
                event.getIp(), 
                event.getDeviceInfo()
            );
        }
    }
}
```

**Verification:** `mvn test -Dtest=LoginLocationServiceTest`

---

## 验证清单

- [ ] 登录锁定机制正常
- [ ] 图形验证码生成与验证正确
- [ ] CSRF Token 验证通过
- [ ] 异地登录提醒触发
- [ ] 单元测试覆盖率 ≥ 80%

---

**创建时间:** 2026-07-14