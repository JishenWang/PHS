<template>
  <div class="login-container">
    <div class="background">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>

    <div class="login-box">
      <!-- 语言切换 -->
      <div class="lang-switch">
        <el-dropdown trigger="click" @command="switchLanguage">
          <span class="lang-btn">
            {{ currentLangLabel }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh">中文</el-dropdown-item>
              <el-dropdown-item command="en">English</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 角色选择 -->
      <div v-if="step === 'select'" class="role-select">
        <h2 class="title">{{ $t('login.title') }}</h2>
        <p class="subtitle">{{ $t('login.subtitle') }}</p>

        <div class="role-list">
          <div class="role-item" @click="selectRole('ADMIN')">
            <span class="role-icon">👨‍💼</span>
            <div class="role-info">
              <h3>{{ $t('login.admin') }}</h3>
              <p>{{ $t('login.adminDesc') }}</p>
            </div>
          </div>

          <div class="role-item" @click="selectRole('DOCTOR')">
            <span class="role-icon">👨‍⚕️</span>
            <div class="role-info">
              <h3>{{ $t('login.doctor') }}</h3>
              <p>{{ $t('login.doctorDesc') }}</p>
            </div>
          </div>

          <div class="role-item" @click="selectRole('DESK')">
            <span class="role-icon">💁</span>
            <div class="role-info">
              <h3>{{ $t('login.desk') }}</h3>
              <p>{{ $t('login.deskDesc') }}</p>
            </div>
          </div>

          <div class="role-item" @click="selectRole('OWNER')">
            <span class="role-icon">🐕</span>
            <div class="role-info">
              <h3>{{ $t('login.owner') }}</h3>
              <p>{{ $t('login.ownerDesc') }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 登录表单 -->
      <div v-else class="login-form-box">
        <div class="form-header">
          <span class="back-link" @click="step = 'select'">← {{ $t('login.back') }}</span>
          <h2>{{ roleName }}{{ $t('login.login') }}</h2>
        </div>

        <form @submit.prevent="handleLogin" class="form-body">
          <div class="form-item">
            <label>{{ $t('login.phone') }}</label>
            <input
              v-model="form.phone"
              type="text"
              :placeholder="$t('login.phonePlaceholder')"
              maxlength="11"
            />
          </div>

          <!-- 密码登录 -->
          <div class="form-item" v-if="loginType === 'password'">
            <label>{{ $t('login.password') }}</label>
            <input
              v-model="form.password"
              type="password"
              :placeholder="$t('login.passwordPlaceholder')"
            />
          </div>

          <!-- 验证码登录 -->
          <div class="form-item" v-else>
            <label>{{ $t('login.code') }}</label>
            <div class="code-box">
              <input
                v-model="form.code"
                type="text"
                :placeholder="$t('login.codePlaceholder')"
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
              <input type="checkbox" v-model="form.remember" /> {{ $t('login.rememberMe') }}
            </label>
            <span class="switch-type" @click="toggleLoginType">
              {{ loginType === 'password' ? $t('login.codeLogin') : $t('login.passwordLogin') }}
            </span>
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? $t('login.loggingIn') : $t('login.login') }}
          </button>
        </form>

        <div class="form-tips">
          <p>🐾 {{ $t('login.welcome') }}</p>
          <p class="sub-tip">{{ $t('login.useRegisteredAccount') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { setToken, setUserInfo, setUserRole } from '@/utils/auth'
import { usePermissionStore } from '@/store/permission'
import { setLocale, getLocale } from '@/locales'

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
      codeText: this.$t('login.getCode'),
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
      const keyMap = {
        'ADMIN': 'login.admin',
        'DOCTOR': 'login.doctor',
        'DESK': 'login.desk',
        'OWNER': 'login.owner'
      }
      return this.$t(keyMap[this.role] || '')
    },
    currentLangLabel() {
      return getLocale() === 'zh' ? '中文' : 'English'
    }
  },
  watch: {
    '$i18n.locale'() {
      this.updateCodeText()
    }
  },
  methods: {
    switchLanguage(lang) {
      setLocale(lang)
      window.location.reload()
    },

    updateCodeText() {
      if (!this.codeSending) {
        this.codeText = this.$t('login.getCode')
      }
    },

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
        alert(this.$t('login.pleaseEnterPhone'))
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        alert(this.$t('login.phoneFormatError'))
        return
      }

      this.codeSending = true
      this.codeText = this.$t('login.sending')

      try {
        const res = await axios.post('/api/auth/sendCode?phone=' + this.form.phone)
        if (res.data.code == 200) {
          alert('Captcha: ' + res.data.data)
          this.startCountdown()
        } else {
          alert(res.data.msg || res.data.message || this.$t('login.sendFailed'))
          this.codeSending = false
          this.codeText = this.$t('login.getCode')
        }
      } catch (err) {
        alert(this.$t('login.networkError'))
        this.codeSending = false
        this.codeText = this.$t('login.getCode')
      }
    },

    startCountdown() {
      let count = 60
      this.codeText = this.$t('login.retryAfter', { count })

      this.codeTimer = setInterval(() => {
        count--
        if (count <= 0) {
          clearInterval(this.codeTimer)
          this.codeSending = false
          this.codeText = this.$t('login.getCode')
        } else {
          this.codeText = this.$t('login.retryAfter', { count })
        }
      }, 1000)
    },

    async handleLogin() {
      if (!this.form.phone) {
        alert(this.$t('login.pleaseEnterPhone'))
        return
      }
      if (this.loginType === 'password' && !this.form.password) {
        alert(this.$t('login.pleaseEnterPassword'))
        return
      }
      if (this.loginType === 'code' && !this.form.code) {
        alert(this.$t('login.pleaseEnterCode'))
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
          alert(res.data.msg || res.data.message || this.$t('login.loginFailed'))
        }
      } catch (err) {
        console.error('登录失败:', err)
        alert(this.$t('login.loginFailed') + '：' + (err.message || this.$t('login.networkError2')))
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
  background: url('@/assets/images/login-bg.jpg') center center / cover no-repeat;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding-right: 10vw;
}

.background {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.circle, .circle-1, .circle-2 {
  display: none;
}

.login-box {
  width: 420px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  position: relative;
  z-index: 1;
}

.lang-switch {
  position: absolute;
  top: 16px;
  right: 20px;
  z-index: 2;
}

.lang-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #3b82f6;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 6px;
  transition: background 0.2s;
}

.lang-btn:hover {
  background: rgba(59, 130, 246, 0.1);
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
  border-color: #3b82f6;
  transform: translateX(5px);
  box-shadow: 0 5px 20px rgba(59, 130, 246, 0.15);
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
  color: #3b82f6;
  cursor: pointer;
  font-size: 14px;
  display: inline-block;
  margin-bottom: 15px;
}

.back-link:hover {
  color: #2563eb;
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
  border-color: #3b82f6;
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
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.code-btn:hover:not(:disabled) {
  background: #2563eb;
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
  color: #3b82f6;
  cursor: pointer;
}

.switch-type:hover {
  text-decoration: underline;
}

.submit-btn {
  height: 48px;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
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