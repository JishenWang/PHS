<template>
  <el-container class="doctor-layout">
    <!-- 左侧侧边栏 - 宽度动态调整 -->
    <el-aside :width="settingsStore.collapseMenu ? '80px' : '260px'" class="sidebar" :class="sidebarClass">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div v-show="!settingsStore.collapseMenu" class="logo-text">
          <div class="title">宠物医院</div>
          <div class="subtitle">医生工作站</div>
        </div>
      </div>
      
      <div class="doctor-profile-mini">
        <el-avatar :size="48" :src="displayAvatar" class="doctor-avatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <div v-show="!settingsStore.collapseMenu" class="doctor-info">
          <div class="name">{{ displayName }}</div>
          <div class="dept">{{ userInfo.department || '全科医疗部' }}</div>
        </div>
        <div v-show="!settingsStore.collapseMenu" class="status-dot" :class="statusClass"></div>
      </div>
      
      <!-- 菜单折叠通过 settingsStore.collapseMenu 控制 -->
      <el-menu
        :default-active="activeMenu"
        router
        class="doctor-menu"
        :collapse="settingsStore.collapseMenu"
        :collapse-transition="false"
      >
        <el-menu-item index="/doctor/accept">
          <el-icon><Calendar /></el-icon>
          <template #title>
            <span>接诊列表</span>
            <el-badge v-if="waitAcceptCount > 0" :value="waitAcceptCount" class="menu-badge" type="danger" />
          </template>
        </el-menu-item>
        
        <el-menu-item index="/doctor/pet">
          <el-icon><FirstAidKit /></el-icon>
          <template #title>
            <span>宠物档案</span>
          </template>
        </el-menu-item>
        
        <el-menu-item index="/doctor/record">
          <el-icon><Document /></el-icon>
          <template #title>
            <span>病历记录</span>
          </template>
        </el-menu-item>
        
        <el-menu-item index="/doctor/prescription">
          <el-icon><Tickets /></el-icon>
          <template #title>
            <span>处方开具</span>
          </template>
        </el-menu-item>
        
        <el-menu-item index="/doctor/consult">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>
            <span>在线咨询</span>
            <el-badge v-if="unreadConsultCount > 0" :value="unreadConsultCount" class="menu-badge" type="warning" />
          </template>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer">
        <div class="work-status">
          <span v-show="!settingsStore.collapseMenu" class="label">工作状态</span>
          <el-dropdown @command="handleStatusChange" trigger="click">
            <div class="status-btn" :class="statusClass">
              <el-icon v-if="doctorStatus === 1"><Sunny /></el-icon>
              <el-icon v-if="doctorStatus === 2"><Loading /></el-icon>
              <el-icon v-if="doctorStatus === 0"><Moon /></el-icon>
              <span v-show="!settingsStore.collapseMenu">{{ statusText }}</span>
              <el-icon v-show="!settingsStore.collapseMenu" class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="1">
                  <el-icon color="#67C23A"><Sunny /></el-icon> 空闲接诊
                </el-dropdown-item>
                <el-dropdown-item command="2">
                  <el-icon color="#E6A23C"><Loading /></el-icon> 接诊中
                </el-dropdown-item>
                <el-dropdown-item command="0">
                  <el-icon color="#909399"><Moon /></el-icon> 休息中
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-aside>

    <!-- 右侧主内容区 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 面包屑 - 通过 settingsStore.showBreadcrumb 控制 -->
          <el-breadcrumb v-if="settingsStore.showBreadcrumb" separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/doctor/accept' }">
              <el-icon><HomeFilled /></el-icon>
              <span>医生工作台</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item 
              v-for="(item, index) in breadcrumbList" 
              :key="index"
              :to="index < breadcrumbList.length - 1 ? item.path : undefined"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div v-else class="breadcrumb-simple">
            <span class="current-page">{{ currentPageTitle }}</span>
          </div>
        </div>
        
        <div class="header-right">
          <!-- 快捷操作 -->
          <div class="quick-actions">
            <!-- 侧边栏折叠切换按钮 -->
            <el-tooltip :content="settingsStore.collapseMenu ? '展开菜单' : '折叠菜单'" placement="bottom">
              <el-button circle @click="toggleCollapse">
                <el-icon>
                  <Fold v-if="!settingsStore.collapseMenu" />
                  <Expand v-else />
                </el-icon>
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="刷新数据" placement="bottom">
              <el-button circle @click="handleManualRefresh" :loading="refreshing">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </el-tooltip>
            
            <el-badge :value="unreadConsultCount" :hidden="unreadConsultCount === 0" class="message-badge">
              <el-button circle @click="goToConsult">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </div>

          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="36" :src="displayAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="user-meta">
                <div class="name">{{ displayName }}</div>
                <div class="role">{{ userInfo.title || '主治医师' }}</div>
              </div>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon> 系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view></router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useSettingsStore } from '@/store/settings'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, FirstAidKit, Document, Tickets, ChatDotRound,
  Sunny, Loading, Moon, ArrowDown, HomeFilled, Refresh, 
  Bell, UserFilled, User, Setting, SwitchButton, Fold, Expand
} from '@element-plus/icons-vue'
import { doctorModule, acceptModule, consultModule } from '@/api/doctor/doctor'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const settingsStore = useSettingsStore()

