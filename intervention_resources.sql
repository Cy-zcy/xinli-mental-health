-- ============================================================
-- 心理健康干预系统 - 心理干预资源模块建表脚本
-- 创建时间：2026-03-02
-- 分支：dev
-- ============================================================

USE xinli_mental;

-- 1. 心理干预资源表
CREATE TABLE IF NOT EXISTS `intervention_resources` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资源ID',
    `title`       VARCHAR(200) NOT NULL COMMENT '资源标题',
    `type`        VARCHAR(20)  NOT NULL COMMENT '资源类型: article/audio/video',
    `content`     TEXT         COMMENT '文章正文内容（article类型使用）',
    `resource_url` VARCHAR(500) COMMENT '音频/视频文件URL',
    `cover_url`   VARCHAR(500) COMMENT '封面图片URL',
    `tags`        VARCHAR(200) COMMENT '标签，逗号分隔，如: 放松,冥想,焦虑',
    `description` VARCHAR(500) COMMENT '简介',
    `view_count`  INT          NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-上架 0-下架',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理干预资源表';

-- 2. 示例数据
INSERT INTO `intervention_resources` (`title`, `type`, `content`, `cover_url`, `tags`, `description`, `status`) VALUES
(
    '正念冥想：5分钟放松练习',
    'article',
    '正念冥想是一种专注于当下的心理训练方法...\n\n**练习步骤：**\n1. 找一个安静的地方坐下，保持舒适的姿势。\n2. 闭上双眼，将注意力集中在自己的呼吸上。\n3. 感受每一次吸气和呼气的过程，不要刻意控制。\n4. 当思绪飘走时，温和地将注意力拉回到呼吸上。\n5. 坚持5分钟，逐渐延长练习时间。\n\n长期坚持正念冥想，可以显著降低焦虑和压力水平，提升整体心理健康状态。',
    NULL,
    '放松,冥想,正念,焦虑缓解',
    '通过简单的正念呼吸练习，帮助你在5分钟内平静焦虑的情绪',
    1
),
(
    '如何与负面情绪和平相处',
    'article',
    '负面情绪是生命的一部分，学会与它共处而非逃避，是心理健康的重要一步...\n\n**常见误区：**\n- 认为负面情绪是不好的，应该压制\n- 尝试用忙碌来逃避情绪\n- 独自承受但不寻求帮助\n\n**健康的应对方式：**\n1. **命名情绪**：说出"我现在感到______"，承认情绪的存在\n2. **身体感知**：关注情绪在身体哪个部位有感觉\n3. **接纳而非对抗**：告诉自己"这种感觉是暂时的"\n4. **寻找支持**：向信任的朋友倾诉，或寻求专业帮助',
    NULL,
    '情绪管理,心理健康,抑郁,焦虑',
    '了解负面情绪的本质，学习科学有效的情绪调节方法',
    1
),
(
    '渐进式肌肉放松音频练习',
    'audio',
    NULL,
    NULL,
    '放松,减压,睡眠,肌肉放松',
    '15分钟的引导式渐进肌肉放松练习，特别适合睡前使用，帮助缓解身体紧张',
    1
),
(
    '认知行为疗法（CBT）基础入门',
    'article',
    '认知行为疗法（CBT）是目前最有科学依据的心理治疗方法之一...\n\n**核心理念：**\n我们的情绪和行为，不是由事件本身决定的，而是由我们对事件的**看法和解读**决定的。\n\n**CBT的三角关系：**\n```\n想法 → 情绪 → 行为\n↑_______________↓\n```\n\n**实用练习——挑战负面思维：**\n1. 记录让你感到不安的想法\n2. 问自己：这个想法有什么证据？\n3. 有没有其他解释这件事的方式？\n4. 如果朋友有同样的想法，你会怎么建议他？',
    NULL,
    'CBT,认知行为,心理治疗,思维',
    '了解认知行为疗法的基本原理，学习用科学方法改变负面思维模式',
    1
);
