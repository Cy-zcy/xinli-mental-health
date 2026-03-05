<script setup lang="ts">
import { toast } from 'vue-sonner'
import { chatApi } from '@/api/modules'
import { getResourceList, type ResourceItem } from '@/api/modules/resource'
import type { ChatSession, ChatMessage, AiCharacter } from '@/api/types'

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

// AI 角色列表
const characters = ref<AiCharacter[]>([])
const selectedCharacterId = ref<number | undefined>(undefined)

// 加载 AI 角色
async function loadCharacters() {
  try {
    const res = await chatApi.getCharacters()
    characters.value = res || []
    if (characters.value.length > 0) {
      // 默认选中第一个活跃角色
      selectedCharacterId.value = characters.value[0].id
    }
  } catch (error) {
    console.warn('获取 AI 角色列表失败:', error)
  }
}

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
      title: `新对话`,
      characterId: selectedCharacterId.value,
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
      } else {
        // 如果删完了，重置选中角色
        if (characters.value.length > 0) {
          selectedCharacterId.value = characters.value[0].id
        }
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

// 页面加载时获取会话列表及角色
onMounted(() => {
  loadCharacters()
  loadSessions()
})
</script>

<template>
  <FmPageLayout :navbar="false" tabbar>
    <!-- 整体背景改用更柔和的渐变底色，搭配暗模式适配 -->
    <div class="flex flex-1 flex-col h-full bg-slate-50/50 dark:bg-slate-900/50 relative">
      <!-- 增加背景光晕效果 (Orb) -->
      <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0">
        <div class="absolute -top-[10%] -left-[10%] w-[40%] h-[40%] rounded-full bg-blue-400/10 blur-[80px] mix-blend-multiply dark:bg-blue-900/20 dark:mix-blend-screen"></div>
        <div class="absolute top-[20%] -right-[10%] w-[30%] h-[50%] rounded-full bg-purple-400/10 blur-[80px] mix-blend-multiply dark:bg-purple-900/20 dark:mix-blend-screen"></div>
      </div>

      <!-- 头部 -->
      <div class="flex items-center justify-between px-5 py-4 bg-white/60 dark:bg-slate-900/60 backdrop-blur-md border-b border-slate-200/50 dark:border-slate-800/50 z-10 sticky top-0 shadow-sm">
        <h1 class="text-xl font-bold bg-gradient-to-r from-slate-800 to-slate-600 dark:from-slate-100 dark:to-slate-300 bg-clip-text text-transparent tracking-tight">AI 心理咨询</h1>
        <div class="flex items-center gap-2">
          <FmButton 
            variant="secondary" 
            size="sm" 
            class="rounded-full shadow-sm bg-white/80 dark:bg-slate-800/80 hover:bg-slate-100 dark:hover:bg-slate-700 backdrop-blur-sm border-0 transition-all active:scale-95"
            @click="router.push('/chat/history')"
          >
            <FmIcon name="i-carbon:time" class="mr-1" />
            历史记录
          </FmButton>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="flex flex-1 overflow-hidden z-10 mx-2 mb-2 mt-2 gap-2">
        <!-- 会话列表 (左侧抽屉/列表) -->
        <div class="w-1/3 bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl rounded-2xl border border-white/50 dark:border-slate-700/50 shadow-sm flex flex-col">
          <div class="p-3 border-b border-slate-100 dark:border-slate-800/50">
            <FmButton 
              class="w-full rounded-xl shadow-sm bg-gradient-to-r from-blue-500 to-indigo-500 hover:from-blue-600 hover:to-indigo-600 text-white font-medium border-0 transition-transform active:scale-95" 
              size="sm" 
              @click="currentSession = null; messages = []"
              :loading="sending"
            >
              <FmIcon name="i-carbon:add-alt" class="mr-1" />
              新对话
            </FmButton>
          </div>
          
          <div class="flex-1 overflow-y-auto p-2 space-y-1.5 scrollbar-hide">
            <div 
              v-for="session in sessions" 
              :key="session.id"
              class="p-3 rounded-xl cursor-pointer transition-all duration-200"
              :class="[
                currentSession?.id === session.id 
                  ? 'bg-blue-50/80 dark:bg-blue-900/30 border border-blue-200/60 dark:border-blue-800/50 shadow-[0_2px_8px_rgba(59,130,246,0.08)]' 
                  : 'hover:bg-slate-50/80 dark:hover:bg-slate-800/50 border border-transparent'
              ]"
              @click="selectSession(session)"
            >
              <div class="font-medium text-[13px] truncate text-slate-800 dark:text-slate-200 leading-tight">
                {{ session.title }}
              </div>
              <div class="text-[11px] text-slate-400 dark:text-slate-500 mt-1.5 flex items-center gap-1 opacity-80">
                <FmIcon name="i-carbon:time" class="text-[10px]" />
                {{ formatTime(session.updatedAt) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 聊天内容 -->
        <div class="flex-1 flex flex-col bg-white/40 dark:bg-slate-900/40 backdrop-blur-md rounded-2xl border border-white/50 dark:border-slate-700/50 shadow-sm overflow-hidden relative">
          <!-- 消息列表 -->
          <div 
            ref="messagesContainer"
            class="flex-1 overflow-y-auto p-5 space-y-6 scroll-smooth"
          >
            <div v-if="!currentSession" class="flex items-center justify-center h-full text-slate-400 dark:text-slate-500">
              <div class="text-center transition-all w-full px-4 md:px-10">
                <div class="w-16 h-16 mx-auto rounded-[16px] bg-gradient-to-tr from-indigo-100 to-purple-100 dark:from-indigo-900/30 dark:to-purple-900/30 flex items-center justify-center mb-6 shadow-sm border border-white/50 dark:border-slate-700/30">
                  <FmIcon name="i-carbon:chat-bot" class="text-3xl text-indigo-400 dark:text-indigo-300" />
                </div>
                <h3 class="text-lg font-bold text-slate-700 dark:text-slate-200 mb-2">选择您的专属心理陪伴</h3>
                <p class="text-sm opacity-70 mb-6">不同的 AI 人格将带给您不同的治愈体验</p>
                
                <div class="flex flex-wrap gap-4 justify-center mt-2 max-w-4xl mx-auto">
                  <div 
                    v-for="char in characters" 
                    :key="char.id" 
                    @click="selectedCharacterId = char.id"
                    :class="[
                      'relative overflow-hidden border rounded-2xl p-5 w-[160px] cursor-pointer transition-all duration-300 active:scale-95 text-left', 
                      selectedCharacterId === char.id 
                        ? 'border-indigo-400 bg-indigo-50/80 dark:bg-indigo-900/40 shadow-md shadow-indigo-500/10' 
                        : 'border-slate-200 dark:border-slate-700 bg-white/60 dark:bg-slate-800/60 hover:border-indigo-300 dark:hover:border-indigo-700 hover:shadow-sm'
                    ]"
                  >
                    <div v-if="selectedCharacterId === char.id" class="absolute -top-10 -right-10 w-20 h-20 bg-indigo-500/10 rounded-full blur-xl pointer-events-none"></div>
                    <div class="flex flex-col items-center">
                      <div class="relative mb-3">
                        <img v-if="char.avatar" :src="char.avatar" class="w-16 h-16 rounded-full object-cover border-2 border-white dark:border-slate-700 shadow-sm" />
                        <div v-else class="w-16 h-16 rounded-full bg-slate-200 dark:bg-slate-700 border-2 border-white dark:border-slate-600 flex items-center justify-center shadow-sm">
                          <FmIcon name="i-carbon:user" class="text-xl text-slate-400" />
                        </div>
                        <div v-if="selectedCharacterId === char.id" class="absolute bottom-0 right-0 w-5 h-5 bg-green-500 border-2 border-white dark:border-slate-800 rounded-full"></div>
                      </div>
                      <div class="font-bold text-slate-800 dark:text-slate-200 text-[15px] mb-1">{{ char.name }}</div>
                      <div class="text-[11px] text-slate-500 dark:text-slate-400 line-clamp-2 h-[32px] text-center leading-snug">{{ char.greeting }}</div>
                    </div>
                  </div>
                </div>
                
                <div v-if="characters.length === 0" class="text-sm opacity-50 mt-4">暂无可用的 AI 人设</div>
              </div>
            </div>
            
            <div v-else-if="messages.length === 0" class="flex items-center justify-center h-full text-slate-400 dark:text-slate-500">
              <div class="text-center animate-fade-in-up">
                <div class="w-20 h-20 mx-auto rounded-full bg-gradient-to-tr from-blue-100 to-purple-100 dark:from-blue-900/30 dark:to-purple-900/30 flex items-center justify-center mb-4 shadow-inner border border-white/50 dark:border-slate-700/30">
                  <FmIcon name="i-carbon:chat-bot" class="text-3xl text-indigo-400 dark:text-indigo-300" />
                </div>
                <p class="font-medium text-slate-600 dark:text-slate-300 mb-2">开始与AI心理助手对话吧</p>
                <p class="text-xs opacity-70">我会倾听您的心声，提供专业的心理支持</p>
              </div>
            </div>

            <div v-for="message in messages" :key="message.id" class="flex gap-4 animate-fade-in">
              <!-- 用户消息 -->
              <div v-if="message.role === 'user'" class="flex justify-end w-full">
                <div class="max-w-[75%] bg-gradient-to-br from-blue-500 to-indigo-500 font-medium text-white rounded-2xl p-4 shadow-md shadow-blue-500/20 rounded-tr-sm">
                  <p class="whitespace-pre-wrap leading-relaxed text-[14px]">{{ message.content }}</p>
                  <div class="text-[11px] text-blue-100 mt-2 flex justify-end opacity-80 font-normal">
                    {{ formatTime(message.createdAt) }}
                  </div>
                </div>
              </div>
              
              <!-- AI消息 -->
              <div v-else class="flex w-full">
                <div class="flex gap-3 max-w-[85%]">
                  <div class="w-9 h-9 rounded-[10px] bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center flex-shrink-0 shadow-sm shadow-indigo-500/30 border border-white/20 dark:border-slate-700">
                    <FmIcon name="i-carbon:chat-bot" class="text-lg text-white" />
                  </div>
                  <div class="bg-white/90 dark:bg-slate-800/90 backdrop-blur-sm rounded-2xl p-4 shadow-sm border border-slate-100 dark:border-slate-700/50 rounded-tl-[4px]">
                    <p class="whitespace-pre-wrap leading-relaxed text-[14px] text-slate-800 dark:text-slate-200">{{ message.content }}</p>
                    <div class="text-[11px] text-slate-400 dark:text-slate-500 mt-2 flex items-center font-normal">
                      {{ formatTime(message.createdAt) }}
                      <span v-if="message.tokensUsed" class="ml-2 flex items-center">
                        <span class="w-1 h-1 rounded-full bg-slate-300 dark:bg-slate-600 mx-1.5"></span>
                        <FmIcon name="i-carbon:data-1" class="mr-1 text-[10px]" />
                        <span class="opacity-80">{{ message.tokensUsed }} Tokens</span>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 相关资源推荐卡片 -->
            <div v-if="showRecommendations && recommendedResources.length > 0" class="w-full mt-4 flex justify-end animate-fade-in-up">
              <div class="max-w-[85%] bg-white/80 dark:bg-slate-800/80 backdrop-blur-md rounded-2xl p-5 border border-indigo-100/50 dark:border-indigo-900/30 shadow-sm shadow-indigo-500/5 relative overflow-hidden">
                <div class="absolute top-0 right-0 w-32 h-32 bg-indigo-500/5 rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none"></div>
                <div class="flex items-center justify-between mb-4 relative z-10">
                  <h4 class="text-sm font-bold text-indigo-900 dark:text-indigo-200 flex items-center gap-1.5 tracking-tight">
                    <FmIcon name="i-ic:outline-library-books" class="text-indigo-500" />
                    为您推荐的相关资源
                  </h4>
                  <button
                    class="text-[11px] text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 px-2 py-1 rounded-full hover:bg-slate-100/50 dark:hover:bg-slate-700/50 transition-colors"
                    @click="dismissRecommendations"
                  >关闭</button>
                </div>
                <div class="space-y-2.5 relative z-10">
                  <div
                    v-for="item in recommendedResources"
                    :key="item.id"
                    class="group flex items-center gap-3 bg-white/90 dark:bg-slate-900/90 rounded-xl p-3 cursor-pointer hover:shadow-md hover:shadow-indigo-500/10 border border-slate-100 dark:border-slate-700/50 hover:border-indigo-200 dark:hover:border-indigo-800 transition-all duration-300 active:scale-[0.98]"
                    @click="router.push(`/resource/${item.id}`)"
                  >
                    <div :class="`flex-shrink-0 w-10 h-10 rounded-xl bg-gradient-to-br ${resourceTypeColor(item.type)} flex items-center justify-center shadow-sm`">
                      <FmIcon :name="resourceIcon(item.type)" class="text-white text-lg" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="text-[13px] font-semibold text-slate-800 dark:text-slate-200 truncate group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">{{ item.title }}</div>
                      <div class="text-[11px] text-slate-400 mt-1 flex items-center gap-1.5">
                        <span class="px-1.5 py-0.5 rounded-[4px] bg-slate-100 dark:bg-slate-800 font-medium">{{ resourceTypeLabel(item.type) }}</span>
                        <span v-if="item.description" class="truncate opacity-80">{{ item.description }}</span>
                      </div>
                    </div>
                    <div class="w-6 h-6 rounded-full bg-slate-50 dark:bg-slate-800 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity -translate-x-2 group-hover:translate-x-0 group-hover:duration-300">
                      <FmIcon name="i-ic:outline-chevron-right" class="text-indigo-500 text-sm" />
                    </div>
                  </div>
                </div>
                <div class="mt-4 text-center relative z-10">
                  <button
                    class="text-[12px] text-indigo-500 hover:text-indigo-600 dark:text-indigo-400 font-medium tracking-wide flex items-center justify-center mx-auto hover:bg-indigo-50 dark:hover:bg-indigo-900/30 px-3 py-1.5 rounded-full transition-colors"
                    @click="router.push('/resource')"
                  >
                    查看更多资源
                    <FmIcon name="i-carbon:arrow-right" class="ml-1" />
                  </button>
                </div>
              </div>
            </div>

            <!-- AI 发送中提示 -->
            <div v-if="sending" class="flex w-full animate-fade-in">
              <div class="flex gap-3 max-w-[85%] mb-4">
                <div class="w-9 h-9 rounded-[10px] bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center flex-shrink-0 shadow-sm shadow-indigo-500/30 border border-white/20 dark:border-slate-700">
                  <FmIcon name="i-carbon:chat-bot" class="text-lg text-white" />
                </div>
                <div class="bg-white/90 dark:bg-slate-800/90 backdrop-blur-sm rounded-2xl p-4 shadow-sm border border-slate-100 dark:border-slate-700/50 rounded-tl-[4px] min-w-[100px] flex items-center h-11">
                  <div class="flex items-center gap-1.5 h-full">
                    <div class="w-1.5 h-1.5 bg-indigo-400/80 rounded-full animate-[bounce_1.4s_infinite_ease-in-out_both]"></div>
                    <div class="w-1.5 h-1.5 bg-indigo-400/80 rounded-full animate-[bounce_1.4s_infinite_ease-in-out_both]" style="animation-delay: 0.16s"></div>
                    <div class="w-1.5 h-1.5 bg-indigo-400/80 rounded-full animate-[bounce_1.4s_infinite_ease-in-out_both]" style="animation-delay: 0.32s"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="p-4 bg-white/60 dark:bg-slate-900/60 backdrop-blur-md border-t border-slate-100 dark:border-slate-800/50 relative z-10">
            <div class="flex gap-3 items-end max-w-4xl mx-auto w-full">
              <div class="flex-1 bg-white/80 dark:bg-slate-800/80 rounded-2xl border border-slate-200/60 dark:border-slate-700/60 shadow-inner focus-within:ring-2 focus-within:ring-indigo-500/20 focus-within:border-indigo-400/50 transition-all overflow-hidden relative group">
                <textarea
                  v-model="inputMessage"
                  placeholder="说点什么吧..."
                  class="w-full bg-transparent border-0 px-4 py-3.5 text-[14px] text-slate-800 dark:text-slate-200 placeholder-slate-400 focus:outline-none resize-none min-h-[52px] max-h-[150px] scrollbar-thin scrollbar-thumb-slate-200 dark:scrollbar-thumb-slate-600 block"
                  :disabled="sending"
                  @keydown="handleKeydown"
                  rows="1"
                  style="field-sizing: content;"
                ></textarea>
              </div>
              <FmButton 
                class="rounded-xl h-[52px] px-6 shadow-md shadow-indigo-500/20 bg-gradient-to-r from-indigo-500 to-purple-500 hover:from-indigo-600 hover:to-purple-600 text-white font-medium border-0 transition-transform active:scale-95 flex-shrink-0"
                @click="sendMessage" 
                :loading="sending"
                :disabled="!inputMessage.trim()"
              >
                <div class="flex items-center font-bold">
                  {{ sending ? '' : '发送' }}
                  <FmIcon v-if="!sending" name="i-carbon:send-alt" class="ml-1.5 text-lg" />
                </div>
              </FmButton>
            </div>
            <div class="text-[11px] text-slate-400 dark:text-slate-500 mt-2.5 text-center font-medium opacity-70">
              按 <kbd class="px-1.5 py-0.5 bg-slate-100 dark:bg-slate-800 rounded border border-slate-200 dark:border-slate-700 font-sans text-[10px]">Enter</kbd> 发送，<kbd class="px-1.5 py-0.5 bg-slate-100 dark:bg-slate-800 rounded border border-slate-200 dark:border-slate-700 font-sans text-[10px]">Shift + Enter</kbd> 换行
            </div>
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
