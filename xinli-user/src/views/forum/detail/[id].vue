<script setup lang="ts">
import { toast } from 'vue-sonner'
import { forumApi } from '@/api/modules'
import type { ForumPost } from '@/api/types'

definePage({
  meta: {
    title: '帖子详情',
    auth: true,
  },
})

const router = useRouter()
const route = useRoute()

const postId = computed(() => Number(route.params.id))
const loading = ref(false)
const post = ref<ForumPost | null>(null)
const isLiked = ref(false)
const commentContent = ref('')
const commenting = ref(false)

// 获取帖子详情
async function loadPostDetail() {
  try {
    loading.value = true
    const response = await forumApi.getPostDetail(postId.value)
    post.value = response
    
    // 这里可以检查用户是否已点赞
    // isLiked.value = response.isLiked || false
    
  } catch (error: any) {
    console.error('加载帖子详情失败:', error)
    toast.error('加载失败', {
      description: error.message || '无法加载帖子详情',
    })
    router.back()
  } finally {
    loading.value = false
  }
}

// 点赞/取消点赞
async function toggleLike() {
  if (!post.value) return
  
  try {
    if (isLiked.value) {
      await forumApi.unlikePost(post.value.id)
      post.value.likeCount--
      isLiked.value = false
      toast.success('已取消点赞')
    } else {
      await forumApi.likePost(post.value.id)
      post.value.likeCount++
      isLiked.value = true
      toast.success('点赞成功')
    }
  } catch (error: any) {
    console.error('点赞操作失败:', error)
    toast.error('操作失败')
  }
}

// 发表评论
async function submitComment() {
  if (!commentContent.value.trim()) {
    toast.warning('请输入评论内容')
    return
  }
  
  try {
    commenting.value = true
    // 这里需要调用评论API，暂时模拟
    // await forumApi.createComment(postId.value, commentContent.value)
    
    commentContent.value = ''
    toast.success('评论发表成功')
    
    // 重新加载帖子详情以获取最新评论
    await loadPostDetail()
  } catch (error: any) {
    console.error('发表评论失败:', error)
    toast.error('发表失败')
  } finally {
    commenting.value = false
  }
}

// 分享帖子
function sharePost() {
  if (navigator.share) {
    navigator.share({
      title: post.value?.title,
      text: post.value?.content,
      url: window.location.href,
    })
  } else {
    // 复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href)
    toast.success('链接已复制到剪贴板')
  }
}

// 格式化时间
function formatTime(dateString: string) {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 格式化相对时间
function formatRelativeTime(dateString: string) {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 24小时内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

function goBack() {
  router.back()
}

// 页面加载时获取帖子详情
onMounted(() => {
  if (postId.value) {
    loadPostDetail()
  } else {
    toast.error('无效的帖子ID')
    router.back()
  }
})
</script>

<template>
  <FmPageLayout title="帖子详情" :navbar="{ back: true }" @back="goBack">
    <div class="flex flex-1 flex-col">
      <!-- 加载状态 -->
      <FmLoading v-if="loading" type="wave" text="加载中..." />

      <!-- 帖子内容 -->
      <div v-else-if="post" class="flex flex-1 flex-col">
        <!-- 帖子详情 -->
        <div class="p-4 bg-card border-b">
          <!-- 作者信息 -->
          <div class="flex items-center gap-3 mb-4">
            <div class="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center">
              <span class="text-white font-medium">
                {{ post.userNickname?.charAt(0) || '用' }}
              </span>
            </div>
            <div class="flex-1">
              <div class="font-medium">{{ post.userNickname || '匿名用户' }}</div>
              <div class="text-sm text-gray-500">
                {{ formatTime(post.createdAt) }}
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="px-3 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-sm rounded-full">
                {{ post.category }}
              </span>
            </div>
          </div>

          <!-- 帖子标题 -->
          <h1 class="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            {{ post.title }}
          </h1>

          <!-- 帖子内容 -->
          <div class="prose prose-sm max-w-none text-gray-700 dark:text-gray-300 mb-6">
            <p class="whitespace-pre-wrap leading-relaxed">{{ post.content }}</p>
          </div>

          <!-- 统计信息 -->
          <div class="flex items-center justify-between text-sm text-gray-500 mb-4">
            <div class="flex items-center gap-4">
              <div class="flex items-center gap-1">
                <FmIcon name="i-carbon:view" class="text-4" />
                <span>{{ post.viewCount || 0 }} 浏览</span>
              </div>
              <div class="flex items-center gap-1">
                <FmIcon name="i-carbon:favorite" class="text-4" />
                <span>{{ post.likeCount || 0 }} 点赞</span>
              </div>
            </div>
            <div>
              最后更新：{{ formatRelativeTime(post.updatedAt) }}
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex items-center gap-3">
            <FmButton 
              :variant="isLiked ? 'default' : 'outline'"
              size="sm"
              @click="toggleLike"
            >
              <FmIcon 
                :name="isLiked ? 'i-carbon:favorite-filled' : 'i-carbon:favorite'" 
                class="mr-1"
                :class="{ 'text-red-500': isLiked }"
              />
              {{ isLiked ? '已点赞' : '点赞' }}
            </FmButton>
            
            <FmButton variant="outline" size="sm" @click="sharePost">
              <FmIcon name="i-carbon:share" class="mr-1" />
              分享
            </FmButton>
          </div>
        </div>

        <!-- 评论区域 -->
        <div class="flex-1 flex flex-col">
          <!-- 评论列表标题 -->
          <div class="p-4 border-b bg-card">
            <h3 class="font-semibold">评论区</h3>
            <p class="text-sm text-gray-500 mt-1">暂无评论功能，敬请期待</p>
          </div>

          <!-- 评论列表 -->
          <div class="flex-1 overflow-y-auto p-4">
            <div class="flex items-center justify-center h-32 text-gray-500">
              <div class="text-center">
                <FmIcon name="i-carbon:chat" class="text-8 mb-2" />
                <p>暂无评论</p>
              </div>
            </div>
          </div>

          <!-- 评论输入框 -->
          <div class="p-4 border-t bg-card">
            <div class="flex gap-3">
              <FmInput
                v-model="commentContent"
                placeholder="写下您的想法..."
                class="flex-1"
                :disabled="commenting"
              />
              <FmButton 
                @click="submitComment"
                :loading="commenting"
                :disabled="!commentContent.trim()"
              >
                发表
              </FmButton>
            </div>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="flex items-center justify-center h-64">
        <div class="text-center">
          <FmIcon name="i-carbon:warning" class="text-12 text-red-400 mb-4" />
          <p class="text-gray-500">加载失败</p>
          <FmButton class="mt-4" @click="loadPostDetail">
            重试
          </FmButton>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
