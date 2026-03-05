package com.example.xinli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.xinli.entity.User;
import com.example.xinli.entity.UserHealthScoreRecord;
import com.example.xinli.mapper.UserHealthScoreRecordMapper;
import com.example.xinli.mapper.UserMapper;
import com.example.xinli.service.UserScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserScoreServiceImpl implements UserScoreService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserHealthScoreRecordMapper recordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeScore(Long userId, Integer scoreChange, String reason, String changeType) {
        if (scoreChange == null || scoreChange == 0) {
            return;
        }

        // 查找用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }

        // 初始化兜底分数为 100（如果旧数据为空的话）
        int currentScore = user.getHealthScore() != null ? user.getHealthScore() : 100;
        int newScore = currentScore + scoreChange;
        
        // 限制分数范围
        if (newScore > 100) newScore = 100;
        if (newScore < 0) newScore = 0; // 最低兜底到0分

        // 如果分数发生实质变化才记录流水
        if (newScore != currentScore) {
            User updateObj = new User();
            updateObj.setId(userId);
            updateObj.setHealthScore(newScore);
            userMapper.updateById(updateObj);

            // 写入流水日志
            UserHealthScoreRecord record = new UserHealthScoreRecord();
            record.setUserId(userId);
            record.setScoreChange(newScore - currentScore); // 记录实际变动的分数
            record.setCurrentScore(newScore);
            record.setReason(reason);
            record.setChangeType(changeType);
            recordMapper.insert(record);
        }
    }

    @Override
    public boolean isDailyLimitReached(Long userId, String changeType, int limit) {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LambdaQueryWrapper<UserHealthScoreRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserHealthScoreRecord::getUserId, userId)
               .eq(UserHealthScoreRecord::getChangeType, changeType)
               .ge(UserHealthScoreRecord::getCreatedAt, todayStart);
        long count = recordMapper.selectCount(wrapper);
        return count >= limit;
    }

    @Override
    public boolean hasRecentRecord(Long userId, String changeType, int days) {
        LocalDateTime timeLimit = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<UserHealthScoreRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserHealthScoreRecord::getUserId, userId)
               .eq(UserHealthScoreRecord::getChangeType, changeType)
               .ge(UserHealthScoreRecord::getCreatedAt, timeLimit);
        return recordMapper.exists(wrapper);
    }
}
