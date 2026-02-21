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
 * 搜索帖子
 * GET /api/forum/search
 */
export function searchPosts(params: {
  keyword: string
  page?: number
  size?: number
  category?: string
}) {
  return api.get<PageResponse<ForumPost>>('/api/forum/search', {
    params: {
      page: 1,
      size: 10,
      ...params
    }
  })
}
