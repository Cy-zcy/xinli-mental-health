import request from '@/utils/http'

// 登录请求参数
export interface LoginParams {
  username: string
  password: string
}

// 登录响应数据
export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  admin: AdminInfo
}

// 管理员信息
export interface AdminInfo {
  id: number
  username: string
  name: string
  role: string
  roles: string[]
  status?: number
  createdAt?: string
  updatedAt?: string
}

// 认证服务
export class AuthService {
  /**
   * 管理员登录
   */
  static login(params: LoginParams) {
    return request.post<LoginResponse>({
      url: '/api/admin/auth/login',
      data: params
    })
  }

  /**
   * 获取当前用户信息
   */
  static getCurrentUser() {
    return request.get<AdminInfo>({
      url: '/api/admin/auth/me'
    })
  }

  /**
   * 退出登录
   */
  static logout() {
    return request.post({
      url: '/api/admin/auth/logout'
    })
  }
}
