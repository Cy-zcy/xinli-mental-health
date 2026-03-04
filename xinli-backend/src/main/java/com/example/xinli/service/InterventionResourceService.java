package com.example.xinli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.ResourceDTO;
import com.example.xinli.entity.InterventionResource;

public interface InterventionResourceService {

    /**
     * 获取上架资源列表（分页，可按类型筛选）
     * @param page 页码
     * @param size 每页数量
     * @param type 类型过滤（null 表示不过滤）
     */
    Page<ResourceDTO> getPublishedResources(int page, int size, String type);

    /**
     * 获取资源详情（同时增加浏览次数）
     */
    InterventionResource getResourceDetail(Long id);

    // ============ Admin 管理端 ============

    /**
     * 获取所有资源（含下架），Admin 分页
     */
    Page<InterventionResource> getAllResources(int page, int size, String type);

    /**
     * 获取单条资源，不限上下架状态（用于 Admin 编辑回显）
     */
    InterventionResource getResourceById(Long id);

    /**
     * 新增资源
     */
    InterventionResource createResource(InterventionResource resource);

    /**
     * 更新资源信息
     */
    InterventionResource updateResource(Long id, InterventionResource resource);

    /**
     * 更新资源状态（上架/下架）
     */
    void updateResourceStatus(Long id, Integer status);

    /**
     * 删除资源
     */
    void deleteResource(Long id);
}
