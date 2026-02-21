package com.example.xinli.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 */
public class PasswordUtil {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 生成测试密码（用于初始化数据）
     */
    public static void main(String[] args) {
        // 生成管理员密码：admin123
        String password = "admin123";
        String encodedPassword = encode(password);
        System.out.println("原密码: " + password);
        System.out.println("加密后: " + encodedPassword);
        
        // 验证
        System.out.println("验证结果: " + matches(password, encodedPassword));
    }
}
