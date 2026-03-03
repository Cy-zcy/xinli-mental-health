package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.dto.ForumPostDTO;
import com.example.xinli.dto.PostDetailDTO;
import com.example.xinli.dto.CreatePostRequest;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.service.UserForumService;
import com.example.xinli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户端论坛控制器
 * 处理用户端论坛相关的所有接口请求
 */
@RestController
@RequestMapping("/api/forum")
@CrossOrigin
public class UserForumController {
    
    @Autowired
    private UserForumService userForumService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 1. 用户端获取帖子列表
     * GET /api/forum/posts
     * 支持分页、分类筛选、关键词搜索
     */
    @GetMapping("/posts")
    public Result<Page<ForumPostDTO>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        try {
            Page<ForumPostDTO> posts = userForumService.getUserPosts(page, size, category, keyword, sortBy, sortOrder);
            return Result.success(posts);
        } catch (Exception e) {
            return Result.error("获取帖子列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 2. 用户发布帖子
     * POST /api/forum/posts
     * 需要用户认证
     */
    @PostMapping("/posts")
    public Result<ForumPost> createPost(@Valid @RequestBody CreatePostRequest request, HttpServletRequest httpRequest) {
        try {
            // 从JWT token获取用户ID
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            ForumPost post = userForumService.createPost(userId, request);
            return Result.success(post);
        } catch (Exception e) {
            return Result.error("发布帖子失败: " + e.getMessage());
        }
    }
    
    /**
     * 3. 用户端获取帖子详情
     * GET /api/forum/posts/{postId}
     * 包含帖子内容、作者信息、点赞状态等
     */
    @GetMapping("/posts/{postId}")
    public Result<PostDetailDTO> getPostDetail(@PathVariable Long postId, HttpServletRequest httpRequest) {
        try {
            // 获取当前用户ID（可能为null，表示未登录用户）
            Long currentUserId = getUserIdFromToken(httpRequest);
            
            PostDetailDTO postDetail = userForumService.getPostDetailForUser(postId, currentUserId);
            if (postDetail != null) {
                return Result.success(postDetail);
            } else {
                return Result.error(404, "帖子不存在或已被删除");
            }
        } catch (Exception e) {
            return Result.error("获取帖子详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 4. 点赞帖子
     * POST /api/forum/posts/{postId}/like
     * 需要用户认证
     */
    @PostMapping("/posts/{postId}/like")
    public Result<Map<String, Object>> likePost(@PathVariable Long postId, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            Map<String, Object> result = userForumService.likePost(userId, postId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("点赞操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 5. 取消点赞
     * DELETE /api/forum/posts/{postId}/like
     * 需要用户认证
     */
    @DeleteMapping("/posts/{postId}/like")
    public Result<Map<String, Object>> unlikePost(@PathVariable Long postId, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            
            Map<String, Object> result = userForumService.unlikePost(userId, postId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("取消点赞失败: " + e.getMessage());
        }
    }
    
    /**
     * 6. 用户发表评论
     * POST /api/forum/posts/{postId}/comments
     * 需要用户认证
     */
    @PostMapping("/posts/{postId}/comments")
    public Result<PostDetailDTO.CommentDTO> createComment(
            @PathVariable Long postId,
            @RequestBody java.util.Map<String, String> body,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }
            String content = body.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }
            PostDetailDTO.CommentDTO comment = userForumService.createComment(userId, postId, content.trim());
            return Result.success(comment);
        } catch (Exception e) {
            return Result.error("发表评论失败: " + e.getMessage());
        }
    }

    /**
     * 7. 获取论坛分类列表
     * GET /api/forum/categories
     * 公开接口，无需认证
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategories() {
        try {
            List<Map<String, Object>> categories = userForumService.getCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("获取分类列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 从HTTP请求中提取用户ID
     * @param request HTTP请求对象
     * @return 用户ID，如果未登录或token无效则返回null
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // Token解析失败，返回null
        }
        return null;
    }
}
