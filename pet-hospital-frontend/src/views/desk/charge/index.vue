<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">{{ $t('charge.title') }}</div>
    </div>

    <el-card shadow="never" class="page__card">
      <el-form :model="query" inline label-width="72px" @submit.prevent>
        <el-form-item :label="$t('common.status')">
          <el-select v-model="query.status" clearable :placeholder="$t('common.all')" style="width:170px">
            <el-option :label="$t('charge.pending')" :value="enums.ChargeStatus.PENDING" />
            <el-option :label="$t('charge.paid')" :value="enums.ChargeStatus.PAID" />
            <el-option :label="$t('charge.refunding')" :value="enums.ChargeStatus.REFUNDING" />
            <el-option :label="$t('charge.refunded')" :value="enums.ChargeStatus.REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.keyword')"><el-input v-model="query.keyword" clearable style="width:260px" :placeholder="$t('charge.keywordPlaceholder')" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="onReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-table :data="rows" v-loading="loading" height="420">
        <el-table-column type="index" width="56" label="#" />
        <el-table-column prop="orderNo" :label="$t('charge.orderNo')" min-width="140" />
        <el-table-column prop="registerNo" :label="$t('charge.regNo')" min-width="130" />
        <el-table-column prop="customerName" :label="$t('register.customer')" min-width="90" />
        <el-table-column :label="$t('register.petSpecies')" min-width="80">
          <template #default="{ row }">{{ getSpeciesLabel(row.petSpecies) }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" :label="$t('register.doctor')" min-width="90" />
        <el-table-column :label="$t('charge.receivable')" width="100"><template #default="{ row }">¥{{ Number(row.total || 0).toFixed(2) }}</template></el-table-column>
        <el-table-column :label="$t('charge.paymentMethod')" width="100"><template #default="{ row }">{{ getPayMethodLabel(row.payMethod) }}</template></el-table-column>
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paidAt" :label="$t('charge.chargeTime')" min-width="170" />
        <el-table-column :label="$t('common.operation')" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">{{ $t('charge.feeDetails') }}</el-button>
            <el-button v-if="row.status === enums.ChargeStatus.PENDING" type="success" link @click="doConfirm(row)">{{ $t('charge.confirmCharge') }}</el-button>
            <el-button v-if="row.status === enums.ChargeStatus.PAID" type="warning" link @click="doRefund(row)">{{ $t('charge.refundRequest') }}</el-button>
            <el-button type="info" link @click="printReceipt(row)">{{ $t('charge.printReceipt') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page__pager">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :current-page="query.page" :page-size="query.pageSize" @update:current-page="onPageChange" @update:page-size="onPageSizeChange" />
      </div>
    </el-card>

    <el-card shadow="never" class="page__card">
      <div class="page__section">{{ $t('charge.todayHistoricalOrders') }}</div>
      <el-table :data="orderRows" height="220">
        <el-table-column prop="orderNo" :label="$t('charge.orderNo')" min-width="150" />
        <el-table-column prop="customerName" :label="$t('register.customer')" min-width="100" />
        <el-table-column prop="doctorName" :label="$t('register.doctor')" min-width="100" />
        <el-table-column :label="$t('charge.paymentMethod')" width="100">
          <template #default="{ row }">{{ getPayMethodLabel(row.payMethod) }}</template>
        </el-table-column>
        <el-table-column :label="$t('charge.amount')" width="100"><template #default="{ row }">¥{{ Number(row.total || 0).toFixed(2) }}</template></el-table-column>
        <el-table-column prop="createdAt" :label="$t('common.time')" min-width="170" />
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" :title="$t('charge.feeCalculationSettlement')" width="700px">
      <el-form :model="editForm" label-width="96px">
        <el-form-item :label="$t('charge.orderNo')"><el-input v-model="editForm.orderNo" disabled /></el-form-item>
        <el-form-item :label="$t('charge.feeDetailsLabel')">
          <div class="charge-items">
            <div v-for="(x, idx) in editForm.detail" :key="idx" class="charge-item">
              <el-input :model-value="getItemNameLabel(x.name)" :placeholder="$t('charge.itemNamePlaceholder')" style="width: 45%" @update:model-value="x.name = $event" />
              <el-input-number v-model="x.amount" :min="0" :step="10" style="width: 45%" />
              <el-button type="danger" link @click="removeDetail(idx)">{{ $t('common.delete') }}</el-button>
            </div>
            <el-button type="primary" link @click="addDetail">{{ $t('charge.addItem') }}</el-button>
          </div>
        </el-form-item>
        <el-form-item :label="$t('charge.discount')"><el-input-number v-model="editForm.discount" :min="0" :step="5" style="width:100%" /></el-form-item>
        <el-form-item :label="$t('charge.reduction')"><el-input-number v-model="editForm.reduction" :min="0" :step="5" style="width:100%" /></el-form-item>
        <el-form-item :label="$t('charge.adjustAmount')"><el-input-number v-model="editForm.adjustAmount" :step="1" style="width:100%" /></el-form-item>
        <el-form-item :label="$t('charge.adjustReason')"><el-input v-model="editForm.adjustReason" :placeholder="$t('charge.adjustReasonPlaceholder')" /></el-form-item>
        <el-alert :closable="false" type="info" :title="alertTitle" />
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="confirmVisible" :title="$t('charge.confirmChargeTitle')" width="420px">
      <el-form label-width="88px">
        <el-form-item :label="$t('charge.paymentMethod')">
          <el-select v-model="confirmPayMethod" :placeholder="$t('charge.selectPaymentMethod')" style="width:100%">
            <el-option v-for="m in enums.payMethods" :key="m" :label="getPayMethodLabel(m)" :value="m" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitConfirmCharge">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applyRefund, buildChargeExportText, confirmCharge, getChargeList, getDeskEnums, getOrderList, updateChargePricing } from '@/api/desk/desk'

const { t } = useI18n()

const enums = getDeskEnums()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const orderRows = ref([])

const query = reactive({ status: '', keyword: '', page: 1, pageSize: 10 })

function statusLabel(status) {
  if (status === enums.ChargeStatus.PENDING) return t('charge.pending')
  if (status === enums.ChargeStatus.PAID) return t('charge.paid')
  if (status === enums.ChargeStatus.REFUNDING) return t('charge.refunding')
  if (status === enums.ChargeStatus.REFUNDED) return t('charge.refunded')
  return '-'
}
function statusTagType(status) {
  if (status === enums.ChargeStatus.PENDING) return 'warning'
  if (status === enums.ChargeStatus.PAID) return 'success'
  if (status === enums.ChargeStatus.REFUNDING) return 'danger'
  return 'info'
}

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
  return map[key] ? t(map[key]) : (species || '-')
}

// 支付方式翻译
function getPayMethodLabel(method) {
  const key = String(method || '').toLowerCase().trim()
  const map = {
    cash: 'order.cash',
    wechat: 'order.wechatPay',
    alipay: 'order.alipay',
    card: 'order.card',
    '现金': 'order.cash',
    '微信支付': 'order.wechatPay',
    '微信': 'order.wechatPay',
    '支付宝': 'order.alipay',
    '刷卡': 'order.card',
  }
  if (map[key]) return t(map[key])
  return method || '-'
}

// 服务类型/项目名称翻译（用于费用明细）
function getItemNameLabel(name) {
  const key = String(name || '').toLowerCase().trim()
  const map = {
    'general clinic': 'reserve.outpatientClinic',
    'consultation': 'reserve.outpatientClinic',
    'vaccination': 'reserve.vaccination',
    'vaccine': 'reserve.vaccination',
    'health check': 'reserve.physicalExam',
    'exam': 'reserve.physicalExam',
    'grooming': 'reserve.bathGrooming',
    'bath grooming': 'reserve.bathGrooming',
    '普通门诊': 'reserve.outpatientClinic',
    '疫苗接种': 'reserve.vaccination',
    '体检': 'reserve.physicalExam',
    '洗澡美容': 'reserve.bathGrooming',
  }
  if (map[key]) return t(map[key])
  if (key.includes('consult') || key.includes('门诊') || key.includes('clinic')) return t('reserve.outpatientClinic')
  if (key.includes('vaccine') || key.includes('疫苗')) return t('reserve.vaccination')
  if (key.includes('exam') || key.includes('体检') || key.includes('check')) return t('reserve.physicalExam')
  if (key.includes('groom') || key.includes('美容') || key.includes('bath') || key.includes('洗澡')) return t('reserve.bathGrooming')
  return name || '-'
}

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getChargeList({ ...query })
    rows.value = data.list || []
    total.value = Number(data.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || t('charge.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function fetchOrders() {
  const { data } = await getOrderList({ page: 1, pageSize: 10 })
  orderRows.value = data.list || []
}

function onSearch() { query.page = 1; fetchList() }
function onReset() { Object.assign(query, { status: '', keyword: '', page: 1, pageSize: 10 }); fetchList() }
function onPageChange(p) { query.page = p; fetchList() }
function onPageSizeChange(ps) { query.pageSize = ps; query.page = 1; fetchList() }

const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ id: '', orderNo: '', detail: [{ name: '医疗服务费', amount: 0 }], discount: 0, reduction: 0, adjustAmount: 0, adjustReason: '' })
const confirmVisible = ref(false)
const confirmPayMethod = ref('Cash')
const confirmTargetId = ref(null)

