package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDetailDTO {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private Long postCount;      // 发帖数量
    private Long commentCount;   // 评论数量
    private Long likeCount;      // 获赞数量
}
