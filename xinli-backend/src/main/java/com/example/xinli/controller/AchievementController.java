package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.ChatSession;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.entity.UserAssessmentRecord;
import com.example.xinli.mapper.ChatSessionMapper;
import com.example.xinli.mapper.ForumPostMapper;
import com.example.xinli.mapper.ToolRecordMapper;
import com.example.xinli.mapper.UserAssessmentRecordMapper;
import com.example.xinli.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 用户端 - 成就徽章控制器
 * 路由前缀: /api/achievements
 *
 * 成就系统设计原则：
 *  - 所有判断逻辑在后端完成，前端只负责渲染
 *  - 每个成就有唯一 id、标题、说明、图标、颜色、是否达成、当前进度、目标值
 */
@RestController
@RequestMapping("/api/achievements")
@CrossOrigin
public class AchievementController {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ToolRecordMapper toolRecordMapper;

    @Autowired
    private UserAssessmentRecordMapper assessmentRecordMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的所有成就（含未达成的，显示进度）
     * GET /api/achievements
     */
    @GetMapping
    public Result<List<Map<String, Object>>> getAchievements(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }

            // ======= 数据聚合 =======

            // AI 聊天会话总数
            long chatSessions = chatSessionMapper.selectCount(
                    new QueryWrapper<ChatSession>().eq("user_id", userId));

            // 发帖数（已审核通过）
            long forumPosts = forumPostMapper.selectCount(
                    new QueryWrapper<ForumPost>().eq("user_id", userId).eq("status", 1));

            // 冥想总分钟数（已完成）
            Integer meditationMinutes = toolRecordMapper.sumTotalMinutes(userId);
            int totalMeditationMinutes = meditationMinutes != null ? meditationMinutes : 0;

            // 呼吸练习完成次数
            Integer breathingSessions = toolRecordMapper.countByType(userId, "breathing");
            int totalBreathing = breathingSessions != null ? breathingSessions : 0;

            // 测评完成次数
            long assessmentCount = assessmentRecordMapper.selectCount(
                    new QueryWrapper<UserAssessmentRecord>().eq("user_id", userId));

            // 工具使用活跃天数
            Integer activeDays = toolRecordMapper.countActiveDays(userId);
            int totalActiveDays = activeDays != null ? activeDays : 0;

            // 获得点赞总数
            Long totalLikes = forumPostMapper.getTotalLikesByUserId(userId);
            long likes = totalLikes != null ? totalLikes : 0;

            // ======= 构建成就列表 =======
            List<Map<String, Object>> achievements = new ArrayList<>();

            // 1. 初次对话
            achievements.add(buildAchievement(
                    "first-chat", "初次对话", "完成第一次AI心理对话",
                    "i-ic:outline-chat", "text-blue-500",
                    chatSessions, 1
            ));

            // 2. 对话达人
            achievements.add(buildAchievement(
                    "chat-master", "对话达人", "完成10次AI心理对话",
                    "i-ic:outline-record-voice-over", "text-sky-500",
                    chatSessions, 10
            ));

            // 3. 冥想初体验
            achievements.add(buildAchievement(
                    "meditation-start", "冥想初体验", "完成第一次冥想练习",
                    "i-ic:outline-self-improvement", "text-purple-400",
                    (long) (totalMeditationMinutes > 0 ? 1 : 0), 1
            ));

            // 4. 冥想达人
            achievements.add(buildAchievement(
                    "meditation-master", "冥想达人", "累计冥想30分钟",
                    "i-ic:outline-spa", "text-purple-500",
                    (long) totalMeditationMinutes, 30
            ));

            // 5. 冥想大师
            achievements.add(buildAchievement(
                    "meditation-expert", "冥想大师", "累计冥想100分钟",
                    "i-ic:outline-psychology", "text-violet-600",
                    (long) totalMeditationMinutes, 100
            ));

            // 6. 呼吸练习者
            achievements.add(buildAchievement(
                    "breathing-start", "呼吸练习者", "完成3次呼吸练习",
                    "i-ic:outline-air", "text-cyan-500",
                    (long) totalBreathing, 3
            ));

            // 7. 论坛新手
            achievements.add(buildAchievement(
                    "forum-newbie", "论坛新手", "发布第一篇帖子",
                    "i-ic:outline-edit", "text-green-400",
                    forumPosts, 1
            ));

            // 8. 论坛贡献者
            achievements.add(buildAchievement(
                    "forum-contributor", "论坛贡献者", "发布10篇帖子",
                    "i-ic:outline-forum", "text-green-500",
                    forumPosts, 10
            ));

            // 9. 人气作者
            achievements.add(buildAchievement(
                    "popular-author", "人气作者", "帖子累计获赞20次",
                    "i-ic:outline-thumb-up", "text-pink-500",
                    likes, 20
            ));

            // 10. 测评初探
            achievements.add(buildAchievement(
                    "assessment-start", "测评初探", "完成第一次心理测评",
                    "i-ic:outline-assignment", "text-amber-500",
                    assessmentCount, 1
            ));

            // 11. 全面了解
            achievements.add(buildAchievement(
                    "assessment-all", "全面了解", "完成3种不同心理测评",
                    "i-ic:outline-analytics", "text-orange-500",
                    assessmentCount, 3
            ));

            // 12. 坚持打卡
            achievements.add(buildAchievement(
                    "active-user", "坚持打卡", "累计使用工具7天",
                    "i-ic:outline-calendar-today", "text-rose-500",
                    (long) totalActiveDays, 7
            ));

            return Result.success(achievements);

        } catch (Exception e) {
            return Result.error("获取成就失败: " + e.getMessage());
        }
    }

    /**
     * 构建单个成就数据对象
     */
    private Map<String, Object> buildAchievement(
            String id, String title, String description,
            String icon, String color,
            long current, long target) {

        boolean earned = current >= target;
        // 进度百分比（最大100）
        int progress = (int) Math.min(100, (current * 100 / target));

        Map<String, Object> achievement = new LinkedHashMap<>();
        achievement.put("id", id);
        achievement.put("title", title);
        achievement.put("description", description);
        achievement.put("icon", icon);
        achievement.put("color", color);
        achievement.put("earned", earned);
        achievement.put("current", current);
        achievement.put("target", target);
        achievement.put("progress", progress);
        return achievement;
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
