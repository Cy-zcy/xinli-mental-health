package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminDTO {
    private Long id;
    private String username;
    private String name;
    private String role;
    private String[] roles; // 角色编码数组
    private java.util.List<String> permissions; // 前端菜单权限标识
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
