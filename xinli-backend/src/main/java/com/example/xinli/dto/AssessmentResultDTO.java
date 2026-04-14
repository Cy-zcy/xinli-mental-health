package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 测评结果 DTO，返回给用户端展示评估结论
 */
@Data
public class AssessmentResultDTO {
    private Long recordId;
    private Long assessmentId;
    private String assessmentTitle;
    private Integer totalScore;
    private String resultSummary;  // 如：轻度抑郁
    private String resultDetails;  // 详细建议
    private LocalDateTime createdAt;

    // ===== 维度分析（雷达图数据） =====
    /** 各维度得分（0-100分） */
    private Map<String, Integer> dimensionScores;

    /** 维度名称列表（前端渲染用） */
    private java.util.List<String> dimensionNames;

    /** 维度分数列表（前端渲染用，与dimensionNames一一对应） */
    private java.util.List<Integer> dimensionValues;

    /** 与上次测评的变化趋势（对比用） */
    private Map<String, Integer> dimensionChanges;
}
