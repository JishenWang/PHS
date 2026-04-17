import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, getUserRole, setUserRole, removeUserRole, clearAuth } from '../utils/auth'
import request from '../utils/request'
import { usePermissionStore } from './permission'
import { resetRouter } from '../router'

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
    avatar: (state) => state.userInfo?.avatar || '',
    userId: (state) => state.userInfo?.id || state.userInfo?.userId || '',
    doctorId: (state) => state.userInfo?.doctorId || state.userInfo?.id || ''
  },
  
  actions: {
    // 登录
    async login(loginData) {
      try {
        const response = await request.post('/auth/login', loginData)
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
        
        return { success: false, message: response.message || '账号或密码错误' }
      } catch (error) {
        return { success: false, message: error.message || '登录失败' }
      }
    },
    
    // 登出
    async logout() {
      try {
        await request.post('/auth/logout')
      } catch (error) {
        console.error('登出请求失败:', error)
      } finally {
        const permissionStore = usePermissionStore()
        permissionStore.resetRoutes()
        resetRouter()
        this.clear()
        clearAuth()
        window.location.href = '/login'
      }
    },
    
    // 退出登录（别名，供 DoctorLayout 调用）
    async logoutAction() {
      return this.logout()
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
        const response = await request.get('/auth/userInfo')
        if (response.code === 200) {
          this.userInfo = response.data
          this.role = response.data.role
          setUserInfo(response.data)
          setUserRole(response.data.role)
          return response.data
        }
        return this.userInfo
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return this.userInfo
      }
    },
    
    // 刷新用户信息
    async refreshUserInfo() {
      try {
        const response = await request.get('/auth/userInfo')
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, ...response.data }
          setUserInfo(this.userInfo)
          return this.userInfo
        }
        return this.userInfo
      } catch (error) {
        console.error('刷新用户信息失败:', error)
        return this.userInfo
      }
    },
    
    // 更新用户信息
    updateUserInfo(data) {
      this.userInfo = { ...this.userInfo, ...data }
      setUserInfo(this.userInfo)
    },
    
    // 更新头像
    updateAvatar(avatarUrl) {
      this.userInfo.avatar = avatarUrl
      setUserInfo(this.userInfo)
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
    }
  }
})