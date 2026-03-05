import http from '@/utils/http'

/**
 * AI 人设对象接口
 */
export interface AiCharacter {
    id?: number
    name: string
    avatar: string
    greeting: string
    background: string
    personality: string
    rules: string
    isActive: number
    createdAt?: string
    updatedAt?: string
}

/**
 * 分页数据响应
 */
export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

export const aiCharacterApi = {
    /**
   * 分页获取角色列表
   */
    getPage(page: number, size: number, keyword?: string) {
        return http.get<PageResult<AiCharacter>>({
            url: '/api/admin/chat/characters',
            params: { page, size, keyword }
        } as any)
    },

    /**
     * 获取详情
     */
    getById(id: number) {
        return http.get<AiCharacter>({
            url: `/api/admin/chat/characters/${id}`
        })
    },

    /**
     * 新增角色
     */
    create(data: Partial<AiCharacter>) {
        return http.post<void>({
            url: '/api/admin/chat/characters',
            data
        } as any)
    },

    /**
     * 更新角色
     */
    update(id: number, data: Partial<AiCharacter>) {
        return http.put<void>({
            url: `/api/admin/chat/characters/${id}`,
            data
        } as any)
    },

    /**
     * 删除角色
     */
    delete(id: number) {
        // 根据项目常见的封装，如果 delete 不存在，可能是 del 或 request({method: 'DELETE'})
        // 我们先用 delete，或者 request({ method: 'delete' }) 更稳妥
        return http.request<void>({
            method: 'delete',
            url: `/api/admin/chat/characters/${id}`
        } as any)
    }
}
