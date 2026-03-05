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
    <!-- 极简高级光晕背景 -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0 bg-slate-50 dark:bg-slate-950">
      <div class="absolute -top-[10%] -left-[10%] w-[60%] h-[60%] rounded-full bg-blue-400/10 blur-[100px] mix-blend-multiply dark:bg-blue-900/20 dark:mix-blend-screen animate-pulse-slow"></div>
      <div class="absolute top-[20%] -right-[10%] w-[50%] h-[50%] rounded-full bg-indigo-400/10 blur-[100px] mix-blend-multiply dark:bg-indigo-900/20 dark:mix-blend-screen animate-pulse-slow" style="animation-delay: 2s"></div>
    </div>

    <div class="flex flex-1 flex-col relative z-10">
      <!-- 顶部问候区域 -->
      <div class="px-5 pt-10 pb-6 animate-fade-in-down">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-extrabold bg-gradient-to-r from-slate-800 to-slate-600 dark:from-slate-100 dark:to-slate-300 bg-clip-text text-transparent tracking-tight">
              {{ greeting }}{{ userStore.isLogin ? `，${userStore.nickname}` : '' }}
            </h1>
            <p class="text-[13px] text-slate-500 dark:text-slate-400 mt-1.5 font-medium tracking-wide">
              {{ userStore.isLogin ? '愿你今天心情愉悦' : '欢迎来到心理健康治愈平台' }}
            </p>
          </div>
          <div class="w-12 h-12 rounded-2xl bg-white/80 dark:bg-slate-800/80 backdrop-blur-md shadow-sm border border-white dark:border-slate-700/50 flex items-center justify-center">
            <FmIcon name="i-ic:outline-favorite" class="text-indigo-500 text-2xl animate-pulse" />
          </div>
        </div>
      </div>

      <!-- 每日心语卡片 -->
      <div class="mx-5 mb-6 animate-fade-in-up">
        <div class="bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl rounded-3xl p-5 shadow-sm border border-white/50 dark:border-slate-700/50 relative overflow-hidden group">
          <div class="absolute top-0 right-0 w-24 h-24 bg-yellow-400/5 rounded-full blur-2xl -mr-8 -mt-8 pointer-events-none transition-transform group-hover:scale-110"></div>
          <div class="flex items-center justify-between mb-3 relative z-10">
            <h2 class="text-[15px] font-bold text-slate-800 dark:text-slate-200 flex items-center tracking-tight">
              <FmIcon name="i-ic:outline-lightbulb" class="mr-1.5 text-yellow-500 text-lg" />
              每日心语
            </h2>
            <button class="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300 bg-white/50 dark:bg-slate-800/50 p-1.5 rounded-full backdrop-blur-sm transition-all active:scale-95" @click="refreshTip">
              <FmIcon name="i-ic:outline-refresh" />
            </button>
          </div>
          <p class="text-[14px] text-slate-600 dark:text-slate-300 leading-relaxed font-medium relative z-10">
            {{ todayTip }}
          </p>
        </div>
      </div>

      <!-- 心理测评入口横幅（登录用户可见） -->
      <div v-if="userStore.isLogin" class="mx-5 mb-6 animate-fade-in-up" style="animation-delay: 0.1s">
        <div
          class="bg-gradient-to-r from-orange-400 to-rose-400 hover:from-orange-500 hover:to-rose-500 rounded-3xl p-5 text-white cursor-pointer active:scale-[0.98] transition-all duration-300 shadow-lg shadow-rose-500/20 relative overflow-hidden"
          @click="navigateTo('/assessment')"
        >
          <div class="absolute right-0 top-0 w-32 h-32 bg-white/10 rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none"></div>
          <div class="flex items-center justify-between relative z-10">
            <div>
              <h3 class="font-bold text-[16px] mb-1 tracking-tight">📊 心理健康测评</h3>
              <p class="text-[13px] opacity-90 font-medium">完成SDS量表，了解你当前的心理状态</p>
            </div>
            <div class="w-12 h-12 rounded-2xl bg-white/20 backdrop-blur-sm border border-white/20 flex items-center justify-center flex-shrink-0 shadow-sm">
              <FmIcon name="i-ic:outline-assignment" class="text-white text-2xl" />
            </div>
          </div>
          <div class="mt-4 flex items-center text-[13px] opacity-95 font-semibold bg-white/10 w-max px-3 py-1 rounded-full border border-white/10">
            <FmIcon name="i-ic:outline-play-circle" class="mr-1" />
            点击开始测评
            <FmIcon name="i-carbon:arrow-right" class="ml-1 text-[10px]" />
          </div>
        </div>
      </div>

      <!-- 快捷功能区域 -->
      <div class="px-5 mb-6 animate-fade-in-up" style="animation-delay: 0.2s">
        <h2 class="text-[17px] font-bold text-slate-800 dark:text-slate-200 mb-4 tracking-tight">
          快捷功能
        </h2>
        <div class="grid grid-cols-2 gap-3.5">
          <div
            v-for="action in quickActions"
            :key="action.path"
            class="bg-white/60 dark:bg-slate-900/60 backdrop-blur-xl rounded-3xl p-4 shadow-sm border border-white/50 dark:border-slate-700/50 cursor-pointer transition-all duration-300 hover:shadow-md hover:border-slate-300/50 dark:hover:border-slate-600/50 hover:-translate-y-0.5 active:scale-95 group relative overflow-hidden"
            @click="navigateTo(action.path)"
          >
            <div class="absolute inset-0 bg-gradient-to-br from-white/40 to-transparent dark:from-white/5 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            <div class="flex flex-col items-center text-center relative z-10">
              <div :class="`w-12 h-12 rounded-2xl bg-gradient-to-r ${action.color} flex items-center justify-center mb-3 shadow-sm shadow-${action.color.split('-')[1]}-500/20 group-hover:scale-110 transition-transform duration-300`">
                <FmIcon :name="action.icon" class="text-white text-xl" />
              </div>
              <h3 class="font-bold text-slate-800 dark:text-slate-200 text-[14px] mb-1 tracking-tight">
                {{ action.title }}
              </h3>
              <p class="text-[11px] text-slate-500 dark:text-slate-400 font-medium">
                {{ action.subtitle }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 登录提示（未登录用户） -->
      <div v-if="!userStore.isLogin" class="mx-5 mb-6 animate-fade-in-up" style="animation-delay: 0.3s">
        <div class="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-3xl p-5 text-white shadow-lg shadow-indigo-500/20 relative overflow-hidden">
          <div class="absolute top-0 right-0 w-40 h-40 bg-white/10 rounded-full blur-3xl -mr-10 -mt-10 pointer-events-none"></div>
          <h3 class="font-bold text-[16px] mb-2 tracking-tight relative z-10">开始您的心理健康之旅</h3>
          <p class="text-[13px] opacity-90 mb-4 font-medium relative z-10">
            登录后可以使用AI聊天、发布帖子等更多功能
          </p>
          <button class="bg-white/20 hover:bg-white/30 backdrop-blur-md border border-white/30 text-white text-[13px] font-bold px-6 py-2 rounded-full transition-all active:scale-95 relative z-10" @click="goToLogin">
            立即登录
          </button>
        </div>
      </div>

      <!-- 底部装饰 -->
      <div class="flex-1 flex items-end justify-center pb-8 animate-fade-in">
        <div class="text-center">
          <div class="w-14 h-14 mx-auto mb-3 rounded-2xl bg-white/50 dark:bg-slate-800/50 backdrop-blur-md border border-white/50 dark:border-slate-700/50 flex items-center justify-center shadow-sm">
            <FmIcon name="i-icon-park-outline:lotus" class="text-indigo-400 dark:text-indigo-500 text-3xl" />
          </div>
          <p class="text-[12px] text-slate-400 dark:text-slate-500 font-medium tracking-wide">
            心理健康，从关爱自己开始
          </p>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
