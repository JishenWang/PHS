<template>
  <div class="login-container">
    <div class="background">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>
    
    <div class="login-box">
      <!-- 角色选择 -->
      <div v-if="step === 'select'" class="role-select">
        <h2 class="title">宠物医院管理系统</h2>
        <p class="subtitle">请选择登录端</p>
        
        <div class="role-list">
          <div class="role-item" @click="selectRole('ADMIN')">
            <span class="role-icon">👨‍💼</span>
            <div class="role-info">
              <h3>管理端</h3>
              <p>系统管理、数据统计</p>
            </div>
          </div>
          
          <div class="role-item" @click="selectRole('DOCTOR')">
            <span class="role-icon">👨‍⚕️</span>
            <div class="role-info">
              <h3>医生端</h3>
              <p>接诊、病历管理</p>
            </div>
          </div>
          
          <div class="role-item" @click="selectRole('DESK')">
            <span class="role-icon">💁</span>
            <div class="role-info">
              <h3>前台端</h3>
              <p>挂号、收费、客户管理</p>
            </div>
          </div>
          
          <div class="role-item" @click="selectRole('OWNER')">
            <span class="role-icon">🐕</span>
            <div class="role-info">
              <h3>客户端</h3>
              <p>预约挂号、查看病历</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 登录表单 -->
      <div v-else class="login-form-box">
        <div class="form-header">
          <span class="back-link" @click="step = 'select'">← 返回</span>
          <h2>{{ roleName }}登录</h2>
        </div>
        
        <form @submit.prevent="handleLogin" class="form-body">
          <div class="form-item">
            <label>手机号</label>
            <input 
              v-model="form.phone" 
              type="text" 
              placeholder="请输入手机号"
              maxlength="11"
            />
          </div>
          
          <!-- 密码登录 -->
          <div class="form-item" v-if="loginType === 'password'">
            <label>密码</label>
            <input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码"
            />
          </div>
          
          <!-- 验证码登录 -->
          <div class="form-item" v-else>
            <label>验证码</label>
            <div class="code-box">
              <input 
                v-model="form.code" 
                type="text" 
                placeholder="请输入验证码"
                maxlength="6"
              />
              <button 
                type="button" 
                class="code-btn"
                :disabled="codeSending"
                @click="sendCode"
              >
                {{ codeText }}
              </button>
            </div>
          </div>
          
          <div class="form-options">
            <label class="remember">
              <input type="checkbox" v-model="form.remember" /> 记住我
            </label>
            <span class="switch-type" @click="toggleLoginType">
              {{ loginType === 'password' ? '验证码登录' : '密码登录' }}
            </span>
          </div>
          
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>
        
        <div class="form-tips">
          <p>🐾 欢迎使用宠物医院管理系统</p>
          <p class="sub-tip">请使用已注册的账号登录</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { setToken, setUserInfo, setUserRole } from '@/utils/auth'
import { usePermissionStore } from '@/store/permission'

axios.defaults.baseURL = ''
axios.defaults.timeout = 10000

