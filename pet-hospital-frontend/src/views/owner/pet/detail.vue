<template>
  <div class="pet-detail-page">
    <div class="page-nav">
      <el-button link @click="goBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('ownerPetDetail.back') }}
      </el-button>
    </div>

    <div v-loading="loading">
      <!-- 宠物信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <span class="card-title">{{ $t('ownerPetDetail.basicInfo') }}</span>
          <el-button type="primary" link @click="handleEdit">
            <el-icon><Edit /></el-icon>
            {{ $t('ownerPetDetail.edit') }}
          </el-button>
        </div>
        <div class="pet-info">
          <div class="pet-avatar">
            <div class="avatar-placeholder">
              <el-icon size="50"><Avatar /></el-icon>
            </div>
            <div class="pet-gender-badge" :class="petDetail.gender === 'male' ? 'male' : 'female'">
              {{ petDetail.gender === 'male' ? $t('ownerPetDetail.male') : $t('ownerPetDetail.female') }}
            </div>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">{{ $t('ownerPetDetail.petName') }}</span>
              <span class="value">{{ petDetail.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('ownerPetDetail.breed') }}</span>
              <span class="value">{{ petDetail.breed }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('ownerPetDetail.gender') }}</span>
              <span class="value">{{ petDetail.gender === 'male' ? $t('ownerPetDetail.male') : $t('ownerPetDetail.female') }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('ownerPetDetail.birthday') }}</span>
              <span class="value">{{ petDetail.birthday }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('ownerPetDetail.age') }}</span>
              <span class="value">{{ petDetail.age }} {{ $t('ownerPetDetail.years') }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('ownerPetDetail.weight') }}</span>
              <span class="value">{{ petDetail.weight }}kg</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('ownerPetDetail.chipNumber') }}</span>
              <span class="value">{{ petDetail.chipNumber || $t('ownerPetDetail.notRecorded') }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('ownerPetDetail.neuteredStatus') }}</span>
              <span class="value">{{ petDetail.neutered ? $t('ownerPetDetail.neutered') : $t('ownerPetDetail.notNeutered') }}</span>
            </div>
            <div class="info-item full-width">
              <span class="label">{{ $t('ownerPetDetail.description') }}</span>
              <span class="value">{{ petDetail.description || $t('ownerPetDetail.none') }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 健康评分卡片 -->
      <div class="health-card">
        <div class="card-header">
          <span class="card-title">{{ $t('ownerPetDetail.healthScore') }}</span>
        </div>
        <div class="health-score">
          <el-progress type="circle" :percentage="healthScore" :width="120" :stroke-width="8" :color="scoreColor" />
          <div class="score-suggestions">
            <h4>{{ $t('ownerPetDetail.healthSuggestions') }}</h4>
            <p v-for="(suggestion, index) in healthSuggestions" :key="index">
              <el-icon><Check /></el-icon>
              {{ suggestion }}
            </p>
          </div>
        </div>
      </div>

      <!-- 就诊记录与处方卡片 -->
      <div class="records-card">
        <div class="card-header">
          <span class="card-title">{{ $t('ownerPetDetail.healthRecords') }}</span>
          <el-button type="primary" link @click="addRecord">+ {{ $t('ownerPetDetail.addRecord') }}</el-button>
        </div>
        <div class="records-list">
          <div v-for="record in recordList" :key="record.id" class="record-item">
            <div class="record-dot" :class="getRecordDotType(record)"></div>
            <div class="record-content">
              <div class="record-header">
                <el-tag :type="getTagType(record.type || record.recordType)" size="small">
                  {{ getTypeName(record.type || record.recordType) }}
                </el-tag>
                <span class="record-title">{{ record.diagnosis || record.title || $t('ownerPetDetail.medicalRecord') }}</span>
                <span class="record-time">{{ record.createTime || record.visitTime }}</span>
              </div>
              <p class="record-desc">{{ record.chiefComplaint || record.symptoms || record.content || '' }}</p>
              <div class="record-footer">
                <div class="record-doctor" v-if="record.doctorName">
                  <el-icon><User /></el-icon>
                  <span>{{ record.doctorName }}</span>
                </div>
                <el-button type="primary" link size="small" @click="viewRecordDetail(record)">
                  {{ $t('ownerPetDetail.viewDetail') }} <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="recordList.length === 0" :description="$t('ownerPetDetail.noHealthRecords')" />
        </div>
      </div>
    </div>

    <!-- 编辑宠物弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="$t('ownerPetDetail.editPet')" width="480px">
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="80px" size="small">
        <el-form-item :label="$t('ownerPetDetail.petName')" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.breed')" prop="breed">
          <el-input v-model="editForm.breed" />
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.gender')" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio value="male">{{ $t('ownerPetDetail.male') }}</el-radio>
            <el-radio value="female">{{ $t('ownerPetDetail.female') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.birthday')" prop="birthday">
          <el-date-picker v-model="editForm.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.weight')" prop="weight">
          <el-input-number v-model="editForm.weight" :min="0" :precision="1" /> kg
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.chipNumber')" prop="chipNumber">
          <el-input v-model="editForm.chipNumber" />
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.neuteredStatus')" prop="neutered">
          <el-switch v-model="editForm.neutered" />
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.description')" prop="description">
          <el-input type="textarea" v-model="editForm.description" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">{{ $t('ownerPetDetail.cancel') }}</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">{{ $t('ownerPetDetail.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 就诊详情弹窗（含处方） -->
    <el-dialog v-model="detailDialogVisible" :title="$t('ownerPetDetail.recordDetail')" width="560px">
      <div v-loading="detailLoading" class="detail-dialog-content">
        <div v-if="recordDetail" class="detail-info">
          <div class="detail-section">
            <h4 class="section-title">{{ $t('ownerPetDetail.basicInfo') }}</h4>
            <div class="detail-row">
              <span class="detail-label">{{ $t('ownerPetDetail.visitTime') }}</span>
              <span class="detail-value">{{ recordDetail.createTime || recordDetail.visitTime || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ $t('ownerPetDetail.doctor') }}</span>
              <span class="detail-value">{{ recordDetail.doctorName || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ $t('ownerPetDetail.chiefComplaint') }}</span>
              <span class="detail-value">{{ recordDetail.chiefComplaint || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ $t('ownerPetDetail.symptoms') }}</span>
              <span class="detail-value">{{ recordDetail.symptoms || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ $t('ownerPetDetail.diagnosis') }}</span>
              <span class="detail-value">{{ recordDetail.diagnosis || '-' }}</span>
            </div>
            <div class="detail-row" v-if="recordDetail.treatmentPlan">
              <span class="detail-label">{{ $t('ownerPetDetail.treatmentPlan') }}</span>
              <span class="detail-value">{{ recordDetail.treatmentPlan }}</span>
            </div>
            <div class="detail-row" v-if="recordDetail.doctorAdvice">
              <span class="detail-label">{{ $t('ownerPetDetail.doctorsAdvice') }}</span>
              <span class="detail-value">{{ recordDetail.doctorAdvice }}</span>
            </div>
            <div class="detail-row" v-if="recordDetail.remark">
              <span class="detail-label">{{ $t('ownerPetDetail.remarks') }}</span>
              <span class="detail-value">{{ recordDetail.remark }}</span>
            </div>
          </div>

          <div class="detail-section" v-if="prescriptionList.length > 0">
            <h4 class="section-title">{{ $t('ownerPetDetail.prescription') }}</h4>
            <el-table :data="prescriptionList" size="small" border class="prescription-table">
              <el-table-column :label="$t('ownerPetDetail.drugName')" min-width="140">
                <template #default="{ row }">
                  <span class="drug-name">{{ row.drugName || row.itemName || '-' }}</span>
                  <div v-if="row.specification" class="drug-spec">{{ row.specification }}</div>
                </template>
              </el-table-column>
              <el-table-column :label="$t('ownerPetDetail.dosage')" prop="dosage" width="80" align="center" />
              <el-table-column :label="$t('ownerPetDetail.frequency')" prop="frequency" width="80" align="center" />
              <el-table-column :label="$t('ownerPetDetail.days')" prop="days" width="60" align="center" />
            </el-table>
          </div>
          <el-empty v-else :description="$t('ownerPetDetail.noPrescription')" />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">{{ $t('ownerPetDetail.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 添加记录弹窗 -->
    <el-dialog v-model="recordDialogVisible" :title="$t('ownerPetDetail.addHealthRecord')" width="480px">
      <el-form :model="recordForm" label-width="80px" size="small">
        <el-form-item :label="$t('ownerPetDetail.recordType')">
          <el-select v-model="recordForm.type" :placeholder="$t('ownerPetDetail.pleaseSelectType')" style="width: 100%">
            <el-option :label="$t('ownerPetDetail.vaccine')" value="vaccine" />
            <el-option :label="$t('ownerPetDetail.deworming')" value="deworming" />
            <el-option :label="$t('ownerPetDetail.exam')" value="exam" />
            <el-option :label="$t('ownerPetDetail.treatment')" value="treatment" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.title')">
          <el-input v-model="recordForm.title" :placeholder="$t('ownerPetDetail.pleaseEnterTitle')" />
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.content')">
          <el-input type="textarea" v-model="recordForm.content" :placeholder="$t('ownerPetDetail.pleaseEnterContent')" :rows="4" />
        </el-form-item>
        <el-form-item :label="$t('ownerPetDetail.recordDate')">
          <el-date-picker v-model="recordForm.recordDate" type="date" :placeholder="$t('ownerPetDetail.selectDate')" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">{{ $t('ownerPetDetail.cancel') }}</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submittingRecord">{{ $t('ownerPetDetail.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, Plus, Avatar, Check, User, ArrowRight } from '@element-plus/icons-vue'
import { getPetDetail, updatePet } from '@/api/owner/owner'
import { getHealthRecords, addOwnerHealthRecord, getHealthRecordDetailWithPrescription } from '@/api/owner/owner'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const recordDetail = ref(null)
const prescriptionList = ref([])
const editFormRef = ref(null)
const healthScore = ref(85)

const healthSuggestions = ref([
  'Regular vaccinations',
  'Keep moderate exercise',
  'Regular deworming'
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
  name: [{ required: true, message: t('ownerPetDetail.pleaseEnterPetName'), trigger: 'blur' }],
  breed: [{ required: true, message: t('ownerPetDetail.pleaseEnterBreed'), trigger: 'blur' }],
  birthday: [{ required: true, message: t('ownerPetDetail.pleaseSelectBirthday'), trigger: 'change' }]
}

const getTagType = (type) => {
  const types = { vaccine: 'success', deworming: 'warning', exam: 'info', treatment: 'danger', 门诊: 'danger', 疫苗: 'success', 体检: 'info', 手术: 'warning' }
  return types[type] || 'info'
}

const getTypeName = (type) => {
  const names = {
    vaccine: t('ownerPetDetail.vaccine'),
    deworming: t('ownerPetDetail.deworming'),
    exam: t('ownerPetDetail.exam'),
    treatment: t('ownerPetDetail.treatment'),
    '门诊': t('ownerPetDetail.treatment'),
    '疫苗': t('ownerPetDetail.vaccine'),
    '体检': t('ownerPetDetail.exam'),
    '手术': t('ownerPetDetail.treatment')
  }
  return names[type] || type
}

const getRecordDotType = (record) => {
  const type = record.type || record.recordType || ''
  const map = { vaccine: 'vaccine', deworming: 'deworming', exam: 'exam', treatment: 'treatment', 疫苗: 'vaccine', 体检: 'exam', 手术: 'treatment', 门诊: 'treatment' }
  return map[type] || 'exam'
}

const translateUsage = (val) => {
  const map = {
    'Oral': t('prescription.usageOral'),
    'External use': t('prescription.usageExternal'),
    'Injection': t('prescription.usageInjection')
  }
  return map[val] || val || '-'
}

const translateFrequency = (val) => {
  const map = {
    'Once daily': t('prescription.frequencyOnceDaily'),
    'Twice daily': t('prescription.frequencyTwiceDaily'),
    'Three times daily': t('prescription.frequencyThreeTimesDaily')
  }
  return map[val] || val || '-'
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
      ElMessage.error(res.message || res.msg || t('ownerPetDetail.failedToLoad'))
    }
  } catch (error) {
    ElMessage.error(t('ownerPetDetail.failedToLoadNetwork'))
    console.error(t('ownerPetDetail.failedToLoad'), error)
  } finally {
    loading.value = false
  }
}

const loadRecords = async () => {
  try {
    const res = await getHealthRecords({ petId: petId, page: 1, pageSize: 10 })
    if (res.code === 200) {
      recordList.value = res.data?.data || res.data?.records || res.data || []
    }
  } catch (error) {
    console.error(t('ownerPetDetail.failedToLoad'), error)
  }
}

// 查看就诊详情（含处方）
const viewRecordDetail = async (record) => {
  const recordId = record.id || record.recordId
  if (!recordId) return

  detailDialogVisible.value = true
  detailLoading.value = true
  prescriptionList.value = []
  recordDetail.value = null

  try {
    const res = await getHealthRecordDetailWithPrescription(recordId)
    if (res.code === 200 && res.data) {
      const data = res.data
      recordDetail.value = data.medicalRecord || data
      // 从 prescriptions 数组中提取所有处方明细并做字段映射
      const items = []
      if (data.prescriptions && Array.isArray(data.prescriptions)) {
        data.prescriptions.forEach(pre => {
          if (pre.items && Array.isArray(pre.items)) {
            pre.items.forEach(item => {
              items.push({
                ...item,
                drugName: item.itemName || item.drugName || item.medicineName,
                medicineName: item.itemName || item.drugName || item.medicineName,
                usage: translateUsage(item.usageMethod || item.usage),
                frequency: translateFrequency(item.frequency),
                days: item.useDays || item.days || item.duration
              })
            })
          }
        })
      }
      prescriptionList.value = items
    } else {
      // 降级：使用列表中的数据
      recordDetail.value = record
    }
  } catch (error) {
    console.error(t('ownerPetDetail.failedToLoad'), error)
    recordDetail.value = record
  } finally {
    detailLoading.value = false
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
        ElMessage.success(t('ownerPetDetail.modifiedSuccessfully'))
        editDialogVisible.value = false
        loadPetDetail()
      } catch (error) {
        console.error(t('ownerPetDetail.modificationFailed'), error)
        ElMessage.error(t('ownerPetDetail.modificationFailed'))
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
    ElMessage.warning(t('ownerPetDetail.pleaseFillAll'))
    return
  }
  submittingRecord.value = true
  try {
    await addOwnerHealthRecord({ petId: petId, ...recordForm })
    ElMessage.success(t('ownerPetDetail.addedSuccessfully'))
    recordDialogVisible.value = false
    loadRecords()
  } catch {
    ElMessage.error(t('ownerPetDetail.failedToAdd'))
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
        
        .record-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 8px;

          .record-doctor {
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

  .detail-dialog-content {
    .detail-info {
      .detail-section {
        margin-bottom: 24px;

        &:last-child {
          margin-bottom: 0;
        }

        .section-title {
          font-size: 15px;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 14px;
          padding-bottom: 8px;
          border-bottom: 1px solid #e2e8f0;
        }

        .detail-row {
          display: flex;
          margin-bottom: 10px;
          line-height: 1.5;

          .detail-label {
            width: 80px;
            flex-shrink: 0;
            color: #64748b;
            font-size: 13px;
          }

          .detail-value {
            flex: 1;
            color: #1e293b;
            font-size: 13px;
            word-break: break-all;
          }
        }

        .prescription-table {
          border-radius: 8px;
          overflow: hidden;

          :deep(.el-table__header-wrapper) {
            th {
              background-color: #f1f5f9;
              color: #475569;
              font-weight: 600;
              font-size: 13px;
            }
          }

          :deep(.el-table__cell) {
            font-size: 13px;
            padding: 8px 0;
          }

          .drug-name {
            font-weight: 500;
            color: #1e293b;
          }

          .drug-spec {
            font-size: 12px;
            color: #94a3b8;
            margin-top: 2px;
          }
        }

        .el-empty {
          padding: 24px 0;
        }
      }
    }
  }
}
</style>
