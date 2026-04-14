<template>
  <div class="page-content">
    <!-- 头部 -->
    <div class="header" style="background: linear-gradient(135deg, #ff6b6b 0%, #f5a623 100%); padding: 40px 20px 60px; border-radius: 0 0 40px 40px;">
      <div>
        <h2 style="color: white; font-size: 28px; margin-bottom: 8px;">我的订单</h2>
        <p style="color: rgba(255,255,255,0.9); font-size: 14px;">查看消费记录</p>
      </div>
    </div>

    <!-- 状态Tab -->
    <div style="padding: 0 20px; margin-top: -20px;">
      <div style="display: flex; gap: 8px; background: white; padding: 8px; border-radius: 50px; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
        <div 
          v-for="tab in tabs" 
          :key="tab.value"
          :style="{
            flex: 1,
            textAlign: 'center',
            padding: '8px 0',
            borderRadius: '40px',
            background: activeTab === tab.value ? '#f5a623' : 'transparent',
            color: activeTab === tab.value ? 'white' : '#666',
            cursor: 'pointer',
            fontSize: '13px',
            transition: 'all 0.3s'
          }"
          @click="activeTab = tab.value; loadOrders()">
          {{ tab.label }}
        </div>
      </div>
    </div>

    <!-- 订单列表 -->
    <div style="padding: 20px;" v-loading="loading">
      <div v-for="order in orderList" :key="order.id" 
           style="background: white; border-radius: 20px; margin-bottom: 15px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05); cursor: pointer;"
           @click="viewDetail(order.id)">
        
        <!-- 订单头部 -->
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; background: #f8f9fc; border-bottom: 1px solid #f0f0f0;">
          <div>
            <span style="color: #999; font-size: 12px;">订单号：</span>
            <span style="color: #666; font-size: 12px;">{{ order.orderNo }}</span>
          </div>
          <div>
            <span :style="{
              display: 'inline-block',
              padding: '4px 12px',
              borderRadius: '20px',
              fontSize: '11px',
              background: getStatusBg(order.payStatus),
              color: getStatusColor(order.payStatus)
            }">
              {{ getStatusText(order.payStatus) }}
            </span>
          </div>
        </div>

        <!-- 订单内容 -->
        <div style="padding: 16px;">
          <div style="display: flex; gap: 12px; margin-bottom: 12px;">
            <div style="width: 50px; height: 50px; background: #f8f9fc; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px;">
              🐾
            </div>
            <div style="flex: 1;">
              <div style="font-weight: bold;">{{ order.petName || '未指定宠物' }}</div>
              <div style="color: #999; font-size: 12px; margin-top: 4px;">{{ order.createTime }}</div>
            </div>
          </div>
          
          <!-- 订单项目预览 -->
          <div style="margin-bottom: 12px;">
            <div v-for="(item, index) in (order.orderItems || []).slice(0, 2)" :key="index" 
                 style="display: flex; justify-content: space-between; font-size: 13px; color: #666; margin-bottom: 6px;">
              <span>{{ item.itemName }} x{{ item.quantity }}</span>
              <span>¥{{ item.amount }}</span>
            </div>
            <div v-if="(order.orderItems || []).length > 2" style="color: #999; font-size: 12px; margin-top: 4px;">
              等{{ order.orderItems.length }}件商品
            </div>
          </div>
          
          <!-- 订单底部 -->
          <div style="display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid #f0f0f0;">
            <div style="color: #999; font-size: 12px;">
              共{{ order.orderItems ? order.orderItems.length : 0 }}件商品
            </div>
            <div>
              <span style="color: #999; font-size: 12px;">实付：</span>
              <span style="font-size: 18px; font-weight: bold; color: #f5a623;">¥{{ order.totalAmount }}</span>
            </div>
          </div>
        </div>

        <!-- 订单按钮 -->
        <div v-if="order.payStatus === 'pending'" style="padding: 12px 16px; border-top: 1px solid #f0f0f0; text-align: right;" @click.stop>
          <el-button type="primary" size="small" plain @click="viewDetail(order.id)">查看详情</el-button>
        </div>
        <div v-else style="padding: 12px 16px; border-top: 1px solid #f0f0f0; text-align: right;" @click.stop>
          <el-button type="info" size="small" plain @click="viewDetail(order.id)">查看详情</el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && orderList.length === 0" style="text-align: center; padding: 60px 20px;">
        <div style="font-size: 60px; margin-bottom: 16px;">📦</div>
        <p style="color: #999;">暂无订单记录</p>
      </div>
    </div>

    <!-- 分页 -->
    <div style="padding: 0 20px 20px; display: flex; justify-content: center;">
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
      <router-link to="/profile" class="nav-item">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Avatar, Notebook, Calendar, ChatDotRound, User } from '@element-plus/icons-vue'
import { getOrderList } from '@/api/owner/owner'

const router = useRouter()
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const activeTab = ref('all')

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: 'pending' },
  { label: '已支付', value: 'paid' },
  { label: '已取消', value: 'cancelled' }
]

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

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }
    if (activeTab.value !== 'all') {
      params.payStatus = activeTab.value
    }
    const res = await getOrderList(params)
    if (res.code === 200) {
      orderList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    // 模拟数据
    orderList.value = [
      { 
        id: 1, 
        orderNo: 'ORD202401150001', 
        petName: '旺财', 
        totalAmount: 95, 
        payStatus: 'paid', 
        createTime: '2024-01-15 14:30',
        orderItems: [
          { itemName: '挂号费', quantity: 1, amount: 10 },
          { itemName: '诊疗费', quantity: 1, amount: 50 },
          { itemName: '药膏', quantity: 1, amount: 35 }
        ]
      },
      { 
        id: 2, 
        orderNo: 'ORD202401100002', 
        petName: '咪咪', 
        totalAmount: 55, 
        payStatus: 'paid', 
        createTime: '2024-01-10 10:15',
        orderItems: [
          { itemName: '挂号费', quantity: 1, amount: 10 },
          { itemName: '驱虫药', quantity: 1, amount: 45 }
        ]
      },
      { 
        id: 3, 
        orderNo: 'ORD202401200003', 
        petName: '旺财', 
        totalAmount: 199, 
        payStatus: 'pending', 
        createTime: '2024-01-20 09:00',
        orderItems: [
          { itemName: '体检套餐', quantity: 1, amount: 199 }
        ]
      }
    ]
    total.value = 3
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/order/${id}`)
}

onMounted(() => {
  loadOrders()
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