import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes, asyncRoutes } from './constant'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...constantRoutes,  // 常量路由：/login, /403, /404
    ...asyncRoutes      // 动态路由：/admin, /owner, /desk, /doctor
  ]
})

// 路由守卫（权限控制）
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 白名单，不需要登录
  const whiteList = ['/login', '/403', '/404']
  
  if (whiteList.includes(to.path)) {
    // 在白名单中，直接放行
    next()
  } else if (!token) {
    // 未登录，跳转到登录页
    next('/login')
  } else {
    // 已登录，放行
    next()
  }
})

export default router