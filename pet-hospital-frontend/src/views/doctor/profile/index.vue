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
        <!-- 左侧：基本信息 -->
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
              <h3 class="user-name">{{ localUserInfo.realName || localUserInfo.username || $t('profile.title') }}</h3>
              <p class="user-title">{{ localUserInfo.title || $t('profile.attendingPhysician') }}</p>
            </div>
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.acceptCount || 0 }}</div>
                <div class="stat-label">{{ $t('profile.acceptanceCount') }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ statistics.consultCount || 0 }}</div>
                <div class="stat-label">{{ $t('profile.consultationCount') }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ statistics.prescriptionCount || 0 }}</div>
                <div class="stat-label">{{ $t('profile.prescriptionCount') }}</div>
              </div>
            </div>
          </el-card>

          <!-- 账号安全 -->
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
                  {{ $t('profile.change') }}
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
                  <el-form-item :label="$t('profile.title')" prop="title">
                    <el-select v-model="formData.title" :placeholder="$t('profile.placeholderTitle')" class="full-width">
                      <el-option :label="$t('profile.chiefPhysician')" value="主任医师" />
                      <el-option :label="$t('profile.associateChiefPhysician')" value="副主任医师" />
                      <el-option :label="$t('profile.attendingPhysician')" value="主治医师" />
                      <el-option :label="$t('profile.residentPhysician')" value="住院医师" />
                      <el-option :label="$t('profile.assistantPhysician')" value="助理医师" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item :label="$t('profile.department')" prop="department">
                    <el-select v-model="formData.department" :placeholder="$t('profile.placeholderDepartment')" class="full-width">
                      <el-option :label="$t('profile.generalPractice')" value="全科医疗部" />
                      <el-option :label="$t('profile.internalMedicine')" value="内科" />
                      <el-option :label="$t('profile.surgery')" value="外科" />
                      <el-option :label="$t('profile.dermatology')" value="皮肤科" />
                      <el-option :label="$t('profile.ophthalmology')" value="眼科" />
                      <el-option :label="$t('profile.dentistry')" value="牙科" />
                      <el-option :label="$t('profile.imaging')" value="影像科" />
                      <el-option :label="$t('profile.laboratory')" value="检验科" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item :label="$t('profile.specialty')" prop="specialty">
                <el-input 
                  v-model="formData.specialty" 
                  type="textarea" 
                  :rows="2"
                  :placeholder="$t('profile.placeholderSpecialty')"
                  maxlength="200"
                  show-word-limit
                />
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

              <el-form-item :label="$t('profile.workSince')">
                <el-date-picker 
                  v-model="formData.workDate" 
                  type="date" 
                  :placeholder="$t('profile.selectDate')"
                  value-format="YYYY-MM-DD"
                  class="full-width"
                />
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 执业资质 -->
          <el-card shadow="hover" class="qualification-card">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon><DocumentChecked /></el-icon>
                  <span>{{ $t('profile.practiceQualification') }}</span>
                </div>
                <el-tag :type="authStatusType" effect="light" round>
                  {{ authStatusText }}
                </el-tag>
              </div>
            </template>

            <!-- 已认证状态 -->
            <div v-if="qualification.authStatus === 2" class="qualification-info">
              <el-descriptions :column="2" border>
                <el-descriptions-item :label="$t('profile.veterinaryLicenseNo')">
                  {{ qualification.licenseNo || $t('profile.notFilled') }}
                </el-descriptions-item>
                <el-descriptions-item :label="$t('profile.issuingAuthority')">
                  {{ qualification.issueOrg || $t('profile.notFilled') }}
                </el-descriptions-item>
                <el-descriptions-item :label="$t('profile.issueDate')">
                  {{ qualification.issueDate || $t('profile.notFilled') }}
                </el-descriptions-item>
                <el-descriptions-item :label="$t('profile.validUntil')">
                  {{ qualification.expiryDate || $t('profile.notFilled') }}
                </el-descriptions-item>
              </el-descriptions>
              
              <div class="qualification-images" v-if="qualification.images && qualification.images.length > 0">
                <div class="images-title">{{ $t('profile.qualificationImages') }}</div>
                <div class="image-list">
                  <el-image
                    v-for="(img, index) in qualification.images"
                    :key="index"
                    :src="img"
                    fit="cover"
                    class="qualification-image"
                    :preview-src-list="qualification.images"
                  />
                </div>
              </div>
            </div>

            <!-- 未认证/认证中状态 -->
            <div v-else class="qualification-upload">
              <el-alert
                v-if="qualification.authStatus === 1"
                :title="$t('profile.reviewMessage')"
                type="warning"
                :closable="false"
                show-icon
                class="auth-alert"
              />
              
              <el-form :model="qualificationForm" label-width="120px" class="qualification-form">
                <el-form-item :label="$t('profile.licenseNo')" required>
                  <el-input v-model="qualificationForm.licenseNo" :placeholder="$t('profile.pleaseEnterLicenseNo')" />
                </el-form-item>
                <el-form-item :label="$t('profile.issuingAuthority')" required>
                  <el-input v-model="qualificationForm.issueOrg" :placeholder="$t('profile.pleaseEnterIssueOrg')" />
                </el-form-item>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item :label="$t('profile.issueDate')" required>
                      <el-date-picker
                        v-model="qualificationForm.issueDate"
                        type="date"
                        :placeholder="$t('profile.selectDate')"
                        value-format="YYYY-MM-DD"
                        class="full-width"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item :label="$t('profile.validUntil')">
                      <el-date-picker
                        v-model="qualificationForm.expiryDate"
                        type="date"
                        :placeholder="$t('profile.selectDateOptional')"
                        value-format="YYYY-MM-DD"
                        class="full-width"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item :label="$t('profile.qualificationCertificate')">
                  <el-upload
                    action="/api/common/upload"
                    list-type="picture-card"
                    :on-success="handleQualificationImageSuccess"
                    :on-error="handleQualificationImageError"
                    :on-remove="handleQualificationImageRemove"
                    :file-list="qualificationImageList"
                    :headers="uploadHeaders"
                    :before-upload="beforeImageUpload"
                    name="file"
                    with-credentials
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <div class="upload-tip">{{ $t('profile.uploadTip2MB') }}</div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitQualification" :loading="submittingQualification">
                    {{ $t('profile.submitForReview') }}
                  </el-button>
                  <el-button @click="resetQualificationForm">{{ $t('profile.reset') }}</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 修改密码对话框 -->
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
          <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">{{ $t('profile.confirmChange') }}</el-button>
        </template>
      </el-dialog>

      <!-- 修改手机对话框 -->
      <el-dialog v-model="showPhoneDialog" :title="phoneDialogTitle" width="450px" destroy-on-close>
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
      <el-dialog v-model="showEmailDialog" :title="emailDialogTitle" width="450px" destroy-on-close>
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
const { t } = useI18n()
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, UserFilled, Camera, Lock, CircleCheck, Warning,
  EditPen, Edit, Check, DocumentChecked, Plus, Phone, Message
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
// 在文件顶部添加导入
import { profileModule } from '@/api/doctor/profile'
import { useI18n } from 'vue-i18n'


