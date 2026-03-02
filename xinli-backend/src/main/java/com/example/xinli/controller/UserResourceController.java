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

    /**
     * 2. 获取资源详情（同时增加浏览次数）
     * GET /api/resource/{id}
     */
    @GetMapping("/{id}")
    public Result<InterventionResource> getResourceDetail(@PathVariable Long id) {
        try {
            return Result.success(resourceService.getResourceDetail(id));
        }
        catch (Exception e) {
            return Result.error("获取资源详情失败: " + e.getMessage());
        }
    }
}
