/**
 * AI聊天相关API
 * 对应后端 ChatController
 */
import api from '../index'
import type {
  ChatSession,
  SessionInfo,
  ChatMessage,
  MessageHistory,
  SendMessageRequest,
  SendMessageResponse,
  CreateSessionRequest,
  ChatHistoryResponse,
  PageResponse,
} from '../types'

/**
 * 发送消息给AI
 * POST /api/chat/send
 */
export function sendMessage(data: SendMessageRequest) {
  return api.post<SendMessageResponse>('/api/chat/send', data)
}

/**
 * 创建新的聊天会话
 * POST /api/chat/session
 */
export function createSession(data: CreateSessionRequest) {
  return api.post<ChatSession>('/api/chat/session', data)
}

/**
 * 获取用户的会话列表
 * GET /api/chat/sessions
 */
export function getSessions(params: {
  page?: number
  size?: number
} = {}) {
  return api.get<PageResponse<SessionInfo>>('/api/chat/sessions', {
    params: {
      page: 1,
      size: 10,
      ...params
    }
  })
}

/**
 * 获取聊天历史记录
 * GET /api/chat/history
 */
export function getChatHistory(params: {
  sessionId: number
  page?: number
  size?: number
}) {
  return api.get<ChatHistoryResponse>('/api/chat/history', {
    params: {
      page: 1,
      size: 50,
      ...params
    }
  })
}

/**
 * 获取会话详情
 * GET /api/chat/session/{sessionId}
 */
export function getSessionDetail(sessionId: number) {
  return api.get<ChatHistoryResponse>(`/api/chat/session/${sessionId}`)
}

/**
 * 删除会话
 * DELETE /api/chat/session/{sessionId}
 */
export function deleteSession(sessionId: number) {
  return api.delete(`/api/chat/session/${sessionId}`)
}

/**
 * 测试AI连接
 * GET /api/chat/test
 */
export function testConnection() {
  return api.get<boolean>('/api/chat/test')
}
