<template>
    <div class="record-page">
      <!-- 搜索区 -->
      <el-card class="search-card" shadow="hover">
        <el-form :model="searchForm" inline>
          <el-form-item label="宠物名称">
            <el-input 
              v-model="searchForm.petName" 
              placeholder="搜索宠物" 
              clearable
              :prefix-icon="Search"
              class="search-input"
            />
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
            <el-button @click="handleReset" :icon="RefreshRight">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 列表视图 -->
      <template v-if="!showForm">
        <el-card shadow="hover" class="list-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="title">病历档案</span>
                <el-radio-group v-model="viewMode" size="small">
                  <el-radio-button label="table">
                    <el-icon><Grid /></el-icon>
                  </el-radio-button>
                  <el-radio-button label="card">
                    <el-icon><Menu /></el-icon>
                  </el-radio-button>
                </el-radio-group>
              </div>
              <el-button type="primary" @click="openCreateForm" :icon="Plus">
                新建病历
              </el-button>
            </div>
          </template>

          <!-- 表格视图 -->
          <el-table 
            v-if="viewMode === 'table'"
            :data="tableData" 
            stripe 
            v-loading="loading"
            class="record-table"
            :header-cell-style="{ background: '#f8fafc', color: '#475569' }"
          >
            <el-table-column prop="recordNo" label="病历编号" width="160">
              <template #default="{ row }">
                <div class="record-no">
                  <el-icon><Document /></el-icon>
                  <span>{{ row.recordNo }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="宠物信息" min-width="180">
              <template #default="{ row }">
                <div class="pet-info">
                  <div class="name">{{ row.petName }}</div>
                  <div class="type">{{ row.petType }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="diagnosis" label="诊断结果" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="diagnosis-text">{{ row.diagnosis }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="doctorName" label="接诊医生" width="120" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag 
                  :type="row.status === 1 ? 'success' : 'warning'" 
                  effect="light"
                  round
                  size="small"
                >
                  {{ row.status === 1 ? '已完成' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" fixed="right" width="200" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
                <el-button 
                  v-if="row.status === 0" 
                  type="warning" 
                  link 
                  :icon="Edit"
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                <el-button type="success" link :icon="Download" @click="handleExport(row)">导出</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 卡片视图 -->
          <div v-else class="card-view">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="row in tableData" :key="row.recordId">
                <div class="record-card-item" :class="{ 'draft': row.status === 0 }">
                  <div class="card-header-mini">
                    <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small" round>
                      {{ row.status === 1 ? '已完成' : '草稿' }}
                    </el-tag>
                    <span class="time">{{ formatDate(row.createTime) }}</span>
                  </div>
                  <div class="card-body">
                    <h4 class="pet-name">{{ row.petName }}</h4>
                    <p class="diagnosis">{{ row.diagnosis }}</p>
                    <div class="doctor">👨‍⚕️ {{ row.doctorName }}</div>
                  </div>
                  <div class="card-footer">
                    <el-button type="primary" text size="small" @click="handleView(row)">查看</el-button>
                    <el-button 
                      v-if="row.status === 0" 
                      type="warning" 
                      text 
                      size="small" 
                      @click="handleEdit(row)"
                    >
                      编辑
                    </el-button>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              layout="total, prev, pager, next, jumper"
              @current-change="handlePageChange"
              background
            />
          </div>
        </el-card>
      </template>

      <!-- 病历表单 -->
      <template v-else>
        <el-card shadow="hover" class="form-card">
          <template #header>
            <div class="form-header">
              <div class="header-left">
                <span class="title">{{ isEdit ? '编辑病历' : '创建病历' }}</span>
              </div>
              <div class="header-actions">
                <el-button @click="showForm = false">取消</el-button>
                <el-button type="success" @click="handleSubmitAndPrescription" :icon="FirstAidKit">
                  保存并开处方
                </el-button>
                <el-button type="primary" @click="handleSubmit" :icon="Check">保存病历</el-button>
              </div>
            </div>
          </template>

          <el-steps :active="currentStep" finish-status="success" class="form-steps">
            <el-step title="基本信息" />
            <el-step title="主诉症状" />
            <el-step title="检查结果" />
            <el-step title="诊断治疗" />
          </el-steps>

          <el-form 
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
            class="record-form"
          >
            <!-- 步骤1: 基本信息 -->
            <div v-show="currentStep === 0" class="form-section">
              <el-divider content-position="left">
                <el-icon><InfoFilled /></el-icon>
                基本信息
              </el-divider>
              <el-row :gutter="24">
                <el-col :span="8">
                  <el-form-item label="挂号单号">
                    <el-input v-model="formData.registerId" disabled>
                      <template #prefix>
                        <el-icon><Document /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="宠物名称">
                    <el-input v-model="formData.petName" disabled>
                      <template #prefix>
                        <el-icon><User /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="接诊医生">
                    <el-input v-model="formData.doctorName" disabled>
                      <template #prefix>
                        <el-icon><FirstAidKit /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- 步骤2: 主诉症状 -->
            <div v-show="currentStep === 1" class="form-section">
              <el-divider content-position="left">
                <el-icon><ChatDotRound /></el-icon>
                主诉与症状
              </el-divider>
              <el-form-item label="主诉" prop="chiefComplaint">
                <el-input 
                  v-model="formData.chiefComplaint" 
                  type="textarea" 
                  :rows="3"
                  placeholder="请输入患者主诉，如：呕吐、腹泻、精神萎靡等"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="症状描述" prop="symptoms">
                <el-input 
                  v-model="formData.symptoms" 
                  type="textarea" 
                  :rows="4"
                  placeholder="详细描述症状表现、持续时间、严重程度等"
                  maxlength="1000"
                  show-word-limit
                />
              </el-form-item>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="现病史">
                    <el-input 
                      v-model="formData.presentIllness" 
                      type="textarea" 
                      :rows="3"
                      placeholder="疾病的发生、发展过程"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="既往史">
                    <el-input 
                      v-model="formData.pastHistory" 
                      type="textarea" 
                      :rows="3"
                      placeholder="既往疾病史、手术史、过敏史等"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <!-- 步骤3: 检查结果 -->
            <div v-show="currentStep === 2" class="form-section">
              <el-divider content-position="left">
                <el-icon><FirstAidKit /></el-icon>
                检查结果
              </el-divider>
              <el-form-item label="体格检查">
                <el-input 
                  v-model="formData.physicalExam" 
                  type="textarea" 
                  :rows="4"
                  placeholder="体温、呼吸、心率、体重、精神状态、体表检查等"
                  maxlength="800"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="辅助检查">
                <el-input 
                  v-model="formData.auxiliaryExam" 
                  type="textarea" 
                  :rows="4"
                  placeholder="化验结果（血常规、生化、尿检等）、影像学检查（X光、B超等）"
                  maxlength="800"
                  show-word-limit
                />
              </el-form-item>
            </div>

            <!-- 步骤4: 诊断治疗 -->
            <div v-show="currentStep === 3" class="form-section">
              <el-divider content-position="left">
                <el-icon><CircleCheck /></el-icon>
                诊断与治疗
              </el-divider>
              <el-form-item label="诊断结果" prop="diagnosis">
                <el-input 
                  v-model="formData.diagnosis" 
                  type="textarea" 
                  :rows="3"
                  placeholder="请输入诊断结果，可包含主要诊断和次要诊断"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="治疗方案">
                <el-input 
                  v-model="formData.treatmentPlan" 
                  type="textarea" 
                  :rows="4"
                  placeholder="药物治疗、手术治疗、护理方案等"
                  maxlength="800"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="医嘱建议">
                <el-input 
                  v-model="formData.doctorAdvice" 
                  type="textarea" 
                  :rows="3"
                  placeholder="饮食建议、活动限制、复诊时间、注意事项等"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="备注">
                <el-input 
                  v-model="formData.remark" 
                  type="textarea" 
                  :rows="2"
                  placeholder="其他需要记录的信息"
                  maxlength="300"
                  show-word-limit
                />
              </el-form-item>
            </div>
          </el-form>

          <!-- 步骤导航 -->
          <div class="step-actions">
            <el-button v-if="currentStep > 0" @click="currentStep--">上一步</el-button>
            <el-button v-if="currentStep < 3" type="primary" @click="nextStep">下一步</el-button>
          </div>
        </el-card>
      </template>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, RefreshRight, Plus, View, Edit, Download, 
  Check, FirstAidKit, InfoFilled, ChatDotRound, 
  CircleCheck, Document, User, Grid, Menu
} from '@element-plus/icons-vue'
import { recordModule } from '@/api/doctor/doctor'
import { useSettingsStore } from '@/store/settings'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const settingsStore = useSettingsStore()
const userStore = useUserStore()

// 列表相关
const loading = ref(false)
const showForm = ref(false)
const isEdit = ref(false)
const viewMode = ref('table')
const tableData = ref([])
const currentStep = ref(0)

const searchForm = reactive({
  petName: '',
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表单相关
const formRef = ref(null)
const formData = reactive({
  recordId: null,
  registerId: '',
  petId: '',
  petName: '',
  doctorName: '',
  chiefComplaint: '',
  symptoms: '',
  presentIllness: '',
  pastHistory: '',
  physicalExam: '',
  auxiliaryExam: '',
  diagnosis: '',
  treatmentPlan: '',
  doctorAdvice: '',
  remark: ''
})

const formRules = {
  chiefComplaint: [{ required: true, message: '请输入主诉', trigger: 'blur' }],
  symptoms: [{ required: true, message: '请输入症状描述', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请输入诊断结果', trigger: 'blur' }]
}

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      petName: searchForm.petName,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1]
    }
    const res = await recordModule.getMedicalRecordList(params)
    tableData.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (datetime) => {
  if (!datetime) return ''
  return datetime.split(' ')[0]
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchList()
}

const handleReset = () => {
  searchForm.petName = ''
  searchForm.dateRange = []
  handleSearch()
}

// 分页
const handlePageChange = (val) => {
  pagination.pageNum = val
  fetchList()
}

// 步骤导航
const nextStep = async () => {
  if (currentStep.value === 1) {
    try {
      await formRef.value.validateField(['chiefComplaint', 'symptoms'])
    } catch {
      return
    }
  }
  if (currentStep.value === 3) {
    try {
      await formRef.value.validateField(['diagnosis'])
    } catch {
      return
    }
  }
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

// 重置表单
const resetForm = () => {
  Object.keys(formData).forEach(key => {
    formData[key] = key === 'recordId' ? null : ''
  })
}

// 应用病历模板
const applyTemplate = () => {
  const templateType = settingsStore.recordTemplate
  const template = settingsStore.getTemplateContent(templateType)
  
  if (template && !isEdit.value) {
    formData.chiefComplaint = template.chiefComplaint || ''
    formData.symptoms = template.symptoms || ''
    formData.presentIllness = template.presentIllness || ''
    formData.pastHistory = template.pastHistory || ''
    formData.physicalExam = template.physicalExam || ''
    formData.auxiliaryExam = template.auxiliaryExam || ''
    formData.diagnosis = template.diagnosis || ''
    formData.treatmentPlan = template.treatmentPlan || ''
    formData.doctorAdvice = template.doctorAdvice || ''
  }
}

// 打开创建表单
const openCreateForm = () => {
  isEdit.value = false
  resetForm()
  currentStep.value = 0
  showForm.value = true
  applyTemplate()
}

// 查看
const handleView = (row) => {
  ElMessage.info('查看病历详情')
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, row)
  currentStep.value = 0
  showForm.value = true
}

// 导出
const handleExport = (row) => {
  ElMessage.success('开始导出处历')
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (isEdit.value) {
      await recordModule.updateMedicalRecord(formData)
      ElMessage.success('更新成功')
    } else {
      await recordModule.createMedicalRecord(formData)
      ElMessage.success('创建成功')
    }
    
    showForm.value = false
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

// 提交并开处方
const handleSubmitAndPrescription = async () => {
  await handleSubmit()
  router.push({
    path: '/doctor/prescription',
    query: {
      petId: formData.petId,
      recordId: formData.recordId
    }
  })
}

onMounted(() => {
  if (route.query.action === 'create') {
    formData.petId = route.query.petId
    formData.registerId = route.query.registerId
    formData.petName = route.query.petName || ''
    formData.doctorName = userStore.userInfo?.realName || userStore.userInfo?.username || ''
    openCreateForm()
  } else {
    fetchList()
  }
})
</script>

<style scoped lang="scss">
.record-page {
  .search-card {
    border-radius: 16px;
    margin-bottom: 20px;
    
    .search-input {
      width: 220px;
    }
  }
  
  .list-card {
    border-radius: 16px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 15px;
        
        .title {
          font-size: 18px;
          font-weight: 600;
        }
      }
    }
    
    .record-table {
      margin-top: 20px;
      
      .record-no {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #3b82f6;
        font-family: monospace;
        font-weight: 500;
      }
      
      .pet-info {
        .name {
          font-weight: 600;
          color: #1e293b;
        }
        
        .type {
          font-size: 13px;
          color: #64748b;
          margin-top: 4px;
        }
      }
      
      .diagnosis-text {
        color: #475569;
        line-height: 1.5;
      }
    }
    
    .card-view {
      margin-top: 20px;
      
      .record-card-item {
        background: white;
        border-radius: 12px;
        padding: 20px;
        margin-bottom: 20px;
        border: 1px solid #e2e8f0;
        transition: all 0.3s;
        
        &:hover {
          box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
          transform: translateY(-2px);
        }
        
        &.draft {
          border-left: 4px solid #f59e0b;
        }
        
        .card-header-mini {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;
          
          .time {
            font-size: 13px;
            color: #94a3b8;
          }
        }
        
        .card-body {
          margin-bottom: 15px;
          
          .pet-name {
            font-size: 18px;
            font-weight: 600;
            color: #1e293b;
            margin-bottom: 8px;
          }
          
          .diagnosis {
            color: #64748b;
            font-size: 14px;
            line-height: 1.5;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }
          
          .doctor {
            margin-top: 10px;
            font-size: 13px;
            color: #94a3b8;
          }
        }
        
        .card-footer {
          display: flex;
          gap: 10px;
          padding-top: 15px;
          border-top: 1px solid #f1f5f9;
        }
      }
    }
    
    .pagination {
      margin-top: 25px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .form-card {
    border-radius: 16px;
    
    .form-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 20px;
        
        .title {
          font-size: 18px;
          font-weight: 600;
          color: #1e293b;
        }
      }
      
      .header-actions {
        display: flex;
        gap: 12px;
      }
    }
    
    .form-steps {
      margin: 30px 0;
      padding: 0 50px;
    }
    
    .form-section {
      animation: fadeIn 0.3s ease;
      
      .el-divider {
        margin: 30px 0;
        
        .el-icon {
          margin-right: 8px;
          color: #3b82f6;
        }
      }
    }
    
    .record-form {
      max-width: 1000px;
      margin: 0 auto;
      
      :deep(.el-textarea__inner) {
        border-radius: 8px;
        padding: 12px;
        line-height: 1.6;
      }
    }
    
    .step-actions {
      display: flex;
      justify-content: center;
      gap: 20px;
      margin-top: 40px;
      padding-top: 30px;
      border-top: 1px solid #f1f5f9;
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>