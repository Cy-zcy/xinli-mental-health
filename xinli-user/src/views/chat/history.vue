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
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

function goHome() {
  router.push('/')
}

// 页面加载时获取会话列表
onMounted(() => {
  loadSessions()
})
</script>

<template>
  <FmPageLayout title="聊天历史" :navbar="{ back: true }" @back="goBack">
    <template #navbar-right>
      <FmIcon name="i-carbon:home" class="text-xl text-gray-600 dark:text-gray-300 active:opacity-60 mr-2" @click="goHome" />
    </template>
    <!-- 整体背景改用更柔和的渐变底色 -->
    <div class="flex flex-1 flex-col h-full bg-slate-50/50 dark:bg-slate-900/50 relative">
      <!-- 增加背景光晕效果 (Orb) -->
      <div class="absolute top-0 right-0 w-full h-full overflow-hidden pointer-events-none z-0">
        <div class="absolute -top-[10%] -right-[10%] w-[50%] h-[50%] rounded-full bg-indigo-400/10 blur-[80px] mix-blend-multiply dark:bg-indigo-900/20 dark:mix-blend-screen"></div>
      </div>

      <!-- 统计信息 -->
      <div class="px-5 py-6 bg-white/60 dark:bg-slate-900/60 backdrop-blur-md border-b border-slate-200/50 dark:border-slate-800/50 relative z-10 shadow-[0_4px_20px_-10px_rgba(0,0,0,0.05)]">
        <div class="text-center flex flex-col items-center justify-center space-y-1">
          <div class="text-4xl font-black bg-gradient-to-br from-indigo-500 to-purple-500 bg-clip-text text-transparent">{{ total }}</div>
          <div class="text-[13px] font-medium text-slate-500 dark:text-slate-400 tracking-wide uppercase">总对话次数</div>
        </div>
      </div>

      <!-- 会话列表 -->
      <div class="flex-1 overflow-y-auto relative z-10">
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

        <div v-else class="space-y-4 p-5">
          <div 
            v-for="(session, index) in sessions" 
            :key="session.id"
            class="group bg-white/80 dark:bg-slate-800/80 backdrop-blur-sm rounded-2xl p-5 border border-slate-200/60 dark:border-slate-700/60 hover:border-indigo-300/50 dark:hover:border-indigo-700/50 shadow-sm shadow-slate-200/20 dark:shadow-none hover:shadow-md hover:shadow-indigo-500/10 transition-all duration-300 relative overflow-hidden"
          >
            <!-- 装饰侧边条 -->
            <div class="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-indigo-400 to-purple-500 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            
            <div class="flex items-start justify-between gap-4">
              <div class="flex-1 min-w-0">
                <h3 class="font-bold text-slate-800 dark:text-slate-200 text-base truncate group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                  {{ session.title }}
                </h3>
                <div class="flex items-center gap-4 mt-3 text-[12px] font-medium text-slate-400 dark:text-slate-500">
                  <span class="flex items-center gap-1 bg-slate-100 dark:bg-slate-700/50 px-2 py-0.5 rounded-md">
                    <FmIcon name="i-carbon:time" class="text-[10px]" />
                    {{ formatRelativeTime(session.updatedAt) }}
                  </span>
                  <span class="opacity-70">{{ formatTime(session.createdAt) }}</span>
                </div>
              </div>
              
              <div class="flex items-center gap-2">
                <FmButton 
                  variant="secondary" 
                  size="sm"
                  class="rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-600 dark:bg-indigo-900/30 dark:hover:bg-indigo-900/50 dark:text-indigo-300 border-0 shadow-sm transition-all"
                  @click="viewSession(session)"
                >
                  查看
                </FmButton>
                <FmButton 
                  size="sm"
                  class="rounded-xl bg-gradient-to-r from-indigo-500 to-purple-500 hover:from-indigo-600 hover:to-purple-600 border-0 shadow-sm shadow-indigo-500/20 transition-all"
                  @click="continueChat(session)"
                >
                  继续
                </FmButton>
                <FmButton 
                  variant="ghost" 
                  size="sm"
                  class="rounded-xl text-slate-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-900/20 ml-1 transition-colors px-2"
                  @click="deleteSession(session, index)"
                >
                  <FmIcon name="i-carbon:trash-can" class="text-lg" />
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
