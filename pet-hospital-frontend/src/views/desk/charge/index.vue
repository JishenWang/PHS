<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">收费与订单账单管理</div>
    </div>

    <el-card shadow="never" class="page__card">
      <el-form :model="query" inline label-width="72px" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:170px">
            <el-option label="待收费" :value="enums.ChargeStatus.PENDING" />
            <el-option label="已收费" :value="enums.ChargeStatus.PAID" />
            <el-option label="退款中" :value="enums.ChargeStatus.REFUNDING" />
            <el-option label="已退款" :value="enums.ChargeStatus.REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字"><el-input v-model="query.keyword" clearable style="width:260px" placeholder="订单号/客户/医生/支付方式" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-table :data="rows" v-loading="loading" height="420">
        <el-table-column type="index" width="56" label="#" />
        <el-table-column prop="orderNo" label="订单号" min-width="140" />
        <el-table-column prop="registerNo" label="挂号号" min-width="130" />
        <el-table-column prop="customerName" label="客户" min-width="90" />
        <el-table-column label="宠物种类" min-width="80">
          <template #default="{ row }">{{ row.petSpecies || '-' }}</template>
        </el-table-column>
        <el-table-column prop="doctorName" label="医生" min-width="90" />
        <el-table-column label="应收" width="100"><template #default="{ row }">¥{{ Number(row.total || 0).toFixed(2) }}</template></el-table-column>
        <el-table-column label="支付方式" width="100"><template #default="{ row }">{{ row.payMethod || '-' }}</template></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paidAt" label="收费时间" min-width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">费用明细</el-button>
            <el-button v-if="row.status === enums.ChargeStatus.PENDING" type="success" link @click="doConfirm(row)">确认收费</el-button>
            <el-button v-if="row.status === enums.ChargeStatus.PAID" type="warning" link @click="doRefund(row)">退款申请</el-button>
            <el-button type="info" link @click="printReceipt(row)">小票打印</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page__pager">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :current-page="query.page" :page-size="query.pageSize" @update:current-page="onPageChange" @update:page-size="onPageSizeChange" />
      </div>
    </el-card>

    <el-card shadow="never" class="page__card">
      <div class="page__section">今日历史订单（便于账单核对）</div>
      <el-table :data="orderRows" height="220">
        <el-table-column prop="orderNo" label="订单号" min-width="150" />
        <el-table-column prop="customerName" label="客户" min-width="100" />
        <el-table-column prop="doctorName" label="医生" min-width="100" />
        <el-table-column prop="payMethod" label="支付方式" width="100" />
        <el-table-column label="金额" width="100"><template #default="{ row }">¥{{ Number(row.total || 0).toFixed(2) }}</template></el-table-column>
        <el-table-column prop="createdAt" label="时间" min-width="170" />
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" title="费用计算与结算" width="700px">
      <el-form :model="editForm" label-width="96px">
        <el-form-item label="订单号"><el-input v-model="editForm.orderNo" disabled /></el-form-item>
        <el-form-item label="费用明细">
          <div class="charge-items">
            <div v-for="(x, idx) in editForm.detail" :key="idx" class="charge-item">
              <el-input v-model="x.name" placeholder="项目名称" style="width: 45%" />
              <el-input-number v-model="x.amount" :min="0" :step="10" style="width: 45%" />
              <el-button type="danger" link @click="removeDetail(idx)">删除</el-button>
            </div>
            <el-button type="primary" link @click="addDetail">+ 新增项目</el-button>
          </div>
        </el-form-item>
        <el-form-item label="优惠折扣"><el-input-number v-model="editForm.discount" :min="0" :step="5" style="width:100%" /></el-form-item>
        <el-form-item label="满减金额"><el-input-number v-model="editForm.reduction" :min="0" :step="5" style="width:100%" /></el-form-item>
        <el-form-item label="调整金额"><el-input-number v-model="editForm.adjustAmount" :step="1" style="width:100%" /></el-form-item>
        <el-form-item label="调整原因"><el-input v-model="editForm.adjustReason" placeholder="手动调整必须备注原因" /></el-form-item>
        <el-alert :closable="false" type="info" :title="`小计 ¥${subtotal.toFixed(2)}，应收 ¥${finalTotal.toFixed(2)}`" />
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="confirmVisible" title="确认收费" width="420px">
      <el-form label-width="88px">
        <el-form-item label="支付方式">
          <el-select v-model="confirmPayMethod" placeholder="请选择支付方式" style="width:100%">
            <el-option v-for="m in enums.payMethods" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfirmCharge">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applyRefund, buildChargeExportText, confirmCharge, getChargeList, getDeskEnums, getOrderList, updateChargePricing } from '@/api/desk/desk'

