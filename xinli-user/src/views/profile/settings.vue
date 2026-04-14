<script setup lang="ts">
import { toTypedSchema } from '@vee-validate/zod'
import { useForm } from 'vee-validate'
import * as z from 'zod'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/ui/shadcn/ui/form'
import { toast } from 'vue-sonner'

definePage({
  meta: {
    title: '设置',
    auth: true,
  },
})

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const showPasswordForm = ref(false)

// 用户信息表单
const userForm = useForm({
  validationSchema: toTypedSchema(z.object({
    nickname: z.string().min(2, '昵称至少2个字符').max(20, '昵称最多20个字符'),
    avatar: z.string().optional(),
  })),
  initialValues: {
    nickname: '',
    avatar: '',
  },
})

// 定义表单字段
const [nickname, nicknameAttrs] = userForm.defineField('nickname')
const [avatar, avatarAttrs] = userForm.defineField('avatar')

// 页面加载时确保用户信息是最新的，并初始化表单
onMounted(async () => {
  if (userStore.isLogin && (!userStore.userInfo || !userStore.nickname)) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  // 等待下一个 tick 确保用户信息已更新
  await nextTick()

  // 初始化表单数据
  const initialData = {
    nickname: userStore.nickname || '',
    avatar: userStore.avatar || '',
  }
  console.log('初始化表单数据:', initialData)
  console.log('当前用户信息:', { nickname: userStore.nickname, avatar: userStore.avatar })

  // 直接设置字段值
  nickname.value = userStore.nickname || ''
  avatar.value = userStore.avatar || ''

  console.log('设置后的字段值:', { nickname: nickname.value, avatar: avatar.value })
})

// 密码修改表单
const passwordForm = useForm({
  validationSchema: toTypedSchema(z.object({
    oldPassword: z.string().min(6, '请输入当前密码'),
    newPassword: z.string().min(6, '新密码至少6位'),
    confirmPassword: z.string().min(6, '请确认新密码'),
  }).refine((data) => data.newPassword === data.confirmPassword, {
    message: '两次输入的密码不一致',
    path: ['confirmPassword'],
  })),
  initialValues: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  },
})

// 更新用户信息
const onUpdateUserInfo = userForm.handleSubmit(async (values) => {
  loading.value = true
  try {
    console.log('表单提交的数据:', values)
    console.log('当前表单状态:', userForm.values)
    await userStore.updateUserInfo(values)
    toast.success('个人信息更新成功')
  } catch (error: any) {
    toast.error('更新失败', {
      description: error.message || '请稍后重试',
    })
  } finally {
    loading.value = false
  }
})

// 修改密码
const onChangePassword = passwordForm.handleSubmit(async (values) => {
  loading.value = true
  try {
    const { authApi } = await import('@/api/modules')
    await authApi.changePassword({
      oldPassword: values.oldPassword,
      newPassword: values.newPassword,
    })
    toast.success('密码修改成功')
    showPasswordForm.value = false
    passwordForm.resetForm()
  } catch (error: any) {
    toast.error('密码修改失败', {
      description: error.message || '请检查当前密码是否正确',
    })
  } finally {
    loading.value = false
  }
})

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

function goHome() {
  router.push('/')
}

// 头像上传成功处理
async function handleAvatarUploadSuccess(data: { relativePath: string; url: string; message: string }) {
  try {
    // 更新表单中的头像路径
    userForm.setFieldValue('avatar', data.relativePath)

    // 更新用户store中的头像信息
    await userStore.updateUserInfo({ avatar: data.relativePath })
  } catch (error: any) {
    console.error('更新用户头像信息失败:', error)
    toast.error('更新用户信息失败', {
      description: error.message || '请稍后重试',
    })
  }
}


</script>

