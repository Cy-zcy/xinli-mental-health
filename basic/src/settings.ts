import type { RecursiveRequired, Settings } from '#/global'
import { cloneDeep } from 'es-toolkit'
import settingsDefault from '@/settings.default'
import { merge } from '@/utils/object'

const globalSettings: Settings.all = {
  // 心理健康治愈平台配置
  app: {
    enableProgress: true,
    enableBackTop: true,
    enableDynamicTitle: true,
  },
  navbar: {
    enable: true,
  },
  tabbar: {
    enable: true,
    list: [
      {
        path: '/',
        icon: 'i-ic:sharp-home',
        activeIcon: 'i-ic:twotone-home',
        text: '首页',
      },
      {
        path: '/chat/',
        icon: 'i-ic:outline-chat',
        activeIcon: 'i-ic:twotone-chat',
        text: 'AI聊天',
      },
      {
        path: '/forum/',
        icon: 'i-ic:outline-forum',
        activeIcon: 'i-ic:twotone-forum',
        text: '论坛',
      },
      {
        path: '/tools/',
        icon: 'i-ic:outline-healing',
        activeIcon: 'i-ic:twotone-healing',
        text: '工具',
      },
      {
        path: '/profile/',
        icon: 'i-ic:baseline-person',
        activeIcon: 'i-ic:twotone-person',
        text: '我的',
      },
    ],
  },
  copyright: {
    enable: false,
    dates: '2025',
    company: '心理健康治愈平台',
    website: '',
    beian: '',
  },
}

export default merge(globalSettings, cloneDeep(settingsDefault)) as RecursiveRequired<Settings.all>
