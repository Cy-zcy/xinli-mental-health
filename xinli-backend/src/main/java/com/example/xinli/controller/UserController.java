package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.dto.UserDetailDTO;
import com.example.xinli.entity.ChatSession;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.entity.User;
import com.example.xinli.entity.UserAssessmentRecord;
import com.example.xinli.entity.UserHealthScoreRecord;
import com.example.xinli.mapper.ChatSessionMapper;
import com.example.xinli.mapper.ForumPostMapper;
import com.example.xinli.mapper.ToolRecordMapper;
import com.example.xinli.mapper.UserAssessmentRecordMapper;
import com.example.xinli.mapper.UserHealthScoreRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xinli.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ToolRecordMapper toolRecordMapper;

    @Autowired
    private UserAssessmentRecordMapper assessmentRecordMapper;

    @Autowired
    private UserHealthScoreRecordMapper healthScoreRecordMapper;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<Page<User>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            Page<User> users = userService.getUsers(page, size, keyword, status);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserDetailDTO> getUserDetail(@PathVariable Long id) {
        try {
            UserDetailDTO userDetail = userService.getUserDetail(id);
            if (userDetail != null) {
                return Result.success(userDetail);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = userService.updateUserStatus(id, status);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新用户状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            boolean success = userService.updateUser(user);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新用户失败");
            }
        } catch (Exception e) {
            return Result.error("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除用户失败");
            }
        } catch (Exception e) {
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 管理端 - 查看指定用户的统计数据
     * GET /api/admin/users/{id}/stats
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long id) {
        try {
            long chatSessions = chatSessionMapper.selectCount(
                    new QueryWrapper<ChatSession>().eq("user_id", id));
            long forumPosts = forumPostMapper.selectCount(
                    new QueryWrapper<ForumPost>().eq("user_id", id).eq("status", 1));
            Long totalLikes = forumPostMapper.getTotalLikesByUserId(id);
            Integer toolMinutes = toolRecordMapper.sumTotalMinutes(id);
            long assessments = assessmentRecordMapper.selectCount(
                    new QueryWrapper<UserAssessmentRecord>().eq("user_id", id));

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("chatSessions", chatSessions);
            stats.put("forumPosts", forumPosts);
            stats.put("totalLikes", totalLikes != null ? totalLikes : 0);
            stats.put("toolMinutes", toolMinutes != null ? toolMinutes : 0);
            stats.put("assessments", assessments);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取用户统计失败: " + e.getMessage());
        }
    }

    /**
     * 管理端 - 查看指定用户的成就情况
     * GET /api/admin/users/{id}/achievements
     * 返回 12 个成就的达成状态及进度，管理员可据此了解用户参与度
     */
    @GetMapping("/{id}/achievements")
    public Result<List<Map<String, Object>>> getUserAchievements(@PathVariable Long id) {
        try {
            long chatSessions = chatSessionMapper.selectCount(
                    new QueryWrapper<ChatSession>().eq("user_id", id));
            long forumPosts = forumPostMapper.selectCount(
                    new QueryWrapper<ForumPost>().eq("user_id", id).eq("status", 1));
            Long totalLikes = forumPostMapper.getTotalLikesByUserId(id);
            long likes = totalLikes != null ? totalLikes : 0;

            Integer meditationMinutes = toolRecordMapper.sumTotalMinutes(id);
            int totalMeditationMinutes = meditationMinutes != null ? meditationMinutes : 0;
            Integer breathingSessions = toolRecordMapper.countByType(id, "breathing");
            int totalBreathing = breathingSessions != null ? breathingSessions : 0;
            Integer activeDays = toolRecordMapper.countActiveDays(id);
            int totalActiveDays = activeDays != null ? activeDays : 0;

            long assessmentCount = assessmentRecordMapper.selectCount(
                    new QueryWrapper<UserAssessmentRecord>().eq("user_id", id));

            List<Map<String, Object>> achievements = new ArrayList<>();
            achievements.add(buildAchievement("first-chat", "初次对话", "完成第一次AI心理对话", chatSessions, 1));
            achievements.add(buildAchievement("chat-master", "对话达人", "完成10次AI心理对话", chatSessions, 10));
            achievements.add(buildAchievement("meditation-start", "冥想初体验", "完成第一次冥想练习",
                    (long)(totalMeditationMinutes > 0 ? 1 : 0), 1));
            achievements.add(buildAchievement("meditation-master", "冥想达人", "累计冥想30分钟", (long) totalMeditationMinutes, 30));
            achievements.add(buildAchievement("meditation-expert", "冥想大师", "累计冥想100分钟", (long) totalMeditationMinutes, 100));
            achievements.add(buildAchievement("breathing-start", "呼吸练习者", "完成3次呼吸练习", (long) totalBreathing, 3));
            achievements.add(buildAchievement("forum-newbie", "论坛新手", "发布第一篇帖子", forumPosts, 1));
            achievements.add(buildAchievement("forum-contributor", "论坛贡献者", "发布10篇帖子", forumPosts, 10));
            achievements.add(buildAchievement("popular-author", "人气作者", "帖子累计获赞20次", likes, 20));
            achievements.add(buildAchievement("assessment-start", "测评初探", "完成第一次心理测评", assessmentCount, 1));
            achievements.add(buildAchievement("assessment-all", "全面了解", "完成3种不同心理测评", assessmentCount, 3));
            achievements.add(buildAchievement("active-user", "坚持打卡", "累计使用工具7天", (long) totalActiveDays, 7));

            return Result.success(achievements);
        } catch (Exception e) {
            return Result.error("获取用户成就失败: " + e.getMessage());
        }
    }

    private Map<String, Object> buildAchievement(String id, String title, String description,
                                                   long current, long target) {
        boolean earned = current >= target;
        int progress = (int) Math.min(100, (current * 100 / target));
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("id", id);
        a.put("title", title);
        a.put("description", description);
        a.put("earned", earned);
        a.put("current", current);
        a.put("target", target);
        a.put("progress", progress);
        return a;
    }

    /**
     * 管理端 - 查看指定用户的健康分变动流水
     * GET /api/admin/users/{id}/health-score-records?page=1&size=10
     */
    @GetMapping("/{id}/health-score-records")
    public Result<Page<UserHealthScoreRecord>> getUserHealthScoreRecords(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<UserHealthScoreRecord> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<UserHealthScoreRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserHealthScoreRecord::getUserId, id)
                   .orderByDesc(UserHealthScoreRecord::getCreatedAt);
            Page<UserHealthScoreRecord> result = healthScoreRecordMapper.selectPage(pageParam, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取用户健康分流水失败: " + e.getMessage());
        }
    }
}

