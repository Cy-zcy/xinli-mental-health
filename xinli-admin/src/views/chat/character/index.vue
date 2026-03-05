<template>
  <div class="character-management">
    <div class="page-header">
      <div class="header-left">
        <h2>AI人设管理</h2>
        <span class="subtitle">管理聊天模块中可供用户选择的各色 AI 人格</span>
      </div>
      <div class="header-right">
        <ElInput
          v-model="queryParams.keyword"
          placeholder="搜索人设名称..."
          clearable
          style="width: 250px; margin-right: 15px;"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <ElIcon><Search /></ElIcon>
          </template>
        </ElInput>
        <ElButton type="primary" @click="handleAdd">
          <ElIcon><Plus /></ElIcon>新增人设
        </ElButton>
      </div>
    </div>

    <!-- 列表区 -->
    <ElCard class="list-card" shadow="never">
      <ElTable v-loading="loading" :data="tableData" style="width: 100%" border>
        <ElTableColumn prop="id" label="ID" width="80" align="center" />
        
        <ElTableColumn label="头像" width="100" align="center">
          <template #default="scope">
            <ElAvatar :size="50" :src="scope.row.avatar" />
          </template>
        </ElTableColumn>

        <ElTableColumn prop="name" label="人设名称" width="150" />
        
        <ElTableColumn prop="greeting" label="开场白" show-overflow-tooltip min-width="180" />

        <ElTableColumn label="状态" width="100" align="center">
          <template #default="scope">
            <ElTag :type="scope.row.isActive === 1 ? 'success' : 'info'">
              {{ scope.row.isActive === 1 ? '已激活' : '已停用' }}
            </ElTag>
          </template>
        </ElTableColumn>

        <ElTableColumn prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </ElTableColumn>

        <ElTableColumn label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <ElButton size="small" type="primary" link @click="handleEdit(scope.row)">
              编辑
            </ElButton>
            <ElButton 
              size="small" 
              :type="scope.row.isActive === 1 ? 'warning' : 'success'" 
              link 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.isActive === 1 ? '停用' : '启用' }}
            </ElButton>
            <ElPopconfirm title="确定要删除该人设吗？" @confirm="handleDelete(scope.row.id)">
              <template #reference>
                <ElButton size="small" type="danger" link>删除</ElButton>
              </template>
            </ElPopconfirm>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="pagination-wrap">
        <ElPagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </ElCard>

    <!-- 编辑/新增弹窗 -->
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px" class="character-form">
        
        <ElFormItem label="人设名称" prop="name">
          <ElInput v-model="form.name" placeholder="请输入人设名称，如：温柔学姐、暴躁老哥" maxlength="50" show-word-limit />
        </ElFormItem>

        <ElFormItem label="头像" prop="avatar">
          <div class="upload-container">
            <ElUpload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :on-error="onUploadError"
              :before-upload="beforeUpload"
            >
              <img v-if="form.avatar" :src="form.avatar" class="avatar-img" />
              <div v-else class="upload-placeholder">
                <ElIcon class="upload-icon"><Plus /></ElIcon>
                <div class="upload-text">点击上传</div>
              </div>
            </ElUpload>
            <div class="el-upload__tip">支持 JPG、PNG，建议正方形，大小不超过 2MB</div>
          </div>
        </ElFormItem>

        <ElFormItem label="专属开场白" prop="greeting">
          <ElInput v-model="form.greeting" type="textarea" :rows="2" placeholder="如：你好呀，今天遇到什么烦心事了吗？" />
          <div class="tip-text">用户选择该角色时，AI 发送的第一句话</div>
        </ElFormItem>

        <ElFormItem label="背景设定" prop="background">
          <ElInput v-model="form.background" type="textarea" :rows="4" placeholder="详细描述该角色的身份、经历等" />
          <div class="tip-text">Prompt的【背景】部分，如：你是一位心理学硕士，擅长倾听...</div>
        </ElFormItem>

        <ElFormItem label="性格呈现" prop="personality">
          <ElInput v-model="form.personality" type="textarea" :rows="4" placeholder="描述角色的说话语气和性格特点" />
          <div class="tip-text">Prompt的【性格】部分，如：语气温暖、会使用颜文字、不过多讲大道理...</div>
        </ElFormItem>

        <ElFormItem label="行为禁忌" prop="rules">
          <ElInput v-model="form.rules" type="textarea" :rows="3" placeholder="描述角色绝对不能做的事" />
          <div class="tip-text">Prompt的【禁忌】部分，如：绝不主动评价用户的对错、必须用中文回答...</div>
        </ElFormItem>

        <ElFormItem label="是否激活" prop="isActive">
          <ElSwitch v-model="isActiveLocal" active-text="激活" inactive-text="停用" />
          <div class="tip-text" style="margin-left: 20px;">停用的角色将不会展示给前端用户</div>
        </ElFormItem>
      </ElForm>

      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="submitForm" :loading="submitLoading">保存</ElButton>
        </div>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { aiCharacterApi, type AiCharacter } from '@/api/aiCharacterApi'
