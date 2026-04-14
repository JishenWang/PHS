// 认证工具 - 负责token存储、用户信息存储

const TOKEN_KEY = 'pet_hospital_token'
const USER_KEY = 'pet_hospital_user'
const ROLE_KEY = 'pet_hospital_role'

/**
 * 获取Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  const userStr = localStorage.getItem(USER_KEY)
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch (e) {
      return null
    }
  }
  return null
}

/**
 * 设置用户信息
 */
export function setUserInfo(userInfo) {
  localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  localStorage.removeItem(USER_KEY)
}

/**
 * 获取用户角色
 */
export function getUserRole() {
  return localStorage.getItem(ROLE_KEY)
}

/**
 * 设置用户角色
 */
export function setUserRole(role) {
  localStorage.setItem(ROLE_KEY, role)
}

/**
 * 移除用户角色
 */
export function removeUserRole() {
  localStorage.removeItem(ROLE_KEY)
}

/**
 * 清除所有登录信息
 */
export function clearAuth() {
  removeToken()
  removeUserInfo()
  removeUserRole()
}

/**
 * 判断是否已登录
 */
export function isLoggedIn() {
  return !!getToken()
}