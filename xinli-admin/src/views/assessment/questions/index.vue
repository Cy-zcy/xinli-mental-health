<template>
  <div class="page-content assessment-questions">
    <div class="header">
      <ElButton :icon="Back" @click="goBack" style="margin-right: 15px">返回问卷列表</ElButton>
      <h2>题目管理: {{ assessmentTitle }}</h2>
    </div>

    <ElCard shadow="hover" style="margin-bottom: 20px;">
      <div class="action-bar">
        <ElButton type="primary" :icon="Plus" @click="openAddQuestionDialog">
          新增题目
        </ElButton>
      </div>

      <div v-loading="isLoading">
        <ElTable :data="questionList" border style="width: 100%">
          <ElTableColumn type="expand">
            <template #default="props">
              <div class="options-container">
                <h4>选项列表
                  <ElButton size="small" type="primary" plain @click="openAddOptionDialog(props.row.id)" style="margin-left: 10px;">添加选项</ElButton>
                </h4>
                <ElTable :data="props.row.options" border size="small" style="width: 100%">
                  <ElTableColumn prop="sortOrder" label="排序" width="80" align="center" />
                  <ElTableColumn prop="content" label="选项内容" min-width="200" />
                  <ElTableColumn prop="score" label="分值" width="80" align="center" />
                  <ElTableColumn label="操作" width="150" align="center" fixed="right">
                    <template #default="optionProps">
                      <ElButton size="small" type="danger" plain @click="handleDeleteOption(optionProps.row.id, props.row.id)">删除</ElButton>
                    </template>
                  </ElTableColumn>
                </ElTable>
              </div>
            </template>
          </ElTableColumn>

          <ElTableColumn prop="sortOrder" label="题目排序" width="100" align="center" />
          <ElTableColumn prop="content" label="题干内容" min-width="200" />
          <ElTableColumn prop="type" label="题型" width="120" align="center">
            <template #default="{ row }">
              <ElTag v-if="row.type === 'single_choice'">单选题</ElTag>
              <ElTag v-else>{{ row.type }}</ElTag>
            </template>
          </ElTableColumn>
          
          <ElTableColumn label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <ElButton size="small" type="danger" plain @click="handleDeleteQuestion(row.id)">删除</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </ElCard>

    <!-- 新增题目对话框 -->
    <ElDialog v-model="addDialogVisible" title="新增题目" width="500px">
      <ElForm ref="addFormRef" :model="formData" :rules="rules" label-width="80px">
        <ElFormItem label="题干" prop="content">
          <ElInput v-model="formData.content" type="textarea" :rows="3" placeholder="请输入题目内容" maxlength="200" show-word-limit />
        </ElFormItem>
        <ElFormItem label="排序" prop="sortOrder">
          <ElInputNumber v-model="formData.sortOrder" :min="1" :max="999" />
        </ElFormItem>
        <ElFormItem label="题型" prop="type">
          <ElSelect v-model="formData.type" placeholder="请选择">
            <ElOption label="单项选择题" value="single_choice" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="addDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="submitAdd" :loading="submitLoading">确定</ElButton>
        </span>
      </template>
    </ElDialog>

    <!-- 新增选项对话框 -->
    <ElDialog v-model="optionDialogVisible" title="新增选项" width="500px">
      <ElForm ref="optionFormRef" :model="optionData" :rules="optionRules" label-width="80px">
        <ElFormItem label="选项内容" prop="content">
          <ElInput v-model="optionData.content" placeholder="选项文字（如：偶尔，经常）" maxlength="100" />
        </ElFormItem>
        <ElFormItem label="分值" prop="score">
          <ElInputNumber v-model="optionData.score" />
        </ElFormItem>
        <ElFormItem label="排序" prop="sortOrder">
          <ElInputNumber v-model="optionData.sortOrder" :min="1" :max="99" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="optionDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="submitAddOption" :loading="optionSubmitLoading">确定</ElButton>
        </span>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Back, Plus } from '@element-plus/icons-vue'
import request from '@/utils/http'

defineOptions({ name: 'AssessmentQuestions' })

const route = useRoute()
const router = useRouter()

const assessmentId = ref(route.query.id as string)
const assessmentTitle = ref(route.query.title as string)

const questionList = ref<any[]>([])
const isLoading = ref(true)

