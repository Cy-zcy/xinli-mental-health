package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户工具使用记录实体
 * 记录呼吸练习、冥想等工具的使用情况
 */
@Data
@TableName("tool_records")
public class ToolRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /**
     * 工具类型：
     * breathing - 呼吸练习
     * meditation - 冥想引导
     */
    private String toolType;

    /** 使用时长（秒） */
    private Integer durationSeconds;

    /** 完成的循环/周期数（呼吸练习用） */
    private Integer cycles;

    /** 完成状态：1-完整完成，0-中途退出 */
    private Integer completed;

    /** 使用的具体模式（如：4-7-8呼吸法、正念冥想等） */
    private String pattern;

    private LocalDateTime createdAt;
}
