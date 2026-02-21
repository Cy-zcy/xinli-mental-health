package com.example.xinli.controller;

import com.example.xinli.dto.DashboardStatsDTO;
import com.example.xinli.dto.Result;
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
     * 获取仪表板统计数据
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
}
