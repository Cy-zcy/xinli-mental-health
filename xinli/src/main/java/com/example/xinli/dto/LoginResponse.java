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
        private String role = "admin";
        private String[] roles = {"R_ADMIN"}; // 添加角色数组，前端需要这个格式
    }
    
    public LoginResponse(String token, Long expiresIn, AdminInfo admin) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.admin = admin;
    }
}
