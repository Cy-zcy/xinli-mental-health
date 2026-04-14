<script setup lang="ts">
import { toast } from 'vue-sonner'
import { assessmentApi } from '@/api/modules'
import type { AssessmentItem } from '@/api/modules/assessment'

definePage({
  meta: {
    title: '心理测评',
    auth: false,
  },
})

const router = useRouter()
const loading = ref(false)
const assessments = ref<AssessmentItem[]>([])
const currentPage = ref(1)
const hasMore = ref(true)

async function loadAssessments(page = 1, append = false) {
  try {
    loading.value = true
    const res = await assessmentApi.getAssessmentList(page, 10)
    const list = res?.records || []
    if (append) {
      assessments.value.push(...list)
    }
    else {
      assessments.value = list
    }
    currentPage.value = page
    hasMore.value = list.length === 10
  }
  catch (e: any) {
    toast.error('加载失败', { description: e?.message || '无法获取问卷列表' })
  }
  finally {
    loading.value = false
  }
}

function startAssessment(id: number) {
  router.push(`/assessment/quiz/${id}`)
}

function viewHistory() {
  router.push('/assessment/history')
}

onMounted(() => loadAssessments())
</script>

<template>
  <FmPageLayout :navbar="false" tabbar>
    <div class="flex flex-1 flex-col bg-gray-50 dark:bg-gray-900 min-h-screen">
      <!-- 头部 Banner -->
      <div class="bg-gradient-to-br from-violet-500 to-indigo-600 px-5 pt-12 pb-8 text-white">
        <div class="flex items-center justify-between mb-2">
          <h1 class="text-2xl font-bold">
            心理测评
          </h1>
          <button
            class="bg-white/20 hover:bg-white/30 text-white text-sm flex items-center gap-1.5 px-3 py-1.5 rounded-full backdrop-blur-sm transition-all border border-white/30"
            @click="viewHistory"
          >
            <FmIcon name="i-carbon:document" class="text-4" />
            我的记录
          </button>
        </div>
        <p class="text-white/75 text-sm leading-relaxed">
          科学的心理评估，帮助你了解自己的内心状态，提供个性化的健康建议。
        </p>
      </div>

      <!-- 问卷列表 -->
      <div class="flex-1 p-4 -mt-4">
        <!-- 加载中 -->
        <FmLoading v-if="loading && assessments.length === 0" type="wave" text="加载中..." />

        <!-- 空状态 -->
        <div v-else-if="assessments.length === 0" class="flex justify-center items-center h-48">
          <div class="text-center text-gray-400">
            <FmIcon name="i-carbon:document" class="text-16 mb-3" />
            <p>暂无可用问卷</p>
          </div>
        </div>

        <!-- 问卷卡片列表 -->
        <div v-else class="space-y-4">
          <div
            v-for="item in assessments"
            :key="item.id"
            class="bg-white dark:bg-gray-800 rounded-2xl p-5 shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-md hover:border-violet-200 transition-all duration-200 cursor-pointer"
            @click="startAssessment(item.id)"
          >
            <div class="flex items-start gap-4">
              <!-- 图标 -->
              <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-violet-400 to-indigo-500 flex items-center justify-center flex-shrink-0 shadow-sm">
                <FmIcon name="i-carbon:chart-radar" class="text-7 text-white" />
              </div>
              <!-- 内容 -->
              <div class="flex-1 min-w-0">
                <h3 class="font-bold text-gray-900 dark:text-white text-base mb-1 line-clamp-1">
                  {{ item.title }}
                </h3>
                <p class="text-gray-500 dark:text-gray-400 text-sm line-clamp-2 leading-relaxed">
                  {{ item.description || '点击开始测评，了解你的心理健康状态。' }}
                </p>
                <div class="flex items-center gap-3 mt-3">
                  <span class="text-xs text-violet-500 bg-violet-50 dark:bg-violet-900/30 px-2 py-0.5 rounded-full">
                    心理评估
                  </span>
                  <span class="text-xs text-gray-400">约5分钟完成</span>
                </div>
              </div>
              <!-- 箭头 -->
              <FmIcon name="i-carbon:chevron-right" class="text-5 text-gray-400 flex-shrink-0 mt-1" />
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="hasMore" class="text-center py-4">
            <FmButton variant="outline" :loading="loading" class="px-8 rounded-full" @click="loadAssessments(currentPage + 1, true)">
              {{ loading ? '加载中...' : '加载更多' }}
            </FmButton>
          </div>
        </div>

        <!-- 免责声明 -->
        <div class="mt-6 bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-xl p-4">
          <div class="flex items-start gap-3">
            <FmIcon name="i-carbon:warning" class="text-5 text-amber-500 flex-shrink-0 mt-0.5" />
            <p class="text-xs text-amber-700 dark:text-amber-400 leading-relaxed">
              本测评仅作为参考，不构成医学诊断。若您存在较严重的心理困扰，建议及时寻求专业人员的帮助。
            </p>
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
