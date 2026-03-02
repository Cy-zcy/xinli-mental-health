<script setup lang="ts">
import { toast } from 'vue-sonner'
import { chatApi } from '@/api/modules'
import type { ChatSession } from '@/api/types'

definePage({
  meta: {
    title: '聊天历史',
    auth: true,
  },
})

const router = useRouter()

const loading = ref(false)
const sessions = ref<ChatSession[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const hasMore = ref(true)

// 获取会话列表
async function loadSessions(page = 1, append = false) {
  try {
    loading.value = true
    const response = await chatApi.getSessions({ 
      page, 
      size: pageSize.value 
    })
    
    if (append) {
      sessions.value.push(...(response.records || []))
    } else {
      sessions.value = response.records || []
    }
    
    total.value = response.total || 0
    currentPage.value = page
    hasMore.value = (response.records?.length || 0) === pageSize.value
    
  } catch (error: any) {
    console.error('加载会话列表失败:', error)
    toast.error('加载失败', {
      description: error.message || '无法加载聊天记录',
    })
  } finally {
    loading.value = false
  }
}

// 加载更多
async function loadMore() {
  if (!hasMore.value || loading.value) return
  await loadSessions(currentPage.value + 1, true)
}

// 删除会话
async function deleteSession(session: ChatSession, index: number) {
  try {
    await chatApi.deleteSession(session.id)
    sessions.value.splice(index, 1)
    total.value--
    toast.success('会话已删除')
  } catch (error: any) {
    console.error('删除会话失败:', error)
    toast.error('删除失败')
  }
}

// 查看会话详情
function viewSession(session: ChatSession) {
  router.push(`/chat/detail/${session.id}`)
}

// 继续对话
function continueChat(session: ChatSession) {
  router.push('/chat')
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
  } else if (diff < 604800000) { // 7天内
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

function goBack() {
  router.back()
}

// 页面加载时获取会话列表
onMounted(() => {
  loadSessions()
})
</script>

<template>
  <FmPageLayout title="聊天历史" :navbar="{ back: true }" @back="goBack">
    <div class="flex flex-1 flex-col">
      <!-- 统计信息 -->
      <div class="p-4 bg-card border-b">
        <div class="text-center">
          <div class="text-2xl font-bold text-blue-500">{{ total }}</div>
          <div class="text-sm text-gray-500">总对话次数</div>
        </div>
      </div>

      <!-- 会话列表 -->
      <div class="flex-1 overflow-y-auto">
        <FmLoading v-if="loading && sessions.length === 0" type="wave" text="加载中..." />

        <div v-else-if="sessions.length === 0" class="flex items-center justify-center h-64">
          <div class="text-center">
            <FmIcon name="i-carbon:chat" class="text-12 text-gray-400 mb-4" />
            <p class="text-gray-500">暂无聊天记录</p>
            <FmButton class="mt-4" @click="router.push('/chat')">
              开始对话
            </FmButton>
          </div>
        </div>

        <div v-else class="space-y-2 p-4">
          <div 
            v-for="(session, index) in sessions" 
            :key="session.id"
            class="bg-card rounded-lg p-4 border hover:shadow-md transition-shadow"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1 min-w-0">
                <h3 class="font-medium text-gray-900 dark:text-gray-100 truncate">
                  {{ session.title }}
                </h3>
                <div class="flex items-center gap-4 mt-2 text-sm text-gray-500">
                  <span>{{ formatRelativeTime(session.updatedAt) }}</span>
                  <span>{{ formatTime(session.createdAt) }}</span>
                </div>
              </div>
              
              <div class="flex items-center gap-2 ml-4">
                <FmButton 
                  variant="outline" 
                  size="sm"
                  @click="viewSession(session)"
                >
                  查看
                </FmButton>
                <FmButton 
                  size="sm"
                  @click="continueChat(session)"
                >
                  继续
                </FmButton>
                <FmButton 
                  variant="outline" 
                  size="sm"
                  @click="deleteSession(session, index)"
                >
                  <FmIcon name="i-carbon:trash-can" class="text-4" />
                </FmButton>
              </div>
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="hasMore" class="text-center py-4">
            <FmButton 
              variant="outline" 
              :loading="loading"
              @click="loadMore"
            >
              加载更多
            </FmButton>
          </div>

          <div v-else-if="sessions.length > 0" class="text-center py-4 text-gray-500 text-sm">
            已显示全部记录
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
