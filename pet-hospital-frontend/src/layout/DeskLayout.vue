<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '80px' : '240px'" class="layout-sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">🐾</div>
        <div v-show="!isCollapse" class="logo-text">
          <div class="logo-title">宠物医院</div>
          <div class="logo-sub">前台收银端</div>
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

      <!-- 医生状态面板 -->
      <div v-show="!isCollapse" class="sidebar-footer">
        <div class="footer-title">医生接诊状态</div>
        <div v-for="d in doctors" :key="d.id" class="doctor-item">
          <span class="doctor-name">{{ d.name }}</span>
          <el-tag size="small" :type="doctorTagType(d.status)">
            {{ doctorStatusLabel(d.status) }}
          </el-tag>
        </div>
      </div>
    </el-aside>

    <!-- 主区域 -->
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <div class="header-title-group">
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-sub">
              今日运营：挂号 {{ stats.registerCount }} / 接诊 {{ stats.doneCount }} / 收费 ¥{{ stats.chargeTotal?.toFixed(2) || '0.00' }}
            </div>
          </div>
        </div>

        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="!unreadCount">
            <el-button text @click="messageVisible = true">
              <el-icon><Bell /></el-icon> 消息
            </el-button>
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatar || defaultAvatar" />
              <span class="username">{{ userStore.username || '前台' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
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
        <router-view />
      </el-main>
    </el-container>

    <!-- 消息抽屉 -->
    <el-drawer v-model="messageVisible" title="消息提醒" size="420px">
      <el-empty v-if="messages.length === 0" description="暂无消息" />
      <div
        v-for="m in messages"
        :key="m.id"
        class="message-item"
        :class="{ unread: !m.read }"
      >
        <div class="message-text">{{ m.content }}</div>
        <div class="message-meta">
          <span>{{ formatTime(m.createdAt) }}</span>
          <el-button v-if="!m.read" type="primary" link @click="markRead(m.id)">
            标记已读
          </el-button>
        </div>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useLayout } from '@/composables/useLayout'
import {
  Fold, Expand, ArrowDown, User, Lock, SwitchButton,
  Bell, UserFilled, Menu
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isCollapse, toggleCollapse, sidebarMenus, activeMenu } = useLayout()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const pageTitle = computed(() => route.meta?.title || '前台工作台')

// 运营数据
const stats = ref({ registerCount: 0, doneCount: 0, chargeTotal: 0 })

// 医生状态（模拟）
const doctors = ref([
  { id: 1, name: '张医生', status: 'FREE' },
  { id: 2, name: '李医生', status: 'BUSY' },
  { id: 3, name: '王医生', status: 'REST' }
])

const doctorStatusMap = {
  FREE: { label: '空闲', type: 'success' },
  BUSY: { label: '接诊中', type: 'warning' },
  REST: { label: '休息', type: 'info' }
}
const doctorStatusLabel = (s) => doctorStatusMap[s]?.label || '未知'
const doctorTagType = (s) => doctorStatusMap[s]?.type || 'info'

// 消息
const messages = ref([])
const messageVisible = ref(false)
const unreadCount = computed(() => messages.value.filter(x => !x.read).length)

const formatTime = (v) => {
  if (!v) return '-'
  const d = new Date(v)
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleString()
}

const markRead = (id) => {
  const msg = messages.value.find(m => m.id === id)
  if (msg) msg.read = true
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile': ElMessage.success('个人中心'); break
    case 'password': ElMessage.success('修改密码'); break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(() => userStore.logout())
      break
  }
}

let dashboardTimer = null
onMounted(() => {
  dashboardTimer = setInterval(() => {}, 30000)
})
onUnmounted(() => {
  if (dashboardTimer) clearInterval(dashboardTimer)
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

/* ===== 底部医生状态面板 ===== */
.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.footer-title {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 12px;
}

.doctor-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0;
  font-size: 13px;
}

.doctor-name {
  color: #334155;
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

.header-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.page-sub {
  font-size: 12px;
  color: #64748b;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
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

/* ===== 消息抽屉 ===== */
.message-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.message-item.unread {
  border-color: #34d399;
  background: #f0fdf4;
}

.message-text {
  color: #1e293b;
  line-height: 1.5;
  font-size: 14px;
}

.message-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
