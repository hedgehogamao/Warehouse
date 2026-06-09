<template>
  <div class="user-management">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item :label="t('common.keyword')">
          <el-input v-model="searchKeyword" :placeholder="t('user.usernamePlaceholder')" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('user.list') }}</span>
          <el-button type="primary" @click="openDialog('add')">{{ t('user.add') }}</el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="username" :label="t('user.username')" min-width="100" />
        <el-table-column :label="t('user.role')" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
              {{ row.role === 'ADMIN' ? t('user.roleAdmin') : t('user.roleStaff') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="realName" :label="t('user.realName')" min-width="100" />
        <el-table-column prop="phone" :label="t('user.phone')" min-width="120" />
        <el-table-column :label="t('common.status')" min-width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" :disabled="row.username === 'admin'" @change="(val) => handleStatusChange(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('common.createdAt')" min-width="160" />
        <el-table-column :label="t('common.actions')" fixed="right" width="160">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog('edit', row)">{{ t('common.edit') }}</el-button>
            <el-button link type="danger" size="small" :disabled="row.username === 'admin'" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" class="pagination" :current-page="page" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? t('user.add') : t('user.edit')" width="480px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="80px">
        <el-form-item :label="t('user.username')" prop="username">
          <el-input v-model="dialogForm.username" :disabled="dialogType === 'edit'" :placeholder="t('login.usernamePlaceholder')" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" :label="t('user.password')" prop="password">
          <el-input v-model="dialogForm.password" type="password" show-password :placeholder="t('user.passwordPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.realName')" prop="realName">
          <el-input v-model="dialogForm.realName" :placeholder="t('user.realNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.phone')" prop="phone">
          <el-input v-model="dialogForm.phone" :placeholder="t('user.phonePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('user.role')" prop="role">
          <el-select v-model="dialogForm.role" :placeholder="t('user.rolePlaceholder')" style="width: 100%">
            <el-option :label="t('user.roleAdmin')" value="ADMIN" />
            <el-option :label="t('user.roleStaff')" value="STAFF" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleDialogSubmit">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import request from '@/api/request'

const { t } = useI18n()

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUserList({ page: page.value, size: pageSize.value, keyword: searchKeyword.value || undefined })
    userList.value = res.data.records
    total.value = Number(res.data.total) || 0
  } catch (e) {} finally { loading.value = false }
}

function handleSearch() { page.value = 1; loadUsers() }
function handleReset() { searchKeyword.value = ''; page.value = 1; loadUsers() }
function handlePageChange(val) { page.value = val; loadUsers() }

async function handleStatusChange(row, val) {
  try {
    await request.put(`/users/${row.id}/status`, { status: val ? 1 : 0 })
    ElMessage.success(val ? t('user.enabled') : t('user.disabled'))
    loadUsers()
  } catch (e) { loadUsers() }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(t('common.confirmDelete', { name: row.username }), t('common.prompt'), { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    loadUsers()
  } catch (e) { if (e !== 'cancel') {} }
}

const dialogVisible = ref(false)
const dialogType = ref('add')
const dialogLoading = ref(false)
const dialogFormRef = ref(null)
const editingId = ref(null)
const dialogForm = reactive({ username: '', password: '', realName: '', phone: '', role: 'STAFF' })
const dialogRules = {
  username: [{ required: true, message: () => t('login.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: () => t('login.passwordRequired'), trigger: 'blur' }, { min: 6, message: () => t('user.passwordMin'), trigger: 'blur' }],
  realName: [{ required: true, message: () => t('user.realNamePlaceholder'), trigger: 'blur' }],
  role: [{ required: true, message: () => t('user.rolePlaceholder'), trigger: 'change' }]
}

function openDialog(type, row) {
  dialogType.value = type
  dialogVisible.value = true
  if (type === 'edit' && row) {
    editingId.value = row.id
    dialogForm.username = row.username; dialogForm.password = ''; dialogForm.realName = row.realName; dialogForm.phone = row.phone || ''; dialogForm.role = row.role
  } else {
    editingId.value = null
    dialogForm.username = ''; dialogForm.password = ''; dialogForm.realName = ''; dialogForm.phone = ''; dialogForm.role = 'STAFF'
  }
}

async function handleDialogSubmit() {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return
  dialogLoading.value = true
  try {
    if (dialogType.value === 'add') {
      await createUser({ username: dialogForm.username, password: dialogForm.password, realName: dialogForm.realName, phone: dialogForm.phone, role: dialogForm.role })
      ElMessage.success(t('common.addSuccess'))
    } else {
      await updateUser(editingId.value, { realName: dialogForm.realName, phone: dialogForm.phone, role: dialogForm.role })
      ElMessage.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false; loadUsers()
  } catch (e) {} finally { dialogLoading.value = false }
}

onMounted(() => { loadUsers() })
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
