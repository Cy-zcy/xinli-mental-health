package com.example.xinli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.entity.SysRole;

import java.util.List;

public interface SysRoleService {
    
    Page<SysRole> getRolesPage(int current, int size, String keyword);
    
    List<SysRole> getAllRoles();
    
    void createRole(SysRole role);
    
    void updateRole(SysRole role);
    
    void deleteRole(Long id);
    
    void updateRoleStatus(Long id, Integer status);
    
    List<String> getRolePermissions(Long roleId);
    
    void saveRolePermissions(Long roleId, List<String> permissionCodes);
}
