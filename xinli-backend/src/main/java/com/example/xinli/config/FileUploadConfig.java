package com.example.xinli.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 */
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    
    /**
     * 文件上传根目录
     */
    private String basePath = "uploads";
    
    /**
     * 头像上传目录
     */
    private String avatarPath = "avatars";
    
    /**
     * 允许的文件类型
     */
    private String[] allowedTypes = {"jpg", "jpeg", "png", "gif", "webp"};
    
    /**
     * 最大文件大小（字节）
     */
    private long maxSize = 5 * 1024 * 1024; // 5MB
    
    /**
     * 访问URL前缀
     */
    private String urlPrefix = "/uploads";
    
    public String getBasePath() {
        return basePath;
    }
    
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    public String getAvatarPath() {
        return avatarPath;
    }
    
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    
    public String[] getAllowedTypes() {
        return allowedTypes;
    }
    
    public void setAllowedTypes(String[] allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
    
    public long getMaxSize() {
        return maxSize;
    }
    
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
    
    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }
    
    /**
     * 获取完整的头像上传路径
     */
    public String getFullAvatarPath() {
        return basePath + "/" + avatarPath;
    }
}
