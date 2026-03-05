/**
 * 用户认证相关API
 * 对应后端 UserAuthController
 */
import api from '../index'
import type {
  LoginRequest,
  RegisterRequest,
  LoginResponse,
  UserInfo,
  UpdateUserInfoRequest,
} from '../types'

/**
 * 用户注册
 * POST /api/auth/register
 */
export function register(data: RegisterRequest) {
  return api.post<LoginResponse>('/api/auth/register', data)
}

/**
 * 用户登录
 * POST /api/auth/login
 */
export function login(data: LoginRequest) {
  return api.post<LoginResponse>('/api/auth/login', data)
}

/**
 * 用户登出
 * POST /api/auth/logout
 */
export function logout() {
  return api.post('/api/auth/logout')
}

/**
 * 刷新Token
 * POST /api/auth/refresh
 */
export function refreshToken() {
  return api.post<{ token: string }>('/api/auth/refresh')
}

/**
 * 检查手机号是否已注册
 * GET /api/auth/check-phone
 */
export function checkPhone(phone: string) {
  return api.get<{ exists: boolean }>('/api/auth/check-phone', {
    params: { phone }
  })
}

/**
 * 获取当前用户信息
 * GET /api/user/info
 */
export function getUserInfo() {
  return api.get<UserInfo>('/api/user/info')
}

/**
 * 更新用户信息
 * PUT /api/user/info
 */
export function updateUserInfo(data: UpdateUserInfoRequest) {
  return api.put<UserInfo>('/api/user/info', data)
}

/**
 * 修改密码
 * PUT /api/user/password
 */
export function changePassword(data: {
  oldPassword: string
  newPassword: string
}) {
  return api.put('/api/user/password', data)
}

/**
 * 上传头像
 * POST /api/upload/avatar
 */
export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return api.post<{
    relativePath: string
    url: string
    message: string
  }>('/api/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 获取文件URL
 * GET /api/url  （注意：后端 FileUploadController @RequestMapping("/api")，方法 @GetMapping("/url")，实际路径为 /api/url）
 */
export function getFileUrl(path: string) {
  return api.get<string>(`/api/url?path=${encodeURIComponent(path)}`)
}
