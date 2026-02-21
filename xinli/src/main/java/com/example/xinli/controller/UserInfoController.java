package com.example.xinli.controller;

import com.example.xinli.dto.Result;
import com.example.xinli.entity.Admin;
import com.example.xinli.entity.User;
import com.example.xinli.service.AuthService;
import com.example.xinli.service.UserService;
import com.example.xinli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserInfoController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<Object> getCurrentUserInfo(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (token == null || token.isEmpty()) {
                return Result.error("未提供认证token");
            }
            
            // 解析token获取用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            
            if (username == null) {
                return Result.error("无效的token");
            }
            
            // 根据角色返回不同的用户信息
            if ("admin".equals(role)) {
                // 管理员信息
                Admin admin = authService.findByUsername(username);
                if (admin != null) {
                    // 创建返回的用户信息对象
                    UserInfoDTO userInfo = new UserInfoDTO();
                    userInfo.setId(admin.getId());
                    userInfo.setUsername(admin.getUsername());
                    userInfo.setName(admin.getName());
                    userInfo.setRole(role);
                    userInfo.setAvatar(""); // 管理员默认头像
                    return Result.success(userInfo);
                }
            } else {
                // 普通用户信息
                User user = userService.getUserByPhone(username); // 假设username是手机号
                if (user != null) {
                    UserInfoDTO userInfo = new UserInfoDTO();
                    userInfo.setId(user.getId());
                    userInfo.setUsername(user.getPhone());
                    userInfo.setName(user.getNickname());
                    userInfo.setRole("user");
                    userInfo.setAvatar(user.getAvatar());
                    return Result.success(userInfo);
                }
            }
            
            return Result.error("用户不存在");
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户信息DTO
     */
    public static class UserInfoDTO {
        private Long id;
        private String username;
        private String name;
        private String role;
        private String avatar;
        
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
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getAvatar() {
            return avatar;
        }
        
        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}
