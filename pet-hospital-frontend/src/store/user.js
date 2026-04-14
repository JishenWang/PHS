import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, getUserRole, setUserRole, removeUserRole, clearAuth } from '../utils/auth'
import request from '../utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getUserInfo(),
    role: getUserRole()
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'admin',
    isOwner: (state) => state.role === 'owner',
    isDesk: (state) => state.role === 'desk',
    isDoctor: (state) => state.role === 'doctor',
    username: (state) => state.userInfo?.username || '',
    avatar: (state) => state.userInfo?.avatar || ''
  },
  
  actions: {
    // 登录
    async login(loginData) {
      try {
        const response = await request.post('/login', loginData)
        if (response.code === 200) {
          const { token, userInfo, role } = response.data
          this.token = token
          this.userInfo = userInfo
          this.role = role
          setToken(token)
          setUserInfo(userInfo)
          setUserRole(role)
          return { success: true, role }
        }
        return { success: false, message: response.message }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },
    
    // 登出
    async logout() {
      try {
        await request.post('/logout')
      } catch (error) {
        console.error('登出请求失败:', error)
      } finally {
        this.clear()
        clearAuth()
      }
    },
    
    // 清除状态
    clear() {
      this.token = null
      this.userInfo = null
      this.role = null
    },
    
    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await request.get('/common/user/info')
        if (response.code === 200) {
          this.userInfo = response.data
          setUserInfo(response.data)
          return response.data
        }
        return null
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return null
      }
    },
    
    // 修改密码
    async changePassword(oldPassword, newPassword) {
      try {
        const response = await request.post('/common/user/change-password', {
          oldPassword,
          newPassword
        })
        return { success: response.code === 200, message: response.message }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },
    
    // 更新用户信息
    async updateUserInfo(userInfo) {
      try {
        const response = await request.put('/common/user/info', userInfo)
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, ...userInfo }
          setUserInfo(this.userInfo)
          return { success: true, message: '更新成功' }
        }
        return { success: false, message: response.message }
      } catch (error) {
        return { success: false, message: error.message }
      }
    }
  }
})