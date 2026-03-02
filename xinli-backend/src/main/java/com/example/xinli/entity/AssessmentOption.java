package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assessment_options")
public class AssessmentOption {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId; // 所属题目ID

    private String content;  // 选项文字内容

    private Integer score;   // 选项对应的分值

    private Integer sortOrder; // 排序

    private LocalDateTime createdAt;
}
