# F5 会话管理实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development

**Goal:** 实现 JWT 双 Token 认证机制，支持静默刷新、全局登出、会话监控。

**Architecture:** Access Token + Refresh Token 双令牌机制，Redis 存储会话状态，支持多设备登录管理。

**Tech Stack:** Spring Security 6, JWT (jjwt 0.12), Redis

---

## 全局约束

- Access Token 有效期: 2小时
- Refresh Token 有效期: 7天（记住密码: 30天）
- Token 刷新阈值: 过期前 30 分钟自动刷新
- 最大并发登录: 5 个设备
- Token 黑名单: 登出后立即失效

---

## Task 1: JWT Token 提供者

**Files:**
- Create: `src/main/java/com/example/login/security/JwtTokenProvider.java`
- Test: `src/test/java/com/example/login/security/JwtTokenProviderTest.java`

**Steps:**

- [ ] 实现 JWT Token 生成与验证
```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.access-token-expiry:7200000}") // 2小时
    private long accessTokenExpiryInMs;
    
    @Value("${jwt.refresh-token-expiry:604800000}") // 7天
    private long refreshTokenExpiryInMs;
    
    private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_BLACKLIST_KEY = "jwt:blacklist:";
    
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiryInMs))
            .setId(UUID.randomUUID().toString())
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
    
    public String generateRefreshToken(UserDetails userDetails, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        
        long expiry = rememberMe ? refreshTokenExpiryInMs * 4 : refreshTokenExpiryInMs;
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiry))
            .setId(UUID.randomUUID().toString())
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            // 检查黑名单
            if (isTokenBlacklisted(token)) {
                return false;
            }
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
    
    public void invalidateToken(String token) {
        String jti = getTokenId(token);
        long expiry = getTokenExpiry(token);
        redisTemplate.opsForValue().set(
            TOKEN_BLACKLIST_KEY + jti, "1", 
            expiry - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
    
    private boolean isTokenBlacklisted(String token) {
        String jti = getTokenId(token);
        return Boolean.TRUE.equals(
            redisTemplate.hasKey(TOKEN_BLACKLIST_KEY + jti));
    }
}
```

**Verification:** `mvn test -Dtest=JwtTokenProviderTest`

---

## Task 2: JWT 认证过滤器

**Files:**
- Create: `src/main/java/com/example/login/security/JwtAuthenticationFilter.java`

**Steps:**

- [ ] 实现 JWT 认证过滤器
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain filterChain) {
        try {
            String token = resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource()
                    .buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**Verification:** 集成测试验证过滤器链

---

## Task 3: 静默刷新机制

**Files:**
- Create: `src/main/java/com/example/login/controller/TokenController.java`

**Steps:**

- [ ] 实现 Token 刷新接口
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    
    @PostMapping("/refresh")
    public Result<RefreshResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        // 生成新的 Access Token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        
        return Result.success(new RefreshResponse(
            newAccessToken, jwtTokenProvider.getAccessTokenExpiry()));
    }
    
    @PostMapping("/refresh/silent")
    public Result<RefreshResponse> silentRefresh(
            HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        
        // 检查是否需要刷新（过期前 30 分钟）
        if (accessToken != null && jwtTokenProvider.needsRefresh(accessToken)) {
            // 从 Cookie 或 Header 获取 Refresh Token
            String refreshToken = getRefreshTokenFromCookie(request);
            
            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
                
                // 设置响应头，前端拦截器自动更新
                response.setHeader("X-New-Access-Token", newAccessToken);
            }
        }
        
        return Result.success(null);
    }
}
```

**Verification:** `mvn test -Dtest=TokenControllerTest`

---

## Task 4: 全局登出

**Files:**
- Modify: `src/main/java/com/example/login/service/impl/AuthServiceImpl.java`

**Steps:**

- [ ] 实现全局登出（所有设备）
```java
@Override
public void logout(String accessToken) {
    // 将当前 Access Token 加入黑名单
    jwtTokenProvider.invalidateToken(accessToken);
}

@Override
public void logoutAll(String username) {
    // 将用户的所有 Token 加入黑名单
    String pattern = "session:" + username + ":*";
    Set<String> keys = redisTemplate.keys(pattern);
    if (keys != null && !keys.isEmpty()) {
        for (String key : keys) {
            String tokenId = key.substring(key.lastIndexOf(":") + 1);
            redisTemplate.opsForValue().set(
                TOKEN_BLACKLIST_KEY + tokenId, "1", 
                7, TimeUnit.DAYS);
        }
        redisTemplate.delete(keys);
    }
}
```

- [ ] 添加登出接口
```java
@PostMapping("/logout")
public Result<Void> logout(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    authService.logout(token);
    return Result.success();
}

@PostMapping("/logout/all")
public Result<Void> logoutAll(HttpServletRequest request) {
    String username = SecurityContextHolder.getContext()
        .getAuthentication().getName();
    authService.logoutAll(username);
    return Result.success();
}
```

**Verification:** `mvn test -Dtest=AuthControllerTest`

---

## Task 5: 多设备登录管理

**Files:**
- Create: `src/main/java/com/example/login/entity/UserSession.java`
- Create: `src/main/java/com/example/login/service/SessionService.java`

**Steps:**

- [ ] 记录会话信息
```java
@Data
public class UserSession {
    private String sessionId;
    private Long userId;
    private String deviceInfo;
    private String ip;
    private String location;
    private Date loginTime;
    private Date lastActiveTime;
}

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final StringRedisTemplate redisTemplate;
    private static final int MAX_SESSIONS = 5;
    
    public void createSession(User user, String sessionId, String deviceInfo) {
        String key = "session:" + user.getId() + ":" + sessionId;
        UserSession session = new UserSession();
        session.setSessionId(sessionId);
        session.setUserId(user.getId());
        session.setDeviceInfo(deviceInfo);
        session.setIp(IpUtils.getClientIp());
        session.setLoginTime(new Date());
        
        redisTemplate.opsForValue().set(key, 
            JSON.toJSONString(session), 30, TimeUnit.DAYS);
        
        // 检查并发登录数
        checkConcurrentSessions(user.getId());
    }
    
    private void checkConcurrentSessions(Long userId) {
        String pattern = "session:" + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && keys.size() > MAX_SESSIONS) {
            // 移除最旧的会话
            // ... 实现逻辑
        }
    }
}
```

**Verification:** `mvn test -Dtest=SessionServiceTest`

---

## 验证清单

- [ ] JWT Token 生成与验证正确
- [ ] 静默刷新机制正常
- [ ] Token 黑名单生效
- [ ] 多设备登录限制生效
- [ ] 单元测试覆盖率 ≥ 80%

---

**创建时间:** 2026-07-14