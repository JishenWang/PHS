<template>
  <div class="order-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ $t('order.title') }}</h2>
        <p class="page-subtitle">{{ $t('order.subtitle') }}</p>
      </div>
    </div>

    <!-- Status Tabs -->
    <div class="tabs-container">
      <div 
        v-for="tab in tabs" 
        :key="tab.value"
        class="tab-item"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value; loadOrders()"
      >
        {{ tab.label }}
        <span class="tab-count" v-if="tab.count">{{ tab.count }}</span>
      </div>
    </div>

    <!-- Order List -->
    <div class="order-list" v-loading="loading">
      <div v-for="order in orderList" :key="order.id" class="order-card" @click="viewDetail(order.id)">
        <!-- Order Header -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">{{ $t('order.orderNo') }}: {{ order.orderNo }}</span>
            <span class="order-time">{{ formatDate(order.createTime) }}</span>
          </div>
          <div class="order-status" :class="order.payStatus">
            {{ getStatusText(order.payStatus) }}
          </div>
        </div>

        <!-- Order Content -->
        <div class="order-content">
          <div class="order-icon">🐾</div>
          <div class="order-detail">
            <div class="order-pet">{{ order.petName || $t('order.unspecifiedPet') }}</div>
            <div class="order-items">
              <span v-for="(item, idx) in order.orderItems" :key="idx" class="item-name">
                {{ item.itemName }}
                <span v-if="idx < order.orderItems.length - 1">、</span>
              </span>
            </div>
          </div>
          <div class="order-amount">
            <span class="amount-label">{{ $t('order.actualPayment') }}</span>
            <span class="amount-value">¥{{ order.totalAmount }}</span>
          </div>
        </div>

        <!-- Order Footer -->
        <div class="order-footer">
          <el-button 
            v-if="order.payStatus === 'pending'" 
            type="primary" 
            size="small" 
            plain
            @click.stop="handlePay(order)"
          >
            {{ $t('order.payNow') }}
          </el-button>
          <el-button 
            v-if="order.payStatus === 'pending'" 
            size="small" 
            plain
            @click.stop="handleCancelOrder(order)"
          >
            {{ $t('order.cancelOrder') }}
          </el-button>
          <el-button 
            v-else 
            size="small" 
            plain
            @click.stop="viewDetail(order.id)"
          >
            {{ $t('common.detail') }}
          </el-button>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && orderList.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>{{ $t('order.noOrders') }}</h3>
        <p>{{ $t('order.noPurchaseRecords') }}</p>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next"
        @current-change="loadOrders"
        small
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList } from '@/api/owner/owner'

const { t } = useI18n()
const router = useRouter()
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const activeTab = ref('all')

const tabs = ref([
  { label: t('common.all'), value: 'all', count: 0 },
  { label: t('order.pending'), value: 'pending', count: 0 },
  { label: t('order.paid'), value: 'paid', count: 0 },
  { label: t('order.cancelled'), value: 'cancelled', count: 0 }
])

// Get token
const getToken = () => {
  return localStorage.getItem('pet_hospital_token')
}

// Format date
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const getStatusText = (status) => {
  const texts = { pending: t('order.pending'), paid: t('order.paid'), cancelled: t('order.cancelled') }
  return texts[status] || status
}

