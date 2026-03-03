<template>
  <div class="resource-edit">
    <div class="editor-wrap">
      <ElRow :gutter="10">
        <ElCol :span="18">
          <ElInput
            v-model.trim="form.title"
            placeholder="请输入资源标题（最多100个字符）"
            maxlength="100"
          />
        </ElCol>
        <ElCol :span="6">
          <ElSelect v-model="form.type" placeholder="请选择资源类型" filterable>
            <ElOption
              v-for="item in resourceTypes"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </ElSelect>
        </ElCol>
      </ElRow>

      <!-- 文章类型的富文本内容输入 -->
      <div v-show="form.type === 'article'" class="el-top">
        <ArtWangEditor v-model="form.content" />
      </div>

      <!-- 音视频类型的富文本内容（简介）输入 -->
      <div v-show="form.type !== 'article'" class="el-top form-wrap" style="padding-bottom: 2px;">
         <h2 style="font-size: 16px; margin-bottom: 10px;">资源简介</h2>
         <ElInput
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入音视频资源简介..."
          />
      </div>

      <div class="form-wrap">
        <h2>发布设置</h2>
        <ElForm label-width="80px">
          <!-- 资源封面上传 -->
          <ElFormItem label="封面图片">
            <div class="upload-container">
              <ElUpload
                class="cover-uploader"
                :action="uploadImageUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="onCoverSuccess"
                :on-error="onError"
                :before-upload="beforeUploadImage"
              >
                <div v-if="!form.coverUrl" class="upload-placeholder">
                  <ElIcon class="upload-icon"><Plus /></ElIcon>
                  <div class="upload-text">点击上传封面</div>
                </div>
                <img v-else :src="form.coverUrl" class="cover-image" />
              </ElUpload>
              <div class="el-upload__tip">建议比例 16:9，JPG/PNG格式，不大于2MB</div>
            </div>
          </ElFormItem>

          <template v-if="form.type !== 'article'">
            <!-- 音视频文件上传或直链 -->
            <ElFormItem label="媒体链接">
              <ElInput v-model="form.mediaUrl" placeholder="请输入音视频外链地址，或点击右侧上传" />
              <div class="el-upload__tip" style="margin-top: 6px; width: 100%;">可以填入外部链接，或者直接上传媒体文件到服务器</div>
            </ElFormItem>
            
            <ElFormItem label="媒体时长">
               <ElInputNumber v-model="form.duration" :min="0" :step="1" placeholder="秒" />
               <span style="font-size: 12px; margin-left: 10px; color: #8c939d;">如果不填将前端自动解析</span>
            </ElFormItem>
          </template>

          <ElFormItem label="资源标签">
            <ElInput v-model="form.tags" placeholder="多个标签以英文逗号 ',' 分隔" />
          </ElFormItem>

          <ElFormItem label="直接上架">
            <ElSwitch v-model="isPublishedLocal" />
          </ElFormItem>
        </ElForm>

        <div style="display: flex; justify-content: flex-end; margin-top: 20px;">
          <ElButton @click="router.back()">取消</ElButton>
          <ElButton type="primary" @click="submit" :loading="submitting" style="width: 100px; margin-left: 15px;">
            {{ pageMode === 'edit' ? '保存更改' : '确认发布' }}
          </ElButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { useRoute, useRouter } from 'vue-router'
import { ResourceService, ResourceFormData } from '@/api/resourceApi'
import { useCommon } from '@/composables/useCommon'

defineOptions({ name: 'ResourcePublish' })

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { accessToken } = userStore

const uploadImageUrl = `${import.meta.env.VITE_API_URL || ''}/api/common/upload`
const uploadHeaders = { Authorization: `Bearer ${accessToken}` }

const pageMode = ref<'add' | 'edit'>('add')
const id = ref<number>(0)
const submitting = ref(false)

const resourceTypes = [
  { id: 'article', name: '图文文章' },
  { id: 'audio', name: '治愈白噪音/心理音频' },
  { id: 'video', name: '冥想/助眠视频' }
]

const form = reactive<ResourceFormData>({
  title: '',
  type: 'article',
  content: '',
  coverUrl: '',
  mediaUrl: '',
  tags: '',
  isPublished: 1,
  duration: 0
})

const isPublishedLocal = computed({
  get: () => form.isPublished === 1,
  set: (val: boolean) => { form.isPublished = val ? 1 : 0 }
})

onMounted(() => {
  useCommon().scrollToTop()
  initPageMode()
})