const userStore = useUserStore()

// 本地用户信息（用于页面显示和编辑）
const localUserInfo = ref({
  userId: '',
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  title: '',
  department: '',
  specialty: '',
  introduction: '',
  workDate: '',
  gender: 1
})

// Page loading
const pageLoading = ref(false)
const saving = ref(false)

// 上传请求头
const uploadHeaders = computed(() => {
  const headers = {}
  const token = userStore.token
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  console.log('上传请求头:', headers)
  return headers
})

// 统计数据
const statistics = ref({
  acceptCount: 0,
  consultCount: 0,
  prescriptionCount: 0
})

// 编辑状态
const isEditing = ref(false)
const formRef = ref(null)
const formData = reactive({
  realName: '',
  username: '',
  gender: 1,
  phone: '',
  title: '',
  department: '',
  specialty: '',
  introduction: '',
  workDate: ''
})

const formRules = {
  realName: [{ required: true, message: t('profile.placeholderRealName'), trigger: 'blur' }],
  title: [{ required: true, message: t('profile.placeholderTitle'), trigger: 'change' }],
  department: [{ required: true, message: t('profile.placeholderDepartment'), trigger: 'change' }]
}

// 资质信息
const qualification = ref({
  authStatus: 0,
  licenseNo: '',
  issueOrg: '',
  issueDate: '',
  expiryDate: '',
  images: []
})

const authStatusType = computed(() => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return map[qualification.value.authStatus] || 'info'
})

const authStatusText = computed(() => {
  const map = { 0: t('profile.unverified'), 1: t('profile.underReview'), 2: t('profile.verified'), 3: t('profile.verificationFailed') }
  return map[qualification.value.authStatus] || t('profile.unknown')
})

