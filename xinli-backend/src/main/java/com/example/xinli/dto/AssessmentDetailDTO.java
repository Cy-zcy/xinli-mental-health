package com.example.xinli.dto;

import lombok.Data;
import java.util.List;

/**
 * 完整问卷详情 DTO，包含问卷信息和全部题目（含选项）
 * 用于用户端答题页面的数据加载
 */
@Data
public class AssessmentDetailDTO {
    private Long id;
    private String title;
    private String description;
    private List<AssessmentQuestionDTO> questions;
}