// 表单部分
const addDialogVisible = ref(false)
const addFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const formData = ref({
  assessmentId: 0,
  content: '',
  type: 'single_choice',
  sortOrder: 1
})

const optionDialogVisible = ref(false)
const optionFormRef = ref<FormInstance>()
const optionSubmitLoading = ref(false)
const currentQuestionIdForOption = ref<number>(0)
const optionData = ref({
  questionId: 0,
  content: '',
  score: 0,
  sortOrder: 1
})

const rules = {
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }]
}
const optionRules = {
  content: [{ required: true, message: '请输入选项内容', trigger: 'blur' }],
  score: [{ required: true, message: '分值不能为空', trigger: 'blur' }]
}

onMounted(() => {
  if (assessmentId.value) {
    formData.value.assessmentId = parseInt(assessmentId.value, 10)
    loadAssessmentDetail()
  } else {
    ElMessage.error('问卷参数缺失')
    goBack()
  }
})

const goBack = () => {
  router.push('/assessment/management')
}

// 通过此前写的 UserAssessmentController.getAssessmentDetail 接口获取该问卷所有题目和选项
// 其实为了复用，管理端也可以调用这个接口看完整结构
const loadAssessmentDetail = async () => {
  isLoading.value = true
  try {
    const res = await request.get<any>({
      url: `/api/assessment/${assessmentId.value}`
    } as any)
    questionList.value = res.questions || []
    // 自动为下一个题目计算默认排序
    if (questionList.value.length > 0) {
      formData.value.sortOrder = questionList.value[questionList.value.length - 1].sortOrder + 1
    }
  } catch (error) {
    ElMessage.error('获取题目失败')
  } finally {
    isLoading.value = false
  }
}

// ----------------- 题目管理 -----------------
const openAddQuestionDialog = () => {
  formData.value.content = ''
  addDialogVisible.value = true
  if (addFormRef.value) addFormRef.value.clearValidate()
}

const submitAdd = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true
      try {
        await request.post({ url: '/api/admin/assessment/question', data: formData.value } as any)
        ElMessage.success('题目添加成功')
        addDialogVisible.value = false
        loadAssessmentDetail()
      } catch (error: any) {
        ElMessage.error(error.message || '添加失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDeleteQuestion = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该题目及其所有选项？', '危险操作', { type: 'error' })
    await request.del({ url: `/api/admin/assessment/question/${id}` } as any)
    ElMessage.success('删除成功')
    loadAssessmentDetail()
  } catch (e) { }
}

// ----------------- 选项管理 -----------------
const openAddOptionDialog = (qId: number) => {
  const q = questionList.value.find((item: any) => item.id === qId)
  let nextSort = 1
  if (q && q.options && q.options.length > 0) {
    nextSort = q.options[q.options.length - 1].sortOrder + 1
  }
  
  currentQuestionIdForOption.value = qId
  optionData.value = {
    questionId: qId,
    content: '',
    score: 0,
    sortOrder: nextSort
  }
  optionDialogVisible.value = true
  if (optionFormRef.value) optionFormRef.value.clearValidate()
}

const submitAddOption = async () => {
  if (!optionFormRef.value) return
  await optionFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      optionSubmitLoading.value = true
      try {
        await request.post({ url: '/api/admin/assessment/option', data: optionData.value } as any)
        ElMessage.success('选项添加成功')
        optionDialogVisible.value = false
        loadAssessmentDetail()
      } catch (error: any) {
        ElMessage.error(error.message || '添加失败')
      } finally {
        optionSubmitLoading.value = false
      }
    }
  })
}

const handleDeleteOption = async (optionId: number, questionId: number) => {
  try {
    await ElMessageBox.confirm('确认删除该选项？', '提示', { type: 'warning' })
    await request.del({ url: `/api/admin/assessment/option/${optionId}` } as any)
    ElMessage.success('删除成功')
    loadAssessmentDetail()
  } catch (e) { }
}

</script>

<style lang="scss" scoped>
.assessment-questions {
  padding: 20px;
  background-color: var(--el-bg-color);
  border-radius: var(--custom-radius);

  .header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      font-size: 18px;
    }
  }

  .options-container {
    padding: 10px 20px;
    background-color: var(--el-fill-color-light);
    border-radius: 4px;
    
    h4 {
      margin-top: 0;
      display: flex;
      align-items: center;
    }
  }
}
</style>
