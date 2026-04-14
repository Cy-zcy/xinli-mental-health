import request from '@/utils/http'

// 仪表板统计数据类型
export interface DashboardStats {
  totalUsers: number        // 用户总数
  totalPosts: number        // 帖子总数
  totalComments: number     // 评论总数
  todayNewUsers: number     // 今日新增用户
  todayNewPosts: number     // 今日新增帖子
  activeUsers: number       // 活跃用户数
  pendingPosts: number      // 待审核帖子数
  // 心理测评统计
  totalAssessments: number  // 测评总次数
  normalCount: number       // 正常
  mildCount: number         // 轻度抑郁
  moderateCount: number     // 中度抑郁
  severeCount: number       // 重度抑郁（高风险）
  // 测评趋势
  trends: MonthlyTrend[]
  // 用户增长趋势
  userGrowthTrends: UserGrowthTrend[]
  // 帖子分类统计
  postCategoryStats: PostCategoryStats[]
}

// 月度测评趋势
export interface MonthlyTrend {
  month: string
  total: number
  highRisk: number
}

// 用户增长趋势
export interface UserGrowthTrend {
  month: string
  newUsers: number
}

// 帖子分类统计
export interface PostCategoryStats {
  category: string
  count: number
}

// 高风险用户记录
export interface HighRiskRecord {
  id: number
  userId: number
  assessmentId: number
  totalScore: number
  resultSummary: string
  resultDetails: string
  createdAt: string
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

// 仪表板服务
export class DashboardService {
  /**
   * 获取仪表板统计数据（含测评分布）
   */
  static getDashboardStats() {
    return request.get<DashboardStats>({
      url: '/api/admin/dashboard/stats'
    })
  }

  /**
   * 获取高风险用户列表（重度抑郁）
   */
  static getHighRiskUsers(page = 1, size = 10) {
    return request.get<PageResult<HighRiskRecord>>({
      url: '/api/admin/dashboard/high-risk',
      params: { page, size }
    } as any)
  }

  /**
   * 获取极低健康分用户预警列表（< 60 分）
   */
  /**
   * 获取极低健康分用户预警列表（< 60 分）
   */
  static getLowHealthScoreUsers(page = 1, size = 10) {
    return request.get<PageResult<any>>({
      url: '/api/admin/dashboard/low-health-score',
      params: { page, size }
    } as any)
  }

  /**
   * 获取 AI 检测到的高危帖子列表（riskLevel >= 2）
   */
  static getCrisisPosts(page = 1, size = 10) {
    return request.get<PageResult<ForumPostAnalysis>>({
      url: '/api/admin/dashboard/crisis-posts',
      params: { page, size }
    } as any)
  }
}

// 帖子情感分析类型
export interface ForumPostAnalysis {
  id: number
  postId: number
  userId: number
  emotionLabel: string
  emotionTags: string
  /** 0正常 1轻度关注 2高危 */
  riskLevel: number
  riskReason: string
  aiSummary: string
  isAlerted: number
  createdAt: string
}


