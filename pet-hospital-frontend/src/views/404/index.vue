<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-message">抱歉，您访问的页面不存在</div>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { getUserRole } from '@/utils/auth'

const router = useRouter()

const goHome = () => {
  const role = getUserRole()
  
  const homePathMap = {
    'admin': '/admin/dashboard',
    'doctor': '/doctor/accept',
    'desk': '/desk/customer',
    'owner': '/owner/pet'
  }
  
  const homePath = homePathMap[role] || '/login'
  router.push(homePath)
}
</script>

<style scoped>
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #909399;
  margin-bottom: 20px;
}

.error-message {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}
</style>