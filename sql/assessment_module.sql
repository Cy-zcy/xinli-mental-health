-- --------------------------------------------------------
-- Phase 1: 心理测评模块 (Psychological Assessment Module)
-- --------------------------------------------------------

-- 1. 测评问卷主表 (Assessments Table)
DROP TABLE IF EXISTS `assessments`;
CREATE TABLE `assessments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问卷标题 (如: SDS抑郁自评量表)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '问卷描述/指导语',
  `status` tinyint DEFAULT 1 COMMENT '状态：1-正常发布，0-下架/草稿',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='心理测评问卷表';

-- 2. 测评题目表 (Assessment Questions Table)
DROP TABLE IF EXISTS `assessment_questions`;
CREATE TABLE `assessment_questions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_id` bigint NOT NULL COMMENT '所属问卷ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题干内容',
  `type` varchar(20) DEFAULT 'single_choice' COMMENT '题目类型 (single_choice: 单选)',
  `sort_order` int DEFAULT 0 COMMENT '题目排序序号',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_assessment_id` (`assessment_id`),
  CONSTRAINT `fk_question_assessment` FOREIGN KEY (`assessment_id`) REFERENCES `assessments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测评题目表';

-- 3. 测评选项表 (Assessment Options Table)
DROP TABLE IF EXISTS `assessment_options`;
CREATE TABLE `assessment_options` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL COMMENT '所属题目ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '选项内容 (如: 偶尔、经常)',
  `score` int NOT NULL DEFAULT 0 COMMENT '该选项代表的分值',
  `sort_order` int DEFAULT 0 COMMENT '选项排序序号',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_question_id` (`question_id`),
  CONSTRAINT `fk_option_question` FOREIGN KEY (`question_id`) REFERENCES `assessment_questions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测评题目选项表';

-- 4. 用户测评记录表 (User Assessment Records Table)
DROP TABLE IF EXISTS `user_assessment_records`;
CREATE TABLE `user_assessment_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '答题用户ID',
  `assessment_id` bigint NOT NULL COMMENT '所答问卷ID',
  `total_score` int NOT NULL COMMENT '测评总得分',
  `result_summary` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '结果概述 (如: 轻度抑郁)',
  `result_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '详细的评估建议或评语',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '测评完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_assessment_id` (`assessment_id`),
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_record_assessment` FOREIGN KEY (`assessment_id`) REFERENCES `assessments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户测评记录表';

-- --------------------------------------------------------
-- 插入一些初始样例数据 (以 SDS 自评量表简化版为例)
-- --------------------------------------------------------

-- 插入一条 SDS 问卷记录
INSERT INTO `assessments` (`id`, `title`, `description`) VALUES 
(1, 'SDS抑郁自评量表', '请根据您过去一周的实际感受，选择最符合您情况的选项。本测试结果仅供初步参考，不作为医学诊断标准。');

-- 插入几道样例题目
INSERT INTO `assessment_questions` (`id`, `assessment_id`, `content`, `sort_order`) VALUES 
(1, 1, '我觉得平常的情况很好', 1),
(2, 1, '我无缘无故感到疲乏', 2),
(3, 1, '我平时的头脑像往常一样清楚', 3);

-- 插入题目的对应选项（简化逻辑：正向或反向计分）
-- 题1: 正向情绪题 (越少越容易抑郁，反向计分)
INSERT INTO `assessment_options` (`question_id`, `content`, `score`, `sort_order`) VALUES 
(1, '没有或很少时间', 4, 1),
(1, '小部分时间', 3, 2),
(1, '相当多时间', 2, 3),
(1, '绝大部分或全部时间', 1, 4);

-- 题2: 负向情绪题 (正向计分)
INSERT INTO `assessment_options` (`question_id`, `content`, `score`, `sort_order`) VALUES 
(2, '没有或很少时间', 1, 1),
(2, '小部分时间', 2, 2),
(2, '相当多时间', 3, 3),
(2, '绝大部分或全部时间', 4, 4);

-- 题3: 正向情绪题 (反向计分)
INSERT INTO `assessment_options` (`question_id`, `content`, `score`, `sort_order`) VALUES 
(3, '没有或很少时间', 4, 1),
(3, '小部分时间', 3, 2),
(3, '相当多时间', 2, 3),
(3, '绝大部分或全部时间', 1, 4);
