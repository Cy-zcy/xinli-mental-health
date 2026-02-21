package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.PageResult;
import com.example.xinli.dto.UpdateUserStatusRequest;
import com.example.xinli.dto.UserDTO;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 分页获取用户列表
     */
    public PageResult<UserDTO> getUserList(Integer page, Integer size, String keyword, Integer status) {
        Page<UserDTO> pageParam = new Page<>(page, size);
        IPage<UserDTO> result = userMapper.selectUserPage(pageParam, keyword, status);

        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), result.getRecords());
    }
    
    /**
     * 获取用户详情
     */
    public UserDTO getUserDetail(Long userId) {
        return userMapper.selectUserDetail(userId);
    }
    
    /**
     * 更新用户状态
     */
    public boolean updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = new User();
        user.setId(userId);
        user.setStatus(request.getStatus());
        
        return userMapper.updateById(user) > 0;
    }
    
    /**
     * 删除用户
     */
    public boolean deleteUser(Long userId) {
        return userMapper.deleteById(userId) > 0;
    }
    
    /**
     * 获取用户统计信息
     */
    public Long getTotalUserCount() {
        return userMapper.selectCount(new QueryWrapper<>());
    }
    
    public Long getActiveUserCount() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        return userMapper.selectCount(wrapper);
    }
}
