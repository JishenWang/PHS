<template>
  <div class="pet-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('common.keyword')">
          <el-input 
            v-model="searchForm.keyword" 
            :placeholder="$t('adminPet.keywordPlaceholder')" 
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

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">{{ $t('adminPet.petRecords') }}</span>
            <el-tag type="info" effect="plain">{{ $t('adminPet.totalPets', { count: tableData.length }) }}</el-tag>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">{{ $t('adminPet.addRecord') }}</el-button>
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
        
        <el-table-column :label="$t('adminPet.petInfo')" min-width="200" fixed="left">
          <template #default="{ row }">
            <div class="pet-info-cell">
              <div class="pet-avatar" :class="row.type">
                {{ getPetEmoji(row.type) }}
              </div>
              <div class="pet-detail">
                <div class="pet-name">{{ row.name }}</div>
                <div class="pet-breed">{{ row.breed }} · {{ row.age }} {{ $t('adminPet.years') }} · {{ row.gender === 'male' ? $t('adminPet.male') : $t('adminPet.female') }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('adminPet.ownerInfo')" min-width="180">
          <template #default="{ row }">
            <div class="owner-info">
              <div class="owner-name">{{ row.ownerName }}</div>
              <div class="owner-phone">
                <el-icon><Phone /></el-icon>
                {{ row.ownerPhone }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('adminPet.healthStatus')" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getHealthType(row.healthStatus)" effect="light" round>
              {{ getHealthText(row.healthStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="$t('adminPet.vaccination')" min-width="200">
          <template #default="{ row }">
            <div class="vaccine-tags">
              <el-tag 
                v-for="vaccine in row.vaccines" 
                :key="vaccine"
                size="small"
                effect="plain"
                type="success"
              >
                {{ getVaccineLabel(vaccine) }}
              </el-tag>
              <el-tag v-if="!row.vaccines?.length" size="small" type="info">{{ $t('adminPet.notVaccinated') }}</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('adminPet.lastVisit')" width="140" align="center">
          <template #default="{ row }">
            <div class="last-visit">
              <el-icon><Calendar /></el-icon>
              <span>{{ row.lastVisit || $t('adminPet.noRecords') }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="$t('common.operation')" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">{{ $t('adminPet.details') }}</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
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
      class="pet-dialog"
    >
      <el-form 
        :model="formData" 
        :rules="formRules" 
        ref="formRef" 
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('adminPet.petName')" prop="name">
              <el-input v-model="formData.name" :placeholder="$t('adminPet.pleaseEnterPetName')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('adminPet.petType')" prop="type">
              <el-select v-model="formData.type" :placeholder="$t('adminPet.selectType')" style="width: 100%">
                <el-option :label="$t('adminPet.typeDog')" value="dog" />
                <el-option :label="$t('adminPet.typeCat')" value="cat" />
                <el-option :label="$t('adminPet.typeRabbit')" value="rabbit" />
                <el-option :label="$t('adminPet.typeOther')" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('adminPet.breed')" prop="breed">
              <el-input v-model="formData.breed" :placeholder="$t('adminPet.pleaseEnterBreed')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('adminPet.gender')" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio label="male">{{ $t('adminPet.male') }}</el-radio>
                <el-radio label="female">{{ $t('adminPet.female') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('adminPet.age')" prop="age">
              <el-input-number v-model="formData.age" :min="0" :max="30" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('adminPet.weightKg')" prop="weight">
              <el-input-number v-model="formData.weight" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('adminPet.ownerName')" prop="ownerName">
              <el-input v-model="formData.ownerName" :placeholder="$t('adminPet.pleaseEnterOwnerName')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('adminPet.ownerPhone')" prop="ownerPhone">
              <el-input v-model="formData.ownerPhone" :placeholder="$t('adminPet.pleaseEnterOwnerPhone')" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="$t('adminPet.healthStatus')" prop="healthStatus">
          <el-radio-group v-model="formData.healthStatus">
            <el-radio label="healthy">{{ $t('adminPet.healthHealthy') }}</el-radio>
            <el-radio label="treatment">{{ $t('adminPet.healthTreatment') }}</el-radio>
            <el-radio label="checkup">{{ $t('adminPet.healthCheckup') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="$t('adminPet.vaccination')">
          <el-select
            v-model="formData.vaccines"
            multiple
            collapse-tags
            :placeholder="$t('adminPet.selectVaccines')"
            style="width: 100%"
          >
            <el-option :label="$t('adminPet.vaccineRabies')" value="狂犬疫苗" />
            <el-option :label="$t('adminPet.vaccineCanineHexavalent')" value="犬六联" />
            <el-option :label="$t('adminPet.vaccineCanineParvovirus')" value="犬细小" />
            <el-option :label="$t('adminPet.vaccineFelineTriple')" value="猫三联" />
            <el-option :label="$t('adminPet.vaccineFelineQuadrivalent')" value="猫四联" />
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('adminPet.allergyHistory')">
          <el-input v-model="formData.allergies" type="textarea" :rows="2" :placeholder="$t('adminPet.allergyPlaceholder')" />
        </el-form-item>

        <el-form-item :label="$t('adminPet.remark')">
          <el-input v-model="formData.remark" type="textarea" :rows="3" :placeholder="$t('adminPet.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog 
      :title="$t('adminPet.petRecordDetails')" 
      v-model="detailVisible" 
      width="700px"
      class="pet-detail-dialog"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="$t('adminPet.petName')">{{ currentPet.name }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.petType')">{{ getPetEmoji(currentPet.type) }} {{ getPetTypeName(currentPet.type) }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.breed')">{{ currentPet.breed }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.gender')">{{ currentPet.gender === 'male' ? $t('adminPet.male') + ' ♂' : $t('adminPet.female') + ' ♀' }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.age')">{{ currentPet.age }} {{ $t('adminPet.years') }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.weightKg')">{{ currentPet.weight }} kg</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.ownerName')">{{ currentPet.ownerName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.ownerPhone')">{{ currentPet.ownerPhone }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.healthStatus')" :span="2">
          <el-tag :type="getHealthType(currentPet.healthStatus)">
            {{ getHealthText(currentPet.healthStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.vaccination')" :span="2">
          <el-tag 
            v-for="vaccine in currentPet.vaccines || []" 
            :key="vaccine"
            type="success"
            style="margin-right: 8px; margin-bottom: 5px;"
          >
            {{ getVaccineLabel(vaccine) }}
          </el-tag>
          <span v-if="!currentPet.vaccines?.length">{{ $t('adminPet.notVaccinated') }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.allergyHistory')" :span="2">{{ currentPet.allergies || $t('common.none') }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminPet.remark')" :span="2">{{ currentPet.remark || $t('common.none') }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshRight, Edit, Delete, View, Phone, Calendar } from '@element-plus/icons-vue'
import { 
  getPetList, 
  addPet, 
  updatePet, 
  deletePet,
} from '@/api/admin/admin'

const { t } = useI18n()

const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref(t('adminPet.addRecord'))
const formRef = ref(null)
const currentPet = ref({})

const searchForm = reactive({
  keyword: ''
})

const formData = reactive({
  id: null,
  name: '',
  type: '',
  breed: '',
  gender: 'male',
  age: 0,
  weight: 0,
  ownerName: '',
  ownerPhone: '',
  healthStatus: 'healthy',
  vaccines: [],
  allergies: '',
  remark: ''
})

const formRules = {
  name: [{ required: true, message: t('adminPet.pleaseEnterPetName'), trigger: 'blur' }],
  type: [{ required: true, message: t('adminPet.pleaseSelectPetType'), trigger: 'change' }],
  breed: [{ required: true, message: t('adminPet.pleaseEnterBreed'), trigger: 'blur' }],
  gender: [{ required: true, message: t('adminPet.pleaseSelectGender'), trigger: 'change' }],
  age: [{ required: true, message: t('adminPet.pleaseEnterAge'), trigger: 'blur' }],
  ownerName: [{ required: true, message: t('adminPet.pleaseEnterOwnerName'), trigger: 'blur' }],
  ownerPhone: [{ required: true, message: t('adminPet.pleaseEnterOwnerPhone'), trigger: 'blur' }],
  healthStatus: [{ required: true, message: t('adminPet.pleaseSelectHealthStatus'), trigger: 'change' }]
}

const tableData = ref([])

// 获取宠物列表 - 不分页
const fetchPetList = async () => {
  loading.value = true
  try {
    const res = await getPetList(searchForm.keyword)
    console.log('Pet list response:', res)  // 添加调试日志
    
    if (res.code === 200) {
      tableData.value = res.data || []
      if (tableData.value.length === 0) {
        console.log('Returned data is empty array, check if database has data')
      }
    } else {
      ElMessage.error(res.message || t('adminPet.fetchFailed'))
    }
  } catch (error) {
    console.error('Failed to fetch pet list:', error)
    console.error('Error details:', error.response?.data || error.message)
    ElMessage.error(error.response?.data?.message || t('adminPet.fetchFailed'))
  } finally {
    loading.value = false
  }
}

const getPetEmoji = (type) => {
  const emojis = { dog: '🐕', cat: '🐱', rabbit: '🐰', other: '🐹', '狗': '🐕', '猫': '🐱', '兔': '🐰', '未知': '🐾' }
  return emojis[type] || '🐾'
}

const getPetTypeName = (type) => {
  const map = { dog: 'typeDog', cat: 'typeCat', rabbit: 'typeRabbit', other: 'typeOther', '狗': 'typeDog', '猫': 'typeCat', '兔': 'typeRabbit', '未知': 'typeUnknown' }
  return map[type] ? t(`adminPet.${map[type]}`) : t('adminPet.typeUnknown')
}

const getHealthType = (status) => {
  const types = { healthy: 'success', treatment: 'danger', checkup: 'warning' }
  return types[status] || 'info'
}

const getHealthText = (status) => {
  const map = { healthy: 'healthHealthy', treatment: 'healthTreatment', checkup: 'healthCheckup' }
  return map[status] ? t(`adminPet.${map[status]}`) : t('adminPet.healthUnknown')
}

const getVaccineLabel = (vaccine) => {
  const map = {
    '狂犬疫苗': 'vaccineRabies',
    '狂犬': 'vaccineRabies',
    '犬六联': 'vaccineCanineHexavalent',
    '犬细小': 'vaccineCanineParvovirus',
    '猫三联': 'vaccineFelineTriple',
    '猫四联': 'vaccineFelineQuadrivalent',
    '兔瘟疫苗': 'vaccineRabbitPlague',
    'Rabies Vaccine': 'vaccineRabies',
    'Canine Hexavalent': 'vaccineCanineHexavalent',
    'Canine Parvovirus': 'vaccineCanineParvovirus',
    'Feline Triple': 'vaccineFelineTriple',
    'Feline Quadrivalent': 'vaccineFelineQuadrivalent'
  }
  return map[vaccine] ? t(`adminPet.${map[vaccine]}`) : vaccine
}

const handleSearch = () => {
  fetchPetList()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = t('adminPet.addRecord')
  Object.assign(formData, {
    id: null,
    name: '',
    type: '',
    breed: '',
    gender: 'male',
    age: 0,
    weight: 0,
    ownerName: '',
    ownerPhone: '',
    healthStatus: 'healthy',
    vaccines: [],
    allergies: '',
    remark: ''
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  currentPet.value = row
  detailVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = t('adminPet.editRecord')
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(t('adminPet.deleteConfirm', { name: row.name }), t('common.warning'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePet(row.id)
      if (res.code === 200) {
        ElMessage.success(t('common.deleteSuccess'))
        fetchPetList()
      }
    } catch (error) {
      ElMessage.error(t('common.deleteFailed'))
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    const api = formData.id ? updatePet : addPet
    const res = await api(formData)
    
    if (res.code === 200) {
      ElMessage.success(formData.id ? t('common.updateSuccess') : t('common.addSuccess'))
      dialogVisible.value = false
      fetchPetList()
    }
  } catch (error) {
    console.error('Submit failed:', error)
    ElMessage.error(error.response?.data?.message || t('common.operationFailed'))
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchPetList()
})
</script>

<style scoped>
.pet-management {
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

/* 宠物信息单元格 */
.pet-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pet-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: var(--bg-color);
}

.pet-avatar.dog { background: #fff3e0; }
.pet-avatar.cat { background: #fce4ec; }
.pet-avatar.rabbit { background: #f3e5f5; }
.pet-avatar.other { background: #e8f5e9; }

.pet-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pet-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 15px;
}

.pet-breed {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 主人信息 */
.owner-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.owner-name {
  font-weight: 500;
  color: var(--text-primary);
}

.owner-phone {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.owner-phone .el-icon {
  color: var(--primary-color);
}

/* 疫苗标签 */
.vaccine-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

/* 上次就诊 */
.last-visit {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.last-visit .el-icon {
  color: var(--info-color);
}

/* 详情对话框 */
.pet-detail-dialog :deep(.el-descriptions__label) {
  width: 100px;
  font-weight: 600;
}

.pet-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px;
  border-bottom: 1px solid var(--border-lighter);
}

.pet-dialog :deep(.el-dialog__body) {
  padding: 30px 20px;
}
</style>
