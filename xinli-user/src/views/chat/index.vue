<script setup lang="ts">
import { toast } from 'vue-sonner'
import { chatApi } from '@/api/modules'
import { getResourceList, type ResourceItem } from '@/api/modules/resource'
import type { ChatSession, ChatMessage } from '@/api/types'

definePage({
  meta: {
    title: 'AI心理聊天',
    auth: true,
  },
})

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const sending = ref(false)
const sessions = ref<ChatSession[]>([])
const currentSession = ref<ChatSession | null>(null)
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement>()

// 资源推荐
const recommendedResources = ref<ResourceItem[]>([])
const showRecommendations = ref(false)
// 用来记录当前已推荐过推荐的 sessionId
const lastRecommendSessionId = ref<number | null>(null)

// 获取会话列表
async function loadSessions() {
  try {
    loading.value = true
    const response = await chatApi.getSessions({ page: 1, size: 20 })
    sessions.value = response.records || []
    
    // 如果有会话，选择第一个
    if (sessions.value.length > 0) {
      await selectSession(sessions.value[0])
    }
  } catch (error: any) {
    console.error('加载会话列表失败:', error)
    toast.error('加载失败', {
      description: error.message || '无法加载聊天记录',
    })
  } finally {
    loading.value = false
  }
}

// 选择会话
async function selectSession(session: ChatSession) {
  try {
    currentSession.value = session
    const response = await chatApi.getChatHistory({ sessionId: session.id })
    messages.value = response.messages || []
    
    // 滚动到底部
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error: any) {
    console.error('加载聊天记录失败:', error)
    toast.error('加载聊天记录失败')
  }
}

// 创建新会话
async function createNewSession() {
  if (!inputMessage.value.trim()) {
    toast.warning('请输入消息内容')
    return
  }

  try {
    sending.value = true
    const response = await chatApi.createSession({
      title: `新对话 ${new Date().toLocaleString()}`,
      firstMessage: inputMessage.value.trim(),
    })
    
    // 添加到会话列表
    sessions.value.unshift(response)
    
    // 选择新会话
    await selectSession(response)
    
    // 清空输入框
    inputMessage.value = ''
    
    toast.success('开始新对话')
  } catch (error: any) {
    console.error('创建会话失败:', error)
    toast.error('创建会话失败', {
      description: error.message || '请稍后重试',
    })
  } finally {
    sending.value = false
  }
}

// 发送消息
async function sendMessage() {
  if (!inputMessage.value.trim()) {
    return
  }

  if (!currentSession.value) {
    await createNewSession()
    return
  }

  try {
    sending.value = true
    const messageContent = inputMessage.value.trim()
    
    const userMessage: ChatMessage = {
      id: Date.now(),
      sessionId: currentSession.value.id,
      userId: userStore.userInfo?.id || 0,
      role: 'user',
      content: messageContent,
      createdAt: new Date().toISOString(),
    }
    messages.value.push(userMessage)
    inputMessage.value = ''
    
    nextTick(() => { scrollToBottom() })
    
    const response = await chatApi.sendMessage({
      sessionId: currentSession.value.id,
      content: messageContent,
    })
    
    const aiMessage: ChatMessage = {
      id: response.messageId,
      sessionId: currentSession.value.id,
      userId: 0,
      role: 'assistant',
      content: response.aiResponse,
      tokensUsed: response.tokensUsed,
      createdAt: response.timestamp,
    }
    messages.value.push(aiMessage)
    
    nextTick(() => { scrollToBottom() })

    // AI 回复完成后，展示资源推荐（每个会话只推荐一次）
    if (currentSession.value && lastRecommendSessionId.value !== currentSession.value.id) {
      fetchRecommendations()
      lastRecommendSessionId.value = currentSession.value.id
    }
    
  } catch (error: any) {
    console.error('发送消息失败:', error)
    toast.error('发送失败', {
      description: error.message || '请稍后重试',
    })
    messages.value = messages.value.filter(msg => msg.id !== Date.now())
  } finally {
    sending.value = false
  }
}

