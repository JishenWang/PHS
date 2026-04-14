import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes } from './constant'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

// 白名单
const whiteList = ['/login', '/403', '/404']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const hasToken = getToken()
  
  if (hasToken) {
    // 已登录
    if (to.path === '/login') {
      const role = localStorage.getItem('pet_hospital_role') || 'owner'
      const homePath = getHomePathByRole(role)
      next(homePath)
    } else {
      next()
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next('/login')
    }
  }
})

function getHomePathByRole(role) {
  switch (role) {
    case 'admin':
      return '/admin/dashboard'
    case 'owner':
      return '/owner/pet'
    case 'desk':
      return '/desk/customer'
    case 'doctor':
      return '/doctor/accept'
    default:
      return '/login'
  }
}

export default router