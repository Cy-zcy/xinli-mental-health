package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminDTO {
    private Long id;
    private String username;
    private String name;
    private String role = "admin";
    private String[] roles = {"R_ADMIN"}; // 添加角色数组，前端需要这个格式
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
