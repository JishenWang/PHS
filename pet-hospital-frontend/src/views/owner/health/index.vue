<template>
  <div class="health-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ $t('health.title') }}</h2>
        <p class="page-subtitle">{{ $t('health.subtitle') }}</p>
      </div>
      <el-button type="primary" size="large" @click="quickAdd" class="add-btn">
        <el-icon><Plus /></el-icon>
        {{ $t('health.addRecord') }}
      </el-button>
    </div>

    <!-- 宠物筛选 -->
    <div class="pet-filter-section">
      <div class="filter-title">{{ $t('health.selectPet') }}</div>
      <div class="pet-filter">
        <div 
          v-for="pet in petList" 
          :key="pet.id"
          class="pet-chip"
          :class="{ active: selectedPetId === pet.id }"
          @click="selectPet(pet.id)"
        >
          <span class="pet-emoji">{{ pet.gender === 'male' ? '🐕' : '🐱' }}</span>
          <span>{{ pet.name }}</span>
        </div>
        <div 
          class="pet-chip"
          :class="{ active: selectedPetId === null }"
          @click="selectPet(null)"
        >
          <span>📋</span>
          <span>{{ $t('common.all') }}</span>
        </div>
      </div>
    </div>

    <!-- 记录类型Tab -->
    <div class="type-tabs">
      <div 
        v-for="tab in recordTabs" 
        :key="tab.key"
        class="type-tab"
        :class="{ active: activeRecordTab === tab.key }"
        @click="activeRecordTab = tab.key; loadRecords()"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 就诊记录列表 -->
    <div v-if="activeRecordTab === 'hospital'" class="records-container" v-loading="loading">
      <div v-for="record in hospitalRecords" :key="record.id" class="medical-card">
        <div class="card-header">
          <div class="header-left">
            <div class="type-badge" :class="record.type">
              {{ getTypeName(record.type) }}
            </div>
            <span class="date">{{ formatDate(record.createTime) }}</span>
          </div>
          <div class="pet-info-tag">
            <span>🐾 {{ record.petName || $t('health.unknownPet') }}</span>
          </div>
        </div>
        
        <div class="card-body">
          <h4 class="title">{{ record.title }}</h4>
          <p class="content">{{ record.content }}</p>
          
          <div v-if="record.diagnosis" class="diagnosis-box">
            <div class="diagnosis-header">
              <span>🏥 {{ $t('health.diagnosisResult') }}</span>
            </div>
            <p>{{ record.diagnosis }}</p>
          </div>
          
          <div v-if="record.prescription" class="prescription-box">
            <div class="prescription-header">
              <span>💊 {{ $t('health.medicationPlan') }}</span>
            </div>
            <p>{{ record.prescription }}</p>
          </div>
        </div>
        
        <div class="card-footer">
          <div v-if="record.doctorName" class="doctor-info">
            <el-icon><User /></el-icon>
            <span>{{ $t('health.doctor') }}：{{ record.doctorName }}</span>
          </div>
          <el-button type="primary" link size="small" @click="viewRecordDetail(record)">
            {{ $t('health.viewDetails') }} <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <el-empty v-if="hospitalRecords.length === 0" :description="$t('health.noMedicalRecords')" />
    </div>

    <!-- 自填记录列表 -->
    <div v-if="activeRecordTab === 'owner'" class="records-container" v-loading="loading">
      <div class="quick-add-card" @click="quickAdd">
        <div class="quick-add-icon">📝</div>
        <div class="quick-add-text">
          <div class="title">{{ $t('health.recordDaily') }}</div>
          <div class="desc">{{ $t('health.recordDailyDesc') }}</div>
        </div>
        <el-button type="primary" size="small" circle>
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>

      <div v-for="record in ownerRecords" :key="record.id" class="owner-card">
        <div class="card-header">
          <div class="header-left">
            <div class="type-badge owner">
              {{ getOwnerTypeName(record.type) }}
            </div>
            <span class="date">{{ formatDate(record.createTime) }}</span>
          </div>
          <div class="pet-info-tag">
            <span>🐾 {{ record.petName || $t('health.unknownPet') }}</span>
          </div>
          <el-button type="danger" link class="delete-btn" @click="deleteRecord(record)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        
        <div class="card-body">
          <h4 class="title">{{ record.title }}</h4>
          <p class="content">{{ record.content }}</p>
          <div v-if="record.recordDate" class="record-date">
            📅 {{ $t('health.recordDate') }}：{{ record.recordDate }}
          </div>
        </div>
      </div>
      
      <el-empty v-if="ownerRecords.length === 0" :description="$t('health.noOwnerRecords')" />
    </div>

    <!-- 就诊详情弹窗（含处方） -->
    <el-dialog v-model="detailDialogVisible" :title="$t('health.visitDetails')" width="720px" class="visit-detail-dialog">
      <div v-loading="detailLoading" class="detail-content">
        <div v-if="recordDetail" class="detail-info">
          <!-- 顶部信息卡片 -->
          <div class="detail-header-card">
            <div class="header-item">
              <el-icon><Calendar /></el-icon>
              <div>
                <div class="header-label">{{ $t('health.visitTime') }}</div>
                <div class="header-value">{{ formatDateTime(recordDetail.createTime || recordDetail.visitTime) }}</div>
              </div>
            </div>
            <div class="header-item">
              <el-icon><User /></el-icon>
              <div>
                <div class="header-label">{{ $t('health.doctor') }}</div>
                <div class="header-value">{{ recordDetail.doctorName || '-' }}</div>
              </div>
            </div>
          </div>

          <!-- 诊断结果 -->
          <div class="diagnosis-card" v-if="recordDetail.diagnosis">
            <div class="diagnosis-label">
              <el-icon><WarningFilled /></el-icon>
              {{ $t('health.diagnosisLabel') }}
            </div>
            <div class="diagnosis-value">{{ recordDetail.diagnosis }}</div>
          </div>

          <!-- 主诉与症状 -->
          <div class="detail-section">
            <h4 class="section-title">{{ $t('health.chiefComplaintSymptoms') }}</h4>
            <div class="info-grid">
              <div class="info-item" v-if="recordDetail.chiefComplaint">
                <span class="info-label">{{ $t('health.chiefComplaint') }}</span>
                <span class="info-value">{{ recordDetail.chiefComplaint }}</span>
              </div>
              <div class="info-item" v-if="recordDetail.symptoms">
                <span class="info-label">{{ $t('health.symptomDescription') }}</span>
                <span class="info-value">{{ recordDetail.symptoms }}</span>
              </div>
            </div>
          </div>

          <!-- 治疗方案 -->
          <div class="detail-section" v-if="recordDetail.treatmentPlan">
            <h4 class="section-title">{{ $t('health.treatmentPlan') }}</h4>
            <div class="text-block">{{ recordDetail.treatmentPlan }}</div>
          </div>

          <!-- 医嘱建议 -->
          <div class="detail-section" v-if="recordDetail.doctorAdvice">
            <h4 class="section-title">{{ $t('health.doctorsAdvice') }}</h4>
            <div class="text-block advice-block">{{ recordDetail.doctorAdvice }}</div>
          </div>

          <!-- 备注 -->
          <div class="detail-section" v-if="recordDetail.remark">
            <h4 class="section-title">{{ $t('health.remarks') }}</h4>
            <div class="text-block remark-block">{{ recordDetail.remark }}</div>
          </div>

          <!-- 处方 -->
          <div class="detail-section">
            <h4 class="section-title">{{ $t('health.prescription') }}</h4>
            <div v-if="prescriptionList.length > 0">
              <el-table :data="prescriptionList" size="small" border class="prescription-table">
                <el-table-column :label="$t('health.drugName')" min-width="180">
                  <template #default="{ row }">
                    <span class="drug-name">{{ row.drugName || row.medicineName || '-' }}</span>
                    <div v-if="row.specification" class="drug-spec">{{ row.specification }}</div>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('health.dosage')" width="90" align="center">
                  <template #default="{ row }">
                    {{ row.dosage || row.dose || '-' }}
                  </template>
                </el-table-column>
                <el-table-column :label="$t('health.usage')" width="90" align="center">
                  <template #default="{ row }">
                    {{ row.usage || row.usageMethod || '-' }}
                  </template>
                </el-table-column>
                <el-table-column :label="$t('health.frequency')" width="90" align="center">
                  <template #default="{ row }">
                    {{ row.frequency || '-' }}
                  </template>
                </el-table-column>
                <el-table-column :label="$t('health.days')" width="70" align="center">
                  <template #default="{ row }">
                    {{ row.days || row.duration || '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-else :description="$t('health.noPrescriptionRecords')" :image-size="80" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- 添加记录弹窗 -->
    <el-dialog v-model="dialogVisible" :title="$t('health.addHealthRecord')" width="480px" class="add-dialog">
      <el-form :model="recordForm" label-width="80px" size="small">
        <el-form-item :label="$t('health.selectPet')">
          <el-select v-model="recordForm.petId" :placeholder="$t('health.pleaseSelectAPet')" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name" :value="pet.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('health.recordType')">
          <el-radio-group v-model="recordForm.type">
            <el-radio value="weight">{{ $t('health.weightRecord') }}</el-radio>
            <el-radio value="deworming">{{ $t('health.dewormingRecord') }}</el-radio>
            <el-radio value="daily">{{ $t('health.dailyObservation') }}</el-radio>
            <el-radio value="other">{{ $t('health.other') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('health.titleLabel')">
          <el-input v-model="recordForm.title" :placeholder="$t('health.pleaseEnterATitle')" />
        </el-form-item>
        <el-form-item :label="$t('health.content')">
          <el-input type="textarea" v-model="recordForm.content" :placeholder="$t('health.pleaseDescribe')" :rows="4" />
        </el-form-item>
        <el-form-item :label="$t('health.recordDate')">
          <el-date-picker v-model="recordForm.recordDate" type="date" :placeholder="$t('health.selectDate')" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitting">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, User, ArrowRight, Calendar, WarningFilled } from '@element-plus/icons-vue'
import { getPetList } from '@/api/owner/owner'
import { getHealthRecords, getOwnerHealthRecords, addOwnerHealthRecord, deleteOwnerHealthRecord, getHealthRecordDetailWithPrescription } from '@/api/owner/owner'

const { t } = useI18n()
const loading = ref(false)
const submitting = ref(false)
const petList = ref([])
const selectedPetId = ref(null)
const activeRecordTab = ref('hospital')
const hospitalRecords = ref([])
const ownerRecords = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const recordDetail = ref(null)
const prescriptionList = ref([])

const recordTabs = ref([
  { key: 'hospital', label: '🏥 ' + t('health.medicalRecords') },
  { key: 'owner', label: '📝 ' + t('health.selfRecorded') }
])

const recordForm = ref({
  petId: '',
  type: 'weight',
  title: '',
  content: '',
  recordDate: ''
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const getTypeName = (type) => {
  const names = { vaccine: t('health.vaccine'), deworming: t('health.deworming'), exam: t('health.exam'), treatment: t('health.treatment') }
  return names[type] || type
}

const getOwnerTypeName = (type) => {
  const names = { weight: t('health.weight'), deworming: t('health.deworming'), daily: t('health.dailyObservation'), other: t('health.other') }
  return names[type] || type
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

const loadPets = async () => {
  try {
    const res = await getPetList({ page: 1, pageSize: 100 })
    if (res.code === 200) {
      petList.value = res.data?.data || res.data?.records || []
    }
  } catch (error) {
    console.error('加载宠物列表失败:', error)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const params = { page: 1, pageSize: 50 }
    if (selectedPetId.value) {
      params.petId = selectedPetId.value
    }
    
    // 并行请求就诊记录和自填记录
    const [hospitalRes, ownerRes] = await Promise.all([
      getHealthRecords(params),
      getOwnerHealthRecords(params)
    ])
    
    if (hospitalRes.code === 200) {
      let hospitalData = hospitalRes.data?.data || hospitalRes.data?.records || []
      // 补充宠物名称
      hospitalData = hospitalData.map(record => {
        const pet = petList.value.find(p => String(p.id) === String(record.petId))
        return { ...record, petName: pet?.name || t('health.unknownPet') }
      })
      hospitalRecords.value = hospitalData
      console.log('就诊记录数:', hospitalRecords.value.length)
    }
    
    if (ownerRes.code === 200) {
      let ownerData = ownerRes.data?.data || ownerRes.data?.records || []
      // 补充宠物名称
      ownerData = ownerData.map(record => {
        const pet = petList.value.find(p => String(p.id) === String(record.petId))
        return { ...record, petName: pet?.name || t('health.unknownPet') }
      })
      ownerRecords.value = ownerData
      console.log('自填记录数:', ownerRecords.value.length)
    }
  } catch (error) {
    console.error('加载记录失败:', error)
  } finally {
    loading.value = false
  }
}

const selectPet = (petId) => {
  selectedPetId.value = petId
  loadRecords()
}

const quickAdd = () => {
  if (petList.value.length === 0) {
    ElMessage.warning(t('health.pleaseAddPetFirst'))
    return
  }
  recordForm.value = {
    petId: selectedPetId.value || petList.value[0]?.id || '',
    type: 'weight',
    title: '',
    content: '',
    recordDate: ''
  }
  dialogVisible.value = true
}

const submitRecord = async () => {
  if (!recordForm.value.petId) {
    ElMessage.warning(t('health.pleaseSelectAPet'))
    return
  }
  if (!recordForm.value.title) {
    ElMessage.warning(t('health.pleaseEnterATitle'))
    return
  }
  if (!recordForm.value.content) {
    ElMessage.warning(t('health.pleaseEnterContent'))
    return
  }
  submitting.value = true
  try {
    const res = await addOwnerHealthRecord(recordForm.value)
    if (res.code === 200) {
      ElMessage.success(t('health.addedSuccessfully'))
      dialogVisible.value = false
      // 延迟刷新，确保数据库已提交
      setTimeout(() => {
        loadRecords()
      }, 500)
    } else {
      ElMessage.error(res.message || res.msg || t('health.failedToAdd'))
    }
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error(t('health.failedToAdd'))
  } finally {
    submitting.value = false
  }
}

const deleteRecord = (record) => {
  ElMessageBox.confirm(t('health.confirmDeleteRecord'), t('common.tip'), { type: 'warning' }).then(async () => {
    try {
      await deleteOwnerHealthRecord(record.id)
      ElMessage.success(t('health.deletedSuccessfully'))
      loadRecords()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(t('health.deleteFailed'))
    }
  })
}

// 格式化时间
const formatDateTime = (time) => {
  if (!time) return '-'
  const str = String(time)
  return str.replace('T', ' ').replace(/\.\d+$/, '')
}

// 查看就诊详情（含处方）
const viewRecordDetail = async (record) => {
  const recordId = record.id || record.recordId
  if (!recordId) {
    // 降级：直接显示列表中的数据
    recordDetail.value = record
    prescriptionList.value = []
    detailDialogVisible.value = true
    return
  }

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
      recordDetail.value = record
    }
  } catch (error) {
    console.error(t('health.failedToGetDetails'), error)
    recordDetail.value = record
  } finally {
    detailLoading.value = false
  }
}

onMounted(async () => {
  await loadPets()
  loadRecords()
})
</script>

<style scoped lang="scss">
.health-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 4px;
    }
    
    .page-subtitle {
      font-size: 14px;
      color: #64748b;
    }
    
    .add-btn {
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
      }
    }
  }
  
  .pet-filter-section {
    background: white;
    border-radius: 20px;
    padding: 16px 20px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    
    .filter-title {
      font-size: 14px;
      font-weight: 500;
      color: #64748b;
      margin-bottom: 12px;
    }
    
    .pet-filter {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      
      .pet-chip {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        background: #f8fafc;
        border-radius: 30px;
        cursor: pointer;
        transition: all 0.3s;
        font-size: 14px;
        color: #64748b;
        
        .pet-emoji {
          font-size: 16px;
        }
        
        &:hover {
          background: #e2e8f0;
        }
        
        &.active {
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          color: white;
          box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
        }
      }
    }
  }
  
  .type-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
    background: white;
    padding: 8px;
    border-radius: 50px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    
    .type-tab {
      flex: 1;
      text-align: center;
      padding: 10px 0;
      border-radius: 40px;
      cursor: pointer;
      transition: all 0.3s;
      font-size: 14px;
      font-weight: 500;
      color: #64748b;
      
      &:hover {
        background: #f8fafc;
      }
      
      &.active {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
      }
    }
  }
  
  .records-container {
    .medical-card, .owner-card {
      background: white;
      border-radius: 20px;
      margin-bottom: 16px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      transition: all 0.3s;
      
      &:hover {
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      }
      
      .card-header {
        padding: 16px 20px;
        background: #f8fafc;
        border-bottom: 1px solid #e2e8f0;
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 12px;
        
        .header-left {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .type-badge {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
            
            &.vaccine {
              background: #d1fae5;
              color: #059669;
            }
            &.deworming {
              background: #fed7aa;
              color: #ea580c;
            }
            &.exam {
              background: #dbeafe;
              color: #2563eb;
            }
            &.treatment {
              background: #fee2e2;
              color: #dc2626;
            }
            &.owner {
              background: #e0e7ff;
              color: #4f46e5;
            }
          }
          
          .date {
            font-size: 12px;
            color: #94a3b8;
          }
        }
        
        .pet-info-tag {
          span {
            font-size: 12px;
            padding: 4px 10px;
            background: #e2e8f0;
            border-radius: 20px;
            color: #64748b;
          }
        }
        
        .delete-btn {
          opacity: 0;
          transition: opacity 0.3s;
        }
      }
      
      &:hover .delete-btn {
        opacity: 1;
      }
      
      .card-body {
        padding: 20px;
        
        .title {
          font-size: 16px;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 8px;
        }
        
        .content {
          font-size: 14px;
          color: #64748b;
          line-height: 1.5;
          margin-bottom: 12px;
        }
        
        .record-date {
          font-size: 12px;
          color: #94a3b8;
          margin-top: 8px;
        }
        
        .diagnosis-box {
          background: #fef3c7;
          border-radius: 12px;
          padding: 12px;
          margin-top: 12px;
          
          .diagnosis-header {
            font-size: 12px;
            color: #d97706;
            margin-bottom: 6px;
          }
          
          p {
            font-size: 13px;
            color: #1e293b;
          }
        }
        
        .prescription-box {
          background: #f0fdf4;
          border-radius: 12px;
          padding: 12px;
          margin-top: 12px;
          
          .prescription-header {
            font-size: 12px;
            color: #059669;
            margin-bottom: 6px;
          }
          
          p {
            font-size: 13px;
            color: #1e293b;
          }
        }
      }
      
      .card-footer {
        padding: 12px 20px;
        background: #f8fafc;
        border-top: 1px solid #e2e8f0;
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 12px;
        color: #94a3b8;

        .doctor-info {
          display: flex;
          align-items: center;
          gap: 6px;
        }
      }
    }

    .quick-add-card {
      background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
      border-radius: 20px;
      padding: 20px;
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
      }
      
      .quick-add-icon {
        font-size: 40px;
      }
      
      .quick-add-text {
        flex: 1;
        
        .title {
          font-size: 16px;
          font-weight: 600;
          color: #0369a1;
        }
        
        .desc {
          font-size: 12px;
          color: #0c4a6e;
          margin-top: 4px;
        }
      }
    }
  }
}

