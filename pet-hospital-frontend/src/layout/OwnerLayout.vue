<template>
  <el-container class="owner-layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo">🐾 宠物医院客户中心</div>
        <el-menu
          :default-active="activeMenu"
          router
          mode="horizontal"
          background-color="#fff"
          text-color="#333"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/owner/pet">我的宠物</el-menu-item>
          <el-menu-item index="/owner/health">健康记录</el-menu-item>
          <el-menu-item index="/owner/reserve">预约挂号</el-menu-item>
          <el-menu-item index="/owner/consult">在线咨询</el-menu-item>
        </el-menu>
        <div class="user-info">
          <el-dropdown>
            <span class="el-dropdown-link">
              王先生 <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <el-main class="main-content">
      <router-view></router-view>
    </el-main>
    
    <el-footer class="footer">
      <p>© 2024 宠物医院管理系统 | 客户服务热线：400-XXX-XXXX</p>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.owner-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.user-info {
  cursor: pointer;
}

.main-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  min-height: calc(100vh - 140px);
}

.footer {
  text-align: center;
  color: #999;
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
}
</style>