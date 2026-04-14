<template>
  <div class="page">
    <div class="page__header">
      <div class="page__title">客户与宠物快速管理</div>
      <div class="page__actions">
        <el-tag size="small" :type="sourceTagType">{{ sourceLabel }}</el-tag>
        <el-button type="primary" @click="tempVisible = true">新增临时客户</el-button>
      </div>
    </div>

    <el-card shadow="never" class="page__card">
      <el-form :model="query" inline label-width="72px" @submit.prevent>
        <el-form-item label="手机号"><el-input v-model="query.phone" clearable style="width:160px" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="query.name" clearable style="width:140px" /></el-form-item>
        <el-form-item label="宠物名"><el-input v-model="query.petName" clearable style="width:140px" /></el-form-item>
        <el-form-item label="关键字"><el-input v-model="query.keyword" clearable style="width:220px" placeholder="姓名/手机号/宠物" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="page__card">
      <el-table :data="rows" v-loading="loading" height="520">
        <el-table-column type="index" width="58" label="#" />
        <el-table-column prop="name" label="客户" min-width="110" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="临时客户" width="90">
          <template #default="{ row }">{{ row.isTemp ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="170" show-overflow-tooltip />
        <el-table-column label="宠物数量" width="90"><template #default="{ row }">{{ row.pets?.length || 0 }}</template></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">档案详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page__pager">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total" :current-page="query.page" :page-size="query.pageSize" @update:current-page="onPageChange" @update:page-size="onPageSizeChange" />
      </div>
    </el-card>

    <el-dialog v-model="tempVisible" title="新增临时客户" width="560px">
      <el-form :model="tempForm" label-width="92px">
        <el-form-item label="客户姓名" required><el-input v-model="tempForm.name" /></el-form-item>
        <el-form-item label="手机号" required><el-input v-model="tempForm.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="tempForm.address" /></el-form-item>
        <el-form-item label="宠物名" required><el-input v-model="tempForm.petName" /></el-form-item>
        <el-form-item label="宠物种类"><el-input v-model="tempForm.petSpecies" placeholder="猫/狗等" /></el-form-item>
        <el-form-item label="宠物性别"><el-input v-model="tempForm.petGender" /></el-form-item>
        <el-form-item label="宠物年龄"><el-input-number v-model="tempForm.petAge" :min="0" :step="1" style="width:100%" /></el-form-item>
        <el-form-item label="过敏史"><el-input v-model="tempForm.allergyHistory" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tempVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitTemp">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="宠物档案与历史记录" size="560px">
      <div v-if="current">
        <el-descriptions border :column="1">
          <el-descriptions-item label="客户">{{ current.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ current.phone }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ current.address || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="page__section">宠物档案</div>
        <el-collapse>
          <el-collapse-item v-for="p in current.pets || []" :key="p.id" :title="`${p.name}（${p.species || '未知'}）`">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="性别">{{ p.gender || '-' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ p.age ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="过敏史">{{ p.allergyHistory || '无' }}</el-descriptions-item>
              <el-descriptions-item label="历史健康记录">
                <div v-if="(p.healthRecords || []).length">
                  <div v-for="(h, idx) in p.healthRecords" :key="idx">{{ h }}</div>
                </div>
                <span v-else>暂无</span>
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
import { ElMessage } from 'element-plus'
import { createTempCustomer, queryCustomers } from '@/api/desk/desk'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const source = ref('mock')

const sourceLabel = computed(() => (source.value === 'api' ? '后端接口' : '本地Mock'))
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
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function submitTemp() {
  if (!tempForm.name || !tempForm.phone || !tempForm.petName) return ElMessage.warning('请填写必填项')
  saving.value = true
  try {
    await createTempCustomer({ ...tempForm })
    ElMessage.success('临时客户新增成功')
    tempVisible.value = false
    Object.assign(tempForm, { name: '', phone: '', address: '', petName: '', petSpecies: '', petGender: '', petAge: 0, allergyHistory: '' })
    query.page = 1
    fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
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
