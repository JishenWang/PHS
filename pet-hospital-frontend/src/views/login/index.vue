<template>
  <div class="login-container">
    <div class="login-background">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    
    <div class="login-content">
      <div class="login-left">
        <h1 class="system-title">宠物医院管理系统</h1>
        <p class="system-desc">Pet Hospital Management System</p>
        <div class="features">
          <div class="feature-item">
            <span class="feature-icon">🏥</span>
            <span>专业诊疗</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">💊</span>
            <span>药品管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📋</span>
            <span>档案记录</span>
          </div>
        </div>
      </div>
      
      <el-card class="login-box" shadow="hover">
        <h2 class="login-title">用户登录</h2>
        
        <el-form 
          :model="loginForm" 
          :rules="rules" 
          ref="loginFormRef"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
            >
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码"
              size="large"
              show-password
              prefix-icon="Lock"
              @keyup.enter="handleLogin"
            >
            </el-input>
          </el-form-item>
          
          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false" @click="handleForgotPassword">忘记密码？</el-link>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading"
              size="large"
              class="login-btn"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-tips">
          <p>提示：管理员账号 admin / 123456</p>
        </div>
      </el-card>
    </div>
    
    <footer class="login-footer">
      <p>© 2024 宠物医院管理系统 | 技术支持：XXX团队</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, getUserInfo } from '@/api/common/index'

const router = useRouter()
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const loginFormRef = ref(null)

onMounted(() => {
  // 检查是否有记住的用户名
  const savedUsername = localStorage.getItem('rememberedUsername')
  if (savedUsername) {
    loginForm.username = savedUsername
    rememberMe.value = true
  }
})

const handleLogin = async () => {
  try {
    // 表单验证
    await loginFormRef.value.validate()
    
    loading.value = true
    
    // 调用登录API
    const res = await login({
      username: loginForm.username,
      password: loginForm.password
    })
    
    if (res.code === 200) {
      const { token, role } = res.data
      
      // 保存登录信息
      localStorage.setItem('token', token)
      localStorage.setItem('userRole', role)
      
      // 记住用户名
      if (rememberMe.value) {
        localStorage.setItem('rememberedUsername', loginForm.username)
      } else {
        localStorage.removeItem('rememberedUsername')
      }
      
      ElMessage.success('登录成功')
      
      // 根据角色跳转到不同页面
      redirectByRole(role)
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请检查用户名和密码')
    }
  } finally {
    loading.value = false
  }
}

// 根据角色跳转
const redirectByRole = (role) => {
  const roleRoutes = {
    'admin': '/admin/dashboard',
    'doctor': '/doctor/accept',
    'desk': '/desk/customer',
    'owner': '/owner/pet'
  }
  
  const targetRoute = roleRoutes[role] || '/login'
  router.push(targetRoute)
}

// 忘记密码
const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 500px;
  height: 500px;
  top: -100px;
  left: -100px;
}

.shape-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: -50px;
}

.shape-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 30%;
}

.login-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 100px;
  padding: 40px;
  z-index: 1;
  position: relative;
}

.login-left {
  color: white;
  max-width: 400px;
}

.system-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
}

.system-desc {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 40px;
  letter-spacing: 2px;
}

.features {
  display: flex;
  gap: 30px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.feature-icon {
  font-size: 32px;
}

.login-box {
  width: 420px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.login-title {
  text-align: center;
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  font-weight: 600;
}

.login-form {
  margin-top: 20px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  border-radius: 8px;
  height: 44px;
}

.login-tips {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.login-footer {
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  padding: 20px;
  font-size: 14px;
  z-index: 1;
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  
  .login-content {
    gap: 0;
  }
}
</style>