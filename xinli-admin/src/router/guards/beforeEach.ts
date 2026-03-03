import type { Router, RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { ref, nextTick } from 'vue'
import NProgress from 'nprogress'
import { useSettingStore } from '@/store/modules/setting'
import { useUserStore } from '@/store/modules/user'
import { useMenuStore } from '@/store/modules/menu'
import { setWorktab } from '@/utils/navigation'
import { setPageTitle, setSystemTheme } from '../utils/utils'
import { menuService } from '@/api/menuApi'
import { registerDynamicRoutes } from '../utils/registerRoutes'
import { AppRouteRecord } from '@/types/router'
import { RoutesAlias } from '../routesAlias'
import { menuDataToRouter } from '../utils/menuToRouter'
import { asyncRoutes } from '../routes/asyncRoutes'
import { loadingService } from '@/utils/ui'
import { useCommon } from '@/composables/useCommon'
import { useWorktabStore } from '@/store/modules/worktab'

// 前端权限模式 loading 关闭延时，提升用户体验
const LOADING_DELAY = 300

// 是否已注册动态路由
const isRouteRegistered = ref(false)

// 跟踪是否需要关闭 loading
const pendingLoading = ref(false)

/**
 * 设置路由全局前置守卫
 */
export function setupBeforeEachGuard(router: Router): void {
  router.beforeEach(
    async (
      to: RouteLocationNormalized,
      from: RouteLocationNormalized,
      next: NavigationGuardNext
    ) => {
      try {
        await handleRouteGuard(to, from, next, router)
      } catch (error) {
        console.error('路由守卫处理失败:', error)
        next('/exception/500')
      }
    }
  )

  // 设置后置守卫以关闭 loading 和进度条
  setupAfterEachGuard(router)
}

/**
 * 设置路由全局后置守卫
 */
function setupAfterEachGuard(router: Router): void {
  router.afterEach(() => {
    // 关闭进度条
    const settingStore = useSettingStore()
    if (settingStore.showNprogress) {
      NProgress.done()
    }

    // 关闭 loading 效果
    if (pendingLoading.value) {
      nextTick(() => {
        loadingService.hideLoading()
        pendingLoading.value = false
      })
    }
  })
}

/**
 * 处理路由守卫逻辑
 */
async function handleRouteGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext,
  router: Router
): Promise<void> {
  console.log('路由守卫处理:', JSON.stringify({ to: to.path, from: from.path }))

  const settingStore = useSettingStore()
  const userStore = useUserStore()

  console.log('用户状态:', JSON.stringify({
    isLogin: userStore.isLogin,
    hasToken: !!userStore.accessToken,
    hasUserInfo: !!userStore.info.id
  }))

  // 处理进度条
  if (settingStore.showNprogress) {
    NProgress.start()
  }

  // 设置系统主题
  setSystemTheme(to)

  // 处理登录状态
  if (!(await handleLoginStatus(to, userStore, next))) {
    console.log('登录状态处理失败，停止路由守卫')
    return
  }

  // 处理动态路由注册
  if (!isRouteRegistered.value && userStore.isLogin) {
    await handleDynamicRoutes(to, router, next)
    return
  }

  // 处理根路径跳转到首页
  if (userStore.isLogin && isRouteRegistered.value && handleRootPathRedirect(to, next)) {
    return
  }

  // 处理已知的匹配路由
  if (to.matched.length > 0) {
    setWorktab(to)
    setPageTitle(to)
    next()
    return
  }

  // 尝试刷新路由重新注册
  if (userStore.isLogin && !isRouteRegistered.value) {
    await handleDynamicRoutes(to, router, next)
    return
  }

  // 未匹配到路由，跳转到 404
  next(RoutesAlias.Exception404)
}

/**
 * 处理登录状态
 */
async function handleLoginStatus(
  to: RouteLocationNormalized,
  userStore: ReturnType<typeof useUserStore>,
  next: NavigationGuardNext
): Promise<boolean> {
  console.log('处理登录状态:', JSON.stringify({ path: to.path, isLogin: userStore.isLogin, hasToken: !!userStore.accessToken }))

  // 如果访问登录页面
  if (to.path === RoutesAlias.Login) {
    // 如果已经登录，重定向到首页
    if (userStore.isLogin && userStore.accessToken) {
      console.log('已登录用户访问登录页，重定向到首页')
      next('/')
      return false
    }
    // 未登录，允许访问登录页面
    console.log('未登录用户访问登录页，允许通过')
    return true
  }

  // 如果页面不需要登录，直接通过
  if (to.meta.noLogin) {
    return true
  }

  // 检查是否有token
  if (!userStore.accessToken) {
    userStore.logOut()
    next(RoutesAlias.Login)
    return false
  }

  // 如果有token但没有用户信息，尝试获取用户信息
  if (!userStore.isLogin || !userStore.info.id) {
    try {
      await userStore.getCurrentUser()
    } catch (error) {
      // token可能已过期，跳转到登录页
      userStore.logOut()
      next(RoutesAlias.Login)
      return false
    }
  }

  return true
}

