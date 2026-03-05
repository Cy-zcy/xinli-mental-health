package com.example.xinli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xinli.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户系统通知 Mapper
 */
@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {
}
