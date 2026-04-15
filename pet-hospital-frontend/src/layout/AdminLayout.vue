<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🐾</span>
        <span class="logo-text">宠物医院管理</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        class="admin-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/doctor">
          <el-icon><FirstAidKit /></el-icon>
          <span>医生管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/pet">
          <el-icon><Tickets /></el-icon>
          <span>宠物管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/system">
          <el-icon><Setting /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <breadcrumb />
        </div>
        <div class="header-right">
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" @click="toggleFullscreen"><FullScreen /></el-icon>
          </el-tooltip>
          
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatar || defaultAvatar" />
              <span class="username">{{ userStore.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区域 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataLine, User, FirstAidKit, Tickets, Setting,
  FullScreen, ArrowDown, SwitchButton
} from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb/index.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/admin/profile')
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
  }).catch(() => {})
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background: #f0f2f5;
}

.sidebar {
  background: #304156;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b3649;
  border-bottom: 1px solid #1f2d3d;
}

.logo-icon {
  font-size: 28px;
  margin-right: 8px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.admin-menu {
  border-right: none;
  padding-top: 10px;
}

:deep(.admin-menu .el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 10px;
  border-radius: 4px;
}

:deep(.admin-menu .el-menu-item:hover) {
  background: #263445;
}

:deep(.admin-menu .el-menu-item.is-active) {
  background: #263445;
}

:deep(.admin-menu .el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 64px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  font-size: 20px;
  color: #606266;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.header-icon:hover {
  color: #409EFF;
  background: #f5f7fa;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-content {
  padding: 20px;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>