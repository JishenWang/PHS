import { defineStore } from 'pinia'
import router from '../router'
import { asyncRoutes, constantRoutes } from '../router/constant'

// 根据角色筛选路由
function filterRoutesByRole(routes, role) {
  const filtered = []
  for (const route of routes) {
    const routeCopy = { ...route }
    if (routeCopy.meta?.roles && !routeCopy.meta.roles.includes(role)) {
      continue
    }
    if (routeCopy.children) {
      routeCopy.children = filterRoutesByRole(routeCopy.children, role)
    }
    filtered.push(routeCopy)
  }
  return filtered
}

// 404路由（必须放在所有路由最后）
const notFoundRoute = {
  path: '/:pathMatch(.*)*',
  name: 'NotFound',
  redirect: '/404',
  hidden: true
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    addRoutes: []
  }),
  
  getters: {
    sidebarRoutes: (state) => {
      return state.routes.filter(route => !route.hidden && route.path !== '/')
    }
  },
  
  actions: {
    generateRoutes(role) {
      return new Promise((resolve) => {
        const accessedRoutes = filterRoutesByRole(asyncRoutes, role)
        
        // ✅ 在动态路由最后添加404通配符
        accessedRoutes.push(notFoundRoute)
        
        this.addRoutes = accessedRoutes
        this.routes = constantRoutes.concat(accessedRoutes)
        
        accessedRoutes.forEach(route => {
          if (!router.hasRoute(route.name)) {
            router.addRoute(route)
            console.log('已添加路由:', route.path)
          }
        })
        
        console.log('当前所有路由:', router.getRoutes().map(r => r.path))
        resolve(accessedRoutes)
      })
    },
    
    resetRoutes() {
      this.routes = []
      this.addRoutes = []
    }
  }
})