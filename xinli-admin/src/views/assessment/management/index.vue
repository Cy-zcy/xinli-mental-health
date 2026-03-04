<template>
  <div class="page-content assessment-management">
    <ElRow justify="space-between" :gutter="10" style="margin-bottom: 20px;">
      <ElCol :lg="6" :md="6" :sm="14" :xs="16">
        <ElInput
          v-model="searchVal"
          :prefix-icon="Search"
          clearable
          placeholder="输入问卷标题查询"
          @keyup.enter="searchAssessment"
          @clear="searchAssessment"
        />
      </ElCol>
      <ElCol :lg="6" :md="6" :sm="10" :xs="8" style="display: flex; justify-content: end">
        <ElButton type="primary" @click="openAddDialog">新增问卷</ElButton>
      </ElCol>
    </ElRow>

    <div v-loading="isLoading">
      <ElTable :data="assessmentList" border stripe style="width: 100%">
        <ElTableColumn prop="id" label="ID" width="80" align="center" />
        <ElTableColumn prop="title" label="问卷标题" min-width="200" show-overflow-tooltip />
        <ElTableColumn prop="description" label="问卷描述" min-width="250" show-overflow-tooltip />
        <ElTableColumn prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已上架' : '已下架' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ useDateFormat(row.createdAt, 'YYYY-MM-DD HH:mm:ss').value }}
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton 
              size="small" 
              type="primary" 
              plain 
              @click="manageQuestions(row)"
            >
              题目管理
            </ElButton>
            <ElButton
              size="small"
              type="info"
              plain
              @click="openEditDialog(row)"
            >
              编辑
            </ElButton>
            <ElButton 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'" 
              plain 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </ElButton>
            <ElButton 
              size="small" 
              type="danger" 
              plain 
              @click="handleDelete(row)"
            >
              删除
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <div style="display: flex; justify-content: center; margin-top: 20px" v-if="total > 0">
      <ElPagination
        size="default"
        background
        v-model:current-page="currentPage"
        :page-size="pageSize"
        layout="prev, pager, next, total, jumper"
        :total="total"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑问卷对话框 -->
    <ElDialog v-model="addDialogVisible" :title="dialogType === 'add' ? '新增测评问卷' : '编辑测评问卷'" width="500px">
      <ElForm ref="addFormRef" :model="formData" :rules="rules" label-width="100px">
        <ElFormItem label="问卷标题" prop="title">
          <ElInput v-model="formData.title" placeholder="请输入问卷标题，如：SDS抑郁自评量表" maxlength="50" show-word-limit />
        </ElFormItem>
        <ElFormItem label="问卷描述" prop="description">
          <ElInput 
            v-model="formData.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入问卷的指导语或简介" 
            maxlength="200" 
            show-word-limit 
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="addDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="submitAdd" :loading="submitLoading">确定</ElButton>
        </span>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useDateFormat } from '@vueuse/core'
import { AssessmentService, AssessmentItem } from '@/api/assessmentApi'

defineOptions({ name: 'AssessmentManagement' })

const router = useRouter()

const searchVal = ref('')
const assessmentList = ref<AssessmentItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const isLoading = ref(true)

const addDialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const currentEditId = ref<number>(0)
const addFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const formData = ref({
  title: '',
  description: ''
})

const rules = {
  title: [
    { required: true, message: '请输入问卷标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入问卷描述', trigger: 'blur' }
  ]
}

onMounted(() => {
  getAssessmentList()
})

const searchAssessment = () => {
  currentPage.value = 1
  getAssessmentList()
}

const getAssessmentList = async () => {
  isLoading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }
    // 注意：目前后端 AssessmentService 尚不支持 keyword 搜索，预留搜索框或仅前端过滤
    
    const { records, total: totalCount } = await AssessmentService.getAssessmentList(params)
    // 根据标题进行简单的本地过滤（如果填写了searchVal）
    if (searchVal.value) {
      const filtered = (records || []).filter(r => r.title.includes(searchVal.value));
      assessmentList.value = filtered;
      total.value = filtered.length;
    } else {
      assessmentList.value = records || []
      total.value = totalCount || 0
    }
  } catch (error) {
    ElMessage.error('获取问卷列表失败')
  } finally {
    isLoading.value = false
  }
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  getAssessmentList()
}

const openAddDialog = () => {
  dialogType.value = 'add'
  formData.value = { title: '', description: '' }
  addDialogVisible.value = true
  if (addFormRef.value) {
    addFormRef.value.clearValidate()
  }
}

const openEditDialog = (item: AssessmentItem) => {
  dialogType.value = 'edit'
  currentEditId.value = item.id
  formData.value = { title: item.title, description: item.description }
  addDialogVisible.value = true
  if (addFormRef.value) {
    addFormRef.value.clearValidate()
  }
}

const submitAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await AssessmentService.createAssessment(formData.value)
          ElMessage.success('新增成功')
        } else {
          await AssessmentService.updateAssessment(currentEditId.value, formData.value)
          ElMessage.success('编辑成功')
        }
        addDialogVisible.value = false
        getAssessmentList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const toggleStatus = async (item: AssessmentItem) => {
  const newStatus = item.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(`确认要${actionText}该问卷吗？`, '提示', {
      type: 'warning'
    })
    
    await AssessmentService.updateAssessmentStatus(item.id, newStatus)
    ElMessage.success(`${actionText}成功`)
    item.status = newStatus
  } catch (e) {
    // cancelled
  }
}

const handleDelete = async (item: AssessmentItem) => {
  try {
    await ElMessageBox.confirm('确认删除该问卷？将会级联删除所有题目和选项，且不可恢复。', '危险操作', {
      type: 'error',
      confirmButtonText: '强制删除'
    })
    
    await AssessmentService.deleteAssessment(item.id)
    ElMessage.success('删除成功')
    getAssessmentList()
  } catch (e) {
    // cancelled
  }
}

const manageQuestions = (item: AssessmentItem) => {
  router.push({ path: `/assessment/questions`, query: { id: item.id, title: item.title } })
}
</script>

<style lang="scss" scoped>
.assessment-management {
  padding: 20px;
  background-color: var(--el-bg-color);
  border-radius: var(--custom-radius);
}
</style>