// 资质表单
const qualificationForm = reactive({
  licenseNo: '',
  issueOrg: '',
  issueDate: '',
  expiryDate: ''
})
const qualificationImageList = ref([])
const submittingQualification = ref(false)

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
  oldPassword: [
    { required: true, message: t('profile.pleaseEnterOldPassword'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: t('profile.pleaseEnterOldPassword'), trigger: 'blur' },
    { min: 6, max: 20, message: t('profile.passwordLengthError'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('profile.pleaseConfirmPassword'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 手机绑定
const phoneStep = ref(0)
const phoneDialogTitle = computed(() => localUserInfo.value.phone ? t('profile.changePhoneTitle') : t('profile.bindPhoneTitle'))
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

// 邮箱绑定
const emailDialogTitle = computed(() => localUserInfo.value.email ? t('profile.changeEmailTitle') : t('profile.bindEmailTitle'))
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
  code: [
    { required: true, message: t('profile.pleaseEnterCode'), trigger: 'blur' }
  ]
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
  formData.title = localUserInfo.value.title || ''
  formData.department = localUserInfo.value.department || ''
  formData.specialty = localUserInfo.value.specialty || ''
  formData.introduction = localUserInfo.value.introduction || ''
  formData.workDate = localUserInfo.value.workDate || ''
}

// 刷新本地用户信息（从 store 同步）
const refreshLocalUserInfo = () => {
  const storeInfo = userStore.userInfo || {}
  localUserInfo.value = {
    userId: storeInfo.userId || storeInfo.id || '',
    username: storeInfo.username || t('profile.doctorDefault'),
    realName: storeInfo.realName || storeInfo.username || t('profile.doctorDefault'),
    phone: storeInfo.phone || '',
    email: storeInfo.email || '',
    avatar: storeInfo.avatar || '',
    title: storeInfo.title || t('profile.attendingPhysician'),
    department: storeInfo.department || t('profile.generalPractice'),
    specialty: storeInfo.specialty || '',
    introduction: storeInfo.introduction || '',
    workDate: storeInfo.workDate || '',
    gender: storeInfo.gender ?? 1
  }
  initFormData()
}

// ========== 初始化 ==========
const loadUserInfo = async () => {
  try {
    // 刷新 store 中的用户信息
    await userStore.refreshUserInfo()
    refreshLocalUserInfo()
  } catch (error) {
    console.error('获取用户信息失败', error)
    // 使用 store 中已有的数据
    refreshLocalUserInfo()
  }
}

const loadStatistics = async () => {
  try {
    // 优先使用 doctor_profile.id，避免 userId / doctorId 混淆
    const doctorId = userStore.userInfo?.doctorId || localUserInfo.value.userId
    if (!doctorId) {
      statistics.value = { acceptCount: 0, consultCount: 0, prescriptionCount: 0 }
      return
    }
    const res = await profileModule.getStatistics(doctorId)
    if (res.code === 200 && res.data) {
      statistics.value = {
        acceptCount: res.data.totalAccept || 0,
        consultCount: res.data.totalConsult || 0,
        prescriptionCount: res.data.totalPrescription || 0
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    statistics.value = { acceptCount: 0, consultCount: 0, prescriptionCount: 0 }
  }
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
  console.log('准备上传头像:', file.name, file.size)
  return true
}

const handleAvatarSuccess = (res) => {
  console.log('头像上传响应:', res)
  if (res.code === 200 && res.data) {
    // 更新本地显示
    localUserInfo.value.avatar = res.data
    // 更新 store
    userStore.updateAvatar(res.data)
    ElMessage.success(t('profile.avatarUpdated'))
  } else {
    ElMessage.error(res.message || res.msg || t('profile.uploadFailed'))
  }
}

const handleAvatarError = (error) => {
  console.error('头像上传失败:', error)
  ElMessage.error(t('profile.avatarUploadFailed'))
}

// ========== 基本信息编辑 ==========
const toggleEdit = async () => {
  if (isEditing.value) {
    // 保存
    try {
      await formRef.value.validate()
      saving.value = true
      
      // 调用后端 API 保存医生信息
      const doctorId = userStore.userInfo?.doctorId || localUserInfo.value.userId
      await profileModule.updateDoctorInfo({
        doctorId: doctorId,
        realName: formData.realName,
        title: formData.title,
        department: formData.department,
        specialty: formData.specialty,
        introduction: formData.introduction
      })
      
      // 更新本地用户信息
      localUserInfo.value.realName = formData.realName
      localUserInfo.value.gender = formData.gender
      localUserInfo.value.title = formData.title
      localUserInfo.value.department = formData.department
      localUserInfo.value.specialty = formData.specialty
      localUserInfo.value.introduction = formData.introduction
      localUserInfo.value.workDate = formData.workDate
      
      // 更新 store
      userStore.updateUserInfo({
        realName: formData.realName,
        gender: formData.gender,
        title: formData.title,
        department: formData.department,
        specialty: formData.specialty,
        introduction: formData.introduction,
        workDate: formData.workDate
      })
      
      ElMessage.success(t('profile.savedSuccessfully'))
      isEditing.value = false
    } catch (error) {
      if (error !== false) {
        ElMessage.error(error?.message || t('profile.pleaseCompleteRequired'))
      }
    } finally {
      saving.value = false
    }
  } else {
    // 进入编辑模式
    initFormData()
    isEditing.value = true
  }
}

// ========== 修改密码 ==========
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    // 模拟修改密码
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(t('profile.passwordChanged'))
    showPasswordDialog.value = false
    
    // 清除表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    // 退出登录
    setTimeout(() => {
      userStore.logoutAction()
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    if (error !== false) {
      ElMessage.error(t('profile.changeFailed'))
    }
  } finally {
    changingPassword.value = false
  }
}

// ========== 手机绑定 ==========
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
      
      // 更新本地和 store
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

// ========== 邮箱绑定 ==========
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
    
    // 更新本地和 store
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

// ========== 资质上传 ==========
const beforeImageUpload = (file) => {
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
  console.log('准备上传资质图片:', file.name, file.size)
  return true
}

const handleQualificationImageSuccess = (res, file) => {
  console.log('资质图片上传响应:', res)
  if (res.code === 200 && res.data) {
    qualificationImageList.value.push({
      name: file.name,
      url: res.data
    })
    ElMessage.success(t('profile.imageUploaded'))
  } else {
    ElMessage.error(res.message || res.msg || t('profile.uploadFailed'))
  }
}

const handleQualificationImageError = (error) => {
  console.error('资质图片上传失败:', error)
  ElMessage.error(t('profile.imageUploadFailed'))
}

const handleQualificationImageRemove = (file) => {
  const index = qualificationImageList.value.findIndex(item => item.url === file.url)
  if (index > -1) {
    qualificationImageList.value.splice(index, 1)
  }
}

const submitQualification = async () => {
  if (!qualificationForm.licenseNo) {
    ElMessage.warning(t('profile.pleaseEnterLicenseNo'))
    return
  }
  if (!qualificationForm.issueOrg) {
    ElMessage.warning(t('profile.pleaseEnterIssueOrg'))
    return
  }
  if (!qualificationForm.issueDate) {
    ElMessage.warning(t('profile.pleaseSelectIssueDate'))
    return
  }
  if (qualificationImageList.value.length === 0) {
    ElMessage.warning(t('profile.pleaseUploadQualification'))
    return
  }
  
  submittingQualification.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    qualification.value.authStatus = 1
    ElMessage.success(t('profile.qualificationSubmitted'))
  } catch (error) {
    ElMessage.error(t('profile.submissionFailed'))
  } finally {
    submittingQualification.value = false
  }
}

const resetQualificationForm = () => {
  qualificationForm.licenseNo = ''
  qualificationForm.issueOrg = ''
  qualificationForm.issueDate = ''
  qualificationForm.expiryDate = ''
  qualificationImageList.value = []
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
  await loadUserInfo()
  await loadStatistics()
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
        color: #3b82f6;
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
            border: 4px solid #e0f2fe;
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
          
          .user-title {
            color: #64748b;
            font-size: 14px;
          }
        }
        
        .user-stats {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 15px;
          padding-top: 20px;
          border-top: 1px solid #f1f5f9;
          
          .stat-item {
            text-align: center;
            
            .stat-value {
              font-size: 28px;
              font-weight: 700;
              color: #3b82f6;
            }
            
            .stat-label {
              font-size: 13px;
              color: #64748b;
              margin-top: 4px;
            }
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
            color: #3b82f6;
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
      display: flex;
      flex-direction: column;
      gap: 20px;
      
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
              color: #3b82f6;
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
      
      .qualification-card {
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
              color: #3b82f6;
            }
          }
        }
        
        .qualification-info {
          padding: 10px;
          
          .qualification-images {
            margin-top: 20px;
            
            .images-title {
              font-weight: 500;
              margin-bottom: 12px;
              color: #334155;
            }
            
            .image-list {
              display: flex;
              gap: 12px;
              flex-wrap: wrap;
              
              .qualification-image {
                width: 120px;
                height: 120px;
                border-radius: 8px;
                object-fit: cover;
              }
            }
          }
        }
        
        .qualification-upload {
          padding: 20px;
          
          .auth-alert {
            margin-bottom: 20px;
          }
          
          .qualification-form {
            .upload-tip {
              font-size: 12px;
              color: #94a3b8;
              margin-top: 8px;
            }
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

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>