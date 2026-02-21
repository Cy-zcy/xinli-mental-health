package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.dto.AdminDTO;
import com.example.xinli.dto.LoginRequest;
import com.example.xinli.dto.LoginResponse;
import com.example.xinli.entity.Admin;
import com.example.xinli.mapper.AdminMapper;
import com.example.xinli.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    /**
     * 管理员登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查找管理员
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        Admin admin = adminMapper.selectOne(wrapper);
        
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查账户状态
        if (admin.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成JWT token
        String token = jwtUtil.generateToken(admin.getUsername(), admin.getId(), "admin");
        
        // 构建响应
        LoginResponse.AdminInfo adminInfo = new LoginResponse.AdminInfo();
        adminInfo.setId(admin.getId());
        adminInfo.setUsername(admin.getUsername());
        adminInfo.setName(admin.getName());
        
        return new LoginResponse(token, jwtExpiration, adminInfo);
    }
    
    /**
     * 获取当前用户信息
     */
    public AdminDTO getCurrentUser(String username) {
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        Admin admin = adminMapper.selectOne(wrapper);
        
        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }
        
        AdminDTO adminDTO = new AdminDTO();
        BeanUtils.copyProperties(admin, adminDTO);
        return adminDTO;
    }
    
    /**
     * 根据用户名查找管理员
     */
    public Admin findByUsername(String username) {
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return adminMapper.selectOne(wrapper);
    }
}