// 滚动到底部
function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 删除会话
async function deleteSession(session: ChatSession) {
  try {
    await chatApi.deleteSession(session.id)
    sessions.value = sessions.value.filter(s => s.id !== session.id)
    
    if (currentSession.value?.id === session.id) {
      currentSession.value = null
      messages.value = []
      
      // 选择下一个会话
      if (sessions.value.length > 0) {
        await selectSession(sessions.value[0])
      }
    }
    
    toast.success('会话已删除')
  } catch (error: any) {
    console.error('删除会话失败:', error)
    toast.error('删除失败')
  }
}

// 处理回车发送
function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// 获取资源推荐
async function fetchRecommendations() {
  try {
    const res = await getResourceList(1, 3)
    recommendedResources.value = res.records || []
    showRecommendations.value = recommendedResources.value.length > 0
    nextTick(() => { scrollToBottom() })
  } catch (e) {
    // 推荐失败不影响主流程
    console.warn('获取推荐资源失败')
  }
}

// 资源类型图标
function resourceIcon(type: string) {
  if (type === 'article') return 'i-ic:outline-article'
  if (type === 'audio') return 'i-ic:outline-headphones'
  return 'i-ic:outline-play-circle'
}

// 资源类型标签
function resourceTypeLabel(type: string) {
  if (type === 'article') return '文章'
  if (type === 'audio') return '音频'
  return '视频'
}

// 资源类型颜色
function resourceTypeColor(type: string) {
  if (type === 'article') return 'from-blue-400 to-blue-600'
  if (type === 'audio') return 'from-purple-400 to-purple-600'
  return 'from-rose-400 to-rose-600'
}

// 关闭推荐卡片
function dismissRecommendations() {
  showRecommendations.value = false
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
  } else {
    return date.toLocaleDateString()
  }
}

// 页面加载时获取会话列表
onMounted(() => {
  loadSessions()
})
</script>

