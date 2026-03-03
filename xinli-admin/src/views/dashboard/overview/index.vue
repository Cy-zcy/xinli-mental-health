<!-- 心理健康平台 - 数据概览 -->
<template>
  <div class="dashboard-overview">
    <!-- 统计卡片 -->
    <ElRow :gutter="20" class="stats-cards">
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <ElIcon><User /></ElIcon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ userStats.total || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
      
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon active-icon">
              <ElIcon><UserFilled /></ElIcon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ userStats.active || 0 }}</div>
              <div class="stat-label">活跃用户</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
      
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon post-icon">
              <ElIcon><Document /></ElIcon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ forumStats.totalPosts || 0 }}</div>
              <div class="stat-label">总帖子数</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
      
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon chat-icon">
              <ElIcon><ChatDotRound /></ElIcon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ chatStats.totalSessions || 0 }}</div>
              <div class="stat-label">AI聊天会话</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <!-- 今日数据统计 -->
    <ElRow :gutter="20" class="stats-cards" style="margin-top: 20px;">
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card today-card">
          <div class="stat-content">
            <div class="stat-icon today-user-icon">
              <ElIcon><User /></ElIcon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ dashboardStats.todayNewUsers || 0 }}</div>
              <div class="stat-label">今日新增用户</div>
            </div>
          </div>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card today-card">
          <div class="stat-content">
            <div class="stat-icon today-post-icon">
              <ElIcon><Document /></ElIcon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ dashboardStats.todayNewPosts || 0 }}</div>
              <div class="stat-label">今日新增帖子</div>
            </div>
          </div>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card pending-card">
          <div class="stat-content">
            <div class="stat-icon pending-icon">
              <ElIcon><Document /></ElIcon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ dashboardStats.pendingPosts || 0 }}</div>
              <div class="stat-label">待审核帖子</div>
            </div>
          </div>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card chat-card">
          <div class="stat-content">
            <div class="stat-icon today-chat-icon">
              <ElIcon><Message /></ElIcon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ chatStats.todayMessages || 0 }}</div>
              <div class="stat-label">今日AI对话</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <!-- 图表区域 -->
    <ElRow :gutter="20" class="charts-section">
      <ElCol :xs="24" :lg="12">
        <ElCard shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div class="chart-container" ref="userChartRef"></div>
        </ElCard>
      </ElCol>
      
      <ElCol :xs="24" :lg="12">
        <ElCard shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>帖子分类统计</span>
            </div>
          </template>
          <div class="chart-container" ref="postChartRef"></div>
        </ElCard>
      </ElCol>
    </ElRow>

    <!-- 心理测评统计 -->
    <ElRow :gutter="20" class="charts-section">
      <!-- 测评分布统计卡片 -->
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card assessment-total-card">
          <div class="stat-content">
            <div class="stat-icon assessment-icon">
              <span style="font-size:24px">📊</span>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ dashboardStats.totalAssessments || 0 }}</div>
              <div class="stat-label">测评总次数</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card" style="border-left: 4px solid #67C23A;">
          <div class="stat-content">
            <div class="stat-icon" style="background:linear-gradient(135deg,#67C23A,#95d475);">
              <span style="font-size:20px">😊</span>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ dashboardStats.normalCount || 0 }}</div>
              <div class="stat-label">正常</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card" style="border-left: 4px solid #E6A23C;">
          <div class="stat-content">
            <div class="stat-icon" style="background:linear-gradient(135deg,#E6A23C,#f0c080);">
              <span style="font-size:20px">😐</span>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ (dashboardStats.mildCount || 0) + (dashboardStats.moderateCount || 0) }}</div>
              <div class="stat-label">轻/中度抑郁</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :md="6">
        <ElCard shadow="hover" class="stat-card" style="border-left: 4px solid #F56C6C;">
          <div class="stat-content">
            <div class="stat-icon" style="background:linear-gradient(135deg,#F56C6C,#f99);">
              <span style="font-size:20px">⚠️</span>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ dashboardStats.severeCount || 0 }}</div>
              <div class="stat-label">重度抑郁（高风险）</div>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <!-- 测评饼图 + 高风险用户表 -->
    <ElRow :gutter="20" class="charts-section">
      <ElCol :xs="24" :lg="10">
        <ElCard shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>测评结果分布</span>
            </div>
          </template>
          <div class="chart-container" ref="assessmentChartRef"></div>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :lg="14">
        <ElCard shadow="never">
          <template #header>
            <div class="card-header">
              <span>⚠️ 高风险用户预警（重度抑郁）</span>
              <ElTag type="danger">{{ highRiskTotal }} 人</ElTag>
            </div>
          </template>
          <ElTable :data="highRiskList" border size="small" style="width:100%" v-loading="highRiskLoading">
            <ElTableColumn prop="userId" label="用户ID" width="80" align="center" />
            <ElTableColumn prop="totalScore" label="测评得分" width="90" align="center" />
            <ElTableColumn prop="resultSummary" label="评估结果" width="100" align="center">
              <template #default="{ row }">
                <ElTag type="danger">{{ row.resultSummary }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createdAt" label="测评时间" show-overflow-tooltip>
              <template #default="{ row }">
                {{ new Date(row.createdAt).toLocaleString('zh-CN') }}
              </template>
            </ElTableColumn>
          </ElTable>
          <div style="display:flex;justify-content:center;margin-top:12px" v-if="highRiskTotal > 10">
            <ElPagination
              size="small"
              background
              v-model:current-page="highRiskPage"
              :page-size="10"
              layout="prev, pager, next"
              :total="highRiskTotal"
              @current-change="loadHighRiskUsers"
            />
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <!-- 最新动态 -->
    <ElRow :gutter="20" class="activity-section">
      <ElCol :xs="24" :lg="16">
        <ElCard shadow="never" class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最新帖子</span>
              <ElButton type="primary" link @click="goToForumManage">查看更多</ElButton>
            </div>
          </template>
          <div class="activity-list">
            <div v-for="post in recentPosts" :key="post.id" class="activity-item">
              <div class="activity-content">
                <div class="activity-title">{{ post.title }}</div>
                <div class="activity-meta">
                  <span class="author">{{ post.user?.nickname || '匿名用户' }}</span>
                  <span class="time">{{ formatTime(post.createdAt) }}</span>
                  <ElTag :type="post.category === 'emotion' ? 'primary' : 'success'" size="small">
                    {{ post.category === 'emotion' ? '情感倾诉' : '经验分享' }}
                  </ElTag>
                </div>
              </div>
            </div>
          </div>
        </ElCard>
      </ElCol>
      
      <ElCol :xs="24" :lg="8">
        <ElCard shadow="never" class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <ElButton type="primary" class="action-btn" @click="goToUserManage">
              <ElIcon><User /></ElIcon>
              用户管理
            </ElButton>
            <ElButton type="success" class="action-btn" @click="goToForumManage">
              <ElIcon><Document /></ElIcon>
              论坛管理
            </ElButton>
            <ElButton type="info" class="action-btn" @click="refreshData">
              <ElIcon><Refresh /></ElIcon>
              刷新数据
            </ElButton>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import { User, UserFilled, Document, ChatDotRound, Message, Refresh } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
  import { DashboardService, type DashboardStats, type HighRiskRecord } from '@/api/dashboardApi'
  import { AdminChatService } from '@/api/chatApi'
  import { useRouter } from 'vue-router'
  import * as echarts from 'echarts'

  defineOptions({ name: 'DashboardOverview' })

  const router = useRouter()

  // 响应式数据
  const dashboardStats = ref<DashboardStats>({
    totalUsers: 0,
    totalPosts: 0,
    totalComments: 0,
    todayNewUsers: 0,
    todayNewPosts: 0,
    activeUsers: 0,
    pendingPosts: 0,
    totalAssessments: 0,
    normalCount: 0,
    mildCount: 0,
    moderateCount: 0,
    severeCount: 0
  })

  const userStats = ref({
    total: 0,
    active: 0
  })

  const forumStats = ref({
    totalPosts: 0,
    activePosts: 0,
    emotionPosts: 0,
    experiencePosts: 0
  })

  const chatStats = ref({
    totalSessions: 0,
    totalMessages: 0,
    totalTokensUsed: 0,
    activeUsers: 0,
    todayMessages: 0
  })

  const recentPosts = ref([])

  // 高风险用户
  const highRiskList = ref<HighRiskRecord[]>([])
  const highRiskTotal = ref(0)
  const highRiskPage = ref(1)
  const highRiskLoading = ref(false)

  // 图表容器引用
  const userChartRef = ref()
  const postChartRef = ref()
  const assessmentChartRef = ref()

  // ECharts 实例引用
  let assessmentChart: echarts.ECharts | null = null

  /**
   * 获取统计数据
   */
  const fetchStats = async () => {
    try {
      const response = await DashboardService.getDashboardStats()
      if (response.data) {
        dashboardStats.value = response.data

        userStats.value.total = response.data.totalUsers
        userStats.value.active = response.data.activeUsers
        forumStats.value.totalPosts = response.data.totalPosts
        forumStats.value.activePosts = response.data.totalPosts - response.data.pendingPosts

        nextTick(() => renderAssessmentChart(response.data as DashboardStats))
      }
      await fetchChatStats()
      await loadHighRiskUsers()
    } catch (error) {
      console.error('获取统计数据失败:', error)
      ElMessage.error('获取统计数据失败')
    }
  }


  /**
   * 获取AI聊天统计数据
   */
  const fetchChatStats = async () => {
    try {
      const response = await AdminChatService.getChatStats()
      if (response.data) {
        chatStats.value = response.data
      }
    } catch (error) {
      console.error('获取AI聊天统计数据失败:', error)
    }
  }

  /**
   * 渲染测评结果饪图
   */
  const renderAssessmentChart = (stats: DashboardStats) => {
    if (!assessmentChartRef.value) return
    if (!assessmentChart) {
      assessmentChart = echarts.init(assessmentChartRef.value)
    }
    assessmentChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', left: 'left' },
      color: ['#67C23A', '#E6A23C', '#F5A623', '#F56C6C'],
      series: [{
        name: '测评结果',
        type: 'pie',
        radius: ['40%', '70%'],
        label: { show: true, formatter: '{b}\n{d}%' },
        data: [
          { value: stats.normalCount || 0,   name: '正常' },
          { value: stats.mildCount || 0,     name: '轻度抑郁' },
          { value: stats.moderateCount || 0, name: '中度抑郁' },
          { value: stats.severeCount || 0,   name: '重度抑郁' }
        ]
      }]
    })
  }

  /**
   * 加载高风险用户列表
   */
  const loadHighRiskUsers = async (page = 1) => {
    highRiskLoading.value = true
    try {
      const res = await DashboardService.getHighRiskUsers(page, 10)
      if (res.data) {
        highRiskList.value = res.data.records || []
        highRiskTotal.value = res.data.total || 0
      }
    } catch (error) {
      console.error('获取高风险用户失败')
    } finally {
      highRiskLoading.value = false
    }
  }


  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    return new Date(time).toLocaleString('zh-CN')
  }

  /**
   * 跳转到用户管理
   */
  const goToUserManage = () => {
    router.push('/user/management')
  }

  /**
   * 跳转到论坛管理
   */
  const goToForumManage = () => {
    router.push('/forum/posts')
  }

  /**
   * 跳转到AI聊天管理
   */
  const goToChatManage = () => {
    router.push('/chat/management')
  }

  /**
   * 刷新数据
   */
  const refreshData = () => {
    fetchStats()
    ElMessage.success('数据已刷新')
  }

  // 页面加载时获取数据
  onMounted(() => {
    fetchStats()
    // 监听窗口大小变化，重绘 ECharts
    window.addEventListener('resize', () => {
      assessmentChart?.resize()
    })
  })
