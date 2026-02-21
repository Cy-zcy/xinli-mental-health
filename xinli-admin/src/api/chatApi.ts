import request from '@/utils/http'

// 聊天会话信息
export interface ChatSession {
  id: number
  title: string
  createdAt: string
  updatedAt: string
  messageCount: number
  lastMessage: string
}

// 聊天消息
export interface ChatMessage {
  id: number
  role: 'user' | 'assistant' | 'system'
  content: string
  tokensUsed?: number
  createdAt: string
}

// 发送消息请求
export interface SendMessageRequest {
  sessionId?: number
  content: string
}

// 发送消息响应
export interface SendMessageResponse {
  sessionId: number
  messageId: number
  userMessage: string
  aiResponse: string
  tokensUsed: number
  timestamp: string
}

// 创建会话请求
export interface CreateSessionRequest {
  title?: string
  firstMessage: string
}

// 聊天历史响应
export interface ChatHistoryResponse {
  sessionId: number
  sessionTitle: string
  messages: ChatMessage[]
  totalMessages: number
  totalTokens: number
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

// AI聊天服务
export class ChatService {
  /**
   * 发送消息给AI
   */
  static sendMessage(data: SendMessageRequest) {
    return request.post<SendMessageResponse>({
      url: '/api/chat/send',
      data
    })
  }

  /**
   * 创建新的聊天会话
   */
  static createSession(data: CreateSessionRequest) {
    return request.post<ChatSession>({
      url: '/api/chat/session',
      data
    })
  }

  /**
   * 获取用户的会话列表
   */
  static getSessions(params: {
    page?: number
    size?: number
  } = {}) {
    return request.get<PageResponse<ChatSession>>({
      url: '/api/chat/sessions',
      params: {
        page: 1,
        size: 10,
        ...params
      }
    })
  }

  /**
   * 获取聊天历史记录
   */
  static getChatHistory(params: {
    sessionId: number
    page?: number
    size?: number
  }) {
    return request.get<ChatHistoryResponse>({
      url: '/api/chat/history',
      params: {
        page: 1,
        size: 50,
        ...params
      }
    })
  }

  /**
   * 获取会话详情
   */
  static getSessionDetail(sessionId: number) {
    return request.get<ChatHistoryResponse>({
      url: `/api/chat/session/${sessionId}`
    })
  }

  /**
   * 删除会话
   */
  static deleteSession(sessionId: number) {
    return request.delete({
      url: `/api/chat/session/${sessionId}`
    })
  }

  /**
   * 测试AI连接
   */
  static testConnection() {
    return request.get<boolean>({
      url: '/api/chat/test'
    })
  }
}

// 聊天会话VO类型（管理员视图）
export interface ChatSessionVO {
  id: number
  userId: number
  userPhone: string
  userNickname: string
  title: string
  status: number
  messageCount: number
  lastActiveTime: string
  createdAt: string
  updatedAt: string
}

// 聊天会话详情类型
export interface ChatSessionDetail {
  session: {
    id: number
    userId: number
    title: string
    status: number
    createdAt: string
    updatedAt: string
  }
  user: {
    id: number
    phone: string
    nickname: string
    avatar?: string
  }
  messages: ChatMessage[]
}

// 管理员聊天服务（用于监控和管理）
export class AdminChatService {
  /**
   * 获取聊天会话列表（分页）
   */
  static getChatSessions(params: {
    current?: number
    size?: number
    userPhone?: string
    userNickname?: string
    startDate?: string
    endDate?: string
  } = {}) {
    return request.get<PageResponse<ChatSessionVO>>({
      url: '/api/admin/chat/sessions',
      params: {
        current: 1,
        size: 10,
        ...params
      }
    })
  }

  /**
   * 获取聊天会话详情和消息记录
   */
  static getChatSessionDetail(sessionId: number) {
    return request.get<ChatSessionDetail>({
      url: `/api/admin/chat/sessions/${sessionId}/messages`
    })
  }

  /**
   * 删除聊天会话
   */
  static deleteChatSession(sessionId: number) {
    return request.delete({
      url: `/api/admin/chat/sessions/${sessionId}`
    })
  }

  /**
   * 获取聊天统计数据
   */
  static getChatStats() {
    return request.get<{
      totalSessions: number
      totalMessages: number
      totalTokensUsed: number
      activeUsers: number
      todayMessages: number
    }>({
      url: '/api/admin/chat/stats'
    })
  }
}

// 聊天会话状态枚举
export const ChatSessionStatus = {
  DELETED: 0,
  ACTIVE: 1
} as const

// 聊天会话状态标签
export const ChatSessionStatusLabels = {
  [ChatSessionStatus.DELETED]: '已删除',
  [ChatSessionStatus.ACTIVE]: '正常'
} as const

// 消息角色标签
export const MessageRoleLabels = {
  user: '用户',
  assistant: 'AI助手',
  system: '系统'
} as const
