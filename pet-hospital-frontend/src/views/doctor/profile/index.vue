<template>
    <div class="profile-page" v-loading="pageLoading">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          个人中心
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
                    更换头像
                  </el-button>
                </el-upload>
              </div>
            </div>
            <div class="user-basic">
              <h3 class="user-name">{{ localUserInfo.realName || localUserInfo.username || '医生' }}</h3>
              <p class="user-title">{{ localUserInfo.title || '主治医师' }}</p>
            </div>
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.acceptCount || 0 }}</div>
                <div class="stat-label">接诊数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ statistics.consultCount || 0 }}</div>
                <div class="stat-label">咨询数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ statistics.prescriptionCount || 0 }}</div>
                <div class="stat-label">处方数</div>
              </div>
            </div>
          </el-card>

          <!-- 账号安全 -->
          <el-card class="security-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Lock /></el-icon>
                <span>账号安全</span>
              </div>
            </template>
            <div class="security-list">
              <div class="security-item">
                <div class="item-info">
                  <el-icon class="success"><CircleCheck /></el-icon>
                  <div class="item-text">
                    <div class="item-title">登录密码</div>
                    <div class="item-desc">已设置，建议定期更换</div>
                  </div>
                </div>
                <el-button type="primary" link @click="showPasswordDialog = true">
                  修改
                </el-button>
              </div>
              <div class="security-item">
                <div class="item-info">
                  <el-icon class="success"><CircleCheck /></el-icon>
                  <div class="item-text">
                    <div class="item-title">手机绑定</div>
                    <div class="item-desc">{{ maskPhone(localUserInfo.phone) || '未绑定' }}</div>
                  </div>
                </div>
                <el-button type="primary" link @click="openPhoneDialog">
                  {{ localUserInfo.phone ? '更换' : '绑定' }}
                </el-button>
              </div>
              <div class="security-item">
                <div class="item-info">
                  <el-icon :class="localUserInfo.email ? 'success' : 'warning'">
                    <component :is="localUserInfo.email ? 'CircleCheck' : 'Warning'" />
                  </el-icon>
                  <div class="item-text">
                    <div class="item-title">邮箱绑定</div>
                    <div class="item-desc">
                      {{ localUserInfo.email ? maskEmail(localUserInfo.email) : '未绑定，建议绑定以接收通知' }}
                    </div>
                  </div>
                </div>
                <el-button type="primary" link @click="openEmailDialog">
                  {{ localUserInfo.email ? '更换' : '绑定' }}
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
                  <span>基本信息</span>
                </div>
                <el-button 
                  :type="isEditing ? 'success' : 'primary'" 
                  @click="toggleEdit"
                  :loading="saving"
                >
                  <el-icon><component :is="isEditing ? 'Check' : 'Edit'" /></el-icon>
                  {{ isEditing ? '保存' : '编辑' }}
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
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用户名">
                    <el-input v-model="formData.username" disabled />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="性别" prop="gender">
                    <el-radio-group v-model="formData.gender">
                      <el-radio :value="1">男</el-radio>
                      <el-radio :value="0">女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号">
                    <el-input v-model="formData.phone" disabled />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="职称" prop="title">
                    <el-select v-model="formData.title" placeholder="请选择职称" class="full-width">
                      <el-option label="主任医师" value="主任医师" />
                      <el-option label="副主任医师" value="副主任医师" />
                      <el-option label="主治医师" value="主治医师" />
                      <el-option label="住院医师" value="住院医师" />
                      <el-option label="助理医师" value="助理医师" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="科室" prop="department">
                    <el-select v-model="formData.department" placeholder="请选择科室" class="full-width">
                      <el-option label="全科医疗部" value="全科医疗部" />
                      <el-option label="内科" value="内科" />
                      <el-option label="外科" value="外科" />
                      <el-option label="皮肤科" value="皮肤科" />
                      <el-option label="眼科" value="眼科" />
                      <el-option label="牙科" value="牙科" />
                      <el-option label="影像科" value="影像科" />
                      <el-option label="检验科" value="检验科" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="擅长领域" prop="specialty">
                <el-input 
                  v-model="formData.specialty" 
                  type="textarea" 
                  :rows="2"
                  placeholder="请输入擅长治疗的疾病或手术，如：小动物内科疾病、软组织外科手术等"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="个人简介" prop="introduction">
                <el-input 
                  v-model="formData.introduction" 
                  type="textarea" 
                  :rows="4"
                  placeholder="请输入个人简介，包括教育背景、工作经历、专业特长等"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="从业时间">
                <el-date-picker 
                  v-model="formData.workDate" 
                  type="date" 
                  placeholder="选择日期"
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
                  <span>执业资质</span>
                </div>
                <el-tag :type="authStatusType" effect="light" round>
                  {{ authStatusText }}
                </el-tag>
              </div>
            </template>

            <!-- 已认证状态 -->
            <div v-if="qualification.authStatus === 2" class="qualification-info">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="执业兽医资格证号">
                  {{ qualification.licenseNo || '未填写' }}
                </el-descriptions-item>
                <el-descriptions-item label="发证机关">
                  {{ qualification.issueOrg || '未填写' }}
                </el-descriptions-item>
                <el-descriptions-item label="发证日期">
                  {{ qualification.issueDate || '未填写' }}
                </el-descriptions-item>
                <el-descriptions-item label="有效期至">
                  {{ qualification.expiryDate || '未填写' }}
                </el-descriptions-item>
              </el-descriptions>
              
              <div class="qualification-images" v-if="qualification.images && qualification.images.length > 0">
                <div class="images-title">资质证书图片</div>
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
                title="您的资质正在审核中，请耐心等待"
                type="warning"
                :closable="false"
                show-icon
                class="auth-alert"
              />
              
              <el-form :model="qualificationForm" label-width="120px" class="qualification-form">
                <el-form-item label="资格证号" required>
                  <el-input v-model="qualificationForm.licenseNo" placeholder="请输入执业兽医资格证号" />
                </el-form-item>
                <el-form-item label="发证机关" required>
                  <el-input v-model="qualificationForm.issueOrg" placeholder="请输入发证机关" />
                </el-form-item>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="发证日期" required>
                      <el-date-picker
                        v-model="qualificationForm.issueDate"
                        type="date"
                        placeholder="选择日期"
                        value-format="YYYY-MM-DD"
                        class="full-width"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="有效期至">
                      <el-date-picker
                        v-model="qualificationForm.expiryDate"
                        type="date"
                        placeholder="选择日期（选填）"
                        value-format="YYYY-MM-DD"
                        class="full-width"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="资质证书">
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
                  <div class="upload-tip">支持jpg、png格式，大小不超过2MB</div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitQualification" :loading="submittingQualification">
                    提交审核
                  </el-button>
                  <el-button @click="resetQualificationForm">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 修改密码对话框 -->
      <el-dialog v-model="showPasswordDialog" title="修改密码" width="450px" destroy-on-close>
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="6-20位字母、数字或符号" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">确认修改</el-button>
        </template>
      </el-dialog>

      <!-- 修改手机对话框 -->
      <el-dialog v-model="showPhoneDialog" :title="phoneDialogTitle" width="450px" destroy-on-close>
        <el-steps :active="phoneStep" finish-status="success" simple class="phone-steps">
          <el-step title="验证身份" />
          <el-step title="绑定新手机" />
        </el-steps>
        
        <div v-if="phoneStep === 0" class="verify-step">
          <p class="step-desc">
            {{ localUserInfo.phone ? `已向 ${maskPhone(localUserInfo.phone)} 发送验证码` : '请输入您要绑定的手机号' }}
          </p>
          <el-form :model="phoneForm" label-width="0">
            <el-form-item v-if="!localUserInfo.phone">
              <el-input v-model="phoneForm.newPhone" placeholder="请输入手机号">
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input v-model="phoneForm.code" placeholder="请输入验证码">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
                <template #append>
                  <el-button 
                    :disabled="codeCountdown > 0" 
                    @click="sendPhoneCode"
                    :loading="sendingCode"
                  >
                    {{ codeCountdown > 0 ? `${codeCountdown}s后重试` : '发送验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        
        <div v-else class="bind-step">
          <p class="step-desc">请输入要绑定的新手机号</p>
          <el-form :model="phoneForm" label-width="0">
            <el-form-item>
              <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号">
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input v-model="phoneForm.newCode" placeholder="请输入验证码">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
                <template #append>
                  <el-button 
                    :disabled="newCodeCountdown > 0" 
                    @click="sendNewPhoneCode"
                    :loading="sendingNewCode"
                  >
                    {{ newCodeCountdown > 0 ? `${newCodeCountdown}s后重试` : '发送验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        
        <template #footer>
          <el-button @click="showPhoneDialog = false">取消</el-button>
          <el-button type="primary" @click="handlePhoneNext" :loading="changingPhone">
            {{ phoneStep === 0 ? '下一步' : '确认绑定' }}
          </el-button>
        </template>
      </el-dialog>

      <!-- 绑定邮箱对话框 -->
      <el-dialog v-model="showEmailDialog" :title="emailDialogTitle" width="450px" destroy-on-close>
        <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-width="0">
          <el-form-item prop="email">
            <el-input v-model="emailForm.email" placeholder="请输入邮箱地址">
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-input v-model="emailForm.code" placeholder="请输入验证码">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
              <template #append>
                <el-button 
                  :disabled="emailCodeCountdown > 0" 
                  @click="sendEmailCode"
                  :loading="sendingEmailCode"
                >
                  {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s后重试` : '发送验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEmailDialog = false">取消</el-button>
          <el-button type="primary" @click="handleBindEmail" :loading="bindingEmail">确认绑定</el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, UserFilled, Camera, Lock, CircleCheck, Warning,
  EditPen, Edit, Check, DocumentChecked, Plus, Phone, Message
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
// 在文件顶部添加导入
import { profileModule } from '@/api/doctor/profile'


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
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }],
  department: [{ required: true, message: '请选择科室', trigger: 'change' }]
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
  const map = { 0: '未认证', 1: '审核中', 2: '已认证', 3: '认证失败' }
  return map[qualification.value.authStatus] || '未知'
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
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 手机绑定
const phoneStep = ref(0)
const phoneDialogTitle = computed(() => localUserInfo.value.phone ? '更换手机号' : '绑定手机号')
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
const emailDialogTitle = computed(() => localUserInfo.value.email ? '更换邮箱' : '绑定邮箱')
const emailFormRef = ref(null)
const emailForm = reactive({
  email: '',
  code: ''
})
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
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
    username: storeInfo.username || '医生',
    realName: storeInfo.realName || storeInfo.username || '医生',
    phone: storeInfo.phone || '',
    email: storeInfo.email || '',
    avatar: storeInfo.avatar || '',
    title: storeInfo.title || '主治医师',
    department: storeInfo.department || '全科医疗部',
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
  // 模拟数据
  statistics.value = {
    acceptCount: 128,
    consultCount: 56,
    prescriptionCount: 89
  }
}

// ========== 头像上传 ==========
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('只支持 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
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
    ElMessage.success('头像更新成功')
  } else {
    ElMessage.error(res.message || res.msg || '上传失败')
  }
}

const handleAvatarError = (error) => {
  console.error('头像上传失败:', error)
  ElMessage.error('头像上传失败，请检查网络或文件格式')
}

// ========== 基本信息编辑 ==========
const toggleEdit = async () => {
  if (isEditing.value) {
    // 保存
    try {
      await formRef.value.validate()
      saving.value = true
      
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
      
      ElMessage.success('保存成功')
      isEditing.value = false
    } catch (error) {
      if (error !== false) {
        ElMessage.error('请完善必填信息')
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
    
    ElMessage.success('密码修改成功，请重新登录')
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
      ElMessage.error('修改失败')
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
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  sendingCode.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('验证码已发送')
    startCountdown('codeCountdown')
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    sendingCode.value = false
  }
}

const sendNewPhoneCode = async () => {
  if (!phoneForm.newPhone) {
    ElMessage.warning('请输入新手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  sendingNewCode.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('验证码已发送')
    startCountdown('newCodeCountdown')
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    sendingNewCode.value = false
  }
}

const handlePhoneNext = async () => {
  if (phoneStep.value === 0) {
    if (!phoneForm.code) {
      ElMessage.warning('请输入验证码')
      return
    }
    
    changingPhone.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      phoneStep.value = 1
    } catch (error) {
      ElMessage.error('验证失败')
    } finally {
      changingPhone.value = false
    }
  } else {
    if (!phoneForm.newPhone) {
      ElMessage.warning('请输入新手机号')
      return
    }
    if (!phoneForm.newCode) {
      ElMessage.warning('请输入验证码')
      return
    }
    
    changingPhone.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 更新本地和 store
      localUserInfo.value.phone = phoneForm.newPhone
      userStore.updateUserInfo({ phone: phoneForm.newPhone })
      
      ElMessage.success('手机号更换成功')
      showPhoneDialog.value = false
    } catch (error) {
      ElMessage.error('更换失败')
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
    ElMessage.warning('请输入邮箱地址')
    return
  }
  
  sendingEmailCode.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('验证码已发送到您的邮箱')
    startCountdown('emailCodeCountdown')
  } catch (error) {
    ElMessage.error('发送失败')
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
    
    ElMessage.success(localUserInfo.value.email ? '邮箱更换成功' : '邮箱绑定成功')
    showEmailDialog.value = false
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
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
    ElMessage.error('只支持 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
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
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.message || res.msg || '上传失败')
  }
}

const handleQualificationImageError = (error) => {
  console.error('资质图片上传失败:', error)
  ElMessage.error('图片上传失败，请检查网络或文件格式')
}

const handleQualificationImageRemove = (file) => {
  const index = qualificationImageList.value.findIndex(item => item.url === file.url)
  if (index > -1) {
    qualificationImageList.value.splice(index, 1)
  }
}

const submitQualification = async () => {
  if (!qualificationForm.licenseNo) {
    ElMessage.warning('请输入资格证号')
    return
  }
  if (!qualificationForm.issueOrg) {
    ElMessage.warning('请输入发证机关')
    return
  }
  if (!qualificationForm.issueDate) {
    ElMessage.warning('请选择发证日期')
    return
  }
  if (qualificationImageList.value.length === 0) {
    ElMessage.warning('请上传资质证书图片')
    return
  }
  
  submittingQualification.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    qualification.value.authStatus = 1
    ElMessage.success('资质已提交，请等待审核')
  } catch (error) {
    ElMessage.error('提交失败')
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