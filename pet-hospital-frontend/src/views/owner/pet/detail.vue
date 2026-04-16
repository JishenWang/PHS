<template>
  <div class="pet-detail-page">
    <div class="page-nav">
      <el-button link @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div v-loading="loading">
      <!-- 宠物信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <span class="card-title">基本信息</span>
          <el-button type="primary" link @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
        </div>
        <div class="pet-info">
          <div class="pet-avatar">
            <div class="avatar-placeholder">
              <el-icon size="50"><Avatar /></el-icon>
            </div>
            <div class="pet-gender-badge" :class="petDetail.gender === 'male' ? 'male' : 'female'">
              {{ petDetail.gender === 'male' ? '♂' : '♀' }}
            </div>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">宠物名称</span>
              <span class="value">{{ petDetail.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">品种</span>
              <span class="value">{{ petDetail.breed }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别</span>
              <span class="value">{{ petDetail.gender === 'male' ? '公' : '母' }}</span>
            </div>
            <div class="info-item">
              <span class="label">出生日期</span>
              <span class="value">{{ petDetail.birthday }}</span>
            </div>
            <div class="info-item">
              <span class="label">年龄</span>
              <span class="value">{{ petDetail.age }}岁</span>
            </div>
            <div class="info-item">
              <span class="label">体重</span>
              <span class="value">{{ petDetail.weight }}kg</span>
            </div>
            <div class="info-item">
              <span class="label">芯片号</span>
              <span class="value">{{ petDetail.chipNumber || '未录入' }}</span>
            </div>
            <div class="info-item">
              <span class="label">绝育状态</span>
              <span class="value">{{ petDetail.neutered ? '已绝育' : '未绝育' }}</span>
            </div>
            <div class="info-item full-width">
              <span class="label">描述</span>
              <span class="value">{{ petDetail.description || '暂无' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 健康评分卡片 -->
      <div class="health-card">
        <div class="card-header">
          <span class="card-title">健康评分</span>
        </div>
        <div class="health-score">
          <el-progress type="circle" :percentage="healthScore" :width="120" :stroke-width="8" :color="scoreColor" />
          <div class="score-suggestions">
            <h4>健康建议</h4>
            <p v-for="(suggestion, index) in healthSuggestions" :key="index">
              <el-icon><Check /></el-icon>
              {{ suggestion }}
            </p>
          </div>
        </div>
      </div>

      <!-- 健康记录卡片 -->
      <div class="records-card">
        <div class="card-header">
          <span class="card-title">健康记录</span>
          <el-button type="primary" link @click="addRecord">+ 添加记录</el-button>
        </div>
        <div class="records-list">
          <div v-for="record in recordList" :key="record.id" class="record-item">
            <div class="record-dot" :class="record.type"></div>
            <div class="record-content">
              <div class="record-header">
                <el-tag :type="getTagType(record.type)" size="small">{{ getTypeName(record.type) }}</el-tag>
                <span class="record-title">{{ record.title }}</span>
                <span class="record-time">{{ record.createTime }}</span>
              </div>
              <p class="record-desc">{{ record.content }}</p>
              <div class="record-doctor" v-if="record.doctorName">
                <el-icon><User /></el-icon>
                <span>{{ record.doctorName }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="recordList.length === 0" description="暂无健康记录" />
        </div>
      </div>
    </div>

    <!-- 编辑宠物弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑宠物" width="480px">
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="80px" size="small">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="editForm.breed" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio value="male">公</el-radio>
            <el-radio value="female">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker v-model="editForm.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="editForm.weight" :min="0" :precision="1" /> kg
        </el-form-item>
        <el-form-item label="芯片号" prop="chipNumber">
          <el-input v-model="editForm.chipNumber" />
        </el-form-item>
        <el-form-item label="绝育" prop="neutered">
          <el-switch v-model="editForm.neutered" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="editForm.description" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加记录弹窗 -->
    <el-dialog v-model="recordDialogVisible" title="添加健康记录" width="480px">
      <el-form :model="recordForm" label-width="80px" size="small">
        <el-form-item label="记录类型">
          <el-select v-model="recordForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="疫苗" value="vaccine" />
            <el-option label="驱虫" value="deworming" />
            <el-option label="体检" value="exam" />
            <el-option label="诊疗" value="treatment" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="recordForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="recordForm.content" placeholder="请输入内容" :rows="4" />
        </el-form-item>
        <el-form-item label="记录日期">
          <el-date-picker v-model="recordForm.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submittingRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, Plus, Avatar, Check, User } from '@element-plus/icons-vue'
import { getPetDetail, updatePet } from '@/api/owner/owner'
import { getHealthRecords, addOwnerHealthRecord } from '@/api/owner/owner'

const route = useRoute()
const router = useRouter()
const petId = route.params.id

const loading = ref(false)
const submitting = ref(false)
const submittingRecord = ref(false)
const petDetail = ref({})
const recordList = ref([])
const editDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const editFormRef = ref(null)
const healthScore = ref(85)

const healthSuggestions = ref([
  '定期进行疫苗接种',
  '保持适量运动',
  '定期驱虫'
])

const scoreColor = computed(() => {
  const score = healthScore.value
  if (score >= 80) return '#10b981'
  if (score >= 60) return '#f59e0b'
  return '#ef4444'
})

const editForm = reactive({
  name: '',
  breed: '',
  gender: 'male',
  birthday: '',
  weight: 0,
  chipNumber: '',
  neutered: false,
  description: ''
})

const recordForm = reactive({
  type: 'vaccine',
  title: '',
  content: '',
  recordDate: ''
})

const rules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  breed: [{ required: true, message: '请输入品种', trigger: 'blur' }],
  birthday: [{ required: true, message: '请选择出生日期', trigger: 'change' }]
}

const getTagType = (type) => {
  const types = { vaccine: 'success', deworming: 'warning', exam: 'info', treatment: 'danger' }
  return types[type] || 'info'
}

const getTypeName = (type) => {
  const names = { vaccine: '疫苗', deworming: '驱虫', exam: '体检', treatment: '诊疗' }
  return names[type] || type
}

const goBack = () => {
  router.push('/owner/pet')
}

const calculateAge = (birthday) => {
  if (!birthday) return 0
  const birthDate = new Date(birthday)
  const today = new Date()
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  return age
}

const loadPetDetail = async () => {
  loading.value = true
  try {
    const res = await getPetDetail(petId)
    if (res.code === 200) {
      const data = res.data?.data || res.data
      petDetail.value = {
        ...data,
        age: calculateAge(data.birthday)
      }
      Object.assign(editForm, {
        name: petDetail.value.name,
        breed: petDetail.value.breed,
        gender: petDetail.value.gender,
        birthday: petDetail.value.birthday,
        weight: petDetail.value.weight,
        chipNumber: petDetail.value.chipNumber || '',
        neutered: petDetail.value.neutered === 1 || petDetail.value.neutered === true,
        description: petDetail.value.description || ''
      })
    } else {
      ElMessage.error(res.message || res.msg || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载失败，请检查网络')
    console.error('加载宠物详情失败:', error)
  } finally {
    loading.value = false
  }
}

const loadRecords = async () => {
  try {
    const res = await getHealthRecords({ petId: petId, page: 1, pageSize: 10 })
    if (res.code === 200) {
      recordList.value = res.data?.data || res.data?.records || []
    }
  } catch (error) {
    console.error('加载健康记录失败:', error)
  }
}

const handleEdit = () => {
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const updateData = {
          name: editForm.name,
          breed: editForm.breed,
          gender: editForm.gender,
          birthday: editForm.birthday,
          weight: editForm.weight,
          chipNumber: editForm.chipNumber,
          neutered: editForm.neutered ? 1 : 0,
          description: editForm.description
        }
        await updatePet(petId, updateData)
        ElMessage.success('修改成功')
        editDialogVisible.value = false
        loadPetDetail()
      } catch (error) {
        console.error('修改失败:', error)
        ElMessage.error('修改失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const addRecord = () => {
  recordForm.type = 'vaccine'
  recordForm.title = ''
  recordForm.content = ''
  recordForm.recordDate = ''
  recordDialogVisible.value = true
}

const submitRecord = async () => {
  if (!recordForm.title || !recordForm.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submittingRecord.value = true
  try {
    await addOwnerHealthRecord({ petId: petId, ...recordForm })
    ElMessage.success('添加成功')
    recordDialogVisible.value = false
    loadRecords()
  } catch {
    ElMessage.error('添加失败')
  } finally {
    submittingRecord.value = false
  }
}

onMounted(() => {
  loadPetDetail()
  loadRecords()
})
</script>

<style scoped lang="scss">
.pet-detail-page {
  .page-nav {
    margin-bottom: 20px;
    
    .back-btn {
      color: #64748b;
      font-size: 14px;
      
      &:hover {
        color: #3b82f6;
      }
    }
  }
  
  .info-card, .health-card, .records-card {
    background: white;
    border-radius: 20px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 1px solid #e2e8f0;
      
      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
      }
    }
  }
  
  .pet-info {
    display: flex;
    gap: 30px;
    flex-wrap: wrap;
    
    .pet-avatar {
      position: relative;
      flex-shrink: 0;
      
      .avatar-placeholder {
        width: 100px;
        height: 100px;
        background: #f0f4f8;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #94a3b8;
      }
      
      .pet-gender-badge {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 28px;
        height: 28px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        font-weight: bold;
        color: white;
        
        &.male { background: #3b82f6; }
        &.female { background: #ef4444; }
      }
    }
    
    .info-grid {
      flex: 1;
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      
      .info-item {
        display: flex;
        flex-direction: column;
        
        .label {
          font-size: 12px;
          color: #94a3b8;
          margin-bottom: 4px;
        }
        
        .value {
          font-size: 14px;
          color: #1e293b;
          font-weight: 500;
        }
        
        &.full-width {
          grid-column: span 2;
        }
      }
    }
  }
  
  .health-score {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 40px;
    padding: 20px;
    flex-wrap: wrap;
    
    .score-suggestions {
      h4 {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 12px;
        color: #1e293b;
      }
      
      p {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 8px 0;
        color: #10b981;
        font-size: 14px;
      }
    }
  }
  
  .records-list {
    .record-item {
      display: flex;
      gap: 16px;
      padding: 16px 0;
      border-bottom: 1px solid #e2e8f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .record-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        margin-top: 6px;
        flex-shrink: 0;
        
        &.vaccine { background: #10b981; }
        &.deworming { background: #f59e0b; }
        &.exam { background: #3b82f6; }
        &.treatment { background: #ef4444; }
      }
      
      .record-content {
        flex: 1;
        
        .record-header {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;
          flex-wrap: wrap;
          
          .record-title {
            font-weight: 600;
            color: #1e293b;
          }
          
          .record-time {
            font-size: 12px;
            color: #94a3b8;
          }
        }
        
        .record-desc {
          font-size: 13px;
          color: #64748b;
          line-height: 1.5;
        }
        
        .record-doctor {
          margin-top: 8px;
          font-size: 12px;
          color: #94a3b8;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }
}
</style>