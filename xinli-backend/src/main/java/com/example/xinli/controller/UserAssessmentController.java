package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.AssessmentDetailDTO;
import com.example.xinli.dto.AssessmentResultDTO;
import com.example.xinli.dto.Result;
import com.example.xinli.dto.SubmitAssessmentRequest;
import com.example.xinli.entity.Assessment;
import com.example.xinli.service.AssessmentService;
import com.example.xinli.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 心理测评控制器
 * 路由前缀: /api/assessment
 */
@RestController
@RequestMapping("/api/assessment")
@CrossOrigin
public class UserAssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 1. 获取已发布的问卷列表（分页）
     * GET /api/assessment/list
     */
    @GetMapping("/list")
    public Result<Page<Assessment>> getAssessmentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return Result.success(assessmentService.getPublishedAssessments(page, size));
        } catch (Exception e) {
            return Result.error("获取问卷列表失败: " + e.getMessage());
        }
    }

    /**
     * 2. 获取问卷详情（含题目和选项），用于渲染答题页
     * GET /api/assessment/{id}
     */
    @GetMapping("/{id}")
    public Result<AssessmentDetailDTO> getAssessmentDetail(@PathVariable Long id) {
        try {
            return Result.success(assessmentService.getAssessmentDetail(id));
        } catch (Exception e) {
            return Result.error("获取问卷详情失败: " + e.getMessage());
        }
    }

    /**
     * 3. 用户提交答卷，获取评估结果
     * POST /api/assessment/submit
     * 需要登录
     */
    @PostMapping("/submit")
    public Result<AssessmentResultDTO> submitAssessment(
            @Valid @RequestBody SubmitAssessmentRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "请先登录后再进行测评");
            }
            return Result.success(assessmentService.submitAssessment(userId, request));
        } catch (Exception e) {
            return Result.error("提交答卷失败: " + e.getMessage());
        }
    }

    /**
     * 4. 查询当前用户的历史测评记录
     * GET /api/assessment/history
     * 需要登录
     */
    @GetMapping("/history")
    public Result<List<AssessmentResultDTO>> getUserHistory(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(assessmentService.getUserAssessmentHistory(userId));
        } catch (Exception e) {
            return Result.error("获取历史记录失败: " + e.getMessage());
        }
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
