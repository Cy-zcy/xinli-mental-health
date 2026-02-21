package com.example.xinli.service;

import com.example.xinli.config.FileUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Service
public class FileUploadService {
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    /**
     * 上传头像文件
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 相对路径
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // 验证文件
        validateFile(file);
        
        // 创建上传目录
        String uploadDir = fileUploadConfig.getFullAvatarPath();
        createDirectoryIfNotExists(uploadDir);
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = generateAvatarFilename(userId, extension);
        
        // 保存文件
        Path filePath = Paths.get(uploadDir, newFilename);
        Files.write(filePath, file.getBytes());
        
        // 返回相对路径
        return fileUploadConfig.getAvatarPath() + "/" + newFilename;
    }
    
    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 检查文件大小
        if (file.getSize() > fileUploadConfig.getMaxSize()) {
            throw new IllegalArgumentException("文件大小不能超过 " + (fileUploadConfig.getMaxSize() / 1024 / 1024) + "MB");
        }
        
        // 检查文件类型
        String extension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedFileType(extension)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持: " + Arrays.toString(fileUploadConfig.getAllowedTypes()));
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
    
    /**
     * 检查是否为允许的文件类型
     */
    private boolean isAllowedFileType(String extension) {
        return Arrays.asList(fileUploadConfig.getAllowedTypes()).contains(extension);
    }
    
    /**
     * 生成头像文件名
     */
    private String generateAvatarFilename(Long userId, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "avatar_" + userId + "_" + timestamp + "_" + uuid + "." + extension;
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
    
    /**
     * 删除旧头像文件
     */
    public void deleteOldAvatar(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return;
        }
        
        try {
            Path filePath = Paths.get(fileUploadConfig.getBasePath(), relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 删除失败不影响主流程，记录日志即可
            System.err.println("删除旧头像失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件的完整URL
     */
    public String getFileUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        return fileUploadConfig.getUrlPrefix() + "/" + relativePath;
    }
}
