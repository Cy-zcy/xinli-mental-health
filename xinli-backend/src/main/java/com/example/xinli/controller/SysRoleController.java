package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.SysRole;
import com.example.xinli.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/system/roles")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping
    public Result<Page<SysRole>> getRolesPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(sysRoleService.getRolesPage(current, size, keyword));
    }

    @GetMapping("/all")
    public Result<List<SysRole>> getAllRoles() {
        return Result.success(sysRoleService.getAllRoles());
    }

    @PostMapping
    public Result<Void> createRole(@RequestBody SysRole role) {
        try {
            sysRoleService.createRole(role);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        try {
            role.setId(id);
            sysRoleService.updateRole(role);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        try {
            sysRoleService.deleteRole(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateRoleStatus(@PathVariable Long id, @RequestBody SysRole role) {
        try {
            sysRoleService.updateRoleStatus(id, role.getStatus());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/permissions")
    public Result<List<String>> getRolePermissions(@PathVariable Long id) {
        return Result.success(sysRoleService.getRolePermissions(id));
    }

    @PostMapping("/{id}/permissions")
    public Result<Void> saveRolePermissions(@PathVariable Long id, @RequestBody List<String> permissionCodes) {
        try {
            sysRoleService.saveRolePermissions(id, permissionCodes);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
