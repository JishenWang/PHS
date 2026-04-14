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
    // 登录（支持角色）
    async login(loginData) {
      try {
        // 如果后端接口还没准备好，使用模拟登录
        // 模拟不同角色的演示账号
        const mockUsers = {
          admin: { username: 'admin', password: '123456', role: 'admin', name: '系统管理员' },
          owner: { username: 'owner', password: '123456', role: 'owner', name: '宠物主人' },
          desk: { username: 'desk', password: '123456', role: 'desk', name: '前台工作人员' },
          doctor: { username: 'doctor', password: '123456', role: 'doctor', name: '张医生' }
        }
        
        const user = mockUsers[loginData.role]
        if (user && loginData.username === user.username && loginData.password === user.password) {
          const token = `mock-token-${user.role}-${Date.now()}`
          const userInfo = {
            id: 1,
            username: user.name,
            phone: '13800138000',
            email: `${user.role}@example.com`,
            role: user.role,
            avatar: ''
          }
          
          this.token = token
          this.userInfo = userInfo
          this.role = user.role
          
          setToken(token)
          setUserInfo(userInfo)
          setUserRole(user.role)
          
          return { success: true, role: user.role }
        }
        
        return { success: false, message: '账号或密码错误' }
      } catch (error) {
        return { success: false, message: error.message || '登录失败' }
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
        return this.userInfo
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