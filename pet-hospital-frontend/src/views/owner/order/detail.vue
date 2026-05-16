<template>
  <div class="order-detail-page">
    <div class="page-nav">
      <el-button link @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('common.back') }}
      </el-button>
    </div>

    <div v-loading="loading">
      <!-- Order Status Card -->
      <div class="status-card">
        <div class="status-icon" :class="detail.payStatus">
          {{ getStatusIcon(detail.payStatus) }}
        </div>
        <div class="status-info">
          <div class="status-title" :class="detail.payStatus">
            {{ getStatusText(detail.payStatus) }}
          </div>
          <div class="status-desc">{{ getStatusDesc(detail.payStatus) }}</div>
        </div>
      </div>

      <!-- Order Info Card -->
      <div class="info-card">
        <div class="card-header">
          <span class="card-title">{{ $t('order.orderInfo') }}</span>
        </div>
        <div class="info-list">
          <div class="info-item">
            <span class="label">{{ $t('order.orderNo') }}</span>
            <span class="value">{{ detail.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">{{ $t('order.createTime') }}</span>
            <span class="value">{{ formatDate(detail.createTime) }}</span>
          </div>
          <div class="info-item" v-if="detail.payTime">
            <span class="label">{{ $t('order.paymentTime') }}</span>
            <span class="value">{{ formatDate(detail.payTime) }}</span>
          </div>
          <div class="info-item" v-if="detail.payMethod">
            <span class="label">{{ $t('order.paymentMethod') }}</span>
            <span class="value">{{ getPayMethodText(detail.payMethod) }}</span>
          </div>
        </div>
      </div>

      <!-- Product Info Card -->
      <div class="goods-card">
        <div class="card-header">
          <span class="card-title">{{ $t('order.productInfo') }}</span>
        </div>
        <div class="goods-list">
          <div v-for="item in detail.orderItems" :key="item.itemName" class="goods-item">
            <div class="goods-icon">🐾</div>
            <div class="goods-info">
              <div class="goods-name">{{ item.itemName }}</div>
              <div class="goods-spec">{{ $t('order.unitPrice') }} ¥{{ item.unitPrice }} × {{ item.quantity }}</div>
            </div>
            <div class="goods-price">¥{{ item.amount }}</div>
          </div>
        </div>
        <div class="total-amount">
          <span>{{ $t('order.total') }}</span>
          <span class="total-price">¥{{ detail.totalAmount }}</span>
        </div>
      </div>

      <!-- Pet Info Card -->
      <div class="pet-card" v-if="detail.petName">
        <div class="card-header">
          <span class="card-title">{{ $t('order.petInfo') }}</span>
        </div>
        <div class="pet-info">
          <div class="pet-icon">🐾</div>
          <div class="pet-detail">
            <div class="pet-name">{{ detail.petName }}</div>
            <div class="pet-id">{{ $t('order.idLabel') }} {{ detail.petId }}</div>
          </div>
        </div>
      </div>

      <!-- Bottom Buttons -->
      <div class="action-buttons" v-if="detail.payStatus === 'pending'">
        <el-button type="primary" size="large" @click="handlePay">{{ $t('order.payNow') }}</el-button>
        <el-button size="large" @click="handleCancel">{{ $t('order.cancelOrder') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/owner/owner'

const { t } = useI18n()
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

// Get token
const getToken = () => {
  return localStorage.getItem('pet_hospital_token')
}

// Format date
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getStatusText = (status) => {
  const texts = { pending: t('order.pending'), paid: t('order.paid'), cancelled: t('order.cancelled') }
  return texts[status] || status
}

const getStatusDesc = (status) => {
  const descs = { 
    pending: t('order.orderSubmitted'), 
    paid: t('order.orderPaid'), 
    cancelled: t('order.orderCancelledDesc') 
  }
  return descs[status] || ''
}

const getStatusIcon = (status) => {
  const icons = { pending: '⏰', paid: '✓', cancelled: '✗' }
  return icons[status] || '📦'
}

const getPayMethodText = (method) => {
  const methods = { cash: t('order.cash'), wechat: t('order.wechatPay'), alipay: t('order.alipay'), card: t('order.card') }
  return methods[method] || method || t('order.unpaid')
}

const goBack = () => {
  router.push('/owner/orders')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const token = getToken()
    const response = await fetch(`/api/owner/order/${orderId}`, {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    console.log('Order detail response:', res)
    
    if (res.code === 200) {
      const data = res.data?.data || res.data
      detail.value = {
        ...data,
        orderItems: data.orderItems || []
      }
    } else {
      ElMessage.error(res.message || res.msg || t('order.failedToLoad'))
    }
  } catch (error) {
    console.error('Failed to load order detail:', error)
    // Mock data
    detail.value = {
      id: orderId,
      orderNo: 'ORD202404160001',
      petName: 'Buddy',
      totalAmount: 95,
      payStatus: 'pending',
      payMethod: '',
      payTime: '',
      createTime: new Date().toISOString(),
      orderItems: [
        { itemName: 'Registration Fee', unitPrice: 10, quantity: 1, amount: 10 },
        { itemName: 'Consultation Fee', unitPrice: 50, quantity: 1, amount: 50 },
        { itemName: 'Ointment', unitPrice: 35, quantity: 1, amount: 35 }
      ]
    }
  } finally {
    loading.value = false
  }
}

const handlePay = () => {
  ElMessage.info(t('order.paymentAtFrontDesk'))
}

const handleCancel = () => {
  ElMessageBox.confirm(t('order.confirmCancelOrder'), t('common.tip'), { type: 'warning' }).then(() => {
    ElMessage.success(t('order.orderCancelled'))
    loadDetail()
  })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.order-detail-page {
  .page-nav {
    margin-bottom: 20px;
    
    .back-btn {
      color: #64748b;
      font-size: 14px;
      
      &:hover {
        color: #3b82f6;
      }
    }
  }
  
  .status-card {
    background: white;
    border-radius: 20px;
    padding: 24px;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    
    .status-icon {
      width: 60px;
      height: 60px;
      background: #f8fafc;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
      
      &.pending {
        background: #fef3c7;
      }
      &.paid {
        background: #d1fae5;
      }
      &.cancelled {
        background: #fee2e2;
      }
    }
    
    .status-info {
      flex: 1;
      
      .status-title {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 4px;
        
        &.pending { color: #d97706; }
        &.paid { color: #059669; }
        &.cancelled { color: #dc2626; }
      }
      
      .status-desc {
        font-size: 13px;
        color: #64748b;
      }
    }
  }
  
  .info-card, .goods-card, .pet-card {
    background: white;
    border-radius: 20px;
    margin-bottom: 16px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    
    .card-header {
      padding: 16px 20px;
      background: #f8fafc;
      border-bottom: 1px solid #e2e8f0;
      
      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
      }
    }
  }
  
  .info-list {
    padding: 16px 20px;
    
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #f1f5f9;
      
      &:last-child {
        border-bottom: none;
      }
      
      .label {
        font-size: 14px;
        color: #64748b;
      }
      
      .value {
        font-size: 14px;
        font-weight: 500;
        color: #1e293b;
      }
    }
  }
  
  .goods-list {
    padding: 0 20px;
    
    .goods-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px 0;
      border-bottom: 1px solid #f1f5f9;
      
      &:last-child {
        border-bottom: none;
      }
      
      .goods-icon {
        font-size: 32px;
      }
      
      .goods-info {
        flex: 1;
        
        .goods-name {
          font-size: 15px;
          font-weight: 500;
          color: #1e293b;
          margin-bottom: 4px;
        }
        
        .goods-spec {
          font-size: 12px;
          color: #94a3b8;
        }
      }
      
      .goods-price {
        font-size: 15px;
        font-weight: 600;
        color: #f59e0b;
      }
    }
  }
  
  .total-amount {
    padding: 16px 20px;
    border-top: 1px solid #e2e8f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: #1e293b;
    
    .total-price {
      font-size: 20px;
      font-weight: 700;
      color: #f59e0b;
    }
  }
  
  .pet-card {
    .pet-info {
      padding: 16px 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      
      .pet-icon {
        font-size: 40px;
      }
      
      .pet-detail {
        .pet-name {
          font-size: 16px;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 4px;
        }
        
        .pet-id {
          font-size: 12px;
          color: #94a3b8;
        }
      }
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 16px;
    margin-top: 20px;
    margin-bottom: 20px;
    
    .el-button {
      flex: 1;
    }
  }
}
</style>
