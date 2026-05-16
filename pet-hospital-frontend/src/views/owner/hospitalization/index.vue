<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">{{ t('hospitalization.myHospitalization') }}</div>
    </div>

    <el-card shadow="never" class="page__card">
      <el-empty v-if="!loading && rows.length === 0" :description="t('hospitalization.noRecords')" />
      <el-timeline v-else>
        <el-timeline-item
          v-for="row in rows"
          :key="row.id"
          :type="row.status === 0 ? 'warning' : 'success'"
          :hollow="row.status === 0"
        >
          <el-card shadow="hover" class="timeline-card">
            <div class="hosp-header">
              <div class="hosp-no">{{ row.hospNo }}</div>
              <el-tag :type="row.status === 0 ? 'warning' : 'success'" effect="light" round size="small">
                {{ row.status === 0 ? t('hospitalization.statusAdmitted') : t('hospitalization.statusDischarged') }}
              </el-tag>
            </div>
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item :label="t('hospitalization.pet')">{{ row.petName }}</el-descriptions-item>
              <el-descriptions-item :label="t('hospitalization.doctor')">{{ row.doctorName }}</el-descriptions-item>
              <el-descriptions-item :label="t('hospitalization.admissionTime')">{{ formatDate(row.admissionTime) }}</el-descriptions-item>
              <el-descriptions-item :label="t('hospitalization.expectedDays')">{{ row.expectedDays }}{{ t('hospitalization.days') }}</el-descriptions-item>
              <el-descriptions-item v-if="row.actualDays" :label="t('hospitalization.actualDays')">{{ row.actualDays }}{{ t('hospitalization.days') }}</el-descriptions-item>
              <el-descriptions-item v-if="row.status === 1" :label="t('hospitalization.totalAmount')">
                <span style="color:#f56c6c;font-weight:600">¥ {{ row.totalAmount }}</span>
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="row.status === 1" class="hosp-footer">
              <el-tag :type="row.payStatus === 1 ? 'success' : 'info'" size="small">
                {{ row.payStatus === 1 ? t('hospitalization.paid') : t('hospitalization.unpaid') }}
              </el-tag>
            </div>

            <!-- 处方信息 -->
            <div v-if="row.prescriptions && row.prescriptions.length > 0" class="prescription-section">
              <div class="section-title">{{ t('hospitalization.prescriptionList') }}</div>
              <el-table :data="row.prescriptions" size="small" border>
                <el-table-column prop="prescriptionNo" :label="t('prescription.prescriptionNo')" width="160" />
                <el-table-column prop="diagnosis" :label="t('prescription.diagnosis')" min-width="120" show-overflow-tooltip />
                <el-table-column prop="serviceNames" :label="t('prescription.services')" min-width="150" show-overflow-tooltip />
                <el-table-column prop="totalAmount" :label="t('prescription.amount')" width="100" align="right">
                  <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="createTime" :label="t('common.time')" width="150" />
              </el-table>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getHospitalizationPage, getHospitalizationPrescriptions } from '@/api/common/hospitalization.js'

const { t } = useI18n()
const userStore = useUserStore()

const loading = ref(false)
const rows = ref([])

function formatDate(str) {
  if (!str) return '-'
  const d = new Date(str)
  if (isNaN(d.getTime())) return str
  return d.toLocaleString('zh-CN', { hour12: false })
}

async function loadList() {
  loading.value = true
  try {
    const ownerId = userStore.userInfo?.id
    const res = await getHospitalizationPage({
      current: 1,
      size: 100,
      ownerId
    })
    if (res.code === 200) {
      const list = res.data.records || []
      // 为每条住院记录加载处方
      for (const item of list) {
        try {
          const presRes = await getHospitalizationPrescriptions(item.id)
          if (presRes.code === 200) {
            item.prescriptions = presRes.data || []
          }
        } catch (e) {
          item.prescriptions = []
        }
      }
      rows.value = list
    } else {
      ElMessage.error(res.msg || t('hospitalization.loadFailed'))
    }
  } catch (e) {
    ElMessage.error(t('hospitalization.loadFailed'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped lang="scss">
.page {
  padding: 20px;
}
.page__header {
  margin-bottom: 16px;
}
.page__title {
  font-size: 20px;
  font-weight: 600;
}
.page__card {
  margin-bottom: 16px;
}
.timeline-card {
  margin-bottom: 8px;
}
.hosp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.hosp-no {
  font-size: 16px;
  font-weight: 600;
}
.hosp-footer {
  margin-top: 12px;
  text-align: right;
}
.prescription-section {
  margin-top: 16px;
  .section-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 8px;
  }
}
</style>
