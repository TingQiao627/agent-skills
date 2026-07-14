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
 * 支付宝OAuth登录实现
 * 基于支付宝开放平台OAuth2.0协议
 */
@Slf4j
@Service("alipayOAuthService")
@RequiredArgsConstructor
public class AlipayOAuthServiceImpl implements OAuthService {

    private final OAuthBindingRepository oauthBindingRepository;
    private final RestTemplate restTemplate;

    @Value("${oauth.alipay.appid:}")
    private String appId;

    @Value("${oauth.alipay.private-key:}")
    private String privateKey;

    @Value("${oauth.alipay.public-key:}")
    private String alipayPublicKey;

    @Value("${oauth.alipay.authorize-url:https://openauth.alipay.com/oauth2/publicAppAuthorize.htm}")
    private String authorizeUrl;

    @Value("${oauth.alipay.access-token-url:https://openapi.alipay.com/gateway.do}")
    private String gatewayUrl;

    private static final String PROVIDER = "alipay";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    public String generateAuthorizeUrl(String provider, String redirectUri, String state) {
        // 支付宝OAuth2.0授权URL
        // 文档: https://opendocs.alipay.com/apis/api_9/alipay.user.info.share
        StringBuilder url = new StringBuilder(authorizeUrl);
        url.append("?app_id=").append(appId);
        url.append("&scope=auth_user");
        url.append("&redirect_uri=").append(redirectUri);
        url.append("&state=").append(state);
        
        log.info("生成支付宝授权URL: {}", url);
        return url.toString();
    }

    @Override
    @Transactional
    public Long handleCallback(String provider, String code, String state, String storedState) {
        // 1. 验证state
        if (!validateState(state, storedState)) {
            throw new RuntimeException("OAuth state验证失败，可能存在CSRF攻击");
        }

        // 2. 通过auth_code获取access_token和用户ID
        Map<String, Object> tokenResult = getAccessToken(code);
        if (tokenResult == null || tokenResult.get("access_token") == null) {
            throw new RuntimeException("获取支付宝access_token失败");
        }

        String accessToken = (String) tokenResult.get("access_token");
        String userId = (String) tokenResult.get("user_id"); // 支付宝用户唯一标识

        // 3. 获取用户信息
        Map<String, Object> userInfo = getUserInfo(accessToken);
        String providerUserId = userId;
        String nickname = (String) userInfo.getOrDefault("nick_name", "支付宝用户");
        String avatar = (String) userInfo.get("avatar");

        // 4. 查找是否已绑定
        Optional<OAuthBinding> bindingOpt = oauthBindingRepository
                .findByProviderAndProviderUserId(PROVIDER, providerUserId);

        if (bindingOpt.isPresent()) {
            // 已绑定，更新登录时间和token
            OAuthBinding binding = bindingOpt.get();
            oauthBindingRepository.updateLastLoginTime(binding.getUserId(), PROVIDER);
            log.info("支付宝用户登录成功: userId={}, systemUserId={}", userId, binding.getUserId());
            return binding.getUserId();
        }

        // 5. 首次登录，自动注册新用户并绑定
        Long newUserId = createNewUser(nickname, avatar, providerUserId);
        
        OAuthBinding binding = OAuthBinding.builder()
                .userId(newUserId)
                .provider(PROVIDER)
                .providerUserId(providerUserId)
                .accessToken(accessToken)
                .refreshToken(null) // 支付宝不提供refresh_token
                .bindTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .status(1)
                .build();
        
        oauthBindingRepository.save(binding);
        log.info("支付宝用户首次登录，自动注册并绑定: userId={}, systemUserId={}", userId, newUserId);
        
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
            throw new RuntimeException("该用户已绑定支付宝账号");
        }

        // 获取access_token
        Map<String, Object> tokenResult = getAccessToken(code);
        String accessToken = (String) tokenResult.get("access_token");
        String alipayUserId = (String) tokenResult.get("user_id");

