package com.example.xinli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinli.entity.UserChatMemory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 AI 长期记忆 Mapper 接口
 */
@Mapper
public interface UserChatMemoryMapper extends BaseMapper<UserChatMemory> {
}
