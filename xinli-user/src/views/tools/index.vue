<script setup lang="ts">
import { toast } from 'vue-sonner'

definePage({
  meta: {
    title: '治愈工具',
    auth: true,
  },
})

const router = useRouter()

const tools = [
  {
    id: 'breathing',
    title: '呼吸练习',
    subtitle: '通过深呼吸放松身心',
    description: '4-7-8呼吸法，帮助缓解焦虑和压力',
    icon: 'i-ic:outline-air',
    color: 'from-blue-400 to-cyan-500',
    path: '/tools/breathing',
    duration: '5-10分钟',
    difficulty: '简单',
  },
  {
    id: 'meditation',
    title: '冥想引导',
    subtitle: '正念冥想，平静内心',
    description: '引导式冥想，帮助专注当下，减少杂念',
    icon: 'i-ic:outline-self-improvement',
    color: 'from-purple-400 to-pink-500',
    path: '/tools/meditation',
    duration: '10-20分钟',
    difficulty: '中等',
  },
  {
    id: 'mood-tracker',
    title: '情绪记录',
    subtitle: '记录和追踪情绪变化',
    description: '每日情绪打卡，了解情绪模式',
    icon: 'i-ic:outline-mood',
    color: 'from-green-400 to-teal-500',
    path: '/tools/mood-tracker',
    duration: '2-5分钟',
    difficulty: '简单',
  },
  {
    id: 'gratitude',
    title: '感恩日记',
    subtitle: '记录生活中的美好',
    description: '培养感恩心态，提升幸福感',
    icon: 'i-ic:outline-favorite',
    color: 'from-pink-400 to-rose-500',
    path: '/tools/gratitude',
    duration: '5-10分钟',
    difficulty: '简单',
  },
  {
    id: 'progressive-relaxation',
    title: '渐进式放松',
    subtitle: '肌肉放松训练',
    description: '通过肌肉紧张和放松来缓解身体压力',
    icon: 'i-ic:outline-spa',
    color: 'from-indigo-400 to-purple-500',
    path: '/tools/progressive-relaxation',
    duration: '15-20分钟',
    difficulty: '中等',
  },
  {
    id: 'positive-affirmations',
    title: '正面肯定',
    subtitle: '积极的自我暗示',
    description: '通过正面话语增强自信和积极心态',
    icon: 'i-ic:outline-psychology',
    color: 'from-yellow-400 to-orange-500',
    path: '/tools/positive-affirmations',
    duration: '3-5分钟',
    difficulty: '简单',
  },
]

const categories = [
  { id: 'all', name: '全部', icon: 'i-ic:outline-apps' },
  { id: 'relaxation', name: '放松', icon: 'i-ic:outline-spa' },
  { id: 'mindfulness', name: '正念', icon: 'i-ic:outline-self-improvement' },
  { id: 'tracking', name: '记录', icon: 'i-ic:outline-analytics' },
]

const currentCategory = ref('all')
const searchKeyword = ref('')

const filteredTools = computed(() => {
  let filtered = tools

  // 分类筛选
  if (currentCategory.value !== 'all') {
    filtered = filtered.filter(tool => {
      switch (currentCategory.value) {
        case 'relaxation':
          return ['breathing', 'meditation', 'progressive-relaxation'].includes(tool.id)
        case 'mindfulness':
          return ['meditation', 'positive-affirmations'].includes(tool.id)
        case 'tracking':
          return ['mood-tracker', 'gratitude'].includes(tool.id)
        default:
          return true
      }
    })
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(tool =>
      tool.title.toLowerCase().includes(keyword) ||
      tool.subtitle.toLowerCase().includes(keyword) ||
      tool.description.toLowerCase().includes(keyword)
    )
  }

  return filtered
})

function navigateToTool(tool: typeof tools[0]) {
  if (tool.path) {
    router.push(tool.path)
  } else {
    toast.info('功能开发中', {
      description: `${tool.title}功能即将上线，敬请期待`,
    })
  }
}

function getDifficultyColor(difficulty: string) {
  switch (difficulty) {
    case '简单':
      return 'text-green-600 bg-green-100 dark:bg-green-900/30 dark:text-green-400'
    case '中等':
      return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-900/30 dark:text-yellow-400'
    case '困难':
      return 'text-red-600 bg-red-100 dark:bg-red-900/30 dark:text-red-400'
    default:
      return 'text-gray-600 bg-gray-100 dark:bg-gray-900/30 dark:text-gray-400'
  }
}
</script>