        // 检查该支付宝账号是否已被其他用户绑定
        Optional<OAuthBinding> existingBinding = oauthBindingRepository
                .findByProviderAndProviderUserId(PROVIDER, alipayUserId);
        if (existingBinding.isPresent()) {
            throw new RuntimeException("该支付宝账号已被其他用户绑定");
        }

        // 创建绑定
        OAuthBinding binding = OAuthBinding.builder()
                .userId(userId)
                .provider(PROVIDER)
                .providerUserId(alipayUserId)
                .accessToken(accessToken)
                .refreshToken(null)
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
        log.info("解绑支付宝账号: userId={}, affected={}", userId, affected);
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
        return state.equals(storedState);
    }

    @Override
    public String generateState() {
        // 生成随机state参数
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 通过auth_code获取access_token
     * 支付宝接口文档: https://opendocs.alipay.com/apis/api_9/alipay.system.oauth.token
     */
    private Map<String, Object> getAccessToken(String code) {
        Map<String, String> params = new TreeMap<>();
        params.put("app_id", appId);
        params.put("method", "alipay.system.oauth.token");
        params.put("charset", CHARSET);
        params.put("sign_type", SIGN_TYPE);
        params.put("timestamp", java.time.LocalDateTime.now().toString());
        params.put("version", "1.0");
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        
        try {
            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);
            
            // 调用支付宝网关
            StringBuilder url = new StringBuilder(gatewayUrl);
            url.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url.toString(), Map.class);
            Map<String, Object> body = response.getBody();
            
            if (body != null && body.containsKey("alipay_system_oauth_token_response")) {
                return (Map<String, Object>) body.get("alipay_system_oauth_token_response");
            }
            
            log.error("获取支付宝access_token失败: {}", body);
            return null;
        } catch (Exception e) {
            log.error("调用支付宝OAuth接口异常", e);
            return null;
        }
    }

    /**
     * 获取支付宝用户信息
     * 文档: https://opendocs.alipay.com/apis/api_2/alipay.user.info.share
     */
    private Map<String, Object> getUserInfo(String accessToken) {
        Map<String, String> params = new TreeMap<>();
        params.put("app_id", appId);
        params.put("method", "alipay.user.info.share");
        params.put("charset", CHARSET);
        params.put("sign_type", SIGN_TYPE);
        params.put("timestamp", java.time.LocalDateTime.now().toString());
        params.put("version", "1.0");
        params.put("auth_token", accessToken);
        
        try {
            String sign = generateSign(params);
            params.put("sign", sign);
            
            StringBuilder url = new StringBuilder(gatewayUrl);
            url.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url.toString(), Map.class);
            Map<String, Object> body = response.getBody();
            
            if (body != null && body.containsKey("alipay_user_info_share_response")) {
                return (Map<String, Object>) body.get("alipay_user_info_share_response");
            }
            
            return new HashMap<>();
        } catch (Exception e) {
            log.error("获取支付宝用户信息异常", e);
            return new HashMap<>();
        }
    }

    /**
     * 生成支付宝签名
     * 实际应该使用支付宝SDK或专门的签名工具
     */
    private String generateSign(Map<String, String> params) {
        try {
            // 拼接签名字符串
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            String signContent = sb.substring(0, sb.length() - 1);
            
            // TODO: 使用RSA2私钥签名
            // 实际实现应使用支付宝SDK: AlipaySignature.rsaSignV2(signContent, privateKey, CHARSET)
            // 这里简化处理，返回占位符
            log.warn("需要实现支付宝RSA2签名逻辑");
            return "SIGNATURE_PLACEHOLDER";
            
        } catch (Exception e) {
            log.error("生成支付宝签名异常", e);
            return "";
        }
    }

    /**
     * 创建新用户（简化实现）
     */
    private Long createNewUser(String nickname, String avatar, String providerUserId) {
        // TODO: 调用用户服务创建用户
        log.warn("需要实现用户创建逻辑，当前使用临时ID");
        return System.currentTimeMillis();
    }
}