package com.example.xinli.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.entity.Admin;
import com.example.xinli.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化组件
 * 在应用启动时创建默认的管理员账号
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initDefaultAdmin();
    }
    
    /**
     * 初始化默认管理员账号
     */
    private void initDefaultAdmin() {
        // 检查是否已存在管理员账号
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", "admin");
        Admin existingAdmin = adminMapper.selectOne(wrapper);
        
        if (existingAdmin == null) {
            // 创建默认管理员账号
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("系统管理员");
            admin.setStatus(1); // 正常状态
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            
            adminMapper.insert(admin);
            System.out.println("✅ 默认管理员账号创建成功:");
            System.out.println("   用户名: admin");
            System.out.println("   密码: admin123");
            System.out.println("   请及时修改默认密码！");
        } else {
            System.out.println("ℹ️ 管理员账号已存在，跳过初始化");
        }
    }
}
