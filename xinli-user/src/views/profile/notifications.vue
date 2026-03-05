<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { notificationApi } from '@/api/modules'
import type { UserNotification } from '@/api/modules/notification'
import { toast } from 'vue-sonner'

definePage({
  meta: {
    title: '通知中心',
    auth: true,
  },
})

const router = useRouter()
const notifications = ref<UserNotification[]>([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)
const size = 15

async function loadData(isRefresh = false) {
  if (isRefresh) {
    page.value = 1
    finished.value = false
  }
  
  try {
    loading.value = true
    const res = await notificationApi.getNotifications(page.value, size)
    const list = res.records || []
    
    if (isRefresh) {
      notifications.value = list
    } else {
      notifications.value.push(...list)
    }
    
    if (list.length < size || notifications.value.length >= res.total) {
      finished.value = true
    } else {
      page.value++
    }
  } catch (error) {
    console.error('获取通知失败:', error)
    if (!isRefresh) finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  loadData(true)
}

const onLoad = () => {
  if (refreshing.value || finished.value) return
  loadData()
}

const handleMarkAsRead = async (item: UserNotification) => {
  if (item.isRead) return
  try {
    await notificationApi.markAsRead(item.id)
    item.isRead = 1
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

const handleMarkAllRead = async () => {
  if (notifications.value.every(n => n.isRead === 1)) return
  try {
    await notificationApi.markAllRead()
    notifications.value.forEach(n => n.isRead = 1)
    toast.success('已全部按读')
  } catch (error) {
    toast.error('操作失败')
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

function goBack() {
  router.back()
}
</script>

<template>
  <FmPageLayout :tabbar="false">
    <template #navbar>
      <FmNavbar title="通知中心" left-arrow @click-left="goBack">
        <template #right>
          <span class="text-sm text-blue-500 active:opacity-60" @click="handleMarkAllRead">全部已读</span>
        </template>
      </FmNavbar>
    </template>

    <div class="min-h-screen bg-gray-50 dark:bg-gray-900 pt-2 pb-6">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
          class="px-4 space-y-3"
        >
          <div
            v-for="item in notifications"
            :key="item.id"
            @click="handleMarkAsRead(item)"
            :class="`bg-white dark:bg-gray-800 rounded-xl p-4 shadow-sm relative overflow-hidden transition-opacity ${item.isRead ? 'opacity-70' : ''}`"
          >
            <!-- 危险预警左侧边条 -->
            <div v-if="item.type === 'CRISIS'" class="absolute left-0 top-0 bottom-0 w-1 bg-red-500"></div>
            
            <div class="flex gap-3 relative">
              <!-- 红点辅助 -->
              <div v-if="!item.isRead" class="absolute -left-1 -top-1 w-2 h-2 rounded-full bg-red-500 z-10"></div>
              
              <!-- 图标 -->
              <div :class="`w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 ${
                item.type === 'CRISIS' ? 'bg-red-100 dark:bg-red-900/30 text-red-500' :
                item.type === 'LIKE' ? 'bg-pink-100 dark:bg-pink-900/30 text-pink-500' :
                'bg-blue-100 dark:bg-blue-900/30 text-blue-500'
              }`">
                <FmIcon :name="
                  item.type === 'CRISIS' ? 'i-ic:outline-warningAmber' :
                  item.type === 'LIKE' ? 'i-ic:outline-favorite' :
                  'i-ic:outline-notifications'
                " class="text-xl" />
              </div>
              
              <!-- 内容 -->
              <div class="flex-1 min-w-0">
                <div class="flex justify-between items-start mb-1">
                  <h3 :class="`font-medium text-sm pr-2 ${item.type === 'CRISIS' ? 'text-red-600 dark:text-red-400' : 'text-gray-900 dark:text-gray-100'}`">
                    {{ item.title }}
                  </h3>
                  <span class="text-xs text-gray-400 flex-shrink-0">{{ formatDate(item.createdAt) }}</span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400 leading-relaxed whitespace-pre-wrap">
                  {{ item.content }}
                </p>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="notifications.length === 0 && !loading && !refreshing" class="text-center py-16 text-gray-400">
            <FmIcon name="i-ic:outline-inbox" class="text-4xl mb-3 opacity-50" />
            <p class="text-sm">暂无任何通知</p>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>
  </FmPageLayout>
</template>
