<script setup lang="ts">
import { toast } from 'vue-sonner'
import { recordToolUsage } from '@/api/modules/tools'

definePage({
  meta: {
    title: '冥想引导',
    auth: true,
  },
})

const router = useRouter()

// 冥想状态
const isActive = ref(false)
const isPaused = ref(false)
const currentTime = ref(0)
const selectedDuration = ref(10) // 默认10分钟
const currentStep = ref(0)

// 冥想引导步骤
const meditationSteps = [
  {
    time: 0,
    title: '准备阶段',
    instruction: '找一个舒适的坐姿，闭上眼睛，让身体放松下来。',
    duration: 60,
  },
  {
    time: 60,
    title: '专注呼吸',
    instruction: '将注意力集中在呼吸上，感受空气进入和离开身体的感觉。',
    duration: 180,
  },
  {
    time: 240,
    title: '身体扫描',
    instruction: '从头顶开始，慢慢感受身体的每一个部位，释放紧张感。',
    duration: 240,
  },
  {
    time: 480,
    title: '正念观察',
    instruction: '观察内心的想法和感受，不做判断，只是静静地观察。',
    duration: 180,
  },
  {
    time: 660,
    title: '结束阶段',
    instruction: '慢慢将注意力带回到当下，轻轻活动手指和脚趾，准备睁开眼睛。',
    duration: 60,
  },
]

// 冥想主题
const meditationThemes = [
  {
    id: 'mindfulness',
    name: '正念冥想',
    description: '专注当下，观察内心',
    duration: [5, 10, 15, 20],
    color: 'from-purple-400 to-indigo-500',
    icon: 'i-ic:outline-self-improvement',
  },
  {
    id: 'relaxation',
    name: '放松冥想',
    description: '深度放松，缓解压力',
    duration: [10, 15, 20, 30],
    color: 'from-blue-400 to-cyan-500',
    icon: 'i-ic:outline-spa',
  },
  {
    id: 'sleep',
    name: '睡眠冥想',
    description: '帮助入眠，改善睡眠',
    duration: [15, 20, 30, 45],
    color: 'from-indigo-400 to-purple-500',
    icon: 'i-ic:outline-bedtime',
  },
]

const selectedTheme = ref(meditationThemes[0])
const timer = ref<NodeJS.Timeout | null>(null)

// 计算进度
const progress = computed(() => {
  const totalSeconds = selectedDuration.value * 60
  return (currentTime.value / totalSeconds) * 100
})

// 格式化时间显示
const formattedTime = computed(() => {
  const totalSeconds = selectedDuration.value * 60
  const remaining = totalSeconds - currentTime.value
  const minutes = Math.floor(remaining / 60)
  const seconds = remaining % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

// 获取当前引导步骤
const currentInstruction = computed(() => {
  const step = meditationSteps.find((step, index) => {
    const nextStep = meditationSteps[index + 1]
    return currentTime.value >= step.time && (!nextStep || currentTime.value < nextStep.time)
  })
  return step || meditationSteps[0]
})

// 开始冥想
function startMeditation() {
  if (isActive.value) {
    if (isPaused.value) {
      resumeMeditation()
    } else {
      pauseMeditation()
    }
    return
  }

  isActive.value = true
  isPaused.value = false
  currentTime.value = 0
  currentStep.value = 0

  runTimer()
  toast.success('开始冥想练习')
}

// 暂停冥想
function pauseMeditation() {
  isPaused.value = true
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
  toast.info('冥想已暂停')
}

// 恢复冥想
function resumeMeditation() {
  isPaused.value = false
  runTimer()
  toast.success('继续冥想')
}

// 停止冥想
function stopMeditation() {
  isActive.value = false
  isPaused.value = false
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
  currentTime.value = 0
  toast.info('冥想练习已停止')
}

// 运行计时器
function runTimer() {
  timer.value = setInterval(() => {
    if (!isPaused.value) {
      currentTime.value++
      
      // 检查是否完成
      if (currentTime.value >= selectedDuration.value * 60) {
        completeMeditation()
      }
    }
  }, 1000)
}

// 完成冥想
async function completeMeditation() {
  isActive.value = false
  isPaused.value = false
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }

  toast.success('恭喜完成冥想练习！', {
    description: `完成了 ${selectedDuration.value} 分钟的冥想`,
  })

  // 向后端记录本次冥想
  try {
    await recordToolUsage({
      toolType: 'meditation',
      durationSeconds: selectedDuration.value * 60,
      completed: true,
      pattern: selectedTheme.value.name,
    })
  }
  catch {
    // 静默处理，不影响用户体验
  }
}

// 选择主题
function selectTheme(theme: typeof meditationThemes[0]) {
  if (isActive.value) {
    toast.warning('请先停止当前练习')
    return
  }
  selectedTheme.value = theme
  // 设置默认时长为该主题的第二个选项
  selectedDuration.value = theme.duration[1]
  toast.success(`已切换到${theme.name}`)
}

// 设置时长
function setDuration(duration: number) {
  if (isActive.value) {
    toast.warning('请先停止当前练习')
    return
  }
  selectedDuration.value = duration
}

function goBack() {
  if (isActive.value) {
    stopMeditation()
  }
  router.back()
}

// 页面离开时清理定时器
onBeforeUnmount(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})
</script>

