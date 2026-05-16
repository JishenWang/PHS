<template>
  <div class="medicine-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('common.keyword')">
          <el-input
            v-model="searchForm.keyword"
            :placeholder="$t('medicine.keywordPlaceholder')"
            clearable
            prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset" :icon="RefreshRight">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">{{ $t('medicine.medicineList') }}</span>
            <el-tag type="info" effect="plain">{{ $t('medicine.totalRecords', { count: tableData.length }) }}</el-tag>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">
              {{ $t('medicine.addMedicine') }}
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        highlight-current-row
        class="data-table"
        max-height="600"
      >
        <el-table-column type="index" :label="$t('common.no')" width="60" align="center" fixed="left" />
        <el-table-column prop="medicineCode" :label="$t('medicine.medicineCode')" width="120" align="center" fixed="left" />
        <el-table-column prop="medicineName" :label="$t('medicine.medicineName')" min-width="150" fixed="left" />
        <el-table-column prop="category" :label="$t('medicine.category')" width="100" align="center" />
        <el-table-column prop="specification" :label="$t('medicine.specification')" width="120" />
        <el-table-column prop="unit" :label="$t('medicine.unit')" width="80" align="center" />
        <el-table-column prop="price" :label="$t('medicine.unitPrice')" width="100" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.price?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('medicine.stock')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStockType(row)" size="small">
              {{ row.stockQty }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warningStockQty" :label="$t('medicine.warningStock')" width="100" align="center" />
        <el-table-column prop="producer" :label="$t('medicine.manufacturer')" min-width="150" show-overflow-tooltip />
        <el-table-column :label="$t('medicine.expiryDate')" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.expiryDate" :class="{ 'text-danger': isExpired(row.expiryDate) }">{{ row.expiryDate }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.status')" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              :active-text="$t('common.on')"
              :inactive-text="$t('common.off')"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('medicine.createdAt')" width="160" align="center" />
        <el-table-column :label="$t('common.operation')" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">
              {{ $t('common.edit') }}
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              {{ $t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="700px"
      destroy-on-close
      class="medicine-dialog"
    >
      <el-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        label-width="100px"
        class="dialog-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('medicine.medicineCode')" prop="medicineCode">
              <el-input v-model="formData.medicineCode" :placeholder="$t('medicine.pleaseEnterMedicineCode')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('medicine.medicineName')" prop="medicineName">
              <el-input v-model="formData.medicineName" :placeholder="$t('medicine.pleaseEnterMedicineName')" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('medicine.category')" prop="category">
              <el-select v-model="formData.category" :placeholder="$t('medicine.selectCategory')" style="width: 100%">
                <el-option :label="$t('medicine.catMedicine')" value="药品" />
                <el-option :label="$t('medicine.catVaccine')" value="疫苗" />
                <el-option :label="$t('medicine.catGastrointestinal')" value="肠胃" />
                <el-option :label="$t('medicine.catDermatology')" value="皮肤" />
                <el-option :label="$t('medicine.catNutrition')" value="营养" />
                <el-option :label="$t('medicine.catConsumables')" value="耗材" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('medicine.unit')" prop="unit">
              <el-input v-model="formData.unit" :placeholder="$t('medicine.unitPlaceholder')" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('medicine.specification')" prop="specification">
              <el-input v-model="formData.specification" :placeholder="$t('medicine.specificationPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('medicine.manufacturer')" prop="producer">
              <el-input v-model="formData.producer" :placeholder="$t('medicine.pleaseEnterManufacturer')" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item :label="$t('medicine.expiryDate')" prop="expiryDate">
              <el-date-picker
                v-model="formData.expiryDate"
                type="date"
                value-format="YYYY-MM-DD"
                :placeholder="$t('medicine.selectExpiryDate')"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('medicine.unitPrice')" prop="price">
              <el-input-number v-model="formData.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('medicine.stock')" prop="stockQty">
              <el-input-number v-model="formData.stockQty" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('medicine.warningStock')" prop="warningStockQty">
              <el-input-number v-model="formData.warningStockQty" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="$t('medicine.contraindicationsAllergy')" prop="caution">
          <el-input
            v-model="formData.caution"
            type="textarea"
            :rows="2"
            :placeholder="$t('medicine.contraindicationsPlaceholder')"
          />
        </el-form-item>

        <el-form-item :label="$t('medicine.usageInstructions')" prop="instruction">
          <el-input
            v-model="formData.instruction"
            type="textarea"
            :rows="2"
            :placeholder="$t('medicine.usageInstructionsPlaceholder')"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ $t('common.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  RefreshRight,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import {
  getMedicineList,
  addMedicine,
  updateMedicine,
  deleteMedicine,
  updateMedicineStatus
} from '@/api/admin/admin'

const { t } = useI18n()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref(t('medicine.addMedicine'))
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const formData = reactive({
  id: null,
  medicineCode: '',
  medicineName: '',
  category: '',
  specification: '',
  unit: '',
  price: 0,
  stockQty: 0,
  warningStockQty: 0,
  producer: '',
  expiryDate: '',
  caution: '',
  instruction: ''
})

const formRules = {
  medicineCode: [
    { required: true, message: t('medicine.pleaseEnterMedicineCode'), trigger: 'blur' }
  ],
  medicineName: [
    { required: true, message: t('medicine.pleaseEnterMedicineName'), trigger: 'blur' }
  ],
  category: [
    { required: true, message: t('medicine.pleaseSelectCategory'), trigger: 'change' }
  ],
  price: [
    { required: true, message: t('medicine.pleaseEnterUnitPrice'), trigger: 'blur' }
  ]
}

const tableData = ref([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMedicineList(searchForm.keyword)
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to fetch medicine list:', error)
    ElMessage.error(t('medicine.fetchFailed'))
  } finally {
    loading.value = false
  }
}

const getStockType = (row) => {
  if (row.stockQty <= (row.warningStockQty || 0)) return 'danger'
  if (row.stockQty <= (row.warningStockQty || 0) + 5) return 'warning'
  return 'success'
}

const isExpired = (dateStr) => {
  if (!dateStr) return false
  return new Date(dateStr) < new Date(new Date().toDateString())
}

const handleSearch = () => {
  fetchList()
}

const handleReset = () => {
  searchForm.keyword = ''
  fetchList()
}

const handleAdd = () => {
  dialogTitle.value = t('medicine.addMedicine')
  Object.assign(formData, {
    id: null,
    medicineCode: '',
    medicineName: '',
    category: '',
    specification: '',
    unit: '',
    price: 0,
    stockQty: 0,
    warningStockQty: 0,
    producer: '',
    expiryDate: '',
    caution: '',
    instruction: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = t('medicine.editMedicine')
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    t('medicine.deleteConfirm', { name: row.medicineName }),
    t('common.warning'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteMedicine(row.id)
      if (res.code === 200) {
        ElMessage.success(t('common.deleteSuccess'))
        fetchList()
      }
    } catch (error) {
      ElMessage.error(t('common.deleteFailed'))
    }
  })
}

const handleStatusChange = async (row) => {
  try {
    const res = await updateMedicineStatus(row.id, row.status)
    if (res.code === 200) {
      ElMessage.success(t('medicine.statusChangeSuccess', { name: row.medicineName, status: row.status === 1 ? t('common.on') : t('common.off') }))
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(t('common.statusUpdateFailed'))
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const submitData = { ...formData }
    delete submitData.id
    delete submitData.createTime
    delete submitData.updateTime

    let res
    if (formData.id) {
      res = await updateMedicine(formData.id, submitData)
    } else {
      res = await addMedicine(submitData)
    }

    if (res.code === 200) {
      ElMessage.success(formData.id ? t('common.updateSuccess') : t('common.addSuccess'))
      dialogVisible.value = false
      fetchList()
    }
  } catch (error) {
    console.error('Submit failed:', error)
    ElMessage.error(error.response?.data?.message || t('common.operationFailed'))
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.medicine-management {
  padding: 20px;
  background: var(--bg-color);
  min-height: calc(100vh - 84px);
}

.search-card {
  margin-bottom: 20px;
  border-radius: var(--radius-large);
}

.search-card :deep(.el-card__body) {
  padding: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-form :deep(.el-input__wrapper),
.search-form :deep(.el-select) {
  width: 240px;
}

.table-card {
  border-radius: var(--radius-large);
}

.table-card :deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left .title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  gap: 10px;
}

.data-table {
  margin-top: 10px;
}

.data-table :deep(.el-table__header th) {
  background: #f5f7fa;
  font-weight: 600;
  color: var(--text-primary);
}

.price {
  font-weight: 600;
  color: #f59e0b;
}

.medicine-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.medicine-dialog :deep(.el-dialog__body) {
  padding: 30px 20px;
}

.dialog-form :deep(.el-input__wrapper),
.dialog-form :deep(.el-select) {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
}

@media (max-width: 768px) {
  .search-form :deep(.el-input__wrapper),
  .search-form :deep(.el-select) {
    width: 100%;
  }

  .header-right {
    flex-direction: column;
    gap: 5px;
  }
}
</style>
