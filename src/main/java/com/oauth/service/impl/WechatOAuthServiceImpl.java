package com.oauth.service.impl;

import com.oauth.entity.OAuthBinding;
import com.oauth.repository.OAuthBindingRepository;
import com.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * 微信OAuth登录实现
 * 基于微信开放平台OAuth2.0协议
 */
@Slf4j
@Service("wechatOAuthService")
@RequiredArgsConstructor
public class WechatOAuthServiceImpl implements OAuthService {

    private final OAuthBindingRepository oauthBindingRepository;
    private final RestTemplate restTemplate;

    @Value("${oauth.wechat.appid:}")
    private String appId;

    @Value("${oauth.wechat.secret:}")
    private String appSecret;

    @Value("${oauth.wechat.authorize-url:https://open.weixin.qq.com/connect/qrconnect}")
    private String authorizeUrl;

    @Value("${oauth.wechat.access-token-url:https://api.weixin.qq.com/sns/oauth2/access_token}")
    private String accessTokenUrl;

    @Value("${oauth.wechat.userinfo-url:https://api.weixin.qq.com/sns/userinfo}")
    private String userinfoUrl;

    private static final String PROVIDER = "wechat";

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    public String generateAuthorizeUrl(String provider, String redirectUri, String state) {
        // 微信OAuth2.0授权URL
        // 文档: https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html
        StringBuilder url = new StringBuilder(authorizeUrl);
        url.append("?appid=").append(appId);
        url.append("&redirect_uri=").append(redirectUri);
        url.append("&response_type=code");
        url.append("&scope=snsapi_login"); // 网页应用使用snsapi_login
        url.append("&state=").append(state);
        url.append("#wechat_redirect");
        
        log.info("生成微信授权URL: {}", url);
        return url.toString();
    }

