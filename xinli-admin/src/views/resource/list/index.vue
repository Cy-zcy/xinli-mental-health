<template>
  <div class="page-content resource-list">
    <ElRow justify="space-between" :gutter="10" style="margin-bottom: 20px;">
      <ElCol :lg="6" :md="6" :sm="14" :xs="16">
        <ElInput
          v-model="searchVal"
          :prefix-icon="Search"
          clearable
          placeholder="输入资源标题查询"
          @keyup.enter="searchResource"
          @clear="searchResource"
        />
      </ElCol>
      <ElCol :lg="12" :md="12" :sm="0" :xs="0">
        <div class="custom-segmented">
          <ElSegmented v-model="typeVal" :options="typeOptions" @change="searchResource" />
        </div>
      </ElCol>
      <ElCol :lg="6" :md="6" :sm="10" :xs="6" style="display: flex; justify-content: end">
        <ElButton type="primary" @click="toAddResource" v-auth="'add'">新增干预资源</ElButton>
      </ElCol>
    </ElRow>

    <div class="list">
      <!-- 借用文章列表页的设计使用卡片展示 -->
      <div class="offset" v-loading="isLoading">
        <div class="item" v-for="item in resourceList" :key="item.id" @click="toEdit(item)">
          <div class="top">
            <ElImage class="cover" :src="item.coverUrl" lazy fit="cover">
              <template #error>
                <div class="image-slot">
                  <ElIcon><icon-picture /></ElIcon>
                </div>
              </template>
            </ElImage>
            <span class="type">{{ getTypeLabel(item.type) }}</span>
            <span class="status" :class="item.isPublished === 1 ? 'published' : 'draft'">
              {{ item.isPublished === 1 ? '已上架' : '已下架' }}
            </span>
          </div>
          <div class="bottom">
            <h2>{{ item.title }}</h2>
            <div class="info">
              <div class="text">
                <i class="iconfont-sys">&#xe6f7;</i>
                <span>{{ useDateFormat(item.createdAt, 'YYYY-MM-DD').value }}</span>
                <div class="line"></div>
                <i class="iconfont-sys">&#xe689;</i>
                <span>{{ item.viewCount }}</span>
              </div>
              <div class="actions">
                <ElButton
                  size="small"
                  :type="item.isPublished === 1 ? 'warning' : 'success'"
                  @click.stop="toggleStatus(item)"
                >
                  {{ item.isPublished === 1 ? '下架' : '上架' }}
                </ElButton>
                <ElButton type="danger" size="small" @click.stop="handleDelete(item)">删除</ElButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div style="margin-top: 16vh" v-if="showEmpty">
      <ElEmpty description="暂无干预资源数据" />
    </div>

    <div style="display: flex; justify-content: center; margin-top: 20px">
      <ElPagination
        size="default"
        background
        v-model:current-page="currentPage"
        :page-size="pageSize"
        layout="prev, pager, next, total, jumper"
        :total="total"
        :hide-on-single-page="true"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Picture as IconPicture } from '@element-plus/icons-vue'
import { useDateFormat } from '@vueuse/core'
import { RoutesAlias } from '@/router/routesAlias'
import { ResourceService, ResourceItem } from '@/api/resourceApi'
import { useCommon } from '@/composables/useCommon'

defineOptions({ name: 'ResourceList' })

const router = useRouter()

const typeVal = ref('All')
const typeOptions = [
  { label: '全部', value: 'All' },
  { label: '文章', value: 'article' },
  { label: '音频', value: 'audio' },
  { label: '视频', value: 'video' }
]

