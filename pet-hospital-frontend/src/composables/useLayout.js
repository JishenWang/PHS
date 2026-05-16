import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  DataLine, UserFilled, FirstAidKit, Tickets, Setting,
  Calendar, Document, ChatDotRound, Money, Notebook, Box,
} from '@element-plus/icons-vue'

const iconRegistry = {
  DataLine, UserFilled, FirstAidKit, Tickets, Setting,
  Calendar, Document, ChatDotRound, Money, Notebook, Box,
}

export function getIcon(name) {
  return iconRegistry[name] || null
}

/**
 * 统一布局 Composable
 * 提供：侧边栏菜单自动生成、折叠控制、历史记录面包屑
 */
export function useLayout() {
  const route = useRoute()
  const router = useRouter()
  const { t } = useI18n()

  // 折叠控制
  const isCollapse = ref(false)
  const toggleCollapse = () => { isCollapse.value = !isCollapse.value }

  // 当前端前缀 /admin /doctor /desk /owner
  const currentPrefix = computed(() => {
    const parts = route.path.split('/')
    return parts.length > 1 ? `/${parts[1]}` : '/'
  })

  // 从路由自动生成侧边栏菜单（过滤 hidden），标题通过 i18n 翻译
  const sidebarMenus = computed(() => {
    const parent = router.getRoutes().find(r => r.path === currentPrefix.value)
    if (!parent?.children?.length) return []
    return parent.children
      .filter(child => !child.meta?.hidden)
      .map(child => ({
        path: parent.path + '/' + child.path,
        title: t(child.meta?.title || ''),
        icon: getIcon(child.meta?.icon),
      }))
  })

  const activeMenu = computed(() => route.path)

  // ==================== 历史记录面包屑 ====================
  const pageHistoryKey = computed(() => 'page_history_' + currentPrefix.value)
  const pageHistory = ref([])

  // 初始化历史记录
  const initHistory = () => {
    const saved = sessionStorage.getItem(pageHistoryKey.value)
    if (saved) {
      try {
        pageHistory.value = JSON.parse(saved)
      } catch (e) {
        pageHistory.value = []
      }
    }
    // 确保当前页面在历史记录中
    const currentPath = route.path
    const currentTitle = route.meta?.title || ''
    const exists = pageHistory.value.some(item => item.path === currentPath)
    if (!exists && currentPath !== '/login') {
      pageHistory.value.push({ path: currentPath, title: currentTitle })
      sessionStorage.setItem(pageHistoryKey.value, JSON.stringify(pageHistory.value))
    }
  }

  initHistory()

  watch(() => route.path, (newPath, oldPath) => {
    if (!newPath || newPath === oldPath) return
    const title = route.meta?.title || ''
    if (newPath === '/login') return

    const idx = pageHistory.value.findIndex(item => item.path === newPath)
    if (idx > -1) {
      // 如果已经在历史栈中，截断到该位置
      pageHistory.value = pageHistory.value.slice(0, idx + 1)
    } else {
      // 否则添加新页面
      pageHistory.value.push({ path: newPath, title })
      // 限制最多保留 6 条
      if (pageHistory.value.length > 6) {
        pageHistory.value = pageHistory.value.slice(-6)
      }
    }

    sessionStorage.setItem(pageHistoryKey.value, JSON.stringify(pageHistory.value))
  })

  // 面包屑标题翻译
  const breadcrumbList = computed(() => {
    return pageHistory.value.map(item => ({
      ...item,
      title: t(item.title)
    }))
  })

  return { isCollapse, toggleCollapse, currentPrefix, sidebarMenus, activeMenu, breadcrumbList }
}
