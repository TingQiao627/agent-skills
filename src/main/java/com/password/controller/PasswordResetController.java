package com.password.controller;

import com.password.dto.ForgotPasswordRequest;
import com.password.dto.ResetRequest;
import com.password.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 密码重置控制器
 */
@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * 忘记密码接口
     * 发送重置链接或验证码
     * 
     * @param request 忘记密码请求（包含邮箱或手机号）
     * @return 响应结果
     */
    @PostMapping("/forgot")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            passwordResetService.initiatePasswordReset(request);
            
            response.put("success", true);
            response.put("message", "如果该邮箱/手机号已注册，您将收到密码重置链接");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 重置密码接口
     * 
     * @param request 重置密码请求（包含令牌和新密码）
     * @return 响应结果
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证令牌
            if (request.getToken() == null || request.getToken().isEmpty()) {
                response.put("success", false);
                response.put("message", "重置令牌无效");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证新密码
            if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                response.put("success", false);
                response.put("message", "新密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 执行重置
            boolean success = passwordResetService.resetPassword(request);

            if (success) {
                response.put("success", true);
                response.put("message", "密码重置成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "令牌无效或已过期");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 验证令牌有效性接口
     * 
     * @param token 重置令牌
     * @return 响应结果
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean valid = passwordResetService.validateToken(token);

            response.put("success", true);
            response.put("valid", valid);
            response.put("message", valid ? "令牌有效" : "令牌无效或已过期");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }
}