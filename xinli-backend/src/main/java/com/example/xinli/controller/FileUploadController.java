package com.example.xinli.controller;

import com.example.xinli.dto.Result;
import com.example.xinli.service.FileUploadService;
import com.example.xinli.service.UserService;
import com.example.xinli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileUploadController {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 通用文件上传（自动识别图片/音视频，用于封面、音视频资源等）
     */
    @PostMapping("/common/upload")
    public Result<String> uploadCommonFile(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType() != null ? file.getContentType() : "";
            String relativePath;
            if (contentType.startsWith("audio/") || contentType.startsWith("video/")) {
                // 音视频走专用上传逻辑（200MB限制，保存到 media/ 目录）
                relativePath = fileUploadService.uploadMedia(file);
            } else {
                // 图片等文件走原有逻辑
                relativePath = fileUploadService.uploadAvatar(file, 0L);
            }
            String url = fileUploadService.getFileUrl(relativePath);
            return Result.success(url, "上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * WangEditor 富文本编辑器图片上传
     */
    @PostMapping("/common/upload/wangeditor")
    public Map<String, Object> uploadWangEditor(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String relativePath = fileUploadService.uploadAvatar(file, 0L);
            String url = fileUploadService.getFileUrl(relativePath);
            
            result.put("errno", 0);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("alt", file.getOriginalFilename());
            data.put("href", url);
            result.put("data", data);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("errno", 1);
            result.put("message", "图片上传失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // 获取当前用户ID
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录或token无效");
            }

            String token = authHeader.substring(7);
            String phone = jwtUtil.getUsernameFromToken(token);
            if (phone == null) {
                return Result.error("token无效");
            }

            // 获取用户信息
            var user = userService.findByPhone(phone);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 保存旧头像路径，用于删除
            String oldAvatarPath = user.getAvatar();
            
            // 上传新头像
            String relativePath = fileUploadService.uploadAvatar(file, user.getId());
            
            // 更新用户头像
            userService.updateAvatar(user.getId(), relativePath);
            
            // 删除旧头像
            if (oldAvatarPath != null && !oldAvatarPath.isEmpty()) {
                fileUploadService.deleteOldAvatar(oldAvatarPath);
            }
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("relativePath", relativePath);
            result.put("url", fileUploadService.getFileUrl(relativePath));
            result.put("message", "头像上传成功");
            
            return Result.success(result);
            
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件访问URL
     */
    @GetMapping("/url")
    public Result<String> getFileUrl(@RequestParam("path") String relativePath) {
        try {
            String url = fileUploadService.getFileUrl(relativePath);
            return Result.success(url);
        } catch (Exception e) {
            return Result.error("获取文件URL失败: " + e.getMessage());
        }
    }
}
