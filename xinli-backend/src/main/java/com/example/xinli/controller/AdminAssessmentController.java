package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.Assessment;
import com.example.xinli.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 心理测评管理控制器
 * 路由前缀: /api/admin/assessment
 */
@RestController
@RequestMapping("/api/admin/assessment")
@CrossOrigin
public class AdminAssessmentController {

    @Autowired
    private AssessmentService assessmentService;

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
     * Body: { "title": "...", "description": "..." }
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
     * 4. 删除问卷（级联删除题目和选项）
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
}
