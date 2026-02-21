<template>
  <div class="chat-management">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <ElRow :gutter="20">
        <ElCol :span="6">
          <ElCard shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <ElIcon size="40" color="#409EFF"><ChatDotRound /></ElIcon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ chatStats.totalSessions }}</div>
                <div class="stat-label">总会话数</div>
              </div>
            </div>
          </ElCard>
        </ElCol>
        <ElCol :span="6">
          <ElCard shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <ElIcon size="40" color="#67C23A"><Message /></ElIcon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ chatStats.totalMessages }}</div>
                <div class="stat-label">总消息数</div>
              </div>
            </div>
          </ElCard>
        </ElCol>
        <ElCol :span="6">
          <ElCard shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <ElIcon size="40" color="#E6A23C"><Cpu /></ElIcon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ formatNumber(chatStats.totalTokensUsed) }}</div>
                <div class="stat-label">总Token使用</div>
              </div>
            </div>
          </ElCard>
        </ElCol>
        <ElCol :span="6">
          <ElCard shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <ElIcon size="40" color="#F56C6C"><User /></ElIcon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ chatStats.activeUsers }}</div>
                <div class="stat-label">活跃用户</div>
              </div>
            </div>
          </ElCard>
        </ElCol>
      </ElRow>
    </div>

    <!-- 搜索和筛选 -->
    <ElCard shadow="never" class="search-card">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="用户手机号">
          <ElInput
            v-model="searchForm.userPhone"
            placeholder="输入手机号"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem label="用户昵称">
          <ElInput
            v-model="searchForm.userNickname"
            placeholder="输入昵称"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem label="开始日期">
          <ElDatePicker
            v-model="searchForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 150px"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem label="结束日期">
          <ElDatePicker
            v-model="searchForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 150px"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">
            <ElIcon><Search /></ElIcon>
            搜索
          </ElButton>
          <ElButton @click="handleReset">
            <ElIcon><Refresh /></ElIcon>
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 会话列表 -->
    <ElCard shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>AI聊天会话列表</span>
          <div class="header-actions">
            <ElButton @click="testAiConnection">
              <ElIcon><Connection /></ElIcon>
              测试AI连接
            </ElButton>
            <ElButton @click="refreshData">
              <ElIcon><Refresh /></ElIcon>
              刷新
            </ElButton>
          </div>
        </div>
      </template>

      <ElTable
        v-loading="loading"
        :data="sessionList"
        stripe
        style="width: 100%"
      >
        <ElTableColumn prop="id" label="会话ID" width="80" />
        <ElTableColumn label="用户信息" width="200">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-phone">{{ row.userPhone }}</div>
              <div class="user-nickname">{{ row.userNickname || '未设置昵称' }}</div>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="title" label="会话标题" min-width="200" />
        <ElTableColumn label="消息数量" width="100" align="center">
          <template #default="{ row }">
            <ElTag type="info">{{ row.messageCount }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="状态" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已删除' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="最后活跃" width="180">
          <template #default="{ row }">
            {{ row.lastActiveTime ? formatTime(row.lastActiveTime) : '无活动' }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <ElButton size="small" @click="viewSessionDetail(row.id)">
              查看
            </ElButton>
            <ElButton
              size="small"
              type="danger"
              @click="deleteSession(row)"
            >
              删除
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <!-- 分页 -->
      <div class="pagination-container">
        <ElPagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </ElCard>

    <!-- 会话详情对话框 -->
    <ElDialog
      v-model="detailDialogVisible"
      title="会话详情"
      width="800px"
      :before-close="handleCloseDetail"
    >
      <div v-if="currentSession" class="session-detail">
        <ElDescriptions :column="2" border class="session-info">
          <ElDescriptionsItem label="会话ID">{{ currentSession.session?.id }}</ElDescriptionsItem>
          <ElDescriptionsItem label="会话标题">{{ currentSession.session?.title }}</ElDescriptionsItem>
          <ElDescriptionsItem label="用户手机号">{{ currentSession.user?.phone }}</ElDescriptionsItem>
          <ElDescriptionsItem label="用户昵称">{{ currentSession.user?.nickname || '未设置' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="消息数量">{{ currentSession.messages?.length || 0 }}</ElDescriptionsItem>
          <ElDescriptionsItem label="创建时间">{{ formatTime(currentSession.session?.createdAt) }}</ElDescriptionsItem>
        </ElDescriptions>

        <div class="messages-container">
          <h4>聊天记录</h4>
          <div class="messages-list">
            <div
              v-for="message in currentSession.messages"
              :key="message.id"
              :class="['message-item', message.role]"
            >
              <div class="message-header">
                <span class="role-tag">{{ getRoleLabel(message.role) }}</span>
                <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                <span v-if="message.tokensUsed" class="tokens">{{ message.tokensUsed }} tokens</span>
              </div>
              <div class="message-content">{{ message.content }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import {
  ChatDotRound,
  Message,
  Cpu,
  User,
  Search,
  Refresh,
  Connection
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatTime } from '@/utils/format'
import { AdminChatService, ChatService } from '@/api/chatApi'
import { handlePageResponse, getApiErrorMessage } from '@/utils/api'

defineOptions({ name: 'ChatManagement' })

// 响应式数据
const loading = ref(false)
const sessionList = ref([])
const detailDialogVisible = ref(false)
const currentSession = ref(null)

// 统计数据
const chatStats = ref({
  totalSessions: 0,
  totalMessages: 0,
  totalTokensUsed: 0,
  activeUsers: 0,
  todayMessages: 0
})

// 搜索表单
const searchForm = ref({
  userPhone: '',
  userNickname: '',
  startDate: '',
  endDate: ''
})

// 分页数据
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

// 获取角色标签
const getRoleLabel = (role: string) => {
  const labels = {
    user: '用户',
    assistant: 'AI助手',
    system: '系统'
  }
  return labels[role] || role
}

// 获取统计数据
const loadChatStats = async () => {
  try {
    const { data } = await AdminChatService.getChatStats()
    chatStats.value = data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取会话列表
const loadSessionList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.value.current,
      size: pagination.value.size,
      ...searchForm.value
    }

    const response = await AdminChatService.getChatSessions(params)
    const pageData = handlePageResponse(response)
    sessionList.value = pageData.records || []
    pagination.value.total = pageData.total || 0
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error))
    console.error('获取会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看会话详情
const viewSessionDetail = async (sessionId: number) => {
  try {
    const response = await AdminChatService.getChatSessionDetail(sessionId)
    const data = response.data || response
    currentSession.value = data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error))
    console.error('获取会话详情失败:', error)
  }
}

// 删除会话
const deleteSession = async (session: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除会话"${session.title}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await AdminChatService.deleteChatSession(session.id)
    ElMessage.success('删除成功')
    await loadSessionList()
    await loadChatStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(getApiErrorMessage(error))
      console.error('删除会话失败:', error)
    }
  }
}

// 测试AI连接
const testAiConnection = async () => {
  try {
    const { data } = await ChatService.testConnection()
    if (data) {
      ElMessage.success('AI连接正常')
    } else {
      ElMessage.warning('AI连接异常')
    }
  } catch (error) {
    ElMessage.error('AI连接测试失败')
    console.error('AI连接测试失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadSessionList()
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    userPhone: '',
    userNickname: '',
    startDate: '',
    endDate: ''
  }
  pagination.value.current = 1
  loadSessionList()
}

// 刷新数据
const refreshData = () => {
  loadSessionList()
  loadChatStats()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.value.size = size
  pagination.value.current = 1
  loadSessionList()
}

const handleCurrentChange = (current: number) => {
  pagination.value.current = current
  loadSessionList()
}

// 关闭详情对话框
const handleCloseDetail = () => {
  detailDialogVisible.value = false
  currentSession.value = null
}

// 初始化
onMounted(() => {
  loadSessionList()
  loadChatStats()
})
</script>

<style scoped lang="scss">
.chat-management {
  padding: 20px;

  .stats-cards {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        padding: 10px;

        .stat-icon {
          margin-right: 15px;
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 5px;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }

    .user-info {
      .user-id {
        font-size: 12px;
        color: #909399;
      }

      .user-nickname {
        font-weight: 500;
        color: #303133;
      }
    }

    .last-message {
      max-width: 300px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #606266;
    }

    .pagination-container {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }

  .session-detail {
    .session-info {
      margin-bottom: 20px;
    }

    .messages-container {
      h4 {
        margin-bottom: 15px;
        color: #303133;
      }

      .messages-list {
        max-height: 400px;
        overflow-y: auto;
        border: 1px solid #ebeef5;
        border-radius: 4px;
        padding: 10px;

        .message-item {
          margin-bottom: 15px;
          padding: 10px;
          border-radius: 8px;

          &.user {
            background-color: #f0f9ff;
            border-left: 4px solid #409eff;
          }

          &.assistant {
            background-color: #f0f9f0;
            border-left: 4px solid #67c23a;
          }

          &.system {
            background-color: #fdf6ec;
            border-left: 4px solid #e6a23c;
          }

          .message-header {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 8px;
            font-size: 12px;

            .role-tag {
              padding: 2px 8px;
              border-radius: 12px;
              background-color: #f4f4f5;
              color: #909399;
              font-weight: 500;
            }

            .message-time {
              color: #909399;
            }

            .tokens {
              color: #e6a23c;
              font-weight: 500;
            }
          }

          .message-content {
            line-height: 1.6;
            color: #303133;
            white-space: pre-wrap;
          }

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
}
</style>
