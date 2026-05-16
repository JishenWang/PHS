<template>
  <div class="user-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('common.keyword')">
          <el-input 
            v-model="searchForm.keyword" 
            :placeholder="$t('user.keywordPlaceholder')" 
            clearable
            prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset" :icon="RefreshRight">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">{{ $t('user.userList') }}</span>
            <el-tag type="info" effect="plain">{{ $t('user.totalRecords', { count: tableData.length }) }}</el-tag>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">
              {{ $t('user.addUser') }}
            </el-button>
          </div>
        </div>
      </template>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        stripe
        highlight-current-row
        class="data-table"
        max-height="600"
      >
        <el-table-column type="index" :label="$t('common.no')" width="60" align="center" fixed="left" />
        
        <el-table-column :label="$t('user.userInfo')" min-width="200" fixed="left">
          <template #default="{ row }">
            <div class="user-info-cell">
              <el-avatar :size="40" :src="row.avatar || getAvatar(row.role)" />
              <div class="user-detail">
                <div class="username">{{ row.username }}</div>
                <div class="realname">{{ row.realName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('user.role')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" effect="light" round>
              {{ getRoleName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="$t('user.contact')" min-width="200">
          <template #default="{ row }">
            <div class="contact-info">
              <div class="contact-item">
                <el-icon><Phone /></el-icon>
                <span>{{ row.phone }}</span>
              </div>
              <div class="contact-item">
                <el-icon><Message /></el-icon>
                <span class="email">{{ row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('common.status')" width="100" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1" 
              :inactive-value="0"
              inline-prompt
              :active-text="$t('common.on')"
              :inactive-text="$t('common.off')"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createTime" :label="$t('user.createdAt')" width="160" align="center">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.createTime }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('common.operation')" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">
              {{ $t('common.edit') }}
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              {{ $t('common.delete') }}
            </el-button>
            <el-dropdown trigger="click">
              <el-button type="primary" link>
                {{ $t('user.more') }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :icon="View" @click="handleView(row)">{{ $t('user.viewDetails') }}</el-dropdown-item>
                  <el-dropdown-item :icon="Key" @click="handleResetPassword(row)">{{ $t('user.resetPassword') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="600px"
      destroy-on-close
      class="user-dialog"
    >
      <el-form 
        :model="formData" 
        :rules="formRules" 
        ref="formRef" 
        label-width="100px"
        class="dialog-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('user.username')" prop="username">
              <el-input v-model="formData.username" :placeholder="$t('user.pleaseEnterUsername')" prefix-icon="User" :disabled="!!formData.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('user.realName')" prop="realName">
              <el-input v-model="formData.realName" :placeholder="$t('user.pleaseEnterRealName')" prefix-icon="UserFilled" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="!formData.id">
          <el-col :span="12">
            <el-form-item :label="$t('user.password')" prop="password">
              <el-input 
                v-model="formData.password" 
                type="password" 
                :placeholder="$t('user.pleaseEnterPassword')" 
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('user.confirmPassword')" prop="confirmPassword">
              <el-input 
                v-model="formData.confirmPassword" 
                type="password" 
                :placeholder="$t('user.pleaseConfirmPassword')" 
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('user.role')" prop="role">
              <el-select v-model="formData.role" :placeholder="$t('user.selectRole')" style="width: 100%">
                <el-option :label="$t('user.roleAdmin')" value="admin" />
                <el-option :label="$t('user.roleDoctor')" value="doctor" />
                <el-option :label="$t('user.roleReception')" value="desk" />
                <el-option :label="$t('user.roleCustomer')" value="owner" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('common.phone')" prop="phone">
              <el-input v-model="formData.phone" :placeholder="$t('user.pleaseEnterPhone')" prefix-icon="Phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="$t('user.email')" prop="email">
          <el-input v-model="formData.email" :placeholder="$t('user.pleaseEnterEmail')" prefix-icon="Message" />
        </el-form-item>

        <el-form-item :label="$t('user.remark')">
          <el-input 
            v-model="formData.remark" 
            type="textarea" 
            :rows="3" 
            :placeholder="$t('user.pleaseEnterRemark')"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog 
      :title="$t('user.userDetails')" 
      v-model="detailVisible" 
      width="600px"
      class="user-detail-dialog"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="$t('user.username')">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item :label="$t('user.realName')">{{ currentUser.realName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('user.role')">
          <el-tag :type="getRoleType(currentUser.role)">{{ getRoleName(currentUser.role) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('common.status')">
          <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
            {{ currentUser.status === 1 ? $t('user.enabled') : $t('user.disabled') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('common.phone')">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item :label="$t('user.email')">{{ currentUser.email }}</el-descriptions-item>
        <el-descriptions-item :label="$t('user.createdAt')" :span="2">{{ currentUser.createTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('user.remark')" :span="2">{{ currentUser.remark || $t('common.none') }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog 
      :title="$t('user.resetPassword')" 
      v-model="resetPasswordVisible" 
      width="400px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordRef" label-width="100px">
        <el-form-item :label="$t('user.newPassword')" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            :placeholder="$t('user.pleaseEnterNewPassword')"
            show-password
          />
        </el-form-item>
        <el-form-item :label="$t('user.confirmNewPassword')" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            :placeholder="$t('user.pleaseConfirmNewPassword')"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit" :loading="resetLoading">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Search, 
  RefreshRight, 
  Edit, 
  Delete, 
  View, 
  Key,
  ArrowDown,
  User,
  UserFilled,
  Lock,
  Phone,
  Message,
  Clock
} from '@element-plus/icons-vue'
import { 
  getUserList, 
  addUser, 
  updateUser, 
  deleteUser, 
  updateUserStatus
} from '@/api/admin/admin'

const { t } = useI18n()

const loading = ref(false)
const submitLoading = ref(false)
const resetLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const resetPasswordVisible = ref(false)
const dialogTitle = ref(t('user.addUser'))
const formRef = ref(null)
const passwordRef = ref(null)
const currentUser = ref({})
const currentUserId = ref(null)

const searchForm = reactive({
  keyword: ''
})

const formData = reactive({
  id: null,
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  role: '',
  phone: '',
  email: '',
  remark: ''
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== formData.password) {
    callback(new Error(t('user.passwordMismatch')))
  } else {
    callback()
  }
}

const validateResetConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error(t('user.passwordMismatch')))
  } else {
    callback()
  }
}

const formRules = {
  username: [
    { required: true, message: t('user.pleaseEnterUsername'), trigger: 'blur' },
    { min: 3, max: 20, message: t('user.usernameLength'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('user.pleaseEnterPassword'), trigger: 'blur' },
    { min: 6, max: 20, message: t('user.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('user.pleaseConfirmPassword'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: t('user.pleaseEnterRealName'), trigger: 'blur' }
  ],
  role: [
    { required: true, message: t('user.pleaseSelectRole'), trigger: 'change' }
  ],
  phone: [
    { required: true, message: t('user.pleaseEnterPhone'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('user.invalidPhone'), trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('user.pleaseEnterEmail'), trigger: 'blur' },
    { type: 'email', message: t('user.invalidEmail'), trigger: 'blur' }
  ]
}

const passwordRules = {
  newPassword: [
    { required: true, message: t('user.pleaseEnterNewPassword'), trigger: 'blur' },
    { min: 6, max: 20, message: t('user.passwordLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('user.pleaseConfirmNewPassword'), trigger: 'blur' },
    { validator: validateResetConfirmPassword, trigger: 'blur' }
  ]
}

const tableData = ref([])

// 获取用户列表 - 不分页
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList(searchForm.keyword)
    if (res.code === 200) {
      tableData.value = res.data
    }
  } catch (error) {
    console.error('Failed to fetch user list:', error)
    ElMessage.error(t('user.fetchFailed'))
  } finally {
    loading.value = false
  }
}

const getAvatar = (role) => {
  const avatars = {
    admin: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    doctor: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    desk: 'https://cube.elemecdn.com/9/2c/e22c6b54e2e84d7c9e1e2e4e6f1epng.png',
    owner: 'https://cube.elemecdn.com/1/2c/3ea6beec64369c2642b92c6726f1epng.png'
  }
  return avatars[role] || avatars.admin
}

const getRoleType = (role) => {
  const types = { admin: 'danger', doctor: 'success', desk: 'warning', owner: 'info' }
  return types[role?.toLowerCase()] || 'info'
}

const getRoleName = (role) => {
  const names = { admin: t('user.roleAdmin'), doctor: t('user.roleDoctor'), desk: t('user.roleReception'), owner: t('user.roleCustomer') }
  return names[role?.toLowerCase()] || role
}

const handleSearch = () => {
  fetchUserList()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = t('user.addUser')
  Object.assign(formData, {
    id: null,
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    role: '',
    phone: '',
    email: '',
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = t('user.editUser')
  Object.assign(formData, { ...row, password: '', confirmPassword: '' })
  dialogVisible.value = true
}

const handleView = (row) => {
  currentUser.value = row
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    t('user.deleteConfirm', { username: row.username }),
    t('common.warning'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteUser(row.id)
      if (res.code === 200) {
        ElMessage.success(t('common.deleteSuccess'))
        fetchUserList()
      }
    } catch (error) {
      ElMessage.error(t('common.deleteFailed'))
    }
  })
}

const handleStatusChange = async (row) => {
  try {
    const res = await updateUserStatus(row.id, row.status)
    if (res.code === 200) {
      ElMessage.success(t('user.statusChangeSuccess', { username: row.username, status: row.status === 1 ? t('user.enabled') : t('user.disabled') }))
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(t('common.statusUpdateFailed'))
  }
}

const handleResetPassword = (row) => {
  currentUserId.value = row.id
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  resetPasswordVisible.value = true
}

// 临时方案：重置密码功能暂未实现，仅提示
const handleResetPasswordSubmit = async () => {
  try {
    await passwordRef.value.validate()
    resetLoading.value = true
    
    // 临时提示功能暂未实现
    // 如需完整功能，需要在 admin.js 中添加 resetUserPassword API
    // 并在后端 AdminController 中添加对应接口
    ElMessage.warning(t('user.resetPasswordNotImplemented'))
    resetPasswordVisible.value = false
  } catch (error) {
    ElMessage.error(t('user.passwordResetFailed'))
  } finally {
    resetLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    const api = formData.id ? updateUser : addUser
    const submitData = { ...formData }
    if (formData.id) {
      delete submitData.password
      delete submitData.confirmPassword
    }
    
    const res = await api(submitData)
    
    if (res.code === 200) {
      ElMessage.success(formData.id ? t('common.updateSuccess') : t('common.addSuccess'))
      dialogVisible.value = false
      fetchUserList()
    }
  } catch (error) {
    console.error('Submit failed:', error)
    ElMessage.error(error.response?.data?.message || t('common.operationFailed'))
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
  background: var(--bg-color);
  min-height: calc(100vh - 84px);
}

/* 搜索区域 */
.search-card {
  margin-bottom: 20px;
  border-radius: var(--radius-large);
}

.search-card :deep(.el-card__body) {
  padding: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-select) {
  width: 200px;
}

/* 表格区域 */
.table-card {
  border-radius: var(--radius-large);
}

.table-card :deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left .title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  gap: 10px;
}

/* 表格样式 */
.data-table {
  margin-top: 10px;
}

.data-table :deep(.el-table__header th) {
  background: #f5f7fa;
  font-weight: 600;
  color: var(--text-primary);
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 600;
  color: var(--text-primary);
}

.realname {
  font-size: 12px;
  color: var(--text-secondary);
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-regular);
}

.contact-item .el-icon {
  color: var(--primary-color);
  font-size: 14px;
}

.contact-item .email {
  color: var(--text-secondary);
}

.time-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.time-cell .el-icon {
  color: var(--info-color);
}

/* 对话框 */
.user-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.user-dialog :deep(.el-dialog__body) {
  padding: 30px 20px;
}

.dialog-form :deep(.el-input__wrapper),
.dialog-form :deep(.el-select) {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-form :deep(.el-input__wrapper),
  .search-form :deep(.el-select) {
    width: 100%;
  }
  
  .header-right {
    flex-direction: column;
    gap: 5px;
  }
}
</style>