import { useUserStore } from '@/store/modules/user'
import dayjs from 'dayjs'

defineOptions({ name: 'CharacterManagement' })

// -- State --
const loading = ref(false)
const tableData = ref<AiCharacter[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

// -- Upload --
const userStore = useUserStore()
const uploadUrl = `${import.meta.env.VITE_API_URL || ''}/api/common/upload`
const uploadHeaders = { Authorization: `Bearer ${userStore.accessToken}` }

// -- Dialog --
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => (isEdit.value ? '编辑 AI 人设' : '新增 AI 人设'))
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<AiCharacter>>({
  name: '',
  avatar: '',
  greeting: '',
  background: '',
  personality: '',
  rules: '',
  isActive: 1
})

const isActiveLocal = computed({
  get: () => form.isActive === 1,
  set: (val: boolean) => { form.isActive = val ? 1 : 0 }
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入人设名称', trigger: 'blur' }],
  avatar: [{ required: true, message: '请上传头像', trigger: 'change' }],
  greeting: [{ required: true, message: '请输入专属开场白', trigger: 'blur' }],
  background: [{ required: true, message: '请输入背景设定', trigger: 'blur' }],
  personality: [{ required: true, message: '请输入性格呈现', trigger: 'blur' }],
  rules: [{ required: true, message: '请输入行为禁忌', trigger: 'blur' }]
})

// -- Methods --
const fetchList = async () => {
  loading.value = true
  try {
    const res = await aiCharacterApi.getPage(queryParams.page, queryParams.size, queryParams.keyword)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取人设列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  fetchList()
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  fetchList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  fetchList()
}

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.avatar = ''
  form.greeting = ''
  form.background = ''
  form.personality = ''
  form.rules = ''
  form.isActive = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: AiCharacter) => {
  isEdit.value = true
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleToggleStatus = async (row: AiCharacter) => {
  const newStatus = row.isActive === 1 ? 0 : 1
  try {
    const updateData = { ...row, isActive: newStatus }
    await aiCharacterApi.update(row.id!, updateData)
    ElMessage.success(`${newStatus === 1 ? '启用' : '停用'}成功`)
    fetchList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await aiCharacterApi.delete(id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && queryParams.page > 1) {
      queryParams.page--
    }
    fetchList()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await aiCharacterApi.update(form.id!, form)
          ElMessage.success('修改成功')
        } else {
          await aiCharacterApi.create(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        ElMessage.error(isEdit.value ? '修改失败' : '新增失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// -- Upload Methods --
const beforeUpload = (file: File) => {
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

const onUploadSuccess = (response: any) => {
  if (response.code === 200 && response.data) {
    form.avatar = response.data.url || response.data
    formRef.value?.validateField('avatar')
    ElMessage.success('头像上传成功')
  } else if (response.url) {
    form.avatar = response.url
    formRef.value?.validateField('avatar')
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.warning('上传异常，请检查接口头信息')
  }
}

const onUploadError = () => {
  ElMessage.error('头像上传失败')
}

// -- Utils --
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.character-management {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      h2 {
        margin: 0;
        font-size: 20px;
        color: var(--el-text-color-primary);
      }
      .subtitle {
        font-size: 13px;
        color: var(--el-text-color-secondary);
        margin-top: 4px;
        display: inline-block;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
    }
  }

  .list-card {
    border-radius: 8px;
  }

  .pagination-wrap {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .character-form {
    .tip-text {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      line-height: 1.2;
      margin-top: 4px;
      width: 100%;
    }
  }

  /* Upload Styles */
  .upload-container {
    .avatar-uploader {
      position: relative;
      overflow: hidden;
      cursor: pointer;
      border-radius: 6px;
      border: 1px dashed var(--el-border-color-darker);
      transition: var(--el-transition-duration);
      width: 100px;
      height: 100px;

      &:hover {
        border-color: var(--el-color-primary);
      }

      .upload-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
        background-color: var(--el-fill-color-light);

        .upload-icon {
          font-size: 24px;
          color: var(--el-text-color-secondary);
        }

        .upload-text {
          margin-top: 5px;
          font-size: 12px;
          color: var(--el-text-color-regular);
        }
      }

      .avatar-img {
        display: block;
        width: 100%;
        height: 100%;
        object-fit: cover;
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