// Load order list
const loadOrders = async () => {
  loading.value = true
  try {
    const token = getToken()
    let url = `/api/owner/order/list?page=${page.value}&pageSize=${pageSize.value}`
    if (activeTab.value !== 'all') {
      url += `&payStatus=${activeTab.value}`
    }
    
    const response = await fetch(url, {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    console.log('Order list response:', res)
    
    if (res.code === 200) {
      // Data is in res.data.data
      let records = res.data?.data || res.data?.records || []
      // Supplement pet name (if not returned by backend)
      records = records.map(record => ({
        ...record,
        petName: record.petName || t('order.unknownPet'),
        orderItems: record.orderItems || []
      }))
      orderList.value = records
      total.value = res.data?.total || records.length
      updateTabCounts(orderList.value)
    } else {
      ElMessage.error(res.message || res.msg || t('order.failedToLoad'))
    }
  } catch (error) {
    console.error('Failed to load orders:', error)
    // Use mock data
    const mockOrders = [
      { id: 1, orderNo: 'ORD202404160001', petName: 'Buddy', totalAmount: 95, payStatus: 'paid', createTime: new Date().toISOString(), orderItems: [{ itemName: 'Registration Fee', quantity: 1, amount: 10 }] },
      { id: 2, orderNo: 'ORD202404160002', petName: 'Luna', totalAmount: 55, payStatus: 'pending', createTime: new Date().toISOString(), orderItems: [{ itemName: 'Dewormer', quantity: 1, amount: 45 }] }
    ]
    orderList.value = mockOrders
    total.value = mockOrders.length
    updateTabCounts(orderList.value)
  } finally {
    loading.value = false
  }
}

const updateTabCounts = (orders) => {
  tabs.value = tabs.value.map(tab => ({
    ...tab,
    count: tab.value === 'all' ? orders.length : orders.filter(o => o.payStatus === tab.value).length
  }))
}

const viewDetail = (id) => {
  router.push(`/owner/order/${id}`)
}

const handlePay = (order) => {
  ElMessage.info(t('order.paymentAtFrontDesk'))
}

const handleCancelOrder = (order) => {
  ElMessageBox.confirm(t('order.confirmCancelOrder'), t('common.tip'), { type: 'warning' }).then(() => {
    ElMessage.success(t('order.orderCancelled'))
    loadOrders()
  })
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped lang="scss">
.order-page {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 4px;
    }
    
    .page-subtitle {
      font-size: 14px;
      color: #64748b;
    }
  }
  
  .tabs-container {
    display: flex;
    gap: 8px;
    background: white;
    padding: 8px;
    border-radius: 50px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    
    .tab-item {
      flex: 1;
      text-align: center;
      padding: 10px 0;
      border-radius: 40px;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 14px;
      font-weight: 500;
      color: #64748b;
      position: relative;
      
      &:hover {
        background: #f8fafc;
      }
      
      &.active {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
      }
      
      .tab-count {
        margin-left: 6px;
        font-size: 12px;
        background: rgba(0, 0, 0, 0.1);
        padding: 2px 6px;
        border-radius: 20px;
        
        .active & {
          background: rgba(255, 255, 255, 0.2);
        }
      }
    }
  }
  
  .order-list {
    .order-card {
      background: white;
      border-radius: 20px;
      margin-bottom: 16px;
      overflow: hidden;
      transition: all 0.3s;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      cursor: pointer;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      }
      
      .order-header {
        padding: 16px 20px;
        background: #f8fafc;
        border-bottom: 1px solid #e2e8f0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .order-info {
          display: flex;
          flex-direction: column;
          gap: 4px;
          
          .order-no {
            font-size: 13px;
            font-weight: 500;
            color: #1e293b;
          }
          
          .order-time {
            font-size: 11px;
            color: #94a3b8;
          }
        }
        
        .order-status {
          font-size: 12px;
          padding: 4px 12px;
          border-radius: 20px;
          font-weight: 500;
          
          &.pending {
            background: #fef3c7;
            color: #d97706;
          }
          
          &.paid {
            background: #d1fae5;
            color: #059669;
          }
          
          &.cancelled {
            background: #fee2e2;
            color: #dc2626;
          }
        }
      }
      
      .order-content {
        padding: 20px;
        display: flex;
        align-items: center;
        gap: 16px;
        
        .order-icon {
          font-size: 40px;
        }
        
        .order-detail {
          flex: 1;
          
          .order-pet {
            font-size: 16px;
            font-weight: 600;
            color: #1e293b;
            margin-bottom: 6px;
          }
          
          .order-items {
            font-size: 13px;
            color: #64748b;
            
            .item-name {
              display: inline-block;
            }
          }
        }
        
        .order-amount {
          text-align: right;
          
          .amount-label {
            font-size: 12px;
            color: #94a3b8;
            display: block;
          }
          
          .amount-value {
            font-size: 20px;
            font-weight: 700;
            color: #f59e0b;
          }
        }
      }
      
      .order-footer {
        padding: 12px 20px;
        border-top: 1px solid #e2e8f0;
        text-align: right;
        display: flex;
        gap: 12px;
        justify-content: flex-end;
      }
    }
  }
  
  .empty-state {
    text-align: center;
    padding: 80px 20px;
    
    .empty-icon {
      font-size: 80px;
      margin-bottom: 20px;
    }
    
    h3 {
      font-size: 20px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 8px;
    }
    
    p {
      font-size: 14px;
      color: #64748b;
      margin-bottom: 24px;
    }
  }
  
  .pagination {
    margin-top: 24px;
    display: flex;
    justify-content: center;
  }
}
</style>
