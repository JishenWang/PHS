import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)
  
  // 页面标题
  const pageTitle = ref('')
  
  // 切换侧边栏
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  
  // 设置页面标题
  function setPageTitle(title) {
    pageTitle.value = title
  }
  
  return {
    sidebarCollapsed,
    pageTitle,
    toggleSidebar,
    setPageTitle
  }
})