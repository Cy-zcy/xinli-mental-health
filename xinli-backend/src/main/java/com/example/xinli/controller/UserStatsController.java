package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.ChatSession;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.mapper.ChatSessionMapper;
import com.example.xinli.mapper.ForumPostMapper;
import com.example.xinli.mapper.ToolRecordMapper;
import com.example.xinli.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户端 - 统计数据控制器
 * 路由前缀: /api/user/stats
 * 提供用户主页所需的统计概况
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserStatsController {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ToolRecordMapper toolRecordMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的统计数据汇总
     * GET /api/user/stats
     * 返回：AI对话次数、发布帖子数、获得点赞数、工具使用分钟数
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }

            // 1. AI 聊天会话数
            long chatSessions = chatSessionMapper.selectCount(
                    new QueryWrapper<ChatSession>().eq("user_id", userId));

            // 2. 发帖数（已审核通过的）
            long forumPosts = forumPostMapper.selectCount(
                    new QueryWrapper<ForumPost>().eq("user_id", userId).eq("status", 1));

            // 3. 获得的点赞总数（各帖子 like_count 之和）
            Long totalLikes = forumPostMapper.getTotalLikesByUserId(userId);

            // 4. 工具使用总分钟数（只统计已完成的）
            Integer toolMinutes = toolRecordMapper.sumTotalMinutes(userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("chatSessions", chatSessions);
            stats.put("forumPosts", forumPosts);
            stats.put("totalLikes", totalLikes != null ? totalLikes : 0);
            stats.put("toolMinutes", toolMinutes != null ? toolMinutes : 0);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                return jwtUtil.getUserIdFromToken(token.substring(7));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
