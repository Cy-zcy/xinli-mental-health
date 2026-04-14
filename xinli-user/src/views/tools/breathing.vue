<script setup lang="ts">
import { toast } from 'vue-sonner'
import { recordToolUsage } from '@/api/modules/tools'

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

// 练习是否已完成
const isCompleted = ref(false)

// 获取最优质的中文女声
function getBestVoice(): SpeechSynthesisVoice | null {
  const voices = window.speechSynthesis.getVoices()
  if (!voices?.length) return null

  // 对所有声音评分，分越高质量越好
  let bestVoice: SpeechSynthesisVoice | null = null
  let maxScore = -1

  for (const v of voices) {
    // 必须是中文
    if (!v.lang.toLowerCase().includes('zh')) continue

    let score = 0
    const name = v.name.toLowerCase()

    // 1. 最高优先级：微软的高级/自然在线语音 (Edge 浏览器特有，极度逼真)
    if (name.includes('xiaoxiao') && name.includes('online')) score += 100
    else if (name.includes('xiaoxiao')) score += 90 // 晓晓 (女声中最温柔的)
    else if (name.includes('shaanxi') && name.includes('xiaoni')) score += 80 // 地方亲切女声
    else if (name.includes('natural')) score += 50
    else if (name.includes('online')) score += 40
    
    // 2. 次优先级：特定大厂的高质量女声
    else if (name.includes('tingting')) score += 30 // 苹果的中文女声
    else if (name.includes('google 简体中文')) score += 20 // 谷歌标准女声

    // 3. 通用关键字筛选 (加分项)
    if (name.includes('female') || name.includes('女')) score += 10

    // 4. 减分项：避免拿到粗犷的男声或过于呆板的机读语音
    if (name.includes('yunxi') || name.includes('yunye') || name.includes('male') || name.includes('男')) score -= 100

    if (score > maxScore) {
      maxScore = score
      bestVoice = v
    }
  }

  // 兜底：如果没挑出来高质量的，找系统默认的中文语音
  if (!bestVoice) {
    bestVoice = voices.find(v => v.lang.includes('zh') && v.default) 
             || voices.find(v => v.lang.includes('zh-cn') || v.lang.includes('zh-tw')) 
             || null
  }
  
  return bestVoice
}

// 确保语音库被异步加载 (重点修复某些浏览器第一次点不发声或声音不对的问题)
if (window.speechSynthesis && window.speechSynthesis.onvoiceschanged === null) {
  window.speechSynthesis.onvoiceschanged = () => {
    // 浏览器后台拉取完语音库后触发
  }
}

// TTS 语音播放核心逻辑（优先中文字符女声）
function playVoice(text: string) {
  if (!window.speechSynthesis) return
  
  // 先停止当前正在播的语音，防止重叠
  window.speechSynthesis.cancel()
  
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = 0.8  // 语速稍慢，更舒缓
  utterance.pitch = 1.1 // 音调略高，女声感觉更明显
  utterance.volume = 0.6 // 音量中等，不刺耳
  
  // 智能分配最佳声音
  const bestVoice = getBestVoice()
  if (bestVoice) {
    utterance.voice = bestVoice
  }
  
  window.speechSynthesis.speak(utterance)
}

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
      return '吸入平静和能量…'
    case 'hold':
      return '感受当下的宁静…'
    case 'exhale':
      return '呼出压力和烦恼…'
    case 'pause':
      return '停驻片刻，体会身心…'
    default:
      return '跟随节奏放松'
  }
})

// 获取当前阶段的颜色
const phaseColor = computed(() => {
  switch (currentPhase.value) {
    case 'inhale':
      return 'from-blue-400 to-indigo-500'
    case 'hold':
      return 'from-indigo-400 to-purple-500'
    case 'exhale':
      return 'from-purple-400 to-pink-500'
    case 'pause':
      return 'from-slate-400 to-blue-300'
    default:
      return 'from-blue-400 to-indigo-500'
  }
})

// 多样化的语音文案库
const voicePrompts = {
  inhale: [
    '慢慢吸气，感受气息充满身体……',
    '随节奏吸气，接纳这一刻的宁静……',
    '深深吸气，放松你的双肩……',
    '缓慢吸气，感受能量进入体内……',
    '轻柔地吸气，什么都不要想……'
  ],
  hold: [
    '屏住呼吸，留住这里的平静……',
    '保持屏息，感受身体的静定……',
    '稍微停顿一下，别着急……',
    '停在这里，体会此刻的轻盈……'
  ],
  exhale: [
    '缓缓呼气，把疲惫都吐出去……',
    '慢慢呼气，让大脑跟着放空……',
    '沉郁地呼气，感受压力的释放……',
    '温柔地呼出，一切都会变好的……',
    '随着呼气，让肩膀彻底垂下来……'
  ]
}

// 随机获取一句引导文案
function getRandomPrompt(phase: 'inhale' | 'hold' | 'exhale') {
  const prompts = voicePrompts[phase]
  return prompts[Math.floor(Math.random() * prompts.length)]
}

