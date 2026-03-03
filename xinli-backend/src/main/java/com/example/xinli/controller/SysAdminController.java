package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.Admin;
import com.example.xinli.service.SysAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/system/admins")
public class SysAdminController {

    @Autowired
    private SysAdminService sysAdminService;

    @GetMapping
    public Result<Page<Admin>> getAdminsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(sysAdminService.getAdminsPage(current, size, keyword));
    }

    @PostMapping
    public Result<Void> createAdmin(@RequestBody Admin admin) {
        try {
            sysAdminService.createAdmin(admin);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Void> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        try {
            admin.setId(id);
            sysAdminService.updateAdmin(admin);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        try {
            sysAdminService.deleteAdmin(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateAdminStatus(@PathVariable Long id, @RequestBody Admin admin) {
        try {
            sysAdminService.updateAdminStatus(id, admin.getStatus());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String newPassword = body.get("password");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            sysAdminService.resetPassword(id, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