.detail-content {
  .detail-header-card {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 20px;

    .header-item {
      display: flex;
      align-items: center;
      gap: 12px;
      background: #f8fafc;
      border-radius: 10px;
      padding: 14px 16px;

      .el-icon {
        font-size: 22px;
        color: #3b82f6;
      }

      .header-label {
        font-size: 12px;
        color: #94a3b8;
        margin-bottom: 4px;
      }

      .header-value {
        font-size: 14px;
        font-weight: 600;
        color: #1e293b;
      }
    }
  }

  .diagnosis-card {
    background: #fef2f2;
    border-left: 4px solid #ef4444;
    border-radius: 0 10px 10px 0;
    padding: 16px;
    margin-bottom: 20px;

    .diagnosis-label {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: #dc2626;
      margin-bottom: 8px;

      .el-icon {
        font-size: 16px;
      }
    }

    .diagnosis-value {
      font-size: 14px;
      color: #7f1d1d;
      line-height: 1.6;
      font-weight: 500;
    }
  }

  .detail-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 12px;
      padding-left: 10px;
      border-left: 3px solid #3b82f6;
    }

    .info-grid {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .info-item {
        display: flex;
        align-items: flex-start;
        line-height: 1.6;

        .info-label {
          width: 70px;
          flex-shrink: 0;
          color: #64748b;
          font-size: 13px;
          margin-right: 8px;
          text-align: right;
        }

        .info-label::after {
          content: '：';
        }

        .info-value {
          flex: 1;
          color: #1e293b;
          font-size: 13px;
          word-break: break-all;
        }
      }
    }

    .text-block {
      background: #f8fafc;
      border-radius: 10px;
      padding: 14px 16px;
      font-size: 13px;
      color: #334155;
      line-height: 1.8;
      white-space: pre-wrap;
    }

    .advice-block {
      background: #eff6ff;
      border: 1px solid #dbeafe;
      color: #1e40af;
    }

    .remark-block {
      background: #fffbeb;
      border: 1px solid #fef3c7;
      color: #92400e;
    }

    .prescription-table {
      border-radius: 10px;
      overflow: hidden;

      :deep(.el-table__header-wrapper) {
        th {
          background-color: #f1f5f9;
          color: #475569;
          font-weight: 600;
          font-size: 13px;
          padding: 10px 0;
        }
      }

      :deep(.el-table__cell) {
        font-size: 13px;
        padding: 10px 0;
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
      padding: 32px 0;
      background: #f8fafc;
      border-radius: 10px;
    }
  }
}

.add-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
  }
  
  :deep(.el-radio-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
}
</style>