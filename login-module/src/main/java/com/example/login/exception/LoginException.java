package com.example.login.exception;

/**
 * 登录异常基类
 */
public class LoginException extends RuntimeException {
    
    public LoginException(String message) {
        super(message);
    }
}

/**
 * F4: 账号锁定异常
 */
public class LoginLockedException extends LoginException {
    public LoginLockedException(String message) {
        super(message);
    }
}

/**
 * 登录失败异常
 */
public class LoginFailedException extends LoginException {
    public LoginFailedException(String message) {
        super(message);
    }
}

/**
 * 验证码异常
 */
public class CaptchaException extends LoginException {
    public CaptchaException(String message) {
        super(message);
    }
}

/**
 * Token异常
 */
public class TokenException extends LoginException {
    public TokenException(String message) {
        super(message);
    }
}