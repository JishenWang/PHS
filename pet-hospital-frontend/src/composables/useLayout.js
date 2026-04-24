import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DataLine, UserFilled, FirstAidKit, Tickets, Setting,
  Calendar, Document, ChatDotRound, Money, Notebook,
} from '@element-plus/icons-vue'

const iconRegistry = {
  DataLine, UserFilled, FirstAidKit, Tickets, Setting,
  Calendar, Document, ChatDotRound, Money, Notebook,
}

export function getIcon(name) {
  return iconRegistry[name] || null
}

/**
 * 统一布局 Composable
 * 提供：侧边栏菜单自动生成、折叠控制、面包屑
 */
export function useLayout() {
  const route = useRoute()
  const router = useRouter()

  // 折叠控制
  const isCollapse = ref(false)
  const toggleCollapse = () => { isCollapse.value = !isCollapse.value }

  // 当前端前缀 /admin /doctor /desk /owner
  const currentPrefix = computed(() => {
    const parts = route.path.split('/')
    return parts.length > 1 ? `/${parts[1]}` : '/'
  })

  // 从路由自动生成侧边栏菜单（过滤 hidden）
  const sidebarMenus = computed(() => {
    const parent = router.getRoutes().find(r => r.path === currentPrefix.value)
    if (!parent?.children?.length) return []
    return parent.children
      .filter(child => !child.meta?.hidden)
      .map(child => ({
        path: child.path,
        title: child.meta?.title || '',
        icon: getIcon(child.meta?.icon),
      }))
  })

  const activeMenu = computed(() => route.path)

  // 面包屑（排除当前端根路由）
  const breadcrumbList = computed(() =>
    route.matched
      .filter(item => item.meta?.title && item.path !== currentPrefix.value)
      .map(item => ({ title: item.meta.title, path: item.path }))
  )

  return { isCollapse, toggleCollapse, currentPrefix, sidebarMenus, activeMenu, breadcrumbList }
}
