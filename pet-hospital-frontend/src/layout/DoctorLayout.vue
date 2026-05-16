<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '80px' : '240px'" class="layout-sidebar" :class="sidebarClass">
      <div class="sidebar-logo">
        <div class="logo-icon">🐾</div>
        <div v-show="!isCollapse" class="logo-text">
          <div class="logo-title">{{ $t('layout.hospitalName') }}</div>
          <div class="logo-sub">{{ $t('layout.doctorSub') }}</div>
        </div>
      </div>

      <!-- 医生信息卡片 -->
      <div v-show="!isCollapse" class="user-card">
        <el-avatar :size="44" :src="displayAvatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <div class="user-card-info">
          <div class="user-name">{{ displayName }}</div>
          <div class="user-dept">{{ displayDepartment }}</div>
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
          v-show="menu.path !== '/doctor/consult' || settingsStore.consultVisible"
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
          <span class="work-label">{{ $t('layout.workStatus') }}</span>
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
                  <el-icon color="#059669"><Sunny /></el-icon> {{ t('layout.statusFree') }}
                </el-dropdown-item>
                <el-dropdown-item command="2">
                  <el-icon color="#f59e0b"><Loading /></el-icon> {{ t('layout.statusBusy') }}
                </el-dropdown-item>
                <el-dropdown-item command="0">
                  <el-icon color="#94a3b8"><Moon /></el-icon> {{ t('layout.statusRest') }}
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
          <div class="quick-actions">
            <el-tooltip content="Refresh" placement="bottom">
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

      <el-main class="layout-main">
        <div class="content-wrapper">
          <router-view :key="refreshKey" />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user'
import { useLayout } from '@/composables/useLayout'
import { useSettingsStore } from '@/store/settings'
import {
  Fold, Expand, ArrowDown, User, Setting, SwitchButton,
  UserFilled, Sunny, Loading, Moon, Refresh, Bell, Menu
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { doctorModule, acceptModule } from '@/api/doctor/doctor'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const layout = useLayout()
const { sidebarMenus, activeMenu, breadcrumbList } = layout

// 从 settingsStore 读取折叠状态
const settingsStore = useSettingsStore()
const isCollapse = ref(settingsStore.collapseMenu)

// 同步折叠状态到 store
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
  settingsStore.collapseMenu = isCollapse.value
}

// 监听 store 变化（恢复默认等操作）
watch(() => settingsStore.collapseMenu, (val) => {
  isCollapse.value = val
})

// 侧边栏样式 class
const sidebarClass = computed(() => {
  return settingsStore.sidebarStyle === 'dark' ? 'sidebar-dark' : 'sidebar-light'
})

// 是否显示面包屑
const showBreadcrumb = computed(() => settingsStore.showBreadcrumb)

const doctorStatus = ref(1)
const waitAcceptCount = ref(0)
const refreshing = ref(false)
const refreshKey = ref(0)
let refreshTimer = null
let timeoutCheckTimer = null
let notifiedTimeoutIds = new Set() // 避免重复通知

const userInfo = computed(() => userStore.userInfo || {})
const displayName = computed(() => userInfo.value.realName || userInfo.value.username || 'Doctor')
const displayAvatar = computed(() => userInfo.value.avatar || '')

const departmentMap = {
  'General Practice': 'profile.generalPractice',
  'General Medicine': 'profile.generalPractice',
  'Internal Medicine': 'profile.internalMedicine',
  'Surgery': 'profile.surgery',
  'Dentistry': 'profile.dentistry',
  'Dermatology': 'profile.dermatology',
  'Ophthalmology': 'profile.ophthalmology',
  'Imaging': 'profile.imaging',
  '全科医疗部': 'profile.generalPractice',
  '内科': 'profile.internalMedicine',
  '外科': 'profile.surgery',
  '牙科': 'profile.dentistry',
  '皮肤科': 'profile.dermatology',
  '眼科': 'profile.ophthalmology',
  '影像科': 'profile.imaging',
}
const displayDepartment = computed(() => {
  const dept = userInfo.value.department
  if (!dept) return t('profile.generalPractice')
  const key = departmentMap[dept]
  return key ? t(key) : dept
})

const statusText = computed(() => {
  const map = { 0: t('layout.statusRest'), 1: t('layout.statusFree'), 2: t('layout.statusBusy') }
  return map[doctorStatus.value] || t('layout.statusUnknown')
})

const statusClass = computed(() => {
  const map = { 0: 'status-rest', 1: 'status-free', 2: 'status-busy' }
  return map[doctorStatus.value] || 'status-rest'
})

// 监听 settingsStore 中的 defaultStatus，实现右侧设置页面保存后左侧实时同步
watch(() => settingsStore.defaultStatus, (newVal) => {
  if (newVal !== doctorStatus.value) {
    doctorStatus.value = newVal
  }
})

