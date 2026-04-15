import { defineStore } from 'pinia'
import router from '../router'
import { asyncRoutes, constantRoutes } from '../router/constant'

// 根据角色筛选路由
function filterRoutesByRole(routes, role) {
  const filtered = []
  for (const route of routes) {
    const routeCopy = { ...route }
    // 检查当前路由是否有权限限制
    if (routeCopy.meta?.roles && !routeCopy.meta.roles.includes(role)) {
      continue
    }
    // 递归筛选子路由
    if (routeCopy.children) {
      routeCopy.children = filterRoutesByRole(routeCopy.children, role)
    }
    filtered.push(routeCopy)
  }
  return filtered
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],        // 所有路由（常量 + 动态）
    addRoutes: []      // 仅动态路由
  }),
  
  getters: {
    // 获取侧边栏路由（用于菜单渲染，过滤 hidden）
    sidebarRoutes: (state) => {
      return state.routes.filter(route => !route.hidden && route.path !== '/')
    }
  },
  
  actions: {
    // 生成动态路由
    generateRoutes(role) {
      return new Promise((resolve) => {
        // 根据角色筛选路由
        const accessedRoutes = filterRoutesByRole(asyncRoutes, role)
        
        // 保存到状态
        this.addRoutes = accessedRoutes
        this.routes = constantRoutes.concat(accessedRoutes)
        
        // 动态添加到 router
        accessedRoutes.forEach(route => {
          // 避免重复添加
          if (!router.hasRoute(route.name)) {
            router.addRoute(route)
            console.log('已添加路由:', route.path)
          }
        })
        
        console.log('当前所有路由:', router.getRoutes().map(r => r.path))
        resolve(accessedRoutes)
      })
    },
    
    // 重置路由
    resetRoutes() {
      this.routes = []
      this.addRoutes = []
    }
  }
})