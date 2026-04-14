import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes, asyncRoutes } from './constant'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

// 白名单（无需登录即可访问）
const whiteList = ['/login', '/403', '/404']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const hasToken = getToken()
  const userStore = useUserStore()

  if (hasToken) {
    // 已登录
    if (to.path === '/login') {
      // 已登录，跳转到对应角色的首页
      const role = userStore.role || 'owner'
      const homePath = getHomePathByRole(role)
      next(homePath)
    } else {
      // 检查是否有角色
      if (userStore.role) {
        next()
      } else {
        try {
          const userInfo = await userStore.getUserInfo()
          if (userInfo && userInfo.role) {
            // 动态添加路由
            asyncRoutes.forEach(route => {
              router.addRoute(route)
            })
            next({ ...to, replace: true })
          } else {
            next('/login')
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
          next('/login')
        }
      }
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

// 根据角色获取首页路径
function getHomePathByRole(role) {
  switch (role) {
    case 'admin':
      return '/admin/dashboard'
    case 'owner':
      return '/owner/pet'
    case 'desk':
      return '/desk/register'
    case 'doctor':
      return '/doctor/accept'
    default:
      return '/login'
  }
}

export default router