const enums = getDeskEnums()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const orderRows = ref([])

const query = reactive({ status: '', keyword: '', page: 1, pageSize: 10 })

function statusLabel(status) {
  if (status === enums.ChargeStatus.PENDING) return '待收费'
  if (status === enums.ChargeStatus.PAID) return '已收费'
  if (status === enums.ChargeStatus.REFUNDING) return '退款中'
  if (status === enums.ChargeStatus.REFUNDED) return '已退款'
  return '-'
}
function statusTagType(status) {
  if (status === enums.ChargeStatus.PENDING) return 'warning'
  if (status === enums.ChargeStatus.PAID) return 'success'
  if (status === enums.ChargeStatus.REFUNDING) return 'danger'
  return 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getChargeList({ ...query })
    rows.value = data.list || []
    total.value = Number(data.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
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
const editForm = reactive({ id: '', orderNo: '', detail: [{ name: '诊疗服务费', amount: 0 }], discount: 0, reduction: 0, adjustAmount: 0, adjustReason: '' })
const confirmVisible = ref(false)
const confirmPayMethod = ref('现金')
const confirmTargetId = ref(null)

const subtotal = computed(() => (editForm.detail || []).reduce((s, x) => s + Number(x.amount || 0), 0))
const finalTotal = computed(() => Math.max(subtotal.value - Number(editForm.discount || 0) - Number(editForm.reduction || 0) + Number(editForm.adjustAmount || 0), 0))

function openEdit(row) {
  editForm.id = row.id
  editForm.orderNo = row.orderNo
  editForm.detail = JSON.parse(JSON.stringify(row.detail || [{ name: '诊疗服务费', amount: 0 }]))
  editForm.discount = Number(row.discount || 0)
  editForm.reduction = Number(row.reduction || 0)
  editForm.adjustAmount = Number(row.adjustAmount || 0)
  editForm.adjustReason = row.adjustReason || ''
  editVisible.value = true
}

function addDetail() { editForm.detail.push({ name: '', amount: 0 }) }
function removeDetail(idx) { editForm.detail.splice(idx, 1) }

async function saveEdit() {
  if (Number(editForm.adjustAmount || 0) !== 0 && !editForm.adjustReason.trim()) return ElMessage.warning('手动调整金额时必须填写调整原因')
  saving.value = true
  try {
    await updateChargePricing(editForm.id, {
      detail: editForm.detail,
      discount: editForm.discount,
      reduction: editForm.reduction,
      adjustAmount: editForm.adjustAmount,
      adjustReason: editForm.adjustReason
    })
    ElMessage.success('费用已更新')
    editVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function doConfirm(row) {
  confirmTargetId.value = row.id
  confirmPayMethod.value = '现金'
  confirmVisible.value = true
}

async function submitConfirmCharge() {
  if (!confirmTargetId.value) return
  try {
    await confirmCharge(confirmTargetId.value, confirmPayMethod.value)
    ElMessage.success('收费成功')
    confirmVisible.value = false
    confirmTargetId.value = null
    fetchList()
    fetchOrders()
  } catch (e) {
    ElMessage.error(e?.message || '收费失败')
  }
}

async function doRefund(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入退款原因（将提交管理员终审）', '退款申请')
    await applyRefund(row.id, value || '')
    ElMessage.success('退款申请已提交')
    fetchList()
  } catch {
    // cancel
  }
}

async function printReceipt(row) {
  const txt = buildChargeExportText(row)
  try {
    await navigator.clipboard.writeText(txt)
    ElMessage.success('小票文本已复制，可直接打印')
  } catch {
    ElMessageBox.alert(`<pre style="white-space: pre-wrap">${txt}</pre>`, '小票预览', { dangerouslyUseHTMLString: true })
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
