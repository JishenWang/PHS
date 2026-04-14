// 认证工具 - 负责token存储、用户信息存储

const TOKEN_KEY = 'pet_hospital_token'
const USER_KEY = 'pet_hospital_user'
const ROLE_KEY = 'pet_hospital_role'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

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

export function setUserInfo(userInfo) {
  localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
}

export function removeUserInfo() {
  localStorage.removeItem(USER_KEY)
}

export function getUserRole() {
  return localStorage.getItem(ROLE_KEY)
}

export function setUserRole(role) {
  localStorage.setItem(ROLE_KEY, role)
}

export function removeUserRole() {
  localStorage.removeItem(ROLE_KEY)
}

export function clearAuth() {
  removeToken()
  removeUserInfo()
  removeUserRole()
}

export function isLoggedIn() {
  return !!getToken()
}