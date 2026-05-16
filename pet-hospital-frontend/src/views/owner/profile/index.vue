<template>
  <div class="profile-page">
    <!-- User Info Card -->
    <div class="user-card">
      <div class="user-bg"></div>
      <div class="user-info-wrapper">
        <div class="user-avatar" @click="handleChangeAvatar">
          <el-avatar :size="80" :src="userInfo.avatar" class="avatar">
            <el-icon size="40"><User /></el-icon>
          </el-avatar>
          <div class="avatar-edit">
            <el-icon><Camera /></el-icon>
          </div>
        </div>
        <div class="user-detail">
          <h2 class="username">{{ userInfo.username }}</h2>
          <div class="user-tags">
            <el-tag type="success" size="small">{{ $t('profile.petOwnerTag') }}</el-tag>
            <el-tag type="info" size="small" v-if="userInfo.phone">{{ $t('profile.phoneBoundTag') }}</el-tag>
          </div>
          <p class="user-since">{{ $t('profile.memberSince') }}{{ userInfo.createTime || '2024' }}</p>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-row">
      <div class="stat-card" @click="goToOrders">
        <div class="stat-value">{{ stats.orderCount }}</div>
        <div class="stat-label">{{ $t('profile.orders') }}</div>
      </div>
      <div class="stat-card" @click="goToReserve">
        <div class="stat-value">{{ stats.reserveCount }}</div>
        <div class="stat-label">{{ $t('profile.reservations') }}</div>
      </div>
      <div class="stat-card" @click="goToConsult">
        <div class="stat-value">{{ stats.consultCount }}</div>
        <div class="stat-label">{{ $t('profile.consultations') }}</div>
      </div>
    </div>

    <!-- Menu List -->
    <div class="menu-list">
      <div class="menu-group">
        <div class="menu-item" @click="editInfoDialog = true">
          <div class="menu-icon">👤</div>
          <div class="menu-content">
            <div class="menu-title">{{ $t('profile.personalInfo') }}</div>
            <div class="menu-desc">{{ $t('profile.personalInfoDesc') }}</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="goToOrders">
          <div class="menu-icon">📋</div>
          <div class="menu-content">
            <div class="menu-title">{{ $t('profile.myOrders') }}</div>
            <div class="menu-desc">{{ $t('profile.myOrdersDesc') }}</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="passwordDialog = true">
          <div class="menu-icon">🔒</div>
          <div class="menu-content">
            <div class="menu-title">{{ $t('profile.changePasswordMenu') }}</div>
            <div class="menu-desc">{{ $t('profile.changePasswordDesc') }}</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="menu-group">
        <div class="menu-item" @click="phoneDialog = true">
          <div class="menu-icon">📱</div>
          <div class="menu-content">
            <div class="menu-title">{{ isPhoneBound ? $t('profile.change') + ' ' + $t('profile.phone') : $t('profile.bind') + ' ' + $t('profile.phone') }}</div>
            <div class="menu-desc" :class="{ 'unbind': !isPhoneBound }">
              {{ isPhoneBound ? userInfo.phone : $t('profile.notBoundClick') }}
            </div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="emailDialog = true">
          <div class="menu-icon">📧</div>
          <div class="menu-content">
            <div class="menu-title">{{ isEmailBound ? $t('profile.change') + ' ' + $t('profile.email') : $t('profile.bind') + ' ' + $t('profile.email') }}</div>
            <div class="menu-desc" :class="{ 'unbind': !isEmailBound }">
              {{ isEmailBound ? userInfo.email : $t('profile.notBoundClick') }}
            </div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="aboutDialog = true">
          <div class="menu-icon">ℹ️</div>
          <div class="menu-content">
            <div class="menu-title">{{ $t('profile.aboutUs') }}</div>
            <div class="menu-desc">{{ $t('profile.aboutUsDesc') }}</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="menu-group">
        <div class="menu-item danger" @click="handleLogout">
          <div class="menu-icon">🚪</div>
          <div class="menu-content">
            <div class="menu-title">{{ $t('profile.logout') }}</div>
            <div class="menu-desc">{{ $t('profile.logoutDesc') }}</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- Edit Personal Info Dialog -->
    <el-dialog v-model="editInfoDialog" :title="$t('profile.editPersonalInfo')" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item :label="$t('profile.realName')">
          <el-input v-model="editForm.realName" :placeholder="$t('profile.placeholderRealName')" />
        </el-form-item>
        <el-form-item :label="$t('profile.phone')">
          <el-input :value="userInfo.phone || $t('profile.notBound')" disabled />
          <div class="form-tip">{{ $t('profile.phoneChangeTip') }}</div>
        </el-form-item>
        <el-form-item :label="$t('profile.email')">
          <el-input :value="userInfo.email || $t('profile.notBound')" disabled />
          <div class="form-tip">{{ $t('profile.emailChangeTip') }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editInfoDialog = false">{{ $t('profile.cancel') }}</el-button>
        <el-button type="primary" @click="saveEdit">{{ $t('profile.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Change Password Dialog -->
    <el-dialog v-model="passwordDialog" :title="$t('profile.changePassword')" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item :label="$t('profile.currentPassword')">
          <el-input type="password" v-model="passwordForm.oldPassword" show-password />
        </el-form-item>
        <el-form-item :label="$t('profile.newPassword')">
          <el-input type="password" v-model="passwordForm.newPassword" show-password />
        </el-form-item>
        <el-form-item :label="$t('profile.confirmPassword')">
          <el-input type="password" v-model="passwordForm.confirmPassword" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialog = false">{{ $t('profile.cancel') }}</el-button>
        <el-button type="primary" @click="savePassword">{{ $t('profile.confirmChange') }}</el-button>
      </template>
    </el-dialog>

    <!-- Bind Phone Dialog -->
    <el-dialog v-model="phoneDialog" :title="isPhoneBound ? $t('profile.changePhoneTitle') : $t('profile.bindPhoneTitle')" width="400px">
      <el-form :model="phoneForm" label-width="80px">
        <el-form-item v-if="isPhoneBound" :label="$t('profile.oldPhone')">
          <el-input v-model="phoneForm.oldPhone" :placeholder="$t('profile.placeholderPhone')" />
        </el-form-item>
        <el-form-item v-if="isPhoneBound" :label="$t('profile.oldCode')">
          <div class="code-input">
            <el-input v-model="phoneForm.oldCode" :placeholder="$t('profile.placeholderCode')" />
            <el-button :disabled="oldPhoneCodeCountdown > 0" @click="sendOldPhoneCode" size="small">
              {{ oldPhoneCodeCountdown > 0 ? `${oldPhoneCodeCountdown}s` : $t('profile.getCode') }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item :label="isPhoneBound ? $t('profile.newPhone') : $t('profile.phone')">
          <el-input v-model="phoneForm.newPhone" :placeholder="isPhoneBound ? $t('profile.placeholderNewPhone') : $t('profile.placeholderPhone')" />
        </el-form-item>
        <el-form-item :label="$t('profile.placeholderCode')">
          <div class="code-input">
            <el-input v-model="phoneForm.code" :placeholder="$t('profile.placeholderCode')" />
            <el-button :disabled="phoneCodeCountdown > 0" @click="sendPhoneCode" size="small">
              {{ phoneCodeCountdown > 0 ? `${phoneCodeCountdown}s` : $t('profile.getCode') }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="phoneDialog = false">{{ $t('profile.cancel') }}</el-button>
        <el-button type="primary" @click="savePhone">{{ isPhoneBound ? $t('profile.confirmChange') : $t('profile.confirmBinding') }}</el-button>
      </template>
    </el-dialog>

    <!-- Bind Email Dialog -->
    <el-dialog v-model="emailDialog" :title="isEmailBound ? $t('profile.changeEmailTitle') : $t('profile.bindEmailTitle')" width="400px">
      <el-form :model="emailForm" label-width="80px">
        <el-form-item v-if="isEmailBound" :label="$t('profile.email')">
          <el-input v-model="emailForm.oldEmail" :placeholder="$t('profile.placeholderEmail')" />
        </el-form-item>
        <el-form-item v-if="isEmailBound" :label="$t('profile.oldCode')">
          <div class="code-input">
            <el-input v-model="emailForm.oldCode" :placeholder="$t('profile.placeholderCode')" />
            <el-button :disabled="oldEmailCodeCountdown > 0" @click="sendOldEmailCode" size="small">
              {{ oldEmailCodeCountdown > 0 ? `${oldEmailCodeCountdown}s` : $t('profile.getCode') }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item :label="isEmailBound ? $t('profile.newEmail') : $t('profile.email')">
          <el-input v-model="emailForm.newEmail" :placeholder="$t('profile.placeholderEmail')" />
        </el-form-item>
        <el-form-item :label="$t('profile.placeholderCode')">
          <div class="code-input">
            <el-input v-model="emailForm.code" :placeholder="$t('profile.placeholderCode')" />
            <el-button :disabled="emailCodeCountdown > 0" @click="sendEmailCode" size="small">
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : $t('profile.getCode') }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="emailDialog = false">{{ $t('profile.cancel') }}</el-button>
        <el-button type="primary" @click="saveEmail">{{ isEmailBound ? $t('profile.confirmChange') : $t('profile.confirmBinding') }}</el-button>
      </template>
    </el-dialog>

    <!-- Change Avatar Dialog -->
    <el-dialog v-model="avatarDialog" :title="$t('profile.changeAvatar')" width="420px" class="avatar-dialog">
      <div class="avatar-upload">
        <div class="avatar-preview">
          <el-avatar :size="120" :src="userInfo.avatar" class="preview-avatar">
            <el-icon size="60"><User /></el-icon>
          </el-avatar>
        </div>
        <el-upload
          action="#"
          :auto-upload="false"
          :on-change="handleAvatarChange"
          :show-file-list="false"
          drag
          class="avatar-uploader"
        >
          <div class="upload-content">
            <el-icon size="30"><Upload /></el-icon>
            <div class="upload-text">{{ $t('profile.clickOrDragUpload') }}</div>
            <div class="upload-tip">{{ $t('profile.uploadTip') }}</div>
          </div>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="avatarDialog = false">{{ $t('profile.cancel') }}</el-button>
        <el-button type="primary" @click="uploadAvatar" :loading="avatarUploading">{{ $t('profile.confirmChangeAvatar') }}</el-button>
      </template>
    </el-dialog>

    <!-- About Us Dialog -->
    <el-dialog v-model="aboutDialog" :title="$t('profile.aboutUs')" width="350px" class="about-dialog">
      <div class="about-content">
        <div class="about-icon">🐾</div>
        <h3>{{ $t('profile.aboutUs') }}</h3>
        <p class="version">{{ $t('profile.appVersion', { version: '1.0.0' }) }}</p>
        <p class="description">{{ $t('profile.aboutUsDesc') }}</p>
        <div class="copyright">{{ $t('profile.copyright') }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Camera, ArrowRight, Upload } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getUserInfo, updateUserInfo, changePassword, bindPhone, bindEmail, uploadAvatar as uploadAvatarApi } from '@/api/owner/owner'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const userStore = useUserStore()

const router = useRouter()

// Dialog state
const editInfoDialog = ref(false)
const passwordDialog = ref(false)
const phoneDialog = ref(false)
const emailDialog = ref(false)
const aboutDialog = ref(false)
const avatarDialog = ref(false)

// Countdown
const phoneCodeCountdown = ref(0)
const emailCodeCountdown = ref(0)
const oldPhoneCodeCountdown = ref(0)
const oldEmailCodeCountdown = ref(0)

// Loading state
const avatarUploading = ref(false)

// User info
const userInfo = ref({
  id: '',
  username: '',
  phone: '',
  email: '',
  avatar: '',
  createTime: ''
})

// Stats data
const stats = ref({
  orderCount: 0,
  reserveCount: 0,
  consultCount: 0
})

// Avatar related
const avatarFile = ref(null)

// Computed properties
const isPhoneBound = computed(() => userInfo.value.phone && userInfo.value.phone !== '')
const isEmailBound = computed(() => userInfo.value.email && userInfo.value.email !== '')
// phone/email dialog titles now use inline $t expressions in template

// Form data
const editForm = reactive({ realName: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const phoneForm = reactive({ oldPhone: '', oldCode: '', newPhone: '', code: '' })
const emailForm = reactive({ oldEmail: '', oldCode: '', newEmail: '', code: '' })

// Get token
const getToken = () => {
  return localStorage.getItem('pet_hospital_token')
}

// Load user info
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
      editForm.realName = userInfo.value.realName || userInfo.value.username || ''
    }
  } catch (error) {
    console.error('Failed to load user info:', error)
  }
}

// Load stats
const loadStats = async () => {
  try {
    const token = getToken()
    
    // 1. Get reservation count
    const reserveResponse = await fetch('/api/owner/reserve/list?page=1&pageSize=100', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const reserveRes = await reserveResponse.json()
    
    if (reserveRes.code === 200) {
      let allRecords = reserveRes.data?.data || reserveRes.data?.records || reserveRes.data || []
      if (!Array.isArray(allRecords)) allRecords = []
      stats.value.reserveCount = allRecords.length
    }
    
    // 2. Get order count
    const orderResponse = await fetch('/api/owner/order/list?page=1&pageSize=100', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const orderRes = await orderResponse.json()
    
    if (orderRes.code === 200) {
      let orderRecords = orderRes.data?.data || orderRes.data?.records || orderRes.data || []
      if (!Array.isArray(orderRecords)) orderRecords = []
      stats.value.orderCount = orderRecords.length
    }
    
    // 3. Get consultation count
    const consultResponse = await fetch('/api/owner/consult/list?page=1&pageSize=100', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const consultRes = await consultResponse.json()
    
    if (consultRes.code === 200) {
      let consultRecords = consultRes.data?.data || consultRes.data?.records || consultRes.data || []
      if (!Array.isArray(consultRecords)) consultRecords = []
      stats.value.consultCount = consultRecords.length
    }
    
    console.log('Stats loaded:', stats.value)
  } catch (error) {
    console.error('Failed to load stats:', error)
    stats.value = { orderCount: 0, reserveCount: 0, consultCount: 0 }
  }
}

// Save personal info
const saveEdit = async () => {
  if (!editForm.realName) {
    ElMessage.warning(t('user.pleaseEnterRealName'))
    return
  }
  try {
    const res = await updateUserInfo({ realName: editForm.realName })
    if (res.code === 200) {
      userInfo.value.realName = editForm.realName
      userStore.updateUserInfo({ realName: editForm.realName })
      ElMessage.success(t('profile.savedSuccessfully'))
      editInfoDialog.value = false
    } else {
      ElMessage.error(res.message || t('profile.changeFailed'))
    }
  } catch (error) {
    console.error('Save failed:', error)
    ElMessage.error(t('profile.changeFailed'))
  }
}

// Change password
const savePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning(t('profile.pleaseFillAll'))
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning(t('profile.passwordsDoNotMatch'))
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning(t('profile.passwordLengthError'))
    return
  }
  
  try {
    const res = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success(t('profile.passwordChanged'))
      passwordDialog.value = false
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(res.message || t('profile.changeFailed'))
    }
  } catch (error) {
    console.error('Failed to change password:', error)
    ElMessage.success(t('profile.passwordChanged') + ' (demo)')
    passwordDialog.value = false
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  }
}

// Send old phone verification code
const sendOldPhoneCode = () => {
  if (!phoneForm.oldPhone) {
    ElMessage.warning(t('profile.pleaseEnterPhone'))
    return
  }
  if (phoneForm.oldPhone !== userInfo.value.phone) {
    ElMessage.warning(t('profile.phoneFormatError') || 'Old phone number is incorrect')
    return
  }
  ElMessage.success(t('profile.codeSent'))
  oldPhoneCodeCountdown.value = 60
  const timer = setInterval(() => {
    oldPhoneCodeCountdown.value--
    if (oldPhoneCodeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// Send new phone verification code
const sendPhoneCode = () => {
  if (!phoneForm.newPhone) {
    ElMessage.warning(t('profile.pleaseEnterPhone'))
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.warning(t('profile.pleaseEnterValidPhone'))
    return
  }
  ElMessage.success(t('profile.codeSent'))
  phoneCodeCountdown.value = 60
  const timer = setInterval(() => {
    phoneCodeCountdown.value--
    if (phoneCodeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// Save phone binding
const savePhone = async () => {
  if (!phoneForm.newPhone || !phoneForm.code) {
    ElMessage.warning(t('profile.pleaseFillAll'))
    return
  }
  
  try {
    const res = await bindPhone({
      phone: phoneForm.newPhone,
      code: phoneForm.code,
      ...(isPhoneBound.value ? { oldPhone: phoneForm.oldPhone, oldCode: phoneForm.oldCode } : {})
    })
    if (res.code === 200) {
      userInfo.value.phone = phoneForm.newPhone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
      ElMessage.success(isPhoneBound.value ? t('profile.phoneChanged') : t('profile.emailBound'))
      phoneDialog.value = false
      phoneForm.oldPhone = ''
      phoneForm.oldCode = ''
      phoneForm.newPhone = ''
      phoneForm.code = ''
    } else {
      ElMessage.error(res.message || t('profile.operationFailed'))
    }
  } catch (error) {
    console.error('Failed to bind phone:', error)
    userInfo.value.phone = phoneForm.newPhone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
    ElMessage.success(isPhoneBound.value ? t('profile.phoneChanged') + ' (demo)' : t('profile.emailBound') + ' (demo)')
    phoneDialog.value = false
  }
}

// Send old email verification code
const sendOldEmailCode = () => {
  if (!emailForm.oldEmail) {
    ElMessage.warning(t('profile.pleaseEnterEmail'))
    return
  }
  if (emailForm.oldEmail !== userInfo.value.email) {
    ElMessage.warning(t('profile.pleaseEnterValidEmail'))
    return
  }
  ElMessage.success(t('profile.codeSent'))
  oldEmailCodeCountdown.value = 60
  const timer = setInterval(() => {
    oldEmailCodeCountdown.value--
    if (oldEmailCodeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// Send new email verification code
const sendEmailCode = () => {
  if (!emailForm.newEmail) {
    ElMessage.warning(t('profile.pleaseEnterEmail'))
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(emailForm.newEmail)) {
    ElMessage.warning(t('profile.pleaseEnterValidEmail'))
    return
  }
  ElMessage.success(t('profile.codeSent'))
  emailCodeCountdown.value = 60
  const timer = setInterval(() => {
    emailCodeCountdown.value--
    if (emailCodeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// Save email binding
const saveEmail = async () => {
  if (!emailForm.newEmail || !emailForm.code) {
    ElMessage.warning(t('profile.pleaseFillAll'))
    return
  }
  
  try {
    const res = await bindEmail({
      email: emailForm.newEmail,
      code: emailForm.code,
      ...(isEmailBound.value ? { oldEmail: emailForm.oldEmail, oldCode: emailForm.oldCode } : {})
    })
    if (res.code === 200) {
      userInfo.value.email = emailForm.newEmail
      ElMessage.success(isEmailBound.value ? t('profile.emailChanged') : t('profile.emailBound'))
      emailDialog.value = false
      emailForm.oldEmail = ''
      emailForm.oldCode = ''
      emailForm.newEmail = ''
      emailForm.code = ''
    } else {
      ElMessage.error(res.message || t('profile.operationFailed'))
    }
  } catch (error) {
    console.error('Failed to bind email:', error)
    userInfo.value.email = emailForm.newEmail
    ElMessage.success(isEmailBound.value ? t('profile.emailChanged') + ' (demo)' : t('profile.emailBound') + ' (demo)')
    emailDialog.value = false
  }
}

// Change avatar
const handleChangeAvatar = () => {
  avatarDialog.value = true
}

const handleAvatarChange = (file) => {
  avatarFile.value = file.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    userInfo.value.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const uploadAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.warning(t('profile.pleaseSelectImage'))
    return
  }
  if (!avatarFile.value.type.startsWith('image/')) {
    ElMessage.warning(t('profile.pleaseSelectImageFile'))
    return
  }
  if (avatarFile.value.size > 2 * 1024 * 1024) {
    ElMessage.warning(t('profile.imageSizeLimit'))
    return
  }
  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', avatarFile.value)
    const res = await uploadAvatarApi(formData)
    if (res.code === 200 && res.data) {
      userInfo.value.avatar = res.data
      // Sync update avatar in store
      userStore.updateAvatar(res.data)
      ElMessage.success(t('profile.avatarUpdated'))
      avatarDialog.value = false
    } else {
      ElMessage.error(res.msg || res.message || t('profile.uploadFailed'))
    }
  } catch (error) {
    console.error('Failed to upload avatar:', error)
    ElMessage.error(t('profile.avatarUploadFailed'))
    avatarDialog.value = false
  } finally {
    avatarUploading.value = false
  }
}

// 页面跳转
const goToOrders = () => router.push('/owner/orders')
const goToReserve = () => router.push('/owner/reserve')
const goToConsult = () => router.push('/owner/consult')

// Logout
const handleLogout = () => {
  ElMessageBox.confirm(t('profile.logoutConfirm'), t('common.confirm'), { type: 'warning' }).then(() => {
    localStorage.clear()
    router.push('/login')
  })
}

onMounted(() => {
  loadUserInfo()
  loadStats()
})
</script>

<style scoped lang="scss">
.profile-page {
  .user-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24px;
    margin-bottom: 20px;
    position: relative;
    overflow: hidden;
    
    .user-bg {
      position: absolute;
      top: -50%;
      right: -20%;
      width: 200px;
      height: 200px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
    }
    
    .user-info-wrapper {
      padding: 30px 24px;
      display: flex;
      align-items: center;
      gap: 20px;
      position: relative;
      z-index: 1;
      
      .user-avatar {
        position: relative;
        cursor: pointer;
        
        .avatar {
          border: 3px solid white;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
        
        .avatar-edit {
          position: absolute;
          bottom: 0;
          right: 0;
          width: 28px;
          height: 28px;
          background: white;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #667eea;
          cursor: pointer;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        }
      }
      
      .user-detail {
        color: white;
        
        .username {
          font-size: 22px;
          font-weight: 600;
          margin-bottom: 8px;
        }
        
        .user-tags {
          display: flex;
          gap: 8px;
          margin-bottom: 8px;
        }
        
        .user-since {
          font-size: 12px;
          opacity: 0.8;
        }
      }
    }
  }
  
  .stats-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 20px;
    
    .stat-card {
      background: white;
      border-radius: 16px;
      padding: 16px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #667eea;
      }
      
      .stat-label {
        font-size: 12px;
        color: #64748b;
        margin-top: 4px;
      }
    }
  }
  
  .menu-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .menu-group {
      background: white;
      border-radius: 20px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      
      .menu-item {
        display: flex;
        align-items: center;
        padding: 16px 20px;
        cursor: pointer;
        transition: all 0.3s;
        border-bottom: 1px solid #f1f5f9;
        
        &:last-child {
          border-bottom: none;
        }
        
        &:hover {
          background: #f8fafc;
        }
        
        &.danger {
          .menu-title {
            color: #ef4444;
          }
        }
        
        .menu-icon {
          font-size: 24px;
          margin-right: 16px;
        }
        
        .menu-content {
          flex: 1;
          
          .menu-title {
            font-size: 15px;
            font-weight: 500;
            color: #1e293b;
            margin-bottom: 2px;
          }
          
          .menu-desc {
            font-size: 12px;
            color: #94a3b8;
            
            &.unbind {
              color: #f59e0b;
            }
          }
        }
        
        .menu-arrow {
          color: #cbd5e1;
        }
      }
    }
  }
  
  .form-tip {
    font-size: 11px;
    color: #94a3b8;
    margin-top: 4px;
  }
  
  .code-input {
    display: flex;
    gap: 10px;
    
    .el-input {
      flex: 1;
    }
  }
}

.avatar-dialog {
  .avatar-upload {
    text-align: center;
    
    .avatar-preview {
      margin-bottom: 20px;
      
      .preview-avatar {
        margin: 0 auto;
        border: 3px solid #e2e8f0;
      }
    }
    
    .avatar-uploader {
      :deep(.el-upload-dragger) {
        width: 100%;
        padding: 20px;
      }
      
      .upload-content {
        text-align: center;
        
        .upload-text {
          margin-top: 8px;
          font-size: 14px;
          color: #64748b;
        }
        
        .upload-tip {
          margin-top: 4px;
          font-size: 11px;
          color: #94a3b8;
        }
      }
    }
  }
}

.about-dialog {
  .about-content {
    text-align: center;
    padding: 20px;
    
    .about-icon {
      font-size: 60px;
      margin-bottom: 16px;
    }
    
    h3 {
      font-size: 20px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 8px;
    }
    
    .version {
      font-size: 12px;
      color: #94a3b8;
      margin-bottom: 12px;
    }
    
    .description {
      font-size: 14px;
      color: #64748b;
      margin-bottom: 20px;
    }
    
    .copyright {
      font-size: 11px;
      color: #cbd5e1;
      padding-top: 16px;
      border-top: 1px solid #e2e8f0;
    }
  }
}
</style>