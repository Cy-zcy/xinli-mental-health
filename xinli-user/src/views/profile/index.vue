<script setup lang="ts">
import { toast } from 'vue-sonner'
import { getUserStats, getAchievements, getToolHistory } from '@/api/modules/tools'
import type { UserStats, Achievement } from '@/api/modules/tools'

definePage({
  meta: {
    title: '个人中心',
    auth: true,
  },
})

const router = useRouter()
const userStore = useUserStore()

// ===== 状态 =====
const userStats = ref<UserStats>({ chatSessions: 0, forumPosts: 0, totalLikes: 0, toolMinutes: 0 })
const achievements = ref<Achievement[]>([])
const achievementLoading = ref(false)
const activeTab = ref<'all' | 'earned' | 'progress'>('all')

const recentActivities = ref<{id: number, icon: string, color: string, title: string, time: string}[]>([])

// ===== 计算属性 =====
const earnedCount = computed(() => achievements.value.filter(a => a.earned).length)

const filteredAchievements = computed(() => {
  switch (activeTab.value) {
    case 'earned': return achievements.value.filter(a => a.earned)
    case 'progress': return achievements.value.filter(a => !a.earned)
    default: return achievements.value
  }
})

// ===== 快捷功能 =====
const quickActions = [
  { id: 'settings', title: '设置', subtitle: '个人信息与偏好', icon: 'i-ic:outline-settings', color: 'from-gray-400 to-gray-600', path: '/profile/settings' },
  { id: 'history', title: '聊天记录', subtitle: '查看历史对话', icon: 'i-ic:outline-history', color: 'from-blue-400 to-blue-600', path: '/chat/history' },
  { id: 'posts', title: '我的帖子', subtitle: '管理发布内容', icon: 'i-ic:outline-article', color: 'from-green-400 to-green-600', path: '/profile/posts' },
  { id: 'favorites', title: '工具记录', subtitle: '练习历史', icon: 'i-ic:outline-spa', color: 'from-purple-400 to-purple-600', path: '/tools' },
]

// ===== 方法 =====
function navigateTo(path: string) {
  router.push(path)
}

function logout() {
  userStore.logout()
  toast.success('已退出登录')
  router.push('/login')
}