// 开始呼吸练习
function startBreathing() {
  if (isActive.value) {
    stopBreathing()
    return
  }

  isCompleted.value = false
  isActive.value = true
  currentPhase.value = 'inhale'
  currentCount.value = 0
  totalCycles.value = 0

  const startLines = [
    '准备好了吗？闭上眼睛，我们一起深吸一口气……',
    '深呼吸马上开始，请找个舒服的姿势，慢慢吸气……',
    '把注意力收回到当下，跟随我的指引，吸气……'
  ]
  playVoice(startLines[Math.floor(Math.random() * startLines.length)])
  runBreathingCycle()
}

// 停止呼吸练习
function stopBreathing() {
  isActive.value = false
  if (timer.value) {
    clearTimeout(timer.value)
    timer.value = null
  }
  currentCount.value = 0
  if (!isCompleted.value) {
    playVoice('没关系，随时可以重新开始。')
    toast.info('呼吸练习已停止')
  }
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
      // 只有在屏息时间大于 0 时才播报屏息文案，提升自然度
      if (selectedPattern.value.hold > 0) {
        playVoice(getRandomPrompt('hold'))
      }
      break
    case 'hold':
      currentPhase.value = 'exhale'
      playVoice(getRandomPrompt('exhale'))
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
      } else {
        playVoice(getRandomPrompt('inhale'))
      }
      break
  }

  // 手机端提供轻微震动反馈
  if (navigator.vibrate) {
    navigator.vibrate(10)
  }

  runBreathingCycle()
}

// 完成练习
async function completeSession() {
  isActive.value = false
  isCompleted.value = true // 展示庆祝屏
  if (timer.value) {
    clearTimeout(timer.value)
    timer.value = null
  }
  
  playVoice('太棒了，你已经完成了本次深呼吸放松，去感受现在的平静吧。')

  // 向后端记录本次练习
  const totalSecs = totalCycles.value
    * (selectedPattern.value.inhale + selectedPattern.value.hold + selectedPattern.value.exhale + selectedPattern.value.pause)
  try {
    await recordToolUsage({
      toolType: 'breathing',
      durationSeconds: totalSecs,
      cycles: totalCycles.value,
      completed: true,
      pattern: selectedPattern.value.name,
    })
  }
  catch {
    // 记录失败不影响用户体验，静默处理
  }
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
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/tools')
  }
}

function goHome() {
  router.push('/')
}

// 页面离开时清理定时器和语音
onBeforeUnmount(() => {
  if (timer.value) {
    clearTimeout(timer.value)
  }
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
})
</script>

