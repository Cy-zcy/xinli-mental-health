import request from '@/utils/http'

// 管理员论坛服务
export class AdminForumService {
  // 获取帖子列表
  static getPostList(params: {
    page?: number
    size?: number
    keyword?: string
    category?: string
    status?: number
  }) {
    return request.get({
      url: '/api/admin/posts',
      params
    })
  }

  // 获取帖子详情
  static getPostDetail(postId: number) {
    return request.get({
      url: `/api/admin/posts/${postId}`
    })
  }

  // 更新帖子状态
  static updatePostStatus(postId: number, data: {
    status: number
    reason?: string
  }) {
    return request.put({
      url: `/api/admin/posts/${postId}/status?status=${data.status}`
    })
  }

  // 删除帖子
  static deletePost(postId: number) {
    return request.del({
      url: `/api/admin/posts/${postId}`
    })
  }

  // 批量删除帖子
  static batchDeletePosts(postIds: number[]) {
    return request.del({
      url: '/api/admin/forum/posts/batch',
      data: { ids: postIds }
    })
  }

  // 获取论坛统计数据
  static getForumStats() {
    return request.get({
      url: '/api/admin/forum/stats'
    })
  }
}
