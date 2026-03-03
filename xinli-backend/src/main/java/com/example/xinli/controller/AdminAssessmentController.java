package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.Assessment;
import com.example.xinli.entity.AssessmentOption;
import com.example.xinli.entity.AssessmentQuestion;
import com.example.xinli.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 心理测评管理控制器
 * 路由前缀: /api/admin/assessment
 *
 * 功能：
 * - 问卷 CRUD（增删上下架）
 * - 题目 增删
 * - 选项 增删
 */
@RestController
@RequestMapping("/api/admin/assessment")
@CrossOrigin
public class AdminAssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    // ===================== 问卷管理 =====================

    /**
     * 1. 获取所有问卷列表（含下架，分页）
     * GET /api/admin/assessment/list
     */
    @GetMapping("/list")
    public Result<Page<Assessment>> getAllAssessments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return Result.success(assessmentService.getAllAssessments(page, size));
        } catch (Exception e) {
            return Result.error("获取问卷列表失败: " + e.getMessage());
        }
    }

    /**
     * 2. 新增问卷
     * POST /api/admin/assessment
     */
    @PostMapping
    public Result<Assessment> createAssessment(@Valid @RequestBody Assessment assessment) {
        try {
            return Result.success(assessmentService.createAssessment(assessment), "问卷创建成功");
        } catch (Exception e) {
            return Result.error("创建问卷失败: " + e.getMessage());
        }
    }

    /**
     * 修改问卷
     * PUT /api/admin/assessment/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateAssessment(@PathVariable Long id, @Valid @RequestBody Assessment assessment) {
        try {
            assessment.setId(id);
            assessmentService.updateAssessment(assessment);
            return Result.success(null, "问卷修改成功");
        } catch (Exception e) {
            return Result.error("修改问卷失败: " + e.getMessage());
        }
    }

    /**
     * 3. 更新问卷状态（上架/下架）
     * PUT /api/admin/assessment/{id}/status?status=0
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            assessmentService.updateAssessmentStatus(id, status);
            String msg = status == 1 ? "问卷已上架" : "问卷已下架";
            return Result.success(null, msg);
        } catch (Exception e) {
            return Result.error("更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 4. 删除问卷（手动级联删除所有题目和选项）
     * DELETE /api/admin/assessment/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAssessment(@PathVariable Long id) {
        try {
            assessmentService.deleteAssessment(id);
            return Result.success(null, "问卷已删除");
        } catch (Exception e) {
            return Result.error("删除问卷失败: " + e.getMessage());
        }
    }

    // ===================== 题目管理 =====================

    /**
     * 5. 新增题目
     * POST /api/admin/assessment/question
     * Body: { "assessmentId": 1, "content": "...", "type": "single_choice", "sortOrder": 1 }
     */
    @PostMapping("/question")
    public Result<AssessmentQuestion> createQuestion(@RequestBody AssessmentQuestion question) {
        try {
            return Result.success(assessmentService.createQuestion(question), "题目添加成功");
        } catch (Exception e) {
            return Result.error("添加题目失败: " + e.getMessage());
        }
    }

    /**
     * 修改题目
     * PUT /api/admin/assessment/question/{id}
     */
    @PutMapping("/question/{id}")
    public Result<Void> updateQuestion(@PathVariable Long id, @RequestBody AssessmentQuestion question) {
        try {
            question.setId(id);
            assessmentService.updateQuestion(question);
            return Result.success(null, "题目修改成功");
        } catch (Exception e) {
            return Result.error("修改题目失败: " + e.getMessage());
        }
    }

    /**
     * 6. 删除题目（同时级联删除该题目下所有选项）
     * DELETE /api/admin/assessment/question/{id}
     */
    @DeleteMapping("/question/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        try {
            assessmentService.deleteQuestion(id);
            return Result.success(null, "题目已删除");
        } catch (Exception e) {
            return Result.error("删除题目失败: " + e.getMessage());
        }
    }

    // ===================== 选项管理 =====================

    /**
     * 7. 新增选项
     * POST /api/admin/assessment/option
     * Body: { "questionId": 1, "content": "偶尔有", "score": 1, "sortOrder": 1 }
     */
    @PostMapping("/option")
    public Result<AssessmentOption> createOption(@RequestBody AssessmentOption option) {
        try {
            return Result.success(assessmentService.createOption(option), "选项添加成功");
        } catch (Exception e) {
            return Result.error("添加选项失败: " + e.getMessage());
        }
    }

    /**
     * 修改选项
     * PUT /api/admin/assessment/option/{id}
     */
    @PutMapping("/option/{id}")
    public Result<Void> updateOption(@PathVariable Long id, @RequestBody AssessmentOption option) {
        try {
            option.setId(id);
            assessmentService.updateOption(option);
            return Result.success(null, "选项修改成功");
        } catch (Exception e) {
            return Result.error("修改选项失败: " + e.getMessage());
        }
    }

    /**
     * 8. 删除选项
     * DELETE /api/admin/assessment/option/{id}
     */
    @DeleteMapping("/option/{id}")
    public Result<Void> deleteOption(@PathVariable Long id) {
        try {
            assessmentService.deleteOption(id);
            return Result.success(null, "选项已删除");
        } catch (Exception e) {
            return Result.error("删除选项失败: " + e.getMessage());
        }
    }
}
