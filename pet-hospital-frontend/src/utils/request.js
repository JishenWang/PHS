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
    console.error('请求错误:', error)
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
      const errMsg = res.msg || res.message || '请求失败'

      if (res.code === 401) {
        ElMessage.error(res.msg || res.message || '登录已过期，请重新登录')
        clearAuth()
        router.push('/login')
        return Promise.reject(new Error(res.msg || res.message || '未授权'))
      }

      if (res.code === 403) {
        ElMessage.error(res.msg || res.message || '权限不足')
        router.push('/403')
        return Promise.reject(new Error(res.msg || res.message || '权限不足'))
      }

      ElMessage.error(errMsg)
      return Promise.reject(new Error(errMsg))
    }

    // Desk 模块大量接口返回：{ success, message, ... } 或直接分页对象
    if (typeof res === 'object' && res !== null) {
      if (res.success === false) {
        const msg = res.message || '请求失败'
        ElMessage.error(msg)
        return Promise.reject(new Error(msg))
      }
      return res
    }

    return res
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          clearAuth()
          router.push('/login')
          break
        case 403:
          ElMessage.error('权限不足')
          router.push('/403')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request