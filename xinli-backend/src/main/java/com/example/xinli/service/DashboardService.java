package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.DashboardStatsDTO;
import com.example.xinli.entity.Comment;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.entity.User;
import com.example.xinli.entity.UserAssessmentRecord;
import com.example.xinli.mapper.CommentMapper;
import com.example.xinli.mapper.ForumPostMapper;
import com.example.xinli.mapper.UserAssessmentRecordMapper;
import com.example.xinli.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DashboardService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserAssessmentRecordMapper recordMapper;
    
    /**
     * 获取仪表板统计数据（含心理测评分布）
     */
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // 基础用户/帖子统计
        stats.setTotalUsers(userMapper.selectCount(null));
        stats.setTotalPosts(forumPostMapper.selectCount(null));
        stats.setTotalComments(commentMapper.selectCount(null));
        
        QueryWrapper<User> activeUserWrapper = new QueryWrapper<>();
        activeUserWrapper.eq("status", 1);
        stats.setActiveUsers(userMapper.selectCount(activeUserWrapper));
        
        QueryWrapper<ForumPost> pendingPostWrapper = new QueryWrapper<>();
        pendingPostWrapper.eq("status", 0);
        stats.setPendingPosts(forumPostMapper.selectCount(pendingPostWrapper));
        
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd   = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        QueryWrapper<User> todayUserWrapper = new QueryWrapper<>();
        todayUserWrapper.between("created_at", todayStart, todayEnd);
        stats.setTodayNewUsers(userMapper.selectCount(todayUserWrapper));
        
        QueryWrapper<ForumPost> todayPostWrapper = new QueryWrapper<>();
        todayPostWrapper.between("created_at", todayStart, todayEnd);
        stats.setTodayNewPosts(forumPostMapper.selectCount(todayPostWrapper));

        // ===== 心理测评统计 =====
        stats.setTotalAssessments(recordMapper.selectCount(null));

        LambdaQueryWrapper<UserAssessmentRecord> normalWrapper = new LambdaQueryWrapper<>();
        normalWrapper.eq(UserAssessmentRecord::getResultSummary, "正常");
        stats.setNormalCount(recordMapper.selectCount(normalWrapper));

        LambdaQueryWrapper<UserAssessmentRecord> mildWrapper = new LambdaQueryWrapper<>();
        mildWrapper.eq(UserAssessmentRecord::getResultSummary, "轻度抑郁");
        stats.setMildCount(recordMapper.selectCount(mildWrapper));

        LambdaQueryWrapper<UserAssessmentRecord> moderateWrapper = new LambdaQueryWrapper<>();
        moderateWrapper.eq(UserAssessmentRecord::getResultSummary, "中度抑郁");
        stats.setModerateCount(recordMapper.selectCount(moderateWrapper));

        LambdaQueryWrapper<UserAssessmentRecord> severeWrapper = new LambdaQueryWrapper<>();
        severeWrapper.eq(UserAssessmentRecord::getResultSummary, "重度抑郁");
        stats.setSevereCount(recordMapper.selectCount(severeWrapper));

        return stats;
    }

    /**
     * 获取高风险用户（测评结果为"重度抑郁"）的记录列表
     * @param page 页码
     * @param size 每页大小
     */
    public Page<UserAssessmentRecord> getHighRiskUsers(int page, int size) {
        Page<UserAssessmentRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserAssessmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAssessmentRecord::getResultSummary, "重度抑郁")
               .orderByDesc(UserAssessmentRecord::getCreatedAt);
        return recordMapper.selectPage(pageParam, wrapper);
    }
}
