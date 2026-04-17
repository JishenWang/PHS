<template>
  <div class="health-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">健康记录</h2>
        <p class="page-subtitle">记录毛孩子的健康数据</p>
      </div>
      <el-button type="primary" size="large" @click="quickAdd" class="add-btn">
        <el-icon><Plus /></el-icon>
        添加记录
      </el-button>
    </div>

    <!-- 宠物筛选 -->
    <div class="pet-filter-section">
      <div class="filter-title">选择宠物</div>
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
          <span>全部</span>
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
            <span>🐾 {{ record.petName || '未知宠物' }}</span>
          </div>
        </div>
        
        <div class="card-body">
          <h4 class="title">{{ record.title }}</h4>
          <p class="content">{{ record.content }}</p>
          
          <div v-if="record.diagnosis" class="diagnosis-box">
            <div class="diagnosis-header">
              <span>🏥 诊断结果</span>
            </div>
            <p>{{ record.diagnosis }}</p>
          </div>
          
          <div v-if="record.prescription" class="prescription-box">
            <div class="prescription-header">
              <span>💊 用药方案</span>
            </div>
            <p>{{ record.prescription }}</p>
          </div>
        </div>
        
        <div class="card-footer" v-if="record.doctorName">
          <el-icon><User /></el-icon>
          <span>医生：{{ record.doctorName }}</span>
        </div>
      </div>
      
      <el-empty v-if="hospitalRecords.length === 0" description="暂无就诊记录" />
    </div>

    <!-- 自填记录列表 -->
    <div v-if="activeRecordTab === 'owner'" class="records-container" v-loading="loading">
      <div class="quick-add-card" @click="quickAdd">
        <div class="quick-add-icon">📝</div>
        <div class="quick-add-text">
          <div class="title">记录日常健康数据</div>
          <div class="desc">如体重变化、在家驱虫等</div>
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
            <span>🐾 {{ record.petName || '未知宠物' }}</span>
          </div>
          <el-button type="danger" link class="delete-btn" @click="deleteRecord(record)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        
        <div class="card-body">
          <h4 class="title">{{ record.title }}</h4>
          <p class="content">{{ record.content }}</p>
          <div v-if="record.recordDate" class="record-date">
            📅 记录日期：{{ record.recordDate }}
          </div>
        </div>
      </div>
      
      <el-empty v-if="ownerRecords.length === 0" description="暂无自填记录" />
    </div>

    <!-- 添加记录弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加健康记录" width="480px" class="add-dialog">
      <el-form :model="recordForm" label-width="80px" size="small">
        <el-form-item label="选择宠物">
          <el-select v-model="recordForm.petId" placeholder="请选择宠物" style="width: 100%">
            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name" :value="pet.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录类型">
          <el-radio-group v-model="recordForm.type">
            <el-radio value="weight">体重记录</el-radio>
            <el-radio value="deworming">驱虫记录</el-radio>
            <el-radio value="daily">日常观察</el-radio>
            <el-radio value="other">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="recordForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="recordForm.content" placeholder="请详细描述" :rows="4" />
        </el-form-item>
        <el-form-item label="记录日期">
          <el-date-picker v-model="recordForm.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, User } from '@element-plus/icons-vue'
import { getPetList } from '@/api/owner/owner'
import { getHealthRecords, getOwnerHealthRecords, addOwnerHealthRecord, deleteOwnerHealthRecord } from '@/api/owner/owner'

const loading = ref(false)
const submitting = ref(false)
const petList = ref([])
const selectedPetId = ref(null)
const activeRecordTab = ref('hospital')
const hospitalRecords = ref([])
const ownerRecords = ref([])
const dialogVisible = ref(false)

const recordTabs = ref([
  { key: 'hospital', label: '🏥 就诊记录' },
  { key: 'owner', label: '📝 自填记录' }
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
  const names = { vaccine: '疫苗', deworming: '驱虫', exam: '体检', treatment: '诊疗' }
  return names[type] || type
}

const getOwnerTypeName = (type) => {
  const names = { weight: '体重', deworming: '驱虫', daily: '日常观察', other: '其他' }
  return names[type] || type
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
        const pet = petList.value.find(p => p.id === record.petId)
        return { ...record, petName: pet?.name || '未知宠物' }
      })
      hospitalRecords.value = hospitalData
      console.log('就诊记录数:', hospitalRecords.value.length)
    }
    
    if (ownerRes.code === 200) {
      let ownerData = ownerRes.data?.data || ownerRes.data?.records || []
      // 补充宠物名称
      ownerData = ownerData.map(record => {
        const pet = petList.value.find(p => p.id === record.petId)
        return { ...record, petName: pet?.name || '未知宠物' }
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
    ElMessage.warning('请先添加宠物')
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
    ElMessage.warning('请选择宠物')
    return
  }
  if (!recordForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!recordForm.value.content) {
    ElMessage.warning('请输入内容')
    return
  }
  submitting.value = true
  try {
    const res = await addOwnerHealthRecord(recordForm.value)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      dialogVisible.value = false
      // 延迟刷新，确保数据库已提交
      setTimeout(() => {
        loadRecords()
      }, 500)
    } else {
      ElMessage.error(res.message || res.msg || '添加失败')
    }
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error('添加失败')
  } finally {
    submitting.value = false
  }
}

const deleteRecord = (record) => {
  ElMessageBox.confirm('确定删除这条记录吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteOwnerHealthRecord(record.id)
      ElMessage.success('删除成功')
      loadRecords()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  loadPets()
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
        gap: 6px;
        font-size: 12px;
        color: #94a3b8;
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