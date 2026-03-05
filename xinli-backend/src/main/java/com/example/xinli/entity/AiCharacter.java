package com.example.xinli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 多维度角色设定实体类
 */
@Data
@TableName("ai_character")
public class AiCharacter {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String avatar;
    private String greeting;
    
    // 多维度人设
    private String background;
    private String personality;
    private String rules;
    
    private Integer isActive;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
