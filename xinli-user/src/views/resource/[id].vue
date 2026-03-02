<script setup lang="ts">
import { toast } from 'vue-sonner'
import { resourceApi } from '@/api/modules'
import type { ResourceDetail } from '@/api/modules/resource'

definePage({
  meta: {
    title: '资源详情',
    auth: false,
  },
})

const route = useRoute()
const router = useRouter()
const resourceId = Number(route.params.id)

const loading = ref(false)
const resource = ref<ResourceDetail | null>(null)

async function loadDetail() {
  try {
    loading.value = true
    resource.value = await resourceApi.getResourceDetail(resourceId)
  }
  catch (e: any) {
    toast.error('加载失败', { description: e?.message || '资源不存在或已下架' })
    router.back()
  }
  finally {
    loading.value = false
  }
}

function getTypeLabel(type: string) {
  return { article: '文章', audio: '音频', video: '视频' }[type] ?? '资源'
}

function getTypeGradient(type: string) {
  return {
    article: 'from-blue-500 to-indigo-600',
    audio: 'from-purple-500 to-violet-600',
    video: 'from-green-500 to-teal-600',
  }[type] ?? 'from-teal-500 to-cyan-600'
}

onMounted(() => loadDetail())
</script>

<template>
  <FmPageLayout :navbar="{ back: true }" :tabbar="false" @back="router.back()">
    <!-- 加载中 -->
    <FmLoading v-if="loading" type="wave" text="加载中..." />

    <template v-else-if="resource">
      <!-- 顶部 Banner -->
      <div :class="`bg-gradient-to-br ${getTypeGradient(resource.type)} px-5 pt-4 pb-10 text-white`">
        <div class="flex items-center gap-2 mb-3">
          <span class="text-xs bg-white/20 px-3 py-0.5 rounded-full font-medium">
            {{ getTypeLabel(resource.type) }}
          </span>
          <span class="text-xs text-white/70 flex items-center gap-1">
            <FmIcon name="i-carbon:view" class="text-3.5" />
            {{ resource.viewCount }} 浏览
          </span>
        </div>
        <h1 class="text-xl font-bold leading-snug">
          {{ resource.title }}
        </h1>
        <p v-if="resource.description" class="mt-2 text-white/75 text-sm leading-relaxed">
          {{ resource.description }}
        </p>
        <!-- 标签 -->
        <div v-if="resource.tags" class="flex flex-wrap gap-2 mt-3">
          <span
            v-for="tag in resource.tags.split(',')"
            :key="tag"
            class="text-xs bg-white/20 px-2 py-0.5 rounded-full"
          >
            #{{ tag.trim() }}
          </span>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="px-5 -mt-4 pb-8 space-y-4">
        <!-- 音频播放器 -->
        <FmCard v-if="resource.type === 'audio' && resource.resourceUrl" class="rounded-2xl">
          <div class="flex flex-col items-center gap-4 py-4">
            <div class="w-20 h-20 rounded-full bg-gradient-to-br from-purple-400 to-violet-500 flex items-center justify-center shadow-lg">
              <FmIcon name="i-carbon:music" class="text-10 text-white" />
            </div>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              {{ resource.title }}
            </p>
            <!-- 原生 HTML5 音频播放器 -->
            <audio
              controls
              class="w-full rounded-xl"
              :src="resource.resourceUrl"
            >
              您的浏览器不支持音频播放
            </audio>
          </div>
        </FmCard>

        <!-- 视频播放器 -->
        <FmCard v-else-if="resource.type === 'video' && resource.resourceUrl" class="rounded-2xl overflow-hidden p-0">
          <video
            controls
            class="w-full rounded-2xl"
            :src="resource.resourceUrl"
            :poster="resource.coverUrl || undefined"
          >
            您的浏览器不支持视频播放
          </video>
        </FmCard>

        <!-- 文章正文 -->
        <FmCard v-if="resource.content" class="rounded-2xl">
          <div class="prose prose-sm max-w-none text-gray-700 dark:text-gray-300">
            <p class="whitespace-pre-wrap leading-relaxed text-sm">
              {{ resource.content }}
            </p>
          </div>
        </FmCard>

        <!-- 暂无内容提示 -->
        <div v-if="!resource.content && !resource.resourceUrl" class="flex justify-center items-center py-12">
          <div class="text-center text-gray-400">
            <FmIcon name="i-carbon:document" class="text-14 mb-3" />
            <p class="text-sm">
              内容暂未上传
            </p>
          </div>
        </div>

        <!-- 底部操作 -->
        <FmCard class="rounded-2xl">
          <p class="text-xs text-gray-500 dark:text-gray-400 mb-3">
            如果这篇内容对你有帮助，不妨和AI助手聊聊你的感受~
          </p>
          <FmButton
            class="w-full bg-gradient-to-r from-teal-500 to-cyan-600 border-0"
            @click="router.push('/chat')"
          >
            <FmIcon name="i-carbon:chat" class="mr-2" />
            与AI助手聊聊
          </FmButton>
        </FmCard>
      </div>
    </template>
  </FmPageLayout>
</template>
