<template>
  <el-container class="desk-layout">
    <el-aside width="200px" class="sidebar">
      <div class="logo">前台接待系统</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/desk/customer">
          <span class="menu-icon">👥</span>
          <span>客户查询</span>
        </el-menu-item>
        <el-menu-item index="/desk/register">
          <span class="menu-icon">📝</span>
          <span>挂号管理</span>
        </el-menu-item>
        <el-menu-item index="/desk/charge">
          <span class="menu-icon">💰</span>
          <span>收费管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="welcome">欢迎，前台工作人员</span>
        </div>
        <div class="header-right">
          <el-button type="danger" size="small" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.desk-layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}

.menu-icon {
  margin-right: 10px;
  font-size: 18px;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome {
  font-size: 14px;
  color: #666;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>