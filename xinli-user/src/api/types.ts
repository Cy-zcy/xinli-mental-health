/**
 * API接口类型定义
 * 适配后端xinli项目的数据结构
 */

// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
  timestamp: string
}

// 分页响应类型
export interface PageResponse<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

// 用户认证相关类型
export interface LoginRequest {
  phone: string
  password: string
}

export interface RegisterRequest {
  phone: string
  password: string
  nickname: string
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export interface UserInfo {
  id: number
  phone: string
  nickname: string
  avatar?: string
  status: number
  createdAt: string
  updatedAt: string
}

// AI聊天相关类型
export interface ChatSession {
  id: number
  userId?: number
  title: string
  status?: number
  createdAt: string
  updatedAt: string
}

// AI人设类型
export interface AiCharacter {
  id: number
  name: string
  avatar: string
  greeting: string
  background: string
  personality: string
  rules: string
  isActive: number
}

// 会话信息（用于会话列表）
export interface SessionInfo {
  id: number
  title: string
  createdAt: string
  updatedAt: string
  messageCount?: number
  lastMessage?: string
}

export interface ChatMessage {
  id: number
  sessionId?: number
  userId?: number
  role: 'user' | 'assistant' | 'system'
  content: string
  tokensUsed?: number
  createdAt: string
}

// 消息历史（用于聊天记录）
export interface MessageHistory {
  id: number
  role: 'user' | 'assistant' | 'system'
  content: string
  tokensUsed?: number
  createdAt: string
}

export interface SendMessageRequest {
  sessionId?: number
  content: string
}

export interface SendMessageResponse {
  sessionId: number
  messageId: number
  userMessage: string
  aiResponse: string
  tokensUsed: number
  timestamp: string
}

export interface CreateSessionRequest {
  title?: string
  characterId?: number
  firstMessage: string
}

export interface ChatHistoryResponse {
  sessionId: number
  sessionTitle: string
  messages: MessageHistory[]
  totalMessages: number
  totalTokens: number
}

// 论坛相关类型
export interface ForumPost {
  id: number
  userId: number
  title: string
  content: string
  category: string
  likeCount: number
  viewCount: number
  status: number
  createdAt: string
  updatedAt: string
  // 作者信息
  author?: {
    id: number
    nickname: string
    avatar?: string
  }
}

// 帖子详情（包含点赞状态等）
export interface PostDetail extends ForumPost {
  liked: boolean // 当前用户是否已点赞
  authorNickname?: string
  authorAvatar?: string
  comments?: CommentInfo[]
}

// 评论信息
export interface CommentInfo {
  id: number
  userId: number
  content: string
  status: number
  createdAt: string
  userNickname: string
  userAvatar?: string
}

export interface CreatePostRequest {
  title: string
  content: string
  category: string
}

export interface PostListParams {
  page?: number
  size?: number
  category?: string
  keyword?: string
  sortBy?: string
  sortOrder?: string
}

// 论坛分类
export interface ForumCategory {
  value: string  // 对应后端的 value 字段
  label: string  // 对应后端的 label 字段
  description?: string
  icon?: string
}

// 兼容性别名
export interface CategoryOption {
  id: string
  name: string
  description?: string
}

// 用户信息更新
export interface UpdateUserInfoRequest {
  nickname?: string
  avatar?: string
}
