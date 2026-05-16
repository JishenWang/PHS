<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-code">403</div>
      <div class="error-message">{{ $t('error403.message') }}</div>
      <el-button type="primary" @click="goBack">{{ $t('error403.backHome') }}</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/')
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
  color: #e6a23c;
  margin-bottom: 20px;
}

.error-message {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}
</style>
