<template>
  <div class="user-management">
    <!-- 搜索和筛选 -->
    <ElCard shadow="never" class="search-card">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="关键词">
          <ElInput
            v-model="searchForm.keyword"
            placeholder="搜索用户昵称或手机号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px">
            <ElOption label="全部" :value="null" />
            <ElOption label="正常" :value="1" />
            <ElOption label="禁用" :value="0" />
          </ElSelect>
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

    <!-- 用户列表 -->
    <ElCard shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="header-actions">
            <ElButton @click="refreshData">
              <ElIcon><Refresh /></ElIcon>
              刷新
            </ElButton>
          </div>
        </div>
      </template>

      <ElTable
        v-loading="loading"
        :data="userList"
        stripe
        style="width: 100%"
      >
        <ElTableColumn prop="id" label="ID" width="80" />
        <ElTableColumn label="头像" width="80">
          <template #default="{ row }">
            <ElAvatar :src="row.avatar" :size="40">
              {{ row.nickname?.charAt(0) }}
            </ElAvatar>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="nickname" label="昵称" />
        <ElTableColumn prop="phone" label="手机号" />
        <ElTableColumn label="健康分" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="getScoreTagType(row.healthScore)" effect="dark">
              {{ row.healthScore !== undefined ? row.healthScore : 100 }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="状态" width="100">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <ElButton size="small" @click="viewUserDetail(row.id)">
              详情
            </ElButton>
            <ElButton size="small" type="primary" plain @click="viewHealthRecords(row)">
              健康流水
            </ElButton>
            <ElButton
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleUserStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <!-- 分页 -->
      <div class="pagination-container">
        <ElPagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </ElCard>

    <!-- 用户详情对话框 -->
    <ElDialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="600px"
      :before-close="handleCloseDetail"
    >
      <div v-if="currentUser" class="user-detail">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="用户ID">{{ currentUser.id }}</ElDescriptionsItem>
          <ElDescriptionsItem label="昵称">{{ currentUser.nickname }}</ElDescriptionsItem>
          <ElDescriptionsItem label="手机号">{{ currentUser.phone }}</ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="注册时间">{{ formatTime(currentUser.createdAt) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="最后更新">{{ formatTime(currentUser.updatedAt) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="发帖数量">{{ currentUser.postCount || 0 }}</ElDescriptionsItem>
          <ElDescriptionsItem label="评论数量">{{ currentUser.commentCount || 0 }}</ElDescriptionsItem>
        </ElDescriptions>
      </div>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
      </template>
    </ElDialog>

    <!-- 健康分变动流水对话框 -->
    <ElDialog
      v-model="healthDialogVisible"
      :title="`${currentHealthUser?.nickname || '用户'} 的健康分流水`"
      width="650px"
    >
      <ElTable v-loading="healthLoading" :data="healthRecords" border size="small" style="width: 100%">
        <ElTableColumn prop="scoreChange" label="变动分数" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.scoreChange > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
              {{ row.scoreChange > 0 ? '+' : '' }}{{ row.scoreChange }}
            </span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="reason" label="变动原因" min-width="150" show-overflow-tooltip />
        <ElTableColumn prop="type" label="变动类型" width="120" align="center" />
        <ElTableColumn prop="createdAt" label="时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="pagination-container" style="margin-top: 15px; text-align: center;">
        <ElPagination
          v-model:current-page="healthPagination.page"
          :page-size="healthPagination.size"
          :total="healthPagination.total"
          layout="total, prev, pager, next"
          background
          size="small"
          @current-change="loadHealthRecords"
        />
      </div>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatTime } from '@/utils/format'
import { AdminUserService } from '@/api/usersApi'
import { handlePageResponse, getApiErrorMessage } from '@/utils/api'

defineOptions({ name: 'UserManagement' })

// 响应式数据
const loading = ref(false)
const userList = ref([])
const detailDialogVisible = ref(false)
const currentUser = ref(null)

const searchForm = ref({
  keyword: '',
  status: null
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 健康分流水专用的响应式数据
const healthDialogVisible = ref(false)
const healthLoading = ref(false)
const healthRecords = ref<any[]>([])
const currentHealthUser = ref<any>(null)
const healthPagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 工具方法：获取健康分的标签颜色
const getScoreTagType = (score: number) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      keyword: searchForm.value.keyword || undefined,
      status: searchForm.value.status
    }

    const response = await AdminUserService.getUserList(params)
    const pageData = handlePageResponse(response)
    userList.value = pageData.records || []
    pagination.value.total = pageData.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error(getApiErrorMessage(error))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    keyword: '',
    status: null
  }
  pagination.value.page = 1
  fetchUserList()
}

// 刷新数据
const refreshData = () => {
  fetchUserList()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.value.size = size
  fetchUserList()
}

const handleCurrentChange = (page: number) => {
  pagination.value.page = page
  fetchUserList()
}

// 查看用户详情
const viewUserDetail = async (userId: number) => {
  try {
    const response = await AdminUserService.getUserDetail(userId)
    if (response.data) {
      currentUser.value = response.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

// 切换用户状态
const toggleUserStatus = async (user: any) => {
  const action = user.status === 1 ? '禁用' : '启用'
  const newStatus = user.status === 1 ? 0 : 1

  try {
    await ElMessageBox.confirm(
      `确定要${action}用户"${user.nickname}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await AdminUserService.updateUserStatus(user.id, { status: newStatus })

    ElMessage.success(`${action}成功`)
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新用户状态失败:', error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 关闭详情对话框
const handleCloseDetail = () => {
  detailDialogVisible.value = false
  currentUser.value = null
}

// ================== 健康分变动流水能力 ==================
// 查看健康流水详情
const viewHealthRecords = (user: any) => {
  currentHealthUser.value = user
  healthPagination.value.page = 1
  healthDialogVisible.value = true
  loadHealthRecords()
}

// 获取健康流水数据
const loadHealthRecords = async () => {
  if (!currentHealthUser.value) return
  healthLoading.value = true
  try {
    const res = await AdminUserService.getUserHealthScoreRecords(
      currentHealthUser.value.id,
      healthPagination.value.page,
      healthPagination.value.size
    )
    if (res && res.data) {
      healthRecords.value = res.data.records || []
      healthPagination.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取健康分流水失败', error)
    ElMessage.error('获取健康分流水记录失败')
  } finally {
    healthLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchUserList()
})
</script>

<style lang="scss" scoped>
.user-management {
  padding: 20px;
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .pagination-container {
      margin-top: 20px;
      text-align: right;
    }
  }
  
  .user-detail {
    .el-descriptions {
      margin-top: 20px;
    }
  }
}
</style>
