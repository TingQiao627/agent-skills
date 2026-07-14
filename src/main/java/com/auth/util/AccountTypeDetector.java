package com.auth.util;

import java.util.regex.Pattern;

/**
 * 账号类型检测工具
 */
public class AccountTypeDetector {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    /**
     * 检测账号类型
     * @param account 账号
     * @return phone/email/username
     */
    public static String detect(String account) {
        if (account == null || account.trim().isEmpty()) {
            return "username";
        }
        
        if (PHONE_PATTERN.matcher(account).matches()) {
            return "phone";
        }
        
        if (EMAIL_PATTERN.matcher(account).matches()) {
            return "email";
        }
        
        return "username";
    }
}