<template>
  <div class="page-content">
    <!-- 头部 -->
    <div class="header" style="background: linear-gradient(135deg, #ff6b6b 0%, #f5a623 100%); padding: 30px 20px; border-radius: 0 0 30px 30px;">
      <div style="display: flex; align-items: center; gap: 16px;">
        <el-button circle :icon="ArrowLeft" style="background: rgba(255,255,255,0.2); border: none; color: white;" @click="goBack" />
        <div>
          <h2 style="color: white; font-size: 20px;">订单详情</h2>
        </div>
      </div>
    </div>

    <div style="padding: 20px;" v-loading="loading">
      <!-- 订单状态 -->
      <div style="background: white; border-radius: 20px; padding: 20px; margin-bottom: 15px; text-align: center; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
        <div :style="{
          width: '60px',
          height: '60px',
          borderRadius: '50%',
          background: getStatusBg(detail.payStatus),
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '30px',
          marginBottom: '12px'
        }">
          {{ getStatusIcon(detail.payStatus) }}
        </div>
        <div :style="{ color: getStatusColor(detail.payStatus), fontWeight: 'bold', fontSize: '18px' }">
          {{ getStatusText(detail.payStatus) }}
        </div>
        <div style="color: #999; font-size: 12px; margin-top: 8px;">
          订单号：{{ detail.orderNo }}
        </div>
      </div>

      <!-- 订单信息 -->
      <div style="background: white; border-radius: 20px; margin-bottom: 15px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
        <div style="padding: 16px; border-bottom: 1px solid #f0f0f0; background: #f8f9fc; font-weight: bold;">
          订单信息
        </div>
        <div style="padding: 16px;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">创建时间</span>
            <span>{{ detail.createTime }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">支付方式</span>
            <span>{{ getPayMethodText(detail.payMethod) }}</span>
          </div>
          <div v-if="detail.payTime" style="display: flex; justify-content: space-between;">
            <span style="color: #999;">支付时间</span>
            <span>{{ detail.payTime }}</span>
          </div>
        </div>
      </div>

      <!-- 商品信息 -->
      <div style="background: white; border-radius: 20px; margin-bottom: 15px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
        <div style="padding: 16px; border-bottom: 1px solid #f0f0f0; background: #f8f9fc; font-weight: bold;">
          商品信息
        </div>
        <div style="padding: 16px;">
          <div style="display: flex; gap: 12px; margin-bottom: 16px;">
            <div style="width: 50px; height: 50px; background: #f8f9fc; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px;">
              🐾
            </div>
            <div>
              <div style="font-weight: bold;">{{ detail.petName || '未指定宠物' }}</div>
              <div style="color: #999; font-size: 12px;">宠物</div>
            </div>
          </div>
          
          <div v-for="item in detail.orderItems" :key="item.itemName" 
               style="display: flex; justify-content: space-between; padding: 10px 0; border-top: 1px solid #f5f5f5;">
            <div>
              <div>{{ item.itemName }}</div>
              <div style="color: #999; font-size: 12px;">单价 ¥{{ item.unitPrice }} x {{ item.quantity }}</div>
            </div>
            <div style="font-weight: bold;">¥{{ item.amount }}</div>
          </div>
          
          <div style="display: flex; justify-content: space-between; padding-top: 12px; margin-top: 8px; border-top: 1px solid #f0f0f0;">
            <span>商品总额</span>
            <span>¥{{ detail.totalAmount }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-top: 8px; font-size: 16px; font-weight: bold;">
            <span>实付金额</span>
            <span style="color: #f5a623;">¥{{ detail.totalAmount }}</span>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div v-if="detail.payStatus === 'pending'" style="display: flex; gap: 12px; margin-top: 20px;">
        <el-button type="primary" style="flex: 1;" @click="handlePay">去支付</el-button>
        <el-button style="flex: 1;" @click="handleCancel">取消订单</el-button>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <router-link to="/pet" class="nav-item">
        <el-icon><Avatar /></el-icon>
        <span>宠物</span>
      </router-link>
      <router-link to="/health" class="nav-item">
        <el-icon><Notebook /></el-icon>
        <span>健康</span>
      </router-link>
      <router-link to="/reserve" class="nav-item">
        <el-icon><Calendar /></el-icon>
        <span>预约</span>
      </router-link>
      <router-link to="/consult" class="nav-item">
        <el-icon><ChatDotRound /></el-icon>
        <span>咨询</span>
      </router-link>
      <router-link to="/profile" class="nav-item active">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Avatar, Notebook, Calendar, ChatDotRound, User } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/owner/owner'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

const loading = ref(false)
const detail = ref({
  id: '',
  orderNo: '',
  petName: '',
  totalAmount: 0,
  payStatus: '',
  payMethod: '',
  payTime: '',
  createTime: '',
  orderItems: []
})

const getStatusBg = (status) => {
  const bg = { pending: '#fff3e8', paid: '#e8f8e8', cancelled: '#f0f0f0' }
  return bg[status] || '#f0f0f0'
}

const getStatusColor = (status) => {
  const colors = { pending: '#f5a623', paid: '#67c23a', cancelled: '#999' }
  return colors[status] || '#999'
}

const getStatusText = (status) => {
  const texts = { pending: '待支付', paid: '已支付', cancelled: '已取消' }
  return texts[status] || status
}

const getStatusIcon = (status) => {
  const icons = { pending: '⏰', paid: '✓', cancelled: '✗' }
  return icons[status] || '📦'
}

const getPayMethodText = (method) => {
  const methods = { cash: '现金', wechat: '微信支付', alipay: '支付宝' }
  return methods[method] || method || '未支付'
}

const goBack = () => {
  // 返回到订单列表页
  router.push('/orders')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200) {
      detail.value = res.data
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    // 模拟数据
    detail.value = {
      id: orderId,
      orderNo: 'ORD202401150001',
      petName: '旺财',
      totalAmount: 95,
      payStatus: 'pending',
      payMethod: '',
      payTime: '',
      createTime: '2024-01-15 14:30',
      orderItems: [
        { itemName: '挂号费', unitPrice: 10, quantity: 1, amount: 10 },
        { itemName: '诊疗费', unitPrice: 50, quantity: 1, amount: 50 },
        { itemName: '药膏', unitPrice: 35, quantity: 1, amount: 35 }
      ]
    }
  } finally {
    loading.value = false
  }
}

const handlePay = () => {
  ElMessage.info('支付功能由前台收银端处理')
}

const handleCancel = () => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' }).then(() => {
    ElMessage.success('已取消订单')
    loadDetail()
  })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.page-content {
  padding-bottom: 80px;
  min-height: 100vh;
  background: #f8f9fc;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 10px 20px 20px;
  box-shadow: 0 -2px 15px rgba(0,0,0,0.05);
  border-radius: 20px 20px 0 0;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #999;
  font-size: 12px;
  transition: all 0.3s;
}

.nav-item.active {
  color: #ff6b6b;
}

.nav-item .el-icon {
  font-size: 22px;
}
</style>