async function loadData() {
  achievementLoading.value = true
  try {
    const [stats, badges, history] = await Promise.all([
      getUserStats(),
      getAchievements(),
      getToolHistory()
    ])
    userStats.value = stats
    achievements.value = badges
    
    // 映射工具记录为最近活动
    recentActivities.value = history.map(record => {
      const isBreathing = record.toolType === 'breathing'
      return {
        id: record.id,
        icon: isBreathing ? 'i-ic:outline-air' : 'i-ic:outline-self-improvement',
        color: isBreathing ? 'text-blue-500' : 'text-purple-500',
        title: `完成${record.pattern ? ' ' + record.pattern : (isBreathing ? '呼吸练习' : '冥想')} ${Math.round(record.durationSeconds / 60)}分钟`,
        time: new Date(record.createdAt).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
      }
    })
  }
  catch (error) {
    console.error('获取数据失败:', error)
  }
  finally {
    achievementLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <FmPageLayout :navbar="false" tabbar>
    <div class="bg-gradient-to-br from-blue-50 to-purple-50 dark:from-gray-900 dark:to-gray-800">
      <!-- 用户信息卡片 -->
      <div class="relative bg-gradient-to-r from-blue-500 to-purple-600">
        <div class="px-6 pt-6 pb-4 text-white">
          <div class="flex items-center gap-4 mb-3">
            <!-- 头像 -->
            <div class="relative">
              <FmAvatar
                :src="userStore.avatar"
                :name="userStore.nickname || '用户'"
                size="base"
                class="!w-20 !h-20 ring-4 ring-white/20"
              />
              <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-500 rounded-full border-2 border-white flex items-center justify-center">
                <FmIcon name="i-ic:outline-check" class="text-white text-xs" />
              </div>
            </div>

            <!-- 用户信息 -->
            <div class="flex-1">
              <h2 class="text-2xl font-bold mb-1">
                {{ userStore.nickname || '用户' }}
              </h2>
              <p class="text-white/80 text-sm mb-2">
                {{ userStore.phone || '未设置手机号' }}
              </p>
              <div class="flex items-center gap-2">
                <span class="px-3 py-1 bg-white/20 backdrop-blur-sm text-white text-xs rounded-full">
                  活跃用户
                </span>
              </div>
            </div>

            <!-- 设置按钮 -->
            <FmButton variant="ghost" size="sm" class="text-white hover:bg-white/10" @click="navigateTo('/profile/settings')">
              <FmIcon name="i-ic:outline-settings" class="text-xl" />
            </FmButton>
          </div>

          <!-- 用户统计 -->
          <div class="grid grid-cols-4 gap-4 mt-3">
            <div class="text-center">
              <div class="text-2xl font-bold mb-1">{{ userStats.chatSessions }}</div>
              <div class="text-xs text-white/80">聊天次数</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold mb-1">{{ userStats.forumPosts }}</div>
              <div class="text-xs text-white/80">发帖数</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold mb-1">{{ userStats.toolMinutes }}</div>
              <div class="text-xs text-white/80">练习分钟</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold mb-1">{{ earnedCount }}</div>
              <div class="text-xs text-white/80">已获成就</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷功能 -->
      <div class="p-4 -mt-8">
        <div class="bg-white dark:bg-gray-800 rounded-t-3xl shadow-lg p-6">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
            <FmIcon name="i-ic:outline-dashboard" class="text-blue-500" />
            快捷功能
          </h3>
          <div class="grid grid-cols-2 gap-4">
            <div
              v-for="action in quickActions"
              :key="action.id"
              class="bg-gray-50 dark:bg-gray-700 rounded-2xl p-4 cursor-pointer transition-all duration-200 hover:shadow-md hover:scale-105 active:scale-95"
              @click="navigateTo(action.path)"
            >
              <div class="flex items-center gap-3">
                <div :class="`w-12 h-12 rounded-2xl bg-gradient-to-r ${action.color} flex items-center justify-center shadow-lg`">
                  <FmIcon :name="action.icon" class="text-white text-lg" />
                </div>
                <div class="flex-1 min-w-0">
                  <h4 class="font-semibold text-gray-800 dark:text-white text-sm mb-1">{{ action.title }}</h4>
                  <p class="text-xs text-gray-500 dark:text-gray-400 truncate">{{ action.subtitle }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 成就徽章 -->
      <div class="px-4 pb-4">
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6">
          <!-- 标题行 -->
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white flex items-center gap-2">
              <FmIcon name="i-ic:outline-emoji-events" class="text-yellow-500" />
              成就徽章
            </h3>
            <span class="text-sm font-medium text-yellow-600 dark:text-yellow-400 bg-yellow-50 dark:bg-yellow-900/20 px-3 py-1 rounded-full">
              {{ achievementLoading ? '加载中...' : `${earnedCount} / ${achievements.length}` }}
            </span>
          </div>

          <!-- 分类 Tab -->
          <div class="flex gap-2 mb-4">
            <button
              v-for="tab in [{ key: 'all', label: '全部' }, { key: 'earned', label: '已获得' }, { key: 'progress', label: '进行中' }]"
              :key="tab.key"
              :class="`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                activeTab === tab.key
                  ? 'bg-yellow-500 text-white shadow-sm'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
              }`"
              @click="activeTab = tab.key as any"
            >
              {{ tab.label }}
            </button>
          </div>

          <!-- 成就列表 -->
          <div v-if="achievementLoading" class="flex justify-center py-8">
            <FmIcon name="i-ic:outline-sync" class="text-gray-400 text-2xl animate-spin" />
          </div>
          <div v-else class="space-y-3">
            <div
              v-for="achievement in filteredAchievements"
              :key="achievement.id"
              :class="`
                rounded-2xl p-4 border transition-all duration-300
                ${ achievement.earned
                  ? 'bg-gradient-to-r from-yellow-50 to-amber-50 dark:from-yellow-900/10 dark:to-amber-900/10 border-yellow-200 dark:border-yellow-800 shadow-sm'
                  : 'bg-gray-50 dark:bg-gray-700/50 border-gray-100 dark:border-gray-700'
                }
              `"
            >
              <div class="flex items-center gap-3">
                <!-- 图标 -->
                <div :class="`
                  w-12 h-12 rounded-2xl flex items-center justify-center flex-shrink-0
                  ${ achievement.earned
                    ? 'bg-gradient-to-br from-yellow-400 to-orange-500 shadow-lg shadow-yellow-200 dark:shadow-yellow-900/30'
                    : 'bg-gray-200 dark:bg-gray-600'
                  }
                `">
                  <FmIcon
                    :name="achievement.icon"
                    :class="achievement.earned ? 'text-white text-lg' : 'text-gray-400 text-lg'"
                  />
                </div>

                <!-- 文字与进度 -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-0.5">
                    <h4 class="font-semibold text-gray-800 dark:text-white text-sm leading-tight">
                      {{ achievement.title }}
                    </h4>
                    <FmIcon
                      v-if="achievement.earned"
                      name="i-ic:outline-verified"
                      class="text-yellow-500 text-base flex-shrink-0"
                    />
                  </div>
                  <p class="text-xs text-gray-500 dark:text-gray-400 mb-2">
                    {{ achievement.description }}
                  </p>

                  <!-- 进度条 -->
                  <div class="flex items-center gap-2">
                    <div class="flex-1 h-1.5 bg-gray-200 dark:bg-gray-600 rounded-full overflow-hidden">
                      <div
                        :class="`h-full rounded-full transition-all duration-700 ${
                          achievement.earned
                            ? 'bg-gradient-to-r from-yellow-400 to-orange-400'
                            : 'bg-gradient-to-r from-blue-400 to-purple-400'
                        }`"
                        :style="{ width: `${achievement.progress}%` }"
                      />
                    </div>
                    <span :class="`text-xs font-medium flex-shrink-0 ${
                      achievement.earned ? 'text-yellow-600 dark:text-yellow-400' : 'text-gray-400'
                    }`">
                      {{ achievement.earned ? '✓' : `${achievement.current}/${achievement.target}` }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredAchievements.length === 0" class="text-center py-8 text-gray-400">
              <FmIcon name="i-ic:outline-inbox" class="text-3xl mb-2" />
              <p class="text-sm">暂无数据</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="px-4 pb-4">
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
            <FmIcon name="i-ic:outline-history" class="text-green-500" />
            最近活动
          </h3>
          <div class="space-y-3">
            <div
              v-for="activity in recentActivities"
              :key="activity.id"
              class="bg-gray-50 dark:bg-gray-700 rounded-xl p-4 transition-all duration-200 hover:shadow-md"
            >
              <div class="flex items-center gap-4">
                <div class="w-10 h-10 rounded-full bg-white dark:bg-gray-600 flex items-center justify-center shadow-sm">
                  <FmIcon :name="activity.icon" :class="`text-lg ${activity.color}`" />
                </div>
                <div class="flex-1">
                  <p class="text-sm font-medium text-gray-800 dark:text-white mb-1">{{ activity.title }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ activity.time }}</p>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="recentActivities.length === 0 && !achievementLoading" class="text-center py-6 text-gray-400">
              <FmIcon name="i-ic:outline-inbox" class="text-3xl mb-2 opacity-50" />
              <p class="text-sm">暂无练习记录，去试试深呼吸放松吧</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 退出登录 -->
      <div class="p-4 pb-20">
        <FmButton variant="outline" class="w-full h-12 text-red-600 border-red-200 hover:bg-red-50 dark:text-red-400 dark:border-red-800 dark:hover:bg-red-900/20" @click="logout">
          <FmIcon name="i-ic:outline-logout" class="mr-2" />
          退出登录
        </FmButton>
      </div>
    </div>
  </FmPageLayout>
</template>