<template>
  <FmPageLayout :navbar="false" tabbar>
    <div class="flex flex-1 flex-col h-full">
      <!-- 头部 -->
      <div class="flex items-center justify-between p-4 border-b bg-card">
        <h1 class="text-lg font-semibold">AI心理聊天</h1>
        <div class="flex items-center gap-2">
          <FmButton 
            variant="outline" 
            size="sm" 
            @click="router.push('/chat/history')"
          >
            历史记录
          </FmButton>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="flex flex-1 overflow-hidden">
        <!-- 会话列表 -->
        <div class="w-1/3 border-r bg-card overflow-y-auto">
          <div class="p-3">
            <FmButton 
              class="w-full" 
              size="sm" 
              @click="createNewSession"
              :loading="sending"
            >
              新建对话
            </FmButton>
          </div>
          
          <div class="space-y-1 p-2">
            <div 
              v-for="session in sessions" 
              :key="session.id"
              class="p-3 rounded-lg cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              :class="{ 'bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800': currentSession?.id === session.id }"
              @click="selectSession(session)"
            >
              <div class="font-medium text-sm truncate">
                {{ session.title }}
              </div>
              <div class="text-xs text-gray-500 mt-1">
                {{ formatTime(session.updatedAt) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 聊天内容 -->
        <div class="flex-1 flex flex-col">
          <!-- 消息列表 -->
          <div 
            ref="messagesContainer"
            class="flex-1 overflow-y-auto p-4 space-y-4"
          >
            <div v-if="!currentSession" class="flex items-center justify-center h-full text-gray-500">
              <div class="text-center">
                <FmIcon name="i-carbon:chat" class="text-12 mb-4" />
                <p>选择一个对话或开始新的对话</p>
              </div>
            </div>
            
            <div v-else-if="messages.length === 0" class="flex items-center justify-center h-full text-gray-500">
              <div class="text-center">
                <FmIcon name="i-carbon:chat-bot" class="text-12 mb-4" />
                <p>开始与AI心理助手对话吧</p>
                <p class="text-sm mt-2">我会倾听您的心声，提供专业的心理支持</p>
              </div>
            </div>

            <div v-for="message in messages" :key="message.id" class="flex gap-3">
              <!-- 用户消息 -->
              <div v-if="message.role === 'user'" class="flex justify-end w-full">
                <div class="max-w-[70%] bg-blue-500 text-white rounded-lg p-3">
                  <p class="whitespace-pre-wrap">{{ message.content }}</p>
                  <div class="text-xs opacity-70 mt-1">
                    {{ formatTime(message.createdAt) }}
                  </div>
                </div>
              </div>
              
              <!-- AI消息 -->
              <div v-else class="flex w-full">
                <div class="flex gap-3 max-w-[70%]">
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-purple-400 to-blue-500 flex items-center justify-center flex-shrink-0">
                    <FmIcon name="i-carbon:chat-bot" class="text-4 text-white" />
                  </div>
                  <div class="bg-gray-100 dark:bg-gray-800 rounded-lg p-3">
                    <p class="whitespace-pre-wrap">{{ message.content }}</p>
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
            
            <!-- 相关资源推荐卡片 -->
            <div v-if="showRecommendations && recommendedResources.length > 0" class="w-full mt-2">
              <div class="bg-gradient-to-br from-indigo-50 to-purple-50 dark:from-gray-800 dark:to-gray-900 rounded-2xl p-4 border border-indigo-100 dark:border-gray-700 shadow-sm">
                <div class="flex items-center justify-between mb-3">
                  <h4 class="text-sm font-semibold text-indigo-700 dark:text-indigo-300 flex items-center gap-1">
                    <FmIcon name="i-ic:outline-library-books" class="text-base" />
                    相关心理资源推荐
                  </h4>
                  <button
                    class="text-xs text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 px-2 py-0.5 rounded-full hover:bg-white/50 transition-colors"
                    @click="dismissRecommendations"
                  >✕ 关闭</button>
                </div>
                <div class="space-y-2">
                  <div
                    v-for="item in recommendedResources"
                    :key="item.id"
                    class="flex items-center gap-3 bg-white dark:bg-gray-800 rounded-xl p-3 cursor-pointer hover:shadow-md transition-all active:scale-98"
                    @click="router.push(`/resource/${item.id}`)"
                  >
                    <div :class="`flex-shrink-0 w-9 h-9 rounded-lg bg-gradient-to-br ${resourceTypeColor(item.type)} flex items-center justify-center`">
                      <FmIcon :name="resourceIcon(item.type)" class="text-white text-lg" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="text-sm font-medium text-gray-800 dark:text-white truncate">{{ item.title }}</div>
                      <div class="text-xs text-gray-400 mt-0.5 flex items-center gap-1">
                        <span class="px-1.5 py-0.5 rounded-md bg-gray-100 dark:bg-gray-700">{{ resourceTypeLabel(item.type) }}</span>
                        <span v-if="item.description" class="truncate">{{ item.description }}</span>
                      </div>
                    </div>
                    <FmIcon name="i-ic:outline-chevron-right" class="text-gray-400 flex-shrink-0" />
                  </div>
                </div>
                <div class="mt-3 text-center">
                  <button
                    class="text-xs text-indigo-500 hover:text-indigo-700 dark:text-indigo-400 font-medium"
                    @click="router.push('/resource')"
                  >查看更多资源 →</button>
                </div>
              </div>
            </div>

            <!-- 发送中提示 -->
            <div v-if="sending" class="flex w-full">
              <div class="flex gap-3 max-w-[70%]">
                <div class="w-8 h-8 rounded-full bg-gradient-to-br from-purple-400 to-blue-500 flex items-center justify-center flex-shrink-0">
                  <FmIcon name="i-carbon:chat-bot" class="text-4 text-white" />
                </div>
                <div class="bg-gray-100 dark:bg-gray-800 rounded-lg p-3">
                  <div class="flex items-center gap-2">
                    <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                    <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                    <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                    <span class="text-sm text-gray-500 ml-2">AI正在思考...</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="border-t p-4">
            <div class="flex gap-3">
              <FmInput
                v-model="inputMessage"
                placeholder="输入您想说的话..."
                class="flex-1"
                :disabled="sending"
                @keydown="handleKeydown"
              />
              <FmButton 
                @click="sendMessage" 
                :loading="sending"
                :disabled="!inputMessage.trim()"
              >
                发送
              </FmButton>
            </div>
            <div class="text-xs text-gray-500 mt-2 text-center">
              按 Enter 发送，Shift + Enter 换行
            </div>
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
