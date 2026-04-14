import request from '@/utils/http'

export class UserService {
  // 登录
  static login(params: Api.Auth.LoginParams) {
    return request.post<Api.Auth.LoginResponse>({
      url: '/api/auth/login',
      params
      // showErrorMessage: false // 不显示错误消息
    })
  }

  // 获取用户信息
  static getUserInfo() {
    return request.get<Api.User.UserInfo>({
      url: '/api/admin/auth/me'
    })
  }

  // 获取用户列表
  static getUserList(params: Api.Common.PaginatingSearchParams) {
    return request.get<Api.User.UserListData>({
      url: '/api/user/list',
      params
    })
  }
}

// 管理员用户服务
export class AdminUserService {
  // 获取用户列表
  static getUserList(params: {
    page?: number
    size?: number
    keyword?: string
    status?: number
  }) {
    return request.get({
      url: '/api/admin/users',
      params
    })
  }

  // 获取用户详情
  static getUserDetail(userId: number) {
    return request.get({
      url: `/api/admin/users/${userId}`
    })
  }

  // 更新用户状态
  static updateUserStatus(userId: number, data: {
    status: number
    reason?: string
  }) {
    return request.put({
      url: `/api/admin/users/${userId}/status`,
      params: { status: data.status }  // 后端用 @RequestParam，必须用 params（查询串）
    })
  }

  // 删除用户
  static deleteUser(userId: number) {
    return request.del({
      url: `/api/admin/users/${userId}`
    })
  }

  // 获取用户健康分变动流水
  static getUserHealthScoreRecords(userId: number, page = 1, size = 10) {
    return request.get<any>({
      url: `/api/admin/users/${userId}/health-score-records`,
      params: { page, size }
    })
  }
}
