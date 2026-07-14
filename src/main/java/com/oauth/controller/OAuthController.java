package com.oauth.controller;

import com.oauth.entity.OAuthBinding;
import com.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OAuth第三方登录控制器
 * 提供微信、支付宝等第三方OAuth登录功能
 */
@Slf4j
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final StringRedisTemplate redisTemplate;
    
    @Qualifier("wechatOAuthService")
    private final OAuthService wechatOAuthService;
    
    @Qualifier("alipayOAuthService")
    private final OAuthService alipayOAuthService;

    // State缓存过期时间（秒）
    private static final long STATE_EXPIRE_SECONDS = 300;

    /**
     * 获取OAuth授权URL
     * GET /api/oauth/{provider}/authorize
     * 
     * @param provider    提供商 (wechat, alipay)
     * @param redirectUri 回调地址
     * @return 包含授权URL和state的响应
     */
    @GetMapping("/{provider}/authorize")
    public ResponseEntity<Map<String, Object>> authorize(
            @PathVariable String provider,
            @RequestParam(required = false) String redirectUri) {
        
        OAuthService oauthService = getOAuthService(provider);
        
        // 生成state参数防CSRF
        String state = oauthService.generateState();
        
        // 将state存入Redis，设置5分钟过期
        String stateKey = "oauth:state:" + provider + ":" + state;
        redisTemplate.opsForValue().set(stateKey, state, STATE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        // 生成授权URL
        String authorizeUrl = oauthService.generateAuthorizeUrl(provider, redirectUri, state);
        
        Map<String, Object> result = new HashMap<>();
        result.put("authorizeUrl", authorizeUrl);
        result.put("state", state);
        result.put("provider", provider);
        
        log.info("生成OAuth授权URL: provider={}, state={}", provider, state);
        return ResponseEntity.ok(result);
    }

    /**
     * OAuth回调接口
     * GET /api/oauth/{provider}/callback
     * 
     * @param provider 提供商
     * @param code     授权码
     * @param state    状态参数
     * @return 登录结果
     */
    @GetMapping("/{provider}/callback")
    public ResponseEntity<Map<String, Object>> callback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String state) {
        
        OAuthService oauthService = getOAuthService(provider);
        
        // 从Redis获取存储的state进行验证
        String stateKey = "oauth:state:" + provider + ":" + state;
        String storedState = redisTemplate.opsForValue().get(stateKey);
        
        // 验证后立即删除state，防止重复使用
        redisTemplate.delete(stateKey);
        
        try {
            // 处理OAuth回调，完成登录或自动注册
            Long userId = oauthService.handleCallback(provider, code, state, storedState);
            
            // TODO: 生成JWT Token返回给前端
            // 实际应该注入JwtService生成token
            String accessToken = "JWT_ACCESS_TOKEN_" + userId;
            String refreshToken = "JWT_REFRESH_TOKEN_" + userId;
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("provider", provider);
            
            log.info("OAuth登录成功: provider={}, userId={}", provider, userId);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            log.error("OAuth登录失败: provider={}, error={}", provider, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("provider", provider);
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 绑定第三方账号
     * POST /api/oauth/{provider}/bind
     * 
     * @param provider 提供商
     * @param userId   当前登录用户ID（实际应从JWT获取）
     * @param code     授权码
     * @param state    状态参数
     * @return 绑定结果
     */
    @PostMapping("/{provider}/bind")
    public ResponseEntity<Map<String, Object>> bindAccount(
            @PathVariable String provider,
            @RequestParam Long userId,
            @RequestParam String code,
            @RequestParam String state) {
        
        OAuthService oauthService = getOAuthService(provider);
        
        // 从Redis获取存储的state
        String stateKey = "oauth:state:" + provider + ":" + state;
        String storedState = redisTemplate.opsForValue().get(stateKey);
        redisTemplate.delete(stateKey);
        
        try {
            OAuthBinding binding = oauthService.bindAccount(userId, provider, code, state, storedState);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("bindingId", binding.getId());
            result.put("provider", binding.getProvider());
            result.put("bindTime", binding.getBindTime());
            
            log.info("绑定第三方账号成功: userId={}, provider={}", userId, provider);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            log.error("绑定第三方账号失败: userId={}, provider={}, error={}", 
                    userId, provider, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 解绑第三方账号
     * DELETE /api/oauth/{provider}/unbind
     * 
     * @param provider 提供商
     * @param userId   当前登录用户ID（实际应从JWT获取）
     * @return 解绑结果
     */
    @DeleteMapping("/{provider}/unbind")
    public ResponseEntity<Map<String, Object>> unbindAccount(
            @PathVariable String provider,
            @RequestParam Long userId) {
        
        OAuthService oauthService = getOAuthService(provider);
        
        try {
            boolean success = oauthService.unbindAccount(userId, provider);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("provider", provider);
            result.put("message", success ? "解绑成功" : "解绑失败，未找到绑定记录");
            
            log.info("解绑第三方账号: userId={}, provider={}, success={}", userId, provider, success);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("解绑第三方账号异常: userId={}, provider={}", userId, provider, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "解绑失败：" + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取用户已绑定的第三方账号列表
     * GET /api/oauth/bindings
     * 
     * @param userId 用户ID（实际应从JWT获取）
     * @return 绑定列表
     */
    @GetMapping("/bindings")
    public ResponseEntity<Map<String, Object>> getBindings(@RequestParam Long userId) {
        // 使用任意服务获取绑定列表
        List<OAuthBinding> bindings = wechatOAuthService.getUserBindings(userId);
        
        List<Map<String, Object>> bindingList = new ArrayList<>();
        for (OAuthBinding binding : bindings) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", binding.getId());
            item.put("provider", binding.getProvider());
            item.put("bindTime", binding.getBindTime());
            item.put("lastLoginTime", binding.getLastLoginTime());
            bindingList.add(item);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("bindings", bindingList);
        result.put("total", bindingList.size());
        
        return ResponseEntity.ok(result);
    }

    /**
     * 检查是否已绑定指定提供商
     * GET /api/oauth/{provider}/bound
     * 
     * @param provider 提供商
     * @param userId   用户ID
     * @return 绑定状态
     */
    @GetMapping("/{provider}/bound")
    public ResponseEntity<Map<String, Object>> checkBound(
            @PathVariable String provider,
            @RequestParam Long userId) {
        
        OAuthService oauthService = getOAuthService(provider);
        boolean isBound = oauthService.isBound(userId, provider);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("provider", provider);
        result.put("isBound", isBound);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取对应的OAuth服务实现
     */
    private OAuthService getOAuthService(String provider) {
        return switch (provider.toLowerCase()) {
            case "wechat" -> wechatOAuthService;
            case "alipay" -> alipayOAuthService;
            default -> throw new RuntimeException("不支持的OAuth提供商: " + provider);
        };
    }
}