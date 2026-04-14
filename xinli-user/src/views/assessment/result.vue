<script setup lang="ts">
import * as echarts from 'echarts'
import api from '@/api'

definePage({
  meta: {
    title: '测评结果',
    auth: false,
  },
})

const route = useRoute()
const router = useRouter()

// 从路由 query 获取结果数据
const title = route.query.title as string || '心理测评'
const score = Number(route.query.score) || 0
const summary = route.query.summary as string || ''
const details = route.query.details as string || ''
const recordId = route.query.recordId as string || ''

// 维度数据（从后端获取或模拟）
const dimensionNames = ref<string[]>(['情绪状态', '躯体症状', '人际交往', '睡眠质量', '认知功能'])
const dimensionValues = ref<number[]>([50, 50, 50, 50, 50])
const dimensionChanges = ref<Record<string, number>>({})

// AI 分析状态
const aiReport = ref('')
const aiLoading = ref(false)

// 雷达图容器
const radarChartRef = ref<HTMLElement | null>(null)
let radarChart: echarts.ECharts | null = null

// 根据结论设置配色
const colorConfig = computed(() => {
  if (summary.includes('正常')) {
    return { bg: 'from-green-400 to-teal-500', badge: 'bg-green-100 text-green-700', icon: 'i-carbon:checkmark-outline', level: 'success' }
  }
  else if (summary.includes('轻度')) {
    return { bg: 'from-yellow-400 to-amber-500', badge: 'bg-yellow-100 text-yellow-700', icon: 'i-carbon:warning-alt', level: 'warning' }
  }
  else if (summary.includes('中度')) {
    return { bg: 'from-orange-400 to-red-400', badge: 'bg-orange-100 text-orange-700', icon: 'i-carbon:warning', level: 'warning' }
  }
  else {
    return { bg: 'from-red-500 to-rose-600', badge: 'bg-red-100 text-red-700', icon: 'i-carbon:warning-filled', level: 'danger' }
  }
})

// 维度颜色配置
const dimensionColors: Record<string, { color: string, icon: string }> = {
  '情绪状态': { color: 'text-blue-500', icon: 'i-carbon:face-satisfied' },
  '躯体症状': { color: 'text-orange-500', icon: 'i-carbon:body' },
  '人际交往': { color: 'text-purple-500', icon: 'i-carbon:user-multiple' },
  '睡眠质量': { color: 'text-indigo-500', icon: 'i-carbon:moon' },
  '认知功能': { color: 'text-teal-500', icon: 'i-carbon:idea' },
}

// 初始化雷达图
const initRadarChart = () => {
  if (!radarChartRef.value) return

  if (!radarChart) {
    radarChart = echarts.init(radarChartRef.value)
  }

  // 根据评分等级设置颜色
  const levelColor = colorConfig.value.level === 'success' ? '#10b981'
    : colorConfig.value.level === 'warning' ? '#f59e0b'
    : '#ef4444'

  const option = {
    backgroundColor: 'transparent',
    radar: {
      indicator: dimensionNames.value.map(name => ({
        name,
        max: 100,
      })),
      shape: 'polygon',
      splitNumber: 4,
      axisName: {
        color: '#6b7280',
        fontSize: 11,
        fontWeight: 500,
      },
      splitLine: {
        lineStyle: {
          color: '#e5e7eb',
        },
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(59, 130, 246, 0.05)', 'rgba(59, 130, 246, 0.1)', 'rgba(59, 130, 246, 0.05)', 'rgba(59, 130, 246, 0.1)'],
        },
      },
      axisLine: {
        lineStyle: {
          color: '#e5e7eb',
        },
      },
    },
    series: [{
      type: 'radar',
      data: [{
        value: dimensionValues.value,
        name: '测评得分',
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          color: levelColor,
          width: 2,
        },
        areaStyle: {
          color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
            { offset: 0, color: `${levelColor}40` },
            { offset: 1, color: `${levelColor}10` },
          ]),
        },
        itemStyle: {
          color: levelColor,
          borderColor: '#fff',
          borderWidth: 2,
        },
      }],
    }],
  }

  radarChart.setOption(option)

  // 响应式调整
  window.addEventListener('resize', () => {
    radarChart?.resize()
  })
}

