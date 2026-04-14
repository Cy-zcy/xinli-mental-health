-- =====================================================
-- 心理健康智能干预系统 - 资源数据脚本
-- =====================================================

SET NAMES utf8mb4;

-- 清空资源表
TRUNCATE TABLE `intervention_resources`;

-- 插入资源数据
INSERT INTO `intervention_resources` (`id`, `title`, `type`, `content`, `resource_url`, `cover_url`, `tags`, `description`, `view_count`, `status`, `created_at`, `updated_at`) VALUES

-- 文章资源
(1, '正念呼吸练习：快速放松身心', 'article',
 '# 正念呼吸：5分钟快速放松\n\n当你感到焦虑或压力时，试着做这个简单的呼吸练习：\n\n1. 找一个安静的地方坐下\n2. 闭上眼睛，专注于呼吸\n3. 吸气4秒，屏息4秒\n4. 呼气6秒，感受压力释放\n5. 重复5-10次\n\n坚持练习，你会发现自己的焦虑感明显降低。',
 NULL,
 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400',
 '呼吸,放松,焦虑,正念',
 '一个简单的呼吸练习，帮助你在5分钟内快速放松身心，缓解焦虑情绪。',
 156, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),

(2, '如何与负面情绪相处', 'article',
 '# 与负面情绪相处\n\n负面情绪是生活的一部分，学会与它相处是一门重要的功课。\n\n## 接纳情绪\n首先，不要试图压抑或逃避负面情绪。承认它的存在。\n\n## 观察情绪\n把自己当作旁观者，观察这个情绪。\n\n## 表达情绪\n找一个安全的方式表达情绪：写日记、运动、倾诉。\n\n## 寻求帮助\n如果负面情绪持续较久，建议寻求专业心理咨询师的帮助。',
 NULL,
 'https://images.unsplash.com/photo-1544027993-37dbfe43562a?w=400',
 '情绪管理,心理健康,自我调节',
 '学会正确面对和处理负面情绪，不让它影响你的生活质量。',
 234, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),

(3, '提升自信的10个方法', 'article',
 '# 提升自信的10个实用方法\n\n1. **记录成就**：每天写下3件自己做得好的事\n2. **正面自我对话**：用积极的语言替代消极的评价\n3. **设定小目标**：从小目标开始，积累成功经验\n4. **改善体态**：站直、挺胸、抬头\n5. **学习新技能**：让自己更有底气\n6. **运动锻炼**：运动释放多巴胺\n7. **整理外表**：干净整洁让你更自信\n8. **接受不完美**：没有人是完美的\n9. **远离负能量**：减少与消极的人相处\n10. **帮助他人**：帮助别人让你感觉有价值',
 NULL,
 'https://images.unsplash.com/photo-1552664730-d307ca884978?w=400',
 '自信,成长,自我提升',
 '实用的自信提升方法，帮助你建立积极的自我认知。',
 189, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),

-- 音频资源
(4, '睡前冥想引导音频', 'audio',
 NULL,
 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
 'https://images.unsplash.com/photo-1511295742362-92c96b1cf484?w=400',
 '睡眠,冥想,放松,失眠',
 '一段15分钟的睡前冥想引导音频，帮助你放松身心，改善睡眠质量。跟随引导，让身体和大脑逐渐放松，进入安稳的睡眠状态。',
 312, 1, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW()),

(5, '舒缓钢琴曲：安静时光', 'audio',
 NULL,
 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
 'https://images.unsplash.com/photo-1514119412350-e174d90d280e?w=400',
 '音乐,放松,钢琴,治愈',
 '精选舒缓钢琴曲，适合在工作间隙或睡前聆听，帮助你放松心情，缓解一天的疲劳。',
 278, 1, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),

(6, '正念冥想入门指导', 'audio',
 NULL,
 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
 'https://images.unsplash.com/photo-1508672017493-813cb2532a53?w=400',
 '正念,冥想,入门,指导',
 '适合初学者的正念冥想指导音频，从基础呼吸练习开始，逐步引导你进入冥想状态。建议每天练习10-15分钟。',
 198, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

-- 视频资源
(7, '焦虑症科普：认识焦虑', 'video',
 NULL,
 'https://www.w3schools.com/html/mov_bbb.mp4',
 'https://images.unsplash.com/photo-1559757175-5700dde675bc?w=400',
 '焦虑,科普,心理健康',
 '了解焦虑症的症状、成因和应对方法。本视频帮助你正确认识焦虑情绪，学会区分正常焦虑和焦虑症，掌握基本的自我调节技巧。',
 456, 1, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW()),

(8, '腹式呼吸法教学视频', 'video',
 NULL,
 'https://www.w3schools.com/html/movie.mp4',
 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400',
 '呼吸,放松,教学,腹式呼吸',
 '腹式呼吸是一种有效的放松技巧。本视频详细演示腹式呼吸的正确方法，帮助你快速缓解紧张和焦虑情绪。',
 523, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),

(9, '改善睡眠的实用技巧', 'video',
 NULL,
 'https://www.w3schools.com/html/mov_bbb.mp4',
 'https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=400',
 '睡眠,失眠,技巧,改善',
 '睡眠问题困扰着很多人。本视频分享改善睡眠的实用技巧，包括作息调整、睡眠环境优化、放松练习等，帮助你获得更好的睡眠质量。',
 389, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),

(10, '正念减压入门课程', 'video',
 NULL,
 'https://www.w3schools.com/html/movie.mp4',
 'https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=400',
 '正念,减压,课程,入门',
 '正念减压(MBSR)是目前国际公认有效的压力管理方法。本视频带你了解正念的基本概念，并指导你进行简单的正念练习。',
 467, 1, NOW(), NOW());

-- 查看插入结果
SELECT '资源数据插入完成！' AS message;
SELECT type AS '类型', COUNT(*) AS '数量' FROM intervention_resources GROUP BY type;