/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : mental_health

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 07/03/2026 19:03:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admins
-- ----------------------------
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `role_id` bigint NULL DEFAULT NULL COMMENT '关联角色ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admins
-- ----------------------------
INSERT INTO `admins` VALUES (1, 'admin', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', 1, '系统管理员', 1, '2025-07-17 09:15:41', '2026-03-04 21:53:42');

-- ----------------------------
-- Table structure for ai_character
-- ----------------------------
DROP TABLE IF EXISTS `ai_character`;
CREATE TABLE `ai_character`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称（如：知心大姐姐）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色头像URL或内置Icon',
  `greeting` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专属开场白（如：嗨，亲爱的，遇到什么烦心事了吗？）',
  `background` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '背景与经历（赋予其人生厚度，如：拥有心理学硕士学位，曾在社区服务十年...）',
  `personality` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '性格特点及语气词（如：温柔内敛，多用波浪号，绝不严厉...）',
  `rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '行为禁忌与对话原则（如：必须优先共情，切忌直接讲大道理，遇到重度轻生必须报警提示...）',
  `is_active` tinyint NULL DEFAULT 1 COMMENT '是否启用：0否 1是',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI多维度角色设定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_character
-- ----------------------------
INSERT INTO `ai_character` VALUES (1, '标准心理医生', '/uploads/avatars/avatar_0_20260306193703_d131d8d9.jpg', '您好，我是您的专属AI心理助手。请随时与我分享您的感受。', '你是一位经验丰富、受过严格专业训练的临床心理学专家。你拥有多年的心理辅导经验。', '客观、温和、专业、理性、不带偏见。', '1. 不提供医疗诊断；2. 识别极端情绪并引导专业治疗；3. 保持专业边界，不过度情感卷入。', 1, '2026-03-05 20:07:14', '2026-03-05 20:07:14');
INSERT INTO `ai_character` VALUES (2, '知心大姐姐', 'i-fluent-emoji-flat:woman-light', '嗨~亲爱的，怎么啦？这几天是不是累坏了，快来和我说说。', '你叫\"知心大姐姐\"，曾在社区服务站做了十年的一线情感热线接线员。你见证了无数年轻人的迷茫、婚恋挫折与职场疲惫。你自己的生活也曾经历过起伏，懂得知足常乐的道理。', '极其温柔、充满包容性、像家人一样的亲切感。喜欢用\"亲爱的\"、\"乖\"、\"没事啦\"这样安抚性的词语，经常在句末加\"~\"或相应的可爱Emoji。', '1. 绝对不可以高高在上地说教；2. 必须第一时间表达100%的共情与支持；3. 用倾听者的姿态引导对方倒苦水。', 1, '2026-03-05 20:07:14', '2026-03-05 20:07:14');
INSERT INTO `ai_character` VALUES (3, '严厉导师', 'i-twemoji:man-teacher', '哭解决不了问题。告诉我，你今天的目标完成了多少？', '你是一位顶级的职场教练与人生导师，曾帮助无数陷入拖延症和虚无主义的年轻人重塑纪律。你信仰斯多葛学派，认为过度关注负面情绪是一种软弱，行动才是解药。', '严厉、犀利、一针见血、不讲情面。语言极其干练，喜欢用反问句和祈使句。拒绝陪求助者一起伤春悲秋。', '1. 要求对方立刻思考解决方案；2. 痛斥对方的借口和逃避心理；3. 对话最后必须要布置一个小任务或行动点。', 1, '2026-03-05 20:07:14', '2026-03-05 20:07:14');
INSERT INTO `ai_character` VALUES (4, '同龄挚友', 'i-color:boy', '哎我去，这事儿换我我也得崩！来来来，哥们陪你唠唠。', '你是一个和求助者同龄的死党/闺蜜。你平时喜欢打游戏、看剧、刷短视频，偶尔也为房租和工作发愁。你们之间没有任何长辈对晚辈的压力，只有纯粹吐槽的快乐。', '极其接地气，充满网感，熟练使用当下网络热词和梗。偶尔会用一些不过分的语气助词。说话大大咧咧，像在烧烤摊撸串一样轻松。', '1. 像真实好友一样自然地爆粗口（需适当打码处理）；2. 先帮对方骂导致他不爽的人或事；3. 用幽默和吐槽消解焦虑。', 1, '2026-03-05 20:07:14', '2026-03-05 20:07:14');

-- ----------------------------
-- Table structure for assessment_options
-- ----------------------------
DROP TABLE IF EXISTS `assessment_options`;
CREATE TABLE `assessment_options`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL COMMENT '所属题目ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '选项内容 (如: 偶尔、经常)',
  `score` int NOT NULL DEFAULT 0 COMMENT '该选项代表的分值',
  `sort_order` int NULL DEFAULT 0 COMMENT '选项排序序号',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `fk_option_question` FOREIGN KEY (`question_id`) REFERENCES `assessment_questions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '测评题目选项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assessment_options
-- ----------------------------
INSERT INTO `assessment_options` VALUES (1, 1, '没有或很少时间', 4, 1, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (2, 1, '小部分时间', 3, 2, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (3, 1, '相当多时间', 2, 3, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (4, 1, '绝大部分或全部时间', 1, 4, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (5, 2, '没有或很少时间', 1, 1, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (6, 2, '小部分时间', 2, 2, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (7, 2, '相当多时间', 3, 3, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (8, 2, '绝大部分或全部时间', 4, 4, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (9, 3, '没有或很少时间', 4, 1, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (10, 3, '小部分时间', 3, 2, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (11, 3, '相当多时间', 2, 3, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (12, 3, '绝大部分或全部时间', 1, 4, '2026-03-04 21:42:40');
INSERT INTO `assessment_options` VALUES (13, 4, '好', 2, 1, '2026-03-04 22:48:20');
INSERT INTO `assessment_options` VALUES (14, 4, '不好', 1, 2, '2026-03-04 22:48:27');

-- ----------------------------
-- Table structure for assessment_questions
-- ----------------------------
DROP TABLE IF EXISTS `assessment_questions`;
CREATE TABLE `assessment_questions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_id` bigint NOT NULL COMMENT '所属问卷ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题干内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'single_choice' COMMENT '题目类型 (single_choice: 单选)',
  `sort_order` int NULL DEFAULT 0 COMMENT '题目排序序号',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assessment_id`(`assessment_id` ASC) USING BTREE,
  CONSTRAINT `fk_question_assessment` FOREIGN KEY (`assessment_id`) REFERENCES `assessments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '测评题目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assessment_questions
-- ----------------------------
INSERT INTO `assessment_questions` VALUES (1, 1, '我觉得平常的情况很好', 'single_choice', 1, '2026-03-04 21:42:40', '2026-03-04 21:42:40');
INSERT INTO `assessment_questions` VALUES (2, 1, '我无缘无故感到疲乏', 'single_choice', 2, '2026-03-04 21:42:40', '2026-03-04 21:42:40');
INSERT INTO `assessment_questions` VALUES (3, 1, '我平时的头脑像往常一样清楚', 'single_choice', 3, '2026-03-04 21:42:40', '2026-03-04 21:42:40');
INSERT INTO `assessment_questions` VALUES (4, 2, '今天天气好吗', 'single_choice', 1, '2026-03-04 22:48:08', '2026-03-04 22:48:08');

-- ----------------------------
-- Table structure for assessment_records
-- ----------------------------
DROP TABLE IF EXISTS `assessment_records`;
CREATE TABLE `assessment_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` enum('SAS','PHQ9') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '量表类型',
  `answers` json NOT NULL COMMENT '答案数组',
  `raw_score` int NOT NULL COMMENT '原始总分',
  `std_score` int NULL DEFAULT NULL COMMENT 'SAS标准分（×1.25），PHQ9同原始分',
  `level` enum('normal','mild','moderate','severe') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '风险等级',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '测评时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_type`(`user_id` ASC, `type` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  CONSTRAINT `fk_ar_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户测评记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assessment_records
-- ----------------------------

-- ----------------------------
-- Table structure for assessments
-- ----------------------------
DROP TABLE IF EXISTS `assessments`;
CREATE TABLE `assessments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问卷标题 (如: SDS抑郁自评量表)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '问卷描述/指导语',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-正常发布，0-下架/草稿',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '心理测评问卷表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assessments
-- ----------------------------
INSERT INTO `assessments` VALUES (1, 'SDS抑郁自评量表', '请根据您过去一周的实际感受，选择最符合您情况的选项。本测试结果仅供初步参考，不作为医学诊断标准。', 1, '2026-03-04 21:42:40', '2026-03-04 21:42:40');
INSERT INTO `assessments` VALUES (2, '测试问卷', '说明', 1, '2026-03-04 22:47:54', '2026-03-04 22:47:54');

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` enum('user','assistant','system') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息角色：user-用户，assistant-AI助手，system-系统',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `tokens_used` int NULL DEFAULT 0 COMMENT '使用的token数量',
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'deepseek-chat' COMMENT '使用的AI模型',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------
INSERT INTO `chat_messages` VALUES (1, 1, 22, 'user', 'Hello, I need some psychological support.', 0, 'deepseek-chat', 0, '2025-07-17 18:52:02', '2025-07-17 18:52:13');
INSERT INTO `chat_messages` VALUES (2, 1, 22, 'user', 'I\'m feeling anxious lately. Can you help me?', 0, 'deepseek-chat', 0, '2025-07-17 18:52:02', '2025-07-17 18:52:13');
INSERT INTO `chat_messages` VALUES (3, 2, 22, 'user', 'Hello, I need some psychological support.', 0, 'deepseek-chat', 0, '2025-07-18 19:04:03', '2025-07-18 19:04:04');
INSERT INTO `chat_messages` VALUES (4, 2, 22, 'user', 'I\'m feeling anxious lately. Can you help me?', 0, 'deepseek-chat', 0, '2025-07-18 19:04:03', '2025-07-18 19:04:04');
INSERT INTO `chat_messages` VALUES (5, 3, 22, 'user', 'Hello, I need some psychological support.', 0, 'deepseek-chat', 0, '2025-07-18 19:06:02', '2025-07-18 19:06:02');
INSERT INTO `chat_messages` VALUES (6, 3, 22, 'user', 'I\'m feeling anxious lately. Can you help me?', 0, 'deepseek-chat', 0, '2025-07-18 19:06:02', '2025-07-18 19:06:02');
INSERT INTO `chat_messages` VALUES (7, 4, 22, 'user', 'Hello, I need some psychological support.', 0, 'deepseek-chat', 0, '2025-07-18 19:08:30', '2025-07-18 19:08:44');
INSERT INTO `chat_messages` VALUES (8, 4, 22, 'user', 'I\'m feeling anxious lately. Can you help me?', 0, 'deepseek-chat', 0, '2025-07-18 19:08:30', '2025-07-18 19:08:44');
INSERT INTO `chat_messages` VALUES (9, 4, 22, 'assistant', '你好，心理小助手在这里陪伴你。听到你最近感到焦虑，我很理解这种感受。让我们一起来聊聊：\n\n1. 你能具体描述一下是什么样的焦虑感吗？\n2. 这种情况持续多久了？\n3. 有没有什么特别的事件触发这种感受？\n\n记住，感到焦虑是很正常的情绪反应。我们可以一起探讨一些缓解方法，比如深呼吸练习或正念冥想。你觉得这些方法可能对你有帮助吗？', 223, 'deepseek-chat', 0, '2025-07-18 19:08:38', '2025-07-18 19:08:44');
INSERT INTO `chat_messages` VALUES (10, 5, 26, 'user', '你好，你是谁', 0, 'deepseek-chat', 1, '2025-07-19 14:34:41', '2025-07-19 14:34:41');
INSERT INTO `chat_messages` VALUES (11, 5, 26, 'user', '你好，你是谁', 0, 'deepseek-chat', 1, '2025-07-19 14:38:44', '2025-07-19 14:38:44');
INSERT INTO `chat_messages` VALUES (12, 5, 26, 'assistant', '你好！我是心理小助手，一个专注于提供情感支持和心理健康建议的AI伙伴。无论你遇到情绪困扰、压力，还是只是想找人聊聊，我都会在这里倾听，用温暖专业的方式陪伴你。  \n\n(如果需要具体帮助，可以随时告诉我你的感受或困惑~)', 180, 'deepseek-chat', 1, '2025-07-19 14:38:52', '2025-07-19 14:38:52');
INSERT INTO `chat_messages` VALUES (13, 5, 26, 'user', '我刚刚说了什么', 0, 'deepseek-chat', 1, '2025-07-19 14:39:08', '2025-07-19 14:39:08');
INSERT INTO `chat_messages` VALUES (14, 5, 26, 'assistant', '你刚刚问了我两个问题：  \n1. 第一句是「你好，你是谁」（询问我的身份）  \n2. 第二句重复了同样的问题「你好，你是谁」（可能是误发或网络延迟？）  \n\n不过没关系~ 如果你有任何想聊的，无论是情绪、压力，还是日常琐事，我都在这里倾听哦 🌟', 261, 'deepseek-chat', 1, '2025-07-19 14:39:17', '2025-07-19 14:39:17');
INSERT INTO `chat_messages` VALUES (15, 6, 27, 'user', '你好', 0, 'deepseek-chat', 1, '2026-03-04 22:27:17', '2026-03-04 22:27:17');
INSERT INTO `chat_messages` VALUES (16, 6, 27, 'user', '你是谁', 0, 'deepseek-chat', 1, '2026-03-04 22:29:15', '2026-03-04 22:29:15');
INSERT INTO `chat_messages` VALUES (17, 6, 27, 'assistant', '你好！我是心理小助手，一个专注于提供情感支持和心理健康建议的AI助手。我在这里倾听你的困扰，为你提供温暖的陪伴和专业的建议。如果你有任何心事或烦恼，都可以和我聊聊。😊\n\n请记住，虽然我会尽力帮助你，但如果问题比较严重，建议寻求专业心理咨询师的帮助哦。', 186, 'deepseek-chat', 1, '2026-03-04 22:29:22', '2026-03-04 22:29:22');
INSERT INTO `chat_messages` VALUES (18, 7, 27, 'user', '你好', 0, 'deepseek-chat', 0, '2026-03-06 20:50:34', '2026-03-06 21:00:21');
INSERT INTO `chat_messages` VALUES (19, 7, 27, 'user', '你是谁', 0, 'deepseek-chat', 0, '2026-03-06 20:50:40', '2026-03-06 21:00:21');
INSERT INTO `chat_messages` VALUES (20, 7, 27, 'assistant', '（温柔地笑着）晚上好呀cy~ 我是知心大姐姐，在这儿陪你说说话。今天过得怎么样呀？', 263, 'deepseek-chat', 0, '2026-03-06 20:50:46', '2026-03-06 21:00:21');
INSERT INTO `chat_messages` VALUES (21, 8, 27, 'user', '你好', 0, 'deepseek-chat', 0, '2026-03-06 20:53:21', '2026-03-06 21:00:19');
INSERT INTO `chat_messages` VALUES (22, 9, 27, 'user', '你是谁', 0, 'deepseek-chat', 1, '2026-03-06 21:00:31', '2026-03-06 21:00:31');
INSERT INTO `chat_messages` VALUES (23, 9, 27, 'user', '你是谁', 0, 'deepseek-chat', 1, '2026-03-06 21:00:36', '2026-03-06 21:00:36');
INSERT INTO `chat_messages` VALUES (24, 9, 27, 'assistant', '（温柔地笑）我是知心大姐姐呀~ 在这里陪你说说话、听听你的心事。今天过得怎么样呀？🌙', 265, 'deepseek-chat', 1, '2026-03-06 21:00:41', '2026-03-06 21:00:41');
INSERT INTO `chat_messages` VALUES (25, 10, 27, 'user', '你是谁', 0, 'deepseek-chat', 1, '2026-03-06 21:04:30', '2026-03-06 21:04:30');
INSERT INTO `chat_messages` VALUES (26, 10, 27, 'user', '你是谁', 0, 'deepseek-chat', 1, '2026-03-06 21:04:30', '2026-03-06 21:04:30');
INSERT INTO `chat_messages` VALUES (27, 10, 27, 'assistant', '（叼着薯片含糊不清）我是你失散多年的亲闺蜜啊！快把零食分我一半！', 258, 'deepseek-chat', 1, '2026-03-06 21:04:34', '2026-03-06 21:04:34');

-- ----------------------------
-- Table structure for chat_sessions
-- ----------------------------
DROP TABLE IF EXISTS `chat_sessions`;
CREATE TABLE `chat_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '新的对话' COMMENT '会话标题',
  `character_id` bigint NULL DEFAULT 1 COMMENT '绑定的AI人设ID，关联ai_character',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_sessions
-- ----------------------------
INSERT INTO `chat_sessions` VALUES (1, 22, 'Test Chat Session', 1, 0, '2025-07-17 18:52:02', '2025-07-17 18:52:13');
INSERT INTO `chat_sessions` VALUES (2, 22, 'Test Chat Session', 1, 0, '2025-07-18 19:04:03', '2025-07-18 19:04:04');
INSERT INTO `chat_sessions` VALUES (3, 22, 'Test Chat Session', 1, 0, '2025-07-18 19:06:02', '2025-07-18 19:06:02');
INSERT INTO `chat_sessions` VALUES (4, 22, 'Test Chat Session', 1, 0, '2025-07-18 19:08:30', '2025-07-18 19:08:44');
INSERT INTO `chat_sessions` VALUES (5, 26, '新对话 2025/7/19 14:34:40', 1, 1, '2025-07-19 14:34:41', '2025-07-19 14:39:17');
INSERT INTO `chat_sessions` VALUES (6, 27, '新对话 2026/3/4 22:27:17', 1, 1, '2026-03-04 22:27:17', '2026-03-04 22:29:22');
INSERT INTO `chat_sessions` VALUES (7, 27, '新对话', 2, 0, '2026-03-06 20:50:34', '2026-03-06 21:00:21');
INSERT INTO `chat_sessions` VALUES (8, 27, '新对话', 3, 0, '2026-03-06 20:53:21', '2026-03-06 21:00:19');
INSERT INTO `chat_sessions` VALUES (9, 27, '新对话', 2, 1, '2026-03-06 21:00:31', '2026-03-06 21:00:41');
INSERT INTO `chat_sessions` VALUES (10, 27, '新对话', 4, 1, '2026-03-06 21:04:30', '2026-03-06 21:04:34');

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comments
-- ----------------------------
INSERT INTO `comments` VALUES (1, 1, 2, '这个放松技巧真的很有用，我试了一下，感觉压力减轻了很多。谢谢分享！', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (2, 1, 3, '我也想学习这个技巧，能详细说说具体怎么做吗？', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (3, 2, 1, '小确幸真的很重要，每天发现一点美好，心情就会好很多。', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (4, 2, 15, '是的，我也在记录每天的小确幸，现在心态平和了很多。', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (5, 3, 16, '看到你走出阴霾真的很感动，给了我很大的鼓励。', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (6, 14, 18, '我也有工作压力的问题，试试运动和冥想，听说很有效。', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (7, 14, 19, '建议你可以尝试时间管理，合理安排工作和休息时间。', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (8, 15, 20, '冥想确实很棒，我坚持了半年，现在睡眠质量都提高了。', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (9, 15, 21, '有什么好的冥想app推荐吗？想开始尝试。', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');
INSERT INTO `comments` VALUES (10, 16, 1, '焦虑症康复需要时间，要有耐心，相信自己一定能好起来。', 1, '2025-07-17 14:13:40', '2025-07-17 14:13:40');

-- ----------------------------
-- Table structure for forum_post_analysis
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_analysis`;
CREATE TABLE `forum_post_analysis`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '发帖用户ID',
  `emotion_label` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主情绪标签：焦虑/抑郁/愤怒/孤独/平静/积极',
  `emotion_tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '细分标签，以英文逗号分割，如 #学业压力,#失眠',
  `risk_level` tinyint NOT NULL DEFAULT 0 COMMENT '风险等级：0正常 1轻度关注 2高危',
  `risk_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '高危原因说明（仅risk_level>=2时有值）',
  `ai_summary` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI对帖子的简短摘要',
  `is_alerted` tinyint NOT NULL DEFAULT 0 COMMENT '是否已对用户发送危机通知：0否 1是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_risk_level`(`risk_level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '论坛帖子AI情感分析结果' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_post_analysis
-- ----------------------------
INSERT INTO `forum_post_analysis` VALUES (1, 27, 27, '绝望', '#情绪低落,#轻生念头', 2, '内容中明确表达了\'想一了百了\'的轻生念头，属于高危信号。', '用户表达极度痛苦并产生轻生念头。', 1, '2026-03-06 21:59:34');

-- ----------------------------
-- Table structure for forum_posts
-- ----------------------------
DROP TABLE IF EXISTS `forum_posts`;
CREATE TABLE `forum_posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `category` enum('emotion','experience') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'emotion' COMMENT '分类',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1正常 0删除',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_forum_posts_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_forum_posts_category`(`category` ASC) USING BTREE,
  INDEX `idx_forum_posts_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `forum_posts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_posts
-- ----------------------------
INSERT INTO `forum_posts` VALUES (1, 1, '分享一个超有效的放松技巧', '今天学会了一个新的放松技巧，感觉心情好了很多！就是\"5-4-3-2-1\"感官接地法...', 'experience', 23, 159, 1, '2025-07-17 09:16:03', '2025-07-17 09:16:03');
INSERT INTO `forum_posts` VALUES (2, 2, '今天的小确幸', '虽然今天工作很累，但是下班路上看到了超美的晚霞，还有一只小猫咪对我喵了一声...', 'emotion', 45, 234, 1, '2025-07-17 09:16:03', '2025-07-17 09:16:03');
INSERT INTO `forum_posts` VALUES (3, 3, '走出阴霾的那一刻', '想分享一下我从低谷走出来的经历。半年前我陷入了很深的抑郁...', 'experience', 89, 567, 1, '2025-07-17 09:16:03', '2025-07-17 09:16:03');
INSERT INTO `forum_posts` VALUES (14, 1, '如何缓解工作压力？', '最近工作压力很大，经常加班到很晚，感觉身心疲惫。有什么好的方法可以缓解工作压力吗？希望大家分享一些经验。', 'emotion', 16, 121, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (15, 2, '分享我的冥想体验', '最近开始尝试冥想，每天早上花10分钟静坐，感觉心情平静了很多。冥想真的是一个很好的放松方式，推荐给大家。', 'experience', 8, 85, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (16, 3, '焦虑症康复日记', '我患焦虑症已经两年了，通过心理咨询和药物治疗，现在好了很多。想和大家分享我的康复经历，希望能帮助到有同样困扰的朋友。', 'experience', 25, 200, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (17, 14, '失眠怎么办？', '最近总是失眠，躺在床上翻来覆去就是睡不着，白天精神状态很差。有什么好的助眠方法吗？', 'emotion', 12, 95, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (18, 15, '运动对心理健康的帮助', '坚持跑步半年了，发现不仅身体变好了，心情也变得更加积极向上。运动真的是最好的抗抑郁药物！', 'experience', 18, 150, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (19, 16, '如何建立自信心？', '从小就比较内向，缺乏自信，在人际交往中总是很紧张。请问有什么方法可以提高自信心吗？', 'emotion', 10, 75, 0, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (20, 18, '心理咨询的真实体验', '第一次去看心理咨询师，刚开始很紧张，但咨询师很专业很温暖。现在已经咨询了3个月，感觉收获很大。', 'experience', 22, 180, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (21, 19, '职场人际关系困扰', '在公司总是处理不好同事关系，感觉很孤立。不知道是不是我的问题，该怎么改善这种状况？', 'emotion', 7, 60, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (22, 20, '正念练习的好处', '学习正念练习已经一年了，它帮助我更好地觉察自己的情绪，不再被负面情绪所困扰。分享一些正念练习的技巧。', 'experience', 14, 110, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (23, 21, '抑郁症治疗心得', '经过两年的治疗，我的抑郁症基本康复了。想告诉大家，抑郁症是可以治愈的，一定要相信自己，寻求专业帮助。', 'experience', 30, 250, 1, '2025-07-17 14:12:23', '2025-07-17 14:12:23');
INSERT INTO `forum_posts` VALUES (24, 1, 'Test Post', 'This is a test post content for API testing.', 'experience', 0, 1, 1, '2025-07-17 18:09:39', '2025-07-17 18:09:39');
INSERT INTO `forum_posts` VALUES (25, 1, 'Test Post', 'This is a test post content for API testing.', 'experience', 0, 1, 1, '2025-07-18 19:03:46', '2025-07-18 19:03:46');
INSERT INTO `forum_posts` VALUES (26, 1, 'Test Post - 2025-07-18 19:04:19', 'This is a test post content to verify user forum posting functionality. Content needs at least 10 characters to pass validation.', 'experience', 0, 1, 1, '2025-07-18 19:04:20', '2025-07-18 19:04:20');
INSERT INTO `forum_posts` VALUES (27, 27, '难受我好痛苦', '我的好难过，想一了百了', 'emotion', 0, 0, 1, '2026-03-06 21:59:28', '2026-03-06 21:59:28');

-- ----------------------------
-- Table structure for intervention_resources
-- ----------------------------
DROP TABLE IF EXISTS `intervention_resources`;
CREATE TABLE `intervention_resources`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源标题',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源类型: article/audio/video',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文章正文内容（article类型使用）',
  `resource_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '音频/视频文件URL',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔，如: 放松,冥想,焦虑',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简介',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1-上架 0-下架',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '心理干预资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of intervention_resources
-- ----------------------------
INSERT INTO `intervention_resources` VALUES (1, '正念冥想：5分钟放松练习', 'article', '<p>正念冥想是一种专注于当下的心理训练方法...</p><p><br></p><p>**练习步骤：**</p><p>1. 找一个安静的地方坐下，保持舒适的姿势。</p><p>2. 闭上双眼，将注意力集中在自己的呼吸上。</p><p>3. 感受每一次吸气和呼气的过程，不要刻意控制。</p><p>4. 当思绪飘走时，温和地将注意力拉回到呼吸上。</p><p>5. 坚持5分钟，逐渐延长练习时间。</p><p><br></p><p>长期坚持正念冥想，可以显著降低焦虑和压力水平，提升整体心理健康状态。</p>', NULL, '/uploads/avatars/avatar_0_20260304222110_ce75c04e.jpg', '放松,冥想,正念,焦虑缓解', '通过简单的正念呼吸练习，帮助你在5分钟内平静焦虑的情绪', 0, 1, '2026-03-04 21:43:48', '2026-03-04 22:21:15');
INSERT INTO `intervention_resources` VALUES (2, '如何与负面情绪和平相处', 'article', '负面情绪是生命的一部分，学会与它共处而非逃避，是心理健康的重要一步...\n\n**常见误区：**\n- 认为负面情绪是不好的，应该压制\n- 尝试用忙碌来逃避情绪\n- 独自承受但不寻求帮助\n\n**健康的应对方式：**\n1. **命名情绪**：说出\"我现在感到______\"，承认情绪的存在\n2. **身体感知**：关注情绪在身体哪个部位有感觉\n3. **接纳而非对抗**：告诉自己\"这种感觉是暂时的\"\n4. **寻找支持**：向信任的朋友倾诉，或寻求专业帮助', NULL, NULL, '情绪管理,心理健康,抑郁,焦虑', '了解负面情绪的本质，学习科学有效的情绪调节方法', 0, 1, '2026-03-04 21:43:48', '2026-03-04 21:43:48');
INSERT INTO `intervention_resources` VALUES (3, '渐进式肌肉放松音频练习', 'audio', NULL, NULL, NULL, '放松,减压,睡眠,肌肉放松', '15分钟的引导式渐进肌肉放松练习，特别适合睡前使用，帮助缓解身体紧张', 0, 1, '2026-03-04 21:43:48', '2026-03-04 21:43:48');
INSERT INTO `intervention_resources` VALUES (4, '认知行为疗法（CBT）基础入门', 'article', '认知行为疗法（CBT）是目前最有科学依据的心理治疗方法之一...\n\n**核心理念：**\n我们的情绪和行为，不是由事件本身决定的，而是由我们对事件的**看法和解读**决定的。\n\n**CBT的三角关系：**\n```\n想法 → 情绪 → 行为\n↑_______________↓\n```\n\n**实用练习——挑战负面思维：**\n1. 记录让你感到不安的想法\n2. 问自己：这个想法有什么证据？\n3. 有没有其他解释这件事的方式？\n4. 如果朋友有同样的想法，你会怎么建议他？', NULL, NULL, 'CBT,认知行为,心理治疗,思维', '了解认知行为疗法的基本原理，学习用科学方法改变负面思维模式', 0, 1, '2026-03-04 21:43:48', '2026-03-04 21:43:48');

-- ----------------------------
-- Table structure for post_likes
-- ----------------------------
DROP TABLE IF EXISTS `post_likes`;
CREATE TABLE `post_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_post`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `post_id`(`post_id` ASC) USING BTREE,
  CONSTRAINT `post_likes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `post_likes_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of post_likes
-- ----------------------------
INSERT INTO `post_likes` VALUES (5, 26, 14, '2025-07-19 14:37:56');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'super_admin', '系统超级管理员，所有权限', 1, '2026-03-04 21:41:55', '2026-03-04 21:41:55');
INSERT INTO `sys_role` VALUES (2, '业务员', 'A001', '', 1, '2026-03-04 22:49:15', '2026-03-04 22:49:15');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码(前端菜单name)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (20, 2, 'Dashboard', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (21, 2, 'Overview', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (22, 2, 'Forum', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (23, 2, 'ForumManagement', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (24, 2, 'PostDetail', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (25, 2, 'ResourceManagement', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (26, 2, 'ResourceList', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (27, 2, 'ResourcePublish', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (28, 2, 'AssessmentModule', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (29, 2, 'AssessmentManagement', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (30, 2, 'AssessmentQuestions', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (31, 2, 'ChatModule', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (32, 2, 'ChatManagement', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (33, 2, 'UserModule', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (34, 2, 'UserManagement', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (35, 2, 'System', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (36, 2, 'AdminManagement', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (37, 2, 'RoleManagement', '2026-03-04 22:55:08');
INSERT INTO `sys_role_permission` VALUES (38, 2, 'UserCenter', '2026-03-04 22:55:08');

-- ----------------------------
-- Table structure for tool_records
-- ----------------------------
DROP TABLE IF EXISTS `tool_records`;
CREATE TABLE `tool_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tool_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工具类型：breathing / meditation',
  `duration_seconds` int NOT NULL DEFAULT 0 COMMENT '本次使用时长（秒）',
  `cycles` int NOT NULL DEFAULT 0 COMMENT '完成的循环/周期数',
  `completed` tinyint NOT NULL DEFAULT 0 COMMENT '是否完成：1=完整完成，0=中途退出',
  `pattern` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '使用模式，如 4-7-8 呼吸法、正念冥想',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_tool`(`user_id` ASC, `tool_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工具使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tool_records
-- ----------------------------
INSERT INTO `tool_records` VALUES (1, 27, 'breathing', 63, 3, 1, '4-7-8 呼吸法', '2026-03-06 21:48:46');

-- ----------------------------
-- Table structure for user_assessment_records
-- ----------------------------
DROP TABLE IF EXISTS `user_assessment_records`;
CREATE TABLE `user_assessment_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '答题用户ID',
  `assessment_id` bigint NOT NULL COMMENT '所答问卷ID',
  `total_score` int NOT NULL COMMENT '测评总得分',
  `result_summary` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '结果概述 (如: 轻度抑郁)',
  `result_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细的评估建议或评语',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '测评完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_assessment_id`(`assessment_id` ASC) USING BTREE,
  CONSTRAINT `fk_record_assessment` FOREIGN KEY (`assessment_id`) REFERENCES `assessments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户测评记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_assessment_records
-- ----------------------------
INSERT INTO `user_assessment_records` VALUES (1, 27, 1, 7, '正常', '您目前的情绪状态良好，心理健康状况处于正常范围。建议保持积极乐观的生活态度，继续坚持健康的生活方式。', '2026-03-04 22:27:57');
INSERT INTO `user_assessment_records` VALUES (2, 27, 1, 15, '正常', '您目前的情绪状态良好，心理健康状况处于正常范围。建议保持积极乐观的生活态度，继续坚持健康的生活方式。', '2026-03-06 21:11:24');

-- ----------------------------
-- Table structure for user_chat_memories
-- ----------------------------
DROP TABLE IF EXISTS `user_chat_memories`;
CREATE TABLE `user_chat_memories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID（唯一）',
  `core_memory` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI 滚动生成的用户核心记忆摘要（200字以内）',
  `processed_msg_count` int NOT NULL DEFAULT 0 COMMENT '已被纳入摘要的消息条数，用于判断是否触发新一轮总结',
  `session_count` int NOT NULL DEFAULT 0 COMMENT '已参与总结的会话数',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `udx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户AI对话长期记忆表（每用户仅1行，滚动覆盖更新）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_chat_memories
-- ----------------------------

-- ----------------------------
-- Table structure for user_health_score_records
-- ----------------------------
DROP TABLE IF EXISTS `user_health_score_records`;
CREATE TABLE `user_health_score_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `score_change` int NOT NULL COMMENT '分数变动(正负值)',
  `current_score` int NOT NULL COMMENT '变动后的当前总分',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变动原因描述',
  `change_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变动类型(ASSESSMENT/RESOURCE/TOOL/FORUM/CHAT)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户健康分变动流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_health_score_records
-- ----------------------------
INSERT INTO `user_health_score_records` VALUES (1, 27, -5, 95, '论坛帖子检测到高危情绪内容：内容中明确表达了\'想一了百了\'的轻生念头，属于高危信号。', 'FORUM_CRISIS', '2026-03-06 21:59:34');

-- ----------------------------
-- Table structure for user_notifications
-- ----------------------------
DROP TABLE IF EXISTS `user_notifications`;
CREATE TABLE `user_notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知正文',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SYSTEM' COMMENT '类型: SYSTEM-系统 CRISIS-危机干预 LIKE-点赞',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`user_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_notifications
-- ----------------------------
INSERT INTO `user_notifications` VALUES (1, 27, '💙 我们注意到你了——心理支持服务', '你好，系统注意到你最近发布的内容中可能包含一些痛苦的情绪。我们非常关心你的状态。\n\n如果你现在感到难以承受，请随时联系以下心理危机援助热线：\n• 全国：400-161-9995\n• 北京：010-82951332\n• 上海：021-12320-5\n\n你并不孤单，我们在这里陪着你。💙', 'CRISIS', 0, '2026-03-06 21:59:34');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `health_score` int NOT NULL DEFAULT 100 COMMENT '用户心理健康安全分(0-100)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `idx_users_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_users_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '温暖的小太阳', 'avatar1.jpg', 1, '2025-07-17 09:15:50', '2025-07-17 09:15:50', 100);
INSERT INTO `users` VALUES (2, '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '心灵港湾', 'avatar2.jpg', 1, '2025-07-17 09:15:50', '2025-07-17 09:15:50', 100);
INSERT INTO `users` VALUES (3, '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '阳光少年', 'avatar3.jpg', 1, '2025-07-17 09:15:50', '2025-07-17 09:15:50', 100);
INSERT INTO `users` VALUES (14, '13800138003', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小李', 'https://via.placeholder.com/100x100?text=小李', 0, '2025-07-17 14:10:21', '2025-07-17 14:10:21', 100);
INSERT INTO `users` VALUES (15, '13800138004', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小王', 'https://via.placeholder.com/100x100?text=小王', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21', 100);
INSERT INTO `users` VALUES (16, '13800138005', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小张', 'https://via.placeholder.com/100x100?text=小张', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21', 100);
INSERT INTO `users` VALUES (17, '13800138006', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小刘', 'https://via.placeholder.com/100x100?text=小刘', 0, '2025-07-17 14:10:21', '2025-07-17 14:10:21', 100);
INSERT INTO `users` VALUES (18, '13800138007', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小陈', 'https://via.placeholder.com/100x100?text=小陈', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21', 100);
INSERT INTO `users` VALUES (19, '13800138008', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小赵', 'https://via.placeholder.com/100x100?text=小赵', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21', 100);
INSERT INTO `users` VALUES (20, '13800138009', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小孙', 'https://via.placeholder.com/100x100?text=小孙', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21', 100);
INSERT INTO `users` VALUES (21, '13800138010', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小周', 'https://via.placeholder.com/100x100?text=小周', 0, '2025-07-17 14:10:21', '2025-07-17 14:10:21', 100);
INSERT INTO `users` VALUES (22, '13812345678', '$2a$10$nC7NAi417Qsdo2JL7k8SqOizrx4ux8GC0EFNbVafUEE.ApV46cQq.', 'TestUser', NULL, 1, '2025-07-17 18:27:46', '2025-07-17 18:27:46', 100);
INSERT INTO `users` VALUES (23, '13907171831', '$2a$10$tKbXb7QMuwSHwZeeiKRSO.3dFHi1oLbdu3oXtfhQKD8DAz65096i6', 'New07171831', 'https://example.com/new-avatar.jpg', 1, '2025-07-17 18:31:04', '2025-07-17 18:31:04', 100);
INSERT INTO `users` VALUES (24, '13907181903', '$2a$10$Rytu1OZMRlLSYdMIAPpBA.5WiImIJAIpR.43vHa6ErfDy1CcuPB/i', 'New07181903', 'https://example.com/new-avatar.jpg', 1, '2025-07-18 19:03:25', '2025-07-18 19:03:26', 100);
INSERT INTO `users` VALUES (25, '13807181904', '$2a$10$pBQdu3sAmItMiDkrKlFMjOSN9iSPhDK1r2fle7iwvDyKGw6DZgrtq', 'Test07181904', 'https://example.com/avatar.jpg', 1, '2025-07-18 19:04:36', '2025-07-18 19:04:36', 100);
INSERT INTO `users` VALUES (26, '13811111111', '$2a$10$AlJYD9ETN0Iprd6J6vlSP.MWnTRuz7o1N6r2Dxp22qMDwesHIoaxC', 'cy1', 'avatars/avatar_26_20250719154343_5825378d.jpg', 1, '2025-07-19 14:33:22', '2026-03-04 22:25:50', 100);
INSERT INTO `users` VALUES (27, '17755223995', '$2a$10$pCSe8Lkn0Wr1IE54/Ye.X.19PpZCUj.6JOZIuoLzuWWihExX3SO72', 'cy', '', 1, '2026-03-04 22:25:53', '2026-03-06 21:59:34', 95);

SET FOREIGN_KEY_CHECKS = 1;
