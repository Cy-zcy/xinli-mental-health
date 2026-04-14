/**
 * 心理测评相关API
 * 对应后端 UserAssessmentController
 */
import api from '../index'

// ======== 类型定义 ========

export interface AssessmentOption {
  id: number
  questionId: number
  content: string
  score: number
  sortOrder: number
}

export interface AssessmentQuestionDTO {
  id: number
  content: string
  type: string
  sortOrder: number
  options: AssessmentOption[]
}

export interface AssessmentItem {
  id: number
  title: string
  description: string
  status: number
  createdAt: string
}

export interface AssessmentDetailDTO {
  id: number
  title: string
  description: string
  questions: AssessmentQuestionDTO[]
}

export interface AssessmentResultDTO {
  recordId: number
  assessmentId: number
  assessmentTitle: string
  totalScore: number
  resultSummary: string
  resultDetails: string
  createdAt: string
  // 维度分析
  dimensionScores?: Record<string, number>
  dimensionNames?: string[]
  dimensionValues?: number[]
  dimensionChanges?: Record<string, number>
}

export interface SubmitAssessmentRequest {
  assessmentId: number
  answers: Record<number, number> // key: questionId, value: optionId
}

export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// ======== API 函数 ========

/**
 * 获取已发布的问卷列表（分页）
 * GET /api/assessment/list
 */
export function getAssessmentList(page = 1, size = 10) {
  return api.get<PageResponse<AssessmentItem>>('/api/assessment/list', {
    params: { page, size },
  })
}

/**
 * 获取问卷详情（含题目和选项）
 * GET /api/assessment/{id}
 */
export function getAssessmentDetail(id: number) {
  return api.get<AssessmentDetailDTO>(`/api/assessment/${id}`)
}

/**
 * 提交答卷，获取测评结果
 * POST /api/assessment/submit
 */
export function submitAssessment(data: SubmitAssessmentRequest) {
  return api.post<AssessmentResultDTO>('/api/assessment/submit', data)
}

/**
 * 获取当前登录用户的历史测评记录
 * GET /api/assessment/history
 */
export function getUserAssessmentHistory() {
  return api.get<AssessmentResultDTO[]>('/api/assessment/history')
}

/**
 * 获取单条测评结果详情（含维度分析）
 * GET /api/assessment/result/{recordId}
 */
export function getAssessmentResult(recordId: number) {
  return api.get<AssessmentResultDTO>(`/api/assessment/result/${recordId}`)
}
