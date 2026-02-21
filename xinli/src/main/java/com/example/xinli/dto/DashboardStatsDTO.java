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
}
