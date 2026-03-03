package com.example.xinli.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private AdminInfo admin;
    
    @Data
    public static class AdminInfo {
        private Long id;
        private String username;
        private String name;
        private String role;
        private String[] roles;
        private java.util.List<String> permissions;
    }
    
    public LoginResponse(String token, Long expiresIn, AdminInfo admin) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.admin = admin;
    }
}
