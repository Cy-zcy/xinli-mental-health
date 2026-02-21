package com.example.xinli.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新用户信息请求DTO
 */
public class UpdateUserInfoRequest {
    
    @Size(min = 1, max = 20, message = "昵称长度必须在1-20字符之间")
    private String nickname; // 新昵称，可选
    
    private String avatar; // 新头像URL，可选
    
    private String oldPassword; // 旧密码，修改密码时必需
    
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    private String newPassword; // 新密码，可选
    
    // Getters and Setters
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getOldPassword() {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
