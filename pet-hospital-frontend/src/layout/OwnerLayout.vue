<template>
  <el-container class="owner-layout">
    <!-- 左侧侧边栏 -->
    <el-aside :width="isCollapse ? '80px' : '260px'" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            <path d="M4 12v5l8 4 8-4v-5"/>
          </svg>
        </div>
        <div v-show="!isCollapse" class="logo-text">
          <div class="title">宠物医院</div>
          <div class="subtitle">客户自助端</div>
        </div>
      </div>
      
      <div class="user-profile-mini">
        <el-avatar :size="48" :src="userStore.avatar || defaultAvatar" class="user-avatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <div v-show="!isCollapse" class="user-info">
          <div class="name">{{ displayName }}</div>
          <div class="role">宠物主人</div>
        </div>
      </div>
      
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon>
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        router
        class="owner-menu"
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <el-menu-item index="/owner/pet">
          <el-icon><FirstAidKit /></el-icon>
          <template #title>我的宠物</template>
        </el-menu-item>
        
        <el-menu-item index="/owner/health">
          <el-icon><Notebook /></el-icon>
          <template #title>健康记录</template>
        </el-menu-item>
        
        <el-menu-item index="/owner/reserve">
          <el-icon><Calendar /></el-icon>
          <template #title>预约申请</template>
        </el-menu-item>
        
        <el-menu-item index="/owner/consult">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>在线咨询</template>
        </el-menu-item>
        
        <el-menu-item index="/owner/orders">
          <el-icon><Tickets /></el-icon>
          <template #title>我的订单</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/owner/pet' }">
              <el-icon><HomeFilled /></el-icon>
              <span>客户自助端</span>
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
            
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
              <el-button circle @click="goToMessages">
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
                <div class="role">宠物主人</div>
              </div>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Tickets /></el-icon> 我的订单
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
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
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FirstAidKit, Notebook, Calendar, ChatDotRound, Tickets,
  ArrowDown, HomeFilled, Refresh, Bell, UserFilled,
  User, SwitchButton, Fold, Expand
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const isCollapse = ref(false)
const unreadCount = ref(2)
const refreshing = ref(false)
let refreshTimer = null

// 计算属性
const activeMenu = computed(() => route.path)
const displayName = computed(() => userStore.username || '宠物主人')

const breadcrumbList = computed(() => {
  return route.matched
    .filter(item => item.meta?.title && item.path !== '/owner')
    .map(item => ({ title: item.meta.title }))
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/owner/profile')
      break
    case 'orders':
      router.push('/owner/orders')
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
    router.push('/login')
  })
}

const goToMessages = () => {
  ElMessage.info('消息通知功能开发中')
}

const handleRefresh = async () => {
  refreshing.value = true
  setTimeout(() => {
    refreshing.value = false
    ElMessage.success('数据已刷新')
    window.location.reload()
  }, 500)
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

<style scoped lang="scss">
.owner-layout {
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
      background: #409eff;
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
    
    .user-profile-mini {
      padding: 20px;
      display: flex;
      align-items: center;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      
      .user-avatar {
        border: 2px solid rgba(255, 255, 255, 0.3);
        flex-shrink: 0;
      }
      
      .user-info {
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
        
        .role {
          font-size: 12px;
          color: rgba(255, 255, 255, 0.7);
          margin-top: 4px;
        }
      }
    }
    
    .owner-menu {
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
          
          &.is-active::before {
            display: none;
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

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(-15px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(15px);
}
</style>