/**
 * 论坛相关API
 * 对应后端 UserForumController
 */
import api from '../index'
import type {
  ForumPost,
  PostDetail,
  CreatePostRequest,
  PostListParams,
  ForumCategory,
  PageResponse,
} from '../types'

/**
 * 获取帖子列表
 * GET /api/forum/posts
 */
export function getPosts(params: PostListParams = {}) {
  return api.get<PageResponse<ForumPost>>('/api/forum/posts', {
    params: {
      page: 1,
      size: 10,
      ...params
    }
  })
}

/**
 * 发布帖子
 * POST /api/forum/posts
 */
export function createPost(data: CreatePostRequest) {
  return api.post<ForumPost>('/api/forum/posts', data)
}

/**
 * 获取帖子详情
 * GET /api/forum/posts/{postId}
 */
export function getPostDetail(postId: number) {
  return api.get<PostDetail>(`/api/forum/posts/${postId}`)
}

/**
 * 点赞帖子
 * POST /api/forum/posts/{postId}/like
 */
export function likePost(postId: number) {
  return api.post<{ liked: boolean; likeCount: number }>(`/api/forum/posts/${postId}/like`)
}

/**
 * 取消点赞
 * DELETE /api/forum/posts/{postId}/like
 */
export function unlikePost(postId: number) {
  return api.delete<{ liked: boolean; likeCount: number }>(`/api/forum/posts/${postId}/like`)
}

/**
 * 获取论坛分类列表
 * GET /api/forum/categories
 */
export function getCategories() {
  return api.get<ForumCategory[]>('/api/forum/categories')
}

/**
 * 搜索帖子（复用帖子列表接口，传入 keyword 参数）
 * GET /api/forum/posts?keyword=xxx （后端 /api/forum/search 不存在，由 /api/forum/posts 的 keyword 参数承担搜索功能）
 */
export function searchPosts(params: {
  keyword: string
  page?: number
  size?: number
  category?: string
}) {
  return api.get<PageResponse<ForumPost>>('/api/forum/posts', {
    params: {
      page: 1,
      size: 10,
      ...params
    }
  })
}

/**
 * 发表评论
 * POST /api/forum/posts/{postId}/comments
 */
export function createComment(postId: number, content: string) {
  return api.post<{
    id: number
    userId: number
    content: string
    status: number
    createdAt: string
    userNickname: string
    userAvatar: string
  }>(`/api/forum/posts/${postId}/comments`, { content })
}

/**
 * 获取我的帖子列表
 * GET /api/forum/my-posts
 * 包含所有状态的帖子（待审核、已通过）
 */
export function getMyPosts(params: { page?: number; size?: number } = {}) {
  return api.get<PageResponse<ForumPost>>('/api/forum/my-posts', {
    params: {
      page: 1,
      size: 10,
      ...params
    }
  })
}
