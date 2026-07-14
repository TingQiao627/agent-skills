package com.oauth.service;

import com.oauth.entity.OAuthBinding;

import java.util.List;
import java.util.Optional;

/**
 * OAuth服务接口
 * 定义第三方OAuth登录的核心功能
 */
public interface OAuthService {

    /**
     * 生成OAuth授权URL
     *
     * @param provider    提供商 (wechat, alipay等)
     * @param redirectUri 回调地址
     * @param state       状态参数(防CSRF)
     * @return 授权URL
     */
    String generateAuthorizeUrl(String provider, String redirectUri, String state);

    /**
     * 通过授权码获取第三方用户信息并完成登录/注册
     * 首次登录自动创建绑定关系
     *
     * @param provider    提供商
     * @param code        授权码
     * @param state       状态参数
     * @param storedState 存储的状态参数(用于验证)
     * @return 用户ID
     */
    Long handleCallback(String provider, String code, String state, String storedState);

    /**
     * 绑定第三方账号到已登录用户
     *
     * @param userId      当前登录用户ID
     * @param provider    提供商
     * @param code        授权码
     * @param state       状态参数
     * @param storedState 存储的状态参数
     * @return 绑定记录
     */
    OAuthBinding bindAccount(Long userId, String provider, String code, String state, String storedState);

    /**
     * 解绑第三方账号
     *
     * @param userId   用户ID
     * @param provider 提供商
     * @return 是否成功
     */
    boolean unbindAccount(Long userId, String provider);

    /**
     * 获取用户的所有绑定账号
     *
     * @param userId 用户ID
     * @return 绑定列表
     */
    List<OAuthBinding> getUserBindings(Long userId);

    /**
     * 检查用户是否已绑定指定提供商
     *
     * @param userId   用户ID
     * @param provider 提供商
     * @return 是否已绑定
     */
    boolean isBound(Long userId, String provider);

    /**
     * 根据第三方用户信息查找绑定的用户ID
     *
     * @param provider       提供商
     * @param providerUserId 第三方用户ID
     * @return 用户ID
     */
    Optional<Long> findUserIdByProviderUser(String provider, String providerUserId);

    /**
     * 获取提供商名称
     *
     * @return 提供商标识
     */
    String getProvider();

    /**
     * 验证state参数
     *
     * @param state       请求中的state
     * @param storedState 存储的state
     * @return 是否有效
     */
    boolean validateState(String state, String storedState);

    /**
     * 生成state参数
     *
     * @return state字符串
     */
    String generateState();
}