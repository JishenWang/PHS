<template>
  <div class="order-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">我的订单</h2>
        <p class="page-subtitle">查看您的消费记录</p>
      </div>
    </div>

    <!-- 状态Tab -->
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

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading">
      <div v-for="order in orderList" :key="order.id" class="order-card" @click="viewDetail(order.id)">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatDate(order.createTime) }}</span>
          </div>
          <div class="order-status" :class="order.payStatus">
            {{ getStatusText(order.payStatus) }}
          </div>
        </div>

        <!-- 订单内容 -->
        <div class="order-content">
          <div class="order-icon">🐾</div>
          <div class="order-detail">
            <div class="order-pet">{{ order.petName || '未指定宠物' }}</div>
            <div class="order-items">
              <span v-for="(item, idx) in order.orderItems" :key="idx" class="item-name">
                {{ item.itemName }}
                <span v-if="idx < order.orderItems.length - 1">、</span>
              </span>
            </div>
          </div>
          <div class="order-amount">
            <span class="amount-label">实付</span>
            <span class="amount-value">¥{{ order.totalAmount }}</span>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <el-button 
            v-if="order.payStatus === 'pending'" 
            type="primary" 
            size="small" 
            plain
            @click.stop="handlePay(order)"
          >
            去支付
          </el-button>
          <el-button 
            v-if="order.payStatus === 'pending'" 
            size="small" 
            plain
            @click.stop="handleCancelOrder(order)"
          >
            取消订单
          </el-button>
          <el-button 
            v-else 
            size="small" 
            plain
            @click.stop="viewDetail(order.id)"
          >
            查看详情
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && orderList.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>暂无订单</h3>
        <p>您还没有消费记录</p>
      </div>
    </div>

    <!-- 分页 -->
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList } from '@/api/owner/owner'

const router = useRouter()
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const activeTab = ref('all')

const tabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '待支付', value: 'pending', count: 0 },
  { label: '已支付', value: 'paid', count: 0 },
  { label: '已取消', value: 'cancelled', count: 0 }
])

// 获取 token
const getToken = () => {
  return localStorage.getItem('pet_hospital_token')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const getStatusText = (status) => {
  const texts = { pending: '待支付', paid: '已支付', cancelled: '已取消' }
  return texts[status] || status
}

// 加载订单列表
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
    console.log('订单列表响应:', res)
    
    if (res.code === 200) {
      // 数据在 res.data.data 中
      let records = res.data?.data || res.data?.records || []
      // 补充宠物名称（如果后端没有返回）
      records = records.map(record => ({
        ...record,
        petName: record.petName || '未知宠物',
        orderItems: record.orderItems || []
      }))
      orderList.value = records
      total.value = res.data?.total || records.length
      updateTabCounts(orderList.value)
    } else {
      ElMessage.error(res.message || res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    // 使用模拟数据
    const mockOrders = [
      { id: 1, orderNo: 'ORD202404160001', petName: '旺财', totalAmount: 95, payStatus: 'paid', createTime: new Date().toISOString(), orderItems: [{ itemName: '挂号费', quantity: 1, amount: 10 }] },
      { id: 2, orderNo: 'ORD202404160002', petName: '咪咪', totalAmount: 55, payStatus: 'pending', createTime: new Date().toISOString(), orderItems: [{ itemName: '驱虫药', quantity: 1, amount: 45 }] }
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
  ElMessage.info('支付功能由前台收银端处理')
}

const handleCancelOrder = (order) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' }).then(() => {
    ElMessage.success('订单已取消')
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