const subtotal = computed(() => (editForm.detail || []).reduce((s, x) => s + Number(x.amount || 0), 0))
const finalTotal = computed(() => Math.max(subtotal.value - Number(editForm.discount || 0) - Number(editForm.reduction || 0) + Number(editForm.adjustAmount || 0), 0))
const alertTitle = computed(() => `${t('charge.subtotal')} ¥${subtotal.value.toFixed(2)}，${t('charge.receivableLabel')} ¥${finalTotal.value.toFixed(2)}`)

function openEdit(row) {
  editForm.id = row.id
  editForm.orderNo = row.orderNo
  editForm.detail = JSON.parse(JSON.stringify(row.detail || [{ name: '医疗服务费', amount: 0 }]))
  editForm.discount = Number(row.discount || 0)
  editForm.reduction = Number(row.reduction || 0)
  editForm.adjustAmount = Number(row.adjustAmount || 0)
  editForm.adjustReason = row.adjustReason || ''
  editVisible.value = true
}

function addDetail() { editForm.detail.push({ name: '', amount: 0 }) }
function removeDetail(idx) { editForm.detail.splice(idx, 1) }

async function saveEdit() {
  if (Number(editForm.adjustAmount || 0) !== 0 && !editForm.adjustReason.trim()) return ElMessage.warning(t('charge.adjustmentReasonRequired'))
  saving.value = true
  try {
    await updateChargePricing(editForm.id, {
      detail: editForm.detail,
      discount: editForm.discount,
      reduction: editForm.reduction,
      adjustAmount: editForm.adjustAmount,
      adjustReason: editForm.adjustReason
    })
    ElMessage.success(t('charge.feeUpdated'))
    editVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || t('common.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function doConfirm(row) {
  confirmTargetId.value = row.id
  confirmPayMethod.value = 'Cash'
  confirmVisible.value = true
}

async function submitConfirmCharge() {
  if (!confirmTargetId.value) return
  try {
    await confirmCharge(confirmTargetId.value, confirmPayMethod.value)
    ElMessage.success(t('charge.chargeSuccess'))
    confirmVisible.value = false
    confirmTargetId.value = null
    fetchList()
    fetchOrders()
  } catch (e) {
    ElMessage.error(e?.message || t('charge.chargeFailed'))
  }
}

async function doRefund(row) {
  try {
    const { value } = await ElMessageBox.prompt(t('charge.refundReasonPrompt'), t('charge.refundRequestTitle'))
    await applyRefund(row.id, value || '')
    ElMessage.success(t('charge.refundRequestSubmitted'))
    fetchList()
  } catch {
    // cancel
  }
}

async function printReceipt(row) {
  const txt = buildChargeExportText(row)
  try {
    await navigator.clipboard.writeText(txt)
    ElMessage.success(t('charge.receiptCopied'))
  } catch {
    ElMessageBox.alert(`<pre style="white-space: pre-wrap">${txt}</pre>`, t('charge.receiptPreview'), { dangerouslyUseHTMLString: true })
  }
}

onMounted(async () => {
  await fetchList()
  await fetchOrders()
})
</script>

<style scoped>
.page__header,.page__pager{display:flex;align-items:center}
.page__header{justify-content:space-between;margin-bottom:12px}
.page__title{font-size:16px;font-weight:600;color:#111827}
.page__card{margin-bottom:12px}
.page__pager{margin-top:12px;justify-content:flex-end}
.page__section{font-weight:600;color:#111827;margin-bottom:10px}
.charge-items{width:100%}
.charge-item{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px}
</style>