</script>

<style lang="scss" scoped>
  .dashboard-overview {
    padding: 20px;
    
    .stats-cards {
      margin-bottom: 20px;
      
      .stat-card {
        .stat-content {
          display: flex;
          align-items: center;
          
          .stat-icon {
            width: 60px;
            height: 60px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 16px;
            font-size: 24px;
            color: white;
            
            &.user-icon {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }
            
            &.active-icon {
              background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            }
            
            &.post-icon {
              background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            }
            
            &.chat-icon {
              background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
            }

            &.today-user-icon {
              background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
            }

            &.today-chat-icon {
              background: linear-gradient(135deg, #a8e6cf 0%, #dcedc1 100%);
            }

            &.today-post-icon {
              background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
            }

            &.pending-icon {
              background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
            }
          }
          
          .stat-info {
            .stat-number {
              font-size: 28px;
              font-weight: bold;
              color: var(--el-text-color-primary);
              line-height: 1;
            }
            
            .stat-label {
              font-size: 14px;
              color: var(--el-text-color-regular);
              margin-top: 4px;
            }
          }
        }

        &.today-card {
          border-left: 4px solid #ffa726;
        }

        &.pending-card {
          border-left: 4px solid #ef5350;
        }

        &.active-card {
          border-left: 4px solid #66bb6a;
        }

        &.chat-card {
          border-left: 4px solid #26c6da;
        }
      }
    }
    
    .charts-section {
      margin-bottom: 20px;
      
      .chart-card {
        .chart-container {
          height: 300px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--el-text-color-placeholder);
          font-size: 14px;
        }
      }
    }
    
    .activity-section {
      .activity-card {
        .activity-list {
          .activity-item {
            padding: 12px 0;
            border-bottom: 1px solid var(--el-border-color-lighter);
            
            &:last-child {
              border-bottom: none;
            }
            
            .activity-content {
              .activity-title {
                font-size: 14px;
                color: var(--el-text-color-primary);
                margin-bottom: 8px;
                cursor: pointer;
                
                &:hover {
                  color: var(--el-color-primary);
                }
              }
              
              .activity-meta {
                display: flex;
                align-items: center;
                gap: 12px;
                font-size: 12px;
                color: var(--el-text-color-regular);
              }
            }
          }
        }
      }
      
      .quick-actions-card {
        .quick-actions {
          display: flex;
          flex-direction: column;
          gap: 12px;
          
          .action-btn {
            width: 100%;
            justify-content: flex-start;
            
            .el-icon {
              margin-right: 8px;
            }
          }
        }
      }
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
</style>
