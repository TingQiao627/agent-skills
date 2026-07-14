package com.sms.service;

/**
 * 短信服务接口
 */
public interface SmsService {
    
    /**
     * 发送验证码
     * 
     * @param phone 手机号
     * @param ip 客户端IP
     * @return 发送结果
     */
    String sendCode(String phone, String ip);
    
    /**
     * 验证验证码
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果，成功返回用户ID，失败返回null
     */
    Long verifyCode(String phone, String code);
}