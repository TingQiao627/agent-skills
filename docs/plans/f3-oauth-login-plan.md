# F3 第三方 OAuth 登录实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development

**Goal:** 实现微信、支付宝、企业微信第三方登录，支持账号绑定与解绑。

**Architecture:** Spring Security OAuth2 Client，统一 OAuth 认证流程，支持账号关联。

**Tech Stack:** Spring Security OAuth2 Client, 微信开放平台 SDK, 支付宝开放平台 SDK

---

## 全局约束

- OAuth 状态参数有效期: 10分钟
- 绑定状态: 每种类型只能绑定一个第三方账号
- Token 缓存: Redis 存储第三方 Token
- 回调地址: 必须在白名单中

---

## Task 1: OAuth 配置

**Files:**
- Create: `src/main/java/com/example/login/config/OAuthConfig.java`
- Create: `src/main/resources/application-oauth.yml`

**Steps:**

- [ ] 配置第三方登录参数
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          wechat:
            client-id: ${WECHAT_APP_ID}
            client-secret: ${WECHAT_APP_SECRET}
            redirect-uri: "{baseUrl}/login/oauth2/code/wechat"
            scope: snsapi_userinfo
          alipay:
            client-id: ${ALIPAY_APP_ID}
            client-secret: ${ALIPAY_PRIVATE_KEY}
            redirect-uri: "{baseUrl}/login/oauth2/code/alipay"
            scope: auth_user
          wework:
            client-id: ${WEWORK_CORP_ID}
            client-secret: ${WEWORK_AGENT_SECRET}
            redirect-uri: "{baseUrl}/login/oauth2/code/wework"
```

**Verification:** 配置文件验证

---

## Task 2: OAuth 认证服务

**Files:**
- Create: `src/main/java/com/example/login/service/OAuthService.java`
- Create: `src/main/java/com/example/login/service/impl/OAuthServiceImpl.java`

**Steps:**

- [ ] 实现统一 OAuth 登录逻辑
```java
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {
    private final UserOAuthMapper userOAuthMapper;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    public LoginResponse loginByOAuth(String oauthType, OAuthUser oauthUser) {
        // 1. 查询绑定关系
        UserOAuth userOAuth = userOAuthMapper.selectByOAuthId(
            oauthType, oauthUser.getOpenId());
        
        User user;
        if (userOAuth != null) {
            // 已绑定，直接登录
            user = userMapper.selectById(userOAuth.getUserId());
        } else {
            // 未绑定，创建新用户并绑定
            user = createAndBindOAuthUser(oauthType, oauthUser);
        }
        
        // 2. 生成 Token
        UserDetails userDetails = createUserDetails(user);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, false);
        
        return new LoginResponse(accessToken, refreshToken, 
            jwtTokenProvider.getAccessTokenExpiry(), buildUserInfo(user));
    }
    
    @Override
    public void bindOAuth(Long userId, String oauthType, OAuthUser oauthUser) {
        // 检查是否已绑定
        if (userOAuthMapper.existsByUserIdAndType(userId, oauthType)) {
            throw new AuthException(ErrorCode.OAUTH_ALREADY_BOUND);
        }
        
        UserOAuth userOAuth = new UserOAuth();
        userOAuth.setUserId(userId);
        userOAuth.setOauthType(oauthType);
        userOAuth.setOauthId(oauthUser.getOpenId());
        userOAuth.setOauthName(oauthUser.getNickname());
        userOAuth.setOauthAvatar(oauthUser.getAvatar());
        
        userOAuthMapper.insert(userOAuth);
    }
    
    @Override
    public void unbindOAuth(Long userId, String oauthType) {
        userOAuthMapper.deleteByUserIdAndType(userId, oauthType);
    }
}
```

**Verification:** `mvn test -Dtest=OAuthServiceTest`

---

## Task 3: OAuth 控制器

**Files:**
- Create: `src/main/java/com/example/login/controller/OAuthController.java`

**Steps:**

- [ ] 实现 OAuth 登录接口
```java
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthService oauthService;
    
    @GetMapping("/login/{type}")
    public void oauthLogin(@PathVariable String type, 
            HttpServletResponse response) throws IOException {
        String authorizeUrl = oauthService.getAuthorizeUrl(type);
        response.sendRedirect(authorizeUrl);
    }
    
    @GetMapping("/callback/{type}")
    public Result<LoginResponse> oauthCallback(@PathVariable String type,
            @RequestParam String code, @RequestParam String state) {
        OAuthUser oauthUser = oauthService.getUserInfo(type, code);
        LoginResponse response = oauthService.loginByOAuth(type, oauthUser);
        return Result.success(response);
    }
    
    @PostMapping("/bind")
    public Result<Void> bindOAuth(@RequestBody BindOAuthRequest request) {
        Long userId = getCurrentUserId();
        oauthService.bindOAuth(userId, request.getOauthType(), request.getOauthUser());
        return Result.success();
    }
    
    @DeleteMapping("/unbind/{type}")
    public Result<Void> unbindOAuth(@PathVariable String type) {
        Long userId = getCurrentUserId();
        oauthService.unbindOAuth(userId, type);
        return Result.success();
    }
}
```

**Verification:** `mvn test -Dtest=OAuthControllerTest`

---

## 验证清单

- [ ] 微信登录流程正常
- [ ] 支付宝登录流程正常
- [ ] 企业微信登录流程正常
- [ ] 账号绑定功能正常
- [ ] 账号解绑功能正常
- [ ] 单元测试覆盖率 ≥ 80%

---

**创建时间:** 2026-07-14