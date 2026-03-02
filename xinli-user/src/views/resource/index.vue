<script setup lang="ts">
import { toast } from 'vue-sonner'
import { resourceApi } from '@/api/modules'
import type { ResourceItem } from '@/api/modules/resource'

definePage({
  meta: {
    title: '心理资源',
    auth: false,
  },
})

const router = useRouter()

// Tab 配置
const tabList = [
  { label: '全部', value: 'all', icon: 'i-carbon:apps' },
  { label: '文章', value: 'article', icon: 'i-carbon:document' },
  { label: '音频', value: 'audio', icon: 'i-carbon:music' },
  { label: '视频', value: 'video', icon: 'i-carbon:video' },
]
const activeTab = ref<string>('all')

const loading = ref(false)
const resources = ref<ResourceItem[]>([])
const currentPage = ref(1)
const hasMore = ref(true)

async function loadResources(tab: string, page = 1, append = false) {
  try {
    loading.value = true
    const type = tab === 'all' ? undefined : tab
    const res = await resourceApi.getResourceList(page, 10, type)
    const list = res?.records || []
    if (append) {
      resources.value.push(...list)
    }
    else {
      resources.value = list
    }
    currentPage.value = page
    hasMore.value = list.length === 10
  }
  catch (e: any) {
    toast.error('加载失败', { description: e?.message || '无法获取资源列表' })
  }
  finally {
    loading.value = false
  }
}

function onTabChange(val: string | number) {
  activeTab.value = String(val)
  loadResources(String(val))
}

function goDetail(id: number) {
  router.push(`/resource/${id}`)
}

// 类型图标和颜色映射
function getTypeConfig(type: string) {
  switch (type) {
    case 'article': return { icon: 'i-carbon:document', label: '文章', color: 'bg-blue-100 text-blue-700' }
    case 'audio': return { icon: 'i-carbon:music', label: '音频', color: 'bg-purple-100 text-purple-700' }
    case 'video': return { icon: 'i-carbon:video', label: '视频', color: 'bg-green-100 text-green-700' }
    default: return { icon: 'i-carbon:document', label: '资源', color: 'bg-gray-100 text-gray-700' }
  }
}

onMounted(() => loadResources('all'))
</script>

<template>
  <FmPageLayout title="心理资源" tabbar>
    <div class="flex flex-1 flex-col bg-gray-50 dark:bg-gray-900">
      <!-- 头部 Banner -->
      <div class="bg-gradient-to-br from-teal-500 to-cyan-600 px-5 pt-4 pb-8 text-white">
        <p class="text-white/80 text-sm leading-relaxed">
          精选心理健康文章、放松音频，帮助你找到内心的平静与力量。
        </p>
      </div>

      <!-- Tab 内容区（FmTabs 负责切换逻辑）-->
      <div class="flex-1 -mt-4 px-4">
        <FmTabs
          :model-value="activeTab"
          :list="tabList"
          class="bg-transparent"
          list-class="bg-white dark:bg-gray-800 rounded-xl shadow-sm mb-4 p-1"
          @update:model-value="onTabChange"
        >
          <!-- 每个 tab 共用同一个内容区，只有 active 时会触发数据加载 -->
          <template v-for="tab in tabList" :key="tab.value" #[tab.value]>
            <!-- 加载中 -->
            <FmLoading v-if="loading && resources.length === 0" type="wave" text="加载中..." />

            <!-- 空状态 -->
            <div v-else-if="resources.length === 0" class="flex justify-center items-center py-16">
              <div class="text-center text-gray-400">
                <FmIcon name="i-carbon:document" class="text-16 mb-3" />
                <p class="text-sm">
                  暂无相关资源
                </p>
              </div>
            </div>

            <!-- 资源卡片列表 -->
            <div v-else class="space-y-3 pb-6">
              <FmCard
                v-for="item in resources"
                :key="item.id"
                class="hover:shadow-md hover:border-teal-200 transition-all duration-200 cursor-pointer"
                @click="goDetail(item.id)"
              >
                <div class="flex items-start gap-4">
                  <!-- 类型图标 -->
                  <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-teal-400 to-cyan-500 flex items-center justify-center flex-shrink-0 shadow-sm">
                    <FmIcon :name="getTypeConfig(item.type).icon" class="text-6 text-white" />
                  </div>
                  <!-- 内容 -->
                  <div class="flex-1 min-w-0">
                    <h3 class="font-bold text-sm text-gray-900 dark:text-white mb-1 line-clamp-1">
                      {{ item.title }}
                    </h3>
                    <p class="text-xs text-gray-500 dark:text-gray-400 line-clamp-2 leading-relaxed mb-2">
                      {{ item.description }}
                    </p>
                    <div class="flex items-center gap-2">
                      <span :class="`text-xs px-2 py-0.5 rounded-full font-medium ${getTypeConfig(item.type).color}`">
                        {{ getTypeConfig(item.type).label }}
                      </span>
                      <span class="text-xs text-gray-400 flex items-center gap-1">
                        <FmIcon name="i-carbon:view" class="text-3.5" />
                        {{ item.viewCount }}
                      </span>
                      <!-- 标签 -->
                      <template v-if="item.tags">
                        <span
                          v-for="tag in item.tags.split(',').slice(0, 2)"
                          :key="tag"
                          class="text-xs text-teal-600 dark:text-teal-400"
                        >
                          #{{ tag.trim() }}
                        </span>
                      </template>
                    </div>
                  </div>
                  <FmIcon name="i-carbon:chevron-right" class="text-4 text-gray-400 flex-shrink-0 mt-1" />
                </div>
              </FmCard>

              <!-- 加载更多 -->
              <div v-if="hasMore" class="text-center py-2">
                <FmButton variant="outline" :disabled="loading" class="w-full rounded-xl" @click="loadResources(activeTab, currentPage + 1, true)">
                  {{ loading ? '加载中...' : '加载更多' }}
                </FmButton>
              </div>
              <div v-else class="text-center py-3 text-xs text-gray-400">
                已显示全部资源
              </div>
            </div>
          </template>
        </FmTabs>
      </div>
    </div>
  </FmPageLayout>
</template>
