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

//  根据角色获取首页路径 
function getHomePathByRole(role) {
  const pathMap = {
    admin: '/admin/dashboard',
    doctor: '/doctor/accept',
    desk: '/desk/customer',
    owner: '/owner/pet'
  }
  return pathMap[role] || '/login'
}

// 强制清理函数
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

  if (hasToken) {

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

        const accessRoutes = await permissionStore.generateRoutes(userStore.role)

        accessRoutes.forEach(route => {
          if (!router.hasRoute(route.name)) {
            router.addRoute(route)
          }
        })
        
        hasAddRoutes = true

        if (to.path === '/' || to.matched.length === 0) {
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
    
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router