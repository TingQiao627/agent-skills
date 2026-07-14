package com.auth.dto;

public class LoginResponse {
    
    private String access_token;
    
    private String refresh_token;
    
    private Long expires_in;
    
    private UserInfo user_info;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String access_token, String refresh_token, Long expires_in, UserInfo user_info) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.expires_in = expires_in;
        this.user_info = user_info;
    }
    
    // Getters and Setters
    public String getAccess_token() {
        return access_token;
    }
    
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
    
    public String getRefresh_token() {
        return refresh_token;
    }
    
    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
    
    public Long getExpires_in() {
        return expires_in;
    }
    
    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
    }
    
    public UserInfo getUser_info() {
        return user_info;
    }
    
    public void setUser_info(UserInfo user_info) {
        this.user_info = user_info;
    }
    
    // Inner class for user info
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String phone;
        
        public UserInfo() {}
        
        public UserInfo(Long id, String username, String email, String phone) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phone = phone;
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}