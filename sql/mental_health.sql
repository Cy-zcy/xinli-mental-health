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

 Date: 19/07/2025 17:51:36
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
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admins
-- ----------------------------
INSERT INTO `admins` VALUES (1, 'admin', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '系统管理员', 1, '2025-07-17 09:15:41', '2025-07-17 13:38:05');

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
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

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

-- ----------------------------
-- Table structure for chat_sessions
-- ----------------------------
DROP TABLE IF EXISTS `chat_sessions`;
CREATE TABLE `chat_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '新的对话' COMMENT '会话标题',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_sessions
-- ----------------------------
INSERT INTO `chat_sessions` VALUES (1, 22, 'Test Chat Session', 0, '2025-07-17 18:52:02', '2025-07-17 18:52:13');
INSERT INTO `chat_sessions` VALUES (2, 22, 'Test Chat Session', 0, '2025-07-18 19:04:03', '2025-07-18 19:04:04');
INSERT INTO `chat_sessions` VALUES (3, 22, 'Test Chat Session', 0, '2025-07-18 19:06:02', '2025-07-18 19:06:02');
INSERT INTO `chat_sessions` VALUES (4, 22, 'Test Chat Session', 0, '2025-07-18 19:08:30', '2025-07-18 19:08:44');
INSERT INTO `chat_sessions` VALUES (5, 26, '新对话 2025/7/19 14:34:40', 1, '2025-07-19 14:34:41', '2025-07-19 14:39:17');

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛评论表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_likes
-- ----------------------------
INSERT INTO `post_likes` VALUES (5, 26, 14, '2025-07-19 14:37:56');

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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `idx_users_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_users_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '温暖的小太阳', 'avatar1.jpg', 1, '2025-07-17 09:15:50', '2025-07-17 09:15:50');
INSERT INTO `users` VALUES (2, '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '心灵港湾', 'avatar2.jpg', 1, '2025-07-17 09:15:50', '2025-07-17 09:15:50');
INSERT INTO `users` VALUES (3, '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '阳光少年', 'avatar3.jpg', 1, '2025-07-17 09:15:50', '2025-07-17 09:15:50');
INSERT INTO `users` VALUES (14, '13800138003', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小李', 'https://via.placeholder.com/100x100?text=小李', 0, '2025-07-17 14:10:21', '2025-07-17 14:10:21');
INSERT INTO `users` VALUES (15, '13800138004', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小王', 'https://via.placeholder.com/100x100?text=小王', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21');
INSERT INTO `users` VALUES (16, '13800138005', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小张', 'https://via.placeholder.com/100x100?text=小张', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21');
INSERT INTO `users` VALUES (17, '13800138006', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小刘', 'https://via.placeholder.com/100x100?text=小刘', 0, '2025-07-17 14:10:21', '2025-07-17 14:10:21');
INSERT INTO `users` VALUES (18, '13800138007', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小陈', 'https://via.placeholder.com/100x100?text=小陈', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21');
INSERT INTO `users` VALUES (19, '13800138008', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小赵', 'https://via.placeholder.com/100x100?text=小赵', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21');
INSERT INTO `users` VALUES (20, '13800138009', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小孙', 'https://via.placeholder.com/100x100?text=小孙', 1, '2025-07-17 14:10:21', '2025-07-17 14:10:21');
INSERT INTO `users` VALUES (21, '13800138010', '$2a$10$ZtTrOTBcXrDfHDzHraQmYurpOZ7Hil6yQzuAKB7DnGeKuDKazwP2G', '小周', 'https://via.placeholder.com/100x100?text=小周', 0, '2025-07-17 14:10:21', '2025-07-17 14:10:21');
INSERT INTO `users` VALUES (22, '13812345678', '$2a$10$nC7NAi417Qsdo2JL7k8SqOizrx4ux8GC0EFNbVafUEE.ApV46cQq.', 'TestUser', NULL, 1, '2025-07-17 18:27:46', '2025-07-17 18:27:46');
INSERT INTO `users` VALUES (23, '13907171831', '$2a$10$tKbXb7QMuwSHwZeeiKRSO.3dFHi1oLbdu3oXtfhQKD8DAz65096i6', 'New07171831', 'https://example.com/new-avatar.jpg', 1, '2025-07-17 18:31:04', '2025-07-17 18:31:04');
INSERT INTO `users` VALUES (24, '13907181903', '$2a$10$Rytu1OZMRlLSYdMIAPpBA.5WiImIJAIpR.43vHa6ErfDy1CcuPB/i', 'New07181903', 'https://example.com/new-avatar.jpg', 1, '2025-07-18 19:03:25', '2025-07-18 19:03:26');
INSERT INTO `users` VALUES (25, '13807181904', '$2a$10$pBQdu3sAmItMiDkrKlFMjOSN9iSPhDK1r2fle7iwvDyKGw6DZgrtq', 'Test07181904', 'https://example.com/avatar.jpg', 1, '2025-07-18 19:04:36', '2025-07-18 19:04:36');
INSERT INTO `users` VALUES (26, '13811111111', '$2a$10$AlJYD9ETN0Iprd6J6vlSP.MWnTRuz7o1N6r2Dxp22qMDwesHIoaxC', 'cy', 'avatars/avatar_26_20250719154343_5825378d.jpg', 1, '2025-07-19 14:33:22', '2025-07-19 15:54:16');

SET FOREIGN_KEY_CHECKS = 1;
