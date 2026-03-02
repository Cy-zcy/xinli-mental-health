package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assessment_questions")
public class AssessmentQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assessmentId; // 所属问卷ID

    private String content;    // 题干内容

    private String type;       // 题目类型 (single_choice)

    private Integer sortOrder; // 排序

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
