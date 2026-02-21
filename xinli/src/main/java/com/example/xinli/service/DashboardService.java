package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.dto.DashboardStatsDTO;
import com.example.xinli.entity.Comment;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.CommentMapper;
import com.example.xinli.mapper.ForumPostMapper;
import com.example.xinli.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DashboardService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    /**
     * 获取仪表板统计数据
     */
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // 用户总数
        stats.setTotalUsers(userMapper.selectCount(null));
        
        // 帖子总数
        stats.setTotalPosts(forumPostMapper.selectCount(null));
        
        // 评论总数
        stats.setTotalComments(commentMapper.selectCount(null));
        
        // 活跃用户数（状态为1的用户）
        QueryWrapper<User> activeUserWrapper = new QueryWrapper<>();
        activeUserWrapper.eq("status", 1);
        stats.setActiveUsers(userMapper.selectCount(activeUserWrapper));
        
        // 待审核帖子数（状态为0的帖子）
        QueryWrapper<ForumPost> pendingPostWrapper = new QueryWrapper<>();
        pendingPostWrapper.eq("status", 0);
        stats.setPendingPosts(forumPostMapper.selectCount(pendingPostWrapper));
        
        // 今日新增用户
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        QueryWrapper<User> todayUserWrapper = new QueryWrapper<>();
        todayUserWrapper.between("created_at", todayStart, todayEnd);
        stats.setTodayNewUsers(userMapper.selectCount(todayUserWrapper));
        
        // 今日新增帖子
        QueryWrapper<ForumPost> todayPostWrapper = new QueryWrapper<>();
        todayPostWrapper.between("created_at", todayStart, todayEnd);
        stats.setTodayNewPosts(forumPostMapper.selectCount(todayPostWrapper));
        
        return stats;
    }
}
