package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
}
