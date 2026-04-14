/**
 * 心理健康系统测试数据生成脚本
 * 通过调用 API 接口添加测试用户和行为数据
 *
 * 功能：
 * 1. 创建不同健康分数阶段的用户
 * 2. 生成丰富的仪表盘统计数据
 * 3. 模拟真实用户行为
 *
 * 使用方法：
 * 1. 确保后端服务运行在 http://localhost:8080
 * 2. 运行: node scripts/generate-mock-data.js
 */

const axios = require('axios')

// API 基础地址
const BASE_URL = 'http://localhost:8080'

// 创建 axios 实例
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 存储用户 token 和 ID
const userTokens = new Map()

// 不同健康分数阶段的测试用户（共30人）
const testUsers = [
  // 高分用户（心理健康状态良好）- 10人
  { phone: '13900000001', password: '123456', nickname: '阳光少年', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000002', password: '123456', nickname: '心灵港湾', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000011', password: '123456', nickname: '快乐天使', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000012', password: '123456', nickname: '向日葵', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000013', password: '123456', nickname: '微笑面对', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000014', password: '123456', nickname: '心若向阳', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000015', password: '123456', nickname: '春风十里', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000016', password: '123456', nickname: '岁月静好', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000017', password: '123456', nickname: '花开半夏', targetHealthScore: 'high', expectedLevel: '正常' },
  { phone: '13900000018', password: '123456', nickname: '晴空万里', targetHealthScore: 'high', expectedLevel: '正常' },

  // 中等分数用户（轻度问题）- 8人
  { phone: '13900000003', password: '123456', nickname: '温暖小屋', targetHealthScore: 'medium', expectedLevel: '轻度抑郁' },
  { phone: '13900000004', password: '123456', nickname: '宁静致远', targetHealthScore: 'medium', expectedLevel: '轻度抑郁' },
  { phone: '13900000019', password: '123456', nickname: '寻找方向', targetHealthScore: 'medium', expectedLevel: '轻度抑郁' },
  { phone: '13900000020', password: '123456', nickname: '慢慢来', targetHealthScore: 'medium', expectedLevel: '轻度抑郁' },
  { phone: '13900000021', password: '123456', nickname: '小确幸', targetHealthScore: 'medium', expectedLevel: '轻度抑郁' },
  { phone: '13900000022', password: '123456', nickname: '努力中', targetHealthScore: 'medium', expectedLevel: '轻度抑郁' },
  { phone: '13900000023', password: '123456', nickname: '等待花开', targetHealthScore: 'medium', expectedLevel: '轻度抑郁' },
  { phone: '13900000024', password: '123456', nickname: '慢慢变好', targetHealthScore: 'medium', expectedLevel: '轻度抑郁' },

  // 低分用户（中度问题）- 6人
  { phone: '13900000005', password: '123456', nickname: '快乐星球', targetHealthScore: 'low', expectedLevel: '中度抑郁' },
  { phone: '13900000006', password: '123456', nickname: '心灵旅者', targetHealthScore: 'low', expectedLevel: '中度抑郁' },
  { phone: '13900000025', password: '123456', nickname: '迷途羔羊', targetHealthScore: 'low', expectedLevel: '中度抑郁' },
  { phone: '13900000026', password: '123456', nickname: '需要帮助', targetHealthScore: 'low', expectedLevel: '中度抑郁' },
  { phone: '13900000027', password: '123456', nickname: '寻找出口', targetHealthScore: 'low', expectedLevel: '中度抑郁' },
  { phone: '13900000028', password: '123456', nickname: '渴望阳光', targetHealthScore: 'low', expectedLevel: '中度抑郁' },

  // 极低分预警用户（重度问题）- 6人
  { phone: '13900000007', password: '123456', nickname: '寻找光明', targetHealthScore: 'critical', expectedLevel: '重度抑郁' },
  { phone: '13900000008', password: '123456', nickname: '黑暗中的光', targetHealthScore: 'critical', expectedLevel: '重度抑郁' },
  { phone: '13900000029', password: '123456', nickname: '需要关爱', targetHealthScore: 'critical', expectedLevel: '重度抑郁' },
  { phone: '13900000030', password: '123456', nickname: '孤独患者', targetHealthScore: 'critical', expectedLevel: '重度抑郁' },
  { phone: '13900000031', password: '123456', nickname: '迷失自我', targetHealthScore: 'critical', expectedLevel: '重度抑郁' },
  { phone: '13900000032', password: '123456', nickname: '等待救赎', targetHealthScore: 'critical', expectedLevel: '重度抑郁' },
]

