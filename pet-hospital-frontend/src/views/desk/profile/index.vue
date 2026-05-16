<template>
  <div class="profile-page" v-loading="pageLoading">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><User /></el-icon>
        {{ $t('profile.title') }}
      </h2>
    </div>

    <div class="profile-container">
      <!-- 左侧：Basic Info -->
      <div class="left-section">
        <!-- 头像卡片 -->
        <el-card class="avatar-card" shadow="hover">
          <div class="avatar-wrapper">
            <el-avatar :size="120" :src="localUserInfo.avatar" class="user-avatar">
              <el-icon :size="60"><UserFilled /></el-icon>
            </el-avatar>
            <div class="avatar-actions">
              <el-upload
                action="/api/common/upload"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :on-error="handleAvatarError"
                :before-upload="beforeAvatarUpload"
                :headers="uploadHeaders"
                name="file"
                with-credentials
              >
                <el-button type="primary" size="small" round>
                  <el-icon><Camera /></el-icon>
                  {{ $t('profile.changeAvatar') }}
                </el-button>
              </el-upload>
            </div>
          </div>
          <div class="user-basic">
            <h3 class="user-name">{{ localUserInfo.realName || localUserInfo.username || $t('profile.receptionCashier') }}</h3>
            <p class="user-role">{{ localUserInfo.roleName || $t('profile.receptionCashier') }}</p>
          </div>
        </el-card>

        <!-- Account Security -->
        <el-card class="security-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Lock /></el-icon>
              <span>{{ $t('profile.accountSecurity') }}</span>
            </div>
          </template>
          <div class="security-list">
            <div class="security-item">
              <div class="item-info">
                <el-icon class="success"><CircleCheck /></el-icon>
                <div class="item-text">
                  <div class="item-title">{{ $t('profile.loginPassword') }}</div>
                  <div class="item-desc">{{ $t('profile.passwordDesc') }}</div>
                </div>
              </div>
              <el-button type="primary" link @click="showPasswordDialog = true">
                {{ $t('profile.modify') }}
              </el-button>
            </div>
            <div class="security-item">
              <div class="item-info">
                <el-icon class="success"><CircleCheck /></el-icon>
                <div class="item-text">
                  <div class="item-title">{{ $t('profile.phoneBinding') }}</div>
                  <div class="item-desc">{{ maskPhone(localUserInfo.phone) || $t('profile.notBound') }}</div>
                </div>
              </div>
              <el-button type="primary" link @click="openPhoneDialog">
                {{ localUserInfo.phone ? $t('profile.change') : $t('profile.bind') }}
              </el-button>
            </div>
            <div class="security-item">
              <div class="item-info">
                <el-icon :class="localUserInfo.email ? 'success' : 'warning'">
                  <component :is="localUserInfo.email ? 'CircleCheck' : 'Warning'" />
                </el-icon>
                <div class="item-text">
                  <div class="item-title">{{ $t('profile.emailBinding') }}</div>
                  <div class="item-desc">
                    {{ localUserInfo.email ? maskEmail(localUserInfo.email) : $t('profile.emailNotBound') }}
                  </div>
                </div>
              </div>
              <el-button type="primary" link @click="openEmailDialog">
                {{ localUserInfo.email ? $t('profile.change') : $t('profile.bind') }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：详细信息编辑 -->
      <div class="right-section">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon><EditPen /></el-icon>
                <span>{{ $t('profile.basicInfo') }}</span>
              </div>
              <el-button
                :type="isEditing ? 'success' : 'primary'"
                @click="toggleEdit"
                :loading="saving"
              >
                <el-icon><component :is="isEditing ? 'Check' : 'Edit'" /></el-icon>
                {{ isEditing ? $t('profile.save') : $t('profile.edit') }}
              </el-button>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
            class="profile-form"
            :disabled="!isEditing"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="$t('profile.realName')" prop="realName">
                  <el-input v-model="formData.realName" :placeholder="$t('profile.placeholderRealName')" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('profile.username')">
                  <el-input v-model="formData.username" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="$t('profile.gender')" prop="gender">
                  <el-radio-group v-model="formData.gender">
                    <el-radio :value="1">{{ $t('profile.male') }}</el-radio>
                    <el-radio :value="0">{{ $t('profile.female') }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('profile.phone')">
                  <el-input v-model="formData.phone" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item :label="$t('profile.role')">
                  <el-tag type="success">{{ formData.roleName || $t('profile.receptionCashier') }}</el-tag>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('profile.registrationTime')">
                  <el-input v-model="formData.createTime" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item :label="$t('profile.email')">
              <el-input v-model="formData.email" :placeholder="$t('profile.placeholderEmail')" />
            </el-form-item>

            <el-form-item :label="$t('profile.introduction')" prop="introduction">
              <el-input
                v-model="formData.introduction"
                type="textarea"
                :rows="4"
                :placeholder="$t('profile.placeholderIntroduction')"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>

    <!-- Change密码对话框 -->
    <el-dialog v-model="showPasswordDialog" :title="$t('profile.changePassword')" width="450px" destroy-on-close>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item :label="$t('profile.oldPassword')" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password :placeholder="$t('profile.placeholderOldPassword')" />
        </el-form-item>
        <el-form-item :label="$t('profile.newPassword')" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password :placeholder="$t('profile.placeholderNewPassword')" />
        </el-form-item>
        <el-form-item :label="$t('profile.confirmPassword')" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password :placeholder="$t('profile.placeholderConfirmPassword')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">{{ $t('profile.cancel') }}</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">{{ $t('profile.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- Change手机对话框 -->
    <el-dialog v-model="showPhoneDialog" :title="localUserInfo.phone ? $t('profile.changePhoneTitle') : $t('profile.bindPhoneTitle')" width="450px" destroy-on-close>
      <el-steps :active="phoneStep" finish-status="success" simple class="phone-steps">
        <el-step :title="$t('profile.verifyIdentity')" />
        <el-step :title="$t('profile.bindNewPhone')" />
      </el-steps>

      <div v-if="phoneStep === 0" class="verify-step">
        <p class="step-desc">
          {{ localUserInfo.phone ? $t('profile.stepDescVerify', { phone: maskPhone(localUserInfo.phone) }) : $t('profile.stepDescBind') }}
        </p>
        <el-form :model="phoneForm" label-width="0">
          <el-form-item v-if="!localUserInfo.phone">
            <el-input v-model="phoneForm.newPhone" :placeholder="$t('profile.placeholderPhone')">
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="phoneForm.code" :placeholder="$t('profile.placeholderCode')">
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
              <template #append>
                <el-button
                  :disabled="codeCountdown > 0"
                  @click="sendPhoneCode"
                  :loading="sendingCode"
                >
                  {{ codeCountdown > 0 ? $t('profile.retryIn', { count: codeCountdown }) : $t('profile.sendCode') }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>

      <div v-else class="bind-step">
        <p class="step-desc">{{ $t('profile.stepDescNewPhone') }}</p>
        <el-form :model="phoneForm" label-width="0">
          <el-form-item>
            <el-input v-model="phoneForm.newPhone" :placeholder="$t('profile.placeholderNewPhone')">
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="phoneForm.newCode" :placeholder="$t('profile.placeholderCode')">
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
              <template #append>
                <el-button
                  :disabled="newCodeCountdown > 0"
                  @click="sendNewPhoneCode"
                  :loading="sendingNewCode"
                >
                  {{ newCodeCountdown > 0 ? $t('profile.retryIn', { count: newCodeCountdown }) : $t('profile.sendCode') }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="showPhoneDialog = false">{{ $t('profile.cancel') }}</el-button>
        <el-button type="primary" @click="handlePhoneNext" :loading="changingPhone">
          {{ phoneStep === 0 ? $t('profile.next') : $t('profile.confirmBinding') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 绑定邮箱对话框 -->
    <el-dialog v-model="showEmailDialog" :title="localUserInfo.email ? $t('profile.changeEmailTitle') : $t('profile.bindEmailTitle')" width="450px" destroy-on-close>
      <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-width="0">
        <el-form-item prop="email">
          <el-input v-model="emailForm.email" :placeholder="$t('profile.placeholderEmail')">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <el-input v-model="emailForm.code" :placeholder="$t('profile.placeholderCode')">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
            <template #append>
              <el-button
                :disabled="emailCodeCountdown > 0"
                @click="sendEmailCode"
                :loading="sendingEmailCode"
              >
                {{ emailCodeCountdown > 0 ? $t('profile.retryIn', { count: emailCodeCountdown }) : $t('profile.sendCode') }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEmailDialog = false">{{ $t('profile.cancel') }}</el-button>
        <el-button type="primary" @click="handleBindEmail" :loading="bindingEmail">{{ $t('profile.confirmBindEmail') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, UserFilled, Camera, Lock, CircleCheck, Warning,
  EditPen, Edit, Check, Phone, Message
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { updateUser } from '@/api/admin/admin'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const userStore = useUserStore()

// 本地用户信息
const localUserInfo = ref({
  userId: '',
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  roleName: '',
  introduction: '',
  gender: 1,
  createTime: ''
})

// 页面加载状态
const pageLoading = ref(false)
const saving = ref(false)

// 上传请求头
const uploadHeaders = computed(() => {
  const headers = {}
  const token = userStore.token
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
})

// 编辑状态
const isEditing = ref(false)
const formRef = ref(null)
const formData = reactive({
  realName: '',
  username: '',
  gender: 1,
  phone: '',
  email: '',
  roleName: '',
  introduction: '',
  createTime: ''
})

const formRules = {
  realName: [{ required: true, message: t('profile.pleaseEnterPhone'), trigger: 'blur' }]
}

// 对话框显示状态
const showPasswordDialog = ref(false)
const showPhoneDialog = ref(false)
const showEmailDialog = ref(false)

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const changingPassword = ref(false)

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error(t('profile.passwordsDoNotMatch')))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: t('profile.pleaseEnterOldPassword'), trigger: 'blur' }],
  newPassword: [
    { required: true, message: t('profile.pleaseEnterOldPassword'), trigger: 'blur' },
    { min: 6, max: 20, message: t('profile.passwordLengthError'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('profile.pleaseConfirmPassword'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// Phone Binding
const phoneStep = ref(0)
// phoneDialogTitle uses inline $t in template
const phoneForm = reactive({
  newPhone: '',
  code: '',
  newCode: ''
})
const codeCountdown = ref(0)
const newCodeCountdown = ref(0)
const sendingCode = ref(false)
const sendingNewCode = ref(false)
const changingPhone = ref(false)

// Email Binding
// emailDialogTitle uses inline $t in template
const emailFormRef = ref(null)
const emailForm = reactive({
  email: '',
  code: ''
})
const emailRules = {
  email: [
    { required: true, message: t('profile.pleaseEnterEmail'), trigger: 'blur' },
    { type: 'email', message: t('profile.pleaseEnterValidEmail'), trigger: 'blur' }
  ],
  code: [{ required: true, message: t('profile.pleaseEnterCode'), trigger: 'blur' }]
}
const emailCodeCountdown = ref(0)
const sendingEmailCode = ref(false)
const bindingEmail = ref(false)

// 初始化表单数据
const initFormData = () => {
  formData.realName = localUserInfo.value.realName || ''
  formData.username = localUserInfo.value.username || ''
  formData.gender = localUserInfo.value.gender ?? 1
  formData.phone = localUserInfo.value.phone || ''
  formData.email = localUserInfo.value.email || ''
  formData.roleName = localUserInfo.value.roleName || ''
  formData.introduction = localUserInfo.value.introduction || ''
  formData.createTime = localUserInfo.value.createTime || ''
}

// 刷新本地用户信息
const refreshLocalUserInfo = () => {
  const storeInfo = userStore.userInfo || {}
  localUserInfo.value = {
    userId: storeInfo.userId || storeInfo.id || '',
    username: storeInfo.username || t('profile.receptionCashier'),
    realName: storeInfo.realName || storeInfo.username || t('profile.receptionCashier'),
    phone: storeInfo.phone || '',
    email: storeInfo.email || '',
    avatar: storeInfo.avatar || '',
    roleName: storeInfo.roleName || t('profile.receptionCashier'),
    introduction: storeInfo.introduction || '',
    gender: storeInfo.gender ?? 1,
    createTime: storeInfo.createTime || ''
  }
  initFormData()
}

// ========== 头像上传 ==========
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error(t('profile.onlyJpgPng'))
    return false
  }
  if (!isLt2M) {
    ElMessage.error(t('profile.imageSizeExceeded'))
    return false
  }
  return true
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200 && res.data) {
    localUserInfo.value.avatar = res.data
    userStore.updateAvatar(res.data)
    ElMessage.success(t('profile.avatarUpdated'))
  } else {
    ElMessage.error(res.message || res.msg || t('profile.uploadFailed'))
  }
}

const handleAvatarError = () => {
  ElMessage.error(t('profile.avatarUploadFailed'))
}

// ========== Basic Info编辑 ==========
const toggleEdit = async () => {
  if (isEditing.value) {
    try {
      await formRef.value.validate()
      saving.value = true

      // 调用后端 API 保存
      const userId = userStore.userInfo?.id || userStore.userInfo?.userId
      if (userId) {
        const res = await updateUser({
          id: userId,
          realName: formData.realName,
          gender: formData.gender,
          email: formData.email,
          introduction: formData.introduction
        })
        if (res.code !== 200) {
          ElMessage.error(res.msg || t('profile.changeFailed'))
          return
        }
      }

      // 更新本地用户信息
      localUserInfo.value.realName = formData.realName
      localUserInfo.value.gender = formData.gender
      localUserInfo.value.email = formData.email
      localUserInfo.value.introduction = formData.introduction

      // 更新 store
      userStore.updateUserInfo({
        realName: formData.realName,
        gender: formData.gender,
        email: formData.email,
        introduction: formData.introduction
      })

      ElMessage.success(t('profile.savedSuccessfully'))
      isEditing.value = false
    } catch (error) {
      if (error !== false) {
        ElMessage.error(error.message || t('profile.pleaseCompleteRequired'))
      }
    } finally {
      saving.value = false
    }
  } else {
    initFormData()
    isEditing.value = true
  }
}

// ========== Change密码 ==========
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    const result = await userStore.changePassword(passwordForm.oldPassword, passwordForm.newPassword)

    if (result.success) {
      ElMessage.success(t('profile.passwordChanged'))
      showPasswordDialog.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      setTimeout(() => {
        userStore.logout()
      }, 1500)
    } else {
      ElMessage.error(result.message || t('profile.changeFailed'))
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || t('profile.changeFailed'))
    }
  } finally {
    changingPassword.value = false
  }
}

// ========== Phone Binding ==========
const openPhoneDialog = () => {
  phoneStep.value = 0
  phoneForm.newPhone = ''
  phoneForm.code = ''
  phoneForm.newCode = ''
  showPhoneDialog.value = true
}

const startCountdown = (refName) => {
  const refMap = { codeCountdown, newCodeCountdown, emailCodeCountdown }
  const target = refMap[refName]
  target.value = 60
  const timer = setInterval(() => {
    target.value--
    if (target.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const sendPhoneCode = async () => {
  const phone = localUserInfo.value.phone || phoneForm.newPhone
  if (!phone) {
    ElMessage.warning(t('profile.pleaseEnterPhone'))
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning(t('profile.pleaseEnterValidPhone'))
    return
  }

  sendingCode.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success(t('profile.codeSent'))
    startCountdown('codeCountdown')
  } catch (error) {
    ElMessage.error(t('profile.sendFailed'))
  } finally {
    sendingCode.value = false
  }
}

const sendNewPhoneCode = async () => {
  if (!phoneForm.newPhone) {
    ElMessage.warning(t('profile.pleaseEnterPhone'))
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.warning(t('profile.pleaseEnterValidPhone'))
    return
  }

  sendingNewCode.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success(t('profile.codeSent'))
    startCountdown('newCodeCountdown')
  } catch (error) {
    ElMessage.error(t('profile.sendFailed'))
  } finally {
    sendingNewCode.value = false
  }
}

const handlePhoneNext = async () => {
  if (phoneStep.value === 0) {
    if (!phoneForm.code) {
      ElMessage.warning(t('profile.pleaseEnterCode'))
      return
    }

    changingPhone.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      phoneStep.value = 1
    } catch (error) {
      ElMessage.error(t('profile.verificationFailed'))
    } finally {
      changingPhone.value = false
    }
  } else {
    if (!phoneForm.newPhone) {
      ElMessage.warning(t('profile.pleaseEnterPhone'))
      return
    }
    if (!phoneForm.newCode) {
      ElMessage.warning(t('profile.pleaseEnterCode'))
      return
    }

    changingPhone.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      localUserInfo.value.phone = phoneForm.newPhone
      userStore.updateUserInfo({ phone: phoneForm.newPhone })
      ElMessage.success(t('profile.phoneChanged'))
      showPhoneDialog.value = false
    } catch (error) {
      ElMessage.error(t('profile.changeFailed'))
    } finally {
      changingPhone.value = false
    }
  }
}

// ========== Email Binding ==========
const openEmailDialog = () => {
  emailForm.email = localUserInfo.value.email || ''
  emailForm.code = ''
  showEmailDialog.value = true
}

const sendEmailCode = async () => {
  if (!emailForm.email) {
    ElMessage.warning(t('profile.pleaseEnterEmail'))
    return
  }

  sendingEmailCode.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success(t('profile.emailSent'))
    startCountdown('emailCodeCountdown')
  } catch (error) {
    ElMessage.error(t('profile.sendFailed'))
  } finally {
    sendingEmailCode.value = false
  }
}

const handleBindEmail = async () => {
  try {
    await emailFormRef.value.validate()
    bindingEmail.value = true

    await new Promise(resolve => setTimeout(resolve, 500))
    localUserInfo.value.email = emailForm.email
    userStore.updateUserInfo({ email: emailForm.email })
    ElMessage.success(localUserInfo.value.email ? t('profile.emailChanged') : t('profile.emailBound'))
    showEmailDialog.value = false
  } catch (error) {
    if (error !== false) {
      ElMessage.error(t('profile.operationFailed'))
    }
  } finally {
    bindingEmail.value = false
  }
}

// ========== 工具函数 ==========
const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const maskEmail = (email) => {
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (!domain) return email
  if (name.length <= 2) {
    return `${name[0]}***@${domain}`
  }
  const maskedName = name.slice(0, 2) + '***' + name.slice(-1)
  return `${maskedName}@${domain}`
}

// ========== 生命周期 ==========
onMounted(async () => {
  pageLoading.value = true
  await userStore.refreshUserInfo()
  refreshLocalUserInfo()
  pageLoading.value = false
})
</script>

<style scoped lang="scss">
.profile-page {
  .page-header {
    margin-bottom: 25px;

    .page-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 24px;
      font-weight: 600;
      color: #1e293b;

      .el-icon {
        color: #059669;
      }
    }
  }

  .profile-container {
    display: grid;
    grid-template-columns: 380px 1fr;
    gap: 25px;

    .left-section {
      display: flex;
      flex-direction: column;
      gap: 20px;

      .avatar-card {
        border-radius: 16px;
        text-align: center;
        padding: 20px;

        .avatar-wrapper {
          position: relative;
          display: inline-block;
          margin-bottom: 20px;

          .user-avatar {
            border: 4px solid #dcfce7;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

            :deep(img) {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
          }

          .avatar-actions {
            margin-top: 15px;
          }
        }

        .user-basic {
          margin-bottom: 25px;

          .user-name {
            font-size: 22px;
            font-weight: 600;
            color: #1e293b;
            margin-bottom: 8px;
          }

          .user-role {
            color: #64748b;
            font-size: 14px;
          }
        }
      }

      .security-card {
        border-radius: 16px;

        .card-header {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 600;

          .el-icon {
            color: #059669;
          }
        }

        .security-list {
          .security-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 18px 0;
            border-bottom: 1px solid #f1f5f9;

            &:last-child {
              border-bottom: none;
              padding-bottom: 0;
            }

            &:first-child {
              padding-top: 0;
            }

            .item-info {
              display: flex;
              align-items: center;
              gap: 12px;

              .el-icon {
                font-size: 24px;

                &.success {
                  color: #10b981;
                }

                &.warning {
                  color: #f59e0b;
                }
              }

              .item-text {
                .item-title {
                  font-weight: 500;
                  color: #334155;
                }

                .item-desc {
                  font-size: 13px;
                  color: #94a3b8;
                  margin-top: 4px;
                }
              }
            }
          }
        }
      }
    }

    .right-section {
      .info-card {
        border-radius: 16px;

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .header-left {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;

            .el-icon {
              color: #059669;
            }
          }
        }

        .profile-form {
          padding: 20px 10px;

          .full-width {
            width: 100%;
          }
        }
      }
    }
  }
}

// 对话框样式
.verify-step,
.bind-step {
  padding: 20px;

  .step-desc {
    color: #64748b;
    margin-bottom: 20px;
    text-align: center;
  }
}

.phone-steps {
  margin-bottom: 20px;
}
</style>
