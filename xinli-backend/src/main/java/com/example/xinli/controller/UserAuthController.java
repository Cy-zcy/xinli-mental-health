package com.example.xinli.controller;

import com.example.xinli.dto.Result;
import com.example.xinli.dto.UserLoginRequest;
import com.example.xinli.dto.UserLoginResponse;
import com.example.xinli.dto.UserRegisterRequest;
import com.example.xinli.dto.UpdateUserInfoRequest;
import com.example.xinli.dto.ChangePasswordRequest;
import com.example.xinli.entity.User;
import com.example.xinli.service.UserAuthService;
import com.example.xinli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 用户端认证控制器
 * 处理用户注册、登录、个人信息管理等功能
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserAuthController {
    
    @Autowired
    private UserAuthService userAuthService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 1. 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/auth/register")
    public Result<User> register(@Valid @RequestBody UserRegisterRequest request) {
        try {
            User user = userAuthService.register(request);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }
    
    /**
     * 2. 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/auth/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            UserLoginResponse response = userAuthService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(401, "登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 3. 更新用户个人信息
     * PUT /api/user/info
     * 需要用户认证
     */
    @PutMapping("/user/info")
    public Result<User> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest request, HttpServletRequest httpRequest) {
        try {
            // 从JWT token获取用户ID
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }

            User updatedUser = userAuthService.updateUserInfo(userId, request);
            return Result.success(updatedUser);
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 4. 修改用户密码
     * PUT /api/user/password
     * 需要用户认证
     */
    @PutMapping("/user/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        try {
            // 从JWT token获取用户ID
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }

            userAuthService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return Result.success();
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 5. 用户退出登录
     * POST /api/auth/logout
     */
    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        // JWT是无状态的，前端删除token即可
        // 这里可以添加token黑名单机制
        return Result.success();
    }
    
    /**
     * 5. 验证手机号是否已注册
     * GET /api/auth/check-phone
     */
    @GetMapping("/auth/check-phone")
    public Result<Boolean> checkPhoneExists(@RequestParam String phone) {
        try {
            boolean exists = userAuthService.checkPhoneExists(phone);
            return Result.success(exists);
        } catch (Exception e) {
            return Result.error("验证手机号失败: " + e.getMessage());
        }
    }
    
    /**
     * 6. 刷新用户token
     * POST /api/auth/refresh
     */
    @PostMapping("/auth/refresh")
    public Result<UserLoginResponse> refreshToken(HttpServletRequest httpRequest) {
        try {
            String token = getTokenFromRequest(httpRequest);
            if (token == null) {
                return Result.error(401, "未提供token");
            }
            
            UserLoginResponse response = userAuthService.refreshToken(token);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(401, "刷新token失败: " + e.getMessage());
        }
    }
    
    /**
     * 从HTTP请求中提取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token != null) {
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // Token解析失败，返回null
        }
        return null;
    }
    
    /**
     * 从HTTP请求中提取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}
