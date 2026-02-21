<script setup lang="ts">
import { toast } from 'vue-sonner'

definePage({
  meta: {
    title: '呼吸练习',
    auth: true,
  },
})

const router = useRouter()

// 呼吸练习状态
const isActive = ref(false)
const currentPhase = ref<'inhale' | 'hold' | 'exhale' | 'pause'>('inhale')
const currentCount = ref(0)
const totalCycles = ref(0)
const targetCycles = ref(5)

// 呼吸模式配置
const breathingPatterns = [
  {
    id: '4-7-8',
    name: '4-7-8 呼吸法',
    description: '吸气4秒，屏息7秒，呼气8秒',
    inhale: 4,
    hold: 7,
    exhale: 8,
    pause: 2,
    benefits: ['缓解焦虑', '改善睡眠', '降低压力'],
  },
  {
    id: '4-4-4',
    name: '方形呼吸法',
    description: '吸气4秒，屏息4秒，呼气4秒，暂停4秒',
    inhale: 4,
    hold: 4,
    exhale: 4,
    pause: 4,
    benefits: ['提高专注力', '平衡情绪', '增强控制感'],
  },
  {
    id: '6-2-6',
    name: '深度呼吸法',
    description: '吸气6秒，屏息2秒，呼气6秒',
    inhale: 6,
    hold: 2,
    exhale: 6,
    pause: 2,
    benefits: ['深度放松', '缓解疲劳', '提升能量'],
  },
]

const selectedPattern = ref(breathingPatterns[0])
const timer = ref<NodeJS.Timeout | null>(null)

// 计算当前阶段的持续时间
const currentDuration = computed(() => {
  switch (currentPhase.value) {
    case 'inhale':
      return selectedPattern.value.inhale
    case 'hold':
      return selectedPattern.value.hold
    case 'exhale':
      return selectedPattern.value.exhale
    case 'pause':
      return selectedPattern.value.pause
    default:
      return 4
  }
})

// 计算进度百分比
const progress = computed(() => {
  return (currentCount.value / currentDuration.value) * 100
})

// 获取当前阶段的指导文字
const phaseText = computed(() => {
  switch (currentPhase.value) {
    case 'inhale':
      return '慢慢吸气'
    case 'hold':
      return '屏住呼吸'
    case 'exhale':
      return '缓缓呼气'
    case 'pause':
      return '自然暂停'
    default:
      return '准备开始'
  }
})

// 获取当前阶段的颜色
const phaseColor = computed(() => {
  switch (currentPhase.value) {
    case 'inhale':
      return 'from-blue-400 to-cyan-500'
    case 'hold':
      return 'from-purple-400 to-indigo-500'
    case 'exhale':
      return 'from-green-400 to-teal-500'
    case 'pause':
      return 'from-gray-400 to-gray-500'
    default:
      return 'from-blue-400 to-cyan-500'
  }
})

// 开始呼吸练习
function startBreathing() {
  if (isActive.value) {
    stopBreathing()
    return
  }

  isActive.value = true
  currentPhase.value = 'inhale'
  currentCount.value = 0
  totalCycles.value = 0

  runBreathingCycle()
  toast.success('开始呼吸练习')
}

// 停止呼吸练习
function stopBreathing() {
  isActive.value = false
  if (timer.value) {
    clearTimeout(timer.value)
    timer.value = null
  }
  currentCount.value = 0
  toast.info('呼吸练习已停止')
}

// 运行呼吸循环
function runBreathingCycle() {
  if (!isActive.value) return

  timer.value = setTimeout(() => {
    currentCount.value++

    if (currentCount.value >= currentDuration.value) {
      // 切换到下一个阶段
      switchToNextPhase()
    } else {
      // 继续当前阶段
      runBreathingCycle()
    }
  }, 1000)
}

// 切换到下一个阶段
function switchToNextPhase() {
  currentCount.value = 0

  switch (currentPhase.value) {
    case 'inhale':
      currentPhase.value = 'hold'
      break
    case 'hold':
      currentPhase.value = 'exhale'
      break
    case 'exhale':
      currentPhase.value = 'pause'
      break
    case 'pause':
      currentPhase.value = 'inhale'
      totalCycles.value++
      
      // 检查是否完成目标循环数
      if (totalCycles.value >= targetCycles.value) {
        completeSession()
        return
      }
      break
  }

  runBreathingCycle()
}