/**
 * 处理动态路由注册
 */
async function handleDynamicRoutes(
  to: RouteLocationNormalized,
  router: Router,
  next: NavigationGuardNext
): Promise<void> {
  try {
    // 显示 loading 并标记 pending
    pendingLoading.value = true
    loadingService.showLoading()

    await getMenuData(router)

    // 处理根路径跳转
    if (handleRootPathRedirect(to, next)) {
      return
    }

    next({
      path: to.path,
      query: to.query,
      hash: to.hash,
      replace: true
    })
  } catch (error) {
    console.error('动态路由注册失败:', error)
    next('/exception/500')
  }
}

/**
 * 获取菜单数据
 */
async function getMenuData(router: Router): Promise<void> {
  try {
    if (useCommon().isFrontendMode.value) {
      await processFrontendMenu(router)
    } else {
      await processBackendMenu(router)
    }
  } catch (error) {
    handleMenuError(error)
    throw error
  }
}

/**
 * 处理前端控制模式的菜单逻辑
 */
async function processFrontendMenu(router: Router): Promise<void> {
  const menuList = asyncRoutes.map((route) => menuDataToRouter(route))
  const userStore = useUserStore()
  const roles = userStore.info.roles || []
  const permissions = userStore.info.permissions || []

  const filteredMenuList = filterMenuByRoles(menuList, roles, permissions)

  // 添加延时以提升用户体验
  await new Promise((resolve) => setTimeout(resolve, LOADING_DELAY))

  await registerAndStoreMenu(router, filteredMenuList)
}

/**
 * 处理后端控制模式的菜单逻辑
 */
async function processBackendMenu(router: Router): Promise<void> {
  const { menuList } = await menuService.getMenuList()
  await registerAndStoreMenu(router, menuList)
}

/**
 * 注册路由并存储菜单数据
 */
async function registerAndStoreMenu(router: Router, menuList: AppRouteRecord[]): Promise<void> {
  if (!isValidMenuList(menuList)) {
    throw new Error('获取菜单列表失败，请重新登录')
  }

  const menuStore = useMenuStore()
  menuStore.setMenuList(menuList)
  registerDynamicRoutes(router, menuList)
  isRouteRegistered.value = true
  useWorktabStore().validateWorktabs(router)
}

/**
 * 处理菜单相关错误
 */
function handleMenuError(error: unknown): void {
  console.error('菜单处理失败:', error)
  useUserStore().logOut()
  throw error instanceof Error ? error : new Error('获取菜单列表失败，请重新登录')
}

/**
 * 根据角色与权限标识过滤菜单
 */
const filterMenuByRoles = (menu: AppRouteRecord[], roles: string[], permissions: string[]): AppRouteRecord[] => {
  const isSuperAdmin = roles.includes('R_SUPER') || roles.includes('super_admin')

  return menu.reduce((acc: AppRouteRecord[], item) => {
    let hasPermission = false

    if (isSuperAdmin) {
      hasPermission = true
    } else {
      // 当非超管时，优先根据分配的 permissions (存储了菜单路由 name) 判定
      if (item.name && permissions.includes(item.name as string)) {
        hasPermission = true
      } else {
        // 如果没有分配 permissions，降级判断路由上的 roles 字段
        const itemRoles = item.meta?.roles
        hasPermission = !itemRoles || itemRoles.some((role) => roles?.includes(role))
      }
    }

    if (hasPermission) {
      const filteredItem = { ...item }
      if (filteredItem.children?.length) {
        filteredItem.children = filterMenuByRoles(filteredItem.children, roles, permissions)
      }
      acc.push(filteredItem)
    }

    return acc
  }, [])
}

/**
 * 验证菜单列表是否有效
 */
function isValidMenuList(menuList: AppRouteRecord[]): boolean {
  return Array.isArray(menuList) && menuList.length > 0
}

/**
 * 重置路由相关状态
 */
export function resetRouterState(): void {
  isRouteRegistered.value = false
  const menuStore = useMenuStore()
  menuStore.removeAllDynamicRoutes()
  menuStore.setMenuList([])
}

/**
 * 处理根路径跳转到首页
 */
function handleRootPathRedirect(to: RouteLocationNormalized, next: NavigationGuardNext): boolean {
  if (to.path === '/') {
    const { homePath } = useCommon()
    // 如果有设置的首页路径，使用它；否则默认跳转到仪表板
    const targetPath = homePath.value || '/dashboard/overview'
    console.log('根路径重定向:', JSON.stringify({ homePath: homePath.value, targetPath }))
    next({ path: targetPath, replace: true })
    return true
  }
  return false
}
