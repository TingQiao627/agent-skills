# F1 账号密码登录实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development

**Goal:** 实现账号密码登录功能，支持多字段登录（用户名/手机号/邮箱）、密码加密传输、记住密码和自动登录功能。

**Architecture:** 采用前后端分离架构，前端使用 RSA 加密密码传输，后端使用 BCrypt 验证密码，支持多字段动态匹配登录。

**Tech Stack:** Spring Security 6, BCrypt, RSA-2048, JWT

---

## 全局约束

- 密码加密: 前端 RSA 公钥加密 → 后端私钥解密 → BCrypt 验证
- 登录字段优先级: 手机号 > 邮箱 > 用户名
- Token 有效期: 访问 Token 2小时, 刷新 Token 7天
- 记住密码: 延长刷新 Token 至 30天

---

## Task 1: 密码加密服务

**Files:**
- Create: `src/main/java/com/example/login/security/EncryptionService.java`
- Create: `src/main/java/com/example/login/config/EncryptionConfig.java`
- Test: `src/test/java/com/example/login/security/EncryptionServiceTest.java`

**Interfaces:**
- Produces: `EncryptionService.encrypt(String plaintext)`, `EncryptionService.decrypt(String ciphertext)`

**Steps:**

- [ ] 创建 `EncryptionConfig.java` 配置类，从配置文件读取 RSA 密钥对
```java
@Configuration
@ConfigurationProperties(prefix = "encryption.rsa")
public class EncryptionConfig {
    private String publicKey;
    private String privateKey;
    // getters, setters
}
```

- [ ] 创建 `EncryptionService.java` 实现 RSA 加解密
```java
@Service
public class EncryptionService {
    @Value("${encryption.rsa.private-key}")
    private String privateKey;
    
    private KeyFactory keyFactory;
    
    @PostConstruct
    public void init() throws Exception {
        keyFactory = KeyFactory.getInstance("RSA");
    }
    
    public String decrypt(String encryptedBase64) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
            Base64.getDecoder().decode(privateKey)
        );
        PrivateKey privKey = keyFactory.generatePrivate(keySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    }
}
```

- [ ] 编写单元测试验证加解密流程
```java
@SpringBootTest
class EncryptionServiceTest {
    @Autowired
    EncryptionService encryptionService;
    
    @Test
    void testEncryptDecrypt() {
        String original = "testPassword123";
        // 使用公钥加密
        String encrypted = encryptWithPublicKey(original);
        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(original, decrypted);
    }
}
```

**Verification:** 运行 `mvn test -Dtest=EncryptionServiceTest`，测试通过

---

## Task 2: 用户认证服务

**Files:**
- Create: `src/main/java/com/example/login/service/AuthService.java`
- Create: `src/main/java/com/example/login/service/impl/AuthServiceImpl.java`
- Modify: `src/main/java/com/example/login/security/JwtTokenProvider.java`
- Test: `src/test/java/com/example/login/service/AuthServiceTest.java`

**Interfaces:**
- Consumes: `JwtTokenProvider.generateToken(UserDetails userDetails)`
- Produces: `AuthService.login(LoginRequest request)`

**Steps:**

- [ ] 创建 `LoginRequest.java` DTO
```java
@Data
public class LoginRequest {
    @NotBlank(message = "登录账号不能为空")
    private String account; // 用户名/手机号/邮箱
    
    @NotBlank(message = "密码不能为空")
    private String password; // RSA 加密的密码
    
    private Boolean rememberMe = false;
    
    private String captchaKey;
    private String captchaCode;
}
```

- [ ] 创建 `LoginResponse.java` DTO
```java
@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo userInfo;
}
```

