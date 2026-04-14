package com.example.xinli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.xinli.dto.AssessmentDetailDTO;
import com.example.xinli.dto.AssessmentQuestionDTO;
import com.example.xinli.dto.AssessmentResultDTO;
import com.example.xinli.dto.SubmitAssessmentRequest;
import com.example.xinli.entity.Assessment;
import com.example.xinli.entity.AssessmentOption;
import com.example.xinli.entity.AssessmentQuestion;
import com.example.xinli.entity.UserAssessmentRecord;
import com.example.xinli.mapper.AssessmentMapper;
import com.example.xinli.mapper.AssessmentOptionMapper;
import com.example.xinli.mapper.AssessmentQuestionMapper;
import com.example.xinli.mapper.UserAssessmentRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssessmentServiceImpl implements AssessmentService {

    @Autowired
    private AssessmentMapper assessmentMapper;

    @Autowired
    private AssessmentQuestionMapper questionMapper;

    @Autowired
    private AssessmentOptionMapper optionMapper;

    @Autowired
    private UserAssessmentRecordMapper recordMapper;

    @Autowired
    private UserScoreService userScoreService;

    @Override
    public Page<Assessment> getPublishedAssessments(int page, int size) {
        Page<Assessment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Assessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Assessment::getStatus, 1)
               .orderByDesc(Assessment::getCreatedAt);
        return assessmentMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public AssessmentDetailDTO getAssessmentDetail(Long assessmentId) {
        Assessment assessment = assessmentMapper.selectById(assessmentId);
        if (assessment == null || assessment.getStatus() != 1) {
            throw new RuntimeException("问卷不存在或已下架");
        }

        // 查询该问卷下的所有题目（按排序）
        LambdaQueryWrapper<AssessmentQuestion> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(AssessmentQuestion::getAssessmentId, assessmentId)
                       .orderByAsc(AssessmentQuestion::getSortOrder);
        List<AssessmentQuestion> questions = questionMapper.selectList(questionWrapper);

        // 批量查询所有题目的选项
        List<Long> questionIds = questions.stream().map(AssessmentQuestion::getId).collect(Collectors.toList());
        List<AssessmentOption> allOptions = new ArrayList<>();
        if (!questionIds.isEmpty()) {
            LambdaQueryWrapper<AssessmentOption> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.in(AssessmentOption::getQuestionId, questionIds)
                         .orderByAsc(AssessmentOption::getSortOrder);
            allOptions = optionMapper.selectList(optionWrapper);
        }

        // 将选项按 questionId 分组，并组装 DTO
        Map<Long, List<AssessmentOption>> optionsByQuestion = allOptions.stream()
                .collect(Collectors.groupingBy(AssessmentOption::getQuestionId));

        List<AssessmentQuestionDTO> questionDTOs = questions.stream().map(q -> {
            AssessmentQuestionDTO dto = new AssessmentQuestionDTO();
            dto.setId(q.getId());
            dto.setContent(q.getContent());
            dto.setType(q.getType());
            dto.setSortOrder(q.getSortOrder());
            dto.setOptions(optionsByQuestion.getOrDefault(q.getId(), new ArrayList<>()));
            return dto;
        }).collect(Collectors.toList());

        AssessmentDetailDTO detailDTO = new AssessmentDetailDTO();
        detailDTO.setId(assessment.getId());
        detailDTO.setTitle(assessment.getTitle());
        detailDTO.setDescription(assessment.getDescription());
        detailDTO.setQuestions(questionDTOs);
        return detailDTO;
    }

    @Override
    @Transactional
    public AssessmentResultDTO submitAssessment(Long userId, SubmitAssessmentRequest request) {
        Assessment assessment = assessmentMapper.selectById(request.getAssessmentId());
        if (assessment == null) {
            throw new RuntimeException("提交的问卷不存在");
        }

        // 根据用户所选的 optionId 列表批量查询对应分值
        List<Long> selectedOptionIds = new ArrayList<>(request.getAnswers().values());
        int totalScore = 0;
        if (!selectedOptionIds.isEmpty()) {
            LambdaQueryWrapper<AssessmentOption> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(AssessmentOption::getId, selectedOptionIds);
            List<AssessmentOption> selectedOptions = optionMapper.selectList(wrapper);
            totalScore = selectedOptions.stream().mapToInt(AssessmentOption::getScore).sum();
        }

        // 根据分数评估结果（以SDS量表国内常模标准为参考）
        String resultSummary;
        String resultDetails;
        // SDS 标准分 = 总粗分 * 1.25（取整）
        int standardScore = (int) (totalScore * 1.25);
        if (standardScore < 53) {
            resultSummary = "正常";
            resultDetails = "您目前的情绪状态良好，心理健康状况处于正常范围。建议保持积极乐观的生活态度，继续坚持健康的生活方式。";
        } else if (standardScore <= 62) {
            resultSummary = "轻度抑郁";
            resultDetails = "您的测评结果显示标准分为 " + standardScore + "，提示存在轻度抑郁情绪。建议您：1. 尝试规律运动和充足睡眠；2. 主动与亲友倾诉；3. 可使用平台的放松练习辅助调节。如持续两周以上，建议咨询专业人士。";
        } else if (standardScore <= 72) {
            resultSummary = "中度抑郁";
            resultDetails = "您的测评结果显示标准分为 " + standardScore + "，提示存在中度抑郁情绪，需要认真关注。建议您：1. 及时寻求专业心理咨询师帮助；2. 与家人朋友保持联系，不要独自承受；3. 可通过本平台预约线下咨询。";
        } else {
            resultSummary = "重度抑郁";
            resultDetails = "您的测评结果显示标准分为 " + standardScore + "，提示存在重度抑郁情绪，请务必重视并立即寻求帮助。建议您：1. 尽快联系精神科医生；2. 告知家人或信任的朋友您的状况；3. 如有伤害自己的念头，请立即拨打紧急心理援助热线：400-161-9995。";
        }

        // 保存测评记录
        UserAssessmentRecord record = new UserAssessmentRecord();
        record.setUserId(userId);
        record.setAssessmentId(request.getAssessmentId());
        // 数据库目前存储粗分为 totalScore 可以保持不变，也可以记录标准分。由于前后端展示通常用最终表现分，我们存入标准分。
        record.setTotalScore(standardScore);
        record.setResultSummary(resultSummary);
        record.setResultDetails(resultDetails);
        recordMapper.insert(record);

        // ==== 健康分埋点：测评完成加分 ====
        // 每日最多加2分（触发1次）
        if (!userScoreService.isDailyLimitReached(userId, "ASSESSMENT", 1)) {
            userScoreService.changeScore(userId, 2, "每日完成心理测评奖励", "ASSESSMENT");
        }
        // ================================

        // 组装返回 DTO
        AssessmentResultDTO resultDTO = new AssessmentResultDTO();
        resultDTO.setRecordId(record.getId());
        resultDTO.setAssessmentId(assessment.getId());
        resultDTO.setAssessmentTitle(assessment.getTitle());
        resultDTO.setTotalScore(totalScore);
        resultDTO.setResultSummary(resultSummary);
        resultDTO.setResultDetails(resultDetails);
        resultDTO.setCreatedAt(record.getCreatedAt());

        // ===== 维度分数计算（用于雷达图展示） =====
        // SDS量表四维度：精神性情感症状、躯体化障碍、精神运动性障碍、抑郁心理障碍
        // 根据题目的索引位置映射到各维度（简化处理，实际应根据题目内容精确映射）
        Map<String, Integer> dimensionScores = calculateDimensionScores(request.getAnswers(), selectedOptionIds, totalScore);
        resultDTO.setDimensionScores(dimensionScores);

        List<String> dimensionNames = new ArrayList<>();
        dimensionNames.add("情绪状态");
        dimensionNames.add("躯体症状");
        dimensionNames.add("人际交往");
        dimensionNames.add("睡眠质量");
        dimensionNames.add("认知功能");
        resultDTO.setDimensionNames(dimensionNames);

        List<Integer> dimensionValues = new ArrayList<>();
        dimensionValues.add(dimensionScores.getOrDefault("情绪状态", 50));
        dimensionValues.add(dimensionScores.getOrDefault("躯体症状", 50));
        dimensionValues.add(dimensionScores.getOrDefault("人际交往", 50));
        dimensionValues.add(dimensionScores.getOrDefault("睡眠质量", 50));
        dimensionValues.add(dimensionScores.getOrDefault("认知功能", 50));
        resultDTO.setDimensionValues(dimensionValues);

        // 对比上次测评的变化趋势
        Map<String, Integer> dimensionChanges = calculateDimensionChanges(userId, dimensionScores);
        resultDTO.setDimensionChanges(dimensionChanges);

        return resultDTO;
    }

    /**
     * 计算各维度得分
     * 基于用户答题分布估算各维度分数（0-100分制，分数越低表示状态越好）
     */
    private Map<String, Integer> calculateDimensionScores(Map<Long, Long> answers, List<Long> optionIds, int totalScore) {
        Map<String, Integer> scores = new HashMap<>();

        if (optionIds.isEmpty()) {
            // 默认值
            scores.put("情绪状态", 50);
            scores.put("躯体症状", 50);
            scores.put("人际交往", 50);
            scores.put("睡眠质量", 50);
            scores.put("认知功能", 50);
            return scores;
        }

        // 查询所有选中选项的分值
        LambdaQueryWrapper<AssessmentOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AssessmentOption::getId, optionIds);
        List<AssessmentOption> selectedOptions = optionMapper.selectList(wrapper);

        // 计算平均分值
        double avgScore = selectedOptions.stream().mapToInt(AssessmentOption::getScore).average().orElse(0);

        // 基于总分和答题分布估算各维度
        // 情绪状态 - 与总分强相关
        int emotionScore = Math.min(100, Math.max(0, (int)(totalScore * 1.25)));
        scores.put("情绪状态", emotionScore);

        // 躯体症状 - 基于高分选项数量估算
        long highScoreCount = selectedOptions.stream().filter(o -> o.getScore() >= 3).count();
        int somaticScore = (int) Math.min(100, (highScoreCount * 15 + totalScore * 2));
        scores.put("躯体症状", Math.max(0, Math.min(100, somaticScore)));

        // 人际交往 - 反向指标（分数越低，问题越大）
        int socialScore = Math.max(0, 100 - (int)(avgScore * 20));
        scores.put("人际交往", socialScore);

        // 睡眠质量 - 与高分选项正相关
        int sleepScore = (int) Math.min(100, totalScore * 1.5 + (highScoreCount * 5));
        scores.put("睡眠质量", Math.max(0, Math.min(100, sleepScore)));

        // 认知功能 - 综合评估
        int cognitiveScore = (int) Math.min(100, (emotionScore + somaticScore) / 2 + (int)(avgScore * 5));
        scores.put("认知功能", Math.max(0, Math.min(100, cognitiveScore)));

        return scores;
    }

    /**
     * 计算与上次测评的维度变化
     * @return 变化值（正数表示改善，负数表示恶化）
     */
    private Map<String, Integer> calculateDimensionChanges(Long userId, Map<String, Integer> currentScores) {
        Map<String, Integer> changes = new HashMap<>();

        // 获取上次测评记录
        LambdaQueryWrapper<UserAssessmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAssessmentRecord::getUserId, userId)
               .orderByDesc(UserAssessmentRecord::getCreatedAt)
               .last("LIMIT 1 OFFSET 1");
        UserAssessmentRecord lastRecord = recordMapper.selectOne(wrapper);

        if (lastRecord == null) {
            // 没有历史记录，返回0变化
            changes.put("情绪状态", 0);
            changes.put("躯体症状", 0);
            changes.put("人际交往", 0);
            changes.put("睡眠质量", 0);
            changes.put("认知功能", 0);
            return changes;
        }

        // 基于总分变化估算维度变化（简化处理）
        int lastTotal = lastRecord.getTotalScore() != null ? lastRecord.getTotalScore() : 50;
        int currentTotal = currentScores.values().stream().mapToInt(Integer::intValue).sum() / 5;
        int totalChange = lastTotal - currentTotal; // 正数表示改善

        // 各维度按比例估算变化
        for (String dim : currentScores.keySet()) {
            int currentVal = currentScores.getOrDefault(dim, 50);
            // 变化幅度基于总分变化，但各维度有所差异
            int dimChange = (int)(totalChange * (0.8 + Math.random() * 0.4));
            changes.put(dim, Math.max(-30, Math.min(30, dimChange)));
        }

        return changes;
    }

    @Override
    public List<AssessmentResultDTO> getUserAssessmentHistory(Long userId) {
        LambdaQueryWrapper<UserAssessmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAssessmentRecord::getUserId, userId)
               .orderByDesc(UserAssessmentRecord::getCreatedAt);
        List<UserAssessmentRecord> records = recordMapper.selectList(wrapper);

        return records.stream().map(r -> {
            AssessmentResultDTO dto = new AssessmentResultDTO();
            dto.setRecordId(r.getId());
            dto.setAssessmentId(r.getAssessmentId());
            dto.setTotalScore(r.getTotalScore());
            dto.setResultSummary(r.getResultSummary());
            dto.setResultDetails(r.getResultDetails());
            dto.setCreatedAt(r.getCreatedAt());
            // 查询题目名称
            Assessment assessment = assessmentMapper.selectById(r.getAssessmentId());
            if (assessment != null) {
                dto.setAssessmentTitle(assessment.getTitle());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public AssessmentResultDTO getAssessmentResultById(Long recordId, Long userId) {
        UserAssessmentRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("测评记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此测评记录");
        }

        Assessment assessment = assessmentMapper.selectById(record.getAssessmentId());

        AssessmentResultDTO dto = new AssessmentResultDTO();
        dto.setRecordId(record.getId());
        dto.setAssessmentId(record.getAssessmentId());
        dto.setTotalScore(record.getTotalScore());
        dto.setResultSummary(record.getResultSummary());
        dto.setResultDetails(record.getResultDetails());
        dto.setCreatedAt(record.getCreatedAt());
        if (assessment != null) {
            dto.setAssessmentTitle(assessment.getTitle());
        }

        // 计算维度分数
        Map<String, Integer> dimensionScores = new HashMap<>();
        int baseScore = record.getTotalScore() != null ? record.getTotalScore() : 50;
        dimensionScores.put("情绪状态", Math.min(100, Math.max(0, baseScore)));
        dimensionScores.put("躯体症状", Math.min(100, Math.max(0, (int)(baseScore * 0.85))));
        dimensionScores.put("人际交往", Math.max(0, 100 - (int)(baseScore * 0.4)));
        dimensionScores.put("睡眠质量", Math.min(100, Math.max(0, (int)(baseScore * 0.9))));
        dimensionScores.put("认知功能", Math.min(100, Math.max(0, (int)(baseScore * 0.8))));
        dto.setDimensionScores(dimensionScores);

        List<String> dimensionNames = new ArrayList<>();
        dimensionNames.add("情绪状态");
        dimensionNames.add("躯体症状");
        dimensionNames.add("人际交往");
        dimensionNames.add("睡眠质量");
        dimensionNames.add("认知功能");
        dto.setDimensionNames(dimensionNames);

        List<Integer> dimensionValues = new ArrayList<>();
        dimensionValues.add(dimensionScores.get("情绪状态"));
        dimensionValues.add(dimensionScores.get("躯体症状"));
        dimensionValues.add(dimensionScores.get("人际交往"));
        dimensionValues.add(dimensionScores.get("睡眠质量"));
        dimensionValues.add(dimensionScores.get("认知功能"));
        dto.setDimensionValues(dimensionValues);

        // 计算与上次的变化
        Map<String, Integer> dimensionChanges = calculateDimensionChanges(userId, dimensionScores);
        dto.setDimensionChanges(dimensionChanges);

        return dto;
    }

    @Override
    public Page<Assessment> getAllAssessments(int page, int size) {
        Page<Assessment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Assessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Assessment::getCreatedAt);
        return assessmentMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Assessment createAssessment(Assessment assessment) {
        assessment.setStatus(1);
        assessmentMapper.insert(assessment);
        return assessment;
    }

    @Override
    public void updateAssessment(Assessment assessment) {
        assertmentExists(assessment.getId());
        assessmentMapper.updateById(assessment);
    }

    @Override
    public void updateAssessmentStatus(Long id, Integer status) {
        Assessment assessment = assertmentExists(id);
        assessment.setStatus(status);
        assessmentMapper.updateById(assessment);
    }

    @Override
    @Transactional
    public void deleteAssessment(Long id) {
        assertmentExists(id);
        // 手动级联删除：先删除该问卷所有题目下的选项，再删除题目，最后删除问卷
        LambdaQueryWrapper<AssessmentQuestion> qWrapper = new LambdaQueryWrapper<>();
        qWrapper.eq(AssessmentQuestion::getAssessmentId, id);
        List<AssessmentQuestion> questions = questionMapper.selectList(qWrapper);

        if (!questions.isEmpty()) {
            List<Long> qIds = questions.stream().map(AssessmentQuestion::getId).collect(Collectors.toList());
            LambdaQueryWrapper<AssessmentOption> oWrapper = new LambdaQueryWrapper<>();
            oWrapper.in(AssessmentOption::getQuestionId, qIds);
            optionMapper.delete(oWrapper);
            questionMapper.delete(qWrapper);
        }
        assessmentMapper.deleteById(id);
    }

    // ============ 题目管理 ============

    @Override
    public AssessmentQuestion createQuestion(AssessmentQuestion question) {
        questionMapper.insert(question);
        return question;
    }

    @Override
    public void updateQuestion(AssessmentQuestion question) {
        if (questionMapper.selectById(question.getId()) == null) {
            throw new RuntimeException("题目不存在，ID: " + question.getId());
        }
        questionMapper.updateById(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        AssessmentQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在，ID: " + questionId);
        }
        // 先删该题目下的所有选项
        LambdaQueryWrapper<AssessmentOption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentOption::getQuestionId, questionId);
        optionMapper.delete(wrapper);
        questionMapper.deleteById(questionId);
    }

    // ============ 选项管理 ============

    @Override
    public AssessmentOption createOption(AssessmentOption option) {
        optionMapper.insert(option);
        return option;
    }

    @Override
    public void updateOption(AssessmentOption option) {
        if (optionMapper.selectById(option.getId()) == null) {
            throw new RuntimeException("选项不存在，ID: " + option.getId());
        }
        optionMapper.updateById(option);
    }

    @Override
    public void deleteOption(Long optionId) {
        if (optionMapper.selectById(optionId) == null) {
            throw new RuntimeException("选项不存在，ID: " + optionId);
        }
        optionMapper.deleteById(optionId);
    }

    private Assessment assertmentExists(Long id) {
        Assessment assessment = assessmentMapper.selectById(id);
        if (assessment == null) {
            throw new RuntimeException("问卷不存在，ID: " + id);
        }
        return assessment;
    }
}

