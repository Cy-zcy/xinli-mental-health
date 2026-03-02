package com.example.xinli.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

/**
 * 用户提交答卷请求 DTO
 * answers 的 key 为 questionId，value 为选中的 optionId
 */
@Data
public class SubmitAssessmentRequest {
    @NotNull(message = "问卷ID不能为空")
    private Long assessmentId;

    @NotEmpty(message = "答案不能为空")
    private Map<Long, Long> answers; // key: questionId, value: optionId
}
