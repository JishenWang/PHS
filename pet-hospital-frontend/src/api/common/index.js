import request from '@/utils/request'

// 通用测试接口（验证前后端连通）
export function testConnect() {
  return request({
    url: '/test',
    method: 'get'
  })
}

// 登录接口（通用，所有角色共用）
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}