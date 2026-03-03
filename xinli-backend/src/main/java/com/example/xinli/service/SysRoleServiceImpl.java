package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.entity.SysRole;
import com.example.xinli.entity.SysRolePermission;
import com.example.xinli.mapper.SysRoleMapper;
import com.example.xinli.mapper.SysRolePermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public Page<SysRole> getRolesPage(int current, int size, String keyword) {
        Page<SysRole> page = new Page<>(current, size);
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like("role_name", keyword).or().like("role_code", keyword);
        }
        wrapper.orderByDesc("created_at");
        return sysRoleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysRole> getAllRoles() {
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return sysRoleMapper.selectList(wrapper);
    }

    @Override
    public void createRole(SysRole role) {
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        
        // 校验 roleCode 唯一
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", role.getRoleCode());
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
        
        sysRoleMapper.insert(role);
    }

    @Override
    public void updateRole(SysRole role) {
        // 校验 roleCode 唯一，除了自己
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", role.getRoleCode())
                .ne("id", role.getId());
        if (sysRoleMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("角色编码已存在");
        }
        sysRoleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        if (id == 1L) {
            throw new RuntimeException("系统初始超级管理员角色不能删除");
        }
        sysRoleMapper.deleteById(id);
        
        // 级联删除关联的权限
        QueryWrapper<SysRolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", id);
        sysRolePermissionMapper.delete(wrapper);
    }

    @Override
    public void updateRoleStatus(Long id, Integer status) {
        if (id == 1L && status == 0) {
            throw new RuntimeException("系统初始超级管理员角色不能禁用");
        }
        SysRole role = new SysRole();
        role.setId(id);
        role.setStatus(status);
        sysRoleMapper.updateById(role);
    }

    @Override
    public List<String> getRolePermissions(Long roleId) {
        QueryWrapper<SysRolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        List<SysRolePermission> list = sysRolePermissionMapper.selectList(wrapper);
        return list.stream().map(SysRolePermission::getPermissionCode).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermissions(Long roleId, List<String> permissionCodes) {
        if (roleId == 1L) {
            throw new RuntimeException("系统初始超级管理员角色权限不可更改");
        }
        
        // 先删除原有权限
        QueryWrapper<SysRolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        sysRolePermissionMapper.delete(wrapper);
        
        // 重新插入新的权限
        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            for (String code : permissionCodes) {
                SysRolePermission permission = new SysRolePermission();
                permission.setRoleId(roleId);
                permission.setPermissionCode(code);
                sysRolePermissionMapper.insert(permission);
            }
        }
    }
}
