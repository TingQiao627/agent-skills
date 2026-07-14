package com.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器
 * 拦截请求，验证JWT Token，实现静默刷新机制
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SessionManager sessionManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService,
                                    JwtProperties jwtProperties,
                                    SessionManager sessionManager,
                                    TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.sessionManager = sessionManager;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 从请求头获取Token
            String token = extractToken(request);

            if (token == null) {
                // 没有Token，继续过滤链（交给后续处理）
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 验证Token格式
            if (!jwtService.validateToken(token)) {
                logger.debug("Invalid JWT token");
                sendUnauthorizedResponse(response, "Invalid token");
                return;
            }

            // 3. 检查Token是否在黑名单中
            if (tokenBlacklistService.isBlacklisted(token)) {
                logger.debug("Token is blacklisted");
                sendUnauthorizedResponse(response, "Token revoked");
                return;
            }

            // 4. 提取用户ID
            String userId = jwtService.extractUserId(token);
            if (userId == null) {
                sendUnauthorizedResponse(response, "Invalid token payload");
                return;
            }

            // 5. 检查是否需要静默刷新
            String refreshToken = extractRefreshToken(request);
            if (jwtService.isTokenExpiringSoon(token) && refreshToken != null) {
                Map<String, String> newTokens = sessionManager.silentRefresh(token, refreshToken);
                if (newTokens != null) {
                    // 在响应头中返回新Token
                    response.setHeader("X-New-Access-Token", newTokens.get("accessToken"));
                    response.setHeader("X-New-Refresh-Token", newTokens.get("refreshToken"));
                    logger.debug("Token silently refreshed for user: {}", userId);
                }
            }

            // 6. 设置Spring Security上下文
            setAuthentication(userId);

            // 7. 继续过滤链
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage(), e);
            sendUnauthorizedResponse(response, "Authentication failed");
        }
    }

    /**
     * 从请求头提取Access Token
     *
     * @param request HTTP请求
     * @return Token字符串，如果不存在返回null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            return bearerToken.substring(jwtProperties.getTokenPrefix().length());
        }
        return null;
    }

    /**
     * 从请求头或Cookie提取Refresh Token
     *
     * @param request HTTP请求
     * @return Refresh Token字符串，如果不存在返回null
     */
    private String extractRefreshToken(HttpServletRequest request) {
        // 优先从请求头获取
        String refreshToken = request.getHeader("X-Refresh-Token");
        if (StringUtils.hasText(refreshToken)) {
            return refreshToken;
        }

        // 如果没有，可以从Cookie中获取（需要时实现）
        // Cookie[] cookies = request.getCookies();
        // if (cookies != null) {
        //     for (Cookie cookie : cookies) {
        //         if ("refresh_token".equals(cookie.getName())) {
        //             return cookie.getValue();
        //         }
        //     }
        // }

        return null;
    }

    /**
     * 设置Spring Security认证上下文
     *
     * @param userId 用户ID
     */
    private void setAuthentication(String userId) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("Authentication set for user: {}", userId);
    }

    /**
     * 发送401未授权响应
     *
     * @param response HTTP响应
     * @param message 错误消息
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        response.getWriter().write(toJson(errorResponse));
    }

    /**
     * 简单的JSON序列化（避免引入额外依赖）
     *
     * @param map Map对象
     * @return JSON字符串
     */
    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 判断是否需要过滤该请求路径
     * 可以配置白名单路径
     *
     * @param request HTTP请求
     * @return 是否需要过滤
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 登录、注册、公开接口等不需要Token验证
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/public") ||
               path.startsWith("/actuator/health");
    }
}