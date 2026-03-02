package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 干预资源列表项 DTO（用于列表页，不包含全文 content）
 */
@Data
public class ResourceDTO {
    private Long id;
    private String title;
    private String type;        // article / audio / video
    private String coverUrl;
    private String tags;
    private String description;
    private Integer viewCount;
    private LocalDateTime createdAt;
}
