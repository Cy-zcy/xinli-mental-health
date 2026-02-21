import { authApi } from '@/api/modules'
import type { LoginRequest, UserInfo } from '@/api/types'
import router from '@/router'

export const useUserStore = defineStore(
  // 唯一ID
  'user',
  () => {
    const phone = ref(localStorage.phone ?? '')
    const token = ref(localStorage.token ?? '')
    const userInfo = ref<UserInfo | null>(
      localStorage.userInfo ? JSON.parse(localStorage.userInfo) : null
    )

    const isLogin = computed(() => {
      return !!token.value
    })

    const avatar = computed(() => {
      return userInfo.value?.avatar || ''
    })

    const nickname = computed(() => {
      return userInfo.value?.nickname || ''
    })

    function login(data: LoginRequest) {
      return new Promise((resolve, reject) => {
        authApi.login(data).then((res) => {
          const { token: userToken, user } = res
          localStorage.setItem('phone', user.phone)
          localStorage.setItem('token', userToken)
          localStorage.setItem('userInfo', JSON.stringify(user))
          phone.value = user.phone
          token.value = userToken
          userInfo.value = user
          resolve(res)
        }).catch((error) => {
          reject(error)
        })
      })
    }
    function logout() {
      // 清除用户信息
      localStorage.removeItem('phone')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      phone.value = ''
      token.value = ''
      userInfo.value = null
      router.push('/login')
    }

    // 获取用户信息
    async function getUserInfo() {
      try {
        const res = await authApi.getUserInfo()
        userInfo.value = res
        localStorage.setItem('userInfo', JSON.stringify(res))
        return res
      } catch (error) {
        console.error('获取用户信息失败:', error)
        throw error
      }
    }

    // 更新用户信息
    async function updateUserInfo(data: { nickname?: string; avatar?: string }) {
      try {
        const res = await authApi.updateUserInfo(data)
        userInfo.value = res
        localStorage.setItem('userInfo', JSON.stringify(res))
        return res
      } catch (error) {
        console.error('更新用户信息失败:', error)
        throw error
      }
    }

    return {
      phone,
      token,
      userInfo,
      avatar,
      nickname,
      isLogin,
      login,
      logout,
      getUserInfo,
      updateUserInfo,
    }
  },
)
