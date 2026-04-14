import request from '@/utils/http'

// 资源项接口定义
export interface ResourceItem {
    id: number
    title: string
    type: string
    content: string
    coverUrl?: string
    mediaUrl?: string
    tags?: string
    viewCount: number
    isPublished: number
    duration?: number
    authorId?: number
    createdAt: string
    updatedAt: string
}

// 资源查询参数
export interface ResourceQueryParams {
    page?: number
    size?: number
    type?: string
    keyword?: string
}

// 分页返回结果
export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

// 新增/修改资源的请求参数
export interface ResourceFormData {
    title: string
    type: 'article' | 'audio' | 'video'
    content?: string
    coverUrl?: string
    mediaUrl?: string
    tags?: string
    isPublished?: number
    duration?: number
}

// 服务调用类
export class ResourceService {
    /**
     * 获取管理端资源列表 (分页)
     */
    static getResourceList(params: ResourceQueryParams = {}) {
        return request.get<PageResult<ResourceItem>>({
            url: '/api/admin/resource/list',
            params
        } as any)
    }

    /**
     * 获取资源详情
     */
    static getResourceDetail(id: number) {
        return request.get<ResourceItem>({
            url: `/api/admin/resource/${id}`
        } as any)
    }

    /**
     * 新增发布资源
     */
    static createResource(data: ResourceFormData) {
        return request.post<ResourceItem>({
            url: '/api/admin/resource',
            data
        } as any)
    }

    /**
     * 更新资源
     */
    static updateResource(id: number, data: ResourceFormData) {
        return request.put<ResourceItem>({
            url: `/api/admin/resource/${id}`,
            data
        } as any)
    }

    /**
     * 更新资源状态（上架/下架）
     */
    static updateResourceStatus(id: number, status: number) {
        return request.put<void>({
            url: `/api/admin/resource/${id}/status`,
            data: { status }   // 后端用 @RequestBody 接收，必须用 data 而非 params
        } as any)
    }

    /**
     * 删除资源
     */
    static deleteResource(id: number) {
        return request.del<void>({
            url: `/api/admin/resource/${id}`
        } as any)
    }
}
