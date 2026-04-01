import { defineStore } from 'pinia'
import { login, logout } from '@/api/common'
import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: {},
    roles: []
  }),
  actions: {
    // 登录
    async loginAction(data) {
      const res = await login(data)
      this.token = res.token
      this.userInfo = res.userInfo
      this.roles = res.roles
      setToken(res.token) // 存储到本地
      return res
    },
    // 退出登录
    async logoutAction() {
      await logout()
      this.token = ''
      this.userInfo = {}
      this.roles = []
      removeToken() // 清除本地token
    }
  },
  persist: true // 持久化（可选，需安装pinia-plugin-persistedstate）
})