// 聊天话题模板 - 按情绪状态分类（扩展版）
const chatTopicsByLevel = {
  high: [
    '最近心情不错，想聊聊保持积极心态的方法',
    '分享一下我的快乐，今天遇到了很多好事',
    '想了解如何更好地帮助身边的人',
    '最近学习冥想，感觉效果很好',
    '想知道怎么保持长期的心理健康',
    '最近读了一本好书，想分享一下',
    '感觉自己越来越能应对压力了',
    '想聊聊如何培养积极的生活习惯',
  ],
  medium: [
    '最近工作压力有点大，感觉喘不过气来',
    '总是失眠，晚上躺在床上翻来覆去睡不着',
    '和家人的关系有点紧张，不知道该怎么沟通',
    '感觉自己很孤独，没有什么朋友可以倾诉',
    '最近总是胡思乱想，停不下来',
    '工作没动力，每天都很疲惫',
    '感觉生活没有以前那么有激情了',
    '想要改变现状，但不知道从哪里开始',
  ],
  low: [
    '对未来感到迷茫，不知道自己想要什么',
    '最近总是焦虑，担心很多事情',
    '觉得自己没有价值，做什么都没有动力',
    '每天都感到很疲惫，提不起精神',
    '感觉自己被所有人忽视了',
    '做什么都提不起兴趣',
    '感觉自己很失败',
    '不知道为什么活着',
  ],
  critical: [
    '感觉生活没有意义，不知道为什么活着',
    '总是想哭，控制不住自己的情绪',
    '没有人理解我，感到非常绝望',
    '很累，对所有事情都失去了兴趣',
    '觉得自己是个负担',
    '想要逃离一切',
    '感觉被困住了，找不到出口',
    '每天都很痛苦，不知道怎么熬过去',
  ],
}

// AI 回复模拟
const aiReplies = [
  '我理解你现在的感受，这确实是很困难的处境。你能具体说说是什么让你感到压力吗？',
  '失眠确实很影响生活质量和情绪状态。你有没有尝试过一些放松的方法呢？比如冥想或深呼吸？',
  '家庭关系的紧张确实会让人心力交瘁。沟通是解决问题的关键，你愿意和我分享更多细节吗？',
  '孤独感是很常见的情绪体验，尤其是在现代社会。你平时有什么兴趣爱好吗？',
  '迷茫是人生旅途中很正常的状态。让我们一起探索，找出你内心真正想要的。',
  '焦虑往往源于对未知的恐惧。我们可以一起分析这些担忧，看看哪些是可以通过行动改变的。',
]

