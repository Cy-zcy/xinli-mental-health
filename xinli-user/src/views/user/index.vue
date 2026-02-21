<script setup lang="ts">
import { toast } from 'vue-sonner'

definePage({
  meta: {
    title: '我的',
    auth: true,
  },
})

const router = useRouter()
const userStore = useUserStore()

const avatarError = ref(false)
const loading = ref(false)

// 监听头像变化
watch(() => userStore.avatar, () => {
  if (avatarError.value) {
    avatarError.value = false
  }
})

// 获取用户信息
onMounted(async () => {
  if (userStore.isLogin && !userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})

// 导航到设置页面
function goToSettings() {
  router.push('/profile/settings')
}

// 导航到聊天历史
function goToChatHistory() {
  router.push('/chat/history')
}

// 导航到我的帖子
function goToMyPosts() {
  router.push('/forum/my-posts')
}

// 退出登录
async function handleLogout() {
  loading.value = true
  try {
    await userStore.logout()
    toast.success('已退出登录')
  } catch (error) {
    console.error('退出登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <FmPageLayout :navbar="false" tabbar>
    <div class="flex flex-1 flex-col gap-6 p-4">
      <!-- 用户信息卡片 -->
      <div class="flex flex-1 flex-col gap-4">
        <div class="flex items-center justify-end gap-4">
          <FmIcon name="i-carbon:settings" class="text-6 cursor-pointer" @click="goToSettings" />
        </div>

        <!-- 用户头像和基本信息 -->
        <div class="flex items-center gap-4 p-4 rounded-lg bg-gradient-to-r from-blue-50 to-purple-50 dark:from-blue-900/20 dark:to-purple-900/20">
          <div class="relative">
            <img
              v-if="userStore.avatar && !avatarError"
              :src="userStore.avatar"
              :onerror="() => (avatarError = true)"
              class="h-16 w-16 rounded-full object-cover border-2 border-white shadow-lg"
            >
            <div v-else class="h-16 w-16 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center">
              <FmIcon name="i-carbon:user-avatar-filled-alt" class="text-8 text-white" />
            </div>
          </div>
          <div class="flex-1">
            <div class="text-lg font-bold text-gray-800 dark:text-gray-200">
              {{ userStore.nickname || '心理健康用户' }}
            </div>
            <div class="text-sm text-gray-600 dark:text-gray-400 mt-1">
              {{ userStore.phone || '未设置手机号' }}
            </div>
            <div class="text-xs text-gray-500 dark:text-gray-500 mt-1">
              愿你内心平静，生活美好 🌸
            </div>
          </div>
        </div>
        <!-- 数据统计 -->
        <div class="grid grid-cols-3 gap-4 mt-4">
          <div class="flex flex-col items-center p-4 rounded-lg bg-card">
            <div class="text-2xl font-bold text-blue-500">0</div>
            <div class="text-xs text-gray-500 mt-1">AI对话次数</div>
          </div>
          <div class="flex flex-col items-center p-4 rounded-lg bg-card">
            <div class="text-2xl font-bold text-green-500">0</div>
            <div class="text-xs text-gray-500 mt-1">发布帖子</div>
          </div>
          <div class="flex flex-col items-center p-4 rounded-lg bg-card">
            <div class="text-2xl font-bold text-purple-500">0</div>
            <div class="text-xs text-gray-500 mt-1">获得点赞</div>
          </div>
        </div>

        <!-- 功能菜单 -->
        <div class="flex flex-col rounded-lg bg-card mt-4">
          <div class="flex items-center gap-3 p-4 border-b border-b-([var(--g-bg)] solid) cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800" @click="goToChatHistory">
            <div class="flex-center inline-flex rounded-full bg-blue-100 dark:bg-blue-900/30 p-2">
              <FmIcon name="i-carbon:chat" class="text-4 text-blue-600 dark:text-blue-400" />
            </div>
            <div class="flex-1">
              <div class="font-medium">聊天记录</div>
              <div class="text-xs text-gray-500">查看AI对话历史</div>
            </div>
            <FmIcon name="i-carbon:chevron-right" class="text-gray-400" />
          </div>

          <div class="flex items-center gap-3 p-4 border-b border-b-([var(--g-bg)] solid) cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800" @click="goToMyPosts">
            <div class="flex-center inline-flex rounded-full bg-green-100 dark:bg-green-900/30 p-2">
              <FmIcon name="i-carbon:document" class="text-4 text-green-600 dark:text-green-400" />
            </div>
            <div class="flex-1">
              <div class="font-medium">我的帖子</div>
              <div class="text-xs text-gray-500">管理发布的内容</div>
            </div>
            <FmIcon name="i-carbon:chevron-right" class="text-gray-400" />
          </div>

          <div class="flex items-center gap-3 p-4 border-b border-b-([var(--g-bg)] solid) cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800" @click="goToSettings">
            <div class="flex-center inline-flex rounded-full bg-purple-100 dark:bg-purple-900/30 p-2">
              <FmIcon name="i-carbon:settings" class="text-4 text-purple-600 dark:text-purple-400" />
            </div>
            <div class="flex-1">
              <div class="font-medium">设置</div>
              <div class="text-xs text-gray-500">个人信息与偏好</div>
            </div>
            <FmIcon name="i-carbon:chevron-right" class="text-gray-400" />
          </div>
        </div>
        <!-- 心理健康工具 -->
        <FmPageMain title="心理健康工具" class="rounded-lg m-0! mt-4">
          <div class="grid grid-cols-4 gap-4">
            <div class="flex flex-col items-center gap-2 p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
              <FmIcon name="i-carbon:breathing" class="text-8 text-blue-500" />
              <div class="text-xs text-center">
                呼吸练习
              </div>
            </div>
            <div class="flex flex-col items-center gap-2 p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
              <FmIcon name="i-carbon:music" class="text-8 text-green-500" />
              <div class="text-xs text-center">
                音乐疗愈
              </div>
            </div>
            <div class="flex flex-col items-center gap-2 p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
              <FmIcon name="i-carbon:mood-happy" class="text-8 text-yellow-500" />
              <div class="text-xs text-center">
                情绪记录
              </div>
            </div>
            <div class="flex flex-col items-center gap-2 p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer">
              <FmIcon name="i-carbon:chart-line" class="text-8 text-purple-500" />
              <div class="text-xs text-center">
                成长报告
              </div>
            </div>
          </div>
        </FmPageMain>
      </div>

      <!-- 退出登录按钮 -->
      <FmButton block variant="outline" :loading @click="handleLogout">
        退出登录
      </FmButton>
    </div>
  </FmPageLayout>
</template>
