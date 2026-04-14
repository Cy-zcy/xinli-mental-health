<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { notificationApi } from '@/api/modules'
import type { UserNotification, NotificationType, UnreadStats } from '@/api/modules/notification'
import { toast } from 'vue-sonner'

definePage({
  meta: {
    title: '通知中心',
    auth: true,
  },
})

const router = useRouter()

// ===== 状态 =====
const notifications = ref<UserNotification[]>([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const page = ref(1)
const size = 15

// 筛选相关
const activeType = ref<NotificationType>('ALL')
const unreadStats = ref<UnreadStats>({ SYSTEM: 0, CRISIS: 0, LIKE: 0 })

// 删除确认弹窗
const showDeleteConfirm = ref(false)
const deleteTarget = ref<UserNotification | null>(null)

// ===== 分类配置 =====
const typeConfig = {
  ALL: { label: '全部', icon: 'i-ic:outline-inbox', color: 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300' },
  SYSTEM: { label: '系统', icon: 'i-ic:outline-notifications', color: 'bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400' },
  CRISIS: { label: '关怀', icon: 'i-ic:outline-favorite', color: 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400' },
  LIKE: { label: '互动', icon: 'i-ic:outline-thumb-up', color: 'bg-pink-100 dark:bg-pink-900/30 text-pink-600 dark:text-pink-400' },
}

const totalUnread = computed(() => {
  if (activeType.value === 'ALL') {
    return unreadStats.value.SYSTEM + unreadStats.value.CRISIS + unreadStats.value.LIKE
  }
  return unreadStats.value[activeType.value as keyof UnreadStats] || 0
})

// ===== 方法 =====
async function loadUnreadStats() {
  try {
    const res = await notificationApi.getUnreadStats()
    unreadStats.value = res
  } catch (error) {
    console.error('获取未读统计失败:', error)
  }
}

async function loadData(isRefresh = false) {
  if (isRefresh) {
    page.value = 1
    finished.value = false
  }

  try {
    loading.value = true
    const res = await notificationApi.getNotifications(page.value, size, activeType.value)
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
  loadUnreadStats()
  loadData(true)
}

const onLoad = () => {
  if (refreshing.value || finished.value) return
  loadData()
}

// 切换分类
const handleTypeChange = (type: NotificationType) => {
  if (activeType.value === type) return
  activeType.value = type
  notifications.value = []
  page.value = 1
  finished.value = false
  loadData(true)
}

// 标记已读
const handleMarkAsRead = async (item: UserNotification) => {
  if (item.isRead) return
  try {
    await notificationApi.markAsRead(item.id)
    item.isRead = 1
    // 更新未读统计
    if (unreadStats.value[item.type as keyof UnreadStats] > 0) {
      unreadStats.value[item.type as keyof UnreadStats]--
    }
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

// 全部已读
const handleMarkAllRead = async () => {
  if (totalUnread.value === 0) {
    toast.info('没有未读通知')
    return
  }
  try {
    await notificationApi.markAllRead()
    notifications.value.forEach(n => n.isRead = 1)
    unreadStats.value = { SYSTEM: 0, CRISIS: 0, LIKE: 0 }
    toast.success('已全部标记为已读')
  } catch (error) {
    toast.error('操作失败')
  }
}

// 显示删除确认
const confirmDelete = (item: UserNotification) => {
  deleteTarget.value = item
  showDeleteConfirm.value = true
}

// 执行删除
const handleDelete = async () => {
  if (!deleteTarget.value) return
  try {
    await notificationApi.deleteNotification(deleteTarget.value.id)
    notifications.value = notifications.value.filter(n => n.id !== deleteTarget.value!.id)
    // 如果是未读消息，更新统计
    if (deleteTarget.value.isRead === 0) {
      const type = deleteTarget.value.type as keyof UnreadStats
      if (unreadStats.value[type] > 0) {
        unreadStats.value[type]--
      }
    }
    toast.success('已删除')
  } catch (error) {
    toast.error('删除失败')
  } finally {
    showDeleteConfirm.value = false
    deleteTarget.value = null
  }
}

// 清空所有
const handleClearAll = async () => {
  if (notifications.value.length === 0) {
    toast.info('没有通知可清空')
    return
  }
  try {
    const res = await notificationApi.deleteAllNotifications()
    notifications.value = []
    unreadStats.value = { SYSTEM: 0, CRISIS: 0, LIKE: 0 }
    finished.value = true
    toast.success(`已清空 ${res.deletedCount} 条通知`)
  } catch (error) {
    toast.error('清空失败')
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取通知类型配置
const getTypeStyle = (type: string) => {
  return typeConfig[type as keyof typeof typeConfig] || typeConfig.SYSTEM
}

// 返回上一页或首页
function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

// 回到首页
function goHome() {
  router.push('/')
}

// 初始化
onMounted(() => {
  loadUnreadStats()
  loadData(true)
})
</script>

<template>
  <FmPageLayout :navbar="false" :tabbar="false">
    <div class="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col">
      <!-- 顶部导航栏 -->
      <div class="bg-white dark:bg-gray-800 px-4 py-3 flex items-center gap-3 border-b border-gray-100 dark:border-gray-700">
        <FmButton variant="ghost" size="icon" @click="goBack">
          <FmIcon name="i-carbon:arrow-left" class="text-5" />
        </FmButton>
        <h2 class="font-bold text-gray-900 dark:text-white text-base flex-1">
          通知中心
        </h2>
        <FmIcon name="i-carbon:home" class="text-xl text-gray-600 dark:text-gray-300 active:opacity-60" @click="goHome" />
        <span class="text-sm text-blue-500 active:opacity-60" @click="handleMarkAllRead">全部已读</span>
        <span class="text-sm text-red-500 active:opacity-60" @click="handleClearAll">清空</span>
      </div>

      <!-- 分类筛选 Tab -->
      <div class="sticky top-0 z-10 bg-white dark:bg-gray-800 border-b border-gray-100 dark:border-gray-700 px-4 py-3">
        <div class="flex gap-2 overflow-x-auto no-scrollbar">
          <button
            v-for="(config, key) in typeConfig"
            :key="key"
            :class="[
              'flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-all whitespace-nowrap',
              activeType === key
                ? 'bg-blue-500 text-white shadow-sm'
                : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 active:scale-95'
            ]"
            @click="handleTypeChange(key as NotificationType)"
          >
            <FmIcon :name="config.icon" class="text-base" />
            <span>{{ config.label }}</span>
            <!-- 未读徽章 -->
            <span
              v-if="key === 'ALL' ? (unreadStats.SYSTEM + unreadStats.CRISIS + unreadStats.LIKE) > 0 : unreadStats[key as keyof UnreadStats] > 0"
              class="ml-0.5 px-1.5 py-0.5 text-xs bg-red-500 text-white rounded-full"
            >
              {{ key === 'ALL' ? (unreadStats.SYSTEM + unreadStats.CRISIS + unreadStats.LIKE) : unreadStats[key as keyof UnreadStats] }}
            </span>
          </button>
        </div>
      </div>

      <!-- 通知列表 -->
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="px-4 pt-3 pb-6">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
          class="space-y-3"
        >
          <div
            v-for="item in notifications"
            :key="item.id"
            :class="[
              'group bg-white dark:bg-gray-800 rounded-2xl shadow-sm relative overflow-hidden transition-all duration-200 hover:shadow-md',
              item.isRead ? 'opacity-70' : ''
            ]"
          >
            <!-- 类型指示条 -->
            <div :class="[
              'absolute left-0 top-0 bottom-0 w-1',
              item.type === 'CRISIS' ? 'bg-red-500' :
              item.type === 'LIKE' ? 'bg-pink-500' : 'bg-blue-500'
            ]" />

            <div class="flex gap-3 p-4 pl-5">
              <!-- 未读红点 -->
              <div v-if="!item.isRead" class="absolute left-3 top-4 w-2 h-2 rounded-full bg-red-500 z-10 animate-pulse" />

              <!-- 图标 -->
              <div :class="['w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0', getTypeStyle(item.type).color]">
                <FmIcon :name="getTypeStyle(item.type).icon" class="text-xl" />
              </div>

              <!-- 内容 -->
              <div class="flex-1 min-w-0" @click="handleMarkAsRead(item)">
                <div class="flex justify-between items-start mb-1.5">
                  <h3 :class="[
                    'font-semibold text-sm pr-2 leading-tight',
                    item.type === 'CRISIS' ? 'text-red-600 dark:text-red-400' : 'text-gray-900 dark:text-gray-100'
                  ]">
                    {{ item.title }}
                  </h3>
                  <span class="text-xs text-gray-400 flex-shrink-0">{{ formatDate(item.createdAt) }}</span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400 leading-relaxed line-clamp-3">
                  {{ item.content }}
                </p>
              </div>

              <!-- 删除按钮 -->
              <button
                class="opacity-0 group-hover:opacity-100 transition-opacity p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 flex-shrink-0"
                @click.stop="confirmDelete(item)"
              >
                <FmIcon name="i-ic:outline-delete" class="text-gray-400 hover:text-red-500 text-lg" />
              </button>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="notifications.length === 0 && !loading && !refreshing" class="text-center py-20">
            <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
              <FmIcon name="i-ic:outline-notifications-none" class="text-4xl text-gray-300 dark:text-gray-600" />
            </div>
            <p class="text-gray-400 dark:text-gray-500 text-sm mb-1">暂无通知</p>
            <p class="text-gray-300 dark:text-gray-600 text-xs">新的消息会在这里显示</p>
          </div>
        </van-list>
      </van-pull-refresh>

      <!-- 删除确认弹窗 -->
      <van-dialog
        v-model:show="showDeleteConfirm"
        title="确认删除"
        message="确定要删除这条通知吗？"
        show-cancel-button
        confirm-button-text="删除"
        confirm-button-color="#ee0a24"
        @confirm="handleDelete"
      />
    </div>
  </FmPageLayout>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>