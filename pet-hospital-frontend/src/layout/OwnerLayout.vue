<template>
  <el-container class="owner-layout">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">🐾</span>
          <span>宠物医院客户中心</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          router
          mode="horizontal"
          background-color="#fff"
          text-color="#333"
          active-text-color="#409EFF"
          class="nav-menu"
        >
          <el-menu-item index="/owner/pet">
            <el-icon><Tickets /></el-icon>
            我的宠物
          </el-menu-item>
          <el-menu-item index="/owner/health">
            <el-icon><FirstAidKit /></el-icon>
            健康记录
          </el-menu-item>
          <el-menu-item index="/owner/reserve">
            <el-icon><Calendar /></el-icon>
            预约挂号
          </el-menu-item>
          <el-menu-item index="/owner/consult">
            <el-icon><ChatDotRound /></el-icon>
            在线咨询
          </el-menu-item>
        </el-menu>
        
        <div class="user-section">
          <el-badge :value="unreadCount" :hidden="!unreadCount" class="message-badge">
            <el-button text @click="goToMessages">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatar || defaultAvatar" />
              <span class="username">{{ userStore.username || '宠物主' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><ShoppingBag /></el-icon>我的订单
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <!-- 主内容区 -->
    <el-main class="main-content">
      <div class="content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </el-main>
    
    <!-- 页脚 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>© 2024 宠物医院管理系统 | 客户服务热线：400-XXX-XXXX</p>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">服务协议</a>
          <a href="#">隐私政策</a>
          <a href="#">帮助中心</a>
        </div>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Tickets, FirstAidKit, Calendar, ChatDotRound,
  Bell, ArrowDown, User, ShoppingBag, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const unreadCount = ref(2) // 模拟未读消息数

const activeMenu = computed(() => route.path)

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
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
  }).catch(() => {})
}

const goToMessages = () => {
  router.push('/owner/messages')
}
</script>

<style scoped>
.owner-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  font-size: 24px;
}

.nav-menu {
  border-bottom: none;
  flex: 1;
  margin: 0 40px;
  justify-content: center;
}

.nav-menu :deep(.el-menu-item) {
  font-size: 15px;
  padding: 0 25px;
  height: 64px;
  line-height: 64px;
}

.nav-menu :deep(.el-menu-item .el-icon) {
  margin-right: 6px;
  font-size: 18px;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.message-badge :deep(.el-badge__content) {
  border: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  flex: 1;
  padding: 0;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 124px);
}

.footer {
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 20px 0;
  height: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.footer-content p {
  margin-bottom: 10px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.footer-links a {
  color: #909399;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #409EFF;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>