// 完成练习
function completeSession() {
  isActive.value = false
  if (timer.value) {
    clearTimeout(timer.value)
    timer.value = null
  }

  toast.success('恭喜完成呼吸练习！', {
    description: `完成了 ${totalCycles.value} 个呼吸循环`,
  })

  // 这里可以记录练习数据
  // recordBreathingSession()
}

// 选择呼吸模式
function selectPattern(pattern: typeof breathingPatterns[0]) {
  if (isActive.value) {
    toast.warning('请先停止当前练习')
    return
  }
  selectedPattern.value = pattern
  toast.success(`已切换到${pattern.name}`)
}

function goBack() {
  if (isActive.value) {
    stopBreathing()
  }
  router.back()
}

// 页面离开时清理定时器
onBeforeUnmount(() => {
  if (timer.value) {
    clearTimeout(timer.value)
  }
})
</script>

<template>
  <FmPageLayout title="呼吸练习" :navbar="{ back: true }" @back="goBack">
    <div class="flex flex-1 flex-col bg-gradient-to-br from-blue-50 to-cyan-50 dark:from-gray-900 dark:to-gray-800">
      <!-- 呼吸可视化区域 -->
      <div class="flex-1 flex items-center justify-center p-8">
        <div class="text-center">
          <!-- 呼吸圆圈 -->
          <div class="relative mb-8">
            <div 
              :class="`w-64 h-64 rounded-full bg-gradient-to-r ${phaseColor} flex items-center justify-center transition-all duration-1000 shadow-2xl`"
              :style="{
                transform: currentPhase === 'inhale' ? 'scale(1.2)' : 
                          currentPhase === 'exhale' ? 'scale(0.8)' : 'scale(1)',
                opacity: isActive ? 0.9 : 0.6
              }"
            >
              <div class="text-white text-center">
                <div class="text-2xl font-bold mb-2">{{ phaseText }}</div>
                <div class="text-lg">{{ currentCount }}/{{ currentDuration }}</div>
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

          <!-- 练习信息 -->
          <div class="mb-6">
            <h2 class="text-xl font-bold text-gray-800 dark:text-white mb-2">
              {{ selectedPattern.name }}
            </h2>
            <p class="text-gray-600 dark:text-gray-300 mb-4">
              {{ selectedPattern.description }}
            </p>
            <div class="text-lg font-semibold text-blue-600 dark:text-blue-400">
              循环进度: {{ totalCycles }}/{{ targetCycles }}
            </div>
          </div>

          <!-- 控制按钮 -->
          <div class="flex justify-center gap-4">
            <FmButton
              :variant="isActive ? 'outline' : 'default'"
              size="lg"
              @click="startBreathing"
            >
              {{ isActive ? '停止练习' : '开始练习' }}
            </FmButton>
          </div>
        </div>
      </div>

      <!-- 设置区域 -->
      <div class="p-4 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm border-t">
        <!-- 目标循环数设置 -->
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            目标循环数: {{ targetCycles }}
          </label>
          <input
            v-model="targetCycles"
            type="range"
            min="3"
            max="20"
            :disabled="isActive"
            class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer dark:bg-gray-700"
          />
          <div class="flex justify-between text-xs text-gray-500 mt-1">
            <span>3</span>
            <span>20</span>
          </div>
        </div>

        <!-- 呼吸模式选择 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            呼吸模式
          </label>
          <div class="grid grid-cols-1 gap-2">
            <button
              v-for="pattern in breathingPatterns"
              :key="pattern.id"
              :class="`p-3 rounded-lg border text-left transition-colors ${
                selectedPattern.id === pattern.id
                  ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                  : 'border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800'
              }`"
              :disabled="isActive"
              @click="selectPattern(pattern)"
            >
              <div class="font-medium text-gray-800 dark:text-white">{{ pattern.name }}</div>
              <div class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ pattern.description }}</div>
              <div class="flex gap-1 mt-2">
                <span
                  v-for="benefit in pattern.benefits"
                  :key="benefit"
                  class="px-2 py-1 bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 text-xs rounded-full"
                >
                  {{ benefit }}
                </span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
