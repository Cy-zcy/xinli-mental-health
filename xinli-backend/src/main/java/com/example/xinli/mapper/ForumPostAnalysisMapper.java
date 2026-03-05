package com.example.xinli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinli.entity.ForumPostAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛帖子情感分析结果 Mapper
 */
@Mapper
public interface ForumPostAnalysisMapper extends BaseMapper<ForumPostAnalysis> {
}
