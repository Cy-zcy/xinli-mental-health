import { RoutesAlias } from '../routesAlias'
import { AppRouteRecord } from '@/types/router'
import { WEB_LINKS } from '@/utils/constants'

/**
 * 心理健康治愈平台 - 异步路由配置
 *
 * 包含以下核心功能模块：
 * - 仪表板统计
 * - 用户管理
 * - 论坛帖子管理
 * - 系统管理
 */
export const asyncRoutes: AppRouteRecord[] = [
  // 仪表板 - 系统统计概览
  {
    name: 'Dashboard',
    path: '/dashboard',
    component: RoutesAlias.Layout,
    meta: {
      title: '仪表板',
      icon: '&#xe721;',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        path: 'overview',
        name: 'Overview',
        component: RoutesAlias.Dashboard,
        meta: {
          title: '数据概览',
          keepAlive: false,
          fixedTab: true
        }
      }
    ]
  },
  // 论坛管理
  {
    path: '/forum',
    name: 'Forum',
    component: RoutesAlias.Layout,
    meta: {
      title: '论坛管理',
      icon: '&#xe860;',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        path: 'management',
        name: 'ForumManagement',
        component: RoutesAlias.ForumManagement,
        meta: {
          title: '论坛帖子管理',
          keepAlive: true
        }
      },
      {
        path: 'post-detail',
        name: 'PostDetail',
        component: RoutesAlias.PostDetail,
        meta: {
          title: '帖子详情',
          isHide: true,
          keepAlive: true,
          activePath: '/forum/management'
        }
      }
    ]
  },

  // 心理健康干预资源管理
  {
    path: '/resource',
    name: 'ResourceManagement',
    component: RoutesAlias.Layout,
    meta: {
      title: '资源管理',
      icon: '&#xe73e;', // 选择一个合适的 icon (也可以用其他字体图标)
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        path: 'management',
        name: 'ResourceList',
        component: RoutesAlias.ResourceManagement,
        meta: {
          title: '干预资源列表',
          keepAlive: true
        }
      },
      {
        path: 'publish',
        name: 'ResourcePublish',
        component: RoutesAlias.ResourcePublish,
        meta: {
          title: '发布干预资源',
          keepAlive: false
        }
      }
    ]
  },

  // 心理测评管理
  {
    path: '/assessment',
    name: 'AssessmentModule',
    component: RoutesAlias.Layout,
    meta: {
      title: '心理测评管理',
      icon: '&#xe7a3;',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        path: 'management',
        name: 'AssessmentManagement',
        component: RoutesAlias.AssessmentManagement,
        meta: {
          title: '问卷管理',
          keepAlive: true
        }
      },
      {
        path: 'questions',
        name: 'AssessmentQuestions',
        component: RoutesAlias.AssessmentQuestions,
        meta: {
          title: '题目管理',
          isHide: true,
          keepAlive: true,
          activePath: '/assessment/management'
        }
      }
    ]
  },

  // AI聊天管理
  {
    path: '/chat',
    name: 'ChatModule',
    component: RoutesAlias.Layout,
    meta: {
      title: 'AI聊天管理',
      icon: '&#xe8b8;',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        path: 'management',
        name: 'ChatManagement',
        component: RoutesAlias.ChatManagement,
        meta: {
          title: '聊天会话管理',
          keepAlive: true
        }
      }
    ]
  },

  // 用户管理
  {
    path: '/user',
    name: 'UserModule',
    component: RoutesAlias.Layout,
    meta: {
      title: '用户管理',
      icon: '&#xe7b9;',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        path: 'management',
        name: 'UserManagement',
        component: RoutesAlias.UserManagement,
        meta: {
          title: '用户管理',
          keepAlive: true
        }
      }
    ]
  },

  // 系统设置
  {
    path: '/system',
    name: 'System',
    component: RoutesAlias.Layout,
    meta: {
      title: '系统设置',
      icon: '&#xe7b9;',
      roles: ['R_SUPER', 'R_ADMIN']
    },
    children: [
      {
        path: 'user-center',
        name: 'UserCenter',
        component: RoutesAlias.UserCenter,
        meta: {
          title: '个人中心',
          keepAlive: true,
          isHideTab: false
        }
      }
    ]
  }
]
