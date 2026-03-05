package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.AiCharacter;
import com.example.xinli.service.AiCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - AI 人设管理接口
 */
@RestController
@RequestMapping("/api/admin/chat/characters")
@CrossOrigin
public class AdminAiCharacterController {

    @Autowired
    private AiCharacterService aiCharacterService;

    /**
     * 分页查询角色列表
     */
    @GetMapping
    public Result<Page<AiCharacter>> getCharacters(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(aiCharacterService.getCharactersPage(page, size, keyword));
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    public Result<AiCharacter> getCharacter(@PathVariable Long id) {
        return Result.success(aiCharacterService.getCharacterById(id));
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result<Void> addCharacter(@RequestBody AiCharacter character) {
        if (aiCharacterService.addCharacter(character)) {
            return Result.success();
        }
        return Result.error("新增角色失败");
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public Result<Void> updateCharacter(@PathVariable Long id, @RequestBody AiCharacter character) {
        character.setId(id);
        if (aiCharacterService.updateCharacter(character)) {
            return Result.success();
        }
        return Result.error("更新角色失败");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCharacter(@PathVariable Long id) {
        if (aiCharacterService.deleteCharacter(id)) {
            return Result.success();
        }
        return Result.error("删除角色失败");
    }
}
