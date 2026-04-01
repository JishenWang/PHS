<template>
  <div class="dashboard-page">
    <h1>宠物医院管理系统 - 管理端首页</h1>
    <el-card style="margin-top: 30px; padding: 20px;">
      <h3>前后端连通状态</h3>
      <p v-if="loading" style="font-size: 16px;">正在连接后端服务...</p>
      <p v-else-if="success" style="color: #67C23A; font-size: 18px; font-weight: bold;">{{ backendMsg }}</p>
      <p v-else style="color: #F56C6C; font-size: 18px; font-weight: bold;">❌ 后端连接失败，请检查服务</p>
    </el-card>
  </div>
</template>

<script>
import { getAdminTest } from '@/api/admin/admin.js'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      backendMsg: '',
      loading: true,
      success: false
    }
  },
  mounted() {
    this.testBackendConnect()
  },
  methods: {
    async testBackendConnect() {
      try {
        const res = await getAdminTest()
        this.backendMsg = res
        this.success = true
        console.log('✅ 管理端前后端连通成功！')
      } catch (err) {
        console.error('❌ 管理端连接后端失败', err)
        this.success = false
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  padding: 30px;
  text-align: center;
}
</style>