// 响应式数据 - 从 settingsStore 读取默认状态作为初始值
const doctorStatus = ref(settingsStore.defaultStatus || 1)
const waitAcceptCount = ref(0)
const unreadConsultCount = ref(0)
const refreshing = ref(false)
let refreshTimer = null

// 计算属性
const activeMenu = computed(() => route.path)
const userInfo = computed(() => userStore.userInfo || {})

// 当前页面标题
const currentPageTitle = computed(() => {
  return route.meta?.title || '医生工作台'
})

// 面包屑列表
const breadcrumbList = computed(() => {
  // 过滤掉没有 title 的路由，以及 Layout 本身（path 为 '/doctor' 的布局组件）
  const breadcrumbs = route.matched.filter(item => {
    return item.meta && item.meta.title && item.path !== '/doctor'
  })
  
  return breadcrumbs.map(item => {
    return {
      title: item.meta.title,
      path: item.path
    }
  })
})

// 根据设置显示真实姓名或用户名
const displayName = computed(() => {
  if (settingsStore.showRealName && userInfo.value.realName) {
    return userInfo.value.realName
  }
  return userInfo.value.username || '医生'
})

// 根据设置显示头像
const displayAvatar = computed(() => {
  return userInfo.value.avatar || ''
})

// 侧边栏样式 class
const sidebarClass = computed(() => {
  return settingsStore.sidebarStyle === 'light' ? 'sidebar-light' : 'sidebar-dark'
})

const statusText = computed(() => {
  const map = { 0: '休息中', 1: '空闲', 2: '接诊中' }
  return map[doctorStatus.value] || '未知'
})

const statusClass = computed(() => {
  const map = { 0: 'status-rest', 1: 'status-free', 2: 'status-busy' }
  return map[doctorStatus.value] || 'status-rest'
})

// ========== 定时器管理 ==========

// 清除旧定时器
const clearRefreshTimer = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 启动自动刷新定时器
const startRefreshTimer = () => {
  // 先清除旧的定时器
  clearRefreshTimer()
  
  // 获取刷新间隔（秒转毫秒）
  const interval = settingsStore.refreshInterval * 1000
  
  console.log(`启动自动刷新，间隔: ${settingsStore.refreshInterval}秒`)
  
  // 创建新的定时器
  refreshTimer = setInterval(() => {
    console.log('自动刷新触发')
    fetchWaitAcceptCount()
    fetchUnreadConsultCount()
  }, interval)
}

// ========== 方法 ==========
const toggleCollapse = () => {
  settingsStore.collapseMenu = !settingsStore.collapseMenu
}

const handleStatusChange = async (status) => {
  try {
    await doctorModule.updateDoctorStatus({ status: parseInt(status) })
    doctorStatus.value = parseInt(status)
    
    // 同步更新系统设置中的默认接诊状态
    settingsStore.defaultStatus = parseInt(status)
    
    ElMessage.success('工作状态已更新')
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/doctor/profile')
      break
    case 'settings':
      router.push('/doctor/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    userStore.logoutAction()
    router.push('/login')
    ElMessage.success('已安全退出系统')
  })
}

