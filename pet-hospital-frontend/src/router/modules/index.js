import adminRoutes from './admin'
import ownerRoutes from './owner'
import deskRoutes from './desk'
import doctorRoutes from './doctor'

// 所有动态路由模块
export const asyncRouteModules = [
  adminRoutes,
  ownerRoutes,
  deskRoutes,
  doctorRoutes
]

// 导出各个模块供单独使用
export { adminRoutes, ownerRoutes, deskRoutes, doctorRoutes }