<script setup lang="ts">
import { toast } from 'vue-sonner'

definePage({
  name: 'login',
  meta: {
    title: '登录 - 心理健康治愈平台',
  },
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const redirect = ref(route.query.redirect?.toString() ?? '/')
const loading = ref(false)
const isRegisterMode = ref(false)

// 简单的表单数据绑定
const loginData = reactive({
  phone: '',
  password: '',
})

// 注册表单数据
const registerData = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

async function onLogin() {
  loading.value = true
  try {
    await userStore.login({
      phone: loginData.phone,
      password: loginData.password,
    })
    toast.success('登录成功')
    router.replace(redirect.value)
  }
  catch (error: any) {
    toast.error('登录失败', {
      description: error.msg || error.message || '请检查手机号和密码',
    })
  }
  finally {
    loading.value = false
  }
}

async function onRegister() {
  if (registerData.password !== registerData.confirmPassword) {
    toast.error('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    // 先调用注册API，注册成功后自动登录
    const { authApi } = await import('@/api/modules')
    await authApi.register({
      phone: registerData.phone,
      password: registerData.password,
      nickname: registerData.nickname,
    })

    // 注册成功后自动登录
    await userStore.login({
      phone: registerData.phone,
      password: registerData.password,
    })

    toast.success('注册成功')
    router.replace(redirect.value)
  }
  catch (error: any) {
    toast.error('注册失败', {
      description: error.message || '请检查输入信息',
    })
  }
  finally {
    loading.value = false
  }
}

function switchMode() {
  isRegisterMode.value = !isRegisterMode.value
  loading.value = false
}
</script>

  <FmPageLayout :navbar="false" copyright>
    <!-- 极简高级光晕背景 -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none z-0 bg-slate-50 dark:bg-slate-950">
      <div class="absolute -top-[20%] -left-[10%] w-[70%] h-[70%] rounded-full bg-blue-400/20 blur-[120px] mix-blend-multiply dark:bg-blue-900/30 dark:mix-blend-screen animate-pulse-slow"></div>
      <div class="absolute top-[40%] -right-[20%] w-[60%] h-[60%] rounded-full bg-purple-400/20 blur-[120px] mix-blend-multiply dark:bg-purple-900/30 dark:mix-blend-screen animate-pulse-slow" style="animation-delay: 2s"></div>
    </div>

    <div class="mx-6 flex flex-1 flex-col justify-center gap-10 relative z-10">
      <!-- Logo和标题 -->
      <div class="text-center animate-fade-in-down">
        <div class="mx-auto w-24 h-24 mb-6 rounded-3xl bg-white/80 dark:bg-slate-800/80 backdrop-blur-xl shadow-xl shadow-blue-500/10 flex items-center justify-center border border-white dark:border-slate-700/50">
          <img src="@/assets/images/logo.png" class="h-16 w-16 object-contain drop-shadow-sm">
        </div>
        <h1 class="text-3xl font-black bg-gradient-to-r from-slate-800 to-slate-600 dark:from-slate-100 dark:to-slate-300 bg-clip-text text-transparent mb-3 tracking-tight">心理健康治愈平台</h1>
        <p class="text-slate-500 dark:text-slate-400 text-sm font-medium tracking-wide">{{ isRegisterMode ? '创建账号，开启您的内心之旅' : '欢迎回来，继续您的治愈之旅' }}</p>
      </div>

      <!-- 登录表单 -->
      <form v-if="!isRegisterMode" @submit.prevent="onLogin" class="animate-fade-in-up">
        <div class="bg-white/70 dark:bg-slate-900/70 backdrop-blur-2xl rounded-3xl border border-white/60 dark:border-slate-700/50 shadow-2xl shadow-slate-200/40 dark:shadow-none p-2 space-y-2 relative overflow-hidden">
          <div class="absolute top-0 right-0 w-32 h-32 bg-indigo-500/10 rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none"></div>
          <div class="bg-white/50 dark:bg-slate-800/50 rounded-2xl p-1 transition-colors focus-within:bg-white dark:focus-within:bg-slate-800 focus-within:shadow-sm">
            <FmInput
              v-model="loginData.phone"
              type="tel"
              placeholder="手机号"
              class="w-full border-none focus-visible:ring-0 focus-visible:ring-offset-0 bg-transparent h-12 text-[15px]"
            />
          </div>
          <div class="bg-white/50 dark:bg-slate-800/50 rounded-2xl p-1 transition-colors focus-within:bg-white dark:focus-within:bg-slate-800 focus-within:shadow-sm">
            <FmInput
              v-model="loginData.password"
              type="password"
              placeholder="密码"
              class="w-full border-none focus-visible:ring-0 focus-visible:ring-offset-0 bg-transparent h-12 text-[15px]"
            />
          </div>
        </div>
        
        <div class="mt-8 px-2">
          <FmButton :loading class="w-full h-14 rounded-2xl text-[16px] font-bold shadow-lg shadow-indigo-500/25 bg-gradient-to-r from-indigo-500 to-purple-500 hover:from-indigo-600 hover:to-purple-600 border-0 transition-transform active:scale-[0.98]" type="submit">
            登 录
          </FmButton>
          <div class="mt-6 text-center">
            <FmButton variant="ghost" size="sm" class="text-slate-500 hover:text-indigo-600 dark:text-slate-400 dark:hover:text-indigo-400 tracking-wide font-medium rounded-full px-6" @click="switchMode">
              还没有账号？<span class="text-indigo-500 font-bold ml-1">立即注册</span>
            </FmButton>
          </div>
        </div>
      </form>

      <!-- 注册表单 -->
      <form v-else @submit.prevent="onRegister" class="animate-fade-in-up">
        <div class="bg-white/70 dark:bg-slate-900/70 backdrop-blur-2xl rounded-3xl border border-white/60 dark:border-slate-700/50 shadow-2xl shadow-slate-200/40 dark:shadow-none p-2 space-y-2 relative overflow-hidden">
          <div class="absolute top-0 right-0 w-32 h-32 bg-indigo-500/10 rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none"></div>
          <div class="bg-white/50 dark:bg-slate-800/50 rounded-2xl p-1 transition-colors focus-within:bg-white dark:focus-within:bg-slate-800 focus-within:shadow-sm">
            <FmInput
              v-model="registerData.phone"
              type="tel"
              placeholder="手机号"
              class="w-full border-none focus-visible:ring-0 focus-visible:ring-offset-0 bg-transparent h-12 text-[15px]"
            />
          </div>
          <div class="bg-white/50 dark:bg-slate-800/50 rounded-2xl p-1 transition-colors focus-within:bg-white dark:focus-within:bg-slate-800 focus-within:shadow-sm">
            <FmInput
              v-model="registerData.nickname"
              type="text"
              placeholder="昵称"
              class="w-full border-none focus-visible:ring-0 focus-visible:ring-offset-0 bg-transparent h-12 text-[15px]"
            />
          </div>
          <div class="bg-white/50 dark:bg-slate-800/50 rounded-2xl p-1 transition-colors focus-within:bg-white dark:focus-within:bg-slate-800 focus-within:shadow-sm">
            <FmInput
              v-model="registerData.password"
              type="password"
              placeholder="密码（至少6位）"
              class="w-full border-none focus-visible:ring-0 focus-visible:ring-offset-0 bg-transparent h-12 text-[15px]"
            />
          </div>
          <div class="bg-white/50 dark:bg-slate-800/50 rounded-2xl p-1 transition-colors focus-within:bg-white dark:focus-within:bg-slate-800 focus-within:shadow-sm">
            <FmInput
              v-model="registerData.confirmPassword"
              type="password"
              placeholder="确认密码"
              class="w-full border-none focus-visible:ring-0 focus-visible:ring-offset-0 bg-transparent h-12 text-[15px]"
            />
          </div>
        </div>
        <div class="mt-8 px-2">
          <FmButton :loading class="w-full h-14 rounded-2xl text-[16px] font-bold shadow-lg shadow-indigo-500/25 bg-gradient-to-r from-indigo-500 to-purple-500 hover:from-indigo-600 hover:to-purple-600 border-0 transition-transform active:scale-[0.98]" type="submit">
            注 册
          </FmButton>
          <div class="mt-6 text-center">
            <FmButton variant="ghost" size="sm" class="text-slate-500 hover:text-indigo-600 dark:text-slate-400 dark:hover:text-indigo-400 tracking-wide font-medium rounded-full px-6" @click="switchMode">
              已有账号？<span class="text-indigo-500 font-bold ml-1">立即登录</span>
            </FmButton>
          </div>
        </div>
      </form>
    </div>
  </FmPageLayout>
</template>

<style scoped>
.animate-pulse-slow {
  animation: pulse 8s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: .7;
    transform: scale(1.05);
  }
}
</style>
