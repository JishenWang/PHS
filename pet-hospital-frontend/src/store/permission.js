import { defineStore } from 'pinia'
import router from '../router'
import { asyncRoutes, constantRoutes } from '../router/constant'

// 根据角色筛选路由
function filterRoutesByRole(routes, role) {
  const filtered = []
  for (const route of routes) {
    const routeCopy = { ...route }
    if (routeCopy.meta?.roles) {
      if (!routeCopy.meta.roles.includes(role)) {
        continue
      }
    }
    if (routeCopy.children) {
      routeCopy.children = filterRoutesByRole(routeCopy.children, role)
    }
    filtered.push(routeCopy)
  }
  return filtered
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    addRoutes: []
  }),
  
  actions: {
    // 生成动态路由
    generateRoutes(role) {
      return new Promise((resolve) => {
        let accessedRoutes = []
        
        // 根据角色筛选路由
        accessedRoutes = filterRoutesByRole(asyncRoutes, role)
        
        this.routes = constantRoutes.concat(accessedRoutes)
        this.addRoutes = accessedRoutes
        
        // 动态添加路由
        accessedRoutes.forEach(route => {
          router.addRoute(route)
        })
        
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