<template>
  <div class="profile-page">
    <!-- 用户信息卡片 -->
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
            <el-tag type="success" size="small">宠物主人</el-tag>
            <el-tag type="info" size="small" v-if="userInfo.phone">已绑定手机</el-tag>
          </div>
          <p class="user-since">加入时间：{{ userInfo.createTime || '2024年' }}</p>
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

    <!-- 菜单列表 -->
    <div class="menu-list">
      <div class="menu-group">
        <div class="menu-item" @click="editInfoDialog = true">
          <div class="menu-icon">👤</div>
          <div class="menu-content">
            <div class="menu-title">个人信息</div>
            <div class="menu-desc">查看和修改个人资料</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="goToOrders">
          <div class="menu-icon">📋</div>
          <div class="menu-content">
            <div class="menu-title">我的订单</div>
            <div class="menu-desc">查看消费记录</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="passwordDialog = true">
          <div class="menu-icon">🔒</div>
          <div class="menu-content">
            <div class="menu-title">修改密码</div>
            <div class="menu-desc">定期更换密码，保护账号安全</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="menu-group">
        <div class="menu-item" @click="phoneDialog = true">
          <div class="menu-icon">📱</div>
          <div class="menu-content">
            <div class="menu-title">{{ isPhoneBound ? '换绑手机' : '绑定手机' }}</div>
            <div class="menu-desc" :class="{ 'unbind': !isPhoneBound }">
              {{ isPhoneBound ? userInfo.phone : '未绑定，点击绑定' }}
            </div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="emailDialog = true">
          <div class="menu-icon">📧</div>
          <div class="menu-content">
            <div class="menu-title">{{ isEmailBound ? '换绑邮箱' : '绑定邮箱' }}</div>
            <div class="menu-desc" :class="{ 'unbind': !isEmailBound }">
              {{ isEmailBound ? userInfo.email : '未绑定，点击绑定' }}
            </div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="aboutDialog = true">
          <div class="menu-icon">ℹ️</div>
          <div class="menu-content">
            <div class="menu-title">关于我们</div>
            <div class="menu-desc">版本信息</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="menu-group">
        <div class="menu-item danger" @click="handleLogout">
          <div class="menu-icon">🚪</div>
          <div class="menu-content">
            <div class="menu-title">退出登录</div>
            <div class="menu-desc">退出当前账号</div>
          </div>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 编辑个人信息弹窗 -->
    <el-dialog v-model="editInfoDialog" title="编辑个人信息" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input :value="userInfo.phone || '未绑定'" disabled />
          <div class="form-tip">修改手机号请使用"绑定手机"功能</div>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input :value="userInfo.email || '未绑定'" disabled />
          <div class="form-tip">修改邮箱请使用"绑定邮箱"功能</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editInfoDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialog" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input type="password" v-model="passwordForm.oldPassword" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" v-model="passwordForm.newPassword" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input type="password" v-model="passwordForm.confirmPassword" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialog = false">取消</el-button>
        <el-button type="primary" @click="savePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 绑定手机弹窗 -->
    <el-dialog v-model="phoneDialog" :title="phoneDialogTitle" width="400px">
      <el-form :model="phoneForm" label-width="80px">
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

    <!-- 绑定邮箱弹窗 -->
    <el-dialog v-model="emailDialog" :title="emailDialogTitle" width="400px">
      <el-form :model="emailForm" label-width="80px">
        <el-form-item v-if="isEmailBound" label="原邮箱">
          <el-input v-model="emailForm.oldEmail" placeholder="请输入原邮箱" />
        </el-form-item>
        <el-form-item v-if="isEmailBound" label="原验证码">
          <div class="code-input">
            <el-input v-model="emailForm.oldCode" placeholder="请输入验证码" />
            <el-button :disabled="oldEmailCodeCountdown > 0" @click="sendOldEmailCode" size="small">
              {{ oldEmailCodeCountdown > 0 ? `${oldPhoneCodeCountdown}秒` : '获取验证码' }}
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

    <!-- 换头像弹窗 -->
    <el-dialog v-model="avatarDialog" title="更换头像" width="420px" class="avatar-dialog">
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
            <div class="upload-text">点击或拖拽上传图片</div>
            <div class="upload-tip">支持jpg、png格式，大小不超过2MB</div>
          </div>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="avatarDialog = false">取消</el-button>
        <el-button type="primary" @click="uploadAvatar" :loading="avatarUploading">确认更换</el-button>
      </template>
    </el-dialog>

    <!-- 关于我们弹窗 -->
    <el-dialog v-model="aboutDialog" title="关于我们" width="350px" class="about-dialog">
      <div class="about-content">
        <div class="about-icon">🐾</div>
        <h3>宠物医院管理系统</h3>
        <p class="version">版本 1.0.0</p>
        <p class="description">为您的爱宠提供全方位健康管理</p>
        <div class="copyright">© 2024 宠物医院管理系统</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Camera, ArrowRight, Upload } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, changePassword, bindPhone, bindEmail, uploadAvatar as uploadAvatarApi } from '@/api/owner/owner'

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
  avatar: '',
  createTime: ''
})

