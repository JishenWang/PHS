import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes } from './constant'
import { getToken, removeToken, removeUserInfo, removeUserRole } from '@/utils/auth'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

const whiteList = ['/login', '/403', '/404']

function getHomePathByRole(role) {
  const pathMap = {
    admin: '/admin/dashboard',
    doctor: '/doctor/accept',
    desk: '/desk/customer',
    owner: '/owner/pet'
  }
  return pathMap[role] || '/login'
}

function forceLogout() {
  removeToken()
  removeUserInfo()
  removeUserRole()
}

let hasAddRoutes = false

export function resetRouter() {
  hasAddRoutes = false
}

router.beforeEach(async (to, from, next) => {
  const hasToken = getToken()
  
  console.log('路由守卫:', { to: to.path, hasToken: !!hasToken, hasAddRoutes })

  // ✅ 关键修改：根路径强制跳转到登录页
  if (to.path === '/') {
    next('/login')
    return
  }

  if (hasToken) {
    // 已登录且访问登录页，跳转到对应角色首页
    if (to.path === '/login') {
      const userStore = useUserStore()
      const role = userStore.role || localStorage.getItem('pet_hospital_role')
      next(getHomePathByRole(role))
      return
    }

    if (!hasAddRoutes) {
      const userStore = useUserStore()
      const permissionStore = usePermissionStore()
      
      try {
        if (!userStore.role) {
          const userInfo = await userStore.getUserInfo()
          if (!userInfo?.role) {
            throw new Error('获取角色失败')
          }
        }

        await permissionStore.generateRoutes(userStore.role)
        hasAddRoutes = true
        
        // 重新导航到当前目标
        next({ ...to, replace: true })
        
      } catch (error) {
        console.error('路由守卫错误:', error)
        forceLogout()
        next('/login')
      }
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

export default router