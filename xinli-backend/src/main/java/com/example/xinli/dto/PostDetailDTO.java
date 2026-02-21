package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailDTO {
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
    private boolean liked; // 当前用户是否已点赞
    private UserInfo author; // 作者信息
    
    // 作者信息
    private String authorNickname;
    private String authorAvatar;
    
    // 评论列表
    private List<CommentDTO> comments;
    
    @Data
    public static class CommentDTO {
        private Long id;
        private Long userId;
        private String content;
        private Integer status;
        private LocalDateTime createdAt;

        // 评论者信息
        private String userNickname;
        private String userAvatar;
    }

    @Data
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }
}
