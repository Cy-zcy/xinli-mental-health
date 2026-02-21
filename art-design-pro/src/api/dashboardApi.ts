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
}

// 仪表板服务
export class DashboardService {
  /**
   * 获取仪表板统计数据
   */
  static getDashboardStats() {
    return request.get<DashboardStats>({
      url: '/api/admin/dashboard/stats'
    })
  }
}
