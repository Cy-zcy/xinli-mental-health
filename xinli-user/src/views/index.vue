<script setup lang="ts">
import { toast } from 'vue-sonner'

definePage({
  meta: {
    title: '心理健康治愈平台',
  },
})

const router = useRouter()
const userStore = useUserStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const quickActions = [
  {
    icon: 'i-ic:outline-chat',
    title: 'AI心理聊天',
    subtitle: '与AI助手倾诉心声',
    color: 'from-blue-400 to-blue-600',
    path: '/chat',
  },
  {
    icon: 'i-ic:outline-forum',
    title: '心理论坛',
    subtitle: '分享经历，互相支持',
    color: 'from-green-400 to-green-600',
    path: '/forum',
  },
  {
    icon: 'i-ic:outline-assignment',
    title: '心理测评',
    subtitle: '了解自己的心理状况',
    color: 'from-orange-400 to-rose-500',
    path: '/assessment',
  },
  {
    icon: 'i-ic:outline-healing',
    title: '治愈工具',
    subtitle: '放松身心，缓解压力',
    color: 'from-purple-400 to-purple-600',
    path: '/tools',
  },
  {
    icon: 'i-ic:outline-library-books',
    title: '心理资源',
    subtitle: '文章音频视频干预',
    color: 'from-teal-400 to-cyan-500',
    path: '/resource',
  },
  {
    icon: 'i-ic:outline-self-improvement',
    title: '个人中心',
    subtitle: '记录心情，追踪进步',
    color: 'from-pink-400 to-pink-600',
    path: '/profile',
  },
]

const dailyTips = [
  '每天给自己一些正面的话语，你值得被爱和关怀。',
  '深呼吸，让内心平静下来，一切都会好起来的。',
  '接纳自己的情绪，它们都是正常的人类体验。',
  '今天也要记得爱自己，你已经很棒了。',
  '困难只是暂时的，你比想象中更坚强。',
  '给自己一个拥抱，你今天辛苦了。',
  '每一个小进步都值得庆祝，为自己感到骄傲吧。',
]

const todayTip = ref(dailyTips[Math.floor(Math.random() * dailyTips.length)])

function navigateTo(path: string) {
  if (userStore.isLogin) {
    router.push(path)
  } else {
    toast.warning('请先登录')
    router.push('/login')
  }
}

function goToLogin() {
  router.push('/login')
}

function refreshTip() {
  todayTip.value = dailyTips[Math.floor(Math.random() * dailyTips.length)]
}
</script>

<template>
  <FmPageLayout :navbar="false" copyright tabbar>
    <div class="flex flex-1 flex-col bg-gradient-to-br from-blue-50 to-purple-50 dark:from-gray-900 dark:to-gray-800">
      <!-- 顶部问候区域 -->
      <div class="px-4 pt-8 pb-4">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
              {{ greeting }}{{ userStore.isLogin ? `，${userStore.nickname}` : '' }}
            </h1>
            <p class="text-sm text-gray-600 dark:text-gray-300 mt-1">
              {{ userStore.isLogin ? '愿你今天心情愉悦' : '欢迎来到心理健康治愈平台' }}
            </p>
          </div>
          <div class="w-12 h-12 rounded-full bg-gradient-to-r from-blue-400 to-purple-500 flex items-center justify-center">
            <FmIcon name="i-ic:outline-favorite" class="text-white text-xl" />
          </div>
        </div>
      </div>

      <!-- 每日心语卡片 -->
      <div class="mx-4 mb-6">
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-4 shadow-sm border border-gray-100 dark:border-gray-700">
          <div class="flex items-center justify-between mb-3">
            <h2 class="text-lg font-semibold text-gray-800 dark:text-white flex items-center">
              <FmIcon name="i-ic:outline-lightbulb" class="mr-2 text-yellow-500" />
              每日心语
            </h2>
            <FmButton variant="ghost" size="sm" @click="refreshTip">
              <FmIcon name="i-ic:outline-refresh" />
            </FmButton>
          </div>
          <p class="text-gray-600 dark:text-gray-300 leading-relaxed">
            {{ todayTip }}
          </p>
        </div>
      </div>

      <!-- 心理测评入口横幅（登录用户可见） -->
      <div v-if="userStore.isLogin" class="mx-4 mb-5">
        <div
          class="bg-gradient-to-r from-orange-400 to-rose-500 rounded-2xl p-4 text-white cursor-pointer active:scale-95 transition-all duration-200 shadow-md"
          @click="navigateTo('/assessment')"
        >
          <div class="flex items-center justify-between">
            <div>
              <h3 class="font-bold text-base mb-1">📊 心理健康小测评</h3>
              <p class="text-sm opacity-90">完成SDS自评量表，了解你当前的心理状态</p>
            </div>
            <div class="w-12 h-12 rounded-full bg-white/20 flex items-center justify-center flex-shrink-0">
              <FmIcon name="i-ic:outline-assignment" class="text-white text-2xl" />
            </div>
          </div>
          <div class="mt-3 flex items-center text-sm opacity-90 font-medium">
            <FmIcon name="i-ic:outline-play-circle" class="mr-1" />
            点击开始测评 →
          </div>
        </div>
      </div>

      <!-- 快捷功能区域 -->
      <div class="px-4 mb-6">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">
          快捷功能
        </h2>
        <div class="grid grid-cols-2 gap-3">
          <div
            v-for="action in quickActions"
            :key="action.path"
            class="bg-white dark:bg-gray-800 rounded-2xl p-4 shadow-sm border border-gray-100 dark:border-gray-700 cursor-pointer transition-all duration-200 hover:shadow-md active:scale-95"
            @click="navigateTo(action.path)"
          >
            <div class="flex flex-col items-center text-center">
              <div :class="`w-12 h-12 rounded-full bg-gradient-to-r ${action.color} flex items-center justify-center mb-3`">
                <FmIcon :name="action.icon" class="text-white text-xl" />
              </div>
              <h3 class="font-semibold text-gray-800 dark:text-white text-sm mb-1">
                {{ action.title }}
              </h3>
              <p class="text-xs text-gray-500 dark:text-gray-400">
                {{ action.subtitle }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 登录提示（未登录用户） -->
      <div v-if="!userStore.isLogin" class="mx-4 mb-6">
        <div class="bg-gradient-to-r from-blue-500 to-purple-600 rounded-2xl p-4 text-white">
          <h3 class="font-semibold mb-2">开始您的心理健康之旅</h3>
          <p class="text-sm opacity-90 mb-3">
            登录后可以使用AI聊天、发布帖子等更多功能
          </p>
          <FmButton variant="secondary" size="sm" @click="goToLogin">
            立即登录
          </FmButton>
        </div>
      </div>

      <!-- 底部装饰 -->
      <div class="flex-1 flex items-end justify-center pb-8">
        <div class="text-center">
          <div class="w-16 h-16 mx-auto mb-3 rounded-full bg-gradient-to-r from-pink-400 to-purple-500 flex items-center justify-center">
            <FmIcon name="i-ic:outline-psychology" class="text-white text-2xl" />
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">
            心理健康，从关爱自己开始
          </p>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
