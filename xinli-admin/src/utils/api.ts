/**
 * API响应处理工具
 */

// 后端统一响应格式
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
  timestamp: string
}

// 分页响应格式
export interface PageResponse<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 处理API响应数据
 * @param response API响应
 * @returns 处理后的数据
 */
export function handleApiResponse<T>(response: any): T {
  // 如果响应有data字段，说明是后端统一格式
  if (response && typeof response.data !== 'undefined') {
    return response.data
  }
  
  // 否则直接返回响应数据
  return response
}

/**
 * 处理分页API响应
 * @param response API响应
 * @returns 分页数据
 */
export function handlePageResponse<T>(response: any): PageResponse<T> {
  const data = handleApiResponse(response)
  
  // 如果数据有records字段，说明是分页数据
  if (data && typeof data.records !== 'undefined') {
    return data
  }
  
  // 否则包装成分页格式
  return {
    records: Array.isArray(data) ? data : [],
    total: Array.isArray(data) ? data.length : 0,
    size: 10,
    current: 1,
    pages: 1
  }
}

/**
 * 检查API响应是否成功
 * @param response API响应
 * @returns 是否成功
 */
export function isApiSuccess(response: any): boolean {
  // 检查HTTP状态码
  if (response.status && response.status !== 200) {
    return false
  }
  
  // 检查业务状态码
  if (response.data && typeof response.data.code !== 'undefined') {
    return response.data.code === 200
  }
  
  return true
}

/**
 * 获取API错误信息
 * @param error 错误对象
 * @returns 错误信息
 */
export function getApiErrorMessage(error: any): string {
  // 如果是API响应错误
  if (error.response && error.response.data) {
    const data = error.response.data
    return data.msg || data.message || '请求失败'
  }
  
  // 如果是网络错误
  if (error.message) {
    return error.message
  }
  
  return '未知错误'
}

/**
 * 格式化API请求参数
 * @param params 参数对象
 * @returns 格式化后的参数
 */
export function formatApiParams(params: Record<string, any>): Record<string, any> {
  const formatted: Record<string, any> = {}
  
  for (const [key, value] of Object.entries(params)) {
    // 过滤掉undefined和null值
    if (value !== undefined && value !== null && value !== '') {
      formatted[key] = value
    }
  }
  
  return formatted
}

/**
 * 创建API请求配置
 * @param options 配置选项
 * @returns 请求配置
 */
export function createApiConfig(options: {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  params?: Record<string, any>
  data?: any
  headers?: Record<string, string>
}) {
  const { url, method = 'GET', params, data, headers = {} } = options
  
  const config: any = {
    url,
    method: method.toLowerCase(),
    headers: {
      'Content-Type': 'application/json',
      ...headers
    }
  }
  
  if (params) {
    config.params = formatApiParams(params)
  }
  
  if (data) {
    config.data = data
  }
  
  return config
}