const handleStatusChange = async (status) => {
  try {
    await doctorModule.updateDoctorStatus({ status: parseInt(status) })
    doctorStatus.value = parseInt(status)
    // 同步到设置中的默认接诊状态
    settingsStore.defaultStatus = parseInt(status)
    ElMessage.success('Status updated')
  } catch {
    ElMessage.error('Status update failed')
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile': router.push('/doctor/profile'); break
    case 'settings': router.push('/doctor/settings'); break
    case 'logout':
      ElMessageBox.confirm(t('layout.logoutConfirm'), 'Confirm', {
        confirmButtonText: t('layout.confirm'), cancelButtonText: t('layout.cancel'), type: 'warning'
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

// 检查接诊超时
const checkAcceptTimeout = async () => {
  if (!settingsStore.shouldNotify('timeout')) return
  try {
    const doctorId = userStore.userInfo?.id
    if (!doctorId) return
    const res = await acceptModule.getWaitAcceptList({
      pageNum: 1,
      pageSize: 100,
      status: 0
    })
    if (res.code === 200 && res.data) {
      const list = res.data.data || res.data.list || res.data.records || []
      const now = new Date().getTime()
      const THIRTY_MIN = 30 * 60 * 1000
      list.forEach(item => {
        const registerId = item.registerId || item.id
        if (notifiedTimeoutIds.has(registerId)) return
        const registerTime = item.registerTime || item.createTime
        if (registerTime) {
          const t = new Date(registerTime).getTime()
          if (now - t > THIRTY_MIN) {
            notifiedTimeoutIds.add(registerId)
            settingsStore.sendDesktopNotification(
              'Accept timeout',
              `Pet ${item.petName || 'Unknown'} has waited over 30 min`
            )
            settingsStore.playNotificationSound()
          }
        }
      })
    }
  } catch (e) { console.error('Check timeout failed', e) }
}

const handleManualRefresh = async () => {
  refreshing.value = true
  await fetchWaitAcceptCount()
  refreshKey.value++ // 强制刷新当前页面内容
  setTimeout(() => { refreshing.value = false; ElMessage.success('Refreshed') }, 500)
}

// 加载医生当前工作状态
const loadDoctorStatus = async () => {
  try {
    const res = await doctorModule.getDoctorInfo()
    if (res.code === 200 && res.data) {
      const status = res.data.status
      // 只有当 status 是有效值 (0/1/2) 时才使用，否则用设置中的默认值
      if (status === 0 || status === 1 || status === 2) {
        doctorStatus.value = status
        settingsStore.defaultStatus = status
      } else {
        doctorStatus.value = settingsStore.defaultStatus
      }
    } else {
      doctorStatus.value = settingsStore.defaultStatus
    }
  } catch (error) {
    console.error('Load doctor status failed', error)
    doctorStatus.value = settingsStore.defaultStatus
  }
}

onMounted(() => {
  loadDoctorStatus()
  fetchWaitAcceptCount()
  refreshTimer = setInterval(fetchWaitAcceptCount, 30000)
  // 启动接诊超时检查（每60秒检查一次）
  checkAcceptTimeout()
  timeoutCheckTimer = setInterval(checkAcceptTimeout, 60000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (timeoutCheckTimer) clearInterval(timeoutCheckTimer)
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
  background: var(--el-color-primary-light-9, #f0fdf4);
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
  background: var(--el-color-primary-light-9, #f8fafc);
  color: var(--el-color-primary, #059669);
}

.layout-menu :deep(.el-menu-item.is-active) {
  background: var(--el-color-primary-light-9, #f0fdf4);
  color: var(--el-color-primary, #059669);
}

.layout-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: var(--el-color-primary, #059669);
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
  color: var(--el-color-primary, #059669);
  background: var(--el-color-primary-light-9, #f0fdf4);
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
  background: var(--el-color-primary-light-9, #f0fdf4);
  color: var(--el-color-primary, #059669);
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
  background: var(--el-color-primary-light-9, #f0fdf4);
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

.sidebar-dark .user-card {
  border-bottom-color: #334155;
}

.sidebar-dark .user-name {
  color: #f1f5f9;
}

.sidebar-dark .user-dept {
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

.sidebar-dark .sidebar-footer {
  border-top-color: #334155;
}

.sidebar-dark .work-label {
  color: #94a3b8;
}

.sidebar-dark .status-btn {
  color: #cbd5e1;
}

.sidebar-dark .status-btn.status-free {
  background: rgba(16, 185, 129, 0.15);
  border-color: rgba(16, 185, 129, 0.3);
}

.sidebar-dark .status-btn.status-busy {
  background: rgba(245, 158, 11, 0.15);
  border-color: rgba(245, 158, 11, 0.3);
}

.sidebar-dark .status-btn.status-rest {
  background: rgba(148, 163, 184, 0.15);
  border-color: rgba(148, 163, 184, 0.3);
}
</style>
