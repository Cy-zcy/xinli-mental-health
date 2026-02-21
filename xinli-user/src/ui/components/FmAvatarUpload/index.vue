<script setup lang="ts">
import { toast } from 'vue-sonner'

interface Props {
  /** 当前头像URL或相对路径 */
  modelValue?: string
  /** 用户名称 */
  name?: string
  /** 头像大小 */
  size?: 'sm' | 'md' | 'lg' | 'xl'
  /** 是否禁用上传 */
  disabled?: boolean
  /** 上传按钮文本 */
  uploadText?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  disabled: false,
  uploadText: '更换头像',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'upload-success': [data: { relativePath: string; url: string }]
  'upload-error': [error: any]
}>()

const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

// 触发文件选择
function handleClick() {
  if (props.disabled || uploading.value) return
  fileInputRef.value?.click()
}

// 处理文件选择
async function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    toast.error('不支持的文件类型', {
      description: '请选择 JPG、PNG、GIF 或 WebP 格式的图片',
    })
    return
  }
  
  // 验证文件大小 (5MB)
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    toast.error('文件过大', {
      description: '图片大小不能超过 5MB',
    })
    return
  }
  
  uploading.value = true
  
  try {
    const { uploadAvatar } = await import('@/api/modules/auth')
    const response = await uploadAvatar(file)

    // 更新v-model值
    emit('update:modelValue', response.relativePath)

    // 触发成功事件
    emit('upload-success', response)

    toast.success('头像上传成功')
  } catch (error: any) {
    console.error('头像上传失败:', error)
    
    // 触发错误事件
    emit('upload-error', error)
    
    toast.error('头像上传失败', {
      description: error.message || '请稍后重试',
    })
  } finally {
    uploading.value = false
    // 清空input值，允许重复选择同一文件
    target.value = ''
  }
}
</script>

<template>
  <div class="flex items-center gap-3">
    <!-- 头像显示 - 点击即可上传 -->
    <div class="relative group">
      <FmAvatar
        :src="modelValue"
        :name="name"
        :size="size"
        clickable
        :uploading="uploading"
        @click="handleClick"
        class="transition-all duration-200 group-hover:brightness-75"
      />

      <!-- 悬停提示 -->
      <div class="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200 rounded-full bg-black/20">
        <FmIcon name="i-carbon:camera" class="text-white text-lg" />
      </div>
    </div>

    <!-- 上传按钮 -->
    <FmButton
      variant="outline"
      size="sm"
      :loading="uploading"
      :disabled="disabled"
      @click="handleClick"
    >
      <FmIcon v-if="!uploading" name="i-carbon:camera" class="mr-1" />
      {{ uploading ? '上传中...' : uploadText }}
    </FmButton>

    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
      class="hidden"
      @change="handleFileChange"
    />
  </div>
</template>
