package com.password.service;

import com.password.dto.ForgotPasswordRequest;
import com.password.dto.ResetRequest;
import com.password.entity.PasswordResetToken;
import com.password.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * 密码重置服务
 */
@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_VALIDITY_HOURS = 24;
    private static final int TOKEN_LENGTH = 32;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    /**
     * 发起密码重置请求
     */
    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        String emailOrPhone = request.getEmailOrPhone();
        
        // 查找用户
        Optional<User> userOpt = request.isEmail() 
            ? userService.findByEmail(emailOrPhone)
            : userService.findByPhone(emailOrPhone);

        if (userOpt.isEmpty()) {
            logger.warn("密码重置请求失败：用户不存在 - {}", emailOrPhone);
            // 不暴露用户是否存在的信息
            return;
        }

        User user = userOpt.get();
        
        // 生成重置令牌
        String token = generateSecureToken();
        LocalDateTime expireTime = LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS);

        // 使之前的令牌失效
        tokenRepository.markAllAsUsedByUserId(user.getId());

        // 创建新令牌
        PasswordResetToken resetToken = new PasswordResetToken(user.getId(), token, expireTime);
        tokenRepository.save(resetToken);

        // 发送重置链接或验证码
        if (request.isEmail()) {
            sendResetEmail(user.getEmail(), token);
        } else {
            sendResetSms(user.getPhone(), token);
        }

        logger.info("密码重置令牌已发送 - userId: {}, contact: {}", user.getId(), emailOrPhone);
    }

    /**
     * 重置密码
     */
    @Transactional
    public boolean resetPassword(ResetRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        // 查找令牌
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            logger.warn("密码重置失败：令牌不存在");
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // 验证令牌有效性
        if (!resetToken.isValid()) {
            logger.warn("密码重置失败：令牌已使用或已过期");
            return false;
        }

        // 查找用户
        Optional<User> userOpt = userService.findById(resetToken.getUserId());
        if (userOpt.isEmpty()) {
            logger.warn("密码重置失败：用户不存在");
            return false;
        }

        User user = userOpt.get();

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        // 标记令牌为已使用
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // 发送通知
        if (user.getEmail() != null) {
            sendPasswordChangedEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            sendPasswordChangedSms(user.getPhone());
        }

        logger.info("密码重置成功 - userId: {}", user.getId());
        return true;
    }

    /**
     * 验证令牌有效性
     */
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.isPresent() && tokenOpt.get().isValid();
    }

    /**
     * 清理过期令牌
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpireTimeBefore(LocalDateTime.now());
        logger.info("已清理过期的密码重置令牌");
    }

    /**
     * 生成安全令牌
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 发送重置邮件
     */
    private void sendResetEmail(String email, String token) {
        String resetUrl = "https://yourapp.com/reset-password?token=" + token;
        String subject = "密码重置请求";
        String content = String.format(
            "您好，\n\n" +
            "您收到此邮件是因为您（或其他人）请求重置密码。\n\n" +
            "请点击以下链接重置密码：\n%s\n\n" +
            "此链接将在24小时后过期。\n\n" +
            "如果您没有请求重置密码，请忽略此邮件。\n\n" +
            "谢谢！",
            resetUrl
        );

        emailService.sendEmail(email, subject, content);
        logger.info("密码重置邮件已发送 - email: {}", email);
    }

    /**
     * 发送重置短信
     */
    private void sendResetSms(String phone, String token) {
        String verificationCode = token.substring(0, 6); // 取前6位作为验证码
        String content = String.format("您的密码重置验证码是：%s，24小时内有效。如非本人操作请忽略。", verificationCode);

        smsService.sendSms(phone, content);
        logger.info("密码重置短信已发送 - phone: {}", maskPhone(phone));
    }

    /**
     * 发送密码修改成功通知邮件
     */
    private void sendPasswordChangedEmail(String email) {
        String subject = "密码修改成功";
        String content = "您好，\n\n您的密码已成功修改。如果这不是您的操作，请立即联系客服。\n\n谢谢！";

        emailService.sendEmail(email, subject, content);
        logger.info("密码修改通知邮件已发送 - email: {}", email);
    }

    /**
     * 发送密码修改成功通知短信
     */
    private void sendPasswordChangedSms(String phone) {
        String content = "您的密码已成功修改。如非本人操作，请立即联系客服。";

        smsService.sendSms(phone, content);
        logger.info("密码修改通知短信已发送 - phone: {}", maskPhone(phone));
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}