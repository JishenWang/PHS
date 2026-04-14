<template>
  <el-container class="doctor-layout">
    <!-- 左侧侧边栏 -->
    <el-aside :width="isCollapse ? '80px' : '260px'" class="sidebar" :class="sidebarClass">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div v-show="!isCollapse" class="logo-text">
          <div class="title">宠物医院</div>
          <div class="subtitle">医生工作站</div>
        </div>
      </div>
      
      <!-- 医生信息卡片 -->
      <div class="doctor-profile-mini">
        <el-avatar :size="48" :src="userStore.avatar || defaultAvatar" class="doctor-avatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <div v-show="!isCollapse" class="doctor-info">
          <div class="name">{{ displayName }}</div>
          <div class="dept">{{ userInfo.department || '全科医疗部' }}</div>
        </div>
        <div v-show="!isCollapse" class="status-dot" :class="statusClass"></div>
      </div>
      
      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon>
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
      
      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        router
        class="doctor-menu"
        :collapse="isCollapse"
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
          <template #title>宠物档案</template>
        </el-menu-item>
        
        <el-menu-item index="/doctor/record">
          <el-icon><Document /></el-icon>
          <template #title>病历记录</template>
        </el-menu-item>
        
        <el-menu-item index="/doctor/prescription">
          <el-icon><Tickets /></el-icon>
          <template #title>处方开具</template>
        </el-menu-item>
        
        <el-menu-item index="/doctor/consult">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>
            <span>在线咨询</span>
            <el-badge v-if="unreadConsultCount > 0" :value="unreadConsultCount" class="menu-badge" type="warning" />
          </template>
        </el-menu-item>
      </el-menu>
      
      <!-- 工作状态 -->
      <div class="sidebar-footer">
        <div class="work-status">
          <span v-show="!isCollapse" class="label">工作状态</span>
          <el-dropdown @command="handleStatusChange" trigger="click">
            <div class="status-btn" :class="statusClass">
              <el-icon v-if="doctorStatus === 1"><Sunny /></el-icon>
              <el-icon v-if="doctorStatus === 2"><Loading /></el-icon>
              <el-icon v-if="doctorStatus === 0"><Moon /></el-icon>
              <span v-show="!isCollapse">{{ statusText }}</span>
              <el-icon v-show="!isCollapse" class="arrow"><ArrowDown /></el-icon>
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
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/doctor/accept' }">
              <el-icon><HomeFilled /></el-icon>
              <span>医生工作台</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <div class="quick-actions">
            <el-tooltip content="刷新数据" placement="bottom">
              <el-button circle @click="handleRefresh" :loading="refreshing">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </el-tooltip>
            
            <el-badge :value="unreadConsultCount" :hidden="unreadConsultCount === 0" class="message-badge">
              <el-button circle @click="goToConsult">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </div>

          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.avatar || defaultAvatar">
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, FirstAidKit, Document, Tickets, ChatDotRound,
  Sunny, Loading, Moon, ArrowDown, HomeFilled, Refresh, 
  Bell, UserFilled, User, Setting, SwitchButton, Fold, Expand
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 状态
const isCollapse = ref(false)
const doctorStatus = ref(1) // 0:休息 1:空闲 2:接诊中
const waitAcceptCount = ref(0)
const unreadConsultCount = ref(0)
const refreshing = ref(false)
let refreshTimer = null

// 计算属性
const activeMenu = computed(() => route.path)
const userInfo = computed(() => userStore.userInfo || {})
const displayName = computed(() => userStore.username || '医生')

const breadcrumbList = computed(() => {
  return route.matched
    .filter(item => item.meta?.title && item.path !== '/doctor')
    .map(item => ({ title: item.meta.title }))
})

const sidebarClass = computed(() => 'sidebar-dark')

const statusText = computed(() => {
  const map = { 0: '休息中', 1: '空闲', 2: '接诊中' }
  return map[doctorStatus.value] || '未知'
})

const statusClass = computed(() => {
  const map = { 0: 'status-rest', 1: 'status-free', 2: 'status-busy' }
  return map[doctorStatus.value] || 'status-rest'
})

// 方法
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleStatusChange = async (status) => {
  doctorStatus.value = parseInt(status)
  ElMessage.success('工作状态已更新')
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
    userStore.logout()
  })
}

const goToConsult = () => {
  router.push('/doctor/consult')
}

const handleRefresh = async () => {
  refreshing.value = true
  setTimeout(() => {
    refreshing.value = false
    ElMessage.success('数据已刷新')
  }, 500)
}

// 生命周期
onMounted(() => {
  refreshTimer = setInterval(() => {
    // 模拟获取待接诊和未读咨询数量
    waitAcceptCount.value = Math.floor(Math.random() * 5)
    unreadConsultCount.value = Math.floor(Math.random() * 3)
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped lang="scss">
.doctor-layout {
  height: 100vh;
  background: #f0f4f8;
  
  .sidebar {
    display: flex;
    flex-direction: column;
    background: linear-gradient(180deg, #1a365d 0%, #2c5282 100%);
    box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
    transition: width 0.3s ease;
    overflow: hidden;
    position: relative;
    
    .logo {
      height: 80px;
      display: flex;
      align-items: center;
      padding: 0 20px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      
      .logo-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        background: rgba(255, 255, 255, 0.15);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        flex-shrink: 0;
        
        svg {
          width: 24px;
          height: 24px;
          color: white;
        }
      }
      
      .logo-text {
        .title {
          font-size: 18px;
          font-weight: 600;
          color: white;
          letter-spacing: 1px;
        }
        
        .subtitle {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.7);
          margin-top: 2px;
        }
      }
    }
    
    .collapse-btn {
      position: absolute;
      right: -12px;
      top: 90px;
      width: 24px;
      height: 24px;
      background: #409EFF;
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
      
      &:hover {
        transform: scale(1.1);
      }
    }
    
    .doctor-profile-mini {
      padding: 20px;
      display: flex;
      align-items: center;
      position: relative;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      
      .doctor-avatar {
        border: 2px solid rgba(255, 255, 255, 0.3);
        flex-shrink: 0;
      }
      
      .doctor-info {
        margin-left: 12px;
        overflow: hidden;
        
        .name {
          font-size: 16px;
          font-weight: 500;
          color: white;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .dept {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.7);
          margin-top: 4px;
        }
      }
      
      .status-dot {
        position: absolute;
        right: 20px;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        border: 2px solid #1a365d;
        
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
        color: rgba(255, 255, 255, 0.8);
        height: 50px;
        line-height: 50px;
        margin: 8px 0;
        border-radius: 10px;
        transition: all 0.3s;
        
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
          }
          
          &.is-active::before {
            display: none;
          }
        }
      }
    }
    
    .sidebar-footer {
      padding: 20px;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      
      .work-status {
        .label {
          display: block;
          font-size: 12px;
          color: rgba(255, 255, 255, 0.6);
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
          color: white;
          white-space: nowrap;
          
          .arrow {
            margin-left: auto;
            font-size: 12px;
          }
          
          &:hover {
            opacity: 0.9;
            transform: translateY(-1px);
          }
          
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
        :deep(.el-breadcrumb__item) {
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