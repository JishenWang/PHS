<template>
  <div class="reserve-detail-page">
    <div class="page-nav">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('reserveDetail.back') }}
      </el-button>
    </div>

    <div v-loading="loading">
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('reserveDetail.appointmentDetails') }}</span>
            <el-tag :type="getStatusType(detail.status)" size="large">
              {{ getStatusName(detail.status) }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('reserveDetail.appointmentNo')">{{ detail.reserveNo || detail.appointmentNo }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.createdAt')">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.petName')">{{ detail.petName }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.petBreed')">{{ detail.petBreed }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.serviceType')">{{ getServiceTypeName(detail.serviceType) }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.doctor')">{{ detail.doctorName || t('reserveDetail.toBeAssigned') }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.appointmentTime')">{{ detail.reserveTime || detail.appointmentTime }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.status')">
            <el-tag :type="getStatusType(detail.status)">{{ getStatusName(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.remarks')" :span="2">{{ detail.remark || t('reserveDetail.none') }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" :label="$t('reserveDetail.cancelReason')" :span="2">
            <span style="color: #ef4444;">{{ detail.cancelReason }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="actions" v-if="detail.status === '0' || detail.status === 'pending'">
          <el-button type="danger" @click="handleCancel">{{ $t('reserveDetail.cancelAppointment') }}</el-button>
        </div>
      </el-card>

      <!-- 就诊记录（如果已完成） -->
      <el-card class="record-card" v-if="(detail.status === '2' || detail.status === 'completed') && medicalRecord">
        <template #header>
          <span>{{ $t('reserveDetail.medicalRecord') }}</span>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="$t('reserveDetail.diagnosis')">{{ medicalRecord.diagnosis }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.prescription')">{{ medicalRecord.prescription }}</el-descriptions-item>
          <el-descriptions-item :label="$t('reserveDetail.doctorAdvice')">{{ medicalRecord.advice }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getReserveList, cancelReserve } from '@/api/owner/owner'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const reserveId = ref(route.params.id)

const loading = ref(false)
const detail = ref({})
const medicalRecord = ref(null)

// 统一使用数字字符串状态映射，与 index.vue 保持一致
const getStatusType = (status) => {
  const types = {
    '0': 'warning',
    '1': 'success',
    '2': 'info',
    '3': 'danger',
    'pending': 'warning',
    'confirmed': 'success',
    'completed': 'info',
    'cancelled': 'danger'
  }
  return types[status] || 'info'
}

const getStatusName = (status) => {
  const names = {
    '0': t('reserveDetail.pending'),
    '1': t('reserveDetail.confirmed'),
    '2': t('reserveDetail.completed'),
    '3': t('reserveDetail.cancelled'),
    'pending': t('reserveDetail.pending'),
    'confirmed': t('reserveDetail.confirmed'),
    'completed': t('reserveDetail.completed'),
    'cancelled': t('reserveDetail.cancelled')
  }
  return names[status] || status || t('reserveDetail.unknownStatus')
}

const getServiceTypeName = (type) => {
  const names = { consultation: t('reserveDetail.outpatientClinic'), vaccine: t('reserveDetail.vaccination'), exam: t('reserveDetail.physicalExam'), grooming: t('reserveDetail.bathGrooming') }
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
      const records = res.data?.records || res.data?.data || []
      const found = records.find(item => item.id == reserveId.value)
      if (found) {
        detail.value = found
        if (found.medicalRecord) {
          medicalRecord.value = found.medicalRecord
        }
      }
    }
  } catch (error) {
    console.error(t('reserveDetail.failedToLoadDetails'), error)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  ElMessageBox.prompt(t('reserveDetail.pleaseEnterCancelReason'), t('reserveDetail.cancelConfirm'), {
    confirmButtonText: t('reserveDetail.confirmCancel'),
    cancelButtonText: t('reserveDetail.notNow'),
    inputPlaceholder: t('reserveDetail.cancelPlaceholder')
  }).then(async ({ value }) => {
    if (value) {
      try {
        const res = await cancelReserve(reserveId.value, { cancelReason: value })
        if (res.code === 200) {
          ElMessage.success(t('reserveDetail.appointmentCancelled'))
          loadDetail()
        }
      } catch (error) {
        ElMessage.error(t('reserveDetail.cancellationFailed'))
      }
    }
  }).catch(() => {})
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
