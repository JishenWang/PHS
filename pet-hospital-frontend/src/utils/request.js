import axios from 'axios'
import { getToken, clearAuth } from './auth'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加Token
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 标准 ResultVo 风格：{ code, message, data }
    if (typeof res?.code === 'number') {
      if (res.code === 200) return res
      const errMsg = res.msg || res.message || 'Request failed'

      if (res.code === 401) {
        ElMessage.error(res.msg || res.message || 'Session expired, please log in again')
        clearAuth()
        router.push('/login')
        return Promise.reject(new Error(res.msg || res.message || 'Unauthorized'))
      }

      if (res.code === 403) {
        ElMessage.error(res.msg || res.message || 'Insufficient permission')
        // 清除认证并跳转登录（token 过期等情况）
        clearAuth()
        router.push('/login')
        return Promise.reject(new Error(res.msg || res.message || 'Insufficient permission'))
      }

      ElMessage.error(errMsg)
      return Promise.reject(new Error(errMsg))
    }

    // Desk 模块大量接口返回：{ success, message, ... } 或直接分页对象
    if (typeof res === 'object' && res !== null) {
      if (res.success === false) {
        const msg = res.message || 'Request failed'
        ElMessage.error(msg)
        return Promise.reject(new Error(msg))
      }
      return res
    }

    return res
  },
  error => {
    console.error('Response error:', error)
    
    if (error.response) {
      const { status } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('Session expired, please log in again')
          clearAuth()
          router.push('/login')
          break
        case 403:
          ElMessage.error('Insufficient permission')
          // 清除认证并跳转登录（token 过期等情况）
          clearAuth()
          router.push('/login')
          break
        case 404:
          ElMessage.error('Requested resource not found')
          break
        case 500:
          ElMessage.error('Server internal error')
          break
        default:
          ElMessage.error(error.response.data?.message || 'Request failed')
      }
    } else if (error.request) {
      ElMessage.error('Network connection failed, please check network')
    } else {
      ElMessage.error(error.message || 'Request failed')
    }
    
    return Promise.reject(error)
  }
)

export default request