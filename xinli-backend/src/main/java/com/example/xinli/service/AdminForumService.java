package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.ForumPostDTO;
import com.example.xinli.dto.PageResult;
import com.example.xinli.dto.UpdatePostStatusRequest;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.mapper.ForumPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminForumService {
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    /**
     * 分页获取帖子列表
     */
    public PageResult<ForumPostDTO> getPostList(Integer page, Integer size, String keyword, String category, Integer status) {
        Page<ForumPostDTO> pageParam = new Page<>(page, size);
        IPage<ForumPostDTO> result = forumPostMapper.selectPostPage(pageParam, keyword, category, status);
        
        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), result.getRecords());
    }
    
    /**
     * 获取帖子详情
     */
    public ForumPostDTO getPostDetail(Long postId) {
        return forumPostMapper.selectPostDetail(postId);
    }
    
    /**
     * 更新帖子状态
     */
    public boolean updatePostStatus(Long postId, UpdatePostStatusRequest request) {
        ForumPost post = new ForumPost();
        post.setId(postId);
        post.setStatus(request.getStatus());
        
        return forumPostMapper.updateById(post) > 0;
    }
    
    /**
     * 删除帖子
     */
    public boolean deletePost(Long postId) {
        return forumPostMapper.deleteById(postId) > 0;
    }
    
    /**
     * 批量删除帖子
     */
    public boolean batchDeletePosts(List<Long> postIds) {
        return forumPostMapper.deleteBatchIds(postIds) > 0;
    }
    
    /**
     * 获取帖子统计信息
     */
    public Long getTotalPostCount() {
        return forumPostMapper.selectCount(new QueryWrapper<>());
    }
    
    public Long getActivePostCount() {
        QueryWrapper<ForumPost> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return forumPostMapper.selectCount(wrapper);
    }
    
    public Long getPostCountByCategory(String category) {
        QueryWrapper<ForumPost> wrapper = new QueryWrapper<>();
        wrapper.eq("category", category).eq("status", 1);
        return forumPostMapper.selectCount(wrapper);
    }
}
