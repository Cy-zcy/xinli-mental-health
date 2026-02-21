package com.example.xinli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建帖子请求DTO
 */
public class CreatePostRequest {
    
    @NotBlank(message = "帖子标题不能为空")
    @Size(min = 1, max = 200, message = "帖子标题长度必须在1-200字符之间")
    private String title;
    
    @NotBlank(message = "帖子内容不能为空")
    @Size(min = 10, max = 10000, message = "帖子内容长度必须在10-10000字符之间")
    private String content;
    
    @NotNull(message = "帖子分类不能为空")
    private String category;
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
}
