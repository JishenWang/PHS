<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">{{ t('hospitalization.doctorTitle') }}</div>
    </div>

    <el-card shadow="never" class="page__card">
      <el-form :model="query" inline label-width="80px" @submit.prevent>
        <el-form-item :label="t('common.status')">
          <el-select v-model="query.status" clearable style="width: 140px" :placeholder="t('common.all')">
            <el-option :label="t('hospitalization.statusAdmitted')" :value="0" />
            <el-option :label="t('hospitalization.statusDischarged')" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">{{ t('common.search') }}</el-button>
          <el-button @click="onReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-table :data="rows" v-loading="loading" height="520">
        <el-table-column type="index" width="58" label="#" />
        <el-table-column prop="hospNo" :label="t('hospitalization.hospNo')" min-width="150" />
        <el-table-column prop="ownerName" :label="t('hospitalization.owner')" min-width="100" />
        <el-table-column prop="petName" :label="t('hospitalization.pet')" min-width="100" />
        <el-table-column prop="expectedDays" :label="t('hospitalization.expectedDays')" width="100" align="center" />
        <el-table-column prop="actualDays" :label="t('hospitalization.actualDays')" width="100" align="center">
          <template #default="{ row }">{{ row.actualDays ?? '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'" effect="light" round>
              {{ row.status === 0 ? t('hospitalization.statusAdmitted') : t('hospitalization.statusDischarged') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operation')" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link size="small" @click="openService(row)">
              {{ t('hospitalization.addService') }}
            </el-button>
            <el-button type="primary" link size="small" @click="viewDetail(row)">{{ t('common.detail') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page__pager">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :current-page="query.page" :page-size="query.pageSize" @update:current-page="onPageChange" @update:page-size="onPageSizeChange" />
      </div>
    </el-card>

    <!-- 添加服务对话框 -->
    <el-dialog v-model="serviceVisible" :title="t('hospitalization.addService')" width="760px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item :label="t('prescription.diagnosis')">
          <el-input v-model="serviceForm.diagnosis" :placeholder="t('prescription.placeholderDiagnosisResult')" />
        </el-form-item>
      </el-form>

      <div class="section-title">{{ t('prescription.serviceItems') }}</div>
      <el-table :data="serviceForm.services" border size="small">
        <el-table-column type="index" width="40" />
        <el-table-column :label="t('prescription.itemName')" min-width="200">
          <template #default="{ row, $index }">
            <el-select v-model="row.serviceId" filterable :placeholder="t('prescription.selectService')" @change="(val) => onServiceChange(val, $index)" size="small">
              <el-option v-for="s in serviceOptions" :key="s.serviceId" :label="s.serviceName" :value="s.serviceId">
                <span>{{ s.serviceName }}</span>
                <span style="float:right;color:#999;font-size:12px">¥{{ s.price }}</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="t('prescription.quantity')" width="90">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="99" size="small" style="width:70px" />
          </template>
        </el-table-column>
        <el-table-column :label="t('prescription.unitPrice')" width="100">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column :label="t('prescription.amount')" width="100">
          <template #default="{ row }">¥{{ ((row.price || 0) * (row.quantity || 1)).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.operation')" width="60">
          <template #default="{ $index }">
            <el-button type="danger" link size="small" @click="removeService($index)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button type="primary" class="add-btn" @click="addService" size="small">
        <el-icon><Plus /></el-icon>{{ t('prescription.addService') }}
      </el-button>

      <div class="total-section">
        <span class="label">{{ t('prescription.totalAmountLabel') }}</span>
        <span class="amount">¥{{ serviceTotalAmount.toFixed(2) }}</span>
      </div>

      <template #footer>
        <el-button @click="serviceVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="serviceSaving" @click="submitService">{{ t('prescription.submitPrescription') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { Plus } from '@element-plus/icons-vue'
import { getHospitalizationPage, getHospitalizationPrescriptions } from '@/api/common/hospitalization.js'
import { prescriptionModule } from '@/api/doctor/doctor.js'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const serviceVisible = ref(false)
const serviceSaving = ref(false)
const currentRow = ref(null)
const serviceOptions = ref([])

const query = reactive({
  page: 1,
  pageSize: 10,
  status: undefined
})

const serviceForm = reactive({
  diagnosis: '',
  services: []
})

const serviceTotalAmount = computed(() => {
  return serviceForm.services.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 1), 0)
})

async function loadList() {
  loading.value = true
  try {
    const doctorId = userStore.userInfo?.doctorId || userStore.userInfo?.id
    const res = await getHospitalizationPage({
      current: query.page,
      size: query.pageSize,
      status: query.status,
      doctorId
    })
    if (res.code === 200) {
      rows.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.msg || t('hospitalization.loadFailed'))
    }
  } catch (e) {
    ElMessage.error(t('hospitalization.loadFailed'))
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  loadList()
}

function onReset() {
  query.page = 1
  query.pageSize = 10
  query.status = undefined
  loadList()
}

function onPageChange(val) {
  query.page = val
  loadList()
}

function onPageSizeChange(val) {
  query.pageSize = val
  query.page = 1
  loadList()
}

async function viewDetail(row) {
  try {
    console.log('viewDetail row.id:', row.id)
    const res = await getHospitalizationPrescriptions(row.id)
    console.log('getHospitalizationPrescriptions res:', res)
    if (res.code === 200 && res.data && res.data.length > 0) {
      // 跳转到最新的处方详情
      const latest = res.data[0]
      router.push({ path: '/doctor/prescription/detail', query: { prescriptionId: latest.id } })
    } else {
      ElMessage.info(t('hospitalization.noPrescriptionYet'))
    }
  } catch (e) {
    console.error('viewDetail error:', e)
    ElMessage.error(t('common.loadFailed'))
  }
}

async function loadServiceOptions() {
  try {
    const res = await prescriptionModule.getServiceList()
    if (res.code === 200 && Array.isArray(res.data)) {
      serviceOptions.value = res.data
    }
  } catch (e) {
    console.error('load services failed', e)
  }
}

function openService(row) {
  currentRow.value = row
  serviceForm.diagnosis = ''
  serviceForm.services = []
  loadServiceOptions()
  serviceVisible.value = true
}

function addService() {
  serviceForm.services.push({ serviceId: null, serviceName: '', quantity: 1, price: 0 })
}

function removeService(index) {
  serviceForm.services.splice(index, 1)
}

function onServiceChange(val, index) {
  const service = serviceOptions.value.find(s => s.serviceId === val)
  if (service) {
    serviceForm.services[index].serviceName = service.serviceName
    serviceForm.services[index].price = service.price || 0
  }
}

async function submitService() {
  if (!serviceForm.diagnosis.trim()) {
    ElMessage.warning(t('prescription.pleaseEnterDiagnosisResult'))
    return
  }
  const validServices = serviceForm.services.filter(s => s.serviceId)
  if (validServices.length === 0) {
    ElMessage.warning(t('prescription.pleaseAddDrugOrService'))
    return
  }
  const doctorId = userStore.userInfo?.doctorId || userStore.userInfo?.id || ''
  const submitData = {
    hospitalizationId: currentRow.value.id,
    petId: currentRow.value.petId,
    doctorId: Number(doctorId),
    prescriptionType: 1,
    diagnosis: serviceForm.diagnosis.trim(),
    totalAmount: serviceTotalAmount.value,
    remark: '',
    drugs: [],
    services: validServices.map(item => ({
      serviceId: item.serviceId,
      serviceName: item.serviceName,
      quantity: item.quantity || 1,
      price: item.price || 0,
      amount: (item.price || 0) * (item.quantity || 1)
    }))
  }
  serviceSaving.value = true
  try {
    const createRes = await prescriptionModule.createPrescription(submitData)
    if (createRes.code !== 200) {
      ElMessage.error(createRes.msg || t('prescription.failedCreatePrescription'))
      return
    }
    const prescriptionId = createRes.data?.prescriptionId
    if (prescriptionId) {
      await prescriptionModule.submitPrescription(prescriptionId)
    }
    ElMessage.success(t('prescription.prescriptionSubmittedSuccess'))
    serviceVisible.value = false
  } catch (e) {
    ElMessage.error(t('common.operationFailed'))
  } finally {
    serviceSaving.value = false
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page__title {
  font-size: 20px;
  font-weight: 600;
}
.page__card {
  margin-bottom: 16px;
}
.page__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  margin: 16px 0 8px;
}
.add-btn {
  margin-top: 8px;
}
.total-section {
  margin-top: 16px;
  text-align: right;
  .label {
    font-size: 14px;
    margin-right: 8px;
  }
  .amount {
    font-size: 20px;
    font-weight: 600;
    color: #f56c6c;
  }
}
</style>