<template>
  <FmPageLayout title="深呼吸沉浸放松" :navbar="{ back: true }" @back="goBack">
    <template #navbar-right>
      <FmIcon name="i-carbon:home" class="text-xl text-gray-600 dark:text-gray-300 active:opacity-60 mr-2" @click="goHome" />
    </template>
    <div class="flex flex-1 flex-col bg-slate-900 transition-colors duration-1000 overflow-hidden relative">
      
      <!-- 庆祝页面 (isCompleted为true时展示) -->
      <div v-if="isCompleted" class="absolute inset-0 z-20 flex flex-col items-center justify-center p-8 bg-slate-900 animate-fade-in text-center">
         <div class="w-24 h-24 mb-6 rounded-full bg-green-500/20 flex items-center justify-center shadow-[0_0_30px_rgba(34,197,94,0.3)]">
             <div class="i-carbon-checkmark-outline text-5xl text-green-400"></div>
         </div>
         <h2 class="text-3xl font-medium text-white mb-3">做得好！</h2>
         <p class="text-lg text-slate-300 mb-2">你刚刚沉浸了 {{ totalCycles }} 个呼吸轮回。</p>
         <p class="text-slate-400 mb-10 text-sm">希望这一刻的宁静，能陪伴你更久。</p>
         <FmButton size="lg" class="!px-10 !rounded-full shadow-lg" @click="goBack">结束放松，返回上一页</FmButton>
      </div>

      <!-- 呼吸可视化区域 (未完成时展示) -->
      <div v-else class="flex-1 flex flex-col items-center justify-center p-8 relative z-10 transition-all duration-700"
           :class="isActive ? '' : 'pb-[320px]'">
        
        <!-- 呼吸圆圈与多层波纹 -->
        <div class="relative w-56 h-56 md:w-64 md:h-64 flex items-center justify-center mt-2 mb-8 select-none pointer-events-none">
           <!-- 外围扩散波纹 1 (吸气时向外扩散并淡出) -->
           <div class="absolute inset-0 rounded-full bg-white/10"
                :class="{ 'animate-ping': isActive && currentPhase === 'inhale', 'opacity-0': !isActive || currentPhase === 'exhale' }"
                style="animation-duration: 3s;">
           </div>
           
           <!-- 外围柔和扩展层 2 (跟随主球缩放，但比主球稍大) -->
           <div class="absolute inset-[-20px] rounded-full bg-white/5 backdrop-blur-[2px]"
                :style="{ 
                  transform: currentPhase === 'inhale' ? 'scale(1.3)' : 
                             currentPhase === 'exhale' ? 'scale(0.85)' : 'scale(1)', 
                  opacity: isActive ? 1 : 0, 
                  transition: `transform ${currentDuration}s ease-in-out, opacity 1s` 
                }">
           </div>
           
           <!-- 核心主呼吸球 -->
           <div 
              class="relative w-full h-full rounded-full flex flex-col items-center justify-center shadow-[0_0_50px_rgba(255,255,255,0.1)] transition-colors duration-1000 bg-gradient-to-tr"
              :class="phaseColor"
              :style="{
                transform: currentPhase === 'inhale' ? 'scale(1.15)' : 
                           currentPhase === 'exhale' ? 'scale(0.8)' : 'scale(1)',
                opacity: isActive ? 1 : 0.8,
                transition: `transform ${currentDuration}s ease-in-out`
              }"
            >
              <div class="text-white text-center z-10 px-4">
                <div class="text-lg font-medium tracking-wider mb-2 drop-shadow-md transition-opacity duration-300">{{ isActive ? phaseText : '点击下方开始' }}</div>
                <div class="text-4xl font-light font-sans tracking-widest tabular-nums mt-1" v-if="isActive">
                   {{ currentDuration - currentCount }}
                </div>
              </div>
            </div>
        </div>

        <!-- 练习信息，只有没开始才显示，开始了就隐藏，追求绝对沉浸 -->
        <div class="transition-all duration-1000 w-full"
             :class="isActive ? 'opacity-0 translate-y-4 pointer-events-none h-0 overflow-hidden' : 'opacity-100 translate-y-0'">
            <h2 class="text-2xl font-light text-white mb-2 text-center">{{ selectedPattern.name }}</h2>
            <p class="text-slate-400 mb-6 text-center text-sm px-4">{{ selectedPattern.description }}</p>
            
            <!-- 开始按钮 -->
            <div class="flex justify-center">
              <FmButton 
                class="min-w-[180px] !rounded-full !py-6 shadow-[0_0_20px_rgba(59,130,246,0.3)] !text-lg bg-blue-600 hover:bg-blue-500 border-none"
                size="lg" 
                @click="startBreathing"
              >
                开启沉浸陪伴
              </FmButton>
            </div>
        </div>

        <!-- 进行中的退出按钮 -->
        <div class="absolute bottom-10 left-1/2 -translate-x-1/2 transition-opacity duration-1000 z-10"
             :class="isActive ? 'opacity-100' : 'opacity-0 pointer-events-none'">
             <button @click="stopBreathing" class="px-6 py-2 rounded-full border border-white/20 text-white/50 hover:text-white hover:bg-white/10 transition text-sm tracking-widest backdrop-blur-md">
                提前结束
             </button>
        </div>
      </div>

      <!-- 设置区域 (页面底部的抽屉，仅未开始时展示，开始后沉底隐藏) -->
      <div class="absolute bottom-0 left-0 right-0 p-6 bg-slate-800/90 backdrop-blur-xl rounded-t-3xl border-t border-slate-700 transition-all duration-700 z-20 shadow-[0_-10px_40px_rgba(0,0,0,0.3)]"
           :class="isActive ? 'translate-y-full opacity-0' : 'translate-y-0 opacity-100'">
        
        <!-- 目标循环数设置 -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-slate-300 mb-3 flex justify-between">
            <span>希望进行几个循环？</span>
            <span class="text-blue-400 font-bold">{{ targetCycles }} 次</span>
          </label>
          <input
            v-model="targetCycles"
            type="range"
            min="3"
            max="20"
            :disabled="isActive"
            class="w-full h-2 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-blue-500"
          />
          <div class="flex justify-between text-[10px] text-slate-500 mt-2">
            <span>短憩(3)</span>
            <span>深度(20)</span>
          </div>
        </div>

        <!-- 呼吸模式选择 -->
        <div>
          <label class="block text-sm font-medium text-slate-300 mb-3">
            选择呼吸模式
          </label>
          <div class="flex gap-3 overflow-x-auto pb-2 snap-x hide-scrollbar">
            <button
              v-for="pattern in breathingPatterns"
              :key="pattern.id"
              class="snap-start shrink-0 w-[240px] p-4 rounded-2xl border text-left transition-all duration-300"
              :class="selectedPattern.id === pattern.id ? 'border-blue-500 bg-blue-900/40 shadow-inner' : 'border-slate-700 bg-slate-800/50 hover:bg-slate-700/80'"
              :disabled="isActive"
              @click="selectPattern(pattern)"
            >
              <div class="font-medium text-slate-100">{{ pattern.name }}</div>
              <div class="text-xs text-slate-400 mt-1 line-clamp-2 h-8">{{ pattern.description }}</div>
              <div class="flex gap-1.5 mt-3 flex-wrap">
                <span
                  v-for="benefit in pattern.benefits"
                  :key="benefit"
                  class="px-2 py-0.5 bg-blue-900/30 text-blue-300 text-[10px] rounded-full border border-blue-800/50"
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

<style scoped>
.hide-scrollbar::-webkit-scrollbar {
  display: none;
}
.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
