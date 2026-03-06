<script setup lang="ts">
import { Icon } from '@iconify/vue'
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

// 弹出层控制
const showSessionList = ref(false)
const showCharacterSelect = ref(false)

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

// 创建新会话并发送第一条消息
async function createNewSession() {
  const firstMsg = inputMessage.value.trim()
  if (!firstMsg) {
    return
  }

  try {
    sending.value = true
    inputMessage.value = ''

    // Step 1: 创建会话（后端同时保存了用户第一条消息）
    const session = await chatApi.createSession({
      title: `新对话`,
      characterId: selectedCharacterId.value,
      firstMessage: firstMsg,
    })

    // Step 2: 加入会话列表并选中
    sessions.value.unshift(session)
    currentSession.value = session

    // Step 3: 乐观渲染用户消息
    const userMessage: ChatMessage = {
      id: Date.now(),
      sessionId: session.id,
      userId: userStore.userInfo?.id || 0,
      role: 'user',
      content: firstMsg,
      createdAt: new Date().toISOString(),
    }
    messages.value = [userMessage]
    nextTick(() => { scrollToBottom() })

    // Step 4: 调用 sendMessage 让 AI 真正回复（后端会读取会话中已有的用户消息）
    const aiRes = await chatApi.sendMessage({
      sessionId: session.id,
      content: firstMsg,
    })

    // Step 5: 将 AI 回复追加到消息列表
    const aiMessage: ChatMessage = {
      id: aiRes.messageId,
      sessionId: session.id,
      userId: 0,
      role: 'assistant',
      content: aiRes.aiResponse,
      tokensUsed: aiRes.tokensUsed,
      createdAt: aiRes.timestamp,
    }
    messages.value.push(aiMessage)
    nextTick(() => { scrollToBottom() })

    toast.success('开始新对话')
  } catch (error: any) {
    console.error('创建会话失败:', error)
    toast.error('创建会话失败', {
      description: error.message || '请稍后重试',
    })
    messages.value = []
    inputMessage.value = firstMsg || ''
  } finally {
    sending.value = false
  }
}