export default {
  name: 'Login',
  data() {
    return {
      step: 'select',
      role: '',
      loginType: 'password',
      loading: false,
      codeSending: false,
      codeText: '获取验证码',
      codeTimer: null,
      form: {
        phone: '',
        password: '',
        code: '',
        remember: false
      }
    }
  },
  computed: {
    roleName() {
      const names = {
        'ADMIN': '管理端',
        'DOCTOR': '医生端',
        'DESK': '前台端',
        'OWNER': '客户端'
      }
      return names[this.role] || ''
    }
  },
  methods: {
    selectRole(role) {
      this.role = role
      this.step = 'login'
      this.form.phone = ''
      this.form.password = ''
      this.form.code = ''
    },
    
    toggleLoginType() {
      this.loginType = this.loginType === 'password' ? 'code' : 'password'
    },
    
    async sendCode() {
      if (!this.form.phone) {
        alert('请输入手机号')
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        alert('手机号格式不正确')
        return
      }
      
      this.codeSending = true
      this.codeText = '发送中...'
      
      try {
        const res = await axios.post('/api/auth/sendCode?phone=' + this.form.phone)
        if (res.data.code == 200) {
          alert('验证码：' + res.data.data)
          this.startCountdown()
        } else {
          alert(res.data.msg || res.data.message || '发送失败')
          this.codeSending = false
          this.codeText = '获取验证码'
        }
      } catch (err) {
        alert('发送失败，请检查网络')
        this.codeSending = false
        this.codeText = '获取验证码'
      }
    },
    
    startCountdown() {
      let count = 60
      this.codeText = count + '秒后重试'
      
      this.codeTimer = setInterval(() => {
        count--
        if (count <= 0) {
          clearInterval(this.codeTimer)
          this.codeSending = false
          this.codeText = '获取验证码'
        } else {
          this.codeText = count + '秒后重试'
        }
      }, 1000)
    },
    
    async handleLogin() {
      if (!this.form.phone) {
        alert('请输入手机号')
        return
      }
      if (this.loginType === 'password' && !this.form.password) {
        alert('请输入密码')
        return
      }
      if (this.loginType === 'code' && !this.form.code) {
        alert('请输入验证码')
        return
      }
      
      this.loading = true
      
      const data = {
        phone: this.form.phone,
        role: this.role,
        loginType: this.loginType,
        remember: this.form.remember
      }
      
      if (this.loginType === 'password') {
        data.password = this.form.password
      } else {
        data.code = this.form.code
      }
      
      try {
        console.log('发送登录请求:', data)
        const res = await axios.post('/api/auth/login', data)
        console.log('登录响应:', res.data)
        
        if (res.data.code == 200) {
          const result = res.data.data
          
          // 存储登录信息
          setToken(result.token || '')
          setUserInfo(result)
          setUserRole(this.role.toLowerCase())
          
          const role = this.role.toLowerCase()
          const permissionStore = usePermissionStore()
          
          await permissionStore.generateRoutes(role)
          
          const homePathMap = {
            'admin': '/admin/dashboard',
            'doctor': '/doctor/accept',
            'desk': '/desk/customer',
            'owner': '/owner/pet'
          }
          const homePath = homePathMap[role] || '/login'
          
          console.log('登录成功，准备跳转到:', homePath)
          

          this.$router.replace(homePath)
          
        } else {
          alert(res.data.msg || res.data.message || '登录失败')
        }
      } catch (err) {
        console.error('登录失败:', err)
        alert('登录失败：' + (err.message || '网络错误'))
      } finally {
        this.loading = false
      }
    }
  },
  
  beforeDestroy() {
    if (this.codeTimer) {
      clearInterval(this.codeTimer)
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.background {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  left: -100px;
}

.circle-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  right: -50px;
}

.login-box {
  width: 420px;
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  position: relative;
  z-index: 1;
}

.role-select {
  text-align: center;
}

.title {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.subtitle {
  color: #999;
  margin-bottom: 30px;
  font-size: 16px;
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.role-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.role-item:hover {
  border-color: #667eea;
  transform: translateX(5px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.15);
}

.role-icon {
  font-size: 36px;
  margin-right: 15px;
}

.role-info {
  text-align: left;
  flex: 1;
}

.role-info h3 {
  color: #333;
  font-size: 18px;
  margin-bottom: 5px;
}

.role-info p {
  color: #999;
  font-size: 13px;
}

.form-header {
  margin-bottom: 30px;
}

.back-link {
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
  display: inline-block;
  margin-bottom: 15px;
}

.back-link:hover {
  color: #764ba2;
}

.form-header h2 {
  font-size: 24px;
  color: #333;
  text-align: center;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  color: #666;
  font-size: 14px;
}

.form-item input {
  height: 44px;
  padding: 0 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.3s;
}

.form-item input:focus {
  border-color: #667eea;
}

.code-box {
  display: flex;
  gap: 10px;
}

.code-box input {
  flex: 1;
}

.code-btn {
  width: 120px;
  height: 44px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.code-btn:hover:not(:disabled) {
  background: #5a6fd6;
}

.code-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.remember {
  color: #666;
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.remember input {
  width: 16px;
  height: 16px;
}

.switch-type {
  color: #667eea;
  cursor: pointer;
}

.switch-type:hover {
  text-decoration: underline;
}

.submit-btn {
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-tips {
  margin-top: 20px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.form-tips p {
  margin: 5px 0;
}

.form-tips .sub-tip {
  font-size: 12px;
  color: #bbb;
  margin-top: 2px;
}
</style>