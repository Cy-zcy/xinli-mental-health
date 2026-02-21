<template>
  <div class="forum-management">
    <!-- 搜索和筛选 -->
    <ElCard shadow="never" class="search-card">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="关键词">
          <ElInput
            v-model="searchForm.keyword"
            placeholder="搜索帖子标题或内容"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem label="分类">
          <ElSelect v-model="searchForm.category" placeholder="选择分类" clearable style="width: 120px">
            <ElOption label="全部" value="" />
            <ElOption label="情感倾诉" value="emotion" />
            <ElOption label="经验分享" value="experience" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px">
            <ElOption label="全部" :value="null" />
            <ElOption label="正常" :value="1" />
            <ElOption label="待审核" :value="0" />
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

    <!-- 帖子列表 -->
    <ElCard shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>帖子列表</span>
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
        :data="postList"
        stripe
        style="width: 100%"
      >
        <ElTableColumn prop="id" label="ID" width="80" />
        <ElTableColumn prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="post-title" @click="viewPostDetail(row.id)">
              {{ row.title }}
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn label="分类" width="100">
          <template #default="{ row }">
            <ElTag :type="row.category === 'emotion' ? 'primary' : 'success'" size="small">
              {{ row.category === 'emotion' ? '情感倾诉' : '经验分享' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="统计" width="120">
          <template #default="{ row }">
            <div class="post-stats">
              <span>👍 {{ row.likeCount || 0 }}</span>
              <span>👁 {{ row.viewCount || 0 }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn label="状态" width="100">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '正常' : '待审核' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <ElButton size="small" @click="viewPostDetail(row.id)">
              详情
            </ElButton>
            <ElButton
              v-if="row.status === 0"
              size="small"
              type="success"
              @click="approvePost(row)"
            >
              通过
            </ElButton>
            <ElButton
              size="small"
              type="danger"
              @click="deletePost(row)"
            >
              删除
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

    <!-- 帖子详情对话框 -->
    <ElDialog
      v-model="detailDialogVisible"
      title="帖子详情"
      width="800px"
      :before-close="handleCloseDetail"
    >
      <div v-if="currentPost" class="post-detail">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="帖子ID">{{ currentPost.id }}</ElDescriptionsItem>
          <ElDescriptionsItem label="标题">{{ currentPost.title }}</ElDescriptionsItem>
          <ElDescriptionsItem label="分类">
            <ElTag :type="currentPost.category === 'emotion' ? 'primary' : 'success'">
              {{ currentPost.category === 'emotion' ? '情感倾诉' : '经验分享' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="currentPost.status === 1 ? 'success' : 'warning'">
              {{ currentPost.status === 1 ? '正常' : '待审核' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="作者">{{ currentPost.authorNickname || '未知' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="发布时间">{{ formatTime(currentPost.createdAt) }}</ElDescriptionsItem>
          <ElDescriptionsItem label="点赞数">{{ currentPost.likeCount || 0 }}</ElDescriptionsItem>
          <ElDescriptionsItem label="浏览数">{{ currentPost.viewCount || 0 }}</ElDescriptionsItem>
        </ElDescriptions>
        
        <div class="post-content">
          <h4>内容：</h4>
          <div class="content-text">{{ currentPost.content }}</div>
        </div>
        
        <div v-if="currentPost.comments && currentPost.comments.length > 0" class="post-comments">
          <h4>评论 ({{ currentPost.comments.length }})：</h4>
          <div v-for="comment in currentPost.comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">{{ comment.userNickname || '匿名' }}</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
        <ElButton v-if="currentPost && currentPost.status === 0" type="success" @click="approveCurrentPost">
          审核通过
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatTime } from '@/utils/format'
import { AdminForumService } from '@/api/forumApi'
import { handlePageResponse, getApiErrorMessage } from '@/utils/api'

defineOptions({ name: 'ForumManagement' })

// 响应式数据
const loading = ref(false)
const postList = ref([])
const detailDialogVisible = ref(false)
const currentPost = ref(null)

const searchForm = ref({
  keyword: '',
  category: '',
  status: null
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 获取帖子列表
const fetchPostList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      keyword: searchForm.value.keyword || undefined,
      category: searchForm.value.category || undefined,
      status: searchForm.value.status
    }

    console.log('发送帖子列表请求，参数:', params)
    const response = await AdminForumService.getPostList(params)
    console.log('帖子列表API响应:', response)

    const pageData = handlePageResponse(response)
    postList.value = pageData.records || []
    pagination.value.total = pageData.total || 0
    console.log('设置帖子列表数据:', postList.value)
    console.log('设置分页总数:', pagination.value.total)
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    ElMessage.error(getApiErrorMessage(error))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  fetchPostList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    keyword: '',
    category: '',
    status: null
  }
  pagination.value.page = 1
  fetchPostList()
}

// 刷新数据
const refreshData = () => {
  fetchPostList()
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.value.size = size
  fetchPostList()
}

const handleCurrentChange = (page: number) => {
  pagination.value.page = page
  fetchPostList()
}

// 查看帖子详情
const viewPostDetail = async (postId: number) => {
  try {
    const response = await AdminForumService.getPostDetail(postId)
    if (response) {
      currentPost.value = response
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
  }
}

// 审核通过帖子
const approvePost = async (post: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要审核通过帖子"${post.title}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await AdminForumService.updatePostStatus(post.id, { status: 1 })
    
    ElMessage.success('审核通过成功')
    fetchPostList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error('审核通过失败')
    }
  }
}

// 审核通过当前帖子
const approveCurrentPost = async () => {
  if (currentPost.value) {
    await approvePost(currentPost.value)
    detailDialogVisible.value = false
  }
}

// 删除帖子
const deletePost = async (post: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除帖子"${post.title}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await AdminForumService.deletePost(post.id)
    
    ElMessage.success('删除成功')
    fetchPostList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 关闭详情对话框
const handleCloseDetail = () => {
  detailDialogVisible.value = false
  currentPost.value = null
}

// 初始化
onMounted(() => {
  fetchPostList()
})
</script>

<style lang="scss" scoped>
.forum-management {
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
    
    .post-title {
      cursor: pointer;
      color: var(--el-color-primary);
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    .post-stats {
      display: flex;
      flex-direction: column;
      gap: 4px;
      font-size: 12px;
      color: var(--el-text-color-regular);
    }
    
    .pagination-container {
      margin-top: 20px;
      text-align: right;
    }
  }
  
  .post-detail {
    .post-content {
      margin: 20px 0;
      
      h4 {
        margin-bottom: 10px;
        color: var(--el-text-color-primary);
      }
      
      .content-text {
        padding: 15px;
        background-color: var(--el-fill-color-lighter);
        border-radius: 6px;
        line-height: 1.6;
        white-space: pre-wrap;
      }
    }
    
    .post-comments {
      margin-top: 20px;
      
      h4 {
        margin-bottom: 15px;
        color: var(--el-text-color-primary);
      }
      
      .comment-item {
        padding: 12px;
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 6px;
        margin-bottom: 10px;
        
        .comment-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          
          .comment-author {
            font-weight: 500;
            color: var(--el-color-primary);
          }
          
          .comment-time {
            font-size: 12px;
            color: var(--el-text-color-regular);
          }
        }
        
        .comment-content {
          line-height: 1.5;
          color: var(--el-text-color-primary);
        }
      }
    }
  }
}
</style>
