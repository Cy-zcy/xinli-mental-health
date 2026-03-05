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

      <ElCol :xs="24" :lg="10">
        <ElCard shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近期测评趋势（近半年）</span>
            </div>
          </template>
          <div class="chart-container" ref="trendChartRef"></div>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :lg="24" style="margin-top: 20px;">
        <ElCard shadow="never">
          <template #header>
            <div class="card-header">
              <span>⚠️ 高风险情况预警（重度抑郁测评结果）</span>
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

      <!-- 模块五：心理健康分预警列表 -->
      <ElCol :xs="24" :lg="24" style="margin-top: 20px;">
        <ElCard shadow="never" style="border-left: 4px solid #F56C6C;">
          <template #header>
            <div class="card-header">
              <span>🚨 动态心理健康分低分预警（< 60 分）</span>
              <ElTag type="danger" effect="dark">{{ lowScoreTotal }} 人</ElTag>
            </div>
          </template>
          <ElTable :data="lowScoreList" border size="small" style="width:100%" v-loading="lowScoreLoading">
            <ElTableColumn prop="id" label="用户ID" width="80" align="center" />
            <ElTableColumn prop="username" label="用户名" width="120" align="center" show-overflow-tooltip />
            <ElTableColumn prop="nickname" label="昵称" width="120" align="center" show-overflow-tooltip />
            <ElTableColumn prop="healthScore" label="当前健康分" width="120" align="center">
              <template #default="{ row }">
                <ElTag :type="getScoreTagType(row.healthScore)" effect="dark">
                  {{ row.healthScore }} 分
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" align="center" width="120">
              <template #default="{ row }">
                <ElButton link type="primary" size="small" @click="() => goToUserManage(row.id)">
                  查看详情
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
          <div style="display:flex;justify-content:center;margin-top:12px" v-if="lowScoreTotal > 10">
            <ElPagination
              size="small"
              background
              v-model:current-page="lowScorePage"
              :page-size="10"
              layout="prev, pager, next"
              :total="lowScoreTotal"
              @current-change="loadLowScoreUsers"
            />
          </div>
        </ElCard>
      </ElCol>

      <!-- 模块六：AI 论坛高危帖子预警 -->
      <ElCol :xs="24" :lg="24" style="margin-top: 20px;">
        <ElCard shadow="never" style="border-left: 4px solid #9C27B0;">
          <template #header>
            <div class="card-header">
              <span>🤖 AI 论坛危机帖子预警</span>
              <ElTag type="danger" effect="dark">{{ crisisPostTotal }} 条</ElTag>
            </div>
          </template>
          <ElTable :data="crisisPostList" border size="small" style="width:100%" v-loading="crisisPostLoading">
            <ElTableColumn prop="postId" label="帖子ID" width="80" align="center" />
            <ElTableColumn prop="userId" label="用户ID" width="80" align="center" />
            <ElTableColumn prop="emotionLabel" label="情绪标签" width="100" align="center">
              <template #default="{ row }">
                <ElTag type="danger" size="small">{{ row.emotionLabel }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="emotionTags" label="细分标签" width="200" show-overflow-tooltip />
            <ElTableColumn prop="aiSummary" label="AI摘要" min-width="200" show-overflow-tooltip />
            <ElTableColumn prop="riskReason" label="高危原因" min-width="180" show-overflow-tooltip />
            <ElTableColumn prop="isAlerted" label="已通知" width="80" align="center">
              <template #default="{ row }">
                <ElTag :type="row.isAlerted ? 'success' : 'warning'" size="small">
                  {{ row.isAlerted ? '已通知' : '未通知' }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createdAt" label="检测时间" width="160" align="center" />
          </ElTable>
          <div style="display:flex;justify-content:center;margin-top:12px" v-if="crisisPostTotal > 10">
            <ElPagination
              size="small"
              background
              v-model:current-page="crisisPostPage"
              :page-size="10"
              layout="prev, pager, next"
              :total="crisisPostTotal"
              @current-change="loadCrisisPosts"
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
  import { ref, onMounted, nextTick } from 'vue'
  import { User, UserFilled, Document, ChatDotRound, Message, Refresh } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
  import { DashboardService, type DashboardStats, type HighRiskRecord, type ForumPostAnalysis } from '@/api/dashboardApi'
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

  const chatStats = ref<any>({
    totalSessions: 0,
    totalMessages: 0,
    totalTokensUsed: 0,
    activeUsers: 0,
    todayMessages: 0
  })

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const recentPosts = ref<any[]>([])

  // 高风险用户
  const highRiskList = ref<HighRiskRecord[]>([])
  const highRiskTotal = ref(0)
  const highRiskPage = ref(1)
  const highRiskLoading = ref(false)

  // 极低健康分预警用户
  const lowScoreList = ref<any[]>([])
  const lowScoreTotal = ref(0)
  const lowScorePage = ref(1)
  const lowScoreLoading = ref(false)

  // AI 高危帖子预警
  const crisisPostList = ref<ForumPostAnalysis[]>([])
  const crisisPostTotal = ref(0)
  const crisisPostPage = ref(1)
  const crisisPostLoading = ref(false)


  // 图表容器引用
  const userChartRef = ref<HTMLElement | null>(null)
  const postChartRef = ref<HTMLElement | null>(null)
  const assessmentChartRef = ref<HTMLElement | null>(null)
  const trendChartRef = ref<HTMLElement | null>(null)
  // ECharts 实例引用
  let assessmentChart: echarts.ECharts | null = null
  let trendChart: echarts.ECharts | null = null

  /**
   * 加载健康分极低用户预警列表
   */
  const loadLowScoreUsers = async (page = 1) => {
    lowScoreLoading.value = true
    try {
      const res = await DashboardService.getLowHealthScoreUsers(page, 10)
      if (res) {
        lowScoreList.value = res.records || []
        lowScoreTotal.value = res.total || 0
      }
    } catch (error) {
      console.error('获取健康分预警用户失败')
    } finally {
      lowScoreLoading.value = false
  }

  /**
   * 加载 AI 危机帖子预警列表
   */
  const loadCrisisPosts = async (page = 1) => {
    crisisPostLoading.value = true
    try {
      const res = await DashboardService.getCrisisPosts(page, 10)
      if (res) {
        crisisPostList.value = res.records || []
        crisisPostTotal.value = res.total || 0
      }
    } catch (error) {
      console.error('获取危机预警帖子失败')
    } finally {
      crisisPostLoading.value = false
    }
  }

  /**
   * 获取统计数据
   */
  const fetchStats = async () => {
    try {
      const response: any = await DashboardService.getDashboardStats()
      if (response) {
        dashboardStats.value = response

        userStats.value.total = response.totalUsers
        userStats.value.active = response.activeUsers
        forumStats.value.activePosts = response.totalPosts - response.pendingPosts

        nextTick(() => {
          renderAssessmentChart(response as any)
          renderTrendChart(response as any)
        })
      }
      await fetchChatStats()
      await loadHighRiskUsers()
      await loadLowScoreUsers()
      await loadCrisisPosts()
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
      if (response) {
        chatStats.value = response
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
   * 渲染测评趋势折线图
   */
  const renderTrendChart = (stats: any) => {
    if (!trendChartRef.value || !stats.trends) return
    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value)
    }

    const months = stats.trends.map((t: any) => t.month)
    const totalData = stats.trends.map((t: any) => t.total)
    const highRiskData = stats.trends.map((t: any) => t.highRisk)

    trendChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['总测评人数', '高危人数（重度抑郁）']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: months
      },
      yAxis: {
        type: 'value'
      },
      color: ['#409EFF', '#F56C6C'],
      series: [
        {
          name: '总测评人数',
          type: 'line',
          smooth: true,
          data: totalData,
          areaStyle: {
            opacity: 0.1
          }
        },
        {
          name: '高危人数（重度抑郁）',
          type: 'line',
          smooth: true,
          data: highRiskData,
          areaStyle: {
            opacity: 0.1
          }
        }
      ]
    })
  }

  /**
   * 加载高风险用户列表
   */
  const loadHighRiskUsers = async (page = 1) => {
    highRiskLoading.value = true
    try {
      const res = await DashboardService.getHighRiskUsers(page, 10)
      if (res) {
        highRiskList.value = res.records || []
        highRiskTotal.value = res.total || 0
      }
    } catch (error) {
      console.error('获取高风险用户失败')
    } finally {
      highRiskLoading.value = false
    }
  }


  /**
   * 跳转到用户管理(如果带id可触发具体操作，此处暂时仅跳页)
   */
  const goToUserManage = (userId?: number) => {
    router.push('/user/management')
    // 可选：利用全局状态或者路由参数将 userId 传递给用户管理页自动展开抽屉
  }

  /**
   * 根据健康分返回Tag颜色
   */
  const getScoreTagType = (score: number) => {
    if (score >= 80) return 'success'
    if (score >= 60) return 'warning'
    return 'danger'
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
      trendChart?.resize()
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
