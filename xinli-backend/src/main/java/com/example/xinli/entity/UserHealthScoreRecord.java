package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户健康分变动流水记录
 */
@Data
@TableName("user_health_score_records")
public class UserHealthScoreRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;          // 用户ID
    
    private Integer scoreChange;  // 分数变动(正负值)
    
    private Integer currentScore; // 变动后的当前总分
    
    private String reason;        // 变动原因描述
    
    private String changeType;    // 变动类型(ASSESSMENT/RESOURCE/TOOL/FORUM/CHAT)
    
    private LocalDateTime createdAt;
}
