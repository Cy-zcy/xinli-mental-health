package com.example.xinli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.xinli.dto.Result;
import com.example.xinli.entity.ToolRecord;
import com.example.xinli.mapper.ToolRecordMapper;
import com.example.xinli.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户端 - 心理工具使用记录控制器
 * 路由前缀: /api/tools
 */
@RestController
@RequestMapping("/api/tools")
@CrossOrigin
public class ToolController {

    @Autowired
    private ToolRecordMapper toolRecordMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 记录工具使用完成情况
     * POST /api/tools/record
     * 请求体: { toolType, durationSeconds, cycles, completed, pattern }
     */
    @PostMapping("/record")
    public Result<Void> recordToolUsage(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }

            ToolRecord record = new ToolRecord();
            record.setUserId(userId);
            record.setToolType((String) body.get("toolType"));
            record.setDurationSeconds(body.get("durationSeconds") != null
                    ? ((Number) body.get("durationSeconds")).intValue() : 0);
            record.setCycles(body.get("cycles") != null
                    ? ((Number) body.get("cycles")).intValue() : 0);
            record.setCompleted(body.get("completed") != null
                    ? (Boolean.TRUE.equals(body.get("completed")) ? 1 : 0) : 0);
            record.setPattern((String) body.getOrDefault("pattern", ""));
            record.setCreatedAt(LocalDateTime.now());

            toolRecordMapper.insert(record);
            return Result.success();
        } catch (Exception e) {
            return Result.error("记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的工具使用历史
     * GET /api/tools/history?toolType=breathing&limit=10
     */
    @GetMapping("/history")
    public Result<List<ToolRecord>> getToolHistory(
            @RequestParam(required = false) String toolType,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }

            QueryWrapper<ToolRecord> wrapper = new QueryWrapper<ToolRecord>()
                    .eq("user_id", userId)
                    .orderByDesc("created_at")
                    .last("LIMIT " + limit);

            if (toolType != null && !toolType.isEmpty()) {
                wrapper.eq("tool_type", toolType);
            }

            return Result.success(toolRecordMapper.selectList(wrapper));
        } catch (Exception e) {
            return Result.error("获取历史失败: " + e.getMessage());
        }
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                return jwtUtil.getUserIdFromToken(token.substring(7));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