// 帖子模板 - 按分类（扩展版）
const postTemplates = {
  share: [
    { title: '分享一个小确幸', content: '今天在公园散步时，看到一只可爱的小狗，主人说它可以握手，那一刻心情特别好。生活中有很多小确幸，分享一下吧~' },
    { title: '冥想练习30天心得', content: '坚持冥想练习30天了，分享一下心得：1. 找一个固定的时间练习；2. 不追求完全放空，而是观察自己的思绪；3. 每天哪怕只有5分钟也坚持。最大的变化是睡眠质量提高了，白天的情绪也更稳定了。' },
    { title: '感恩日记的魔力', content: '开始写感恩日记一周了，每天记录三件感恩的事。神奇的是，现在会不自觉地关注生活中的美好，而不是总盯着问题和烦恼。强烈推荐大家试试！' },
    { title: '运动对心情的影响', content: '坚持晨跑两个月了，发现对心情真的有很大帮助。每天早上跑步后，整个人的精神状态都好了很多，焦虑也减少了。' },
    { title: '学会说"不"之后', content: '以前总是不好意思拒绝别人，结果把自己搞得很累。最近学会了适当说"不"，发现生活轻松了很多，也更有时间照顾自己了。' },
    { title: '阅读治愈了我的焦虑', content: '这半年养成了每天阅读的习惯，发现读书真的能让心静下来。推荐几本对我有帮助的书：《被讨厌的勇气》《当下的力量》《正念的奇迹》。' },
  ],
  question: [
    { title: '如何缓解工作焦虑？', content: '最近工作压力特别大，每天都很焦虑，晚上也睡不好。有没有类似经历的朋友，你们是怎么调整过来的？' },
    { title: '和父母沟通真的很难', content: '每次和父母聊天都会吵架，他们总觉得我还小，很多事情不理解。其实我已经工作了几年，有自己的想法。怎么才能让父母更理解我呢？' },
    { title: '社恐怎么办？', content: '从小就很害怕和人交流，现在工作需要经常开会发言，每次都很紧张。有没有克服社恐的经验分享？' },
    { title: '如何建立自信？', content: '总觉得自己什么都不如别人，很自卑。想要改变，但不知道从哪里开始。有什么建立自信的方法吗？' },
    { title: '失眠很严重怎么办？', content: '最近几个月失眠越来越严重，每天躺在床上两三个小时才能睡着。试过数羊、听音乐，效果都不好。有经验的朋友能给点建议吗？' },
    { title: '如何处理职场人际关系？', content: '刚入职一家新公司，发现同事们都有小圈子，感觉很难融入。应该怎么处理这种职场人际关系呢？' },
  ],
  emotion: [
    { title: '学会接纳自己的不完美', content: '以前总是追求完美，什么事情都要做到最好，结果把自己搞得很累。最近开始学着接纳不完美的自己，发现生活轻松了很多。' },
    { title: '今天又是一个人', content: '周末了，朋友们都有约会，只有我一个人在家。有时候感觉挺孤单的，但也在学着享受独处的时光。' },
    { title: '谢谢你陪我度过最难的日子', content: '半年前经历了人生的低谷，感谢这个平台和这里的朋友们。虽然我们素不相识，但你们的鼓励给了我很大的力量。' },
    { title: '终于走出来了', content: '经历了半年的抑郁，终于感觉好起来了。想告诉还在挣扎的朋友们：不要放弃，一切都会好起来的。' },
    { title: '有时候真的很累', content: '每天装作很开心的样子，其实内心很疲惫。不知道什么时候才能不用戴着面具生活。' },
    { title: '想要被理解', content: '总觉得没人真正理解我，包括最亲近的人。有时候想说点什么，但话到嘴边又咽回去了。' },
  ],
  daily: [
    { title: '今天的云很美', content: '下班路上抬头看天，发现今天的晚霞特别美。停下来拍了张照片，感觉一整天的疲惫都被治愈了。' },
    { title: '学会了一道新菜', content: '周末学会了做红烧肉，虽然卖相一般，但味道还不错！做饭的过程很解压，推荐大家试试。' },
    { title: '养了一只小猫', content: '上周领养了一只小橘猫，每天回家看到它在门口等我，心情就好很多。宠物真的是很好的陪伴。' },
    { title: '开始学画画了', content: '报了一个水彩画班，每周去两次。虽然画得不好，但沉浸在色彩中的感觉很治愈。' },
    { title: '周末徒步记', content: '和朋友去郊外徒步了一天，好久没有这么放松过了。大自然真的是最好的疗愈师。' },
  ],
}

// 评论模板（扩展版）
const commentTemplates = [
  '说得太好了，很有共鸣！',
  '感谢分享，对我很有帮助~',
  '我也有类似的经历，加油！',
  '抱抱你，一切都会好起来的',
  '建议你可以试试冥想，对我很有用',
  '理解你的感受，我们都在努力',
  '支持你！勇敢地表达自己',
  '你并不孤单，我们都在这里',
  '谢谢你的分享，让我学到了很多',
  '加油！相信你可以的',
  '同感！完全理解你的心情',
  '你的文字让我很感动',
  '保持积极的心态，一切都会好转的',
  '感谢你的真诚分享',
  '慢慢来，不要给自己太大压力',
  '你已经做得很好了',
]

// 工具记录 - 不同类型
const toolRecords = [
  { toolType: 'meditation', durationSeconds: 600, completed: 1, pattern: '正念冥想' },
  { toolType: 'meditation', durationSeconds: 900, completed: 1, pattern: '放松冥想' },
  { toolType: 'meditation', durationSeconds: 1200, completed: 1, pattern: '睡眠冥想' },
  { toolType: 'breathing', durationSeconds: 300, completed: 1, pattern: '4-7-8 呼吸法', cycles: 5 },
  { toolType: 'breathing', durationSeconds: 180, completed: 1, pattern: '腹式呼吸', cycles: 3 },
  { toolType: 'breathing', durationSeconds: 240, completed: 1, pattern: '箱式呼吸', cycles: 4 },
]

