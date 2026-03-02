import request from '@/api'

// ==================== 类型定义 ====================

export interface ResourceItem {
    id: number
    title: string
    type: 'article' | 'audio' | 'video'
    coverUrl?: string
    tags?: string
    description?: string
    viewCount: number
    createdAt: string
}

export interface ResourceDetail extends ResourceItem {
    content?: string       // 文章正文（article）
    resourceUrl?: string   // 音频/视频 URL
}

export interface ResourcePage {
    records: ResourceItem[]
    total: number
    current: number
    size: number
}

// ==================== API 函数（命名导出，与 forum.ts 保持一致）====================

/**
 * 获取上架资源列表（分页，可按类型筛选）
 * type: 'article' | 'audio' | 'video' | undefined（不传则全部）
 */
export function getResourceList(page = 1, size = 10, type?: string): Promise<ResourcePage> {
    const params: Record<string, any> = { page, size }
    if (type)
        params.type = type
    return request.get('/api/resource/list', { params })
}

/**
 * 获取资源详情
 */
export function getResourceDetail(id: number): Promise<ResourceDetail> {
    return request.get(`/api/resource/${id}`)
}
