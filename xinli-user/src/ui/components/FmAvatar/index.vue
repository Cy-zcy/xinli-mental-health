<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import type { AvatarVariants } from './avatar'
import { Avatar, AvatarFallback, AvatarImage } from './avatar'

defineOptions({
  name: 'FmAvatar',
})

interface Props {
  /** 头像URL或相对路径 */
  src?: string
  /** 备用文本 */
  fallback?: string
  /** 用户名称，用于alt属性 */
  name?: string
  /** 头像大小 */
  size?: AvatarVariants['size']
  /** 头像形状 */
  shape?: AvatarVariants['shape']
  /** 自定义样式类 */
  class?: HTMLAttributes['class']
  /** 是否可点击 */
  clickable?: boolean
  /** 是否显示上传状态 */
  uploading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  clickable: false,
  uploading: false,
})

const emit = defineEmits<{
  click: []
}>()

// 获取头像URL
function getAvatarUrl() {
  if (!props.src) return ''

  // 如果是完整URL，直接返回
  if (props.src.startsWith('http')) {
    return props.src
  }

  // 如果是相对路径，拼接基础URL
  return `${import.meta.env.VITE_APP_API_BASEURL}/uploads/${props.src}`
}

function handleClick() {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<template>
  <div
    :class="[
      'relative',
      {
        'cursor-pointer transition-all hover:ring-2 hover:ring-primary/20 rounded-full': clickable,
        'opacity-60': uploading,
      }
    ]"
    @click="handleClick"
  >
    <Avatar :shape :class="props.class">
      <AvatarImage :src="getAvatarUrl()" :alt="name || '用户头像'" />
      <AvatarFallback class="inline-flex bg-gradient-to-br from-blue-400 to-purple-500 text-white">
        <slot>
          <FmIcon v-if="!fallback" name="i-carbon:user-avatar-filled-alt" class="text-lg" />
          <span v-else>{{ fallback }}</span>
        </slot>
      </AvatarFallback>
    </Avatar>

    <!-- 上传状态遮罩 -->
    <div
      v-if="uploading"
      class="absolute inset-0 bg-black/50 flex items-center justify-center rounded-full"
    >
      <FmIcon name="i-line-md:loading-twotone-loop" class="text-white text-lg" />
    </div>
  </div>
</template>