// 颜色输出
const colors = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  red: '\x1b[31m',
  cyan: '\x1b[36m',
  magenta: '\x1b[35m',
}

function log(color, message) {
  console.log(`${colors[color]}${message}${colors.reset}`)
}

// API 调用函数
async function registerOrLogin(user) {
  // 先尝试登录
  let loginRes
  try {
    loginRes = await api.post('/api/auth/login', {
      phone: user.phone,
      password: user.password
    })
    if (loginRes.data.code === 200) {
      log('green', `用户 ${user.nickname} 登录成功`)
      return { token: loginRes.data.data.token, userId: loginRes.data.data.user?.id }
    }
    // 登录失败（code !== 200），继续尝试注册
  } catch (e) {
    // HTTP错误，继续尝试注册
  }

  // 尝试注册
  log('yellow', `用户 ${user.nickname} 不存在，尝试注册...`)
  try {
    const registerRes = await api.post('/api/auth/register', {
      phone: user.phone,
      password: user.password,
      nickname: user.nickname
    })
    if (registerRes.data.code === 200) {
      log('green', `用户 ${user.nickname} 注册成功`)
      // 注册成功后需要登录获取token
      const loginRes = await api.post('/api/auth/login', {
        phone: user.phone,
        password: user.password
      })
      if (loginRes.data.code === 200) {
        return { token: loginRes.data.data.token, userId: loginRes.data.data.user?.id }
      }
    } else {
      log('red', `用户 ${user.nickname} 注册失败: ${registerRes.data.msg}`)
    }
  } catch (err) {
    log('red', `用户 ${user.nickname} 注册失败: ${err.message}`)
  }
  return null
}

async function createChatSession(token, title) {
  try {
    const res = await api.post('/api/chat/session',
      { title, firstMessage: '你好，我想和你聊聊' },
      { headers: { Authorization: `Bearer ${token}` } }
    )
    if (res.data.code === 200) {
      return res.data.data
    }
  } catch (e) {
    log('red', `创建会话失败: ${e.message}`)
  }
  return null
}

async function sendMessage(token, sessionId, content) {
  try {
    const res = await api.post('/api/chat/send',
      { sessionId, content },
      { headers: { Authorization: `Bearer ${token}` } }
    )
    if (res.data.code === 200) {
      return res.data.data
    }
  } catch (e) {
    // 静默处理，AI 可能未配置
  }
  return null
}

async function createPost(token, post, category) {
  try {
    const res = await api.post('/api/forum/posts',
      { title: post.title, content: post.content, category: category },
      { headers: { Authorization: `Bearer ${token}` } }
    )
    if (res.data.code === 200) {
      log('blue', `  发布帖子: ${post.title}`)
      return res.data.data
    }
  } catch (e) {
    log('red', `发布帖子失败: ${e.message}`)
  }
  return null
}

async function getPostList(page = 1, size = 20) {
  try {
    const res = await api.get(`/api/forum/posts?page=${page}&size=${size}`)
    if (res.data.code === 200) {
      return res.data.data.records || []
    }
  } catch (e) {
    // 静默处理
  }
  return []
}

