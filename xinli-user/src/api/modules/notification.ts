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

/** 通知类型枚举 */
export type NotificationType = 'ALL' | 'SYSTEM' | 'CRISIS' | 'LIKE'

/** 未读统计 */
export interface UnreadStats {
    SYSTEM: number
    CRISIS: number
    LIKE: number
}

/**
 * 获取通知列表（分页）
 * GET /api/notifications
 * @param type 通知类型筛选（ALL/SYSTEM/CRISIS/LIKE）
 */
export function getNotifications(page = 1, size = 20, type: NotificationType = 'ALL') {
    return api.get<PageResponse<UserNotification>>('/api/notifications', {
        params: { page, size, type }
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
 * 获取各类型未读数量统计
 * GET /api/notifications/unread-stats
 */
export function getUnreadStats() {
    return api.get<UnreadStats>('/api/notifications/unread-stats')
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

/**
 * 删除单条通知
 * DELETE /api/notifications/{id}
 */
export function deleteNotification(id: number) {
    return api.delete<void>(`/api/notifications/${id}`)
}

/**
 * 清空所有通知
 * DELETE /api/notifications/all
 */
export function deleteAllNotifications() {
    return api.delete<{ deletedCount: number }>('/api/notifications/all')
}
