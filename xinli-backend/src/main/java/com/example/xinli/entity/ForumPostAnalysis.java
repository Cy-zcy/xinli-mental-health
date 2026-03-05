package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛帖子 AI 情感分析结果实体
 */
@Data
@TableName("forum_post_analysis")
public class ForumPostAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 发帖用户ID */
    private Long userId;

    /**
     * 主情绪标签
     * 枚举值：焦虑 / 抑郁 / 愤怒 / 孤独 / 平静 / 积极 / 其他
     */
    private String emotionLabel;

    /**
     * 细分情绪标签（英文逗号分隔）
     * 示例：#学业压力,#失眠,#人际关系
     */
    private String emotionTags;

    /**
     * 风险等级
     * 0 = 正常
     * 1 = 轻度关注
     * 2 = 高危（包含自伤/自杀/极端情绪）
     */
    private Integer riskLevel;

    /** 高危原因说明，riskLevel >= 2 时有值 */
    private String riskReason;

    /** AI 对帖子的简短摘要（一两句话） */
    private String aiSummary;

    /** 是否已对用户发送危机通知：0 否 / 1 是 */
    private Integer isAlerted;

    private LocalDateTime createdAt;
}