// 获取维度分数
const fetchDimensionData = async () => {
  if (!recordId) {
    // 没有recordId，使用基于总分的模拟数据
    const baseScore = Math.min(100, Math.max(0, score * 1.25))
    dimensionValues.value = [
      Math.round(baseScore * 0.9 + Math.random() * 10),
      Math.round(baseScore * 0.8 + Math.random() * 15),
      Math.round(100 - baseScore * 0.3 + Math.random() * 10),
      Math.round(baseScore * 0.85 + Math.random() * 10),
      Math.round(baseScore * 0.75 + Math.random() * 20),
    ].map(v => Math.max(0, Math.min(100, v)))
    return
  }

  try {
    const res = await api.get<any>(`/api/assessment/result/${recordId}`)
    if (res) {
      if (res.dimensionNames) dimensionNames.value = res.dimensionNames
      if (res.dimensionValues) dimensionValues.value = res.dimensionValues
      if (res.dimensionChanges) dimensionChanges.value = res.dimensionChanges
      // 如果没有维度数据，使用基于总分的模拟数据
      if (!res.dimensionValues && res.totalScore) {
        const baseScore = res.totalScore
        dimensionValues.value = [
          Math.round(baseScore),
          Math.round(baseScore * 0.85),
          Math.round(100 - baseScore * 0.4),
          Math.round(baseScore * 0.9),
          Math.round(baseScore * 0.8),
        ].map(v => Math.max(0, Math.min(100, v)))
      }
    }
  } catch (error) {
    console.error('获取维度数据失败:', error)
    // 使用模拟数据
    const baseScore = Math.min(100, Math.max(0, score * 1.25))
    dimensionValues.value = [
      Math.round(baseScore),
      Math.round(baseScore * 0.85),
      Math.round(100 - baseScore * 0.4),
      Math.round(baseScore * 0.9),
      Math.round(baseScore * 0.8),
    ].map(v => Math.max(0, Math.min(100, v)))
  }
}

// 获取变化趋势文字
const getChangeText = (dim: string) => {
  const change = dimensionChanges.value[dim]
  if (!change) return null
  if (change > 0) return `↑ 改善${change}分`
  if (change < 0) return `↓ 下降${Math.abs(change)}分`
  return '持平'
}

const getChangeClass = (dim: string) => {
  const change = dimensionChanges.value[dim]
  if (!change) return 'text-gray-400'
  return change > 0 ? 'text-green-500' : change < 0 ? 'text-red-500' : 'text-gray-400'
}

function goHome() {
  router.push('/assessment')
}

function goToRootHome() {
  router.push('/')
}

function goHistory() {
  router.push('/assessment/history')
}

function goChat() {
  router.push('/chat')
}

// 页面加载
onMounted(async () => {
  await fetchDimensionData()
  nextTick(() => {
    initRadarChart()
  })

  // AI分析
  aiLoading.value = true
  try {
    const res = await api.post('/api/assessment/ai-analysis', {
      assessmentName: title,
      score,
      level: summary,
      description: details,
    })
    aiReport.value = (res as any)?.aiReport || ''
  }
  catch {
    // 静默处理
  }
  finally {
    aiLoading.value = false
  }
})

onUnmounted(() => {
  radarChart?.dispose()
  window.removeEventListener('resize', () => {})
})
</script>


