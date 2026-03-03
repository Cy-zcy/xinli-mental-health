import request from '@/utils/http'

// 测评问卷接口定义
export interface AssessmentItem {
    id: number
    title: string
    description: string
    status: number // 1-正常 0-下架
    createdAt: string
    updatedAt: string
}

// 分页返回结果
export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

export interface AssessmentFormData {
    title: string
    description?: string
}

// 服务调用类
export class AssessmentService {
    /**
     * 获取管理端测评列表 (分页)
     */
    static getAssessmentList(params: { page?: number; size?: number } = {}) {
        return request.get<PageResult<AssessmentItem>>({
            url: '/api/admin/assessment/list',
            params
        } as any)
    }

    /**
     * 新增测评问卷
     */
    static createAssessment(data: AssessmentFormData) {
        return request.post<AssessmentItem>({
            url: '/api/admin/assessment',
            data
        } as any)
    }

    /**
     * 更新问卷状态（上架/下架）
     */
    static updateAssessmentStatus(id: number, status: number) {
        return request.put<void>({
            url: `/api/admin/assessment/${id}/status`,
            params: { status }
        } as any)
    }

    /**
     * 删除问卷
     */
    static deleteAssessment(id: number) {
        return request.del<void>({
            url: `/api/admin/assessment/${id}`
        } as any)
    }

    // ========== 题目/选项相关接口后续补充 ==========
}
