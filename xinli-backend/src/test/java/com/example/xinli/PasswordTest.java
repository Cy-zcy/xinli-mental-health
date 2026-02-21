package com.example.xinli;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    
    @Test
    public void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "admin123";
        String encodedPassword = "$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G";
        
        System.out.println("原密码: " + rawPassword);
        System.out.println("加密密码: " + encodedPassword);
        System.out.println("验证结果: " + encoder.matches(rawPassword, encodedPassword));
        
        // 生成新的加密密码
        String newEncoded = encoder.encode(rawPassword);
        System.out.println("新加密密码: " + newEncoded);
        System.out.println("新密码验证: " + encoder.matches(rawPassword, newEncoded));
    }
}
