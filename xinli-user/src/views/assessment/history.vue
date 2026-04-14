<script setup lang="ts">
import { toast } from 'vue-sonner'
import { assessmentApi } from '@/api/modules'
import type { AssessmentResultDTO } from '@/api/modules/assessment'

definePage({
  meta: {
    title: '测评历史',
    auth: true,
  },
})

const router = useRouter()
const loading = ref(false)
const records = ref<AssessmentResultDTO[]>([])

async function loadHistory() {
  try {
    loading.value = true
    records.value = await assessmentApi.getUserAssessmentHistory()
  }
  catch (e: any) {
    toast.error('加载失败', { description: e?.message || '无法获取历史记录' })
  }
  finally {
    loading.value = false
  }
}

function formatTime(dateString: string) {
  if (!dateString)
    return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getSummaryColor(summary: string) {
  if (summary?.includes('正常'))
    return 'bg-green-100 text-green-700'
  if (summary?.includes('轻度'))
    return 'bg-yellow-100 text-yellow-700'
  if (summary?.includes('中度'))
    return 'bg-orange-100 text-orange-700'
  return 'bg-red-100 text-red-700'
}

function viewResult(record: AssessmentResultDTO) {
  router.push({
    path: '/assessment/result',
    query: {
      recordId: record.recordId,
      title: record.assessmentTitle,
      score: record.totalScore,
      summary: record.resultSummary,
      details: record.resultDetails,
    },
  })
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/assessment')
  }
}

function goHome() {
  router.push('/')
}

onMounted(() => loadHistory())
</script>

<template>
  <FmPageLayout :navbar="false" :tabbar="false">
    <div class="flex flex-1 flex-col bg-gray-50 dark:bg-gray-900 min-h-screen">

      <!-- 顶部导航栏 -->
      <div class="bg-white dark:bg-gray-800 px-4 py-3 flex items-center gap-3 border-b border-gray-100 dark:border-gray-700">
        <FmButton variant="ghost" size="icon" @click="goBack">
          <FmIcon name="i-carbon:arrow-left" class="text-5" />
        </FmButton>
        <h2 class="font-bold text-gray-900 dark:text-white text-base flex-1">
          我的测评记录
        </h2>
        <FmIcon name="i-carbon:home" class="text-xl text-gray-600 dark:text-gray-300 active:opacity-60" @click="goHome" />
      </div>

      <!-- 加载中 -->
      <FmLoading v-if="loading" type="wave" text="加载中..." />

      <!-- 空状态 -->
      <div v-else-if="records.length === 0" class="flex justify-center items-center flex-1">
        <div class="text-center text-gray-400 px-8">
          <FmIcon name="i-carbon:document" class="text-20 mb-4" />
          <p class="text-base font-medium mb-2">
            暂无测评记录
          </p>
          <p class="text-sm mb-6">
            完成一次心理测评，了解你的内心状态
          </p>
          <FmButton class="bg-gradient-to-r from-violet-500 to-indigo-600 border-0" @click="router.push('/assessment')">
            立即测评
          </FmButton>
        </div>
      </div>

      <!-- 记录列表 -->
      <div v-else class="p-4 space-y-4">
        <div
          v-for="record in records"
          :key="record.recordId"
          class="bg-white dark:bg-gray-800 rounded-2xl p-5 shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-md hover:border-violet-200 transition-all duration-200 cursor-pointer"
          @click="viewResult(record)"
        >
          <div class="flex items-start justify-between gap-3 mb-3">
            <h3 class="font-bold text-gray-900 dark:text-white text-sm flex-1 line-clamp-1">
              {{ record.assessmentTitle }}
            </h3>
            <span :class="`text-xs px-3 py-1 rounded-full font-medium flex-shrink-0 ${getSummaryColor(record.resultSummary)}`">
              {{ record.resultSummary }}
            </span>
          </div>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <FmIcon name="i-carbon:chart-bar" class="text-4 text-violet-400" />
              <span class="text-sm text-gray-600 dark:text-gray-400">得分：<span class="font-bold text-violet-600">{{ record.totalScore }}</span> 分</span>
            </div>
            <div class="flex items-center gap-1 text-xs text-gray-400">
              <FmIcon name="i-carbon:time" class="text-3.5" />
              <span>{{ formatTime(record.createdAt) }}</span>
            </div>
          </div>
          <div class="mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
            <p class="text-xs text-gray-500 dark:text-gray-400 line-clamp-2 leading-relaxed">
              {{ record.resultDetails }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
