<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import type { ButtonVariants } from './button'
import { Button } from './button'

defineOptions({
  name: 'FmButton',
  inheritAttrs: false,
})

const props = defineProps<{
  variant?: ButtonVariants['variant']
  size?: ButtonVariants['size']
  class?: HTMLAttributes['class']
  disabled?: boolean
  loading?: boolean
}>()

// 获取所有属性和事件监听器
const attrs = useAttrs()
</script>

<template>
  <Button
    :variant
    :size
    :disabled="props.disabled || props.loading"
    :class="props.class"
    v-bind="attrs"
  >
    <FmIcon v-if="loading" name="i-line-md:loading-twotone-loop" />
    <slot />
  </Button>
</template>