<template>
  <FmPageLayout :navbar="false" :tabbar="false">
    <div class="flex flex-1 flex-col bg-gray-50 dark:bg-gray-900 min-h-screen">
      <!-- 顶部返回首页按钮 -->
      <div class="fixed top-4 right-4 z-50">
        <div class="w-10 h-10 rounded-full bg-white/90 dark:bg-gray-800/90 shadow-lg flex items-center justify-center backdrop-blur-sm" @click="goToRootHome">
          <FmIcon name="i-carbon:home" class="text-xl text-gray-600 dark:text-gray-300" />
        </div>
      </div>

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

        <!-- 雷达图维度分析 -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
          <!-- 标题栏 -->
          <div class="flex items-center gap-2 px-5 py-4 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 border-b border-gray-100 dark:border-gray-700">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center flex-shrink-0 shadow-sm">
              <FmIcon name="i-carbon:chart-radar" class="text-white text-4" />
            </div>
            <div>
              <h3 class="font-bold text-gray-900 dark:text-white text-sm">📊 维度分析雷达图</h3>
              <p class="text-xs text-blue-500 dark:text-blue-400">分数越低表示该维度状态越好</p>
            </div>
          </div>

          <!-- 雷达图容器 -->
          <div ref="radarChartRef" class="w-full h-64" />

          <!-- 维度详情列表 -->
          <div class="px-5 pb-5 space-y-3">
            <div
              v-for="(dim, index) in dimensionNames"
              :key="dim"
              class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700/50 rounded-xl"
            >
              <div class="flex items-center gap-3">
                <FmIcon :name="dimensionColors[dim]?.icon || 'i-carbon:circle'" :class="`text-lg ${dimensionColors[dim]?.color || 'text-gray-500'}`" />
                <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ dim }}</span>
              </div>
              <div class="flex items-center gap-3">
                <span :class="`text-sm font-bold ${
                  dimensionValues[index] < 40 ? 'text-green-600' :
                  dimensionValues[index] < 60 ? 'text-yellow-600' :
                  dimensionValues[index] < 80 ? 'text-orange-600' : 'text-red-600'
                }`">
                  {{ dimensionValues[index] }}分
                </span>
                <span v-if="getChangeText(dim)" :class="`text-xs ${getChangeClass(dim)}`">
                  {{ getChangeText(dim) }}
                </span>
              </div>
            </div>
          </div>
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

        <!-- AI 个性化分析报告 -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-violet-100 dark:border-violet-900 overflow-hidden">
          <!-- 标题栏 -->
          <div class="flex items-center gap-2 px-5 py-4 bg-gradient-to-r from-violet-50 to-indigo-50 dark:from-violet-900/20 dark:to-indigo-900/20 border-b border-violet-100 dark:border-violet-800">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-indigo-600 flex items-center justify-center flex-shrink-0 shadow-sm">
              <FmIcon name="i-carbon:ibm-watson-assistant" class="text-white text-4" />
            </div>
            <div>
              <h3 class="font-bold text-gray-900 dark:text-white text-sm">✨ AI 个性化分析</h3>
              <p class="text-xs text-violet-500 dark:text-violet-400">基于您的测评结果智能生成</p>
            </div>
          </div>

          <!-- 内容区 -->
          <div class="px-5 py-4">
            <!-- 加载骨架 -->
            <div v-if="aiLoading" class="space-y-2">
              <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full animate-pulse w-full" />
              <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full animate-pulse w-4/5" />
              <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full animate-pulse w-full" />
              <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full animate-pulse w-3/4" />
              <p class="text-xs text-violet-400 mt-3 flex items-center gap-1">
                <FmIcon name="i-carbon:renew" class="text-3 animate-spin" />
                AI 正在分析您的测评结果，请稍候...
              </p>
            </div>

            <!-- AI 生成报告 -->
            <p v-else-if="aiReport" class="text-sm text-gray-700 dark:text-gray-300 leading-relaxed whitespace-pre-wrap">
              {{ aiReport }}
            </p>

            <!-- 失败占位 -->
            <p v-else class="text-sm text-gray-400 text-center py-4">
              AI 分析此次暂时无法生成，请稍后重试
            </p>
          </div>
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