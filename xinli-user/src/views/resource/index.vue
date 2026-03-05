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
    <div class="flex flex-1 flex-col bg-slate-50/50 dark:bg-slate-900/50 relative overflow-hidden">
      <!-- 增加背景光晕效果 (Orb) -->
      <div class="absolute top-0 right-0 w-full h-full overflow-hidden pointer-events-none z-0">
        <div class="absolute -top-[5%] -left-[10%] w-[60%] h-[40%] rounded-full bg-teal-400/10 blur-[80px] mix-blend-multiply dark:bg-teal-900/20 dark:mix-blend-screen"></div>
        <div class="absolute top-[20%] -right-[10%] w-[50%] h-[50%] rounded-full bg-cyan-400/10 blur-[80px] mix-blend-multiply dark:bg-cyan-900/20 dark:mix-blend-screen"></div>
      </div>

      <!-- 头部 Banner -->
      <div class="relative z-10 px-5 pt-8 pb-10 bg-gradient-to-br from-teal-500/90 to-cyan-600/90 backdrop-blur-md shadow-lg shadow-teal-500/10 overflow-hidden rounded-b-[2.5rem]">
        <div class="absolute top-0 right-0 w-48 h-48 bg-white/10 rounded-full blur-3xl -mr-16 -mt-16 pointer-events-none"></div>
        <h1 class="text-2xl font-black text-white mb-2 tracking-tight drop-shadow-sm">探索心灵的角落</h1>
        <p class="text-white/90 text-[13px] leading-relaxed font-medium drop-shadow-sm max-w-[90%]">
          精选心理健康文章、放松音频与视频，帮助你找到内心的平静与力量。
        </p>
      </div>

      <!-- Tab 内容区（FmTabs 负责切换逻辑）-->
      <div class="flex-1 -mt-6 px-4 relative z-20">
        <FmTabs
          :model-value="activeTab"
          :list="tabList"
          class="bg-transparent"
          list-class="bg-white/80 dark:bg-slate-800/80 backdrop-blur-xl rounded-2xl shadow-sm border border-slate-200/50 dark:border-slate-700/50 mb-5 p-1.5"
          @update:model-value="onTabChange"
        >
          <!-- 每个 tab 共用同一个内容区，只有 active 时会触发数据加载 -->
          <template v-for="tab in tabList" :key="tab.value" #[tab.value]>
            <!-- 加载中 -->
            <FmLoading v-if="loading && resources.length === 0" type="wave" text="加载中..." />

            <!-- 空状态 -->
            <div v-else-if="resources.length === 0" class="flex justify-center items-center py-20 animate-fade-in-up">
              <div class="text-center">
                <div class="w-16 h-16 mx-auto mb-4 rounded-3xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center">
                  <FmIcon name="i-carbon:document" class="text-3xl text-slate-300 dark:text-slate-600" />
                </div>
                <p class="text-[14px] font-medium text-slate-500 dark:text-slate-400">
                  此时此刻，暂无资源
                </p>
              </div>
            </div>

            <!-- 资源卡片列表 -->
            <div v-else class="space-y-4 pb-8">
              <FmCard
                v-for="(item, index) in resources"
                :key="item.id"
                class="group bg-white/80 dark:bg-slate-800/80 backdrop-blur-sm rounded-3xl p-4 border border-slate-200/60 dark:border-slate-700/60 hover:border-teal-300/50 dark:hover:border-teal-700/50 shadow-sm shadow-slate-200/20 dark:shadow-none hover:shadow-md hover:shadow-teal-500/10 transition-all duration-300 cursor-pointer active:scale-[0.98] animate-fade-in-up !border-x-slate-200/60"
                :style="`animation-delay: ${index * 0.05}s`"
                @click="goDetail(item.id)"
              >
                <div class="flex items-start gap-4">
                  <!-- 类型图标 -->
                  <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-teal-400 to-cyan-500 flex items-center justify-center flex-shrink-0 shadow-sm shadow-teal-500/20 group-hover:scale-105 transition-transform duration-300">
                    <FmIcon :name="getTypeConfig(item.type).icon" class="text-2xl text-white drop-shadow-sm" />
                  </div>
                  <!-- 内容 -->
                  <div class="flex-1 min-w-0 py-0.5">
                    <h3 class="font-bold text-[15px] text-slate-800 dark:text-slate-200 mb-1.5 line-clamp-1 group-hover:text-teal-600 dark:group-hover:text-teal-400 transition-colors tracking-tight">
                      {{ item.title }}
                    </h3>
                    <p class="text-[12px] text-slate-500 dark:text-slate-400 line-clamp-2 leading-relaxed mb-3">
                      {{ item.description }}
                    </p>
                    <div class="flex items-center gap-2.5 flex-wrap">
                      <span :class="`text-[10px] px-2 py-0.5 rounded-md font-bold ${getTypeConfig(item.type).color}`">
                        {{ getTypeConfig(item.type).label }}
                      </span>
                      <span class="text-[11px] font-medium text-slate-400 flex items-center gap-1 bg-slate-50 dark:bg-slate-900/50 px-1.5 py-0.5 rounded-md">
                        <FmIcon name="i-carbon:view" class="text-[12px]" />
                        {{ item.viewCount }}
                      </span>
                      <!-- 标签 -->
                      <template v-if="item.tags">
                        <span
                          v-for="tag in item.tags.split(',').slice(0, 2)"
                          :key="tag"
                          class="text-[11px] font-semibold text-teal-600/80 dark:text-teal-400/80 bg-teal-50 dark:bg-teal-900/30 px-1.5 py-0.5 rounded-md"
                        >
                          #{{ tag.trim() }}
                        </span>
                      </template>
                    </div>
                  </div>
                </div>
              </FmCard>

              <!-- 加载更多 -->
              <div v-if="hasMore" class="text-center py-4">
                <FmButton variant="secondary" :disabled="loading" class="w-[60%] rounded-2xl text-[13px] font-bold tracking-wide bg-white/60 dark:bg-slate-800/60 backdrop-blur-sm border border-slate-200/50 dark:border-slate-700/50 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-all text-slate-600 dark:text-slate-300" @click="loadResources(activeTab, currentPage + 1, true)">
                  {{ loading ? '加载中...' : '加载更多' }}
                </FmButton>
              </div>
              <div v-else class="text-center py-6">
                <div class="inline-flex items-center justify-center space-x-2 text-[12px] text-slate-400 font-medium">
                  <span class="w-8 h-[1px] bg-slate-200 dark:bg-slate-700"></span>
                  <span>已显示全部内容</span>
                  <span class="w-8 h-[1px] bg-slate-200 dark:bg-slate-700"></span>
                </div>
              </div>
            </div>
          </template>
        </FmTabs>
      </div>
    </div>
  </FmPageLayout>
</template>