<template>
  <FmPageLayout :navbar="false" tabbar>
    <div class="flex flex-1 flex-col bg-gradient-to-br from-blue-50 to-purple-50 dark:from-gray-900 dark:to-gray-800">
      <!-- 头部 -->
      <div class="p-4 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border-b">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h1 class="text-xl font-bold text-gray-800 dark:text-white">治愈工具</h1>
            <p class="text-sm text-gray-600 dark:text-gray-300">选择适合的工具，开始心理调节之旅</p>
          </div>
          <div class="w-12 h-12 rounded-full bg-gradient-to-r from-purple-400 to-pink-500 flex items-center justify-center">
            <FmIcon name="i-ic:outline-healing" class="text-white text-xl" />
          </div>
        </div>

        <!-- 搜索框 -->
        <div class="mb-4">
          <FmInput
            v-model="searchKeyword"
            placeholder="搜索工具..."
            class="w-full"
          >
            <template #prefix>
              <FmIcon name="i-ic:outline-search" class="text-gray-400" />
            </template>
          </FmInput>
        </div>

        <!-- 分类标签 -->
        <div class="flex gap-2 overflow-x-auto">
          <FmButton
            v-for="category in categories"
            :key="category.id"
            :variant="currentCategory === category.id ? 'default' : 'outline'"
            size="sm"
            @click="currentCategory = category.id"
          >
            <FmIcon :name="category.icon" class="mr-1" />
            {{ category.name }}
          </FmButton>
        </div>
      </div>

      <!-- 工具列表 -->
      <div class="flex-1 overflow-y-auto p-4">
        <div v-if="filteredTools.length === 0" class="flex items-center justify-center h-64">
          <div class="text-center">
            <FmIcon name="i-ic:outline-search-off" class="text-12 text-gray-400 mb-4" />
            <p class="text-gray-500">没有找到相关工具</p>
            <FmButton class="mt-4" @click="searchKeyword = ''; currentCategory = 'all'">
              重置筛选
            </FmButton>
          </div>
        </div>

        <div v-else class="grid grid-cols-1 gap-4">
          <div
            v-for="tool in filteredTools"
            :key="tool.id"
            class="bg-white dark:bg-gray-800 rounded-2xl p-4 shadow-sm border border-gray-100 dark:border-gray-700 cursor-pointer transition-all duration-200 hover:shadow-md active:scale-95"
            @click="navigateToTool(tool)"
          >
            <div class="flex items-start gap-4">
              <!-- 工具图标 -->
              <div :class="`w-16 h-16 rounded-2xl bg-gradient-to-r ${tool.color} flex items-center justify-center flex-shrink-0`">
                <FmIcon :name="tool.icon" class="text-white text-2xl" />
              </div>

              <!-- 工具信息 -->
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between mb-2">
                  <div>
                    <h3 class="font-semibold text-gray-800 dark:text-white text-lg">
                      {{ tool.title }}
                    </h3>
                    <p class="text-sm text-gray-600 dark:text-gray-400">
                      {{ tool.subtitle }}
                    </p>
                  </div>
                  <FmIcon name="i-ic:outline-arrow-forward" class="text-gray-400 text-xl" />
                </div>

                <p class="text-sm text-gray-600 dark:text-gray-300 mb-3 line-clamp-2">
                  {{ tool.description }}
                </p>

                <!-- 工具标签 -->
                <div class="flex items-center gap-2 flex-wrap">
                  <span class="px-2 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-xs rounded-full">
                    {{ tool.duration }}
                  </span>
                  <span :class="`px-2 py-1 text-xs rounded-full ${getDifficultyColor(tool.difficulty)}`">
                    {{ tool.difficulty }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="p-4 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border-t">
        <div class="text-center">
          <p class="text-sm text-gray-600 dark:text-gray-400">
            建议每天花10-20分钟使用这些工具
          </p>
          <p class="text-xs text-gray-500 dark:text-gray-500 mt-1">
            持续练习效果更佳 ✨
          </p>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