const initPageMode = () => {
  const queryId = route.query.id
  if (queryId) {
    pageMode.value = 'edit'
    id.value = Number(queryId)
    getResourceDetail(id.value)
  } else {
    pageMode.value = 'add'
  }
}

const getResourceDetail = async (resourceId: number) => {
  try {
    const res = await ResourceService.getResourceDetail(resourceId)
    if (res) {
      form.title = res.title
      form.type = res.type as any
      form.content = res.content || ''
      form.coverUrl = res.coverUrl || ''
      form.mediaUrl = res.mediaUrl || ''
      form.tags = res.tags || ''
      form.isPublished = res.isPublished
      form.duration = res.duration || 0
    }
  } catch (error) {
    ElMessage.error('获取资源详情失败')
    router.back()
  }
}

const validateForm = () => {
  if (!form.title) {
    ElMessage.error('请输入资源标题')
    return false
  }
  if (!form.type) {
    ElMessage.error('请选择资源类型')
    return false
  }
  if (form.type === 'article' && (!form.content || form.content === '<p><br></p>')) {
    ElMessage.error('请输入图文文章内容')
    return false
  }
  if (!form.coverUrl) {
    ElMessage.error('请上传封面图片')
    return false
  }
  if (form.type !== 'article' && !form.mediaUrl) {
    ElMessage.warning('目前为音视频资源，未设置外链媒体地址，请确保前端能正确展示处理')
  }
  return true
}

const delCodeTrim = (content: string): string => {
  if (!content) return ''
  return content.replace(/(\s*)<\/code>/g, '</code>')
}

const submit = async () => {
  if (!validateForm()) return
  
  submitting.value = true
  try {
    const submitData = { ...form }
    
    // 如果是文章，处理一下富文本尾部空白问题
    if (submitData.type === 'article' && submitData.content) {
      submitData.content = delCodeTrim(submitData.content)
    }

    if (pageMode.value === 'edit') {
      await ResourceService.updateResource(id.value, submitData)
      ElMessage.success('修改成功')
    } else {
      await ResourceService.createResource(submitData)
      ElMessage.success('发布成功')
    }
    
    setTimeout(() => {
      router.back()
    }, 500)
    
  } catch (err) {
    ElMessage.error(pageMode.value === 'edit' ? '修改失败' : '发布失败')
  } finally {
    submitting.value = false
  }
}

const onCoverSuccess = (response: any) => {
  // 适配后端不同的响应结构
  if (response.code === 200 && response.data) {
    form.coverUrl = response.data.url || response.data
    ElMessage.success('封面上传成功')
  } else if (response.url) {
    form.coverUrl = response.url
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.warning('上传返回结构异常，请配置正确的图床或检查接口头信息')
  }
}

const onError = () => {
  ElMessage.error('图片获取失败')
}

const beforeUploadImage = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}
</script>

<style lang="scss" scoped>
.resource-edit {
  .editor-wrap {
    max-width: 1000px;
    margin: 20px auto;

    .el-top {
      margin-top: 15px;
    }

    .form-wrap {
      padding: 24px;
      margin-top: 20px;
      background-color: var(--art-main-bg-color);
      border: 1px solid var(--art-border-color);
      border-radius: calc(var(--custom-radius) / 2 + 2px);

      h2 {
        margin-bottom: 24px;
        font-size: 18px;
        font-weight: 500;
        color: var(--el-text-color-primary);
        border-bottom: 1px solid var(--art-border-color);
        padding-bottom: 12px;
      }
    }
  }

  .upload-container {
    .cover-uploader {
      position: relative;
      overflow: hidden;
      cursor: pointer;
      border-radius: 6px;
      transition: var(--el-transition-duration);

      &:hover {
        border-color: var(--el-color-primary);
      }

      .upload-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 260px;
        height: 146px; /* 16:9 ratio */
        border: 1px dashed var(--el-border-color-darker);
        border-radius: 6px;
        background-color: var(--el-fill-color-light);

        .upload-icon {
          font-size: 28px;
          color: var(--el-text-color-secondary);
        }

        .upload-text {
          margin-top: 8px;
          font-size: 13px;
          color: var(--el-text-color-regular);
        }
      }

      .cover-image {
        display: block;
        width: 260px;
        height: 146px;
        object-fit: cover;
        border-radius: 6px;
        border: 1px solid var(--el-border-color-lighter);
      }
    }

    .el-upload__tip {
      margin-top: 8px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}
</style>
