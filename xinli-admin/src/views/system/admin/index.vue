<template>
  <div class="page-content">
    <ElForm>
      <ElRow :gutter="12">
        <ElCol :xs="24" :sm="12" :lg="6">
          <ElFormItem>
            <ElInput placeholder="请输入管理员用户名或姓名" v-model="queryParams.keyword"></ElInput>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12" :lg="6">
          <ElFormItem>
            <ElButton v-ripple @click="handleSearch">搜索</ElButton>
            <ElButton type="primary" @click="showDialog('add')" v-ripple>新增管理员</ElButton>
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <ArtTable :data="adminList" index>
      <template #default>
        <ElTableColumn label="登录账号(用户名)" prop="username" />
        <ElTableColumn label="姓名" prop="name" />
        <ElTableColumn label="角色" prop="roleName">
          <template #default="scope">
            <ElTag type="success" v-if="scope.row.roleName">
              {{ scope.row.roleName }}
            </ElTag>
            <span v-else>暂无分配</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="状态" prop="status">
          <template #default="scope">
            <ElSwitch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn label="创建时间" prop="createdAt">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </ElTableColumn>
        <ElTableColumn fixed="right" label="操作" width="100px">
          <template #default="scope">
            <ArtButtonMore
              :list="[
                { key: 'edit', label: '编辑修改' },
                { key: 'password', label: '重置密码' },
                { key: 'delete', label: '删除账户' }
              ]"
              @click="buttonMoreClick($event, scope.row)"
            />
          </template>
        </ElTableColumn>
      </template>
    </ArtTable>

    <!-- 分页 (如果 ArtTable 通过属性自动分页可忽略，这里手写一个简易分页供需) -->
    <div style="display: flex; justify-content: flex-end; margin-top: 15px;">
      <ElPagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getTableData"
        @current-change="getTableData"
      />
    </div>

    <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增管理员' : '编辑管理员'"
      width="400px"
      align-center
    >
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="80px">
        <ElFormItem label="用户名" prop="username">
          <ElInput v-model="form.username" :disabled="dialogType === 'edit'" placeholder="用于登录" />
        </ElFormItem>
        <ElFormItem label="姓名" prop="name">
          <ElInput v-model="form.name" placeholder="显示名字" />
        </ElFormItem>
        <ElFormItem label="分配角色" prop="roleId">
          <ElSelect v-model="form.roleId" placeholder="请选择角色" clearable style="width: 100%;">
            <ElOption
              v-for="item in allRoles"
              :key="item.id"
              :label="item.roleName"
              :value="item.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="密码" prop="password" v-if="dialogType === 'add'">
          <ElInput v-model="form.password" type="password" show-password placeholder="不填写则默认为123456" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSwitch v-model="form.status" :active-value="1" :inactive-value="0" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleSubmit(formRef)">提交</ElButton>
        </div>
      </template>
    </ElDialog>

    <ElDialog v-model="resetDialogVisible" title="重置密码" width="400px" align-center>
      <ElForm label-width="80px">
        <ElFormItem label="新密码">
          <ElInput v-model="resetPasswordObj.newPassword" type="password" show-password />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="resetDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="saveResetPassword">重置</ElButton>
        </div>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { ButtonMoreItem } from '@/components/core/forms/art-button-more/index.vue'
import {
  getAdminPage,
  createAdmin,
  updateAdmin,
  deleteAdmin,
  updateAdminStatus,
  resetAdminPassword
} from '@/api/system/adminApi'
import { getAllRoles } from '@/api/system/roleApi'

defineOptions({ name: 'AdminManagement' })

const queryParams = reactive({
  keyword: '',
  current: 1,
  size: 10
})
const total = ref(0)
const adminList = ref<any[]>([])
const allRoles = ref<any[]>([])

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref<FormInstance>()
const form = reactive({
  id: undefined as number | undefined,
  username: '',
  name: '',
  password: '',
  roleId: undefined as number | undefined,
  status: 1
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
})

const resetDialogVisible = ref(false)
const resetPasswordObj = reactive({
  id: null as number | null,
  newPassword: ''
})

onMounted(() => {
  getTableData()
  fetchRoles()
})

const fetchRoles = async () => {
  try {
    const res = await getAllRoles()
    allRoles.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const getTableData = async () => {
  try {
    const res = await getAdminPage(queryParams)
    adminList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  queryParams.current = 1
  getTableData()
}

const handleStatusChange = async (row: any) => {
  try {
    await updateAdminStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (e: any) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(e.message || '更新失败')
  }
}

const showDialog = (type: string, row?: any) => {
  dialogVisible.value = true
  dialogType.value = type

  if (type === 'edit' && row) {
    form.id = row.id
    form.username = row.username
    form.name = row.name
    form.password = ''
    form.roleId = row.roleId || undefined
    form.status = row.status
  } else {
    form.id = undefined
    form.username = ''
    form.name = ''
    form.password = ''
    form.roleId = undefined
    form.status = 1
  }
}

const buttonMoreClick = (item: ButtonMoreItem, row: any) => {
  if (item.key === 'edit') {
    if (row.username === 'admin') {
      ElMessage.warning('内置超级管理员不支持编辑')
      return;
    }
    showDialog('edit', row)
  } else if (item.key === 'password') {
    resetPasswordObj.id = row.id
    resetPasswordObj.newPassword = ''
    resetDialogVisible.value = true
  } else if (item.key === 'delete') {
    if (row.username === 'admin') {
      ElMessage.warning('内置超级管理员不支持删除')
      return;
    }
    deleteAdminHandler(row.id)
  }
}

const handleSubmit = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await createAdmin(form)
          ElMessage.success('新增管理员成功')
        } else {
          await updateAdmin(form.id as number, form)
          ElMessage.success('更新管理员成功')
        }
        dialogVisible.value = false
        getTableData()
      } catch (e: any) {
        ElMessage.error(e.message || '操作失败')
      }
    }
  })
}

const deleteAdminHandler = (id: number) => {
  ElMessageBox.confirm('确定要删除该管理员吗？该操作不可逆！', '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      await deleteAdmin(id)
      ElMessage.success('删除成功')
      getTableData()
    } catch (e: any) {
      ElMessage.error(e.message || '删除失败')
    }
  }).catch(() => {})
}

const saveResetPassword = async () => {
  if (!resetPasswordObj.newPassword) {
    ElMessage.error('密码不能为空')
    return
  }
  if (!resetPasswordObj.id) return

  try {
    await resetAdminPassword(resetPasswordObj.id, { newPassword: resetPasswordObj.newPassword })
    ElMessage.success('重置密码成功')
    resetDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '重置密码失败')
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date)
    .toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
    .replace(/\//g, '-')
}
</script>

<style lang="scss" scoped>
.page-content {
  margin: 0;
}
</style>
