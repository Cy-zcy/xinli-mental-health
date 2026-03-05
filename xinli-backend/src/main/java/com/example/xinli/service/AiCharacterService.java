package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.entity.AiCharacter;
import com.example.xinli.mapper.AiCharacterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 人设服务
 */
@Service
public class AiCharacterService {

    @Autowired
    private AiCharacterMapper aiCharacterMapper;

    /**
     * 获取所有启用的 AI 人设列表（供用户端选择）
     */
    public List<AiCharacter> getActiveCharacters() {
        QueryWrapper<AiCharacter> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", 1).orderByAsc("id");
        return aiCharacterMapper.selectList(wrapper);
    }

    /**
     * 分页查询所有角色（供管理端使用）
     */
    public Page<AiCharacter> getCharactersPage(int page, int size, String keyword) {
        QueryWrapper<AiCharacter> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("name", keyword).or().like("background", keyword);
        }
        wrapper.orderByDesc("created_at");
        return aiCharacterMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 根据 ID 获取详细人设
     */
    public AiCharacter getCharacterById(Long id) {
        return aiCharacterMapper.selectById(id);
    }

    /**
     * 添加新人设
     */
    public boolean addCharacter(AiCharacter character) {
        return aiCharacterMapper.insert(character) > 0;
    }

    /**
     * 更新人设
     */
    public boolean updateCharacter(AiCharacter character) {
        return aiCharacterMapper.updateById(character) > 0;
    }

    /**
     * 删除人设（逻辑上应考虑限制或校验，这里为了后台管理演示直接实现删除）
     */
    public boolean deleteCharacter(Long id) {
        return aiCharacterMapper.deleteById(id) > 0;
    }
}
