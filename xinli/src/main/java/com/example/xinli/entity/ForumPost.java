package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_posts")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private String category; // emotion, experience
    
    private Integer likeCount;
    
    private Integer viewCount;
    
    private Integer status; // 1正常 0删除
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
