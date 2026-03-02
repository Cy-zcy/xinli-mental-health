<script setup lang="ts">
definePage({
  meta: {
    title: '测评结果',
    auth: false,
  },
})

const route = useRoute()
const router = useRouter()

// 从路由 query 获取结果数据（由答题页携带过来）
const title = route.query.title as string || '心理测评'
const score = Number(route.query.score) || 0
const summary = route.query.summary as string || ''
const details = route.query.details as string || ''

// 根据结论设置配色
const colorConfig = computed(() => {
  if (summary.includes('正常')) {
    return { bg: 'from-green-400 to-teal-500', badge: 'bg-green-100 text-green-700', icon: 'i-carbon:checkmark-outline' }
  }
  else if (summary.includes('轻度')) {
    return { bg: 'from-yellow-400 to-amber-500', badge: 'bg-yellow-100 text-yellow-700', icon: 'i-carbon:warning-alt' }
  }
  else if (summary.includes('中度')) {
    return { bg: 'from-orange-400 to-red-400', badge: 'bg-orange-100 text-orange-700', icon: 'i-carbon:warning' }
  }
  else {
    return { bg: 'from-red-500 to-rose-600', badge: 'bg-red-100 text-red-700', icon: 'i-carbon:warning-filled' }
  }
})

function goHome() {
  router.push('/assessment')
}

function goHistory() {
  router.push('/assessment/history')
}

function goChat() {
  router.push('/chat')
}
</script>

<template>
  <FmPageLayout :navbar="false" :tabbar="false">
    <div class="flex flex-1 flex-col bg-gray-50 dark:bg-gray-900 min-h-screen">

      <!-- 结果头部 Banner -->
      <div :class="`bg-gradient-to-br ${colorConfig.bg} px-5 pt-14 pb-12 text-white text-center relative`">
        <FmIcon :name="colorConfig.icon" class="text-20 mb-4 opacity-90" />
        <h1 class="text-2xl font-bold mb-1">
          测评完成
        </h1>
        <p class="text-white/80 text-sm">
          {{ title }}
        </p>

        <!-- 分数圆圈 -->
        <div class="absolute -bottom-8 left-1/2 -translate-x-1/2 w-20 h-20 rounded-full bg-white shadow-lg flex flex-col items-center justify-center">
          <span class="text-2xl font-extrabold text-gray-800">{{ score }}</span>
          <span class="text-xs text-gray-500">得分</span>
        </div>
      </div>

      <!-- 结果详情卡片 -->
      <div class="px-5 pt-14 pb-6 space-y-4">
        <!-- 结论标签 -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-5 shadow-sm border border-gray-100 dark:border-gray-700 text-center">
          <p class="text-gray-500 text-sm mb-3">
            您的当前状态评估结论
          </p>
          <span :class="`inline-block px-5 py-2 rounded-full text-lg font-bold ${colorConfig.badge}`">
            {{ summary }}
          </span>
        </div>

        <!-- 建议详情 -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-5 shadow-sm border border-gray-100 dark:border-gray-700">
          <h3 class="font-bold text-gray-900 dark:text-white flex items-center gap-2 mb-3">
            <FmIcon name="i-carbon:idea" class="text-5 text-violet-500" />
            <span>专业建议</span>
          </h3>
          <p class="text-gray-600 dark:text-gray-400 text-sm leading-relaxed">
            {{ details }}
          </p>
        </div>

        <!-- 免责说明 -->
        <div class="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-xl p-4">
          <div class="flex gap-3">
            <FmIcon name="i-carbon:warning" class="text-4 text-amber-500 flex-shrink-0 mt-0.5" />
            <p class="text-xs text-amber-700 dark:text-amber-400 leading-relaxed">
              本测评结果仅供参考，不构成医学诊断。如有需要，请及时寻求专业心理咨询师的帮助。
            </p>
          </div>
        </div>

        <!-- 操作按钮组 -->
        <div class="space-y-3 pt-2">
          <FmButton class="w-full py-3 rounded-xl bg-gradient-to-r from-violet-500 to-indigo-600 border-0 font-bold" @click="goChat">
            <FmIcon name="i-carbon:chat" class="mr-2" />
            与AI助手聊聊
          </FmButton>
          <div class="flex gap-3">
            <FmButton variant="outline" class="flex-1 py-3 rounded-xl" @click="goHistory">
              查看历史记录
            </FmButton>
            <FmButton variant="outline" class="flex-1 py-3 rounded-xl" @click="goHome">
              返回测评列表
            </FmButton>
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