const goToConsult = () => {
  router.push('/doctor/consult')
}

// 获取待接诊数量
const fetchWaitAcceptCount = async () => {
  try {
    const res = await acceptModule.getWaitAcceptCount()
    waitAcceptCount.value = res.data || 0
  } catch (error) {
    console.error('获取待接诊数量失败', error)
  }
}

// 获取未读咨询数量
const fetchUnreadConsultCount = async () => {
  try {
    const res = await consultModule.getUnreadConsultCount()
    unreadConsultCount.value = res.data || 0
  } catch (error) {
    console.error('获取未读咨询数量失败', error)
  }
}

// 手动刷新数据
const handleManualRefresh = async () => {
  refreshing.value = true
  await Promise.all([fetchWaitAcceptCount(), fetchUnreadConsultCount()])
  setTimeout(() => {
    refreshing.value = false
    ElMessage.success('数据已刷新')
  }, 500)
}

// 应用默认工作状态（首次加载时）
const applyDefaultStatus = async () => {
  const defaultStatusValue = settingsStore.defaultStatus
  doctorStatus.value = defaultStatusValue
  try {
    await doctorModule.updateDoctorStatus({ status: defaultStatusValue })
    console.log('默认工作状态已应用:', defaultStatusValue)
  } catch (error) {
    console.error('应用默认工作状态失败', error)
  }
}

// ========== 监听器 ==========

// 监听系统设置中的 defaultStatus 变化，实时同步到侧边栏
watch(() => settingsStore.defaultStatus, async (newStatus) => {
  // 只有当状态真的改变时才更新
  if (doctorStatus.value !== newStatus) {
    doctorStatus.value = newStatus
    // 同步到后端
    try {
      await doctorModule.updateDoctorStatus({ status: newStatus })
      console.log('工作状态已同步:', newStatus)
    } catch (error) {
      console.error('同步工作状态失败', error)
    }
  }
})

// 监听刷新间隔变化 - 重启定时器
watch(() => settingsStore.refreshInterval, (newInterval, oldInterval) => {
  console.log(`刷新间隔变化: ${oldInterval}秒 -> ${newInterval}秒`)
  startRefreshTimer()
})

// ========== 生命周期 ==========

onMounted(() => {
  // 初始化设置
  settingsStore.initSettings()
  settingsStore.applyThemeColor(settingsStore.themeColor)
  
  // 应用默认工作状态
  applyDefaultStatus()

  // 首次获取数据
  fetchWaitAcceptCount()
  fetchUnreadConsultCount()
  
  // 启动自动刷新定时器
  startRefreshTimer()
  
  console.log('医生端布局已加载，自动刷新已启动')
})

onUnmounted(() => {
  clearRefreshTimer()
  console.log('医生端布局已卸载，自动刷新已停止')
})
</script>

