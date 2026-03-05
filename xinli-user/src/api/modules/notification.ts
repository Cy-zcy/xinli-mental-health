/**
 * 用户通知相关 API
 * 对应后端 UserNotificationController
 */
import api from '../index'
import type { PageResponse } from '../types'

/** 通知类型 */
export interface UserNotification {
    id: number
    userId: number
    title: string
    content: string
    /** SYSTEM | CRISIS | LIKE */
    type: string
    /** 0 未读 / 1 已读 */
    isRead: number
    createdAt: string
}

/**
 * 获取通知列表（分页）
 * GET /api/notifications
 */
export function getNotifications(page = 1, size = 20) {
    return api.get<PageResponse<UserNotification>>('/api/notifications', {
        params: { page, size }
    })
}

/**
 * 获取未读通知数量
 * GET /api/notifications/unread-count
 */
export function getUnreadCount() {
    return api.get<{ count: number }>('/api/notifications/unread-count')
}

/**
 * 标记单条通知为已读
 * PUT /api/notifications/{id}/read
 */
export function markAsRead(id: number) {
    return api.put<void>(`/api/notifications/${id}/read`)
}

/**
 * 一键全部已读
 * PUT /api/notifications/read-all
 */
export function markAllRead() {
    return api.put<void>('/api/notifications/read-all')
}
