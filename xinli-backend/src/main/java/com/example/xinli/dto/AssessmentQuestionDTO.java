package com.example.xinli.dto;

import com.example.xinli.entity.AssessmentOption;
import lombok.Data;

import java.util.List;

/**
 * 问卷题目（含选项）DTO，用于前端渲染答题页面
 */
@Data
public class AssessmentQuestionDTO {
    private Long id;
    private String content;           // 题干
    private String type;              // 题目类型
    private Integer sortOrder;        // 排序
    private List<AssessmentOption> options; // 该题目的所有选项
}
