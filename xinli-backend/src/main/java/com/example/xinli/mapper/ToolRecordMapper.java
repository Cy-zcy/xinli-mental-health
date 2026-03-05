package com.example.xinli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinli.entity.ToolRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 工具使用记录 Mapper
 */
@Mapper
public interface ToolRecordMapper extends BaseMapper<ToolRecord> {

    /**
     * 统计某用户的工具使用总时长（分钟）
     */
    @Select("SELECT COALESCE(SUM(duration_seconds) / 60, 0) FROM tool_records WHERE user_id = #{userId} AND completed = 1")
    Integer sumTotalMinutes(@Param("userId") Long userId);

    /**
     * 统计某用户某类型工具的完成次数
     */
    @Select("SELECT COUNT(*) FROM tool_records WHERE user_id = #{userId} AND tool_type = #{toolType} AND completed = 1")
    Integer countByType(@Param("userId") Long userId, @Param("toolType") String toolType);

    /**
     * 统计用户的活跃天数（有记录的不同天数）
     */
    @Select("SELECT COUNT(DISTINCT DATE(created_at)) FROM tool_records WHERE user_id = #{userId}")
    Integer countActiveDays(@Param("userId") Long userId);
}