<style scoped lang="scss">
.doctor-layout {
  height: 100vh;
  background: #f0f4f8;
  
  .sidebar {
    display: flex;
    flex-direction: column;
    box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
    transition: width 0.3s ease, background 0.3s ease;
    overflow: hidden;
    
    // 深色样式（默认）
    &.sidebar-dark {
      background: linear-gradient(180deg, #1a365d 0%, #2c5282 100%);
      
      .logo {
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        
        .logo-text {
          color: white;
          
          .title { color: white; }
          .subtitle { color: rgba(255, 255, 255, 0.7); }
        }
        
        .logo-icon {
          background: rgba(255, 255, 255, 0.15);
          
          svg { color: white; }
        }
      }
      
      .doctor-profile-mini {
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        
        .doctor-info {
          color: white;
          
          .name { color: white; }
          .dept { color: rgba(255, 255, 255, 0.7); }
        }
        
        .doctor-avatar {
          border-color: rgba(255, 255, 255, 0.3);
          background: rgba(255, 255, 255, 0.2);
        }
        
        .status-dot {
          border-color: #1a365d;
        }
      }
      
      .doctor-menu {
        :deep(.el-menu-item) {
          color: rgba(255, 255, 255, 0.8);
          
          &:hover {
            background: rgba(255, 255, 255, 0.1);
            color: white;
          }
          
          &.is-active {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            
            &::before {
              content: '';
              position: absolute;
              left: 0;
              top: 50%;
              transform: translateY(-50%);
              width: 4px;
              height: 20px;
              background: #63b3ed;
              border-radius: 0 2px 2px 0;
            }
          }
        }
        
        // 折叠模式特殊处理
        &.el-menu--collapse {
          :deep(.el-menu-item) {
            &.is-active::before {
              display: none;
            }
          }
        }
      }
      
      .sidebar-footer {
        border-top: 1px solid rgba(255, 255, 255, 0.1);
        
        .work-status {
          .label { color: rgba(255, 255, 255, 0.6); }
          
          .status-btn {
            color: white;
            
            &.status-free {
              background: rgba(103, 194, 58, 0.2);
              border: 1px solid rgba(103, 194, 58, 0.4);
            }
            
            &.status-busy {
              background: rgba(230, 162, 60, 0.2);
              border: 1px solid rgba(230, 162, 60, 0.4);
            }
            
            &.status-rest {
              background: rgba(144, 147, 153, 0.2);
              border: 1px solid rgba(144, 147, 153, 0.4);
            }
          }
        }
      }
    }
    
    // 浅色样式
    &.sidebar-light {
      background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
      border-right: 1px solid #e2e8f0;
      
      .logo {
        border-bottom: 1px solid #e2e8f0;
        
        .logo-text {
          .title { color: #1e293b; }
          .subtitle { color: #64748b; }
        }
        
        .logo-icon {
          background: #e0f2fe;
          
          svg { color: #3b82f6; }
        }
      }
      
      .doctor-profile-mini {
        border-bottom: 1px solid #e2e8f0;
        
        .doctor-info {
          .name { color: #334155; }
          .dept { color: #64748b; }
        }
        
        .doctor-avatar {
          border-color: #e2e8f0;
          background: #f1f5f9;
        }
        
        .status-dot {
          border-color: #f8fafc;
        }
      }
      
      .doctor-menu {
        :deep(.el-menu-item) {
          color: #64748b;
          
          &:hover {
            background: #f1f5f9;
            color: #334155;
          }
          
          &.is-active {
            background: #e0f2fe;
            color: #3b82f6;
            
            &::before {
              content: '';
              position: absolute;
              left: 0;
              top: 50%;
              transform: translateY(-50%);
              width: 4px;
              height: 20px;
              background: #3b82f6;
              border-radius: 0 2px 2px 0;
            }
          }
        }
        
        // 折叠模式特殊处理
        &.el-menu--collapse {
          :deep(.el-menu-item) {
            &.is-active::before {
              display: none;
            }
          }
        }
      }
      
      .sidebar-footer {
        border-top: 1px solid #e2e8f0;
        
        .work-status {
          .label { color: #64748b; }
          
          .status-btn {
            &.status-free {
              background: #dcfce7;
              border: 1px solid #86efac;
              color: #166534;
            }
            
            &.status-busy {
              background: #fef3c7;
              border: 1px solid #fde68a;
              color: #92400e;
            }
            
            &.status-rest {
              background: #f3f4f6;
              border: 1px solid #d1d5db;
              color: #4b5563;
            }
          }
        }
      }
    }
    
    .logo {
      height: 80px;
      display: flex;
      align-items: center;
      padding: 0 20px;
      transition: padding 0.3s;
      
      .logo-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        flex-shrink: 0;
        
        svg {
          width: 24px;
          height: 24px;
        }
      }
      
      .logo-text {
        .title {
          font-size: 18px;
          font-weight: 600;
          letter-spacing: 1px;
          white-space: nowrap;
        }
        
        .subtitle {
          font-size: 12px;
          margin-top: 2px;
          white-space: nowrap;
        }
      }
    }
    
    .doctor-profile-mini {
      padding: 20px;
      display: flex;
      align-items: center;
      position: relative;
      
      .doctor-avatar {
        border: 2px solid;
        flex-shrink: 0;
      }
      
      .doctor-info {
        margin-left: 12px;
        overflow: hidden;
        
        .name {
          font-size: 16px;
          font-weight: 500;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .dept {
          font-size: 12px;
          margin-top: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
      
      .status-dot {
        position: absolute;
        right: 20px;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        border: 2px solid;
        
        &.status-free {
          background: #67C23A;
          box-shadow: 0 0 8px #67C23A;
        }
        
        &.status-busy {
          background: #E6A23C;
          box-shadow: 0 0 8px #E6A23C;
          animation: pulse 2s infinite;
        }
        
        &.status-rest {
          background: #909399;
        }
      }
    }
    
    .doctor-menu {
      flex: 1;
      background: transparent;
      border-right: none;
      padding: 10px;
      
      :deep(.el-menu-item) {
        height: 50px;
        line-height: 50px;
        margin: 8px 0;
        border-radius: 10px;
        transition: all 0.3s;
        
        .el-icon {
          font-size: 20px;
        }
        
        .menu-badge {
          position: absolute;
          right: 15px;
          top: 50%;
          transform: translateY(-50%);
        }
      }
      
      // 折叠模式
      &.el-menu--collapse {
        padding: 10px 5px;
        
        :deep(.el-menu-item) {
          padding: 0 !important;
          display: flex;
          align-items: center;
          justify-content: center;
          
          .el-icon {
            margin-right: 0 !important;
            font-size: 22px;
          }
          
          span {
            display: none;
          }
          
          .menu-badge {
            right: 2px;
            top: 2px;
            transform: none;
            
            :deep(.el-badge__content) {
              font-size: 10px;
              height: 16px;
              line-height: 16px;
              padding: 0 4px;
            }
          }
        }
      }
    }
    
    .sidebar-footer {
      padding: 20px;
      
      .work-status {
        .label {
          display: block;
          font-size: 12px;
          margin-bottom: 8px;
        }
        
        .status-btn {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 10px 15px;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.3s;
          white-space: nowrap;
          
          .arrow {
            margin-left: auto;
            font-size: 12px;
          }
          
          &:hover {
            opacity: 0.9;
            transform: translateY(-1px);
          }
        }
      }
    }
  }
  
  .header {
    height: 70px;
    background: white;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    z-index: 10;
    
    .header-left {
      .breadcrumb {
        display: flex;
        align-items: center;
        
        :deep(.el-breadcrumb__item) {
          display: flex;
          align-items: center;
          
          .el-breadcrumb__inner {
            display: flex;
            align-items: center;
            gap: 6px;
            color: #4a5568;
            font-weight: 400;
            
            .el-icon {
              font-size: 16px;
            }
            
            &:hover {
              color: #3b82f6;
            }
          }
          
          &:last-child {
            .el-breadcrumb__inner {
              color: #1e293b;
              font-weight: 600;
              
              &:hover {
                color: #1e293b;
                cursor: default;
              }
            }
          }
        }
        
        :deep(.el-breadcrumb__separator) {
          color: #cbd5e1;
          margin: 0 8px;
        }
      }
      
      .breadcrumb-simple {
        .current-page {
          font-size: 18px;
          font-weight: 600;
          color: #2d3748;
        }
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .quick-actions {
        display: flex;
        gap: 10px;
        
        .el-button {
          width: 40px;
          height: 40px;
          border: none;
          background: #f7fafc;
          color: #4a5568;
          
          &:hover {
            background: #e2e8f0;
            color: #3182ce;
          }
        }
        
        .message-badge {
          :deep(.el-badge__content) {
            border: none;
          }
        }
      }
      
      .user-dropdown {
        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 8px 15px;
          border-radius: 30px;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            background: #f7fafc;
          }
          
          .user-meta {
            .name {
              font-size: 14px;
              font-weight: 500;
              color: #2d3748;
            }
            
            .role {
              font-size: 12px;
              color: #718096;
            }
          }
          
          .arrow {
            color: #a0aec0;
            font-size: 12px;
          }
        }
      }
    }
  }
  
  .main-content {
    padding: 0;
    overflow-y: auto;
    background: #f0f4f8;
    
    .content-wrapper {
      padding: 25px;
      min-height: 100%;
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}
</style>