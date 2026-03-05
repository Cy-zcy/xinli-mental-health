package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String phone;
    
    private String password;
    
    private String nickname;
    
    private String avatar;
    
    private Integer status; // 1正常 0禁用
    
    private Integer healthScore; // 用户心理健康安全分(0-100)
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
