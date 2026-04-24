<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '80px' : '240px'" class="layout-sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">🐾</div>
        <div v-show="!isCollapse" class="logo-text">
          <div class="logo-title">宠物医院</div>
          <div class="logo-sub">客户自助端</div>
        </div>
      </div>

      <!-- 用户信息卡片 -->
      <div v-show="!isCollapse" class="user-card">
        <el-avatar :size="44" :src="userStore.avatar || defaultAvatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <div class="user-card-info">
          <div class="user-name">{{ displayName }}</div>
          <div class="user-role">宠物主人</div>
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
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="layout-breadcrumb">
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
          <div class="quick-actions">
            <el-tooltip content="刷新数据" placement="bottom">
              <el-button circle @click="handleRefresh">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </el-tooltip>

            <el-badge :value="unreadCount" :hidden="unreadCount === 0">
              <el-button circle @click="goToMessages">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </div>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatar || defaultAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Tickets /></el-icon>我的订单
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useLayout } from '@/composables/useLayout'
import {
  Fold, Expand, ArrowDown, User, Tickets, SwitchButton,
  UserFilled, Refresh, Bell, Menu
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const { isCollapse, toggleCollapse, sidebarMenus, activeMenu, breadcrumbList } = useLayout()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const displayName = computed(() => userStore.username || '宠物主人')

const unreadCount = ref(0)
let refreshTimer = null

const handleCommand = (command) => {
  switch (command) {
    case 'profile': router.push('/owner/profile'); break
    case 'orders': router.push('/owner/orders'); break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        userStore.logout()
        router.push('/login')
      })
      break
  }
}

const goToMessages = () => ElMessage.info('消息通知功能开发中')

const handleRefresh = () => {
  ElMessage.success('数据已刷新')
  window.location.reload()
}

onMounted(() => {
  refreshTimer = setInterval(() => {
    unreadCount.value = Math.floor(Math.random() * 3)
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: #f8fafc;
}

/* ===== 侧边栏（绿色系） ===== */
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

/* ===== 用户信息卡片 ===== */
.user-card {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
}

.user-card-info {
  margin-left: 12px;
  overflow: hidden;
}

.user-name {
  font-size: 15px;
  font-weight: 500;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #64748b;
  margin-top: 3px;
}

/* ===== 菜单 ===== */
.layout-menu {
  flex: 1;
  background: transparent;
  border-right: none;
  padding: 10px;
  overflow-y: auto;
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

/* 折叠 */
.layout-menu:deep(.el-menu--collapse) {
  padding: 10px 5px;
}

.layout-menu:deep(.el-menu--collapse .el-menu-item) {
  padding: 0 !important;
  justify-content: center;
}

.layout-menu:deep(.el-menu--collapse .el-icon) {
  margin-right: 0 !important;
  font-size: 22px;
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
  gap: 16px;
}

.quick-actions {
  display: flex;
  gap: 8px;
}

.quick-actions .el-button {
  width: 36px;
  height: 36px;
  border: none;
  background: #f1f5f9;
  color: #64748b;
}

.quick-actions .el-button:hover {
  background: #f0fdf4;
  color: #059669;
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
  padding: 0;
  overflow-y: auto;
  background: #f8fafc;
}

.content-wrapper {
  padding: 20px;
  min-height: 100%;
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
