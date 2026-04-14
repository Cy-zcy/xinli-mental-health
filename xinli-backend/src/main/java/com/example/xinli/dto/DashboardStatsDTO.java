package com.example.xinli.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private Long totalUsers;        // 用户总数
    private Long totalPosts;        // 帖子总数
    private Long totalComments;     // 评论总数
    private Long todayNewUsers;     // 今日新增用户
    private Long todayNewPosts;     // 今日新增帖子
    private Long activeUsers;       // 活跃用户数（状态为1的用户）
    private Long pendingPosts;      // 待审核帖子数（状态为0的帖子）

    // ===== 心理测评统计 =====
    private Long totalAssessments;  // 测评总次数
    private Long normalCount;       // 正常人数
    private Long mildCount;         // 轻度抑郁人数
    private Long moderateCount;     // 中度抑郁人数
    private Long severeCount;       // 重度抑郁人数（高风险）

    // ===== 测评趋势统计（近6个月） =====
    private java.util.List<MonthlyTrend> trends;

    // ===== 用户增长趋势（近6个月） =====
    private java.util.List<UserGrowthTrend> userGrowthTrends;

    // ===== 帖子分类统计 =====
    private java.util.List<PostCategoryStats> postCategoryStats;

    @Data
    public static class MonthlyTrend {
        private String month;       // 月份 (如: 2023-10)
        private Long total;         // 该月总评测数
        private Long highRisk;      // 该月高危数(重度抑郁)
    }

    @Data
    public static class UserGrowthTrend {
        private String month;       // 月份 (如: 2023-10)
        private Long newUsers;      // 该月新增用户数
    }

    @Data
    public static class PostCategoryStats {
        private String category;    // 帖子分类
        private Long count;         // 该分类帖子数量
    }
}

