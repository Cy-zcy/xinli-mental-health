package com.example.xinli.service;

public interface UserScoreService {
    
    /**
     * 变更用户健康分
     * 
     * @param userId      用户ID
     * @param scoreChange 变更的分数（可以是正数或负数）
     * @param reason      变更原因描述
     * @param changeType  变更类型：ASSESSMENT, TOOL, RESOURCE, CHAT, FORUM
     */
    void changeScore(Long userId, Integer scoreChange, String reason, String changeType);

    /**
     * 判断某项加分/扣分行为今日是否已达上限
     * @param userId     用户ID
     * @param changeType 类型
     * @param limit      次数上限
     * @return true=已达上限, false=未达上限
     */
    boolean isDailyLimitReached(Long userId, String changeType, int limit);
    
    /**
     * 判断过去一段时间内是否存在特定类型的记录 (用于重复测评的冷却控制)
     * @param userId     用户ID
     * @param changeType 类型，可拼接细节，如 ASSESSMENT_12
     * @param days       天数
     * @return true=已存在, false=不存在
     */
    boolean hasRecentRecord(Long userId, String changeType, int days);
}
