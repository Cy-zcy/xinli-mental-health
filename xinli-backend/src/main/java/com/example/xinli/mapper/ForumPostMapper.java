package com.example.xinli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.ForumPostDTO;
import com.example.xinli.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    /**
     * 分页查询帖子列表
     */
    IPage<ForumPostDTO> selectPostPage(Page<ForumPostDTO> page,
                                       @Param("keyword") String keyword,
                                       @Param("category") String category,
                                       @Param("status") Integer status);

    /**
     * 查询帖子详情
     */
    ForumPostDTO selectPostDetail(@Param("postId") Long postId);

    /**
     * 统计用户获赞数量（所有帖子的点赞总数）
     */
    @org.apache.ibatis.annotations.Select("SELECT IFNULL(SUM(like_count), 0) FROM forum_posts WHERE user_id = #{userId}")
    Long getTotalLikesByUserId(@Param("userId") Long userId);
}
