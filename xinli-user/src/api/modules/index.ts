/**
 * API模块统一导出
 */

// 用户认证相关API
export * as authApi from './auth'

// AI聊天相关API
export * as chatApi from './chat'

// 论坛相关API
export * as forumApi from './forum'

// 心理测评相关API
export * as assessmentApi from './assessment'

// 导出类型定义
export * from '../types'
