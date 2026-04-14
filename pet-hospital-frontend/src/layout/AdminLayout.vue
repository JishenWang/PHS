<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🐾</span>
        <span class="logo-text" v-show="!isCollapse">宠物医院管理</span>
      </div>
      
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon>
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="admin-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/doctor">
          <el-icon><FirstAidKit /></el-icon>
          <template #title>医生管理</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/pet">
          <el-icon><Calendar /></el-icon>
          <template #title>宠物管理</template>
        </el-menu-item>
        
        <el-menu-item index="/admin/system">
          <el-icon><Setting /></el-icon>
          <template #title>系统配置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
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
          
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 标签页 -->
      <div class="tabs-view">
        <el-tabs v-model="activeTab" type="card" closable @tab-remove="removeTab">
          <el-tab-pane 
            v-for="item in tabs" 
            :key="item.path" 
            :label="item.title" 
            :name="item.path"
          />
        </el-tabs>
      </div>
      
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
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataLine, User, FirstAidKit, Calendar, Setting, Fold, Expand, FullScreen, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb/index.vue'

const route = useRoute()
const router = useRouter()

// 侧边栏折叠
const isCollapse = ref(false)
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 当前菜单
const activeMenu = computed(() => route.path)

// 标签页
const activeTab = ref(route.path)
const tabs = ref([
  { title: '数据看板', path: '/admin/dashboard' }
])

watch(() => route.path, (newPath) => {
  activeTab.value = newPath
  const exist = tabs.value.find(item => item.path === newPath)
  if (!exist) {
    tabs.value.push({ title: route.meta.title || '未命名', path: newPath })
  }
})

const removeTab = (targetPath) => {
  const index = tabs.value.findIndex(item => item.path === targetPath)
  if (index > -1) {
    tabs.value.splice(index, 1)
    if (activeTab.value === targetPath) {
      const nextTab = tabs.value[index] || tabs.value[index - 1]
      if (nextTab) {
        router.push(nextTab.path)
      }
    }
  }
}

// 全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background: var(--bg-color);
}

.sidebar {
  background: linear-gradient(180deg, #304156 0%, #263445 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  transition: width 0.3s;
  position: relative;
  z-index: 100;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
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

.collapse-btn {
  position: absolute;
  right: -12px;
  top: 80px;
  width: 24px;
  height: 24px;
  background: var(--primary-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  font-size: 12px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
  z-index: 101;
  transition: transform 0.3s;
}

.collapse-btn:hover {
  transform: scale(1.1);
}

.admin-menu {
  border-right: none;
  background: transparent;
  padding-top: 10px;
}

:deep(.admin-menu .el-menu-item) {
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 10px;
  border-radius: var(--radius-base);
  height: 48px;
  line-height: 48px;
  transition: all 0.3s;
}

:deep(.admin-menu .el-menu-item:hover) {
  color: #fff;
  background: rgba(64, 158, 255, 0.2);
}

:deep(.admin-menu .el-menu-item.is-active) {
  color: #fff;
  background: var(--primary-color);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

:deep(.admin-menu .el-icon) {
  color: inherit;
  font-size: 18px;
  margin-right: 12px;
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 64px;
  background: var(--bg-white);
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
  color: var(--text-regular);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-small);
  transition: all 0.3s;
}

.header-icon:hover {
  color: var(--primary-color);
  background: var(--primary-light);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-base);
  transition: all 0.3s;
}

.user-info:hover {
  background: var(--bg-color);
}

.username {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.tabs-view {
  background: var(--bg-white);
  padding: 8px 24px 0;
  border-bottom: 1px solid var(--border-lighter);
}

:deep(.tabs-view .el-tabs__header) {
  margin: 0;
}

:deep(.tabs-view .el-tabs__item) {
  border-radius: var(--radius-small) var(--radius-small) 0 0;
  margin-right: 4px;
  transition: all 0.3s;
}

:deep(.tabs-view .el-tabs__item.is-active) {
  background: var(--primary-light);
  border-bottom-color: var(--primary-light);
  color: var(--primary-color);
}

.main-content {
  padding: 20px;
  overflow-y: auto;
  background: var(--bg-color);
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