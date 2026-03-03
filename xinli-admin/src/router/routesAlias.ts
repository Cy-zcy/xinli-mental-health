/**
 * 心理健康治愈平台 - 路由别名
 * 方便快速找到页面，同时可以用作路由跳转
 */

/** 路由别名 */
export enum RoutesAlias {
  // 基础路由
  Layout = '/index/index', // 布局容器
  Login = '/auth/login', // 登录

  // 异常页面
  Exception403 = '/exception/403', // 403
  Exception404 = '/exception/404', // 404
  Exception500 = '/exception/500', // 500

  // 仪表板
  Dashboard = '/dashboard/overview', // 数据概览

  // 论坛管理
  ForumManagement = '/forum/management', // 论坛帖子管理
  PostDetail = '/forum/post-detail', // 帖子详情

  // 心理干预资源管理
  ResourceManagement = '/resource/management', // 资源列表
  ResourcePublish = '/resource/publish', // 资源发布/编辑

  // 心理测评管理
  AssessmentManagement = '/assessment/management', // 问卷列表管理
  AssessmentQuestions = '/assessment/questions', // 问卷题目管理



  // AI聊天管理
  ChatManagement = '/chat/management', // AI聊天管理

  // 用户管理
  UserManagement = '/user/management', // 用户管理

  // 系统设置
  UserCenter = '/system/user-center', // 个人中心
  RoleManagement = '/system/role', // 角色权限管理
  AdminManagement = '/system/admin' // 后台管理员账户管理
}
