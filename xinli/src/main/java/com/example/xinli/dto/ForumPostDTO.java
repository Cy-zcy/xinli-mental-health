package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumPostDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String category;
    private Integer likeCount;
    private Integer viewCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 作者信息
    private UserInfo author;

    @Data
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }
}
