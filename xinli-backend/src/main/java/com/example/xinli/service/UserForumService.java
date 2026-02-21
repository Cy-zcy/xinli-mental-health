package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.CreatePostRequest;
import com.example.xinli.dto.ForumPostDTO;
import com.example.xinli.dto.PostDetailDTO;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.entity.PostLike;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.ForumPostMapper;
import com.example.xinli.mapper.PostLikeMapper;
import com.example.xinli.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户端论坛服务类
 * 处理用户端论坛相关的业务逻辑
 */
@Service
public class UserForumService {
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    @Autowired
    private PostLikeMapper postLikeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 获取用户端帖子列表
     * 只返回状态为正常(status=1)的帖子
     */
    public Page<ForumPostDTO> getUserPosts(int page, int size, String category, String keyword, String sortBy, String sortOrder) {
        Page<ForumPost> pageInfo = new Page<>(page, size);
        QueryWrapper<ForumPost> wrapper = new QueryWrapper<>();
        
        // 只查询正常状态的帖子
        wrapper.eq("status", 1);
        
        // 分类筛选
        if (category != null && !category.isEmpty() && !"all".equals(category)) {
            wrapper.eq("category", category);
        }
        
        // 关键词搜索（标题和内容）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("title", keyword.trim()).or().like("content", keyword.trim()));
        }
        
        // 排序
        if ("like_count".equals(sortBy)) {
            wrapper.orderBy(true, "desc".equals(sortOrder), "like_count");
        } else if ("view_count".equals(sortBy)) {
            wrapper.orderBy(true, "desc".equals(sortOrder), "view_count");
        } else {
            // 默认按创建时间排序
            wrapper.orderBy(true, "desc".equals(sortOrder), "created_at");
        }
        
        Page<ForumPost> postPage = forumPostMapper.selectPage(pageInfo, wrapper);
        
        // 转换为DTO
        Page<ForumPostDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(postPage, dtoPage, "records");
        
        List<ForumPostDTO> dtoList = new ArrayList<>();
        for (ForumPost post : postPage.getRecords()) {
            ForumPostDTO dto = convertToForumPostDTO(post);
            dtoList.add(dto);
        }
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 用户发布帖子
     */
    @Transactional
    public ForumPost createPost(Long userId, CreatePostRequest request) {
        // 验证用户是否存在且状态正常
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new RuntimeException("用户不存在或已被禁用");
        }
        
        // 创建帖子
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setLikeCount(0);
        post.setViewCount(0);
        post.setStatus(1); // 默认为正常状态，如需审核可设为0
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        
        int result = forumPostMapper.insert(post);
        if (result > 0) {
            return post;
        } else {
            throw new RuntimeException("发布帖子失败");
        }
    }
    
    /**
     * 获取帖子详情（用户端）
     * 包含作者信息和当前用户的点赞状态
     */
    public PostDetailDTO getPostDetailForUser(Long postId, Long currentUserId) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            return null;
        }
        
        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        forumPostMapper.updateById(post);
        
        // 获取作者信息
        User author = userMapper.selectById(post.getUserId());
        
        // 检查当前用户是否已点赞
        boolean isLiked = false;
        if (currentUserId != null) {
            QueryWrapper<PostLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.eq("user_id", currentUserId).eq("post_id", postId);
            isLiked = postLikeMapper.selectCount(likeWrapper) > 0;
        }
        
        // 构建详情DTO
        PostDetailDTO dto = new PostDetailDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCategory(post.getCategory());
        dto.setLikeCount(post.getLikeCount());
        dto.setViewCount(post.getViewCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setLiked(isLiked);
        
        // 设置作者信息
        if (author != null) {
            PostDetailDTO.UserInfo userInfo = new PostDetailDTO.UserInfo();
            userInfo.setId(author.getId());
            userInfo.setNickname(author.getNickname());
            userInfo.setAvatar(author.getAvatar());
            dto.setAuthor(userInfo);
        }
        
        return dto;
    }
    
    /**
     * 点赞帖子
     */
    @Transactional
    public Map<String, Object> likePost(Long userId, Long postId) {
        // 检查帖子是否存在
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new RuntimeException("帖子不存在或已被删除");
        }
        
        // 检查是否已经点赞
        QueryWrapper<PostLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("post_id", postId);
        if (postLikeMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("您已经点赞过这个帖子");
        }
        
        // 添加点赞记录
        PostLike like = new PostLike();
        like.setUserId(userId);
        like.setPostId(postId);
        like.setCreatedAt(LocalDateTime.now());
        postLikeMapper.insert(like);
        
        // 更新帖子点赞数
        post.setLikeCount(post.getLikeCount() + 1);
        forumPostMapper.updateById(post);
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", true);
        result.put("likeCount", post.getLikeCount());
        result.put("message", "点赞成功");
        
        return result;
    }
    
    /**
     * 取消点赞
     */
    @Transactional
    public Map<String, Object> unlikePost(Long userId, Long postId) {
        // 检查帖子是否存在
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null || post.getStatus() != 1) {
            throw new RuntimeException("帖子不存在或已被删除");
        }
        
        // 检查是否已经点赞
        QueryWrapper<PostLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("post_id", postId);
        PostLike existingLike = postLikeMapper.selectOne(wrapper);
        if (existingLike == null) {
            throw new RuntimeException("您还没有点赞这个帖子");
        }
        
        // 删除点赞记录
        postLikeMapper.deleteById(existingLike.getId());
        
        // 更新帖子点赞数
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        forumPostMapper.updateById(post);
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", false);
        result.put("likeCount", post.getLikeCount());
        result.put("message", "取消点赞成功");
        
        return result;
    }
    
    /**
     * 获取论坛分类列表
     */
    public List<Map<String, Object>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        
        Map<String, Object> emotion = new HashMap<>();
        emotion.put("value", "emotion");
        emotion.put("label", "情感倾诉");
        emotion.put("description", "分享情感困扰，寻求理解和支持");
        categories.add(emotion);
        
        Map<String, Object> experience = new HashMap<>();
        experience.put("value", "experience");
        experience.put("label", "经验分享");
        experience.put("description", "分享心理健康相关的经验和心得");
        categories.add(experience);
        
        return categories;
    }
    
    /**
     * 转换ForumPost为ForumPostDTO
     */
    private ForumPostDTO convertToForumPostDTO(ForumPost post) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent().length() > 200 ? 
                      post.getContent().substring(0, 200) + "..." : post.getContent());
        dto.setCategory(post.getCategory());
        dto.setLikeCount(post.getLikeCount());
        dto.setViewCount(post.getViewCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        
        // 获取作者信息
        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            ForumPostDTO.UserInfo userInfo = new ForumPostDTO.UserInfo();
            userInfo.setId(author.getId());
            userInfo.setNickname(author.getNickname());
            userInfo.setAvatar(author.getAvatar());
            dto.setAuthor(userInfo);
        }
        
        return dto;
    }
}
