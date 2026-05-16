<template>
  <div class="cage-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('cage.cageNo')">
          <el-input
            v-model="searchForm.cageNo"
            :placeholder="t('cage.cageNoPlaceholder')"
            clearable
            prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="t('cage.area')">
          <el-input
            v-model="searchForm.area"
            :placeholder="t('cage.areaPlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-select v-model="searchForm.status" :placeholder="t('common.select')" clearable style="width: 120px">
            <el-option :label="t('cage.statusFree')" :value="0" />
            <el-option :label="t('cage.statusOccupied')" :value="1" />
          </el-select>
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
            <span class="title">{{ t('cage.title') }}</span>
            <el-tag type="info" effect="plain">{{ t('cage.totalRecords', { count: pagination.total }) }}</el-tag>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">
              {{ $t('common.add') }}
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
        <el-table-column type="index" :label="$t('common.no')" width="60" align="center" />
        <el-table-column :label="t('cage.cageNo')" prop="cageNo" min-width="120" align="center" />
        <el-table-column :label="t('cage.area')" prop="area" min-width="150" />
        <el-table-column :label="t('common.status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" effect="light" round>
              {{ row.status === 0 ? t('cage.statusFree') : t('cage.statusOccupied') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('cage.currentPet')" min-width="150">
          <template #default="{ row }">
            <span v-if="row.status === 0 || !row.currentPetName">{{ t('cage.none') }}</span>
            <span v-else>{{ row.currentPetName }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.time')" min-width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <div><el-icon><Clock /></el-icon> {{ formatDateTime(row.createTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)" :icon="Edit">
              {{ $t('common.edit') }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)" :icon="Delete">
              {{ $t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? t('cage.editTitle') : t('cage.addTitle')"
      width="500px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item :label="t('cage.cageNo')" prop="cageNo">
          <el-input v-model="form.cageNo" :placeholder="t('cage.cageNoPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('cage.area')" prop="area">
          <el-input v-model="form.area" :placeholder="t('cage.areaPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, Edit, Delete, Clock } from '@element-plus/icons-vue'
import { getCagePage, addCage, updateCage, deleteCage } from '@/api/admin/cage.js'

const { t } = useI18n()

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  cageNo: '',
  area: '',
  status: undefined
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: undefined,
  cageNo: '',
  area: ''
})

const rules = {
  cageNo: [{ required: true, message: () => t('cage.cageNoRequired'), trigger: 'blur' }],
  area: [{ required: true, message: () => t('cage.areaRequired'), trigger: 'blur' }]
}

function formatDateTime(str) {
  if (!str) return '-'
  const d = new Date(str)
  if (isNaN(d.getTime())) return str
  return d.toLocaleString('zh-CN', { hour12: false })
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCagePage({
      current: pagination.current,
      size: pagination.size,
      cageNo: searchForm.cageNo || undefined,
      area: searchForm.area || undefined,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.msg || t('cage.loadFailed'))
    }
  } catch (e) {
    ElMessage.error(t('cage.loadFailed'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  searchForm.cageNo = ''
  searchForm.area = ''
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

function handleSizeChange(val) {
  pagination.size = val
  loadData()
}

function handleCurrentChange(val) {
  pagination.current = val
  loadData()
}

function handleAdd() {
  isEdit.value = false
  form.id = undefined
  form.cageNo = ''
  form.area = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.cageNo = row.cageNo
  form.area = row.area
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const res = isEdit.value ? await updateCage(form) : await addCage(form)
      if (res.code === 200) {
        ElMessage.success(res.data || (isEdit.value ? t('common.updateSuccess') : t('common.addSuccess')))
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.msg || t('common.operationFailed'))
      }
    } catch (e) {
      ElMessage.error(t('common.operationFailed'))
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(
    t('cage.deleteConfirm', { no: row.cageNo }),
    t('common.tip'),
    { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
  ).then(async () => {
    try {
      const res = await deleteCage(row.id)
      if (res.code === 200) {
        ElMessage.success(t('common.deleteSuccess'))
        loadData()
      } else {
        ElMessage.error(res.msg || t('common.deleteFailed'))
      }
    } catch (e) {
      ElMessage.error(t('common.deleteFailed'))
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.cage-management {
  padding: 20px;
}
.search-card {
  margin-bottom: 16px;
}
.table-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .header-left {
      display: flex;
      align-items: center;
      gap: 10px;
      .title {
        font-size: 16px;
        font-weight: 600;
      }
    }
  }
}
.data-table {
  margin-top: 10px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.time-cell {
  font-size: 13px;
  color: #666;
  .el-icon {
    margin-right: 4px;
    vertical-align: middle;
  }
}
</style>
