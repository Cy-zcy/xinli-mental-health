package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.entity.Admin;
import com.example.xinli.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysAdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private com.example.xinli.mapper.SysRoleMapper sysRoleMapper; // 引入角色查询

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<Admin> getAdminsPage(int current, int size, String keyword) {
        Page<Admin> page = new Page<>(current, size);
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like("username", keyword).or().like("name", keyword);
        }
        wrapper.orderByDesc("created_at");
        Page<Admin> adminPage = adminMapper.selectPage(page, wrapper);
        
        // 擦除返回给前端的密码并写入角色名称
        adminPage.getRecords().forEach(admin -> {
            admin.setPassword(null);
            if (admin.getRoleId() != null) {
                com.example.xinli.entity.SysRole role = sysRoleMapper.selectById(admin.getRoleId());
                if (role != null) {
                    admin.setRoleName(role.getRoleName());
                }
            }
        });
        
        return adminPage;
    }

    public void createAdmin(Admin admin) {
        if (admin.getStatus() == null) {
            admin.setStatus(1);
        }
        
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", admin.getUsername());
        if (adminMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 默认密码 123456 加密
        String rawPassword = StringUtils.hasText(admin.getPassword()) ? admin.getPassword() : "123456";
        admin.setPassword(passwordEncoder.encode(rawPassword));
        
        adminMapper.insert(admin);
    }

    public void updateAdmin(Admin admin) {
        // 如果是修改超级管理员（默认 username='admin'或 id=1），不许修改其 roleId
        if (admin.getId() == 1L && admin.getRoleId() != null && admin.getRoleId() != 1L) {
            throw new RuntimeException("超级管理员角色不可更改");
        }
        
        admin.setPassword(null); // 防止外层直接覆盖密码
        adminMapper.updateById(admin);
    }

    public void deleteAdmin(Long id) {
        if (id == 1L) {
            throw new RuntimeException("系统初始超级管理员不可删除");
        }
        adminMapper.deleteById(id);
    }

    public void updateAdminStatus(Long id, Integer status) {
        if (id == 1L && status == 0) {
            throw new RuntimeException("系统初始超级管理员不可禁用");
        }
        Admin admin = new Admin();
        admin.setId(id);
        admin.setStatus(status);
        adminMapper.updateById(admin);
    }

    public void resetPassword(Long id, String newPassword) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminMapper.updateById(admin);
    }
}