// 发送消息（仅用于已有会话的后续消息）
async function sendMessage() {
  if (!inputMessage.value.trim()) {
    return
  }

  // 没有当前会话时，走新建会话流程（第一条消息由后端处理）
  if (!currentSession.value) {
    await createNewSession()
    return
  }

  const session = currentSession.value

  try {
    sending.value = true
    const messageContent = inputMessage.value.trim()
    
    const userMessage: ChatMessage = {
      id: Date.now(),
      sessionId: session.id,
      userId: userStore.userInfo?.id || 0,
      role: 'user',
      content: messageContent,
      createdAt: new Date().toISOString(),
    }
    messages.value.push(userMessage)
    inputMessage.value = ''
    
    nextTick(() => { scrollToBottom() })
    
    const response = await chatApi.sendMessage({
      sessionId: session.id,
      content: messageContent,
    })
    
    const aiMessage: ChatMessage = {
      id: response.messageId,
      sessionId: session.id,
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
  <FmPageLayout tabbar>
    <template #navbar-start>
      <FmButton 
        variant="text" 
        class="!p-1 text-slate-700 dark:text-slate-200 active:scale-95 transition-transform"
        @click="showSessionList = true"
      >
        <FmIcon name="i-carbon:menu" class="text-2xl" />
      </FmButton>
    </template>
    <template #navbar-end>
      <FmButton 
        variant="secondary" 
        size="sm" 
        class="rounded-full shadow-sm bg-slate-100 dark:bg-slate-800/80 hover:bg-slate-200 dark:hover:bg-slate-700 border-0 transition-all active:scale-95"
        @click="router.push('/chat/history')"
      >
        <FmIcon name="i-carbon:time" class="mr-1" />
        历史
      </FmButton>
    </template>

    <!-- 整体背景改用更柔和的渐变底色，搭配暗模式适配 -->
    <div class="flex flex-1 flex-col h-full bg-slate-50/50 dark:bg-slate-900/50 relative">
      <!-- 增加背景光晕效果 (Orb) -->
      <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none z-0">
        <div class="absolute -top-[10%] -left-[10%] w-[40%] h-[40%] rounded-full bg-blue-400/10 blur-[80px] mix-blend-multiply dark:bg-blue-900/20 dark:mix-blend-screen"></div>
        <div class="absolute top-[20%] -right-[10%] w-[30%] h-[50%] rounded-full bg-purple-400/10 blur-[80px] mix-blend-multiply dark:bg-purple-900/20 dark:mix-blend-screen"></div>
      </div>

      <!-- 聊天区域 -->
      <div class="flex flex-1 overflow-hidden z-10 w-full relative">
        <!-- 会话列表 (左侧抽屉/Popup) -->
        <van-popup 
          v-model:show="showSessionList" 
          position="left" 
          teleport="#app"
          :style="{ width: '80vw', maxWidth: '320px', height: '100%' }"
          class="bg-slate-50 dark:bg-slate-900 flex flex-col"
        >
          <div class="p-4 pt-safe mt-2 border-b border-slate-100 dark:border-slate-800/50 flex justify-between items-center bg-white dark:bg-slate-900">
            <h2 class="text-[16px] font-bold text-slate-800 dark:text-slate-200 flex items-center gap-2">
              <FmIcon name="i-carbon:chat" class="text-indigo-500" />
              历史对话
            </h2>
            <FmButton 
              class="rounded-full shadow-md shadow-indigo-500/20 bg-gradient-to-r from-indigo-500 to-purple-500 text-white !w-8 !h-8 !p-0 flex items-center justify-center border-0 active:scale-95" 
              @click="currentSession = null; messages = []; showSessionList = false; showCharacterSelect = true"
              :loading="sending"
            >
              <FmIcon name="i-carbon:add" class="text-lg" />
            </FmButton>
          </div>
          
          <div class="flex-1 overflow-y-auto p-3 space-y-2.5 scrollbar-hide">
            <div 
              v-for="session in sessions" 
              :key="session.id"
              class="p-4 rounded-2xl cursor-pointer transition-all duration-200"
              :class="[
                currentSession?.id === session.id 
                  ? 'bg-white dark:bg-slate-800 border-2 border-indigo-400 dark:border-indigo-500 shadow-sm' 
                  : 'bg-white/60 dark:bg-slate-800/60 border-2 border-transparent hover:bg-white dark:hover:bg-slate-800 active:scale-[0.98]'
              ]"
              @click="selectSession(session); showSessionList = false"
            >
              <div class="font-bold text-[14px] truncate text-slate-800 dark:text-slate-200 leading-tight">
                {{ session.title }}
              </div>
              <div class="text-[12px] text-slate-400 dark:text-slate-500 mt-2 flex items-center justify-between opacity-80">
                <span class="flex items-center gap-1.5"><FmIcon name="i-carbon:time" class="text-[12px]" /> {{ formatTime(session.updatedAt) }}</span>
                <FmIcon v-if="currentSession?.id === session.id" name="i-carbon:checkmark" class="text-indigo-500" />
              </div>
            </div>
            
            <div v-if="sessions.length === 0" class="flex flex-col items-center justify-center py-10 text-slate-400">
              <FmIcon name="i-carbon:chat-off" class="text-4xl mb-3 opacity-30" />
              <p class="text-[13px]">暂无对话记录</p>
            </div>
          </div>
        </van-popup>

        <!-- 聊天内容 -->
        <div class="flex-1 flex flex-col bg-transparent overflow-hidden relative">
          <!-- 消息列表 -->
          <div 
            ref="messagesContainer"
            class="flex-1 overflow-y-auto p-5 space-y-6 scroll-smooth"
          >
            <div v-if="!currentSession || messages.length === 0" class="flex flex-col items-center justify-center h-full text-slate-400 dark:text-slate-500 pb-20 mt-10">
              <div class="text-center animate-fade-in-up w-full px-6">
                <div class="w-24 h-24 mx-auto rounded-[28px] bg-gradient-to-tr from-indigo-100 to-purple-100 dark:from-indigo-900/30 dark:to-purple-900/30 flex items-center justify-center mb-6 shadow-md border border-white/50 dark:border-slate-700/30 relative">
                  <template v-if="characters.find(c => c.id === selectedCharacterId)?.avatar">
                    <img v-if="!characters.find(c => c.id === selectedCharacterId)?.avatar?.startsWith('i-')" :src="characters.find(c => c.id === selectedCharacterId)?.avatar" class="w-full h-full rounded-[28px] object-cover" />
                    <Icon v-else :icon="characters.find(c => c.id === selectedCharacterId)?.avatar?.replace(/^i-/, '')" class="text-5xl text-inherit" />
                  </template>
                  <FmIcon v-else name="i-carbon:chat-bot" class="text-5xl text-indigo-400 dark:text-indigo-300" />
                </div>
                <h3 class="text-[22px] font-bold text-slate-800 dark:text-slate-100 mb-3 tracking-tight">你好，我是 {{ characters.find(c => c.id === selectedCharacterId)?.name || 'AI' }}</h3>
                <p class="text-[15px] opacity-80 mb-8 max-w-[280px] mx-auto leading-relaxed text-slate-500 dark:text-slate-400">{{ characters.find(c => c.id === selectedCharacterId)?.greeting || '让我们开始一段治愈的对话吧' }}</p>
                
                <FmButton 
                  v-if="!currentSession"
                  class="rounded-full shadow-sm bg-white/80 dark:bg-slate-800/80 text-indigo-500 dark:text-indigo-400 border border-slate-200 dark:border-slate-700 hover:bg-slate-50 px-5 py-2 h-auto active:scale-95 transition-all"
                  @click="showCharacterSelect = true"
                >
                  <FmIcon name="i-carbon:user-multiple" class="mr-1.5" />
                  切换陪伴角色
                </FmButton>
              </div>
            </div>
            
            <!-- Character Selector Bottom Sheet -->
            <van-popup
              v-model:show="showCharacterSelect"
              position="bottom"
              round
              teleport="#app"
              :style="{ height: '65%', maxHeight: '600px' }"
              class="bg-slate-50 dark:bg-slate-900 flex flex-col pt-2 max-w-[600px] mx-auto left-0 right-0"
            >
              <div class="w-10 h-1.5 bg-slate-200 dark:bg-slate-700 rounded-full mx-auto my-2"></div>
              <div class="px-5 py-3 border-b border-slate-100 dark:border-slate-800 mb-2">
                <h3 class="text-lg font-bold text-slate-800 dark:text-slate-100">选择陪伴角色</h3>
                <p class="text-xs text-slate-500 mt-1">不同的角色将带给您不同的治愈体验</p>
              </div>
              <div class="flex-1 overflow-y-auto px-5 pb-8 space-y-3 scrollbar-hide">
                <div 
                  v-for="char in characters" 
                  :key="char.id" 
                  @click="selectedCharacterId = char.id; showCharacterSelect = false"
                  :class="[
                    'flex items-center gap-4 p-4 rounded-2xl border-2 transition-all active:scale-[0.98]',
                    selectedCharacterId === char.id 
                      ? 'border-indigo-500 bg-indigo-50/50 dark:bg-indigo-900/20 shadow-sm' 
                      : 'border-transparent bg-white dark:bg-slate-800 shadow-sm hover:border-indigo-200 hover:shadow-md'
                  ]"
                >
                  <div class="relative flex-shrink-0 flex items-center justify-center">
                    <template v-if="char.avatar">
                      <img v-if="!char.avatar.startsWith('i-')" :src="char.avatar" class="w-14 h-14 rounded-2xl object-cover shadow-sm" />
                      <div v-else class="w-14 h-14 rounded-2xl bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                        <Icon :icon="char.avatar.replace(/^i-/, '')" class="text-3xl text-slate-600 dark:text-slate-300" />
                      </div>
                    </template>
                    <div v-else class="w-14 h-14 rounded-2xl bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                      <FmIcon name="i-carbon:user" class="text-xl text-slate-400" />
                    </div>
                    <div v-if="selectedCharacterId === char.id" class="absolute -top-1.5 -right-1.5 w-5 h-5 bg-indigo-500 rounded-full text-white flex items-center justify-center border-2 border-white dark:border-slate-800 shadow-sm">
                      <FmIcon name="i-carbon:checkmark" class="text-[10px]" />
                    </div>
                  </div>
                  <div class="flex-1 min-w-0">
                    <h4 class="text-[15px] font-bold text-slate-800 dark:text-slate-100 truncate">{{ char.name }}</h4>
                    <p class="text-[12px] text-slate-500 dark:text-slate-400 line-clamp-2 mt-1 leading-snug">{{ char.greeting }}</p>
                  </div>
                </div>
              </div>
            </van-popup>

            <div v-for="message in messages" :key="message.id" class="flex gap-4 animate-fade-in">
              <!-- 用户消息 -->
              <div v-if="message.role === 'user'" class="flex justify-end w-full">
                <div class="max-w-[85%] bg-indigo-500 font-medium text-white rounded-[22px] rounded-tr-[4px] px-4 py-3 shadow-md shadow-indigo-500/10 active:scale-[0.98] transition-all">
                  <p class="whitespace-pre-wrap leading-relaxed text-[15px]">{{ message.content }}</p>
                  <div class="text-[10px] text-indigo-100 mt-1.5 flex justify-end opacity-70 font-normal">
                    {{ formatTime(message.createdAt) }}
                  </div>
                </div>
              </div>
              
              <!-- AI消息 -->
              <div v-else class="flex w-full">
                <div class="flex gap-2.5 max-w-[90%]">
                  <div class="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center flex-shrink-0 shadow-sm border border-slate-200/50 dark:border-slate-700 mt-0.5 overflow-hidden">
                    <template v-if="characters.find(c => c.id === selectedCharacterId)?.avatar">
                      <img v-if="!characters.find(c => c.id === selectedCharacterId)?.avatar?.startsWith('i-')" :src="characters.find(c => c.id === selectedCharacterId)?.avatar" class="w-full h-full object-cover" />
                      <Icon v-else :icon="characters.find(c => c.id === selectedCharacterId)?.avatar?.replace(/^i-/, '')" class="text-[20px] text-slate-600 dark:text-slate-300" />
                    </template>
                    <FmIcon v-else name="i-carbon:chat-bot" class="text-sm text-indigo-500 dark:text-indigo-400" />
                  </div>
                  <div class="bg-white dark:bg-slate-800 backdrop-blur-sm rounded-[22px] rounded-tl-[4px] px-4 py-3 shadow-sm border border-slate-100/80 dark:border-slate-700/50">
                    <p class="whitespace-pre-wrap leading-relaxed text-[15px] text-slate-800 dark:text-slate-200">{{ message.content }}</p>
                    <div class="text-[10px] text-slate-400 dark:text-slate-500 mt-1.5 flex items-center font-normal">
                      {{ formatTime(message.createdAt) }}
                      <span v-if="message.tokensUsed" class="ml-2.5 flex items-center opacity-70">
                        <FmIcon name="i-carbon:data-1" class="mr-0.5 text-[10px]" />
                        <span>{{ message.tokensUsed }}</span>
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
          <div class="px-3 py-2.5 bg-white/90 dark:bg-slate-900/90 backdrop-blur-xl border-t border-slate-100 dark:border-slate-800/80 pb-safe relative z-10 w-full shadow-[0_-4px_20px_rgba(0,0,0,0.02)]">
            <div class="flex gap-2 items-end mx-auto w-full relative">
              <div class="flex-1 bg-slate-100/90 dark:bg-slate-800/90 rounded-[22px] border border-transparent focus-within:border-indigo-300/50 dark:focus-within:border-indigo-700/50 focus-within:bg-white dark:focus-within:bg-slate-800 transition-all overflow-hidden relative flex items-center min-h-[44px] shadow-inner">
                <textarea
                  v-model="inputMessage"
                  placeholder="给 AI 发消息..."
                  class="w-full bg-transparent border-0 px-4 py-2.5 text-[15px] text-slate-800 dark:text-slate-100 placeholder-slate-400 focus:outline-none resize-none max-h-[120px] scrollbar-hide block leading-relaxed"
                  :disabled="sending"
                  @keydown="handleKeydown"
                  rows="1"
                  style="field-sizing: content;"
                ></textarea>
              </div>
              <FmButton 
                class="rounded-full !w-[44px] !h-[44px] !p-0 shadow-sm bg-indigo-500 hover:bg-indigo-600 text-white border-0 transition-transform active:scale-90 flex-shrink-0 flex items-center justify-center disabled:opacity-40 disabled:bg-slate-300 dark:disabled:bg-slate-700"
                @click="sendMessage" 
                :loading="sending"
                :disabled="!inputMessage.trim()"
              >
                <FmIcon v-if="!sending" name="i-carbon:send-filled" class="text-xl" />
              </FmButton>
            </div>
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
