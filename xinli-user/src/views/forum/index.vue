<script setup lang="ts">
import { toast } from 'vue-sonner'
import { forumApi } from '@/api/modules'
import type { ForumPost, ForumCategory } from '@/api/types'

definePage({
  meta: {
    title: '心理论坛',
    auth: true,
  },
})

const router = useRouter()

const loading = ref(false)
const refreshing = ref(false)
const posts = ref<ForumPost[]>([])
const categories = ref<ForumCategory[]>([])
const currentCategory = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
const searchKeyword = ref('')

// 获取论坛分类
async function loadCategories() {
  try {
    const response = await forumApi.getCategories()
    // 转换后端数据格式为前端期望的格式
    const convertedCategories = response.map(cat => ({
      value: cat.value,
      label: cat.label,
      description: cat.description
    }))
    categories.value = [
      { value: '', label: '全部', description: '所有分类' },
      ...convertedCategories
    ]
  } catch (error: any) {
    console.error('加载分类失败:', error)
    // 提供默认分类
    categories.value = [
      { value: '', label: '全部', description: '所有分类' },
      { value: 'emotion', label: '情感倾诉', description: '分享情感困扰，寻求理解和支持' },
      { value: 'experience', label: '经验分享', description: '分享心理健康相关的经验和心得' }
    ]
  }
}

