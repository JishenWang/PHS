// 统一导出所有模块路由
import adminRoutes from './admin'
import ownerRoutes from './owner'
import deskRoutes from './desk'
import doctorRoutes from './doctor'

// 处理格式不统一的问题
// admin.js 和 desk.js 导出的是数组，需要取第一个元素
const getRouteObject = (route) => {
  if (Array.isArray(route)) {
    return route[0]
  }
  return route
}

export const adminRoute = getRouteObject(adminRoutes)
export const ownerRoute = ownerRoutes
export const deskRoute = getRouteObject(deskRoutes)
export const doctorRoute = getRouteObject(doctorRoutes)

// 所有动态路由数组（统一格式）
export const asyncRoutes = [
  adminRoute,
  ownerRoute,
  deskRoute,
  doctorRoute
].filter(Boolean)