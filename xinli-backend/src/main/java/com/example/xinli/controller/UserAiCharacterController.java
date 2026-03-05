package com.example.xinli.controller;

import com.example.xinli.dto.Result;
import com.example.xinli.entity.AiCharacter;
import com.example.xinli.service.AiCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 - AI 人设接口
 */
@RestController
@RequestMapping("/api/chat/characters")
@CrossOrigin
public class UserAiCharacterController {

    @Autowired
    private AiCharacterService aiCharacterService;

    /**
     * 获取可用的预设角色列表
     * GET /api/chat/characters
     */
    @GetMapping
    public Result<List<AiCharacter>> getActiveCharacters() {
        return Result.success(aiCharacterService.getActiveCharacters());
    }
}