- [ ] 创建 `AuthServiceImpl.java` 实现登录逻辑
```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 检查登录尝试次数
        String clientIp = IpUtils.getClientIp();
        if (loginAttemptService.isBlocked(clientIp)) {
            throw new AuthException(ErrorCode.ACCOUNT_LOCKED);
        }
        
        // 2. 解密密码
        String rawPassword = encryptionService.decrypt(request.getPassword());
        
        // 3. 根据账号类型查询用户
        User user = findUserByAccount(request.getAccount());
        if (user == null) {
            loginAttemptService.loginFailed(clientIp);
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        // 4. 验证密码
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            loginAttemptService.loginFailed(clientIp);
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        // 5. 检查用户状态
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AuthException(ErrorCode.ACCOUNT_LOCKED);
        }
        
        // 6. 生成 Token
        UserDetails userDetails = createUserDetails(user);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(
            userDetails, request.getRememberMe()
        );
        
        // 7. 重置登录失败计数
        loginAttemptService.loginSucceeded(clientIp);
        
        return new LoginResponse(accessToken, refreshToken, 
            jwtTokenProvider.getAccessTokenExpiry(), buildUserInfo(user));
    }
    
    private User findUserByAccount(String account) {
        if (ValidationUtils.isPhone(account)) {
            return userMapper.selectByPhone(account);
        } else if (ValidationUtils.isEmail(account)) {
            return userMapper.selectByEmail(account);
        } else {
            return userMapper.selectByUsername(account);
        }
    }
}
```

- [ ] 编写单元测试
```java
@SpringBootTest
class AuthServiceTest {
    @MockBean
    private UserMapper userMapper;
    
    @Autowired
    private AuthService authService;
    
    @Test
    void testLoginWithUsername() {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        
        when(userMapper.selectByUsername("testuser")).thenReturn(user);
        
        // 执行登录
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser");
        request.setPassword(encryptPassword("password123"));
        
        LoginResponse response = authService.login(request);
        
        // 验证结果
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }
}
```

**Verification:** 运行 `mvn test -Dtest=AuthServiceTest`，测试通过

---

## Task 3: 登录控制器

**Files:**
- Create: `src/main/java/com/example/login/controller/AuthController.java`
- Test: `src/test/java/com/example/login/controller/AuthControllerTest.java`

**Interfaces:**
- Consumes: `AuthService.login()`
- Produces: `POST /api/auth/login`

**Steps:**

- [ ] 创建 `AuthController.java`
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }
    
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        authService.logout(token);
        return Result.success();
    }
}
```

- [ ] 编写集成测试
```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser");
        request.setPassword(encryptPassword("password123"));
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists());
    }
}
```

**Verification:** 运行 `mvn test -Dtest=AuthControllerTest`，测试通过

---

## Task 4: 记住密码与自动登录

**Files:**
- Modify: `src/main/java/com/example/login/security/JwtTokenProvider.java`
- Modify: `src/main/java/com/example/login/service/impl/AuthServiceImpl.java`
- Test: `src/test/java/com/example/login/security/JwtTokenProviderTest.java`

**Steps:**

- [ ] 修改 `JwtTokenProvider` 支持延长 Token
```java
public String generateRefreshToken(UserDetails userDetails, boolean rememberMe) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("type", "refresh");
    
    long expiry = rememberMe ? 
        rememberMeExpiryInMs : refreshTokenExpiryInMs;
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiry))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact();
}
```

- [ ] 添加自动登录验证逻辑
```java
public boolean validateAutoLogin(String refreshToken) {
    try {
        Jwts.parser().setSigningKey(secretKey).parseClaimsJws(refreshToken);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

**Verification:** 运行 `mvn test -Dtest=JwtTokenProviderTest`，测试通过

---

## Task 5: 前端集成（示例）

**Files:**
- Create: `src/main/resources/static/js/login.js`
- Modify: `src/main/resources/static/login.html`

**Steps:**

- [ ] 实现前端登录表单（Vue 3 示例）
```vue
<template>
  <form @submit.prevent="handleLogin">
    <input v-model="form.account" placeholder="用户名/手机号/邮箱" />
    <input v-model="form.password" type="password" placeholder="密码" />
    <checkbox v-model="form.rememberMe">记住密码</checkbox>
    <button type="submit">登录</button>
  </form>
</template>

<script setup>
const handleLogin = async () => {
  // RSA 加密密码
  const encryptedPassword = encryptWithPublicKey(form.password);
  
  const response = await axios.post('/api/auth/login', {
    account: form.account,
    password: encryptedPassword,
    rememberMe: form.rememberMe
  });
  
  // 存储 Token
  localStorage.setItem('accessToken', response.data.accessToken);
  localStorage.setItem('refreshToken', response.data.refreshToken);
};
</script>
```

**Verification:** 手动测试登录流程正常

---

## 验证清单

- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试通过
- [ ] 密码加密传输验证
- [ ] 多字段登录测试
- [ ] 记住密码功能验证
- [ ] 性能测试: 登录接口响应 < 200ms

---

**创建时间:** 2026-07-14