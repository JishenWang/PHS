<template>
  <div class="reserve-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">预约申请</h2>
        <p class="page-subtitle">在线预约，省时省心</p>
      </div>
      <el-button type="primary" size="large" @click="handleCreate" class="add-btn">
        <el-icon><Plus /></el-icon>
        新建预约
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div
        v-for="stat in stats"
        :key="stat.key"
        class="stat-card"
        :class="{ active: activeTab === stat.key }"
        @click="handleTabChange(stat.key)"
      >
        <div class="stat-value" :style="{ color: stat.color }">{{ stat.count }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 预约列表 -->
    <div class="reserve-list" v-loading="loading">
      <div v-for="item in reserveList" :key="item.id" class="reserve-card" :class="{ cancelled: item.status === '3' }">
        <div class="reserve-status">
          <span class="status-dot" :class="'status-' + item.status"></span>
          <span class="status-text">{{ getStatusName(item.status) }}</span>
          <span class="reserve-time">{{ formatDateTime(item.appointmentTime) }}</span>
        </div>
        <div class="reserve-content">
          <div class="reserve-icon">🐾</div>
          <div class="reserve-info">
            <div class="reserve-pet">{{ item.petName || `宠物ID:${item.petId}` }}</div>
            <div class="reserve-service">{{ getServiceName(item.serviceType) }}</div>
            <div class="reserve-doctor" v-if="item.doctorName">
              <el-icon><User /></el-icon>
              <span>{{ item.doctorName }}</span>
            </div>
          </div>
        </div>
        <div class="reserve-footer" v-if="item.status === '0'">
          <el-button type="danger" plain size="small" @click.stop="handleCancelReserve(item)">取消预约</el-button>
        </div>
        <div class="reserve-footer" v-else>
          <el-button type="primary" plain size="small" @click.stop="viewDetail(item.id)">查看详情</el-button>
        </div>
      </div>

      <el-empty v-if="!loading && reserveList.length === 0" description="暂无预约记录">
        <el-button type="primary" @click="handleCreate">立即预约</el-button>
      </el-empty>
    </div>

    <!-- 创建预约弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建预约" width="500px">
      <el-steps :active="step" finish-status="success" align-center style="margin-bottom: 30px">
        <el-step title="选择宠物" />
        <el-step title="选择服务" />
        <el-step title="确认信息" />
      </el-steps>

      <div v-show="step === 0">
        <div class="pet-selector">
          <div v-for="pet in petList" :key="pet.id" class="pet-option" :class="{ active: form.petId === pet.id }" @click="form.petId = pet.id">
            <div class="pet-avatar">
              <el-icon size="30"><Avatar /></el-icon>
            </div>
            <div class="pet-name">{{ pet.name }}</div>
            <div class="pet-breed">{{ pet.breed }}</div>
          </div>
        </div>
        <div v-if="petList.length === 0" class="empty-pets">
          <el-empty description="暂无宠物，请先添加宠物" :image-size="80">
            <el-button type="primary" @click="router.push('/owner/pets')">去添加宠物</el-button>
          </el-empty>
        </div>
      </div>

      <div v-show="step === 1">
        <div class="service-selector">
          <div v-for="service in services" :key="service.value" class="service-option" :class="{ active: form.serviceType === service.value }" @click="form.serviceType = service.value">
            <div class="service-icon">{{ service.icon }}</div>
            <div class="service-name">{{ service.label }}</div>
            <div class="service-price">¥{{ service.price }}</div>
          </div>
        </div>
        <div style="margin-top: 20px">
          <el-date-picker
            v-model="form.appointmentTime"
            type="datetime"
            placeholder="选择预约时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </div>
      </div>

      <div v-show="step === 2">
        <div class="confirm-info">
          <div class="confirm-item">
            <span class="label">宠物</span>
            <span class="value">{{ selectedPet?.name }}</span>
          </div>
          <div class="confirm-item">
            <span class="label">服务</span>
            <span class="value">{{ getServiceName(form.serviceType) }}</span>
          </div>
          <div class="confirm-item">
            <span class="label">时间</span>
            <span class="value">{{ form.appointmentTime }}</span>
          </div>
          <div class="confirm-item">
            <span class="label">费用</span>
            <span class="value price">¥{{ getServicePrice(form.serviceType) }}</span>
          </div>
        </div>
        <el-input type="textarea" v-model="form.remark" placeholder="备注（选填）" :rows="3" />
      </div>

      <template #footer>
        <el-button v-if="step > 0" @click="step--">上一步</el-button>
        <el-button v-if="step < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="step === 2" type="primary" @click="submitReserve" :loading="submitting">提交预约</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 预约详情弹窗 -->
    <el-dialog v-model="detailVisible" title="预约详情" width="450px">
      <div v-if="currentDetail">
        <div class="detail-item">
          <span class="label">预约号</span>
          <span class="value">{{ currentDetail.appointmentNo }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态</span>
          <span class="value" :style="{ color: getStatusColor(currentDetail.status) }">{{ getStatusName(currentDetail.status) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">宠物</span>
          <span class="value">{{ currentDetail.petName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">服务</span>
          <span class="value">{{ getServiceName(currentDetail.serviceType) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">医生</span>
          <span class="value">{{ currentDetail.doctorName || '待分配' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">预约时间</span>
          <span class="value">{{ formatDateTime(currentDetail.appointmentTime) }}</span>
        </div>
        <div class="detail-item" v-if="currentDetail.remark">
          <span class="label">备注</span>
          <span class="value">{{ currentDetail.remark }}</span>
        </div>
        <div class="detail-item" v-if="currentDetail.cancelReason">
          <span class="label">取消原因</span>
          <span class="value" style="color: #ef4444;">{{ currentDetail.cancelReason }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentDetail?.status === '0'" type="danger" @click="cancelFromDetail">取消预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Avatar } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const reserveList = ref([])
const petList = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentDetail = ref(null)
const activeTab = ref('all')
const step = ref(0)

const stats = ref([
  { key: 'all', label: '全部', count: 0, color: '#64748b' },
  { key: '0', label: '待确认', count: 0, color: '#f59e0b' },
  { key: '1', label: '已确认', count: 0, color: '#10b981' },
  { key: '2', label: '已完成', count: 0, color: '#3b82f6' },
  { key: '3', label: '已取消', count: 0, color: '#ef4444' }
])

const services = [
  { value: 'consultation', label: '门诊诊疗', icon: '🏥', price: 100 },
  { value: 'vaccine', label: '疫苗接种', icon: '💉', price: 80 },
  { value: 'exam', label: '体检', icon: '📋', price: 150 },
  { value: 'grooming', label: '洗澡美容', icon: '🛁', price: 60 }
]

const form = ref({
  petId: '',
  serviceType: '',
  appointmentTime: '',
  remark: ''
})

const selectedPet = computed(() => petList.value.find(p => p.id === form.value.petId))

// 获取 token
const getToken = () => {
  return localStorage.getItem('pet_hospital_token')
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    '0': '待确认',
    '1': '已确认',
    '2': '已完成',
    '3': '已取消'
  }
  return statusMap[status] || '未知状态'
}

const getStatusColor = (status) => {
  const colors = {
    '0': '#f59e0b',
    '1': '#10b981',
    '2': '#3b82f6',
    '3': '#ef4444'
  }
  return colors[status] || '#64748b'
}

const getServiceName = (value) => {
  const serviceMap = {
    'consultation': '门诊诊疗',
    'vaccine': '疫苗接种',
    'exam': '体检',
    'grooming': '洗澡美容',
    '普通门诊': '门诊诊疗'
  }
  return serviceMap[value] || value || '未知服务'
}

const getServicePrice = (value) => {
  const service = services.find(s => s.value === value)
  return service?.price || 0
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 86400000
}

// 加载宠物列表
const loadPets = async () => {
  try {
    const token = getToken()
    const response = await fetch('/api/owner/pet/list?page=1&pageSize=100', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    if (res.code === 200) {
      const petData = res.data?.data || res.data?.records || res.data || []
      petList.value = Array.isArray(petData) ? petData : []
    }
  } catch (error) {
    console.error('加载宠物列表失败:', error)
    petList.value = []
  }
}

// 加载预约列表（核心修改）
const loadReserveList = async () => {
  loading.value = true
  try {
    const token = getToken()
    // 一次性获取所有预约数据
    const response = await fetch('/api/owner/reserve/list?page=1&pageSize=100', {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const res = await response.json()
    if (res.code === 200) {
      // 获取所有预约数据（处理嵌套结构）
      let allRecords = res.data?.data || res.data?.records || res.data || []
      if (!Array.isArray(allRecords)) allRecords = []
      // 1. 更新统计数据（基于全部数据）
      stats.value = stats.value.map(stat => {
        if (stat.key === 'all') {
          return { ...stat, count: allRecords.length }
        }
        const count = allRecords.filter(item => String(item.status) === stat.key).length
        return { ...stat, count: count }
      })
      // 2. 根据选中的标签筛选要显示的数据
      let showRecords = allRecords
      if (activeTab.value !== 'all') {
        showRecords = allRecords.filter(item => String(item.status) === activeTab.value)
      }
      // 3. 关联宠物名称
      reserveList.value = showRecords.map(item => {
        const pet = petList.value.find(p => p.id === item.petId)
        return {
          ...item,
          petName: pet?.name || `宠物ID:${item.petId}`,
          status: String(item.status)
        }
      })
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    reserveList.value = []
  } finally {
    loading.value = false
  }
}

// 切换标签页
const handleTabChange = (key) => {
  activeTab.value = key
  loadReserveList()
}

const handleCreate = () => {
  if (petList.value.length === 0) {
    ElMessage.warning('请先添加宠物')
    router.push('/owner/pets')
    return
  }
  step.value = 0
  form.value = { petId: '', serviceType: '', appointmentTime: '', remark: '' }
  dialogVisible.value = true
}

const nextStep = () => {
  if (step.value === 0 && !form.value.petId) {
    ElMessage.warning('请选择宠物')
    return
  }
  if (step.value === 1 && (!form.value.serviceType || !form.value.appointmentTime)) {
    ElMessage.warning('请完整填写信息')
    return
  }
  step.value++
}

const submitReserve = async () => {
  if (!form.value.petId || !form.value.serviceType || !form.value.appointmentTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    const token = getToken()
    let formattedTime = form.value.appointmentTime
    if (formattedTime && !formattedTime.includes('T')) {
      formattedTime = formattedTime.replace(' ', 'T')
    }
    const submitData = {
      petId: Number(form.value.petId),
      serviceType: form.value.serviceType,
      appointmentTime: formattedTime,
      remark: form.value.remark || ''
    }
    const response = await fetch('/api/owner/reserve', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify(submitData)
    })
    const res = await response.json()
    if (res.code === 200) {
      ElMessage.success('预约提交成功')
      dialogVisible.value = false
      await loadReserveList()
    } else {
      ElMessage.error(res.message || res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const handleCancelReserve = (item) => {
  ElMessageBox.prompt('请输入取消原因', '取消预约', {
    confirmButtonText: '确定取消',
    cancelButtonText: '暂不取消',
    inputPlaceholder: '请填写取消原因'
  }).then(async ({ value }) => {
    if (value) {
      try {
        const token = getToken()
        const response = await fetch(`/api/owner/reserve/${item.id}/cancel`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
          },
          body: JSON.stringify({ cancelReason: value })
        })
        const res = await response.json()
        if (res.code === 200) {
          ElMessage.success('已取消预约')
          await loadReserveList()
        } else {
          ElMessage.error(res.message || '取消失败')
        }
      } catch (error) {
        console.error('取消失败:', error)
        ElMessage.error('取消失败')
      }
    }
  }).catch(() => {})
}

const cancelFromDetail = () => {
  if (currentDetail.value) {
    handleCancelReserve(currentDetail.value)
    detailVisible.value = false
  }
}

const viewDetail = (id) => {
  const detail = reserveList.value.find(item => item.id === id)
  if (detail) {
    currentDetail.value = detail
    detailVisible.value = true
  }
}

onMounted(async () => {
  await loadPets()
  await loadReserveList()
})
</script>

<style scoped lang="scss">
.reserve-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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

    .add-btn {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(245, 158, 11, 0.4);
      }
    }
  }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 12px;
    margin-bottom: 24px;

    .stat-card {
      background: white;
      border-radius: 16px;
      padding: 16px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      }

      &.active {
        border: 2px solid #f59e0b;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 700;
      }

      .stat-label {
        font-size: 12px;
        color: #64748b;
        margin-top: 4px;
      }
    }
  }

  .reserve-list {
    .reserve-card {
      background: white;
      border-radius: 20px;
      margin-bottom: 16px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      }

      &.cancelled {
        opacity: 0.6;
      }

      .reserve-status {
        background: #f8fafc;
        padding: 12px 20px;
        display: flex;
        align-items: center;
        gap: 12px;
        border-bottom: 1px solid #e2e8f0;

        .status-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          &.status-0 { background: #f59e0b; }
          &.status-1 { background: #10b981; }
          &.status-2 { background: #3b82f6; }
          &.status-3 { background: #ef4444; }
        }

        .status-text {
          font-size: 13px;
          font-weight: 500;
        }

        .reserve-time {
          margin-left: auto;
          font-size: 12px;
          color: #94a3b8;
        }
      }

      .reserve-content {
        padding: 16px 20px;
        display: flex;
        gap: 16px;
        align-items: center;

        .reserve-icon {
          font-size: 40px;
        }

        .reserve-info {
          flex: 1;

          .reserve-pet {
            font-size: 16px;
            font-weight: 600;
            color: #1e293b;
            margin-bottom: 4px;
          }

          .reserve-service {
            font-size: 13px;
            color: #64748b;
            margin-bottom: 4px;
          }

          .reserve-doctor {
            font-size: 12px;
            color: #94a3b8;
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }

      .reserve-footer {
        padding: 12px 20px;
        border-top: 1px solid #e2e8f0;
        text-align: right;
      }
    }
  }

  .pet-selector {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .pet-option {
      background: #f8fafc;
      border-radius: 16px;
      padding: 16px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
      border: 2px solid transparent;

      &:hover {
        background: #f1f5f9;
      }

      &.active {
        border-color: #f59e0b;
        background: #fffbeb;
      }

      .pet-avatar {
        font-size: 40px;
        color: #94a3b8;
        margin-bottom: 8px;
      }

      .pet-name {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
      }

      .pet-breed {
        font-size: 12px;
        color: #64748b;
      }
    }
  }

  .service-selector {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    margin-bottom: 20px;

    .service-option {
      background: #f8fafc;
      border-radius: 16px;
      padding: 16px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
      border: 2px solid transparent;

      &:hover {
        background: #f1f5f9;
      }

      &.active {
        border-color: #f59e0b;
        background: #fffbeb;
      }

      .service-icon {
        font-size: 32px;
        margin-bottom: 8px;
      }

      .service-name {
        font-size: 14px;
        font-weight: 500;
        color: #1e293b;
      }

      .service-price {
        font-size: 12px;
        color: #f59e0b;
        margin-top: 4px;
      }
    }
  }

  .confirm-info {
    background: #f8fafc;
    border-radius: 16px;
    padding: 16px;
    margin-bottom: 20px;

    .confirm-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #e2e8f0;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: #64748b;
        font-size: 14px;
      }

      .value {
        font-weight: 500;
        color: #1e293b;

        &.price {
          color: #f59e0b;
          font-size: 16px;
        }
      }
    }
  }

  .detail-item {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid #e2e8f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #64748b;
      font-size: 14px;
    }

    .value {
      font-weight: 500;
      color: #1e293b;
    }
  }
  .empty-pets {
    padding: 20px;
  }
}
</style>