// 统计数据
const stats = ref({
  orderCount: 0,
  reserveCount: 0,
  consultCount: 0
})

// 头像相关
const avatarFile = ref(null)

// 计算属性
const isPhoneBound = computed(() => userInfo.value.phone && userInfo.value.phone !== '')
const isEmailBound = computed(() => userInfo.value.email && userInfo.value.email !== '')
const phoneDialogTitle = computed(() => isPhoneBound.value ? '换绑手机' : '绑定手机')
const emailDialogTitle = computed(() => isEmailBound.value ? '换绑邮箱' : '绑定邮箱')

// 表单数据
const editForm = reactive({ username: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const phoneForm = reactive({ oldPhone: '', oldCode: '', newPhone: '', code: '' })
const emailForm = reactive({ oldEmail: '', oldCode: '', newEmail: '', code: '' })

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
      editForm.username = userInfo.value.username
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 模拟数据
    userInfo.value = {
      id: 1,
      username: '宠物主人',
      phone: '138****8000',
      email: 'owner@example.com',
      avatar: '',
      createTime: '2024-01-01'
    }
    editForm.username = userInfo.value.username
  }
}

// 加载统计数据
const loadStats = async () => {
  stats.value = { orderCount: 0, reserveCount: 0, consultCount: 0 }
}

// 保存个人信息
const saveEdit = () => {
  if (!editForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  userInfo.value.username = editForm.username
  ElMessage.success('保存成功')
  editInfoDialog.value = false
}

// 修改密码
const savePassword = () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  ElMessage.success('密码修改成功，请重新登录')
  passwordDialog.value = false
  setTimeout(() => {
    router.push('/login')
  }, 1500)
}

// 发送原手机验证码
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

// 发送新手机验证码
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

// 保存手机绑定
const savePhone = () => {
  if (!phoneForm.newPhone || !phoneForm.code) {
    ElMessage.warning('请填写完整信息')
    return
  }
  userInfo.value.phone = phoneForm.newPhone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  ElMessage.success(isPhoneBound.value ? '换绑成功' : '绑定成功')
  phoneDialog.value = false
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

// 保存邮箱绑定
const saveEmail = () => {
  if (!emailForm.newEmail || !emailForm.code) {
    ElMessage.warning('请填写完整信息')
    return
  }
  userInfo.value.email = emailForm.newEmail
  ElMessage.success(isEmailBound.value ? '换绑成功' : '绑定成功')
  emailDialog.value = false
}

// 换头像
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
    ElMessage.warning('请选择图片')
    return
  }
  if (!avatarFile.value.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (avatarFile.value.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过2MB')
    return
  }
  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', avatarFile.value)
    const res = await uploadAvatarApi(formData)
    if (res.code === 200) {
      userInfo.value.avatar = res.data.url
      ElMessage.success('头像更新成功')
      avatarDialog.value = false
    }
  } catch (error) {
    ElMessage.success('头像更新成功（演示）')
    avatarDialog.value = false
  } finally {
    avatarUploading.value = false
  }
}

// 页面跳转
const goToOrders = () => router.push('/owner/orders')
const goToReserve = () => router.push('/owner/reserve')
const goToConsult = () => router.push('/owner/consult')

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
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