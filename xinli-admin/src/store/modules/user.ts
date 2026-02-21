import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { LanguageEnum } from '@/enums/appEnum'
import { router } from '@/router'
import { useSettingStore } from './setting'
import { useWorktabStore } from './worktab'
import { AppRouteRecord } from '@/types/router'
import { setPageTitle } from '@/router/utils/utils'
import { resetRouterState } from '@/router/guards/beforeEach'
import { RoutesAlias } from '@/router/routesAlias'
import { useMenuStore } from './menu'
import { AuthService, type AdminInfo, type LoginParams } from '@/api/authApi'
import { ElMessage } from 'element-plus'

/**
 * 用户状态管理
 * 管理用户登录状态、个人信息、语言设置、搜索历史、锁屏状态等
 */
export const useUserStore = defineStore(
  'userStore',
  () => {
    // 语言设置
    const language = ref(LanguageEnum.ZH)
    // 登录状态
    const isLogin = ref(false)
    // 锁屏状态
    const isLock = ref(false)
    // 锁屏密码
    const lockPassword = ref('')
    // 管理员信息
    const info = ref<Partial<AdminInfo>>({})
    // 搜索历史记录
    const searchHistory = ref<AppRouteRecord[]>([])
    // 访问令牌
    const accessToken = ref('')
    // 刷新令牌
    const refreshToken = ref('')

    // 计算属性：获取用户信息
    const getUserInfo = computed(() => info.value)
    // 计算属性：获取设置状态
    const getSettingState = computed(() => useSettingStore().$state)
    // 计算属性：获取工作台状态
    const getWorktabState = computed(() => useWorktabStore().$state)

    /**
     * 设置用户信息
     * @param newInfo 新的用户信息
     */
    const setUserInfo = (newInfo: AdminInfo) => {
      info.value = newInfo
    }

    /**
     * 设置登录状态
     * @param status 登录状态
     */
    const setLoginStatus = (status: boolean) => {
      isLogin.value = status
    }

    /**
     * 设置语言
     * @param lang 语言枚举值
     */
    const setLanguage = (lang: LanguageEnum) => {
      setPageTitle(router.currentRoute.value)
      language.value = lang
    }

    /**
     * 设置搜索历史
     * @param list 搜索历史列表
     */
    const setSearchHistory = (list: AppRouteRecord[]) => {
      searchHistory.value = list
    }

    /**
     * 设置锁屏状态
     * @param status 锁屏状态
     */
    const setLockStatus = (status: boolean) => {
      isLock.value = status
    }

    /**
     * 设置锁屏密码
     * @param password 锁屏密码
     */
    const setLockPassword = (password: string) => {
      lockPassword.value = password
    }

    /**
     * 设置令牌
     * @param newAccessToken 访问令牌
     * @param newRefreshToken 刷新令牌（可选）
     */
    const setToken = (newAccessToken: string, newRefreshToken?: string) => {
      accessToken.value = newAccessToken
      if (newRefreshToken) {
        refreshToken.value = newRefreshToken
      }
    }

    /**
     * 管理员登录
     * @param params 登录参数
     */
    const login = async (params: LoginParams) => {
      try {
        const response = await AuthService.login(params)
        console.log('AuthService.login 响应:', response)
        console.log('响应类型:', typeof response, '是否有data:', !!response.data)

        // 检查响应数据结构
        const loginData = response.data || response
        console.log('登录数据:', loginData)

        if (loginData && loginData.token) {
          // 设置token
          setToken(loginData.token)
          // 设置用户信息
          setUserInfo(loginData.admin)
          // 设置登录状态
          setLoginStatus(true)

          ElMessage.success('登录成功')
          return loginData
        } else {
          console.error('登录响应数据结构异常:', { response, loginData })
          throw new Error('登录响应数据为空或格式错误')
        }
      } catch (error: any) {
        console.error('登录错误:', error)
        ElMessage.error(error.message || '登录失败')
        throw error
      }
    }

    /**
     * 获取当前用户信息
     */
    const getCurrentUser = async () => {
      try {
        const response = await AuthService.getCurrentUser()
        if (response.data) {
          setUserInfo(response.data)
          setLoginStatus(true)
          return response.data
        }
      } catch (error: any) {
        // token可能已过期，清空登录状态
        logOut()
        throw error
      }
    }

    /**
     * 退出登录
     * 清空所有用户相关状态并跳转到登录页
     */
    const logOut = async () => {
      try {
        // 调用后端退出登录接口
        await AuthService.logout()
      } catch (error) {
        // 忽略退出登录的错误
      } finally {
        // 清空用户信息
        info.value = {}
        // 重置登录状态
        isLogin.value = false
        // 重置锁屏状态
        isLock.value = false
        // 清空锁屏密码
        lockPassword.value = ''
        // 清空访问令牌
        accessToken.value = ''
        // 清空刷新令牌
        refreshToken.value = ''
        // 清空工作台已打开页面
        useWorktabStore().opened = []
        // 移除iframe路由缓存
        sessionStorage.removeItem('iframeRoutes')
        // 清空主页路径
        useMenuStore().setHomePath('')
        // 重置路由状态
        resetRouterState()
        // 跳转到登录页
        router.push(RoutesAlias.Login)
      }
    }

    return {
      language,
      isLogin,
      isLock,
      lockPassword,
      info,
      searchHistory,
      accessToken,
      refreshToken,
      getUserInfo,
      getSettingState,
      getWorktabState,
      setUserInfo,
      setLoginStatus,
      setLanguage,
      setSearchHistory,
      setLockStatus,
      setLockPassword,
      setToken,
      login,
      getCurrentUser,
      logOut
    }
  },
  {
    persist: {
      key: 'user',
      storage: localStorage
    }
  }
)
