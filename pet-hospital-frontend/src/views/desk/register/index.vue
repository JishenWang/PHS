<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">{{ $t('register.title') }}</div>
      <div class="page__actions">
        <el-tag size="small" :type="sourceTagType">{{ sourceLabel }}</el-tag>
        <el-button type="primary" @click="openCreate">{{ $t('register.newRegistration') }}</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page__card">
      <div class="page__section">{{ $t('register.appointmentVerification') }}</div>
      <el-table :data="reserveRows" v-loading="reserveLoading" height="220">
        <el-table-column prop="reserveTime" :label="$t('register.appointmentTime')" min-width="170" />
        <el-table-column prop="customerName" :label="$t('register.customer')" min-width="100" />
        <el-table-column :label="$t('register.petSpecies')" min-width="90">
          <template #default="{ row }">{{ getSpeciesLabel(row.petSpecies) }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" :label="$t('register.doctor')" min-width="90" />
        <el-table-column :label="$t('register.serviceType')" min-width="110">
          <template #default="{ row }">{{ getServiceTypeLabel(row.serviceType) }}</template>
        </el-table-column>
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === enums.ReserveStatus.BOOKED ? 'warning' : row.status === enums.ReserveStatus.VERIFIED ? 'success' : 'info'">
              {{ reserveStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === enums.ReserveStatus.PENDING" type="warning" link @click="doConfirm(row)">{{ $t('register.confirm') }}</el-button>
            <el-button v-if="row.status === enums.ReserveStatus.BOOKED" type="primary" link @click="doVerify(row)">{{ $t('register.verify') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-form :model="query" inline label-width="72px" @submit.prevent>
        <el-form-item :label="$t('common.status')">
          <el-select v-model="query.status" clearable :placeholder="$t('common.all')" style="width:160px">
            <el-option :label="$t('register.waiting')" :value="enums.RegisterStatus.WAITING" />
            <el-option :label="$t('register.inProgress')" :value="enums.RegisterStatus.IN_PROGRESS" />
            <el-option :label="$t('register.done')" :value="enums.RegisterStatus.DONE" />
            <el-option :label="$t('register.canceled')" :value="enums.RegisterStatus.CANCELED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('register.doctor')">
          <el-select v-model="query.doctorId" clearable :placeholder="$t('common.all')" style="width:160px">
            <el-option v-for="d in enums.doctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.keyword')">
          <el-input v-model="query.keyword" :placeholder="$t('register.keywordPlaceholder')" clearable style="width:220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="onReset">{{ $t('common.reset') }}</el-button>
          <el-button type="info" @click="exportCsv">{{ $t('register.exportExcel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-table :data="rows" v-loading="loading" height="480">
        <el-table-column type="index" width="56" label="#" />
        <el-table-column prop="registerNo" :label="$t('register.regNo')" min-width="140" />
        <el-table-column prop="customerName" :label="$t('register.customer')" min-width="110" />
        <el-table-column prop="phone" :label="$t('common.phone')" min-width="130" />
        <el-table-column :label="$t('register.petSpecies')" min-width="90">
          <template #default="{ row }">{{ getSpeciesLabel(row.petSpecies) }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" :label="$t('register.doctor')" min-width="90" />
        <el-table-column :label="$t('register.serviceType')" min-width="110">
          <template #default="{ row }">{{ getServiceTypeLabel(row.serviceType) }}</template>
        </el-table-column>
        <el-table-column prop="reason" :label="$t('register.chiefComplaint')" min-width="140" show-overflow-tooltip />
        <el-table-column :label="$t('common.time')" min-width="160"><template #default="{ row }">{{ formatTime(row.visitTime) }}</template></el-table-column>
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === enums.RegisterStatus.WAITING" type="primary" link @click="changeStatus(row, enums.RegisterStatus.IN_PROGRESS)">{{ $t('register.startVisit') }}</el-button>
            <el-button v-if="row.status !== enums.RegisterStatus.CANCELED && row.status !== enums.RegisterStatus.DONE" type="success" link @click="changeStatus(row, enums.RegisterStatus.DONE)">{{ $t('register.completeVisit') }}</el-button>
            <el-button v-if="row.status !== enums.RegisterStatus.CANCELED && row.status !== enums.RegisterStatus.DONE" type="danger" link @click="changeStatus(row, enums.RegisterStatus.CANCELED)">{{ $t('common.cancel') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="page__pager">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :current-page="query.page" :page-size="query.pageSize" @update:current-page="onPageChange" @update:page-size="onPageSizeChange" />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" :title="$t('register.newRegistration')" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item :label="$t('register.customer')" required>
          <el-select v-model="form.customerId" filterable :placeholder="$t('register.selectCustomer')" style="width:100%" @change="onCustomerChange">
            <el-option v-for="c in customers" :key="c.id" :label="`${c.name}（${c.phone}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('register.pet')" required>
          <el-select v-model="form.petId" :disabled="!form.customerId" :placeholder="$t('register.selectCustomerFirst')" style="width:100%">
            <el-option v-for="p in currentPets" :key="p.id" :label="`${p.name}（${getSpeciesLabel(p.species)}）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('register.doctor')" required>
          <el-select v-model="form.doctorId" :placeholder="$t('register.selectDoctor')" style="width:100%">
            <el-option v-for="d in enums.doctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('register.serviceType')">
          <el-select v-model="form.serviceType" :placeholder="$t('register.serviceTypePlaceholder')" style="width:100%">
            <el-option v-for="opt in serviceTypeOptions" :key="opt.value" :label="t(opt.label)" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('register.visitTime')"><el-date-picker v-model="form.visitTime" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item :label="$t('register.chiefComplaint')"><el-input v-model="form.reason" type="textarea" :rows="3" maxlength="100" show-word-limit /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { buildRegisterExportCsv, confirmReserve, createRegister, getDeskEnums, getDoctorStatusList, getRegisterList, getReserveList, queryCustomers, updateRegisterStatus, verifyReserve } from '@/api/desk/desk'

const { t } = useI18n()

const enums = getDeskEnums()

// 宠物种类翻译
function getSpeciesLabel(species) {
  const key = String(species || '').toLowerCase().trim()
  const map = {
    dog: 'dashboard.typeDog',
    cat: 'dashboard.typeCat',
    rabbit: 'dashboard.typeRabbit',
    '狗': 'dashboard.typeDog',
    '猫': 'dashboard.typeCat',
    '兔': 'dashboard.typeRabbit',
  }
  return map[key] ? t(map[key]) : (species || t('dashboard.typeUnknown'))
}

// 服务类型翻译（与客户端预约保持一致）
function getServiceTypeLabel(type) {
  const key = String(type || '').toLowerCase().trim()
  const map = {
    consultation: 'reserve.outpatientClinic',
    vaccine: 'reserve.vaccination',
    exam: 'reserve.physicalExam',
    grooming: 'reserve.bathGrooming',
    '普通门诊': 'reserve.outpatientClinic',
    '疫苗接种': 'reserve.vaccination',
    '体检': 'reserve.physicalExam',
    '洗澡美容': 'reserve.bathGrooming',
    'general clinic': 'reserve.outpatientClinic',
    'vaccination': 'reserve.vaccination',
    'health check': 'reserve.physicalExam',
    'grooming': 'reserve.bathGrooming',
    'spay/neuter': 'accept.serviceSpayNeuter',
    'spay': 'accept.serviceSpayNeuter',
    'neuter': 'accept.serviceSpayNeuter',
    '绝育手术': 'accept.serviceSpayNeuter',
    'disease treatment': 'accept.serviceDiseaseTreatment',
    '疾病诊疗': 'accept.serviceDiseaseTreatment',
    'dental basic check': 'accept.serviceDentalBasicCheck',
    '口腔基础检查': 'accept.serviceDentalBasicCheck',
  }
  if (map[key]) return t(map[key])
  // 模糊匹配
  if (key.includes('consult') || key.includes('门诊') || key.includes('clinic')) return t('reserve.outpatientClinic')
  if (key.includes('vaccine') || key.includes('疫苗')) return t('reserve.vaccination')
  if (key.includes('exam') || key.includes('体检') || key.includes('check')) return t('reserve.physicalExam')
  if (key.includes('groom') || key.includes('美容') || key.includes('bath') || key.includes('洗澡')) return t('reserve.bathGrooming')
  if (key.includes('spay') || key.includes('neuter') || key.includes('绝育')) return t('accept.serviceSpayNeuter')
  if (key.includes('disease') || key.includes('疾病')) return t('accept.serviceDiseaseTreatment')
  if (key.includes('dental') || key.includes('口腔')) return t('accept.serviceDentalBasicCheck')
  return type || '-'
}

// 服务类型选项（与客户端预约保持一致：4种）
const serviceTypeOptions = [
  { label: 'reserve.outpatientClinic', value: 'consultation' },
  { label: 'reserve.vaccination', value: 'vaccine' },
  { label: 'reserve.physicalExam', value: 'exam' },
  { label: 'reserve.bathGrooming', value: 'grooming' },
]
const loading = ref(false)
const reserveLoading = ref(false)
const rows = ref([])
const reserveRows = ref([])
const total = ref(0)
const customers = ref([])
const source = ref('mock')

const sourceLabel = computed(() => (source.value === 'api' ? t('register.backendApi') : t('register.localMock')))
const sourceTagType = computed(() => (source.value === 'api' ? 'success' : 'info'))

const query = reactive({ status: '', doctorId: '', keyword: '', page: 1, pageSize: 10 })
const createVisible = ref(false)
const saving = ref(false)
const form = reactive({ customerId: '', petId: '', doctorId: '', serviceType: 'consultation', visitTime: new Date(), reason: '' })

const currentCustomer = computed(() => customers.value.find(c => c.id === form.customerId) || null)
const currentPets = computed(() => currentCustomer.value?.pets || [])

function statusLabel(status) {
  if (status === enums.RegisterStatus.WAITING) return t('register.waiting')
  if (status === enums.RegisterStatus.IN_PROGRESS) return t('register.inProgress')
  if (status === enums.RegisterStatus.DONE) return t('register.done')
  if (status === enums.RegisterStatus.CANCELED) return t('register.canceled')
  return '-'
}
function statusTagType(status) {
  if (status === enums.RegisterStatus.WAITING) return 'warning'
  if (status === enums.RegisterStatus.IN_PROGRESS) return 'primary'
  if (status === enums.RegisterStatus.DONE) return 'success'
  return 'info'
}
function reserveStatusLabel(status) {
  if (status === enums.ReserveStatus.PENDING) return t('register.pending')
  if (status === enums.ReserveStatus.BOOKED) return t('register.confirmed')
  if (status === enums.ReserveStatus.VERIFIED) return t('register.verified')
  return t('register.canceled')
}
function formatTime(v) {
  if (!v) return '-'
  const d = new Date(v)
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleString()
}

async function fetchCustomers() {
  const { data } = await queryCustomers({ page: 1, pageSize: 999 })
  customers.value = data.list || []
}

async function fetchDoctors() {
  try {
    const { data } = await getDoctorStatusList()
    const list = Array.isArray(data) ? data : []
    if (list.length > 0) {
      enums.doctors.splice(0, enums.doctors.length, ...list)
      if (!form.doctorId) {
        form.doctorId = list[0].id
      }
    }
  } catch (e) {
    ElMessage.error(e?.message || t('register.loadDoctorListFailed'))
  }
}

async function fetchReserve() {
  reserveLoading.value = true
  try {
    const { data } = await getReserveList({ page: 1, pageSize: 5 })
    reserveRows.value = data.list || []
  } finally {
    reserveLoading.value = false
  }
}

async function fetchList() {
  loading.value = true
  try {
    const { data, source: s } = await getRegisterList({ ...query })
    source.value = s
    rows.value = data.list || []
    total.value = Number(data.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || t('register.loadFailed'))
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  fetchList()
}
function onReset() {
  Object.assign(query, { status: '', doctorId: '', keyword: '', page: 1, pageSize: 10 })
  fetchList()
}
function onPageChange(p) {
  query.page = p
  fetchList()
}
function onPageSizeChange(ps) {
  query.pageSize = ps
  query.page = 1
  fetchList()
}

function resetForm() {
  form.customerId = ''
  form.petId = ''
  form.doctorId = enums.doctors?.[0]?.id || ''
  form.serviceType = 'consultation'
  form.visitTime = new Date()
  form.reason = ''
}
function openCreate() {
  resetForm()
  createVisible.value = true
}
function onCustomerChange() {
  form.petId = ''
}

async function submitCreate() {
  if (!form.customerId || !form.petId || !form.doctorId) return ElMessage.warning(t('register.selectCustomerPetDoctor'))
  const customer = customers.value.find(c => c.id === form.customerId)
  const pet = currentPets.value.find(p => p.id === form.petId)
  const doctor = enums.doctors.find(d => d.id === form.doctorId)
  saving.value = true
  try {
    await createRegister({
      customerId: form.customerId,
      customerName: customer?.name || '',
      phone: customer?.phone || '',
      petId: form.petId,
      petName: pet?.name || '',
      petSpecies: pet?.species || '',
      doctorId: form.doctorId,
      doctorName: doctor?.name || '',
      serviceType: form.serviceType,
      reason: form.reason,
      visitTime: form.visitTime instanceof Date ? form.visitTime.toISOString() : String(form.visitTime || '')
    })
    ElMessage.success(t('register.registrationSuccess'))
    createVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || t('register.registrationFailed'))
  } finally {
    saving.value = false
  }
}

async function changeStatus(row, nextStatus) {
  try {
    await updateRegisterStatus(row.id, nextStatus)
    ElMessage.success(t('register.statusUpdated'))
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || t('common.operationFailed'))
  }
}

async function doConfirm(row) {
  try {
    await confirmReserve(row.id)
    ElMessage.success(t('register.appointmentConfirmed'))
    fetchReserve()
  } catch (e) {
    ElMessage.error(e?.message || t('register.confirmationFailed'))
  }
}

async function doVerify(row) {
  try {
    await verifyReserve(row.id)
    ElMessage.success(t('register.appointmentVerified'))
    fetchReserve()
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || t('register.verificationFailed'))
  }
}

function exportCsv() {
  const csv = buildRegisterExportCsv(rows.value)
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'registration_records.csv'
  link.click()
  URL.revokeObjectURL(link.href)
}

onMounted(async () => {
  await fetchDoctors()
  await fetchCustomers()
  await fetchReserve()
  await fetchList()
})
</script>

<style scoped>
.page__header,.page__actions,.page__pager{display:flex;align-items:center}
.page__header{justify-content:space-between;margin-bottom:12px}
.page__actions{gap:8px}
.page__title{font-size:16px;font-weight:600;color:#111827}
.page__section{font-weight:600;margin-bottom:8px;color:#111827}
.page__card{margin-bottom:12px}
.page__pager{margin-top:12px;justify-content:flex-end}
</style>
