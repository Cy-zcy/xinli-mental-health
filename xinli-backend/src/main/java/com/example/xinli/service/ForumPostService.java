package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.PostDetailDTO;
import com.example.xinli.entity.Comment;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.CommentMapper;
import com.example.xinli.mapper.ForumPostMapper;
import com.example.xinli.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumPostService {
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    /**
     * 分页查询帖子列表
     */
    public Page<ForumPost> getPosts(int page, int size, String keyword, String category, Integer status) {
        Page<ForumPost> postPage = new Page<>(page, size);
        QueryWrapper<ForumPost> wrapper = new QueryWrapper<>();
        
        // 关键词搜索（标题或内容）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }
        
        // 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq("category", category);
        }
        
        // 状态筛选
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc("created_at");
        
        return forumPostMapper.selectPage(postPage, wrapper);
    }
    
    /**
     * 获取帖子详情（包含作者信息和评论列表）
     */
    public PostDetailDTO getPostDetail(Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            return null;
        }
        
        PostDetailDTO postDetail = new PostDetailDTO();
        BeanUtils.copyProperties(post, postDetail);
        
        // 获取作者信息
        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            postDetail.setAuthorNickname(author.getNickname());
            postDetail.setAuthorAvatar(author.getAvatar());
        }
        
        // 获取评论列表
        QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
        commentWrapper.eq("post_id", id);
        commentWrapper.eq("status", 1); // 只显示正常状态的评论
        commentWrapper.orderByAsc("created_at");
        
        List<Comment> comments = commentMapper.selectList(commentWrapper);
        List<PostDetailDTO.CommentDTO> commentDTOs = comments.stream().map(comment -> {
            PostDetailDTO.CommentDTO commentDTO = new PostDetailDTO.CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);
            
            // 获取评论者信息
            User commentUser = userMapper.selectById(comment.getUserId());
            if (commentUser != null) {
                commentDTO.setUserNickname(commentUser.getNickname());
                commentDTO.setUserAvatar(commentUser.getAvatar());
            }
            
            return commentDTO;
        }).collect(Collectors.toList());
        
        postDetail.setComments(commentDTOs);
        
        return postDetail;
    }
    
    /**
     * 更新帖子状态（审核通过/拒绝）
     */
    public boolean updatePostStatus(Long id, Integer status) {
        ForumPost post = new ForumPost();
        post.setId(id);
        post.setStatus(status);
        return forumPostMapper.updateById(post) > 0;
    }
    
    public ForumPost getPostById(Long id) {
        return forumPostMapper.selectById(id);
    }
    
    public boolean updatePost(ForumPost post) {
        return forumPostMapper.updateById(post) > 0;
    }
    
    public boolean deletePost(Long id) {
        return forumPostMapper.deleteById(id) > 0;
    }
}
