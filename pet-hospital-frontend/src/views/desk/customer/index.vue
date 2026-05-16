<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">{{ $t('customer.title') }}</div>
      <div class="page__actions">
        <el-tag size="small" :type="sourceTagType">{{ sourceLabel }}</el-tag>
        <el-button type="primary" @click="tempVisible = true">{{ t('common.add') }} {{ $t('customer.tempCustomer') }}</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page__card">
      <el-form :model="query" inline label-width="72px" @submit.prevent>
        <el-form-item :label="$t('common.phone')"><el-input v-model="query.phone" clearable style="width:160px" /></el-form-item>
        <el-form-item :label="$t('common.name')"><el-input v-model="query.name" clearable style="width:140px" /></el-form-item>
        <el-form-item :label="$t('customer.petName')"><el-input v-model="query.petName" clearable style="width:140px" /></el-form-item>
        <el-form-item :label="$t('common.keyword')"><el-input v-model="query.keyword" clearable style="width:220px" :placeholder="$t('customer.keywordPlaceholder')" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">{{ t('common.search') }}</el-button>
          <el-button @click="onReset">{{ t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-table :data="rows" v-loading="loading" height="520">
        <el-table-column type="index" width="58" label="#" />
        <el-table-column prop="name" :label="$t('register.customer')" min-width="110" />
        <el-table-column prop="phone" :label="$t('common.phone')" min-width="130" />
        <el-table-column :label="$t('customer.temp')" width="90">
          <template #default="{ row }">{{ row.isTemp ? $t('common.yes') : $t('common.no') }}</template>
        </el-table-column>
        <el-table-column prop="address" :label="$t('customer.address')" min-width="170" show-overflow-tooltip />
        <el-table-column :label="$t('customer.pets')" width="90"><template #default="{ row }">{{ row.pets?.length || 0 }}</template></el-table-column>
        <el-table-column :label="$t('common.operation')" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">{{ $t('common.detail') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page__pager">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :current-page="query.page" :page-size="query.pageSize" @update:current-page="onPageChange" @update:page-size="onPageSizeChange" />
      </div>
    </el-card>

    <el-dialog v-model="tempVisible" :title="$t('customer.addTempCustomer')" width="560px">
      <el-form :model="tempForm" label-width="92px">
        <el-form-item :label="$t('common.name')" required><el-input v-model="tempForm.name" /></el-form-item>
        <el-form-item :label="$t('common.phone')" required><el-input v-model="tempForm.phone" /></el-form-item>
        <el-form-item :label="$t('customer.address')"><el-input v-model="tempForm.address" /></el-form-item>
        <el-form-item :label="$t('customer.petName')" required><el-input v-model="tempForm.petName" /></el-form-item>
        <el-form-item :label="$t('customer.species')"><el-input v-model="tempForm.petSpecies" :placeholder="$t('customer.speciesPlaceholder')" /></el-form-item>
        <el-form-item :label="$t('customer.gender')"><el-input v-model="tempForm.petGender" /></el-form-item>
        <el-form-item :label="$t('customer.age')"><el-input-number v-model="tempForm.petAge" :min="0" :step="1" style="width:100%" /></el-form-item>
        <el-form-item :label="$t('customer.allergy')"><el-input v-model="tempForm.allergyHistory" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tempVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submitTemp">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" :title="$t('customer.petRecords')" size="560px">
      <div v-if="current">
        <el-descriptions border :column="1">
          <el-descriptions-item :label="$t('register.customer')">{{ current.name }}</el-descriptions-item>
          <el-descriptions-item :label="$t('common.phone')">{{ current.phone }}</el-descriptions-item>
          <el-descriptions-item :label="$t('customer.address')">{{ current.address || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="page__section">{{ $t('customer.petRecords') }}</div>
        <el-collapse>
          <el-collapse-item v-for="p in current.pets || []" :key="p.id" :title="`${p.name}（${p.species || '-'}）`">
            <el-descriptions :column="1" border>
              <el-descriptions-item :label="$t('customer.gender')">{{ p.gender || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="$t('customer.age')">{{ p.age ?? '-' }}</el-descriptions-item>
              <el-descriptions-item :label="$t('customer.allergy')">{{ p.allergyHistory || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="$t('customer.healthHistory')">
                <div v-if="(p.healthRecords || []).length">
                  <div v-for="(h, idx) in p.healthRecords" :key="idx">{{ h }}</div>
                </div>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { createTempCustomer, queryCustomers } from '@/api/desk/desk'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const source = ref('mock')

const sourceLabel = computed(() => (source.value === 'api' ? t('customer.sourceApi') : t('customer.sourceMock')))
const sourceTagType = computed(() => (source.value === 'api' ? 'success' : 'info'))

const query = reactive({ phone: '', name: '', petName: '', keyword: '', page: 1, pageSize: 10 })

const tempVisible = ref(false)
const tempForm = reactive({ name: '', phone: '', address: '', petName: '', petSpecies: '', petGender: '', petAge: 0, allergyHistory: '' })

const detailVisible = ref(false)
const current = ref(null)

function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

async function fetchList() {
  loading.value = true
  try {
    const { data, source: s } = await queryCustomers({ ...query })
    source.value = s
    rows.value = data.list || []
    total.value = Number(data.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || t('customer.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function submitTemp() {
  if (!tempForm.name || !tempForm.phone || !tempForm.petName) return ElMessage.warning(t('customer.fillRequiredFields'))
  saving.value = true
  try {
    await createTempCustomer({ ...tempForm })
    ElMessage.success(t('customer.tempCustomerAdded'))
    tempVisible.value = false
    Object.assign(tempForm, { name: '', phone: '', address: '', petName: '', petSpecies: '', petGender: '', petAge: 0, allergyHistory: '' })
    query.page = 1
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || t('common.saveFailed'))
  } finally {
    saving.value = false
  }
}

function onSearch() {
  query.page = 1
  fetchList()
}
function onReset() {
  Object.assign(query, { phone: '', name: '', petName: '', keyword: '', page: 1, pageSize: 10 })
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

onMounted(fetchList)
</script>

<style scoped>
.page__header,.page__actions,.page__pager{display:flex;align-items:center}
.page__header{justify-content:space-between;margin-bottom:12px}
.page__actions{gap:8px}
.page__title{font-size:16px;font-weight:600;color:#111827}
.page__card{margin-bottom:12px}
.page__pager{margin-top:12px;justify-content:flex-end}
.page__section{margin:14px 0 8px;font-weight:600}
</style>
