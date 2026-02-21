package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.PostDetailDTO;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts")
@CrossOrigin
public class ForumPostController {
    
    @Autowired
    private ForumPostService forumPostService;
    
    /**
     * 分页查询帖子列表
     */
    @GetMapping
    public Result<Page<ForumPost>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        try {
            Page<ForumPost> posts = forumPostService.getPosts(page, size, keyword, category, status);
            return Result.success(posts);
        } catch (Exception e) {
            return Result.error("获取帖子列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取帖子详情（包含评论）
     */
    @GetMapping("/{id}")
    public Result<PostDetailDTO> getPostDetail(@PathVariable Long id) {
        try {
            PostDetailDTO postDetail = forumPostService.getPostDetail(id);
            if (postDetail != null) {
                return Result.success(postDetail);
            } else {
                return Result.error("帖子不存在");
            }
        } catch (Exception e) {
            return Result.error("获取帖子信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新帖子状态（审核）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updatePostStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = forumPostService.updatePostStatus(id, status);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新帖子状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新帖子状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新帖子信息
     */
    @PutMapping("/{id}")
    public Result<Void> updatePost(@PathVariable Long id, @RequestBody ForumPost post) {
        try {
            post.setId(id);
            boolean success = forumPostService.updatePost(post);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新帖子失败");
            }
        } catch (Exception e) {
            return Result.error("更新帖子失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        try {
            boolean success = forumPostService.deletePost(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除帖子失败");
            }
        } catch (Exception e) {
            return Result.error("删除帖子失败: " + e.getMessage());
        }
    }
}
