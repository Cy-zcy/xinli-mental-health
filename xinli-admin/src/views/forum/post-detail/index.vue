<!-- 帖子详情页面 -->
<template>
  <div class="post-detail-page">
    <ElCard shadow="never" class="detail-card">
      <!-- 头部操作栏 -->
      <template #header>
        <div class="header-actions">
          <ElButton @click="goBack">
            <ElIcon><ArrowLeft /></ElIcon>
            返回列表
          </ElButton>
          <div class="actions">
            <ElButton 
              :type="postDetail?.status === 1 ? 'warning' : 'success'"
              @click="togglePostStatus"
            >
              {{ postDetail?.status === 1 ? '删除帖子' : '恢复帖子' }}
            </ElButton>
          </div>
        </div>
      </template>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <ElSkeleton :rows="8" animated />
      </div>

      <!-- 帖子内容 -->
      <div v-else-if="postDetail" class="post-content">
        <!-- 帖子标题 -->
        <div class="post-header">
          <h1 class="post-title">{{ postDetail.title }}</h1>
          <div class="post-meta">
            <div class="meta-left">
              <ElTag 
                :type="postDetail.category === 'emotion' ? 'primary' : 'success'" 
                size="small"
              >
                {{ postDetail.category === 'emotion' ? '情感倾诉' : '经验分享' }}
              </ElTag>
              <ElTag 
                :type="postDetail.status === 1 ? 'success' : 'danger'" 
                size="small"
              >
                {{ postDetail.status === 1 ? '正常' : '已删除' }}
              </ElTag>
            </div>
            <div class="meta-right">
              <span class="author">作者: {{ postDetail.user?.nickname || '匿名用户' }}</span>
              <span class="time">发布时间: {{ formatTime(postDetail.createdAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 帖子统计 -->
        <div class="post-stats">
          <div class="stat-item">
            <ElIcon><View /></ElIcon>
            <span>{{ postDetail.viewCount || 0 }} 浏览</span>
          </div>
          <div class="stat-item">
            <ElIcon><Star /></ElIcon>
            <span>{{ postDetail.likeCount || 0 }} 点赞</span>
          </div>
        </div>

        <!-- 帖子正文 -->
        <div class="post-body">
          <div class="content-text">
            {{ postDetail.content }}
          </div>
        </div>

        <!-- 用户信息 -->
        <div class="author-info" v-if="postDetail.user">
          <ElCard shadow="hover" class="author-card">
            <div class="author-content">
              <div class="author-avatar">
                <ElAvatar :size="50" :src="postDetail.user.avatar">
                  {{ postDetail.user.nickname?.charAt(0) || 'U' }}
                </ElAvatar>
              </div>
              <div class="author-details">
                <div class="author-name">{{ postDetail.user.nickname || '匿名用户' }}</div>
                <div class="author-phone">{{ postDetail.user.phone || '未知' }}</div>
              </div>
            </div>
          </ElCard>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="error-container">
        <ElEmpty description="帖子不存在或已被删除" />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ArrowLeft, View, Star } from '@element-plus/icons-vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useRouter, useRoute } from 'vue-router'
  import { AdminForumService } from '@/api/forumApi'

  defineOptions({ name: 'PostDetail' })

  const router = useRouter()
  const route = useRoute()

  // 响应式数据
  const loading = ref(true)
  const postDetail = ref(null)

  /**
   * 获取帖子详情
   */
  const fetchPostDetail = async () => {
    const postId = route.params.id || route.query.id
    if (!postId) {
      ElMessage.error('帖子ID不存在')
      goBack()
      return
    }

    try {
      loading.value = true
      const response = await AdminForumService.getPostDetail(Number(postId))
      if (response.data) {
        postDetail.value = response.data
      } else {
        ElMessage.error('帖子不存在')
      }
    } catch (error) {
      console.error('获取帖子详情失败:', error)
      ElMessage.error('获取帖子详情失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 切换帖子状态
   */
  const togglePostStatus = async () => {
    if (!postDetail.value) return

    const newStatus = postDetail.value.status === 1 ? 0 : 1
    const action = newStatus === 0 ? '删除' : '恢复'

    try {
      await ElMessageBox.confirm(`确定要${action}该帖子吗？`, `${action}帖子`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await AdminForumService.updatePostStatus(postDetail.value.id, { status: newStatus })
      ElMessage.success(`${action}成功`)
      
      // 更新本地状态
      postDetail.value.status = newStatus
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(`${action}失败`)
      }
    }
  }

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    return new Date(time).toLocaleString('zh-CN')
  }

  /**
   * 返回列表
   */
  const goBack = () => {
    router.push('/forum/management')
  }

  // 页面加载时获取数据
  onMounted(() => {
    fetchPostDetail()
  })
</script>

<style lang="scss" scoped>
  .post-detail-page {
    padding: 20px;
    
    .detail-card {
      .header-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .loading-container {
        padding: 20px;
      }
      
      .post-content {
        .post-header {
          margin-bottom: 20px;
          
          .post-title {
            font-size: 24px;
            font-weight: bold;
            color: var(--el-text-color-primary);
            margin: 0 0 12px 0;
            line-height: 1.4;
          }
          
          .post-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
            
            .meta-left {
              display: flex;
              gap: 8px;
            }
            
            .meta-right {
              display: flex;
              gap: 16px;
              font-size: 14px;
              color: var(--el-text-color-regular);
            }
          }
        }
        
        .post-stats {
          display: flex;
          gap: 24px;
          margin-bottom: 24px;
          padding: 16px;
          background: var(--el-bg-color-page);
          border-radius: 8px;
          
          .stat-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: var(--el-text-color-regular);
            
            .el-icon {
              font-size: 16px;
            }
          }
        }
        
        .post-body {
          margin-bottom: 32px;
          
          .content-text {
            font-size: 16px;
            line-height: 1.8;
            color: var(--el-text-color-primary);
            white-space: pre-wrap;
            word-break: break-word;
          }
        }
        
        .author-info {
          .author-card {
            .author-content {
              display: flex;
              align-items: center;
              gap: 16px;
              
              .author-details {
                .author-name {
                  font-size: 16px;
                  font-weight: 500;
                  color: var(--el-text-color-primary);
                  margin-bottom: 4px;
                }
                
                .author-phone {
                  font-size: 14px;
                  color: var(--el-text-color-regular);
                }
              }
            }
          }
        }
      }
      
      .error-container {
        padding: 40px;
        text-align: center;
      }
    }
  }
  
  @media (max-width: 768px) {
    .post-detail-page {
      padding: 10px;
      
      .post-content {
        .post-header {
          .post-meta {
            flex-direction: column;
            align-items: flex-start;
            
            .meta-right {
              flex-direction: column;
              gap: 8px;
            }
          }
        }
        
        .post-stats {
          flex-direction: column;
          gap: 12px;
        }
      }
    }
  }
</style>
