package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.UserDetailDTO;
import com.example.xinli.entity.Comment;
import com.example.xinli.entity.ForumPost;
import com.example.xinli.entity.User;
import com.example.xinli.mapper.CommentMapper;
import com.example.xinli.mapper.ForumPostMapper;
import com.example.xinli.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    /**
     * 分页查询用户列表
     */
    public Page<User> getUsers(int page, int size, String keyword, Integer status) {
        Page<User> userPage = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        
        // 关键词搜索（昵称或手机号）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("nickname", keyword).or().like("phone", keyword));
        }
        
        // 状态筛选
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc("created_at");
        
        return userMapper.selectPage(userPage, wrapper);
    }
    
    /**
     * 获取用户详情
     */
    public UserDetailDTO getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        
        UserDetailDTO userDetail = new UserDetailDTO();
        BeanUtils.copyProperties(user, userDetail);
        
        // 统计用户发帖数量
        QueryWrapper<ForumPost> postWrapper = new QueryWrapper<>();
        postWrapper.eq("user_id", id);
        userDetail.setPostCount(forumPostMapper.selectCount(postWrapper));
        
        // 统计用户评论数量
        QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
        commentWrapper.eq("user_id", id);
        userDetail.setCommentCount(commentMapper.selectCount(commentWrapper));
        
        // 统计用户获赞数量（所有帖子的点赞总数）
        QueryWrapper<ForumPost> likeWrapper = new QueryWrapper<>();
        likeWrapper.eq("user_id", id);
        likeWrapper.select("IFNULL(SUM(like_count), 0) as total_likes");
        // 这里需要自定义SQL，暂时设为0
        userDetail.setLikeCount(0L);
        
        return userDetail;
    }
    
    /**
     * 更新用户状态
     */
    public boolean updateUserStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }
    
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }
    
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    /**
     * 根据手机号获取用户
     */
    public User getUserByPhone(String phone) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 根据手机号查找用户（别名方法）
     */
    public User findByPhone(String phone) {
        return getUserByPhone(phone);
    }

    /**
     * 更新用户头像
     */
    public void updateAvatar(Long userId, String avatarPath) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarPath);
        userMapper.updateById(user);
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(Long userId, String nickname) {
        User user = new User();
        user.setId(userId);
        user.setNickname(nickname);
        userMapper.updateById(user);
    }

    /**
     * 根据用户ID列表批量获取用户信息
     */
    public List<User> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userMapper.selectBatchIds(userIds);
    }
}
