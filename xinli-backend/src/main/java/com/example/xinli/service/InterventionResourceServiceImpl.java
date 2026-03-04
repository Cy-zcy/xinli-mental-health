package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.ResourceDTO;
import com.example.xinli.entity.InterventionResource;
import com.example.xinli.mapper.InterventionResourceMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InterventionResourceServiceImpl implements InterventionResourceService {

    @Autowired
    private InterventionResourceMapper resourceMapper;

    @Override
    public Page<ResourceDTO> getPublishedResources(int page, int size, String type) {
        // 先查实体分页
        Page<InterventionResource> entityPage = new Page<>(page, size);
        LambdaQueryWrapper<InterventionResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterventionResource::getStatus, 1);
        if (StringUtils.hasText(type)) {
            wrapper.eq(InterventionResource::getType, type);
        }
        wrapper.orderByDesc(InterventionResource::getCreatedAt);
        resourceMapper.selectPage(entityPage, wrapper);

        // 转换为 DTO（不含 content 字段，节省流量）
        Page<ResourceDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream().map(entity -> {
            ResourceDTO dto = new ResourceDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).toList());
        return dtoPage;
    }

    @Override
    public InterventionResource getResourceDetail(Long id) {
        InterventionResource resource = resourceMapper.selectById(id);
        if (resource == null || resource.getStatus() != 1) {
            throw new RuntimeException("资源不存在或已下架");
        }
        // 浏览次数 +1
        LambdaUpdateWrapper<InterventionResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(InterventionResource::getId, id)
                     .setSql("view_count = view_count + 1");
        resourceMapper.update(null, updateWrapper);
        resource.setViewCount(resource.getViewCount() + 1);
        return resource;
    }

    @Override
    public Page<InterventionResource> getAllResources(int page, int size, String type) {
        Page<InterventionResource> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<InterventionResource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(InterventionResource::getType, type);
        }
        wrapper.orderByDesc(InterventionResource::getCreatedAt);
        return resourceMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public InterventionResource getResourceById(Long id) {
        return assertResourceExists(id);
    }

    @Override
    public InterventionResource createResource(InterventionResource resource) {
        resource.setStatus(1);
        resource.setViewCount(0);
        resourceMapper.insert(resource);
        return resource;
    }

    @Override
    public InterventionResource updateResource(Long id, InterventionResource resource) {
        assertResourceExists(id);
        resource.setId(id);
        resourceMapper.updateById(resource);
        return resourceMapper.selectById(id);
    }

    @Override
    public void updateResourceStatus(Long id, Integer status) {
        InterventionResource resource = assertResourceExists(id);
        resource.setStatus(status);
        resourceMapper.updateById(resource);
    }

    @Override
    public void deleteResource(Long id) {
        assertResourceExists(id);
        resourceMapper.deleteById(id);
    }

    private InterventionResource assertResourceExists(Long id) {
        InterventionResource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new RuntimeException("资源不存在，ID: " + id);
        }
        return resource;
    }
}
