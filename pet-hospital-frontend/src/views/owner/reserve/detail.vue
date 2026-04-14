<template>
  <div class="reserve-detail-page">
    <div class="page-nav">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div v-loading="loading">
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>预约详情</span>
            <el-tag :type="getStatusType(detail.status)" size="large">
              {{ getStatusName(detail.status) }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="预约号">{{ detail.reserveNo }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="宠物名称">{{ detail.petName }}</el-descriptions-item>
          <el-descriptions-item label="宠物品种">{{ detail.petBreed }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ getServiceTypeName(detail.serviceType) }}</el-descriptions-item>
          <el-descriptions-item label="预约医生">{{ detail.doctorName || '待分配' }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ detail.reserveTime }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detail.status)">{{ getStatusName(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="actions" v-if="detail.status === 'pending'">
          <el-button type="danger" @click="handleCancel">取消预约</el-button>
        </div>
      </el-card>

      <!-- 就诊记录（如果已完成） -->
      <el-card class="record-card" v-if="detail.status === 'completed' && medicalRecord">
        <template #header>
          <span>就诊记录</span>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="诊断结果">{{ medicalRecord.diagnosis }}</el-descriptions-item>
          <el-descriptions-item label="用药方案">{{ medicalRecord.prescription }}</el-descriptions-item>
          <el-descriptions-item label="医嘱">{{ medicalRecord.advice }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReserveList, cancelReserve } from '@/api/owner/owner'

const route = useRoute()
const router = useRouter()
const reserveId = ref(route.params.id)

const loading = ref(false)
const detail = ref({})
const medicalRecord = ref(null)

const getStatusType = (status) => {
  const types = { pending: 'warning', confirmed: 'success', completed: 'info', cancelled: 'danger' }
  return types[status] || 'info'
}

const getStatusName = (status) => {
  const names = { pending: '待确认', confirmed: '已确认', completed: '已完成', cancelled: '已取消' }
  return names[status] || status
}

const getServiceTypeName = (type) => {
  const names = { consultation: '门诊诊疗', vaccine: '疫苗接种', exam: '体检', grooming: '洗澡美容' }
  return names[type] || type
}

const goBack = () => {
  router.push('/owner/reserve')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getReserveList({ page: 1, pageSize: 100 })
    if (res.code === 200) {
      const found = res.data.records.find(item => item.id == reserveId.value)
      if (found) {
        detail.value = found
        if (found.medicalRecord) {
          medicalRecord.value = found.medicalRecord
        }
      }
    }
  } catch (error) {
    console.error('加载预约详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  ElMessageBox.confirm('确定要取消该预约吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await cancelReserve(reserveId.value)
      if (res.code === 200) {
        ElMessage.success('已取消预约')
        loadDetail()
      }
    } catch (error) {
      ElMessage.error('取消失败')
    }
  })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.reserve-detail-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-nav {
  margin-bottom: 16px;
}

.detail-card,
.record-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  margin-top: 20px;
  text-align: center;
}
</style>