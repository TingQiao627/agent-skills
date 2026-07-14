package com.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * 
 * Configures:
 * - CORS (Cross-Origin Resource Sharing) settings
 * - Interceptors for request processing
 * - Resource handlers
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Configure CORS settings
     * 
     * Allows cross-origin requests for the authentication API
     * 
     * @param registry CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            // Allowed origins - configure as needed for production
            .allowedOriginPatterns("*")
            // Allowed HTTP methods
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            // Allowed headers
            .allowedHeaders("*")
            // Allow credentials (cookies, authorization headers)
            .allowCredentials(true)
            // Exposed headers that clients can access
            .exposedHeaders(
                "Authorization",
                "X-Refresh-Token",
                "X-Request-Id",
                "X-Rate-Limit-Remaining"
            )
            // Cache preflight response for 1 hour
            .maxAge(3600);
    }

    /**
     * Configure interceptors
     * 
     * Add custom interceptors for:
     * - Request logging
     * - Rate limiting
     * - Authentication
     * - Request tracing
     * 
     * @param registry Interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO: Add request logging interceptor
        // registry.addInterceptor(new RequestLoggingInterceptor())
        //     .addPathPatterns("/**")
        //     .excludePathPatterns("/health", "/actuator/**");
        
        // TODO: Add rate limiting interceptor
        // registry.addInterceptor(new RateLimitInterceptor())
        //     .addPathPatterns("/auth/**")
        //     .order(1);
        
        // TODO: Add JWT authentication interceptor
        // registry.addInterceptor(new JwtAuthenticationInterceptor())
        //     .addPathPatterns("/**")
        //     .excludePathPatterns(
        //         "/auth/login",
        //         "/auth/register",
        //         "/auth/refresh",
        //         "/health",
        //         "/actuator/**"
        //     )
        //     .order(2);
    }
}