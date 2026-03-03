package com.example.xinli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.AssessmentDetailDTO;
import com.example.xinli.dto.AssessmentResultDTO;
import com.example.xinli.dto.SubmitAssessmentRequest;
import com.example.xinli.entity.Assessment;
import com.example.xinli.entity.AssessmentOption;
import com.example.xinli.entity.AssessmentQuestion;

import java.util.List;

public interface AssessmentService {

    /**
     * 获取所有已发布的问卷列表（分页）
     */
    Page<Assessment> getPublishedAssessments(int page, int size);

    /**
     * 获取问卷详情（含题目和选项），用于前端渲染答题页面
     */
    AssessmentDetailDTO getAssessmentDetail(Long assessmentId);

    /**
     * 用户提交答卷，计算分数并生成结果
     */
    AssessmentResultDTO submitAssessment(Long userId, SubmitAssessmentRequest request);

    /**
     * 查询指定用户的历史测评记录
     */
    List<AssessmentResultDTO> getUserAssessmentHistory(Long userId);

    // ============ Admin 端管理接口 - 问卷 ============

    /**
     * 获取所有问卷（含下架的），Admin端分页
     */
    Page<Assessment> getAllAssessments(int page, int size);

    /**
     * 新增问卷
     */
    Assessment createAssessment(Assessment assessment);

    /**
     * 更新问卷状态（上架/下架）
     */
    void updateAssessmentStatus(Long id, Integer status);

    /**
     * 删除问卷（级联删除题目和选项）
     */
    void deleteAssessment(Long id);

    // ============ Admin 端管理接口 - 题目 ============

    /**
     * 新增题目
     */
    AssessmentQuestion createQuestion(AssessmentQuestion question);

    /**
     * 删除题目（级联删除该题目下的所有选项）
     */
    void deleteQuestion(Long questionId);

    // ============ Admin 端管理接口 - 选项 ============

    /**
     * 新增选项
     */
    AssessmentOption createOption(AssessmentOption option);

    /**
     * 删除选项
     */
    void deleteOption(Long optionId);
}