const searchVal = ref('')
const resourceList = ref<ResourceItem[]>([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const isLoading = ref(true)

const showEmpty = computed(() => {
  return resourceList.value.length === 0 && !isLoading.value
})

onMounted(() => {
  getResourceList()
})

const getTypeLabel = (type: string) => {
  const t = typeOptions.find(o => o.value === type)
  return t ? t.label : type
}

const searchResource = () => {
  currentPage.value = 1
  getResourceList({ backTop: true })
}

const getResourceList = async (options = { backTop: false }) => {
  isLoading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (searchVal.value) {
      params.keyword = searchVal.value
    }
    
    if (typeVal.value !== 'All') {
      params.type = typeVal.value
    }

    const { records, total: totalCount } = await ResourceService.getResourceList(params)
    resourceList.value = records || []
    total.value = totalCount || 0
  } catch (error) {
    ElMessage.error('获取资源列表失败')
  } finally {
    isLoading.value = false
    if (options.backTop) {
      useCommon().scrollToTop()
    }
  }
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  getResourceList({ backTop: true })
}

const toEdit = (item: ResourceItem) => {
  router.push({
    path: RoutesAlias.ResourcePublish,
    query: { id: item.id }
  })
}

const toAddResource = () => {
  router.push({
    path: RoutesAlias.ResourcePublish
  })
}

const toggleStatus = async (item: ResourceItem) => {
  const newStatus = item.isPublished === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(`确认要${actionText}该资源吗？`, '提示', {
      type: 'warning'
    })
    
    await ResourceService.updateResourceStatus(item.id, newStatus)
    ElMessage.success(`${actionText}成功`)
    item.isPublished = newStatus
  } catch (e) {
    // cancelled or error
  }
}

const handleDelete = async (item: ResourceItem) => {
  try {
    await ElMessageBox.confirm('确认删除该干预资源？删除后不可恢复。', '警告', {
      type: 'error',
      confirmButtonText: '删除'
    })
    
    await ResourceService.deleteResource(item.id)
    ElMessage.success('删除成功')
    getResourceList()
  } catch (e) {
    // cancelled or error
  }
}
</script>

<style lang="scss" scoped>
.resource-list {
  .custom-segmented .el-segmented {
    height: 40px;
    padding: 6px;
    --el-border-radius-base: 8px;
  }

  .list {
    margin-top: 20px;

    .offset {
      display: flex;
      flex-wrap: wrap;
      width: calc(100% + 20px);

      .item {
        box-sizing: border-box;
        width: calc(20% - 20px);
        margin: 0 20px 20px 0;
        cursor: pointer;
        border: 1px solid var(--art-border-color);
        border-radius: calc(var(--custom-radius) / 2 + 2px);
        overflow: hidden;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 8px 16px rgba(0,0,0,0.1);
          .bottom .info .actions .el-button {
            opacity: 1;
          }
        }

        .top {
          position: relative;
          aspect-ratio: 16/9.5;

          .cover {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
            object-fit: cover;
            background: var(--art-gray-200);

            .image-slot {
              font-size: 26px;
              color: var(--art-gray-400);
            }
          }

          .type {
            position: absolute;
            top: 10px;
            right: 10px;
            padding: 4px 8px;
            font-size: 12px;
            color: #fff;
            background: rgba(0, 0, 0, 0.6);
            border-radius: 4px;
          }

          .status {
            position: absolute;
            top: 10px;
            left: 10px;
            padding: 4px 8px;
            font-size: 12px;
            color: #fff;
            border-radius: 4px;
            &.published {
              background: var(--el-color-success);
            }
            &.draft {
              background: var(--el-color-warning);
            }
          }
        }

        .bottom {
          padding: 12px;

          h2 {
            font-size: 16px;
            font-weight: 500;
            color: var(--el-text-color-primary);
            margin: 0 0 10px 0;
            @include ellipsis();
          }

          .info {
            display: flex;
            justify-content: space-between;
            align-items: center;
            height: 28px;

            .text {
              display: flex;
              align-items: center;
              color: var(--art-text-gray-600);

              i {
                margin-right: 4px;
                font-size: 14px;
              }

              span {
                font-size: 13px;
                color: var(--art-gray-600);
              }

              .line {
                width: 1px;
                height: 12px;
                margin: 0 10px;
                background-color: var(--art-border-dashed-color);
              }
            }

            .actions {
              display: flex;
              gap: 8px;
              .el-button {
                opacity: 0;
                transition: opacity 0.3s;
              }
            }
          }
        }
      }
    }
  }
}

/* 响应式样式保持与模板一致 */
@media only screen and (max-width: $device-notebook) {
  .resource-list .list .offset .item { width: calc(25% - 20px); }
}
@media only screen and (max-width: $device-ipad-pro) {
  .resource-list .list .offset .item { width: calc(33.333% - 20px); }
}
@media only screen and (max-width: $device-ipad) {
  .resource-list .list .offset .item { width: calc(50% - 20px); }
}
@media only screen and (max-width: $device-phone) {
  .resource-list .list .offset .item { width: calc(100% - 20px); }
}
</style>
