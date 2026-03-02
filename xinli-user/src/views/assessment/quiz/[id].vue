<script setup lang="ts">
import { toast } from 'vue-sonner'
import { assessmentApi } from '@/api/modules'
import type { AssessmentDetailDTO, AssessmentQuestionDTO } from '@/api/modules/assessment'

definePage({
  meta: {
    title: '开始测评',
    auth: false,
  },
})

const route = useRoute()
const router = useRouter()
const assessmentId = Number(route.params.id)

const loading = ref(false)
const submitting = ref(false)
const assessment = ref<AssessmentDetailDTO | null>(null)
const currentIndex = ref(0)   // 当前题目索引
const answers = ref<Record<number, number>>({}) // questionId -> optionId

// 当前题目
const currentQuestion = computed<AssessmentQuestionDTO | null>(
  () => assessment.value?.questions[currentIndex.value] ?? null,
)

// 总题数
const totalCount = computed(() => assessment.value?.questions.length ?? 0)
// 进度百分比
const progress = computed(() => totalCount.value ? Math.round(((currentIndex.value) / totalCount.value) * 100) : 0)
// 当前题是否已选
const currentAnswered = computed(() =>
  currentQuestion.value ? answers.value[currentQuestion.value.id] !== undefined : false,
)
// 是否最后一题
const isLast = computed(() => currentIndex.value === totalCount.value - 1)

async function loadAssessment() {
  try {
    loading.value = true
    assessment.value = await assessmentApi.getAssessmentDetail(assessmentId)
  }
  catch (e: any) {
    toast.error('加载失败', { description: e?.message || '无法获取问卷' })
    router.back()
  }
  finally {
    loading.value = false
  }
}

function selectOption(questionId: number, optionId: number) {
  answers.value[questionId] = optionId
}

function nextQuestion() {
  if (!currentAnswered.value) {
    toast.warning('请选择一个选项后再继续')
    return
  }
  if (!isLast.value) {
    currentIndex.value++
  }
}

function prevQuestion() {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

async function submitAnswers() {
  if (!currentAnswered.value) {
    toast.warning('请完成当前题目')
    return
  }
  // 验证所有题目都已作答
  const unanswered = assessment.value?.questions.filter((q: { id: number }) => answers.value[q.id] === undefined)
  if (unanswered && unanswered.length > 0) {
    toast.warning('请完成所有题目后再提交')
    return
  }
  try {
    submitting.value = true
    const result = await assessmentApi.submitAssessment({
      assessmentId,
      answers: answers.value,
    })
    // 跳转到结果页，携带结果数据
    router.push({
      path: '/assessment/result',
      query: {
        recordId: result.recordId,
        title: result.assessmentTitle,
        score: result.totalScore,
        summary: result.resultSummary,
        details: result.resultDetails,
      },
    })
  }
  catch (e: any) {
    toast.error('提交失败', { description: e?.message || '请稍后再试' })
  }
  finally {
    submitting.value = false
  }
}

onMounted(() => loadAssessment())
</script>

<template>
  <FmPageLayout :navbar="false" :tabbar="false">
    <div class="flex flex-1 flex-col bg-gray-50 dark:bg-gray-900 min-h-screen">

      <!-- 加载中 -->
      <FmLoading v-if="loading" type="wave" text="加载问卷中..." />

      <template v-else-if="assessment">
        <!-- 顶部导航 -->
        <div class="bg-white dark:bg-gray-800 px-4 py-3 flex items-center gap-3 border-b border-gray-100 dark:border-gray-700">
          <FmButton variant="ghost" size="icon" @click="router.back()">
            <FmIcon name="i-carbon:arrow-left" class="text-5" />
          </FmButton>
          <div class="flex-1">
            <h2 class="font-bold text-gray-900 dark:text-white text-sm line-clamp-1">
              {{ assessment.title }}
            </h2>
            <p class="text-xs text-gray-500">
              第 {{ currentIndex + 1 }} / {{ totalCount }} 题
            </p>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="h-1.5 bg-gray-200 dark:bg-gray-700">
          <div
            class="h-full bg-gradient-to-r from-violet-500 to-indigo-500 transition-all duration-500 rounded-r-full"
            :style="{ width: `${progress}%` }"
          />
        </div>

        <!-- 题目区域 -->
        <div class="flex-1 p-5 flex flex-col">
          <div v-if="currentQuestion" class="flex-1">
            <!-- 题目编号 + 内容 -->
            <div class="bg-white dark:bg-gray-800 rounded-2xl p-5 shadow-sm mb-5 border border-gray-100 dark:border-gray-700">
              <div class="flex items-center gap-2 mb-3">
                <span class="w-7 h-7 rounded-full bg-violet-500 text-white text-sm font-bold flex items-center justify-center flex-shrink-0">
                  {{ currentIndex + 1 }}
                </span>
                <span class="text-xs text-violet-500 font-medium">
                  {{ isLast ? '最后一题' : `还剩 ${totalCount - currentIndex - 1} 题` }}
                </span>
              </div>
              <p class="text-gray-900 dark:text-white text-base font-medium leading-relaxed">
                {{ currentQuestion.content }}
              </p>
            </div>

            <!-- 选项列表 -->
            <div class="space-y-3">
              <button
                v-for="option in currentQuestion.options"
                :key="option.id"
                class="w-full text-left p-4 rounded-xl border-2 transition-all duration-200 font-medium"
                :class="answers[currentQuestion.id] === option.id
                  ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 shadow-md'
                  : 'border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:border-violet-300 hover:bg-violet-50/50'"
                @click="selectOption(currentQuestion!.id, option.id)"
              >
                <div class="flex items-center gap-3">
                  <div
                    class="w-5 h-5 rounded-full border-2 flex-shrink-0 flex items-center justify-center transition-all"
                    :class="answers[currentQuestion.id] === option.id
                      ? 'border-violet-500 bg-violet-500'
                      : 'border-gray-300 dark:border-gray-500'"
                  >
                    <div v-if="answers[currentQuestion.id] === option.id" class="w-2 h-2 rounded-full bg-white" />
                  </div>
                  <span class="flex-1">{{ option.content }}</span>
                </div>
              </button>
            </div>
          </div>

          <!-- 底部操作按钮 -->
          <div class="flex gap-3 mt-6 pt-4 border-t border-gray-100 dark:border-gray-700">
            <FmButton
              v-if="currentIndex > 0"
              variant="outline"
              class="flex-1 rounded-xl py-3"
              @click="prevQuestion"
            >
              <FmIcon name="i-carbon:arrow-left" class="mr-1" />
              上一题
            </FmButton>

            <FmButton
              v-if="!isLast"
              class="flex-1 rounded-xl py-3 bg-gradient-to-r from-violet-500 to-indigo-600 border-0"
              :disabled="!currentAnswered"
              @click="nextQuestion"
            >
              下一题
              <FmIcon name="i-carbon:arrow-right" class="ml-1" />
            </FmButton>

            <FmButton
              v-else
              class="flex-1 rounded-xl py-3 bg-gradient-to-r from-violet-500 to-indigo-600 border-0"
              :loading="submitting"
              :disabled="!currentAnswered"
              @click="submitAnswers"
            >
              {{ submitting ? '提交中...' : '提交答卷' }}
            </FmButton>
          </div>
        </div>
      </template>
    </div>
  </FmPageLayout>
</template>
