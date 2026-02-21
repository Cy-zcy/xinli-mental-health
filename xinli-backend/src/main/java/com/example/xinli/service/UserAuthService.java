package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.dto.UpdateUserInfoRequest;
import com.example.xinli.dto.UserLoginRequest;
import com.example.xinli.dto.UserLoginResponse;
import com.example.xinli.dto.UserRegisterRequest;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.UserMapper;
import com.example.xinli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 用户认证服务类
 * 处理用户注册、登录、信息更新等业务逻辑
 */
@Service
public class UserAuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    /**
     * 用户注册
     */
    @Transactional
    public User register(UserRegisterRequest request) {
        // 1. 验证手机号格式
        if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new RuntimeException("手机号格式不正确");
        }
        
        // 2. 检查手机号是否已注册
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该手机号已注册");
        }
        
        // 3. 验证密码强度
        if (request.getPassword().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        
        // 4. 验证昵称
        if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
            throw new RuntimeException("昵称不能为空");
        }
        
        // 检查昵称是否已存在
        QueryWrapper<User> nicknameWrapper = new QueryWrapper<>();
        nicknameWrapper.eq("nickname", request.getNickname().trim());
        if (userMapper.selectCount(nicknameWrapper) > 0) {
            throw new RuntimeException("该昵称已被使用");
        }
        
        // 5. 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        user.setAvatar(request.getAvatar()); // 可以为空，使用默认头像
        user.setStatus(1); // 默认状态为正常
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        int result = userMapper.insert(user);
        if (result > 0) {
            // 不返回密码
            user.setPassword(null);
            return user;
        } else {
            throw new RuntimeException("注册失败，请稍后重试");
        }
    }
    
    /**
     * 用户登录
     */
    public UserLoginResponse login(UserLoginRequest request) {
        // 1. 查找用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("手机号或密码错误");
        }
        
        // 2. 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用，请联系客服");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("手机号或密码错误");
        }
        
        // 4. 生成JWT token
        String token = jwtUtil.generateToken(user.getPhone(), user.getId(), "user");
        
        // 5. 构建响应
        UserLoginResponse.UserInfo userInfo = new UserLoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setPhone(user.getPhone());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setStatus(user.getStatus());
        userInfo.setCreatedAt(user.getCreatedAt());
        
        return new UserLoginResponse(token, jwtExpiration, userInfo);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        // 1. 查找用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用");
        }
        
        // 2. 更新昵称（如果提供）
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            String newNickname = request.getNickname().trim();
            
            // 检查昵称是否已被其他用户使用
            QueryWrapper<User> nicknameWrapper = new QueryWrapper<>();
            nicknameWrapper.eq("nickname", newNickname);
            nicknameWrapper.ne("id", userId);
            if (userMapper.selectCount(nicknameWrapper) > 0) {
                throw new RuntimeException("该昵称已被使用");
            }
            
            user.setNickname(newNickname);
        }
        
        // 3. 更新头像（如果提供）
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        // 4. 更新密码（如果提供）
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            // 验证旧密码
            if (request.getOldPassword() == null || 
                !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("原密码不正确");
            }
            
            // 验证新密码强度
            if (request.getNewPassword().length() < 6) {
                throw new RuntimeException("新密码长度不能少于6位");
            }
            
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        
        // 5. 更新时间
        user.setUpdatedAt(LocalDateTime.now());
        
        // 6. 保存更新
        int result = userMapper.updateById(user);
        if (result > 0) {
            // 不返回密码
            user.setPassword(null);
            return user;
        } else {
            throw new RuntimeException("更新用户信息失败");
        }
    }
    
    /**
     * 检查手机号是否已注册
     */
    public boolean checkPhoneExists(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new RuntimeException("手机号格式不正确");
        }
        
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        return userMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 刷新token
     */
    public UserLoginResponse refreshToken(String oldToken) {
        // 1. 验证旧token
        if (!jwtUtil.validateToken(oldToken, jwtUtil.getUsernameFromToken(oldToken))) {
            throw new RuntimeException("无效的token");
        }
        
        // 2. 获取用户信息
        String phone = jwtUtil.getUsernameFromToken(oldToken);
        Long userId = jwtUtil.getUserIdFromToken(oldToken);
        
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null || user.getStatus() != 1) {
            throw new RuntimeException("用户不存在或已被禁用");
        }
        
        // 3. 生成新token
        String newToken = jwtUtil.generateToken(user.getPhone(), user.getId(), "user");
        
        // 4. 构建响应
        UserLoginResponse.UserInfo userInfo = new UserLoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setPhone(user.getPhone());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setStatus(user.getStatus());
        userInfo.setCreatedAt(user.getCreatedAt());
        
        return new UserLoginResponse(newToken, jwtExpiration, userInfo);
    }

    /**
     * 修改用户密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查找用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用");
        }

        // 2. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }

        // 3. 验证新密码强度
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }

        // 4. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        // 5. 保存更新
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new RuntimeException("修改密码失败");
        }
    }
}
