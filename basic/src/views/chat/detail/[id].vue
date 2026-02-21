<script setup lang="ts">
import { toast } from 'vue-sonner'
import { chatApi } from '@/api/modules'
import type { ChatMessage, ChatHistoryResponse } from '@/api/types'

definePage({
  meta: {
    title: '对话详情',
    auth: true,
  },
})

const router = useRouter()
const route = useRoute()

const sessionId = computed(() => Number(route.params.id))
const loading = ref(false)
const sessionData = ref<ChatHistoryResponse | null>(null)
const messages = ref<ChatMessage[]>([])

// 获取会话详情
async function loadSessionDetail() {
  try {
    loading.value = true
    const response = await chatApi.getSessionDetail(sessionId.value)
    sessionData.value = response
    messages.value = response.messages || []
  } catch (error: any) {
    console.error('加载会话详情失败:', error)
    toast.error('加载失败', {
      description: error.message || '无法加载对话详情',
    })
    router.back()
  } finally {
    loading.value = false
  }
}

// 继续对话
function continueChat() {
  router.push('/chat')
}

// 删除会话
async function deleteSession() {
  try {
    await chatApi.deleteSession(sessionId.value)
    toast.success('会话已删除')
    router.back()
  } catch (error: any) {
    console.error('删除会话失败:', error)
    toast.error('删除失败')
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
    second: '2-digit',
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

// 页面加载时获取会话详情
onMounted(() => {
  if (sessionId.value) {
    loadSessionDetail()
  } else {
    toast.error('无效的会话ID')
    router.back()
  }
})
</script>

<template>
  <FmPageLayout title="对话详情" :navbar="{ back: true }" @back="goBack">
    <div class="flex flex-1 flex-col">
      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center h-64">
        <div class="text-center">
          <div class="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
          <p class="text-gray-500">加载中...</p>
        </div>
      </div>

      <!-- 会话信息 -->
      <div v-else-if="sessionData" class="flex flex-1 flex-col">
        <!-- 会话头部信息 -->
        <div class="p-4 bg-card border-b">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                {{ sessionData.sessionTitle }}
              </h2>
              <div class="mt-2 space-y-1 text-sm text-gray-500">
                <div class="flex items-center gap-4">
                  <span>消息数量: {{ sessionData.totalMessages }}</span>
                  <span>Token消耗: {{ sessionData.totalTokens }}</span>
                </div>
              </div>
            </div>
            
            <div class="flex items-center gap-2 ml-4">
              <FmButton 
                size="sm"
                @click="continueChat"
              >
                继续对话
              </FmButton>
              <FmButton 
                variant="outline" 
                size="sm"
                @click="deleteSession"
              >
                删除
              </FmButton>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="flex-1 overflow-y-auto p-4 space-y-4">
          <div v-if="messages.length === 0" class="flex items-center justify-center h-32">
            <p class="text-gray-500">暂无消息记录</p>
          </div>

          <div v-for="message in messages" :key="message.id" class="flex gap-3">
            <!-- 用户消息 -->
            <div v-if="message.role === 'user'" class="flex justify-end w-full">
              <div class="max-w-[80%]">
                <div class="bg-blue-500 text-white rounded-lg p-3">
                  <p class="whitespace-pre-wrap">{{ message.content }}</p>
                </div>
                <div class="text-xs text-gray-500 mt-1 text-right">
                  {{ formatTime(message.createdAt) }}
                </div>
              </div>
            </div>
            
            <!-- AI消息 -->
            <div v-else class="flex w-full">
              <div class="flex gap-3 max-w-[80%]">
                <div class="w-8 h-8 rounded-full bg-gradient-to-br from-purple-400 to-blue-500 flex items-center justify-center flex-shrink-0">
                  <FmIcon name="i-carbon:chat-bot" class="text-4 text-white" />
                </div>
                <div class="flex-1">
                  <div class="bg-gray-100 dark:bg-gray-800 rounded-lg p-3">
                    <p class="whitespace-pre-wrap">{{ message.content }}</p>
                  </div>
                  <div class="text-xs text-gray-500 mt-1">
                    {{ formatTime(message.createdAt) }}
                    <span v-if="message.tokensUsed" class="ml-2">
                      · {{ message.tokensUsed }} tokens
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作 -->
        <div class="p-4 border-t bg-card">
          <div class="flex gap-3">
            <FmButton 
              class="flex-1"
              @click="continueChat"
            >
              继续这个对话
            </FmButton>
            <FmButton 
              variant="outline"
              @click="router.push('/chat')"
            >
              开始新对话
            </FmButton>
          </div>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="flex items-center justify-center h-64">
        <div class="text-center">
          <FmIcon name="i-carbon:warning" class="text-12 text-red-400 mb-4" />
          <p class="text-gray-500">加载失败</p>
          <FmButton class="mt-4" @click="loadSessionDetail">
            重试
          </FmButton>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
