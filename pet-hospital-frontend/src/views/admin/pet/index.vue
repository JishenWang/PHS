<template>
  <div class="pet-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入宠物名称或主人姓名" 
            clearable
            prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="handleReset" :icon="RefreshRight">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">宠物档案</span>
            <el-tag type="info" effect="plain">共 {{ tableData.length }} 只宠物</el-tag>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增档案</el-button>
            <el-button type="success" :icon="Download" @click="handleExport">导出</el-button>
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
        <el-table-column type="index" label="序号" width="60" align="center" fixed="left" />
        
        <el-table-column label="宠物信息" min-width="200" fixed="left">
          <template #default="{ row }">
            <div class="pet-info-cell">
              <div class="pet-avatar" :class="row.type">
                {{ getPetEmoji(row.type) }}
              </div>
              <div class="pet-detail">
                <div class="pet-name">{{ row.name }}</div>
                <div class="pet-breed">{{ row.breed }} · {{ row.age }}岁 · {{ row.gender === 'male' ? '公' : '母' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="主人信息" min-width="180">
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

        <el-table-column label="健康状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getHealthType(row.healthStatus)" effect="light" round>
              {{ getHealthText(row.healthStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="疫苗情况" min-width="200">
          <template #default="{ row }">
            <div class="vaccine-tags">
              <el-tag 
                v-for="vaccine in row.vaccines" 
                :key="vaccine"
                size="small"
                effect="plain"
                type="success"
              >
                {{ vaccine }}
              </el-tag>
              <el-tag v-if="!row.vaccines?.length" size="small" type="info">未接种</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="上次就诊" width="140" align="center">
          <template #default="{ row }">
            <div class="last-visit">
              <el-icon><Calendar /></el-icon>
              <span>{{ row.lastVisit || '无记录' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">详情</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
            <el-form-item label="宠物名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入宠物名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="宠物种类" prop="type">
              <el-select v-model="formData.type" placeholder="请选择种类" style="width: 100%">
                <el-option label="狗" value="dog" />
                <el-option label="猫" value="cat" />
                <el-option label="兔子" value="rabbit" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品种" prop="breed">
              <el-input v-model="formData.breed" placeholder="请输入品种" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio label="male">公</el-radio>
                <el-radio label="female">母</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="formData.age" :min="0" :max="30" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number v-model="formData.weight" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主人姓名" prop="ownerName">
              <el-input v-model="formData.ownerName" placeholder="请输入主人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主人电话" prop="ownerPhone">
              <el-input v-model="formData.ownerPhone" placeholder="请输入主人电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="健康状态" prop="healthStatus">
          <el-radio-group v-model="formData.healthStatus">
            <el-radio label="healthy">健康</el-radio>
            <el-radio label="treatment">治疗中</el-radio>
            <el-radio label="checkup">需复查</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="疫苗记录">
          <el-select
            v-model="formData.vaccines"
            multiple
            collapse-tags
            placeholder="请选择已接种疫苗"
            style="width: 100%"
          >
            <el-option label="狂犬疫苗" value="狂犬疫苗" />
            <el-option label="犬六联" value="犬六联" />
            <el-option label="犬细小" value="犬细小" />
            <el-option label="猫三联" value="猫三联" />
            <el-option label="猫四联" value="猫四联" />
          </el-select>
        </el-form-item>

        <el-form-item label="过敏史">
          <el-input v-model="formData.allergies" type="textarea" :rows="2" placeholder="请输入过敏史，无则留空" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog 
      title="宠物档案详情" 
      v-model="detailVisible" 
      width="700px"
      class="pet-detail-dialog"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="宠物名称">{{ currentPet.name }}</el-descriptions-item>
        <el-descriptions-item label="宠物种类">{{ getPetEmoji(currentPet.type) }} {{ getPetTypeName(currentPet.type) }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ currentPet.breed }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentPet.gender === 'male' ? '公 ♂' : '母 ♀' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentPet.age }}岁</el-descriptions-item>
        <el-descriptions-item label="体重">{{ currentPet.weight }}kg</el-descriptions-item>
        <el-descriptions-item label="主人姓名">{{ currentPet.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="主人电话">{{ currentPet.ownerPhone }}</el-descriptions-item>
        <el-descriptions-item label="健康状态" :span="2">
          <el-tag :type="getHealthType(currentPet.healthStatus)">
            {{ getHealthText(currentPet.healthStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="疫苗记录" :span="2">
          <el-tag 
            v-for="vaccine in currentPet.vaccines || []" 
            :key="vaccine"
            type="success"
            style="margin-right: 8px; margin-bottom: 5px;"
          >
            {{ vaccine }}
          </el-tag>
          <span v-if="!currentPet.vaccines?.length">未接种</span>
        </el-descriptions-item>
        <el-descriptions-item label="过敏史" :span="2">{{ currentPet.allergies || '无' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentPet.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshRight, Download, Edit, Delete, View, Phone, Calendar } from '@element-plus/icons-vue'
import { 
  getPetList, 
  addPet, 
  updatePet, 
  deletePet,
  exportPets 
} from '@/api/admin/admin'

const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增档案')
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
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择宠物种类', trigger: 'change' }],
  breed: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  ownerName: [{ required: true, message: '请输入主人姓名', trigger: 'blur' }],
  ownerPhone: [{ required: true, message: '请输入主人电话', trigger: 'blur' }],
  healthStatus: [{ required: true, message: '请选择健康状态', trigger: 'change' }]
}

const tableData = ref([])

// 获取宠物列表 - 不分页
const fetchPetList = async () => {
  loading.value = true
  try {
    const res = await getPetList(searchForm.keyword)
    console.log('宠物列表响应:', res)  // 添加调试日志
    
    if (res.code === 200) {
      tableData.value = res.data || []
      if (tableData.value.length === 0) {
        console.log('返回数据为空数组，检查数据库是否有数据')
      }
    } else {
      ElMessage.error(res.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取宠物列表失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error(error.response?.data?.message || '获取宠物列表失败')
  } finally {
    loading.value = false
  }
}

const getPetEmoji = (type) => {
  const emojis = { dog: '🐕', cat: '🐱', rabbit: '🐰', other: '🐹' }
  return emojis[type] || '🐾'
}

const getPetTypeName = (type) => {
  const names = { dog: '狗', cat: '猫', rabbit: '兔子', other: '其他' }
  return names[type] || '未知'
}

const getHealthType = (status) => {
  const types = { healthy: 'success', treatment: 'danger', checkup: 'warning' }
  return types[status] || 'info'
}

const getHealthText = (status) => {
  const texts = { healthy: '健康', treatment: '治疗中', checkup: '需复查' }
  return texts[status] || '未知'
}

const handleSearch = () => {
  fetchPetList()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleExport = async () => {
  try {
    const res = await exportPets(searchForm)
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `宠物档案_${new Date().toLocaleDateString()}.xlsx`
    link.click()
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增档案'
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
  dialogTitle.value = '编辑档案'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除宠物 "${row.name}" 的档案吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePet(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchPetList()
      }
    } catch (error) {
      ElMessage.error('删除失败')
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
      ElMessage.success(formData.id ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      fetchPetList()
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
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