<template>
  <div class="page-content">
    <!-- 头部 -->
    <div class="header" style="background: linear-gradient(135deg, #ff6b6b 0%, #f5a623 100%); padding: 40px 20px 60px; border-radius: 0 0 40px 40px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h2 style="color: white; font-size: 28px; margin-bottom: 8px;">预约申请</h2>
          <p style="color: rgba(255,255,255,0.9); font-size: 14px;">在线预约，省时省心</p>
        </div>
        <el-button circle :icon="Plus" style="background: rgba(255,255,255,0.2); border: none; color: white; width: 44px; height: 44px;" @click="handleCreate" />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div style="display: flex; gap: 12px; padding: 0 20px; margin-top: -30px;">
      <div v-for="stat in stats" :key="stat.key" 
           :style="{
             flex: 1,
             background: 'white',
             borderRadius: '16px',
             padding: '12px',
             textAlign: 'center',
             boxShadow: '0 4px 15px rgba(0,0,0,0.05)',
             cursor: 'pointer',
             border: activeTab === stat.key ? `2px solid ${stat.color}` : 'none'
           }"
           @click="activeTab = stat.key; loadReserveList()">
        <div :style="{ fontSize: '24px', fontWeight: 'bold', color: stat.color }">{{ stat.count }}</div>
        <div style="font-size: 12px; color: #999;">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 预约列表 -->
    <div style="padding: 20px;" v-loading="loading">
      <div v-for="item in reserveList" :key="item.id" 
           :style="{
             background: 'white',
             borderRadius: '20px',
             marginBottom: '15px',
             padding: '16px',
             opacity: item.status === 'cancelled' ? 0.6 : 1,
             boxShadow: '0 2px 10px rgba(0,0,0,0.03)',
             cursor: 'pointer'
           }"
           @click="viewDetail(item.id)">
        <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span :style="{ 
              display: 'inline-block',
              width: '8px', 
              height: '8px', 
              borderRadius: '50%', 
              background: getStatusColor(item.status),
              marginRight: '6px'
            }"></span>
            <span :style="{ color: getStatusColor(item.status), fontSize: '12px', fontWeight: 'bold' }">
              {{ getStatusName(item.status) }}
            </span>
          </div>
          <span style="color: #999; font-size: 12px;">{{ item.reserveTime }}</span>
        </div>
        
        <div style="display: flex; gap: 12px; margin-bottom: 12px;">
          <div style="width: 50px; height: 50px; background: #f8f9fc; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px;">
            🐾
          </div>
          <div style="flex: 1;">
            <div style="font-weight: bold; font-size: 16px;">{{ item.petName }}</div>
            <div style="color: #999; font-size: 12px; margin-top: 4px;">{{ item.serviceTypeName }}</div>
          </div>
        </div>
        
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div style="color: #999; font-size: 12px;">
            <el-icon><User /></el-icon>
            <span style="margin-left: 4px;">医生：{{ item.doctorName || '待分配' }}</span>
          </div>
          <div v-if="item.status === 'pending'" @click.stop>
            <el-button type="danger" size="small" plain @click="cancelReserve(item)">取消预约</el-button>
          </div>
          <div v-else-if="item.status === 'confirmed'" @click.stop>
            <el-button type="primary" size="small" plain @click="viewDetail(item.id)">查看详情</el-button>
          </div>
          <div v-else-if="item.status === 'completed'" @click.stop>
            <el-button type="info" size="small" plain @click="viewDetail(item.id)">查看记录</el-button>
          </div>
          <div v-else-if="item.status === 'cancelled'" @click.stop>
            <el-button type="info" size="small" plain @click="viewDetail(item.id)">查看详情</el-button>
          </div>
        </div>
      </div>

      <div v-if="!loading && reserveList.length === 0" style="text-align: center; padding: 60px 20px;">
        <div style="font-size: 60px; margin-bottom: 16px;">📅</div>
        <p style="color: #999;">暂无预约记录</p>
        <el-button type="primary" plain style="margin-top: 16px;" @click="handleCreate">立即预约</el-button>
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
      <router-link to="/reserve" class="nav-item active">
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

    <!-- 创建预约弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建预约" width="90%" style="border-radius: 20px; max-width: 400px;">
      <el-form :model="form" label-width="70px" size="small">
        <el-form-item label="选择宠物">
          <el-select v-model="form.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name" :value="pet.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类型">
          <el-select v-model="form.serviceType" placeholder="请选择服务" style="width: 100%">
            <el-option v-for="service in serviceTypes" :key="service.value" :label="service.label" :value="service.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择医生">
          <el-select v-model="form.doctorId" placeholder="请选择医生（可选）" style="width: 100%" clearable>
            <el-option v-for="doc in doctorList" :key="doc.id" :label="doc.name" :value="doc.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间">
          <el-date-picker 
            v-model="form.reserveTime" 
            type="datetime" 
            placeholder="选择日期和时间" 
            style="width: 100%"
            :disabled-date="disabledDate"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="form.remark" placeholder="选填，如有特殊需求请备注" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReserve" :loading="submitting">提交预约</el-button>
      </template>
    </el-dialog>

    <!-- 预约详情弹窗 -->
    <el-dialog v-model="detailVisible" title="预约详情" width="90%" style="border-radius: 20px; max-width: 400px;">
      <div v-if="currentDetail">
        <div style="margin-bottom: 16px;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">预约单号</span>
            <span style="font-weight: bold;">{{ currentDetail.reserveNo }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">状态</span>
            <span :style="{ color: getStatusColor(currentDetail.status) }">{{ getStatusName(currentDetail.status) }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">宠物</span>
            <span>{{ currentDetail.petName }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">服务</span>
            <span>{{ currentDetail.serviceTypeName }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">医生</span>
            <span>{{ currentDetail.doctorName || '待分配' }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">预约时间</span>
            <span>{{ currentDetail.reserveTime }}</span>
          </div>
          <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
            <span style="color: #999;">创建时间</span>
            <span>{{ currentDetail.createTime }}</span>
          </div>
          <div v-if="currentDetail.remark" style="margin-bottom: 12px;">
            <div style="color: #999; margin-bottom: 4px;">备注</div>
            <div style="background: #f5f5f5; padding: 8px; border-radius: 8px;">{{ currentDetail.remark }}</div>
          </div>
          <div v-if="currentDetail.cancelReason" style="margin-bottom: 12px;">
            <div style="color: #f56c6c; margin-bottom: 4px;">取消原因</div>
            <div style="background: #ffe8e8; padding: 8px; border-radius: 8px;">{{ currentDetail.cancelReason }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentDetail?.status === 'pending'" type="danger" @click="cancelFromDetail">取消预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Avatar, Notebook, Calendar, ChatDotRound, User } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const reserveList = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentDetail = ref(null)
const activeTab = ref('all')

// 统计数据
const stats = ref([
  { key: 'all', label: '全部', count: 0, color: '#909399' },
  { key: 'pending', label: '待确认', count: 0, color: '#f5a623' },
  { key: 'confirmed', label: '已确认', count: 0, color: '#67c23a' },
  { key: 'completed', label: '已完成', count: 0, color: '#409eff' },
  { key: 'cancelled', label: '已取消', count: 0, color: '#f56c6c' }
])

// 宠物列表
const petList = ref([
  { id: 1, name: '旺财' },
  { id: 2, name: '咪咪' }
])

// 医生列表
const doctorList = ref([
  { id: 1, name: '张医生' },
  { id: 2, name: '李医生' },
  { id: 3, name: '王医生' }
])

// 服务类型
const serviceTypes = ref([
  { value: 'consultation', label: '门诊诊疗' },
  { value: 'vaccine', label: '疫苗接种' },
  { value: 'exam', label: '体检' },
  { value: 'grooming', label: '洗澡美容' }
])

// 表单
const form = ref({
  petId: '',
  serviceType: '',
  doctorId: '',
  reserveTime: '',
  remark: ''
})

// 获取状态颜色
const getStatusColor = (status) => {
  const colors = { pending: '#f5a623', confirmed: '#67c23a', completed: '#409eff', cancelled: '#f56c6c' }
  return colors[status] || '#999'
}

// 获取状态名称
const getStatusName = (status) => {
  const names = { pending: '待确认', confirmed: '已确认', completed: '已完成', cancelled: '已取消' }
  return names[status] || status
}

// 获取服务名称
const getServiceName = (value) => {
  const service = serviceTypes.value.find(s => s.value === value)
  return service ? service.label : value
}

// 禁止选择过去的时间
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 86400000
}

// 加载预约列表
const loadReserveList = () => {
  loading.value = true
  setTimeout(() => {
    // 模拟数据
    const allReserves = [
      { id: 1, reserveNo: 'R001', petId: 1, petName: '旺财', serviceType: 'consultation', serviceTypeName: '门诊诊疗', doctorId: 1, doctorName: '张医生', reserveTime: '2024-01-20 10:00', createTime: '2024-01-15 14:30', status: 'pending', remark: '狗狗最近胃口不好' },
      { id: 2, reserveNo: 'R002', petId: 2, petName: '咪咪', serviceType: 'vaccine', serviceTypeName: '疫苗接种', doctorId: 2, doctorName: '李医生', reserveTime: '2024-01-15 14:00', createTime: '2024-01-10 09:00', status: 'confirmed', remark: '' },
      { id: 3, reserveNo: 'R003', petId: 1, petName: '旺财', serviceType: 'grooming', serviceTypeName: '洗澡美容', doctorId: 3, doctorName: '王医生', reserveTime: '2024-01-10 09:30', createTime: '2024-01-05 16:20', status: 'completed', remark: '需要修剪指甲' },
      { id: 4, reserveNo: 'R004', petId: 2, petName: '咪咪', serviceType: 'exam', serviceTypeName: '体检', doctorId: 1, doctorName: '张医生', reserveTime: '2024-01-05 11:00', createTime: '2023-12-28 10:15', status: 'cancelled', cancelReason: '临时有事' }
    ]
    
    // 根据tab筛选
    if (activeTab.value === 'all') {
      reserveList.value = allReserves
    } else {
      reserveList.value = allReserves.filter(item => item.status === activeTab.value)
    }
    
    // 更新统计数量
    stats.value = stats.value.map(stat => ({
      ...stat,
      count: stat.key === 'all' ? allReserves.length : allReserves.filter(item => item.status === stat.key).length
    }))
    
    loading.value = false
  }, 300)
}

// 创建预约
const handleCreate = () => {
  if (petList.value.length === 0) {
    ElMessage.warning('请先添加宠物')
    return
  }
  form.value = {
    petId: petList.value[0]?.id || '',
    serviceType: '',
    doctorId: '',
    reserveTime: '',
    remark: ''
  }
  dialogVisible.value = true
}

// 提交预约
const submitReserve = () => {
  if (!form.value.petId) {
    ElMessage.warning('请选择宠物')
    return
  }
  if (!form.value.serviceType) {
    ElMessage.warning('请选择服务类型')
    return
  }
  if (!form.value.reserveTime) {
    ElMessage.warning('请选择预约时间')
    return
  }
  
  submitting.value = true
  setTimeout(() => {
    const pet = petList.value.find(p => p.id === form.value.petId)
    const doctor = doctorList.value.find(d => d.id === form.value.doctorId)
    
    // 添加到列表最前面
    const newReserve = {
      id: Date.now(),
      reserveNo: 'R' + Date.now(),
      petId: form.value.petId,
      petName: pet?.name || '未知',
      serviceType: form.value.serviceType,
      serviceTypeName: getServiceName(form.value.serviceType),
      doctorId: form.value.doctorId,
      doctorName: doctor?.name,
      reserveTime: form.value.reserveTime,
      createTime: new Date().toLocaleString(),
      status: 'pending',
      remark: form.value.remark
    }
    
    reserveList.value = [newReserve, ...reserveList.value]
    ElMessage.success('预约提交成功，请等待确认')
    dialogVisible.value = false
    loadReserveList()
    submitting.value = false
  }, 500)
}

// 取消预约
const cancelReserve = (item) => {
  ElMessageBox.prompt('请输入取消原因', '取消预约', {
    confirmButtonText: '确定取消',
    cancelButtonText: '暂不取消',
    inputPlaceholder: '请填写取消原因'
  }).then(({ value }) => {
    if (value) {
      // 更新列表中的状态
      const index = reserveList.value.findIndex(r => r.id === item.id)
      if (index !== -1) {
        reserveList.value[index].status = 'cancelled'
        reserveList.value[index].cancelReason = value
      }
      ElMessage.success('已取消预约')
      loadReserveList()
    }
  }).catch(() => {})
}

// 从详情弹窗取消
const cancelFromDetail = () => {
  if (currentDetail.value) {
    cancelReserve(currentDetail.value)
    detailVisible.value = false
  }
}

// 查看详情
const viewDetail = (id) => {
  const detail = reserveList.value.find(item => item.id === id)
  if (detail) {
    currentDetail.value = detail
    detailVisible.value = true
  }
}

onMounted(() => {
  loadReserveList()
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
  color: #f5a623;
}

.nav-item .el-icon {
  font-size: 22px;
}
</style>