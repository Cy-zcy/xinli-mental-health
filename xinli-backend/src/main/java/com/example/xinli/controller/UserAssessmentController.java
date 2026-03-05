package com.example.xinli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.AssessmentDetailDTO;
import com.example.xinli.dto.AssessmentResultDTO;
import com.example.xinli.dto.ChatRequest;
import com.example.xinli.dto.Result;
import com.example.xinli.dto.SubmitAssessmentRequest;
import com.example.xinli.entity.Assessment;
import com.example.xinli.service.AssessmentService;
import com.example.xinli.service.DeepSeekApiService;
import com.example.xinli.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.xinli.service.UserScoreService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户端 - 心理测评控制器
 * 路由前缀: /api/assessment
 */
@RestController
@RequestMapping("/api/assessment")
@CrossOrigin
public class UserAssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private DeepSeekApiService deepSeekApiService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserScoreService userScoreService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 1. 获取已发布的问卷列表（分页）
     * GET /api/assessment/list
     */
    @GetMapping("/list")
    public Result<Page<Assessment>> getAssessmentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return Result.success(assessmentService.getPublishedAssessments(page, size));
        } catch (Exception e) {
            return Result.error("获取问卷列表失败: " + e.getMessage());
        }
    }

    /**
     * 2. 获取问卷详情（含题目和选项），用于渲染答题页
     * GET /api/assessment/{id}
     */
    @GetMapping("/{id}")
    public Result<AssessmentDetailDTO> getAssessmentDetail(@PathVariable Long id) {
        try {
            return Result.success(assessmentService.getAssessmentDetail(id));
        } catch (Exception e) {
            return Result.error("获取问卷详情失败: " + e.getMessage());
        }
    }

    /**
     * 3. 用户提交答卷，获取评估结果
     * POST /api/assessment/submit
     * 需要登录
     */
    @PostMapping("/submit")
    public Result<AssessmentResultDTO> submitAssessment(
            @Valid @RequestBody SubmitAssessmentRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "请先登录后再进行测评");
            }
            return Result.success(assessmentService.submitAssessment(userId, request));
        } catch (Exception e) {
            return Result.error("提交答卷失败: " + e.getMessage());
        }
    }

    /**
     * 4. 查询当前用户的历史测评记录
     * GET /api/assessment/history
     * 需要登录
     */
    @GetMapping("/history")
    public Result<List<AssessmentResultDTO>> getUserHistory(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(assessmentService.getUserAssessmentHistory(userId));
        } catch (Exception e) {
            return Result.error("获取历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 5. AI 测评结果分析报告
     * POST /api/assessment/ai-analysis
     * 请求体: { assessmentName, score, level, description }
     * 返回:   { aiReport: "AI 生成的个性化分析文字" }
     *
     * 前端将测评的量表名称、分数、等级传入，AI 生成一段温暖、专业的个性化建议报告。
     * 每次调用消耗少量 Token，建议缓存在前端避免重复请求同一次测评结果。
     */
    @PostMapping("/ai-analysis")
    public Result<Map<String, String>> getAiAnalysis(
            @RequestBody Map<String, Object> body,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "请先登录");
            }

            String assessmentName = (String) body.getOrDefault("assessmentName", "心理健康测评");
            Object scoreObj = body.get("score");
            int score = scoreObj != null ? ((Number) scoreObj).intValue() : 0;
            String level = (String) body.getOrDefault("level", "");
            String description = (String) body.getOrDefault("description", "");

            // 构建提示词
            String userPrompt = String.format("""
                    用户刚刚完成了【%s】测评。
                    测评得分：%d分
                    结果等级：%s
                    系统说明：%s

                    请你作为专业的心理健康顾问，根据以上测评结果，生成一份个性化的分析报告，并评估当前用户的心理健康状态变动分数。
                    请**强制以 JSON 格式**返回以下三个字段：
                    1. "aiReport": 字符串。一段温暖、鼓励、非评判的分析报告。简要解释该分数意味着什么，给出2-3条具体的自我照护建议，如有必要温和地建议寻求专业帮助。字数控制在200字以内。
                    2. "scoreChange": 整数。根据测评等级的危机程度给出的健康分扣分。
                       - 若结果为"正常"或轻度波动，返回 0
                       - 若结果存在明显的抑郁/焦虑倾向，根据程度返回 -5 到 -30 之间的负数
                    3. "scoreReason": 字符串。给出评分的核心理由（20字以内）。

                    切记只能输出合法标准的JSON字符串，不能包裹```json```标签。
                    """, assessmentName, score, level, description);

            // 构建消息列表（system + user）
            List<ChatRequest.Message> messages = new ArrayList<>();

            ChatRequest.Message systemMsg = new ChatRequest.Message();
            systemMsg.setRole("system");
            systemMsg.setContent("你是一个专业的心理健康顾问，必须全盘使用JSON返回结果。请使用中文回复。");
            messages.add(systemMsg);

            ChatRequest.Message userMsg = new ChatRequest.Message();
            userMsg.setRole("user");
            userMsg.setContent(userPrompt);
            messages.add(userMsg);

            // 调用 DeepSeek（同步等待）
            var chatResponse = deepSeekApiService.sendChatRequest(messages).block();
            if (chatResponse == null || chatResponse.getContent() == null) {
                return Result.error("AI 分析生成失败，请稍后重试");
            }
            
            String responseText = chatResponse.getContent();
            String aiReport = "分析生成中遇到格式异常，请稍后再试。";
            
            try {
                // 去除可能存在的 markdown json 标记
                String jsonStr = responseText.trim();
                if (jsonStr.startsWith("```json")) {
                    jsonStr = jsonStr.substring(7);
                }
                if (jsonStr.startsWith("```")) {
                    jsonStr = jsonStr.substring(3);
                }
                if (jsonStr.endsWith("```")) {
                    jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
                }
                
                JsonNode root = objectMapper.readTree(jsonStr.trim());
                if (root.has("aiReport")) {
                    aiReport = root.get("aiReport").asText();
                }
                
                // ================= 核心：联动健康分模块 =================
                if (root.has("scoreChange")) {
                    int scoreChange = root.get("scoreChange").asInt();
                    if (scoreChange < 0) {
                        String reason = root.has("scoreReason") ? root.get("scoreReason").asText() : "根据测评【" + assessmentName + "】结果AI判定扣分";
                        String changeType = "ASSESSMENT_AI_" + assessmentName;
                        // 重复测评防震荡：同一量表7天内只扣一次分
                        if (!userScoreService.hasRecentRecord(userId, changeType, 7)) {
                            userScoreService.changeScore(userId, scoreChange, reason, changeType);
                        }
                    }
                }
                // ==========================================================
                
            } catch (Exception e) {
                return Result.error("解析 AI 评价 JSON 失败: " + e.getMessage());
            }

            return Result.success(Map.of("aiReport", aiReport));

        } catch (Exception e) {
            return Result.error("AI 分析失败: " + e.getMessage());
        }
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}


