import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes } from './constant'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

// 白名单
const whiteList = ['/login', '/403', '/404']
let hasAddRoutes = false

function getHomePathByRole(role) {
  const pathMap = {
    admin: '/admin/dashboard',
    owner: '/owner/pet',
    desk: '/desk/register',
    doctor: '/doctor/accept'
  }
  return pathMap[role] || '/login'
}

// ========== 新增：强制清理函数 ==========
function forceLogout() {
  removeToken()
  removeUserInfo()
  removeUserRole()
  hasAddRoutes = false
}

router.beforeEach(async (to, from, next) => {
  const hasToken = getToken()
  
  if (hasToken) {
    // ========== 已登录状态 ==========
    
    // 如果访问登录页，根据角色跳转首页
    if (to.path === '/login') {
      const role = localStorage.getItem('pet_hospital_role') || 'owner'
      const homePath = getHomePathByRole(role)
      next(homePath)
    } else {
      next()
    }
    
    // 访问其他页面
    if (!hasAddRoutes) {
      const userStore = useUserStore()
      const permissionStore = usePermissionStore()
      
      try {
        // 获取用户信息
        if (!userStore.role) {
          const userInfo = await userStore.getUserInfo()
          if (!userInfo?.role) {
            throw new Error('获取角色失败')
          }
        }

        // 生成动态路由
        await permissionStore.generateRoutes(userStore.role)
        hasAddRoutes = true

        // 关键修复：访问根路径直接跳转首页
        if (to.path === '/') {
          next(getHomePathByRole(userStore.role))
        } else {
          next({ ...to, replace: true })
        }
        
      } catch (error) {
        console.error('路由守卫错误:', error)
        forceLogout()
        next('/login')
      }
    } else {
      next()
    }
    
  } else {
    // ========== 未登录状态 ==========
    
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