import { useUserStore } from '../store/user'

/**
 * Permission directive v-permission
 * Usage: v-permission="'admin'" or v-permission="['admin', 'doctor']"
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()
    const userRole = userStore.role
    
    if (value) {
      // Required permissions
      const requiredRoles = Array.isArray(value) ? value : [value]
      
      // Check permissions
      const hasPermission = requiredRoles.includes(userRole)
      
      if (!hasPermission) {
        // No permission, remove element
        el.parentNode?.removeChild(el)
      }
    } else {
      throw new Error('Please set permission value, e.g. v-permission="\'admin\'"')
    }
  }
}