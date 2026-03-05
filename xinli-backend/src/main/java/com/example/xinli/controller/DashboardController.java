package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.DashboardStatsDTO;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.UserAssessmentRecord;
import com.example.xinli.entity.User;
import com.example.xinli.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    /**
     * 获取仪表板统计数据（含心理测评分布）
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getDashboardStats() {
        try {
            DashboardStatsDTO stats = dashboardService.getDashboardStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取高风险用户列表（重度抑郁）
     * GET /api/admin/dashboard/high-risk?page=1&size=10
     */
    @GetMapping("/high-risk")
    public Result<Page<UserAssessmentRecord>> getHighRiskUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return Result.success(dashboardService.getHighRiskUsers(page, size));
        } catch (Exception e) {
            return Result.error("获取高风险用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取健康分极低的预警用户列表（< 60 分）
     * GET /api/admin/dashboard/low-health-score?page=1&size=10
     */
    @GetMapping("/low-health-score")
    public Result<Page<User>> getLowHealthScoreUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return Result.success(dashboardService.getLowHealthScoreUsers(page, size));
        } catch (Exception e) {
            return Result.error("获取健康分预警列表失败: " + e.getMessage());
        }
    }
}
