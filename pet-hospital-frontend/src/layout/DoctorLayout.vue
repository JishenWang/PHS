<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '80px' : '240px'" class="layout-sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">🐾</div>
        <div v-show="!isCollapse" class="logo-text">
          <div class="logo-title">宠物医院</div>
          <div class="logo-sub">医生工作站</div>
        </div>
      </div>

      <!-- 医生信息卡片 -->
      <div v-show="!isCollapse" class="user-card">
        <el-avatar :size="44" :src="displayAvatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <div class="user-card-info">
          <div class="user-name">{{ displayName }}</div>
          <div class="user-dept">{{ userInfo.department || '全科医疗部' }}</div>
        </div>
        <div class="status-dot" :class="statusClass"></div>
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
          <template #title>
            <span>{{ menu.title }}</span>
            <el-badge
              v-if="menu.path === '/doctor/accept' && waitAcceptCount > 0"
              :value="waitAcceptCount"
              class="menu-badge"
              type="danger"
            />
          </template>
        </el-menu-item>
      </el-menu>

      <!-- 工作状态切换 -->
      <div v-show="!isCollapse" class="sidebar-footer">
        <div class="work-status">
          <span class="work-label">工作状态</span>
          <el-dropdown @command="handleStatusChange" trigger="click">
            <div class="status-btn" :class="statusClass">
              <el-icon v-if="doctorStatus === 1"><Sunny /></el-icon>
              <el-icon v-else-if="doctorStatus === 2"><Loading /></el-icon>
              <el-icon v-else><Moon /></el-icon>
              <span>{{ statusText }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="1">
                  <el-icon color="#059669"><Sunny /></el-icon> 空闲接诊
                </el-dropdown-item>
                <el-dropdown-item command="2">
                  <el-icon color="#f59e0b"><Loading /></el-icon> 接诊中
                </el-dropdown-item>
                <el-dropdown-item command="0">
                  <el-icon color="#94a3b8"><Moon /></el-icon> 休息中
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
              <el-button circle @click="handleManualRefresh" :loading="refreshing">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </el-tooltip>
            <el-button circle @click="goToConsult">
              <el-icon><Bell /></el-icon>
            </el-button>
          </div>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="displayAvatar">
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

      <el-main class="layout-main">
        <div class="content-wrapper">
          <router-view />
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
  Fold, Expand, ArrowDown, User, Setting, SwitchButton,
  UserFilled, Sunny, Loading, Moon, Refresh, Bell, Menu
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { doctorModule, acceptModule } from '@/api/doctor/doctor'

const router = useRouter()
const userStore = useUserStore()
const { isCollapse, toggleCollapse, sidebarMenus, activeMenu, breadcrumbList } = useLayout()

const doctorStatus = ref(1)
const waitAcceptCount = ref(0)
const refreshing = ref(false)
let refreshTimer = null

const userInfo = computed(() => userStore.userInfo || {})
const displayName = computed(() => userInfo.value.realName || userInfo.value.username || '医生')
const displayAvatar = computed(() => userInfo.value.avatar || '')

const statusText = computed(() => {
  const map = { 0: '休息中', 1: '空闲', 2: '接诊中' }
  return map[doctorStatus.value] || '未知'
})

const statusClass = computed(() => {
  const map = { 0: 'status-rest', 1: 'status-free', 2: 'status-busy' }
  return map[doctorStatus.value] || 'status-rest'
})

const handleStatusChange = async (status) => {
  try {
    await doctorModule.updateDoctorStatus({ status: parseInt(status) })
    doctorStatus.value = parseInt(status)
    ElMessage.success('工作状态已更新')
  } catch {
    ElMessage.error('状态更新失败')
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile': router.push('/doctor/profile'); break
    case 'settings': router.push('/doctor/settings'); break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        userStore.logoutAction()
        router.push('/login')
      })
      break
  }
}

const goToConsult = () => router.push('/doctor/consult')

const fetchWaitAcceptCount = async () => {
  try {
    const doctorId = userStore.userInfo?.id
    if (!doctorId) return
    const res = await acceptModule.getWaitAcceptCount(doctorId)
    if (res.code === 200) waitAcceptCount.value = res.data || 0
  } catch (e) { console.error(e) }
}

const handleManualRefresh = async () => {
  refreshing.value = true
  await fetchWaitAcceptCount()
  setTimeout(() => { refreshing.value = false; ElMessage.success('数据已刷新') }, 500)
}

onMounted(() => {
  fetchWaitAcceptCount()
  refreshTimer = setInterval(fetchWaitAcceptCount, 30000)
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

/* ===== 医生信息卡片 ===== */
.user-card {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
  position: relative;
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

.user-dept {
  font-size: 12px;
  color: #64748b;
  margin-top: 3px;
}

.status-dot {
  position: absolute;
  right: 20px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #fff;
}

.status-dot.status-free {
  background: #34d399;
  box-shadow: 0 0 8px #34d399;
}

.status-dot.status-busy {
  background: #fbbf24;
  box-shadow: 0 0 8px #fbbf24;
}

.status-dot.status-rest {
  background: #94a3b8;
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

.menu-badge {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
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

/* ===== 底部工作状态 ===== */
.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.work-label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.status-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #334155;
}

.status-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.status-btn.status-free {
  background: #f0fdf4;
  border: 1px solid #a7f3d0;
}

.status-btn.status-busy {
  background: #fffbeb;
  border: 1px solid #fde68a;
}

.status-btn.status-rest {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.arrow {
  margin-left: auto;
  font-size: 12px;
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
</style>
