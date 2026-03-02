package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("intervention_resources")
public class InterventionResource {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;       // 资源标题

    private String type;        // 资源类型: article / audio / video

    private String content;     // 文章正文（article 类型）

    private String resourceUrl; // 音频/视频 URL

    private String coverUrl;    // 封面图片 URL

    private String tags;        // 标签，逗号分隔

    private String description; // 简介

    private Integer viewCount;  // 浏览次数

    private Integer status;     // 1-上架 0-下架

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
