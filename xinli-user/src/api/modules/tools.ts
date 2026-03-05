/**
 * 心理工具 & 用户统计 API 模块
 */
import api from '@/api'

// ===================== 类型定义 =====================

export interface ToolRecordRequest {
    /** 工具类型：breathing | meditation */
    toolType: 'breathing' | 'meditation'
    /** 本次使用时长（秒） */
    durationSeconds: number
    /** 完成的循环/周期数（呼吸练习适用） */
    cycles?: number
    /** 是否完整完成 */
    completed: boolean
    /** 使用的模式名称，如 "4-7-8 呼吸法" */
    pattern?: string
}

export interface ToolRecord extends ToolRecordRequest {
    id: number
    userId: number
    createdAt: string
}

export interface UserStats {
    /** AI 对话会话数 */
    chatSessions: number
    /** 发布帖子数 */
    forumPosts: number
    /** 获得的总点赞数 */
    totalLikes: number
    /** 工具使用总分钟数 */
    toolMinutes: number
}

// ===================== API 函数 =====================

/**
 * 记录一次工具使用情况
 * POST /api/tools/record
 */
export function recordToolUsage(data: ToolRecordRequest): Promise<void> {
    return api.post('/api/tools/record', data)
}

/**
 * 获取工具使用历史记录
 * GET /api/tools/history?toolType=breathing&limit=20
 */
export function getToolHistory(toolType?: string, limit = 20): Promise<ToolRecord[]> {
    return api.get('/api/tools/history', {
        params: { toolType, limit },
    })
}

/**
 * 获取当前用户统计数据
 * GET /api/user/stats
 */
export function getUserStats(): Promise<UserStats> {
    return api.get('/api/user/stats')
}

// ===================== 成就系统 =====================

export interface Achievement {
    id: string
    title: string
    description: string
    icon: string
    color: string
    /** 是否已达成 */
    earned: boolean
    /** 当前进度值 */
    current: number
    /** 达成所需目标值 */
    target: number
    /** 进度百分比 0-100 */
    progress: number
}

/**
 * 获取当前用户成就列表（含已达成 + 未达成进度）
 * GET /api/achievements
 */
export function getAchievements(): Promise<Achievement[]> {
    return api.get('/api/achievements')
}
