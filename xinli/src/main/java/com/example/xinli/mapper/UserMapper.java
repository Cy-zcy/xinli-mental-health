package com.example.xinli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.UserDTO;
import com.example.xinli.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT u.*, " +
            "(SELECT COUNT(*) FROM forum_posts fp WHERE fp.user_id = u.id AND fp.status = 1) as postCount, " +
            "(SELECT COUNT(*) FROM post_likes pl WHERE pl.user_id = u.id) as likeCount " +
            "FROM users u " +
            "WHERE (#{keyword} IS NULL OR u.phone LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR u.status = #{status}) " +
            "ORDER BY u.created_at DESC")
    IPage<UserDTO> selectUserPage(Page<UserDTO> page, @Param("keyword") String keyword, @Param("status") Integer status);
    
    @Select("SELECT u.*, " +
            "(SELECT COUNT(*) FROM forum_posts fp WHERE fp.user_id = u.id AND fp.status = 1) as postCount, " +
            "(SELECT COUNT(*) FROM post_likes pl WHERE pl.user_id = u.id) as likeCount " +
            "FROM users u WHERE u.id = #{userId}")
    UserDTO selectUserDetail(@Param("userId") Long userId);
}
