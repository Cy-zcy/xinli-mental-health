<script setup lang="ts">
import { toast } from 'vue-sonner'

definePage({
  meta: {
    title: '个人中心',
    auth: true,
  },
})

const router = useRouter()
const userStore = useUserStore()

// 用户统计数据
const userStats = ref({
  chatSessions: 0,
  forumPosts: 0,
  meditationMinutes: 0,
  daysActive: 0,
})

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'chat',
    title: '完成了一次AI心理聊天',
    time: '2小时前',
    icon: 'i-ic:outline-chat',
    color: 'text-blue-500',
  },
  {
    id: 2,
    type: 'meditation',
    title: '完成了10分钟冥想练习',
    time: '昨天',
    icon: 'i-ic:outline-self-improvement',
    color: 'text-purple-500',
  },
  {
    id: 3,
    type: 'forum',
    title: '发布了一篇论坛帖子',
    time: '2天前',
    icon: 'i-ic:outline-forum',
    color: 'text-green-500',
  },
])

// 快捷功能
const quickActions = [
  {
    id: 'settings',
    title: '设置',
    subtitle: '个人信息与偏好',
    icon: 'i-ic:outline-settings',
    color: 'from-gray-400 to-gray-600',
    path: '/profile/settings',
  },
  {
    id: 'history',
    title: '聊天记录',
    subtitle: '查看历史对话',
    icon: 'i-ic:outline-history',
    color: 'from-blue-400 to-blue-600',
    path: '/chat/history',
  },
  {
    id: 'posts',
    title: '我的帖子',
    subtitle: '管理发布内容',
    icon: 'i-ic:outline-article',
    color: 'from-green-400 to-green-600',
    path: '/profile/posts',
  },
  {
    id: 'favorites',
    title: '收藏夹',
    subtitle: '保存的内容',
    icon: 'i-ic:outline-bookmark',
    color: 'from-yellow-400 to-yellow-600',
    path: '/profile/favorites',
  },
]

// 成就徽章
const achievements = [
  {
    id: 'first-chat',
    title: '初次对话',
    description: '完成第一次AI聊天',
    icon: 'i-ic:outline-chat',
    earned: true,
    color: 'text-blue-500',
  },
  {
    id: 'meditation-master',
    title: '冥想达人',
    description: '累计冥想100分钟',
    icon: 'i-ic:outline-self-improvement',
    earned: false,
    color: 'text-purple-500',
  },
  {
    id: 'forum-contributor',
    title: '论坛贡献者',
    description: '发布10篇帖子',
    icon: 'i-ic:outline-forum',
    earned: false,
    color: 'text-green-500',
  },
  {
    id: 'daily-user',
    title: '每日用户',
    description: '连续使用7天',
    icon: 'i-ic:outline-calendar',
    earned: true,
    color: 'text-orange-500',
  },
]

function navigateTo(path: string) {
  router.push(path)
}

function logout() {
  userStore.logout()
  toast.success('已退出登录')
  router.push('/login')
}

// 获取用户统计数据
async function fetchUserStats() {
  try {
    // 这里应该调用API获取真实数据
    // const stats = await userApi.getStats()
    // userStats.value = stats
    
    // 模拟数据
    userStats.value = {
      chatSessions: 15,
      forumPosts: 3,
      meditationMinutes: 45,
      daysActive: 7,
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

onMounted(() => {
  fetchUserStats()
})
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
              <div class="text-2xl font-bold mb-1">{{ userStats.meditationMinutes }}</div>
              <div class="text-xs text-white/80">冥想分钟</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold mb-1">{{ userStats.daysActive }}</div>
              <div class="text-xs text-white/80">活跃天数</div>
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
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-4 flex items-center gap-2">
            <FmIcon name="i-ic:outline-emoji-events" class="text-yellow-500" />
            成就徽章
          </h3>
          <div class="grid grid-cols-2 gap-4">
            <div
              v-for="achievement in achievements"
              :key="achievement.id"
              :class="`bg-gray-50 dark:bg-gray-700 rounded-2xl p-4 transition-all duration-200 ${
                achievement.earned ? 'ring-2 ring-yellow-200 dark:ring-yellow-800' : 'opacity-60'
              }`"
            >
              <div class="flex items-center gap-3">
                <div :class="`w-12 h-12 rounded-2xl ${achievement.earned ? 'bg-gradient-to-r from-yellow-400 to-orange-500 shadow-lg' : 'bg-gray-200 dark:bg-gray-600'} flex items-center justify-center`">
                  <FmIcon :name="achievement.icon" :class="achievement.earned ? 'text-white text-lg' : 'text-gray-400'" />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1">
                    <h4 class="font-semibold text-gray-800 dark:text-white text-sm">{{ achievement.title }}</h4>
                    <FmIcon
                      v-if="achievement.earned"
                      name="i-ic:outline-check-circle"
                      class="text-green-500 text-sm"
                    />
                  </div>
                  <p class="text-xs text-gray-500 dark:text-gray-400">{{ achievement.description }}</p>
                </div>
              </div>
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
