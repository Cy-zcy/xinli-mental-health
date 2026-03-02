package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.InterventionResource;
import com.example.xinli.service.InterventionResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 心理干预资源管理控制器
 * 路由前缀: /api/admin/resource
 */
@RestController
@RequestMapping("/api/admin/resource")
@CrossOrigin
public class AdminResourceController {

    @Autowired
    private InterventionResourceService resourceService;

    /**
     * 1. 获取所有资源列表（含下架，分页，支持类型筛选）
     * GET /api/admin/resource/list?page=1&size=10&type=audio
     */
    @GetMapping("/list")
    public Result<Page<InterventionResource>> getAllResources(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {
        try {
            return Result.success(resourceService.getAllResources(page, size, type));
        }
        catch (Exception e) {
            return Result.error("获取资源列表失败: " + e.getMessage());
        }
    }

    /**
     * 2. 新增资源
     * POST /api/admin/resource
     * Body: InterventionResource JSON
     */
    @PostMapping
    public Result<InterventionResource> createResource(@RequestBody InterventionResource resource) {
        try {
            return Result.success(resourceService.createResource(resource), "资源发布成功");
        }
        catch (Exception e) {
            return Result.error("发布资源失败: " + e.getMessage());
        }
    }

    /**
     * 3. 更新资源信息
     * PUT /api/admin/resource/{id}
     */
    @PutMapping("/{id}")
    public Result<InterventionResource> updateResource(
            @PathVariable Long id,
            @RequestBody InterventionResource resource) {
        try {
            return Result.success(resourceService.updateResource(id, resource), "资源更新成功");
        }
        catch (Exception e) {
            return Result.error("更新资源失败: " + e.getMessage());
        }
    }

    /**
     * 4. 更新资源状态（上架/下架）
     * PUT /api/admin/resource/{id}/status?status=0
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            resourceService.updateResourceStatus(id, status);
            return Result.success(null, status == 1 ? "资源已上架" : "资源已下架");
        }
        catch (Exception e) {
            return Result.error("更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 5. 删除资源
     * DELETE /api/admin/resource/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(@PathVariable Long id) {
        try {
            resourceService.deleteResource(id);
            return Result.success(null, "资源已删除");
        }
        catch (Exception e) {
            return Result.error("删除资源失败: " + e.getMessage());
        }
    }
}
