package com.example.xinli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinli.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话Mapper接口
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
