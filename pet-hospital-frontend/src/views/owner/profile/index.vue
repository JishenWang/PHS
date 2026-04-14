<template>
  <div class="page-content">
    <!-- 头部 - 用户卡片 -->
    <div class="header" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 20px 60px; border-radius: 0 0 40px 40px;">
      <div class="user-card">
        <div class="avatar-wrapper" @click="handleChangeAvatar">
          <el-avatar :size="80" :src="userInfo.avatar" class="user-avatar">
            <el-icon size="50"><User /></el-icon>
          </el-avatar>
          <div class="avatar-edit">
            <el-icon><Camera /></el-icon>
          </div>
        </div>
        <div class="user-info">
          <h2 class="username">{{ userInfo.username }}</h2>
          <p class="user-role">
            <el-tag type="success" size="small">{{ getRoleName(userInfo.role) }}</el-tag>
          </p>
          <p class="user-since">加入时间：{{ userInfo.createTime || '2024年1月' }}</p>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card" @click="goToOrders">
        <div class="stat-value">{{ stats.orderCount }}</div>
        <div class="stat-label">订单数量</div>
      </div>
      <div class="stat-card" @click="goToReserve">
        <div class="stat-value">{{ stats.reserveCount }}</div>
        <div class="stat-label">预约次数</div>
      </div>
      <div class="stat-card" @click="goToConsult">
        <div class="stat-value">{{ stats.consultCount }}</div>
        <div class="stat-label">咨询次数</div>
      </div>
    </div>

    <!-- 设置卡片 -->
    <div class="settings-card">
      <div class="card-title">
        <el-icon><Setting /></el-icon>
        <span>账号设置</span>
      </div>

      <!-- 个人信息（只读显示） -->
      <div class="setting-item" @click="editInfoDialog = true">
        <div class="setting-info">
          <div class="setting-icon">👤</div>
          <div>
            <div class="setting-label">个人信息</div>
            <div class="setting-desc">{{ userInfo.username }}</div>
          </div>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 我的订单 -->
      <div class="setting-item" @click="goToOrders">
        <div class="setting-info">
          <div class="setting-icon">📋</div>
          <div>
            <div class="setting-label">我的订单</div>
            <div class="setting-desc">查看消费记录</div>
          </div>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 修改密码 -->
      <div class="setting-item" @click="passwordDialog = true">
        <div class="setting-info">
          <div class="setting-icon">🔒</div>
          <div>
            <div class="setting-label">修改密码</div>
            <div class="setting-desc">定期更换密码，保护账号安全</div>
          </div>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 绑定/换绑手机 -->
      <div class="setting-item" @click="phoneDialog = true">
        <div class="setting-info">
          <div class="setting-icon">📱</div>
          <div>
            <div class="setting-label">{{ isPhoneBound ? '换绑手机' : '绑定手机' }}</div>
            <div class="setting-desc" :class="{ 'unbind': !isPhoneBound }">
              {{ isPhoneBound ? userInfo.phone : '未绑定，点击绑定' }}
            </div>
          </div>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 绑定/换绑邮箱 -->
      <div class="setting-item" @click="emailDialog = true">
        <div class="setting-info">
          <div class="setting-icon">📧</div>
          <div>
            <div class="setting-label">{{ isEmailBound ? '换绑邮箱' : '绑定邮箱' }}</div>
            <div class="setting-desc" :class="{ 'unbind': !isEmailBound }">
              {{ isEmailBound ? userInfo.email : '未绑定，点击绑定' }}
            </div>
          </div>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 关于我们 -->
      <div class="setting-item" @click="aboutDialog = true">
        <div class="setting-info">
          <div class="setting-icon">ℹ️</div>
          <div>
            <div class="setting-label">关于我们</div>
            <div class="setting-desc">版本信息</div>
          </div>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>

      <!-- 退出登录 -->
      <div class="setting-item danger" @click="logout">
        <div class="setting-info">
          <div class="setting-icon">🚪</div>
          <div>
            <div class="setting-label">退出登录</div>
            <div class="setting-desc">退出当前账号</div>
          </div>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    

    <!-- 编辑个人信息弹窗（只修改用户名） -->
    <el-dialog v-model="editInfoDialog" title="编辑个人信息" width="90%" style="border-radius: 20px; max-width: 400px;">
      <el-form :model="editForm" label-width="80px" size="small">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input :value="userInfo.phone || '未绑定'" disabled />
          <div style="font-size: 12px; color: #999;">修改手机号请使用"换绑手机"功能</div>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input :value="userInfo.email || '未绑定'" disabled />
          <div style="font-size: 12px; color: #999;">修改邮箱请使用"换绑邮箱"功能</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editInfoDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialog" title="修改密码" width="90%" style="border-radius: 20px; max-width: 400px;">
      <el-form :model="passwordForm" label-width="80px" size="small">
        <el-form-item label="原密码">
          <el-input type="password" v-model="passwordForm.oldPassword" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialog = false">取消</el-button>
        <el-button type="primary" @click="savePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 绑定/换绑手机弹窗 -->
    <el-dialog v-model="phoneDialog" :title="phoneDialogTitle" width="90%" style="border-radius: 20px; max-width: 400px;">
      <el-form :model="phoneForm" label-width="80px" size="small">
        <!-- 换绑时需要验证原手机号 -->
        <el-form-item v-if="isPhoneBound" label="原手机">
          <el-input v-model="phoneForm.oldPhone" placeholder="请输入原手机号" />
        </el-form-item>
        <el-form-item v-if="isPhoneBound" label="原验证码">
          <div class="code-input">
            <el-input v-model="phoneForm.oldCode" placeholder="请输入验证码" />
            <el-button :disabled="oldPhoneCodeCountdown > 0" @click="sendOldPhoneCode" size="small">
              {{ oldPhoneCodeCountdown > 0 ? `${oldPhoneCodeCountdown}秒` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item :label="isPhoneBound ? '新手机' : '手机号'">
          <el-input v-model="phoneForm.newPhone" :placeholder="isPhoneBound ? '请输入新手机号' : '请输入手机号'" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-input">
            <el-input v-model="phoneForm.code" placeholder="请输入验证码" />
            <el-button :disabled="phoneCodeCountdown > 0" @click="sendPhoneCode" size="small">
              {{ phoneCodeCountdown > 0 ? `${phoneCodeCountdown}秒` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="phoneDialog = false">取消</el-button>
        <el-button type="primary" @click="savePhone">{{ isPhoneBound ? '确认换绑' : '确认绑定' }}</el-button>
      </template>
    </el-dialog>

    <!-- 绑定/换绑邮箱弹窗 -->
    <el-dialog v-model="emailDialog" :title="emailDialogTitle" width="90%" style="border-radius: 20px; max-width: 400px;">
      <el-form :model="emailForm" label-width="80px" size="small">
        <el-form-item v-if="isEmailBound" label="原邮箱">
          <el-input v-model="emailForm.oldEmail" placeholder="请输入原邮箱" />
        </el-form-item>
        <el-form-item v-if="isEmailBound" label="原验证码">
          <div class="code-input">
            <el-input v-model="emailForm.oldCode" placeholder="请输入验证码" />
            <el-button :disabled="oldEmailCodeCountdown > 0" @click="sendOldEmailCode" size="small">
              {{ oldEmailCodeCountdown > 0 ? `${oldEmailCodeCountdown}秒` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item :label="isEmailBound ? '新邮箱' : '邮箱'">
          <el-input v-model="emailForm.newEmail" :placeholder="isEmailBound ? '请输入新邮箱' : '请输入邮箱'" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-input">
            <el-input v-model="emailForm.code" placeholder="请输入验证码" />
            <el-button :disabled="emailCodeCountdown > 0" @click="sendEmailCode" size="small">
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}秒` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="emailDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEmail">{{ isEmailBound ? '确认换绑' : '确认绑定' }}</el-button>
      </template>
    </el-dialog>

    <!-- 关于我们弹窗 -->
    <el-dialog v-model="aboutDialog" title="关于我们" width="90%" style="border-radius: 20px; max-width: 350px;">
      <div style="text-align: center; padding: 20px;">
        <div style="font-size: 48px; margin-bottom: 12px;">🐾</div>
        <h3>宠物医院管理系统</h3>
        <p style="color: #999; font-size: 12px; margin-top: 8px;">版本 1.0.0</p>
        <p style="color: #999; font-size: 12px;">为您的爱宠提供全方位健康管理</p>
        <div style="margin-top: 20px; padding-top: 16px; border-top: 1px solid #f0f0f0;">
          <p style="color: #ccc; font-size: 11px;">© 2024 宠物医院管理系统</p>
        </div>
      </div>
    </el-dialog>

    <!-- 换头像弹窗 -->
    <el-dialog v-model="avatarDialog" title="更换头像" width="90%" style="border-radius: 20px; max-width: 350px;">
      <div style="text-align: center; padding: 20px;">
        <el-upload
          action="#"
          :auto-upload="false"
          :on-change="handleAvatarChange"
          :show-file-list="false"
          drag
        >
          <div style="padding: 30px;">
            <el-icon size="40"><Upload /></el-icon>
            <div style="margin-top: 12px;">点击或拖拽上传图片</div>
            <div style="font-size: 12px; color: #999;">支持jpg、png格式</div>
          </div>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="avatarDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAvatar" :loading="avatarUploading">确认更换</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Setting, ArrowRight, Avatar, Notebook, Calendar, ChatDotRound, Camera, Upload } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, changePassword, bindPhone, bindEmail, sendVerifyCode } from '@/api/owner/owner'

const router = useRouter()

// 弹窗状态
const editInfoDialog = ref(false)
const passwordDialog = ref(false)
const phoneDialog = ref(false)
const emailDialog = ref(false)
const aboutDialog = ref(false)
const avatarDialog = ref(false)

// 倒计时
const phoneCodeCountdown = ref(0)
const emailCodeCountdown = ref(0)
const oldPhoneCodeCountdown = ref(0)
const oldEmailCodeCountdown = ref(0)

// 加载状态
const avatarUploading = ref(false)

// 用户信息
const userInfo = ref({
  id: '',
  username: '',
  phone: '',
  email: '',
  role: '',
  avatar: '',
  createTime: ''
})

// 统计数据
const stats = ref({
  orderCount: 0,
  reserveCount: 0,
  consultCount: 0
})

// 判断是否已绑定
const isPhoneBound = computed(() => userInfo.value.phone && userInfo.value.phone !== '')
const isEmailBound = computed(() => userInfo.value.email && userInfo.value.email !== '')

// 弹窗标题
const phoneDialogTitle = computed(() => isPhoneBound.value ? '换绑手机' : '绑定手机')
const emailDialogTitle = computed(() => isEmailBound.value ? '换绑邮箱' : '绑定邮箱')

// 表单数据
const editForm = reactive({
  username: '',
  phone: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const phoneForm = reactive({
  oldPhone: '',
  oldCode: '',
  newPhone: '',
  code: ''
})

const emailForm = reactive({
  oldEmail: '',
  oldCode: '',
  newEmail: '',
  code: ''
})

// 获取角色名称
const getRoleName = (role) => {
  const names = { admin: '管理员', owner: '宠物主人', desk: '前台', doctor: '医生' }
  return names[role] || role
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
      editForm.username = userInfo.value.username
    }
  } catch (error) {
    // 模拟数据
    userInfo.value = {
      id: 1,
      username: '宠物主人',
      phone: '138****8000',
      email: 'owner@example.com',
      role: 'owner',
      avatar: '',
      createTime: '2024-01-01'
    }
    editForm.username = userInfo.value.username
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    stats.value = {
      orderCount: 3,
      reserveCount: 5,
      consultCount: 2
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// 保存个人信息（只修改用户名）
const saveEdit = () => {
  if (!editForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  userInfo.value.username = editForm.username
  ElMessage.success('保存成功')
  editInfoDialog.value = false
}

// 修改头像
const handleChangeAvatar = () => {
  avatarDialog.value = true
}

const handleAvatarChange = (file) => {
  avatarUploading.value = true
  setTimeout(() => {
    const mockAvatarUrl = URL.createObjectURL(file.raw)
    userInfo.value.avatar = mockAvatarUrl
    ElMessage.success('头像上传成功')
    avatarDialog.value = false
    avatarUploading.value = false
  }, 500)
}

const saveAvatar = () => {
  ElMessage.info('请先选择图片')
}

// 保存密码
const savePassword = () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度至少6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (passwordForm.oldPassword === passwordForm.newPassword) {
    ElMessage.warning('新密码不能与旧密码相同')
    return
  }
  
  ElMessage.success('密码修改成功，请重新登录')
  passwordDialog.value = false
  
  setTimeout(() => {
    ElMessage.info('请重新登录')
    router.push('/login')
  }, 1500)
}

// 发送原手机验证码（换绑时验证）
const sendOldPhoneCode = () => {
  if (!phoneForm.oldPhone) {
    ElMessage.warning('请输入原手机号')
    return
  }
  if (phoneForm.oldPhone !== userInfo.value.phone) {
    ElMessage.warning('原手机号不正确')
    return
  }
  ElMessage.success('验证码已发送')
  oldPhoneCodeCountdown.value = 60
  const timer = setInterval(() => {
    oldPhoneCodeCountdown.value--
    if (oldPhoneCodeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// 发送新手机验证码（绑定/换绑时）
const sendPhoneCode = () => {
  if (!phoneForm.newPhone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  ElMessage.success('验证码已发送')
  phoneCodeCountdown.value = 60
  const timer = setInterval(() => {
    phoneCodeCountdown.value--
    if (phoneCodeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// 保存手机绑定/换绑
const savePhone = () => {
  // 换绑时需要验证原手机
  if (isPhoneBound.value) {
    if (!phoneForm.oldPhone) {
      ElMessage.warning('请输入原手机号')
      return
    }
    if (phoneForm.oldPhone !== userInfo.value.phone) {
      ElMessage.warning('原手机号不正确')
      return
    }
    if (!phoneForm.oldCode) {
      ElMessage.warning('请输入原手机验证码')
      return
    }
  }
  
  // 验证新手机
  if (!phoneForm.newPhone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!phoneForm.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  // 绑定/换绑成功
  userInfo.value.phone = phoneForm.newPhone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  ElMessage.success(isPhoneBound.value ? '换绑成功' : '绑定成功')
  phoneDialog.value = false
  
  // 清空表单
  phoneForm.oldPhone = ''
  phoneForm.oldCode = ''
  phoneForm.newPhone = ''
  phoneForm.code = ''
}

// 发送原邮箱验证码
const sendOldEmailCode = () => {
  if (!emailForm.oldEmail) {
    ElMessage.warning('请输入原邮箱')
    return
  }
  if (emailForm.oldEmail !== userInfo.value.email) {
    ElMessage.warning('原邮箱不正确')
    return
  }
  ElMessage.success('验证码已发送')
  oldEmailCodeCountdown.value = 60
  const timer = setInterval(() => {
    oldEmailCodeCountdown.value--
    if (oldEmailCodeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// 发送新邮箱验证码
const sendEmailCode = () => {
  if (!emailForm.newEmail) {
    ElMessage.warning('请输入邮箱')
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(emailForm.newEmail)) {
    ElMessage.warning('请输入正确的邮箱')
    return
  }
  ElMessage.success('验证码已发送')
  emailCodeCountdown.value = 60
  const timer = setInterval(() => {
    emailCodeCountdown.value--
    if (emailCodeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// 保存邮箱绑定/换绑
const saveEmail = () => {
  // 换绑时需要验证原邮箱
  if (isEmailBound.value) {
    if (!emailForm.oldEmail) {
      ElMessage.warning('请输入原邮箱')
      return
    }
    if (emailForm.oldEmail !== userInfo.value.email) {
      ElMessage.warning('原邮箱不正确')
      return
    }
    if (!emailForm.oldCode) {
      ElMessage.warning('请输入原邮箱验证码')
      return
    }
  }
  
  // 验证新邮箱
  if (!emailForm.newEmail) {
    ElMessage.warning('请输入邮箱')
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(emailForm.newEmail)) {
    ElMessage.warning('请输入正确的邮箱')
    return
  }
  if (!emailForm.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  userInfo.value.email = emailForm.newEmail
  ElMessage.success(isEmailBound.value ? '换绑成功' : '绑定成功')
  emailDialog.value = false
  
  // 清空表单
  emailForm.oldEmail = ''
  emailForm.oldCode = ''
  emailForm.newEmail = ''
  emailForm.code = ''
}

// 跳转
const goToOrders = () => {
  router.push('/orders')
}

const goToReserve = () => {
  router.push('/reserve')
}

const goToConsult = () => {
  router.push('/consult')
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
    ElMessage.success('已退出登录')
    router.push('/login')
  })
}

onMounted(() => {
  loadUserInfo()
  loadStats()
})
</script>

<style scoped>
.page-content {
  padding-bottom: 80px;
  min-height: 100vh;
  background: #f8f9fc;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  border: 3px solid white;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
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
}

.username {
  font-size: 22px;
  margin-bottom: 8px;
}

.user-role {
  margin-bottom: 4px;
}

.user-since {
  font-size: 12px;
  opacity: 0.8;
}

.stats-row {
  display: flex;
  gap: 15px;
  padding: 0 20px;
  margin-top: -30px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: white;
  border-radius: 16px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-3px);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.settings-card {
  background: white;
  border-radius: 20px;
  margin: 0 20px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.card-title {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.setting-item:hover {
  background: #f8f9fc;
}

.setting-item.danger {
  color: #f56c6c;
}

.setting-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.setting-icon {
  font-size: 22px;
  width: 32px;
}

.setting-label {
  font-size: 14px;
  font-weight: 500;
}

.setting-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.setting-desc.unbind {
  color: #f56c6c;
}

.arrow {
  color: #ccc;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 10px 20px 20px;
  box-shadow: 0 -2px 15px rgba(0,0,0,0.05);
  border-radius: 20px 20px 0 0;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #999;
  font-size: 12px;
  transition: all 0.3s;
}

.nav-item.active {
  color: #667eea;
}

.nav-item .el-icon {
  font-size: 22px;
}

.code-input {
  display: flex;
  gap: 10px;
}

.code-input .el-input {
  flex: 1;
}
</style>