// 获取帖子列表
async function loadPosts(page = 1, append = false) {
  try {
    loading.value = true
    const response = await forumApi.getPosts({
      page,
      size: pageSize.value,
      category: currentCategory.value || undefined,
      keyword: searchKeyword.value || undefined,
    })
    
    if (append) {
      posts.value.push(...(response.records || []))
    } else {
      posts.value = response.records || []
    }
    
    currentPage.value = page
    hasMore.value = (response.records?.length || 0) === pageSize.value
    
  } catch (error: any) {
    console.error('加载帖子失败:', error)
    toast.error('加载失败', {
      description: error.message || '无法加载帖子列表',
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 刷新数据
async function refresh() {
  refreshing.value = true
  currentPage.value = 1
  await loadPosts(1, false)
}

// 加载更多
async function loadMore() {
  if (!hasMore.value || loading.value) return
  await loadPosts(currentPage.value + 1, true)
}

// 切换分类
async function switchCategory(categoryId: string) {
  currentCategory.value = categoryId
  currentPage.value = 1
  await loadPosts(1, false)
}

// 搜索帖子
async function searchPosts() {
  currentPage.value = 1
  await loadPosts(1, false)
}

// 点赞帖子
async function toggleLike(post: ForumPost, index: number) {
  try {
    // 这里需要根据后端API判断是点赞还是取消点赞
    // 暂时假设总是点赞
    await forumApi.likePost(post.id)
    posts.value[index].likeCount++
    toast.success('点赞成功')
  } catch (error: any) {
    console.error('点赞失败:', error)
    toast.error('操作失败')
  }
}

// 查看帖子详情
function viewPost(post: ForumPost) {
  router.push(`/forum/detail/${post.id}`)
}

// 发布新帖子
function createPost() {
  router.push('/forum/publish')
}

// 格式化时间
function formatTime(dateString: string) {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 24小时内
    return `${Math.floor(diff / 3600000)}小时前`
  } else if (diff < 604800000) { // 7天内
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 获取分类名称
function getCategoryName(categoryValue: string) {
  const category = categories.value.find(c => c.value === categoryValue)
  return category?.label || '未知分类'
}

// 页面加载时获取数据
onMounted(async () => {
  await loadCategories()
  await loadPosts()
})
</script>

<template>
  <FmPageLayout :navbar="false" tabbar>
    <div class="flex flex-1 flex-col bg-gray-50 dark:bg-gray-900">
      <!-- 头部 -->
      <div class="sticky top-0 z-10 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
        <div class="p-4">
          <div class="flex items-center justify-between mb-4">
            <h1 class="text-xl font-bold text-gray-900 dark:text-white">心理论坛</h1>
            <FmButton size="sm" @click="createPost" class="bg-blue-500 hover:bg-blue-600">
              <FmIcon name="i-carbon:add" class="mr-1" />
              发帖
            </FmButton>
          </div>

          <!-- 搜索框 -->
          <div class="flex gap-2">
            <FmInput
              v-model="searchKeyword"
              placeholder="搜索帖子..."
              class="flex-1"
              @keyup.enter="searchPosts"
            />
            <FmButton variant="outline" @click="searchPosts">
              <FmIcon name="i-carbon:search" class="mr-1" />
              搜索
            </FmButton>
          </div>
        </div>

        <!-- 分类标签 -->
        <div class="px-4 pb-3">
          <div class="flex gap-2 overflow-x-auto scrollbar-hide">
            <FmButton
              v-for="category in categories"
              :key="category.value"
              :variant="currentCategory === category.value ? 'default' : 'outline'"
              size="sm"
              class="whitespace-nowrap"
              @click="switchCategory(category.value)"
            >
              {{ category.label }}
            </FmButton>
          </div>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="flex-1 overflow-y-auto">
        <!-- 下拉刷新提示 -->
        <FmLoading v-if="refreshing" type="wave" :size="30" text="刷新中..." />

        <!-- 加载状态 -->
        <FmLoading v-if="loading && posts.length === 0" type="wave" text="加载中..." />

        <!-- 空状态 -->
        <div v-else-if="posts.length === 0" class="flex items-center justify-center h-64">
          <div class="text-center">
            <FmIcon name="i-carbon:document" class="text-12 text-gray-400 mb-4" />
            <p class="text-gray-500">暂无帖子</p>
            <FmButton class="mt-4" @click="createPost">
              发布第一个帖子
            </FmButton>
          </div>
        </div>

        <!-- 帖子列表 -->
        <div v-else class="p-4 space-y-4">
          <div
            v-for="(post, index) in posts"
            :key="post.id"
            class="bg-white dark:bg-gray-800 rounded-xl p-5 shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-md hover:border-blue-200 dark:hover:border-blue-600 transition-all duration-200 cursor-pointer"
            @click="viewPost(post)"
          >
            <!-- 帖子头部 -->
            <div class="flex items-start justify-between mb-4">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center shadow-sm">
                  <span class="text-white text-sm font-semibold">
                    {{ post.userNickname?.charAt(0) || '用' }}
                  </span>
                </div>
                <div>
                  <div class="font-semibold text-gray-900 dark:text-white text-sm">{{ post.userNickname || '匿名用户' }}</div>
                  <div class="text-xs text-gray-500 dark:text-gray-400">{{ formatTime(post.createdAt) }}</div>
                </div>
              </div>

              <div class="flex items-center gap-2">
                <span class="px-3 py-1 bg-gradient-to-r from-blue-50 to-purple-50 dark:from-blue-900/30 dark:to-purple-900/30 text-blue-600 dark:text-blue-400 text-xs rounded-full font-medium border border-blue-100 dark:border-blue-800">
                  {{ getCategoryName(post.category) }}
                </span>
              </div>
            </div>

            <!-- 帖子内容 -->
            <div class="mb-4">
              <h3 class="font-bold text-gray-900 dark:text-white mb-2 line-clamp-2 text-lg leading-tight">
                {{ post.title }}
              </h3>
              <p class="text-gray-600 dark:text-gray-300 text-sm line-clamp-3 leading-relaxed">
                {{ post.content }}
              </p>
            </div>

            <!-- 帖子统计 -->
            <div class="flex items-center justify-between pt-3 border-t border-gray-100 dark:border-gray-700">
              <div class="flex items-center gap-6">
                <div class="flex items-center gap-1.5 text-gray-500 dark:text-gray-400">
                  <FmIcon name="i-carbon:view" class="text-4" />
                  <span class="text-sm">{{ post.viewCount || 0 }}</span>
                </div>
                <button
                  class="flex items-center gap-1.5 text-gray-500 dark:text-gray-400 hover:text-red-500 dark:hover:text-red-400 transition-colors"
                  @click.stop="toggleLike(post, index)"
                >
                  <FmIcon name="i-carbon:favorite" class="text-4" />
                  <span class="text-sm">{{ post.likeCount || 0 }}</span>
                </button>
              </div>

              <div class="text-xs text-gray-400 dark:text-gray-500">
                {{ formatTime(post.updatedAt) }}
              </div>
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="hasMore" class="text-center py-6">
            <FmButton
              variant="outline"
              :loading="loading"
              @click="loadMore"
              class="px-8 py-2 rounded-full border-2 hover:bg-blue-50 dark:hover:bg-blue-900/20"
            >
              <FmIcon v-if="!loading" name="i-carbon:chevron-down" class="mr-2" />
              {{ loading ? '加载中...' : '加载更多' }}
            </FmButton>
          </div>

          <div v-else-if="posts.length > 0" class="text-center py-6">
            <div class="inline-flex items-center gap-2 text-gray-400 dark:text-gray-500 text-sm">
              <div class="w-8 h-px bg-gray-300 dark:bg-gray-600"></div>
              <span>已显示全部帖子</span>
              <div class="w-8 h-px bg-gray-300 dark:bg-gray-600"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 悬浮发帖按钮 -->
      <div class="fixed bottom-24 right-6 z-20">
        <FmButton
          class="w-16 h-16 rounded-full shadow-xl bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 border-0 transform hover:scale-105 transition-all duration-200"
          @click="createPost"
        >
          <FmIcon name="i-carbon:add" class="text-7 text-white" />
        </FmButton>
      </div>
    </div>
  </FmPageLayout>
</template>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
