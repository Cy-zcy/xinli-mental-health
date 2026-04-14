<script setup lang="ts">
import { toTypedSchema } from '@vee-validate/zod'
import { useForm } from 'vee-validate'
import * as z from 'zod'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/ui/shadcn/ui/form'
import { toast } from 'vue-sonner'
import { forumApi } from '@/api/modules'
import type { ForumCategory } from '@/api/types'

definePage({
  meta: {
    title: '发布帖子',
    auth: true,
  },
})

const router = useRouter()

const loading = ref(false)
const categories = ref<ForumCategory[]>([])

// 表单验证
const form = useForm({
  validationSchema: toTypedSchema(z.object({
    title: z.string()
      .min(5, '标题至少5个字符')
      .max(100, '标题最多100个字符'),
    content: z.string()
      .min(10, '内容至少10个字符')
      .max(5000, '内容最多5000个字符'),
    category: z.string().min(1, '请选择分类'),
  })),
  initialValues: {
    title: '',
    content: '',
    category: '',
  },
})

// 获取论坛分类
async function loadCategories() {
  try {
    const response = await forumApi.getCategories()
    categories.value = response.filter(c => c.value !== '') // 过滤掉"全部"分类
  } catch (error: any) {
    console.error('加载分类失败:', error)
    toast.error('加载分类失败')
    // 提供默认分类
    categories.value = [
      { value: 'emotion', label: '情感倾诉', description: '分享情感困扰，寻求理解和支持' },
      { value: 'experience', label: '经验分享', description: '分享心理健康相关的经验和心得' }
    ]
  }
}

// 发布帖子
const onSubmit = form.handleSubmit(async (values) => {
  loading.value = true
  try {
    await forumApi.createPost({
      title: values.title,
      content: values.content,
      category: values.category,
    })
    
    toast.success('发布成功')
    router.push('/forum')
  } catch (error: any) {
    console.error('发布失败:', error)
    toast.error('发布失败', {
      description: error.message || '请稍后重试',
    })
  } finally {
    loading.value = false
  }
})

// 保存草稿
function saveDraft() {
  const values = form.values
  localStorage.setItem('forum_draft', JSON.stringify(values))
  toast.success('草稿已保存')
}

// 加载草稿
function loadDraft() {
  const draft = localStorage.getItem('forum_draft')
  if (draft) {
    try {
      const values = JSON.parse(draft)
      form.setValues(values)
      toast.success('草稿已加载')
    } catch (error) {
      console.error('加载草稿失败:', error)
    }
  }
}

// 清除草稿
function clearDraft() {
  localStorage.removeItem('forum_draft')
  form.resetForm()
  toast.success('草稿已清除')
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/forum')
  }
}

function goHome() {
  router.push('/')
}

// 页面加载时获取分类和草稿
onMounted(async () => {
  await loadCategories()
  
  // 检查是否有草稿
  const draft = localStorage.getItem('forum_draft')
  if (draft) {
    loadDraft()
  }
})

// 页面离开时自动保存草稿
onBeforeUnmount(() => {
  const values = form.values
  if (values.title || values.content) {
    localStorage.setItem('forum_draft', JSON.stringify(values))
  }
})
</script>

<template>
  <FmPageLayout title="发布帖子" :navbar="{ back: true }" @back="goBack">
    <template #navbar-right>
      <FmIcon name="i-carbon:home" class="text-xl text-gray-600 dark:text-gray-300 active:opacity-60 mr-2" @click="goHome" />
    </template>
    <div class="flex flex-1 flex-col">
      <form @submit="onSubmit" class="flex flex-1 flex-col">
        <div class="flex-1 p-4 space-y-6">
          <!-- 分类选择 -->
          <FormField v-slot="{ componentField }" name="category">
            <FormItem>
              <FormLabel class="text-base font-semibold">分类</FormLabel>
              <FormControl>
                <div class="relative">
                  <select
                    v-bind="componentField"
                    class="w-full p-4 pr-10 border-2 border-gray-200 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 appearance-none cursor-pointer"
                  >
                    <option value="" class="text-gray-500">请选择分类</option>
                    <option
                      v-for="category in categories"
                      :key="category.value"
                      :value="category.value"
                      class="text-gray-900 dark:text-white"
                    >
                      {{ category.label }}
                    </option>
                  </select>
                  <FmIcon name="i-carbon:chevron-down" class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 pointer-events-none" />
                </div>
              </FormControl>
              <FormMessage />
            </FormItem>
          </FormField>

          <!-- 标题输入 -->
          <FormField v-slot="{ componentField }" name="title">
            <FormItem>
              <FormLabel class="text-base font-semibold">标题</FormLabel>
              <FormControl>
                <FmInput
                  type="text"
                  placeholder="请输入帖子标题（5-100字符）"
                  v-bind="componentField"
                  class="p-4 text-base border-2 border-gray-200 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          </FormField>

          <!-- 内容输入 -->
          <FormField v-slot="{ componentField }" name="content">
            <FormItem>
              <FormLabel class="text-base font-semibold">内容</FormLabel>
              <FormControl>
                <textarea
                  v-bind="componentField"
                  placeholder="分享您的想法、经历或寻求帮助...&#10;&#10;请遵守社区规范，友善交流，共同营造温暖的心理健康社区。"
                  class="w-full h-72 p-4 text-base border-2 border-gray-200 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-800 text-gray-900 dark:text-white resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 leading-relaxed"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          </FormField>

          <!-- 发布提示 -->
          <div class="p-5 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-xl border border-blue-200 dark:border-blue-700">
            <div class="flex items-start gap-4">
              <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center flex-shrink-0">
                <FmIcon name="i-carbon:information" class="text-white text-4" />
              </div>
              <div class="text-sm text-blue-800 dark:text-blue-200">
                <p class="font-semibold mb-3 text-base">发布须知</p>
                <ul class="space-y-2 text-sm leading-relaxed">
                  <li class="flex items-start gap-2">
                    <span class="text-blue-500 mt-1">•</span>
                    <span>请保持内容积极正面，避免负面情绪传播</span>
                  </li>
                  <li class="flex items-start gap-2">
                    <span class="text-blue-500 mt-1">•</span>
                    <span>尊重他人隐私，不要分享个人敏感信息</span>
                  </li>
                  <li class="flex items-start gap-2">
                    <span class="text-blue-500 mt-1">•</span>
                    <span>如遇紧急情况，请及时寻求专业帮助</span>
                  </li>
                  <li class="flex items-start gap-2">
                    <span class="text-blue-500 mt-1">•</span>
                    <span>内容将经过审核，违规内容将被删除</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="sticky bottom-0 p-4 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 shadow-lg">
          <div class="flex gap-3">
            <FmButton
              variant="outline"
              type="button"
              @click="saveDraft"
              class="px-6 py-3 rounded-xl border-2 hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <FmIcon name="i-carbon:save" class="mr-2" />
              保存草稿
            </FmButton>
            <FmButton
              variant="outline"
              type="button"
              @click="clearDraft"
              class="px-6 py-3 rounded-xl border-2 hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <FmIcon name="i-carbon:trash-can" class="mr-2" />
              清除
            </FmButton>
            <FmButton
              type="submit"
              :loading="loading"
              class="flex-1 px-6 py-3 rounded-xl bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white font-semibold shadow-lg"
            >
              <FmIcon v-if="!loading" name="i-carbon:send" class="mr-2" />
              {{ loading ? '发布中...' : '发布帖子' }}
            </FmButton>
          </div>
        </div>
      </form>
    </div>
  </FmPageLayout>
</template>
