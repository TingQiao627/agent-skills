package com.oauth.service.impl;

import com.oauth.dto.OAuthUserInfo;
import com.oauth.entity.OAuthBinding;
import com.oauth.repository.OAuthBindingRepository;
import com.oauth.service.OAuthService;
import com.auth.entity.User;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * OAuth 服务实现 - 支持微信/支付宝/企业微信
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {
    
    private final OAuthBindingRepository oauthBindingRepository;
    private final UserRepository userRepository;
    
    @Override
    public String getAuthorizationUrl(String platform, String redirectUri, String state) {
        return switch (platform.toLowerCase()) {
            case "wechat" -> buildWechatAuthUrl(redirectUri, state);
            case "alipay" -> buildAlipayAuthUrl(redirectUri, state);
            case "wework" -> buildWeworkAuthUrl(redirectUri, state);
            default -> throw new IllegalArgumentException("不支持的OAuth平台: " + platform);
        };
    }
    
    @Override
    @Transactional
    public OAuthUserInfo login(String platform, String code, String state) {
        // 1. 获取OAuth用户信息
        OAuthUserInfo oauthInfo = fetchOAuthUserInfo(platform, code);
        
        // 2. 查询是否已绑定
        Long userId = getUserIdByOAuth(platform, oauthInfo.getOauthId());
        
        boolean isNewUser = false;
        if (userId == null) {
            // 3. 自动注册新用户
            User newUser = createOAuthUser(platform, oauthInfo);
            userId = newUser.getId();
            isNewUser = true;
            
            // 4. 创建绑定关系
            createBinding(userId, platform, oauthInfo);
        }
        
        oauthInfo.setUserId(userId);
        oauthInfo.setIsNewUser(isNewUser);
        
        return oauthInfo;
    }
    
    @Override
    @Transactional
    public void bindAccount(Long userId, String platform, String code) {
        // 1. 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 2. 检查是否已绑定该平台
        if (isBound(platform, userId.toString())) {
            throw new RuntimeException("该平台账号已绑定");
        }
        
        // 3. 获取OAuth信息
        OAuthUserInfo oauthInfo = fetchOAuthUserInfo(platform, code);
        
        // 4. 检查该OAuth是否被其他账号绑定
        if (isBound(platform, oauthInfo.getOauthId())) {
            throw new RuntimeException("该第三方账号已被其他账号绑定");
        }
        
        // 5. 创建绑定
        createBinding(userId, platform, oauthInfo);
    }
    
    @Override
    @Transactional
    public void unbindAccount(Long userId, String platform) {
        oauthBindingRepository.deleteByUserIdAndOauthType(userId, platform);
    }
    
    @Override
    public boolean isBound(String platform, String oauthId) {
        return oauthBindingRepository.findByOauthTypeAndOauthId(platform, oauthId).isPresent();
    }
    
    @Override
    public Long getUserIdByOAuth(String platform, String oauthId) {
        return oauthBindingRepository.findByOauthTypeAndOauthId(platform, oauthId)
                .map(OAuthBinding::getUserId)
                .orElse(null);
    }
    
    // ========== 私有方法 ==========
    
    private String buildWechatAuthUrl(String redirectUri, String state) {
        String appId = "YOUR_WECHAT_APPID";
        return String.format(
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s",
            appId, redirectUri, state
        );
    }
    
    private String buildAlipayAuthUrl(String redirectUri, String state) {
        String appId = "YOUR_ALIPAY_APPID";
        return String.format(
            "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=%s&redirect_uri=%s&state=%s",
            appId, redirectUri, state
        );
    }
    
    private String buildWeworkAuthUrl(String redirectUri, String state) {
        String corpId = "YOUR_WEWORK_CORPID";
        return String.format(
            "https://open.work.weixin.qq.com/wwopen/sso/qrConnect?appid=%s&agentid=AGENT_ID&redirect_uri=%s&state=%s",
            corpId, redirectUri, state
        );
    }
    
    private OAuthUserInfo fetchOAuthUserInfo(String platform, String code) {
        // 实际应调用第三方API
        // 这里返回模拟数据
        return OAuthUserInfo.builder()
                .platform(platform)
                .oauthId(UUID.randomUUID().toString())
                .oauthName("用户" + platform)
                .oauthAvatar("https://via.placeholder.com/100")
                .build();
    }
    
    private User createOAuthUser(String platform, OAuthUserInfo oauthInfo) {
        User user = User.builder()
                .username(platform + "_" + System.currentTimeMillis())
                .nickname(oauthInfo.getOauthName())
                .avatar(oauthInfo.getOauthAvatar())
                .status(1)
                .build();
        return userRepository.save(user);
    }
    
    private void createBinding(Long userId, String platform, OAuthUserInfo oauthInfo) {
        OAuthBinding binding = OAuthBinding.builder()
                .userId(userId)
                .oauthType(platform)
                .oauthId(oauthInfo.getOauthId())
                .oauthName(oauthInfo.getOauthName())
                .oauthAvatar(oauthInfo.getOauthAvatar())
                .build();
        oauthBindingRepository.save(binding);
    }
}