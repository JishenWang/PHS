<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">{{ t('hospitalization.deskTitle') }}</div>
      <div class="page__actions">
        <el-button type="primary" @click="openCreate">{{ t('hospitalization.newAdmission') }}</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page__card">
      <el-form :model="query" inline label-width="80px" @submit.prevent>
        <el-form-item :label="t('common.status')">
          <el-select v-model="query.status" clearable style="width: 140px" :placeholder="t('common.all')">
            <el-option :label="t('hospitalization.statusAdmitted')" :value="0" />
            <el-option :label="t('hospitalization.statusDischarged')" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('hospitalization.hospNo')">
          <el-input v-model="query.hospNo" clearable style="width: 180px" />
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
        <el-table-column prop="doctorName" :label="t('hospitalization.doctor')" min-width="100" />
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
        <el-table-column :label="t('hospitalization.payStatus')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 1 ? 'success' : 'info'" effect="light" size="small">
              {{ row.payStatus === 1 ? t('hospitalization.paid') : t('hospitalization.unpaid') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operation')" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="onInterimCharge(row)">
              {{ t('hospitalization.interimCharge') }}
            </el-button>
            <el-button v-if="row.status === 0" type="primary" link size="small" @click="onDischarge(row)">
              {{ t('hospitalization.discharge') }}
            </el-button>
            <el-button type="primary" link size="small" @click="onView(row)">{{ t('common.detail') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page__pager">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :current-page="query.page" :page-size="query.pageSize" @update:current-page="onPageChange" @update:page-size="onPageSizeChange" />
      </div>
    </el-card>

    <!-- 新建住院 -->
    <el-dialog v-model="createVisible" :title="t('hospitalization.newAdmission')" width="560px" destroy-on-close>
      <el-form :model="form" label-width="110px" :rules="formRules" ref="formRef">
        <el-form-item :label="t('hospitalization.customer')" prop="ownerId">
          <el-select-v2
            v-model="form.ownerId"
            :options="customerOptions"
            :placeholder="t('hospitalization.selectCustomer')"
            filterable
            clearable
            style="width: 100%"
            @change="onCustomerChange"
          />
        </el-form-item>
        <el-form-item :label="t('hospitalization.pet')" prop="petId">
          <el-select v-model="form.petId" :placeholder="t('hospitalization.selectPet')" style="width: 100%" clearable :disabled="!form.ownerId">
            <el-option v-for="p in petOptions" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('hospitalization.doctor')" prop="doctorId">
          <el-select v-model="form.doctorId" :placeholder="t('hospitalization.selectDoctor')" style="width: 100%" clearable filterable>
            <el-option v-for="d in doctorOptions" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('hospitalization.cage')" prop="cageId">
          <el-select v-model="form.cageId" :placeholder="t('hospitalization.selectCage')" style="width: 100%" clearable filterable>
            <el-option v-for="c in cageOptions" :key="c.id" :label="`${c.cageNo} (${c.area})`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('hospitalization.expectedDays')" prop="expectedDays">
          <el-input-number v-model="form.expectedDays" :min="1" :max="365" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="t('hospitalization.bedFeePerDay')" prop="bedFeePerDay">
          <el-input-number v-model="form.bedFeePerDay" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 出院结算 -->
    <el-dialog v-model="dischargeVisible" :title="t('hospitalization.dischargeTitle')" width="480px">
      <el-form :model="dischargeForm" label-width="110px">
        <el-form-item :label="t('hospitalization.hospNo')"><span>{{ currentRow?.hospNo }}</span></el-form-item>
        <el-form-item :label="t('hospitalization.actualDays')"><span>{{ actualDays }}</span></el-form-item>
        <el-form-item :label="t('hospitalization.bedFeePerDay')">
          <el-input-number v-model="dischargeForm.bedFeePerDay" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="t('hospitalization.totalBedFee')"><span style="color:#f56c6c;font-weight:600">¥ {{ totalBedFee }}</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dischargeVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submitDischarge">{{ t('hospitalization.confirmDischarge') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryCustomers } from '@/api/desk/desk.js'
import { getDoctorStatusList } from '@/api/desk/desk.js'
import { getAvailableCages } from '@/api/admin/cage.js'
import { createHospitalization, getHospitalizationPage, dischargeHospitalization, interimCharge } from '@/api/common/hospitalization.js'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const createVisible = ref(false)
const dischargeVisible = ref(false)
const currentRow = ref(null)
const formRef = ref(null)

const customerOptions = ref([])
const petOptions = ref([])
const doctorOptions = ref([])
const cageOptions = ref([])

const query = reactive({
  page: 1,
  pageSize: 10,
  status: undefined,
  hospNo: ''
})

const form = reactive({
  ownerId: undefined,
  petId: undefined,
  doctorId: undefined,
  cageId: undefined,
  expectedDays: 1,
  bedFeePerDay: 50.00
})

const dischargeForm = reactive({
  bedFeePerDay: 50.00
})

const formRules = {
  ownerId: [{ required: true, message: () => t('hospitalization.selectCustomer'), trigger: 'change' }],
  petId: [{ required: true, message: () => t('hospitalization.selectPet'), trigger: 'change' }],
  doctorId: [{ required: true, message: () => t('hospitalization.selectDoctor'), trigger: 'change' }],
  cageId: [{ required: true, message: () => t('hospitalization.selectCage'), trigger: 'change' }],
  expectedDays: [{ required: true, message: () => t('hospitalization.enterExpectedDays'), trigger: 'blur' }],
  bedFeePerDay: [{ required: true, message: () => t('hospitalization.enterBedFee'), trigger: 'blur' }]
}

const actualDays = computed(() => {
  if (!currentRow.value || !currentRow.value.admissionTime) return 1
  const admission = new Date(currentRow.value.admissionTime)
  const now = new Date()
  const diff = Math.ceil((now - admission) / (1000 * 60 * 60 * 24))
  return diff < 1 ? 1 : diff
})

const totalBedFee = computed(() => {
  const fee = Number(dischargeForm.bedFeePerDay || 0)
  return (fee * actualDays.value).toFixed(2)
})

async function loadList() {
  loading.value = true
  try {
    const res = await getHospitalizationPage({
      current: query.page,
      size: query.pageSize,
      status: query.status,
      hospNo: query.hospNo || undefined
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
  query.hospNo = ''
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

async function loadCustomers() {
  try {
    const res = await queryCustomers({ page: 1, pageSize: 500 })
    const list = res.data?.records || res.data?.list || res.data || []
    customerOptions.value = list.map(c => ({
      value: c.id,
      label: `${c.name || c.realName || ''} (${c.phone || ''})`,
      pets: c.pets || []
    }))
  } catch (e) {
    console.error('load customers failed', e)
  }
}

async function loadDoctors() {
  try {
    const res = await getDoctorStatusList()
    const list = res.data || []
    doctorOptions.value = list.map(d => ({ id: d.id, name: d.name || d.realName || 'Doctor' }))
  } catch (e) {
    console.error('load doctors failed', e)
  }
}

async function loadCages() {
  try {
    const res = await getAvailableCages()
    if (res.code === 200) {
      cageOptions.value = res.data || []
    }
  } catch (e) {
    console.error('load cages failed', e)
  }
}

function onCustomerChange(val) {
  form.petId = undefined
  const customer = customerOptions.value.find(c => c.value === val)
  petOptions.value = customer?.pets?.map(p => ({ id: p.id, name: p.name })) || []
}

function openCreate() {
  form.ownerId = undefined
  form.petId = undefined
  form.doctorId = undefined
  form.cageId = undefined
  form.expectedDays = 1
  form.bedFeePerDay = 50.00
  petOptions.value = []
  loadCustomers()
  loadDoctors()
  loadCages()
  createVisible.value = true
}

async function submitCreate() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const res = await createHospitalization({ ...form })
      if (res.code === 200) {
        ElMessage.success(t('hospitalization.createSuccess'))
        createVisible.value = false
        loadList()
      } else {
        ElMessage.error(res.msg || t('common.operationFailed'))
      }
    } catch (e) {
      ElMessage.error(t('common.operationFailed'))
    } finally {
      saving.value = false
    }
  })
}

function onDischarge(row) {
  currentRow.value = row
  dischargeForm.bedFeePerDay = Number(row.bedFeePerDay || 50)
  dischargeVisible.value = true
}

function onView(row) {
  // 查看详情，可后续扩展
  ElMessage.info(t('hospitalization.hospNo') + ': ' + row.hospNo)
}

async function onInterimCharge(row) {
  try {
    await ElMessageBox.confirm(
      t('hospitalization.interimChargeConfirm', { no: row.hospNo }),
      t('common.tip'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    saving.value = true
    const res = await interimCharge(row.id)
    if (res.code === 200) {
      const data = res.data
      ElMessage.success(t('hospitalization.interimChargeSuccess', { amount: data.payableAmount }))
      loadList()
    } else {
      ElMessage.error(res.msg || t('common.operationFailed'))
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('common.operationFailed'))
    }
  } finally {
    saving.value = false
  }
}

async function submitDischarge() {
  if (!currentRow.value) return
  saving.value = true
  try {
    const res = await dischargeHospitalization(currentRow.value.id, dischargeForm.bedFeePerDay)
    if (res.code === 200) {
      ElMessage.success(t('hospitalization.dischargeSuccess'))
      dischargeVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.msg || t('common.operationFailed'))
    }
  } catch (e) {
    ElMessage.error(t('common.operationFailed'))
  } finally {
    saving.value = false
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
</style>