async function likePost(token, postId) {
  try {
    await api.post(`/api/forum/posts/${postId}/like`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
  } catch (e) {
    // 静默处理
  }
}

async function createComment(token, postId, content) {
  try {
    const res = await api.post(`/api/forum/posts/${postId}/comments`,
      { content },
      { headers: { Authorization: `Bearer ${token}` } }
    )
    if (res.data.code === 200) {
      return res.data.data
    }
  } catch (e) {
    // 静默处理
  }
  return null
}

async function recordToolUsage(token, record) {
  try {
    const res = await api.post('/api/tools/record', record, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.data.code === 200) {
      log('blue', `  记录工具使用: ${record.toolType} - ${Math.floor(record.durationSeconds/60)}分钟`)
    }
  } catch (e) {
    // 静默处理
  }
}

async function getAssessmentList(token) {
  try {
    const res = await api.get('/api/assessment/list', {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.data.code === 200) {
      return res.data.data.records || []
    }
  } catch (e) {
    // 静默处理
  }
  return []
}

async function getAssessmentDetail(token, id) {
  try {
    const res = await api.get(`/api/assessment/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (res.data.code === 200) {
      return res.data.data
    }
  } catch (e) {
    // 静默处理
  }
  return null
}

// 根据目标等级选择答案，影响测评结果
// SDS量表：每题4个选项，分值1-4，共20题，总分20-80
// 标准分 = 粗分 * 1.25
// 正常: 标准分 < 53 → 粗分 < 42
// 轻度: 53 <= 标准分 <= 62 → 粗分 43-50
// 中度: 63 <= 标准分 <= 72 → 粗分 51-58
// 重度: 标准分 > 72 → 粗分 > 58
function selectAnswersForLevel(questions, targetLevel) {
  const answers = {}

  // 目标平均分（每题）：根据期望等级设置
  // 1=低分选项，2=中等偏低，3=中等偏高，4=高分选项
  const targetAvgScore = {
    '正常': 1.5,      // 偏向选择1-2分选项
    '轻度抑郁': 2.5,  // 偏向选择2-3分选项
    '中度抑郁': 3.2,  // 偏向选择3分选项
    '重度抑郁': 3.8,  // 强烈偏向4分选项
  }

  const avgScore = targetAvgScore[targetLevel] || 2

  questions.forEach(q => {
    if (q.options && q.options.length > 0) {
      // 根据目标平均分计算选择概率
      // 假设选项按分数从低到高排列（1-4分）
      let selectedIndex

      if (avgScore <= 2) {
        // 偏向低分选项
        const rand = Math.random()
        if (rand < (2 - avgScore)) {
          selectedIndex = 0 // 选择最低分选项
        } else {
          selectedIndex = Math.floor(Math.random() * Math.min(2, q.options.length))
        }
      } else if (avgScore >= 3.5) {
        // 偏向高分选项
        const rand = Math.random()
        if (rand < (avgScore - 3)) {
          selectedIndex = q.options.length - 1 // 选择最高分选项
        } else {
          selectedIndex = Math.max(2, Math.floor(Math.random() * q.options.length))
        }
      } else {
        // 中等分数，正态分布选择
        const centerIndex = Math.round((avgScore - 1) / 3 * (q.options.length - 1))
        const offset = Math.random() < 0.5 ? -1 : 1
        selectedIndex = Math.max(0, Math.min(q.options.length - 1, centerIndex + offset))
      }

      answers[q.id] = q.options[selectedIndex].id
    }
  })

  return answers
}

async function submitAssessment(token, assessmentId, questions, targetLevel) {
  try {
    const answers = selectAnswersForLevel(questions, targetLevel)

    const res = await api.post('/api/assessment/submit',
      { assessmentId, answers },
      { headers: { Authorization: `Bearer ${token}` } }
    )
    if (res.data.code === 200) {
      log('cyan', `  完成测评: ID ${assessmentId}, 目标结果: ${targetLevel}`)
      return res.data.data
    }
  } catch (e) {
    log('red', `提交测评失败: ${e.message}`)
  }
  return null
}

// 设置用户健康分（使用管理员API）
async function setUserHealthScore(userId, score) {
  try {
    const res = await api.put(`/api/admin/users/${userId}/health-score?score=${score}`)
    if (res.data.code === 200) {
      return true
    }
  } catch (e) {
    log('red', `设置健康分失败: ${e.message}`)
  }
  return false
}

// 主函数
async function main() {
  log('yellow', '='.repeat(70))
  log('yellow', '心理健康系统 - 测试数据生成脚本 (增强版)')
  log('yellow', '='.repeat(70))

  // 1. 注册/登录用户
  log('yellow', '\n[步骤1] 创建不同健康分数阶段的测试用户...')
  for (const user of testUsers) {
    const result = await registerOrLogin(user)
    if (result) {
      userTokens.set(user.phone, { token: result.token, userId: result.userId, ...user })
    }
    await sleep(200)
  }
  log('green', `成功创建/登录 ${userTokens.size} 个用户`)

  // 2. 为每个用户创建聊天会话
  log('yellow', '\n[步骤2] 创建聊天会话...')
  for (const [phone, userData] of userTokens) {
    const topics = chatTopicsByLevel[userData.targetHealthScore] || chatTopicsByLevel.medium
    log('blue', `为用户 ${userData.nickname} (${userData.expectedLevel}) 创建聊天会话...`)

    // 根据健康分数阶段决定会话数量
    const sessionCount = userData.targetHealthScore === 'critical' ? 4 :
                         userData.targetHealthScore === 'low' ? 3 : 2

    for (let i = 0; i < sessionCount; i++) {
      const topic = topics[Math.floor(Math.random() * topics.length)]
      const session = await createChatSession(userData.token, topic.substring(0, 30))
      if (session) {
        log('blue', `  创建会话: ${topic.substring(0, 25)}...`)

        // 发送几条消息
        const replyCount = 1 + Math.floor(Math.random() * 2)
        for (let j = 0; j < replyCount; j++) {
          const userMsg = topics[Math.floor(Math.random() * topics.length)]
          await sendMessage(userData.token, session.id, userMsg)
          await sleep(300)
        }
      }
      await sleep(300)
    }
  }

  // 3. 发布帖子 - 不同分类
  log('yellow', '\n[步骤3] 发布论坛帖子（多分类）...')
  const allPosts = []
  const categories = ['share', 'question', 'emotion', 'daily']

  for (const [phone, userData] of userTokens) {
    log('blue', `为用户 ${userData.nickname} 发布帖子...`)

    // 每个用户发布 2-5 个帖子
    const postCount = 2 + Math.floor(Math.random() * 4)
    for (let i = 0; i < postCount; i++) {
      const category = categories[i % categories.length]
      const posts = postTemplates[category]
      const post = posts[Math.floor(Math.random() * posts.length)]
      const createdPost = await createPost(userData.token, post, category)
      if (createdPost) {
        allPosts.push({ ...createdPost, authorPhone: phone, category })
      }
      await sleep(200)
    }
  }

  // 4. 用户互动 - 点赞和评论
  log('yellow', '\n[步骤4] 用户互动（点赞+评论）...')
  for (const [phone, userData] of userTokens) {
    // 随机点赞其他用户的帖子 (3-6个)
    const otherPosts = allPosts.filter(p => p.authorPhone !== phone)
    const likeCount = Math.min(3 + Math.floor(Math.random() * 4), otherPosts.length)
    for (let i = 0; i < likeCount; i++) {
      const randomPost = otherPosts[Math.floor(Math.random() * otherPosts.length)]
      await likePost(userData.token, randomPost.id)
      await sleep(50)
    }

    // 随机评论 (1-4个)
    const commentCount = Math.min(1 + Math.floor(Math.random() * 4), otherPosts.length)
    for (let i = 0; i < commentCount; i++) {
      const randomPost = otherPosts[Math.floor(Math.random() * otherPosts.length)]
      const comment = commentTemplates[Math.floor(Math.random() * commentTemplates.length)]
      await createComment(userData.token, randomPost.id, comment)
      await sleep(100)
    }
    await sleep(200)
  }
  log('green', `  互动完成：点赞和评论已生成`)

  // 5. 记录工具使用
  log('yellow', '\n[步骤5] 记录工具使用...')
  for (const [phone, userData] of userTokens) {
    log('blue', `为用户 ${userData.nickname} 记录工具使用...`)

    // 高分用户更倾向于使用工具
    const recordCount = userData.targetHealthScore === 'high' ? 4 :
                        userData.targetHealthScore === 'medium' ? 3 :
                        userData.targetHealthScore === 'low' ? 2 : 1

    for (let i = 0; i < recordCount; i++) {
      const record = toolRecords[Math.floor(Math.random() * toolRecords.length)]
      await recordToolUsage(userData.token, record)
      await sleep(200)
    }
  }

  // 6. 完成心理测评 - 根据目标等级选择答案
  log('yellow', '\n[步骤6] 完成心理测评（生成不同结果）...')

  // 先获取测评列表
  const firstUser = Array.from(userTokens.values())[0]
  if (firstUser) {
    const assessments = await getAssessmentList(firstUser.token)

    if (assessments.length > 0) {
      let normalCount = 0, mildCount = 0, moderateCount = 0, severeCount = 0

      for (const [phone, userData] of userTokens) {
        log('blue', `为用户 ${userData.nickname} 完成测评（目标: ${userData.expectedLevel}）...`)

        // 每个用户完成 1-2 个测评
        const assessmentCount = Math.min(1 + Math.floor(Math.random() * 2), assessments.length)
        for (let i = 0; i < assessmentCount; i++) {
          const assessment = assessments[i]
          const detail = await getAssessmentDetail(userData.token, assessment.id)
          if (detail && detail.questions) {
            const result = await submitAssessment(userData.token, assessment.id, detail.questions, userData.expectedLevel)
            if (result) {
              // 统计实际结果
              if (result.resultSummary?.includes('正常')) normalCount++
              else if (result.resultSummary?.includes('轻度')) mildCount++
              else if (result.resultSummary?.includes('中度')) moderateCount++
              else if (result.resultSummary?.includes('重度')) severeCount++
            }
          }
          await sleep(500)
        }
      }

      log('magenta', `\n  测评结果分布:`)
      log('green', `    正常: ${normalCount} 人`)
      log('yellow', `    轻度抑郁: ${mildCount} 人`)
      log('yellow', `    中度抑郁: ${moderateCount} 人`)
      log('red', `    重度抑郁: ${severeCount} 人`)
    } else {
      log('yellow', '  没有可用的测评问卷，跳过测评步骤')
    }
  }

  // 7. 设置用户健康分（模拟不同心理状态）
  log('yellow', '\n[步骤7] 设置用户健康分（生成预警数据）...')

  // 健康分目标范围
  const healthScoreRanges = {
    'high': { min: 80, max: 100 },      // 心理健康状态良好
    'medium': { min: 60, max: 75 },     // 轻度问题，但不在预警范围
    'low': { min: 40, max: 55 },        // 中度问题，触发预警
    'critical': { min: 20, max: 40 },   // 重度问题，严重预警
  }

  let lowScoreCount = 0
  for (const [phone, userData] of userTokens) {
    if (!userData.userId) continue

    const range = healthScoreRanges[userData.targetHealthScore] || healthScoreRanges.medium
    const score = range.min + Math.floor(Math.random() * (range.max - range.min + 1))

    const success = await setUserHealthScore(userData.userId, score)
    if (success) {
      log('blue', `  用户 ${userData.nickname}: 健康分设置为 ${score}`)
      if (score < 60) lowScoreCount++
    }
    await sleep(100)
  }
  log('green', `  健康分设置完成，其中 ${lowScoreCount} 人触发预警（< 60分）`)

  // 完成
  log('green', '\n' + '='.repeat(70))
  log('green', '测试数据生成完成!')
  log('green', '='.repeat(70))

  log('cyan', '\n📊 生成统计:')
  log('cyan', `  - 用户总数: ${userTokens.size}`)

  const highScoreUsers = Array.from(userTokens.values()).filter(u => u.targetHealthScore === 'high').length
  const mediumScoreUsers = Array.from(userTokens.values()).filter(u => u.targetHealthScore === 'medium').length
  const lowScoreUsers = Array.from(userTokens.values()).filter(u => u.targetHealthScore === 'low').length
  const criticalUsers = Array.from(userTokens.values()).filter(u => u.targetHealthScore === 'critical').length

  log('green', `  - 高分用户（正常）: ${highScoreUsers}`)
  log('yellow', `  - 中等分数用户（轻度）: ${mediumScoreUsers}`)
  log('yellow', `  - 低分用户（中度）: ${lowScoreUsers}`)
  log('red', `  - 极低分预警用户（重度）: ${criticalUsers}`)

  log('cyan', `  - 帖子总数: ${allPosts.length}`)

  const sharePosts = allPosts.filter(p => p.category === 'share').length
  const questionPosts = allPosts.filter(p => p.category === 'question').length
  const emotionPosts = allPosts.filter(p => p.category === 'emotion').length
  const dailyPosts = allPosts.filter(p => p.category === 'daily').length

  log('blue', `    - 经验分享: ${sharePosts}`)
  log('blue', `    - 求助问答: ${questionPosts}`)
  log('blue', `    - 情感倾诉: ${emotionPosts}`)
  log('blue', `    - 日常记录: ${dailyPosts}`)

  log('cyan', '\n💡 提示:')
  log('blue', '  - 仪表盘将显示丰富的统计数据')
  log('blue', '  - 健康分预警列表将显示低分用户')
  log('blue', '  - 如果 AI 聊天未正常工作，请检查 DeepSeek API 配置')
  log('yellow', '\n⚠️  注意: 测评结果由答案决定，可能与目标略有差异')
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// 运行
main().catch(err => {
  log('red', `脚本执行失败: ${err.message}`)
  process.exit(1)
})