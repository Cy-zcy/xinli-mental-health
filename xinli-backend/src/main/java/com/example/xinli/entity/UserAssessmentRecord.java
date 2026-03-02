package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_assessment_records")
public class UserAssessmentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;          // 答题用户ID

    private Long assessmentId;    // 所答问卷ID

    private Integer totalScore;   // 测评总得分

    private String resultSummary; // 结果概述 (如: 轻度抑郁)

    private String resultDetails; // 详细评估建议

    private LocalDateTime createdAt;
}