<template>
  <FmPageLayout title="冥想引导" :navbar="{ back: true }" @back="goBack">
    <div class="flex flex-1 flex-col bg-gradient-to-br from-purple-50 to-indigo-50 dark:from-gray-900 dark:to-gray-800">
      <!-- 冥想可视化区域 -->
      <div class="flex-1 flex items-center justify-center p-8">
        <div class="text-center max-w-md">
          <!-- 冥想圆圈 -->
          <div class="relative mb-8">
            <div 
              :class="`w-64 h-64 rounded-full bg-gradient-to-r ${selectedTheme.color} flex items-center justify-center transition-all duration-2000 shadow-2xl`"
              :style="{
                transform: isActive && !isPaused ? 'scale(1.1)' : 'scale(1)',
                opacity: isActive ? 0.9 : 0.6
              }"
            >
              <div class="text-white text-center">
                <div class="text-3xl font-bold mb-2">{{ formattedTime }}</div>
                <div class="text-sm opacity-80">{{ selectedTheme.name }}</div>
              </div>
            </div>
            
            <!-- 进度环 -->
            <div class="absolute inset-0 w-64 h-64">
              <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                <circle
                  cx="50"
                  cy="50"
                  r="45"
                  fill="none"
                  stroke="rgba(255,255,255,0.2)"
                  stroke-width="2"
                />
                <circle
                  cx="50"
                  cy="50"
                  r="45"
                  fill="none"
                  stroke="rgba(255,255,255,0.8)"
                  stroke-width="3"
                  stroke-linecap="round"
                  :stroke-dasharray="`${progress * 2.83} 283`"
                  class="transition-all duration-1000"
                />
              </svg>
            </div>
          </div>

          <!-- 当前引导 -->
          <div class="mb-6">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-2">
              {{ currentInstruction.title }}
            </h3>
            <p class="text-gray-600 dark:text-gray-300 leading-relaxed">
              {{ currentInstruction.instruction }}
            </p>
          </div>

          <!-- 控制按钮 -->
          <div class="flex justify-center gap-4">
            <FmButton
              v-if="!isActive"
              size="lg"
              @click="startMeditation"
            >
              开始冥想
            </FmButton>
            <template v-else>
              <FmButton
                variant="outline"
                size="lg"
                @click="startMeditation"
              >
                {{ isPaused ? '继续' : '暂停' }}
              </FmButton>
              <FmButton
                variant="outline"
                size="lg"
                @click="stopMeditation"
              >
                停止
              </FmButton>
            </template>
          </div>
        </div>
      </div>

      <!-- 设置区域 -->
      <div class="p-4 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border-t">
        <!-- 冥想主题选择 -->
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            冥想主题
          </label>
          <div class="grid grid-cols-1 gap-2">
            <button
              v-for="theme in meditationThemes"
              :key="theme.id"
              :class="`p-3 rounded-lg border text-left transition-colors ${
                selectedTheme.id === theme.id
                  ? 'border-purple-500 bg-purple-50 dark:bg-purple-900/20'
                  : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800'
              }`"
              :disabled="isActive"
              @click="selectTheme(theme)"
            >
              <div class="flex items-center gap-3">
                <div :class="`w-10 h-10 rounded-full bg-gradient-to-r ${theme.color} flex items-center justify-center`">
                  <FmIcon :name="theme.icon" class="text-white" />
                </div>
                <div>
                  <div class="font-medium text-gray-800 dark:text-white">{{ theme.name }}</div>
                  <div class="text-sm text-gray-600 dark:text-gray-400">{{ theme.description }}</div>
                </div>
              </div>
            </button>
          </div>
        </div>

        <!-- 时长选择 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            冥想时长
          </label>
          <div class="grid grid-cols-4 gap-2">
            <button
              v-for="duration in selectedTheme.duration"
              :key="duration"
              :class="`p-2 rounded-lg border text-center transition-colors ${
                selectedDuration === duration
                  ? 'border-purple-500 bg-purple-50 dark:bg-purple-900/20 text-purple-600 dark:text-purple-400'
                  : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800'
              }`"
              :disabled="isActive"
              @click="setDuration(duration)"
            >
              {{ duration }}分钟
            </button>
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
