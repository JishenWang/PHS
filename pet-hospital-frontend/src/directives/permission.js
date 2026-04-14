import { useUserStore } from '../store/user'

/**
 * 权限指令 v-permission
 * 用法：v-permission="'admin'" 或 v-permission="['admin', 'doctor']"
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const userRole = userStore.role
    
    if (value) {
      // 需要的权限
      const requiredRoles = Array.isArray(value) ? value : [value]
      
      // 检查是否有权限
      const hasPermission = requiredRoles.includes(userRole)
      
      if (!hasPermission) {
        // 没有权限，移除元素
        el.parentNode?.removeChild(el)
      }
    } else {
      throw new Error('请设置权限值，如 v-permission="\'admin\'"')
    }
  }
}