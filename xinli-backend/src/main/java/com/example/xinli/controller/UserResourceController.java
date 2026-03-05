package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.ResourceDTO;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.InterventionResource;
import com.example.xinli.service.InterventionResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端 - 心理干预资源控制器
 * 路由前缀: /api/resource
 */
@RestController
@RequestMapping("/api/resource")
@CrossOrigin
public class UserResourceController {

    @Autowired
    private InterventionResourceService resourceService;

    /**
     * 1. 获取上架资源列表（分页，支持类型筛选）
     * GET /api/resource/list?page=1&size=10&type=article
     */
    @GetMapping("/list")
    public Result<Page<ResourceDTO>> getResourceList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {
        try {
            return Result.success(resourceService.getPublishedResources(page, size, type));
        }
        catch (Exception e) {
            return Result.error("获取资源列表失败: " + e.getMessage());
        }
    }

    @Autowired
    private UserScoreService userScoreService;

    /**
     * 2. 获取资源详情（同时增加浏览次数，触发健康分奖励）
     * GET /api/resource/{id}
     */
    @GetMapping("/{id}")
    public Result<InterventionResource> getResourceDetail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            InterventionResource resource = resourceService.getResourceDetail(id);
            
            // ==== 健康分埋点：资源学习加分 ====
            if (token != null && token.startsWith("Bearer ")) {
                Long userId = com.example.xinli.utils.AuthUtils.getUserIdFromToken(token.substring(7));
                if (userId != null) {
                    // 每日最多加3分（前3次有效）
                    if (!userScoreService.isDailyLimitReached(userId, "RESOURCE", 3)) {
                        userScoreService.changeScore(userId, 1, "学习心理干预资源奖励: " + resource.getTitle(), "RESOURCE");
                    }
                }
            }
            // ================================
            
            return Result.success(resource);
        }
        catch (Exception e) {
            return Result.error("获取资源详情失败: " + e.getMessage());
        }
    }
}
