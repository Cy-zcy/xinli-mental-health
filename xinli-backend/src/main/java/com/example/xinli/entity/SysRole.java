package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roleName;
    
    private String roleCode;
    
    private String description;
    
    private Integer status; // 1正常 0禁用
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
