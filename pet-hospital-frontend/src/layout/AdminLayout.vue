<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '80px' : '240px'" class="layout-sidebar" :class="sidebarClass">
      <div class="sidebar-logo">
        <div class="logo-icon">🐾</div>
        <div v-show="!isCollapse" class="logo-text">
          <div class="logo-title">{{ $t('layout.hospitalName') }}</div>
          <div class="logo-sub">{{ $t('layout.adminSub') }}</div>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        :collapse="isCollapse"
        :collapse-transition="false"
        class="layout-menu"
      >
        <el-menu-item
          v-for="menu in sidebarMenus"
          :key="menu.path"
          :index="menu.path"
        >
          <el-icon v-if="menu.icon">
            <component :is="menu.icon || Menu" />
          </el-icon>
          <template #title>{{ menu.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主区域 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb v-if="showBreadcrumb" separator="/" class="layout-breadcrumb">
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbList"
              :key="index"
              :to="index < breadcrumbList.length - 1 ? item.path : undefined"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatar || defaultAvatar" />
              <span v-show="!isCollapse" class="username">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>{{ $t('layout.profile') }}
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>{{ $t('layout.settings') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>{{ $t('layout.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="refreshKey" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user'
import { useSettingsStore } from '@/store/settings'
import { useLayout } from '@/composables/useLayout'
import {
  Fold, Expand, ArrowDown, User, Setting, SwitchButton, Menu
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const router = useRouter()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const { sidebarMenus, activeMenu, breadcrumbList } = useLayout()
const refreshKey = ref(0)

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 折叠状态从 store 管理
const isCollapse = ref(settingsStore.collapseMenu)
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
  settingsStore.collapseMenu = isCollapse.value
}
watch(() => settingsStore.collapseMenu, (val) => {
  isCollapse.value = val
})

// 侧边栏样式 class
const sidebarClass = computed(() => {
  return settingsStore.sidebarStyle === 'dark' ? 'sidebar-dark' : 'sidebar-light'
})

// 是否显示面包屑
const showBreadcrumb = computed(() => settingsStore.showBreadcrumb)

// 显示名称优先取真实姓名
const displayName = computed(() => {
  return userStore.userInfo?.realName || userStore.username || 'Admin'
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/admin/profile')
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'logout':
      ElMessageBox.confirm(t('layout.logoutConfirm'), 'Tip', {
        confirmButtonText: t('layout.confirm'),
        cancelButtonText: t('layout.cancel'),
        type: 'warning'
      }).then(() => userStore.logout())
      break
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: #f8fafc;
}

/* ===== 侧边栏 ===== */
.layout-sidebar {
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
}

.sidebar-logo {
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #f0fdf4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.logo-text {
  margin-left: 12px;
  overflow: hidden;
}

.logo-title {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
}

.logo-sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
  white-space: nowrap;
}

/* ===== 菜单 ===== */
.layout-menu {
  flex: 1;
  background: transparent;
  border-right: none;
  padding: 10px;
}

.layout-menu :deep(.el-menu-item) {
  color: #334155;
  height: 48px;
  line-height: 48px;
  margin: 6px 0;
  border-radius: 10px;
  transition: all 0.3s;
}

.layout-menu :deep(.el-menu-item:hover) {
  background: #f8fafc;
  color: #059669;
}

.layout-menu :deep(.el-menu-item.is-active) {
  background: #f0fdf4;
  color: #059669;
}

.layout-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #059669;
  border-radius: 0 2px 2px 0;
}

.layout-menu :deep(.el-icon) {
  font-size: 20px;
}

/* 折叠模式 */
.layout-menu:deep(.el-menu--collapse) {
  padding: 10px 5px;
}

.layout-menu:deep(.el-menu--collapse .el-menu-item) {
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
}

.layout-menu:deep(.el-menu--collapse .el-icon) {
  margin-right: 0 !important;
  font-size: 22px;
}

/* ===== 深色侧边栏模式 ===== */
.layout-sidebar.sidebar-dark {
  background: #1e293b;
  border-right-color: #334155;
}

.sidebar-dark .sidebar-logo {
  border-bottom-color: #334155;
}

.sidebar-dark .logo-title {
  color: #f1f5f9;
}

.sidebar-dark .logo-sub {
  color: #94a3b8;
}

.sidebar-dark .layout-menu :deep(.el-menu-item) {
  color: #cbd5e1;
}

.sidebar-dark .layout-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.05);
  color: var(--el-color-primary, #3b82f6);
}

.sidebar-dark .layout-menu :deep(.el-menu-item.is-active) {
  background: rgba(59, 130, 246, 0.15);
  color: var(--el-color-primary, #3b82f6);
}

/* ===== 顶部 ===== */
.layout-header {
  height: 64px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  color: #64748b;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.3s;
}

.collapse-btn:hover {
  color: #059669;
  background: #f0fdf4;
}

.layout-breadcrumb :deep(.el-breadcrumb__inner) {
  color: #64748b;
  font-weight: 400;
}

.layout-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #1e293b;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f0fdf4;
}

.username {
  font-size: 14px;
  color: #334155;
}

/* ===== 主内容区 ===== */
.layout-main {
  padding: 20px;
  overflow-y: auto;
  background: #f8fafc;
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(-12px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(12px);
}
</style>
