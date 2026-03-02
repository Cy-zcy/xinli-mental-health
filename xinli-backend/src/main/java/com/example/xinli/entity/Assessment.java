package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assessments")
public class Assessment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;     // 问卷标题

    private String description; // 问卷描述/指导语

    private Integer status;   // 1-正常 0-下架

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