    @Override
    @Transactional
    public Long handleCallback(String provider, String code, String state, String storedState) {
        // 1. 验证state
        if (!validateState(state, storedState)) {
            throw new RuntimeException("OAuth state验证失败，可能存在CSRF攻击");
        }

        // 2. 通过code获取access_token
        Map<String, Object> tokenResult = getAccessToken(code);
        if (tokenResult == null || tokenResult.get("access_token") == null) {
            throw new RuntimeException("获取微信access_token失败");
        }

        String accessToken = (String) tokenResult.get("access_token");
        String refreshToken = (String) tokenResult.get("refresh_token");
        String openid = (String) tokenResult.get("openid");

        // 3. 获取用户信息
        Map<String, Object> userInfo = getUserInfo(accessToken, openid);
        String providerUserId = openid;
        String nickname = (String) userInfo.get("nickname");
        String headImgUrl = (String) userInfo.get("headimgurl");

        // 4. 查找是否已绑定
        Optional<OAuthBinding> bindingOpt = oauthBindingRepository
                .findByProviderAndProviderUserId(PROVIDER, providerUserId);

        if (bindingOpt.isPresent()) {
            // 已绑定，更新登录时间和token
            OAuthBinding binding = bindingOpt.get();
            oauthBindingRepository.updateLastLoginTime(binding.getUserId(), PROVIDER);
            oauthBindingRepository.updateToken(binding.getUserId(), PROVIDER, accessToken, refreshToken);
            log.info("微信用户登录成功: openid={}, userId={}", openid, binding.getUserId());
            return binding.getUserId();
        }

        // 5. 首次登录，自动注册新用户并绑定
        // 注意：这里简化了用户注册逻辑，实际应该调用用户服务创建用户
        Long newUserId = createNewUser(nickname, headImgUrl, providerUserId);
        
        OAuthBinding binding = OAuthBinding.builder()
                .userId(newUserId)
                .provider(PROVIDER)
                .providerUserId(providerUserId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .bindTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .status(1)
                .build();
        
        oauthBindingRepository.save(binding);
        log.info("微信用户首次登录，自动注册并绑定: openid={}, userId={}", openid, newUserId);
        
        return newUserId;
    }

    @Override
    @Transactional
    public OAuthBinding bindAccount(Long userId, String provider, String code, String state, String storedState) {
        // 验证state
        if (!validateState(state, storedState)) {
            throw new RuntimeException("OAuth state验证失败");
        }

        // 检查是否已绑定
        if (oauthBindingRepository.existsByUserIdAndProviderAndStatus(userId, PROVIDER)) {
            throw new RuntimeException("该用户已绑定微信账号");
        }

        // 获取access_token
        Map<String, Object> tokenResult = getAccessToken(code);
        String accessToken = (String) tokenResult.get("access_token");
        String refreshToken = (String) tokenResult.get("refresh_token");
        String openid = (String) tokenResult.get("openid");

        // 检查该微信账号是否已被其他用户绑定
        Optional<OAuthBinding> existingBinding = oauthBindingRepository
                .findByProviderAndProviderUserId(PROVIDER, openid);
        if (existingBinding.isPresent()) {
            throw new RuntimeException("该微信账号已被其他用户绑定");
        }

        // 创建绑定
        OAuthBinding binding = OAuthBinding.builder()
                .userId(userId)
                .provider(PROVIDER)
                .providerUserId(openid)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .bindTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .status(1)
                .build();

        return oauthBindingRepository.save(binding);
    }

    @Override
    @Transactional
    public boolean unbindAccount(Long userId, String provider) {
        int affected = oauthBindingRepository.unbindByUserIdAndProvider(userId, PROVIDER);
        log.info("解绑微信账号: userId={}, affected={}", userId, affected);
        return affected > 0;
    }

    @Override
    public List<OAuthBinding> getUserBindings(Long userId) {
        return oauthBindingRepository.findActiveBindingsByUserId(userId);
    }

    @Override
    public boolean isBound(Long userId, String provider) {
        return oauthBindingRepository.existsByUserIdAndProviderAndStatus(userId, PROVIDER);
    }

    @Override
    public Optional<Long> findUserIdByProviderUser(String provider, String providerUserId) {
        return oauthBindingRepository.findByProviderAndProviderUserId(PROVIDER, providerUserId)
                .map(OAuthBinding::getUserId);
    }

    @Override
    public boolean validateState(String state, String storedState) {
        if (state == null || storedState == null) {
            return false;
        }
        // 简单比较，生产环境应该使用Redis存储并设置过期时间
        return state.equals(storedState);
    }

    @Override
    public String generateState() {
        // 生成随机state参数，用于防CSRF攻击
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 通过code获取access_token
     */
    private Map<String, Object> getAccessToken(String code) {
        String url = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                accessTokenUrl, appId, appSecret, code);
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            
            if (body != null && body.containsKey("errcode")) {
                log.error("获取微信access_token失败: {}", body);
                return null;
            }
            
            return body;
        } catch (Exception e) {
            log.error("调用微信access_token接口异常", e);
            return null;
        }
    }

    /**
     * 获取微信用户信息
     */
    private Map<String, Object> getUserInfo(String accessToken, String openid) {
        String url = String.format("%s?access_token=%s&openid=%s", userinfoUrl, accessToken, openid);
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("获取微信用户信息异常", e);
            return new HashMap<>();
        }
    }

    /**
     * 创建新用户（简化实现）
     * 实际应该注入UserService并调用用户注册逻辑
     */
    private Long createNewUser(String nickname, String headImgUrl, String openid) {
        // TODO: 调用用户服务创建用户
        // 这里返回一个临时ID，实际应该调用：
        // UserCreateRequest request = new UserCreateRequest();
        // request.setNickname(nickname);
        // request.setAvatar(headImgUrl);
        // User user = userService.createOAuthUser(request, PROVIDER, openid);
        // return user.getId();
        
        log.warn("需要实现用户创建逻辑，当前使用临时ID");
        return System.currentTimeMillis(); // 临时返回时间戳作为用户ID
    }
}