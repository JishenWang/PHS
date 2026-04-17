<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">预约核销与挂号管理</div>
      <div class="page__actions">
        <el-tag size="small" :type="sourceTagType">{{ sourceLabel }}</el-tag>
        <el-button type="primary" @click="openCreate">新建挂号</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page__card">
      <div class="page__section">预约核销</div>
      <el-table :data="reserveRows" v-loading="reserveLoading" height="220">
        <el-table-column prop="reserveTime" label="预约时间" min-width="170" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column label="宠物种类" min-width="90">
          <template #default="{ row }">{{ row.petSpecies || '-' }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" min-width="90" />
        <el-table-column prop="serviceType" label="服务类型" min-width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === enums.ReserveStatus.BOOKED ? 'warning' : row.status === enums.ReserveStatus.VERIFIED ? 'success' : 'info'">
              {{ reserveStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === enums.ReserveStatus.BOOKED" type="primary" link @click="doVerify(row)">核销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-form :model="query" inline label-width="72px" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:160px">
            <el-option label="待就诊" :value="enums.RegisterStatus.WAITING" />
            <el-option label="就诊中" :value="enums.RegisterStatus.IN_PROGRESS" />
            <el-option label="已完成" :value="enums.RegisterStatus.DONE" />
            <el-option label="已取消" :value="enums.RegisterStatus.CANCELED" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="query.doctorId" clearable placeholder="全部" style="width:160px">
            <el-option v-for="d in enums.doctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="挂号号/客户/宠物/手机号" clearable style="width:220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="info" @click="exportCsv">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-table :data="rows" v-loading="loading" height="480">
        <el-table-column type="index" width="56" label="#" />
        <el-table-column prop="registerNo" label="挂号编号" min-width="140" />
        <el-table-column prop="customerName" label="客户" min-width="110" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="宠物种类" min-width="90">
          <template #default="{ row }">{{ row.petSpecies || '-' }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" min-width="90" />
        <el-table-column prop="serviceType" label="服务类型" min-width="110" />
        <el-table-column prop="reason" label="主诉" min-width="140" show-overflow-tooltip />
        <el-table-column label="时间" min-width="160"><template #default="{ row }">{{ formatTime(row.visitTime) }}</template></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === enums.RegisterStatus.WAITING" type="primary" link @click="changeStatus(row, enums.RegisterStatus.IN_PROGRESS)">开始就诊</el-button>
            <el-button v-if="row.status !== enums.RegisterStatus.CANCELED && row.status !== enums.RegisterStatus.DONE" type="success" link @click="changeStatus(row, enums.RegisterStatus.DONE)">完成就诊</el-button>
            <el-button v-if="row.status !== enums.RegisterStatus.CANCELED && row.status !== enums.RegisterStatus.DONE" type="danger" link @click="changeStatus(row, enums.RegisterStatus.CANCELED)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="page__pager">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :current-page="query.page" :page-size="query.pageSize" @update:current-page="onPageChange" @update:page-size="onPageSizeChange" />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="新建挂号" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="客户" required>
          <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width:100%" @change="onCustomerChange">
            <el-option v-for="c in customers" :key="c.id" :label="`${c.name}（${c.phone}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物" required>
          <el-select v-model="form.petId" :disabled="!form.customerId" placeholder="请先选择客户" style="width:100%">
            <el-option v-for="p in currentPets" :key="p.id" :label="`${p.name}（${p.species || '未知'}）`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" required>
          <el-select v-model="form.doctorId" placeholder="请选择医生" style="width:100%">
            <el-option v-for="d in enums.doctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类型"><el-input v-model="form.serviceType" placeholder="普通门诊/疫苗/体检" /></el-form-item>
        <el-form-item label="就诊时间"><el-date-picker v-model="form.visitTime" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="主诉"><el-input v-model="form.reason" type="textarea" :rows="3" maxlength="100" show-word-limit /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { buildRegisterExportCsv, createRegister, getDeskEnums, getDoctorStatusList, getRegisterList, getReserveList, queryCustomers, updateRegisterStatus, verifyReserve } from '@/api/desk/desk'

const enums = getDeskEnums()
const loading = ref(false)
const reserveLoading = ref(false)
const rows = ref([])
const reserveRows = ref([])
const total = ref(0)
const customers = ref([])
const source = ref('mock')

const sourceLabel = computed(() => (source.value === 'api' ? '后端接口' : '本地Mock'))
const sourceTagType = computed(() => (source.value === 'api' ? 'success' : 'info'))

const query = reactive({ status: '', doctorId: '', keyword: '', page: 1, pageSize: 10 })
const createVisible = ref(false)
const saving = ref(false)
const form = reactive({ customerId: '', petId: '', doctorId: '', serviceType: '普通门诊', visitTime: new Date(), reason: '' })

const currentCustomer = computed(() => customers.value.find(c => c.id === form.customerId) || null)
const currentPets = computed(() => currentCustomer.value?.pets || [])

function statusLabel(status) {
  if (status === enums.RegisterStatus.WAITING) return '待就诊'
  if (status === enums.RegisterStatus.IN_PROGRESS) return '就诊中'
  if (status === enums.RegisterStatus.DONE) return '已完成'
  if (status === enums.RegisterStatus.CANCELED) return '已取消'
  return '-'
}
function statusTagType(status) {
  if (status === enums.RegisterStatus.WAITING) return 'warning'
  if (status === enums.RegisterStatus.IN_PROGRESS) return 'primary'
  if (status === enums.RegisterStatus.DONE) return 'success'
  return 'info'
}
function reserveStatusLabel(status) {
  if (status === enums.ReserveStatus.BOOKED) return '已预约'
  if (status === enums.ReserveStatus.VERIFIED) return '已核销'
  return '已取消'
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
    ElMessage.error(e?.message || '医生列表加载失败')
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
    ElMessage.error(e?.message || '加载失败')
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
  form.serviceType = '普通门诊'
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
  if (!form.customerId || !form.petId || !form.doctorId) return ElMessage.warning('请完整选择客户、宠物、医生')
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
    ElMessage.success('挂号成功')
    createVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '挂号失败')
  } finally {
    saving.value = false
  }
}

async function changeStatus(row, nextStatus) {
  try {
    await updateRegisterStatus(row.id, nextStatus)
    ElMessage.success('状态已更新')
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function doVerify(row) {
  try {
    await verifyReserve(row.id)
    ElMessage.success('预约核销成功')
    fetchReserve()
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '核销失败')
  }
}

function exportCsv() {
  const csv = buildRegisterExportCsv(rows.value)
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '挂号记录.csv'
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
