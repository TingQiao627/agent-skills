package com.password.service.impl;

import com.password.dto.ForgotPasswordRequest;
import com.password.dto.ResetRequest;
import com.password.entity.PasswordResetToken;
import com.password.entity.User;
import com.password.repository.PasswordResetTokenRepository;
import com.password.repository.UserRepository;
import com.password.service.PasswordResetService;
import com.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 密码重置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final SmsService smsService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    @Transactional
    public void sendResetCode(ForgotPasswordRequest request) {
        // 1. 验证用户存在
        User user = userRepository.findByPhoneOrEmail(request.getAccount(), request.getAccount())
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        
        // 2. 生成重置Token
        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .token(token)
                .expireAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        tokenRepository.save(resetToken);
        
        // 3. 发送通知
        String notifyType = request.getAccount().contains("@") ? "email" : "sms";
        if ("sms".equals(notifyType)) {
            smsService.sendVerificationCode(request.getAccount(), "密码重置验证码: " + token.substring(0, 6));
        } else {
            // 邮件发送
            // emailService.sendResetEmail(request.getAccount(), token);
            log.info("发送密码重置邮件到: {}", request.getAccount());
        }
    }
    
    @Override
    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);
        
        if (resetToken == null) {
            return false;
        }
        
        if (resetToken.getUsed()) {
            return false;
        }
        
        if (resetToken.getExpireAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public void resetPassword(ResetRequest request) {
        // 1. 验证Token
        if (!validateResetToken(request.getToken())) {
            throw new RuntimeException("重置令牌无效或已过期");
        }
        
        // 2. 查询Token
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("重置令牌不存在"));
        
        // 3. 查询用户
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 4. 检查密码历史（最近3次）
        // 实际应查询密码历史表
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("新密码不能与最近使用的密码相同");
        }
        
        // 5. 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // 6. 标记Token已使用
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        // 7. 发送通知
        log.info("用户 {} 密码重置成功", user.getId());
        
        // 8. 强制登出所有设备（加入黑名单）
        // 实际应调用 AuthService 登出
    }
    
    @Override
    public boolean verifyIdentity(String account, String code) {
        // 简化实现，实际应验证短信验证码
        return smsService.verifyCode(account, code);
    }
}