<template>
  <FmPageLayout title="设置" :navbar="{ back: true }" @back="goBack">
    <template #navbar-right>
      <FmIcon name="i-carbon:home" class="text-xl text-gray-600 dark:text-gray-300 active:opacity-60 mr-2" @click="goHome" />
    </template>
    <div class="flex flex-1 flex-col gap-6 p-4">
      <!-- 个人信息设置 -->
      <div class="bg-card rounded-lg p-4">
        <h3 class="text-lg font-semibold mb-4">个人信息</h3>
        <form @submit="onUpdateUserInfo">
          <div class="space-y-4">
            <FormField name="nickname">
              <FormItem>
                <FormLabel>昵称</FormLabel>
                <FormControl>
                  <FmInput
                    type="text"
                    :placeholder="userStore.nickname ? `当前昵称：${userStore.nickname}` : '请输入昵称'"
                    v-model="nickname"
                    v-bind="nicknameAttrs"
                    @input="(e) => console.log('输入变化:', e.target.value, '当前值:', nickname)"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            </FormField>

            <div class="space-y-2">
              <label class="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">头像</label>
              <!-- 头像上传组件 -->
              <FmAvatarUpload
                v-model="userForm.values.avatar"
                :name="userStore.nickname"
                size="md"
                @upload-success="handleAvatarUploadSuccess"
              />
            </div>

            <div class="pt-4">
              <FmButton type="submit" :loading class="w-full">
                保存修改
              </FmButton>
            </div>
          </div>
        </form>
      </div>

      <!-- 账号安全 -->
      <div class="bg-card rounded-lg p-4">
        <h3 class="text-lg font-semibold mb-4">账号安全</h3>
        
        <div class="space-y-3">
          <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
            <div>
              <div class="font-medium">手机号</div>
              <div class="text-sm text-gray-500">{{ userStore.phone || '未设置' }}</div>
            </div>
          </div>

          <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
            <div>
              <div class="font-medium">登录密码</div>
              <div class="text-sm text-gray-500">定期更换密码保护账号安全</div>
            </div>
            <FmButton 
              variant="outline" 
              size="sm" 
              @click="showPasswordForm = !showPasswordForm"
            >
              {{ showPasswordForm ? '取消' : '修改' }}
            </FmButton>
          </div>

          <!-- 密码修改表单 -->
          <div v-if="showPasswordForm" class="mt-4 p-4 border rounded-lg">
            <form @submit="onChangePassword">
              <div class="space-y-4">
                <FormField v-slot="{ componentField }" name="oldPassword">
                  <FormItem>
                    <FormLabel>当前密码</FormLabel>
                    <FormControl>
                      <FmInput 
                        type="password" 
                        placeholder="请输入当前密码" 
                        v-bind="componentField" 
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <FormField v-slot="{ componentField }" name="newPassword">
                  <FormItem>
                    <FormLabel>新密码</FormLabel>
                    <FormControl>
                      <FmInput 
                        type="password" 
                        placeholder="请输入新密码（至少6位）" 
                        v-bind="componentField" 
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <FormField v-slot="{ componentField }" name="confirmPassword">
                  <FormItem>
                    <FormLabel>确认新密码</FormLabel>
                    <FormControl>
                      <FmInput 
                        type="password" 
                        placeholder="请再次输入新密码" 
                        v-bind="componentField" 
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                </FormField>

                <div class="flex gap-3">
                  <FmButton type="submit" :loading class="flex-1">
                    确认修改
                  </FmButton>
                  <FmButton 
                    variant="outline" 
                    type="button" 
                    class="flex-1"
                    @click="showPasswordForm = false"
                  >
                    取消
                  </FmButton>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- 应用设置 -->
      <div class="bg-card rounded-lg p-4">
        <h3 class="text-lg font-semibold mb-4">应用设置</h3>
        
        <div class="space-y-3">
          <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
            <div>
              <div class="font-medium">深色模式</div>
              <div class="text-sm text-gray-500">跟随系统设置</div>
            </div>
            <FmButton variant="outline" size="sm">
              设置
            </FmButton>
          </div>

          <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
            <div>
              <div class="font-medium">消息通知</div>
              <div class="text-sm text-gray-500">接收重要消息提醒</div>
            </div>
            <FmButton variant="outline" size="sm">
              设置
            </FmButton>
          </div>
        </div>
      </div>

      <!-- 关于 -->
      <div class="bg-card rounded-lg p-4">
        <h3 class="text-lg font-semibold mb-4">关于</h3>
        
        <div class="space-y-3">
          <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
            <div>
              <div class="font-medium">版本信息</div>
              <div class="text-sm text-gray-500">v1.0.0</div>
            </div>
          </div>

          <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
            <div>
              <div class="font-medium">用户协议</div>
              <div class="text-sm text-gray-500">查看服务条款</div>
            </div>
            <FmIcon name="i-carbon:chevron-right" class="text-gray-400" />
          </div>

          <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
            <div>
              <div class="font-medium">隐私政策</div>
              <div class="text-sm text-gray-500">了解数据使用方式</div>
            </div>
            <FmIcon name="i-carbon:chevron-right" class="text-gray-400" />
          </div>
        </div>
      </div>
    </div>
  </FmPageLayout>
</template>
