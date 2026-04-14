<template>
    <div class="prescription-page">
      <!-- 列表视图 -->
      <template v-if="!showForm">
        <el-card class="search-card" shadow="hover">
          <el-form :model="searchForm" inline>
            <el-form-item label="宠物名称">
              <el-input v-model="searchForm.petName" placeholder="请输入" clearable>
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="处方号">
              <el-input v-model="searchForm.prescriptionNo" placeholder="请输入" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>查询
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshRight /></el-icon>重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" class="list-card">
          <template #header>
            <div class="card-header">
              <span class="title">处方列表</span>
              <el-button type="primary" @click="handleCreate">
                <el-icon><Plus /></el-icon>新建处方
              </el-button>
            </div>
          </template>

          <el-table :data="tableData" stripe v-loading="loading" class="prescription-table">
            <el-table-column prop="prescriptionNo" label="处方号" width="170">
              <template #default="{ row }">
                <div class="prescription-no">
                  <el-icon><Tickets /></el-icon>
                  <span>{{ row.prescriptionNo }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="petName" label="宠物" width="120" />
            <el-table-column prop="diagnosis" label="诊断" min-width="200" show-overflow-tooltip />
            <el-table-column prop="totalAmount" label="金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount">¥{{ row.totalAmount?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" effect="light" round size="small">
                  {{ row.statusDesc || '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" fixed="right" width="250" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">查看</el-button>
                <el-button 
                  v-if="row.status === 0" 
                  type="success" 
                  link 
                  @click="handleSubmit(row)"
                >
                  提交
                </el-button>
                <el-button type="warning" link @click="handlePrint(row)">打印</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              layout="total, prev, pager, next"
              @current-change="handlePageChange"
              background
            />
          </div>
        </el-card>
      </template>

      <!-- 处方表单 -->
      <template v-else>
        <el-card shadow="hover" class="form-card">
          <template #header>
            <div class="form-header">
              <div class="header-left">
                <el-button @click="showForm = false">
                  <el-icon><ArrowLeft /></el-icon>返回
                </el-button>
                <span class="title">开具处方</span>
              </div>
              <div class="header-actions">
                <el-button type="primary" @click="handleSave">保存草稿</el-button>
                <el-button type="success" @click="handleSubmitPrescription">提交处方</el-button>
              </div>
            </div>
          </template>

          <!-- 宠物信息 -->
          <el-descriptions :column="3" border class="info-section">
            <el-descriptions-item label="宠物名称">{{ petInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="宠物类型">{{ petInfo.type }}</el-descriptions-item>
            <el-descriptions-item label="诊断结果">
              <el-input v-model="formData.diagnosis" placeholder="请输入诊断结果" />
            </el-descriptions-item>
          </el-descriptions>

          <!-- 过敏警告 -->
          <el-alert
            v-if="petInfo.allergy"
            :title="`过敏史提醒：该宠物对 ${petInfo.allergy} 过敏`"
            type="error"
            :closable="false"
            show-icon
            class="allergy-alert"
          />

          <!-- 药品列表 -->
          <div class="section-title">药品明细</div>
          <el-table :data="drugList" border class="detail-table">
            <el-table-column type="index" width="50" />
            <el-table-column label="药品名称" width="200">
              <template #default="{ row, $index }">
                <el-select 
                  v-model="row.drugId" 
                  filterable 
                  placeholder="选择药品"
                  @change="(val) => handleDrugChange(val, $index)"
                >
                  <el-option
                    v-for="drug in drugOptions"
                    :key="drug.drugId"
                    :label="drug.drugName"
                    :value="drug.drugId"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="specification" label="规格" width="150" />
            <el-table-column label="数量" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :max="999" />
              </template>
            </el-table-column>
            <el-table-column label="用量" width="120">
              <template #default="{ row }">
                <el-input v-model="row.dosage" placeholder="如：1片" />
              </template>
            </el-table-column>
            <el-table-column label="用法" width="120">
              <template #default="{ row }">
                <el-select v-model="row.usage" placeholder="选择">
                  <el-option label="口服" value="口服" />
                  <el-option label="外用" value="外用" />
                  <el-option label="注射" value="注射" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="频次" width="130">
              <template #default="{ row }">
                <el-select v-model="row.frequency" placeholder="选择">
                  <el-option label="每日一次" value="每日一次" />
                  <el-option label="每日两次" value="每日两次" />
                  <el-option label="每日三次" value="每日三次" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="天数" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.days" :min="1" :max="30" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="{ row }">
                ¥{{ row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="金额" width="100">
              <template #default="{ row }">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeDrug($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-button type="primary" class="add-btn" @click="addDrug">
            <el-icon><Plus /></el-icon>添加药品
          </el-button>

          <!-- 服务项目 -->
          <div class="section-title">服务项目</div>
          <el-table :data="serviceList" border class="detail-table">
            <el-table-column type="index" width="50" />
            <el-table-column label="项目名称" min-width="200">
              <template #default="{ row, $index }">
                <el-select 
                  v-model="row.serviceId" 
                  placeholder="选择服务"
                  @change="(val) => handleServiceChange(val, $index)"
                >
                  <el-option
                    v-for="service in serviceOptions"
                    :key="service.serviceId"
                    :label="service.serviceName"
                    :value="service.serviceId"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :max="99" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                ¥{{ row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeService($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-button type="primary" class="add-btn" @click="addService">
            <el-icon><Plus /></el-icon>添加服务
          </el-button>

          <!-- 合计 -->
          <div class="total-section">
            <span class="label">合计金额：</span>
            <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
          </div>

          <!-- 备注 -->
          <el-form label-width="80px">
            <el-form-item label="备注">
              <el-input 
                v-model="formData.remark" 
                type="textarea" 
                :rows="2"
                placeholder="请输入备注信息"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </template>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, RefreshRight, Plus, ArrowLeft, Tickets
} from '@element-plus/icons-vue'
import { prescriptionModule } from '@/api/doctor/doctor'


const route = useRoute()
const router = useRouter()

// 列表相关
const loading = ref(false)
const showForm = ref(false)
const tableData = ref([])

const searchForm = reactive({
  petName: '',
  prescriptionNo: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 表单相关
const petInfo = reactive({
  name: '豆豆',
  type: '犬类',
  breed: '金毛',
  allergy: '青霉素'
})

const formData = reactive({
  registerId: '',
  petId: '',
  diagnosis: '',
  remark: ''
})

const drugList = ref([])
const serviceList = ref([])
const drugOptions = ref([])
const serviceOptions = ref([])

// 计算属性
const totalAmount = computed(() => {
  const drugTotal = drugList.value.reduce((sum, item) => {
    return sum + (item.price || 0) * (item.quantity || 0)
  }, 0)
  const serviceTotal = serviceList.value.reduce((sum, item) => {
    return sum + (item.price || 0) * (item.quantity || 0)
  }, 0)
  return drugTotal + serviceTotal
})

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      petName: searchForm.petName,
      prescriptionNo: searchForm.prescriptionNo
    }
    const res = await prescriptionModule.getPrescriptionList(params)
    tableData.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

// 获取药品/服务选项
const fetchOptions = async () => {
  try {
    const [drugRes, serviceRes] = await Promise.all([
      prescriptionModule.getDrugList(),
      prescriptionModule.getServiceList()
    ])
    drugOptions.value = drugRes.data || []
    serviceOptions.value = serviceRes.data || []
  } catch (error) {
    console.error('获取选项失败', error)
  }
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchList()
}

const handleReset = () => {
  searchForm.petName = ''
  searchForm.prescriptionNo = ''
  handleSearch()
}

const handlePageChange = (val) => {
  pagination.pageNum = val
  fetchList()
}

// 新建
const handleCreate = () => {
  resetForm()
  showForm.value = true
  fetchOptions()
}

// 查看
const handleView = (row) => {
  ElMessage.info('查看处方详情')
}

// 提交处方
const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确定要提交该处方吗？提交后将同步至前台收银端', '提示', {
      type: 'warning'
    })
    await prescriptionModule.submitPrescription(row.prescriptionId)
    ElMessage.success('提交成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

// 打印
const handlePrint = (row) => {
  ElMessage.success('开始打印')
}

// 药品操作
const addDrug = () => {
  drugList.value.push({
    drugId: null,
    drugName: '',
    specification: '',
    quantity: 1,
    dosage: '',
    usage: '',
    frequency: '',
    days: 1,
    price: 0
  })
}

const removeDrug = (index) => {
  drugList.value.splice(index, 1)
}

const handleDrugChange = (val, index) => {
  const drug = drugOptions.value.find(d => d.drugId === val)
  if (drug) {
    drugList.value[index].drugName = drug.drugName
    drugList.value[index].specification = drug.specification
    drugList.value[index].price = drug.price
    
    if (petInfo.allergy && drug.drugName.includes(petInfo.allergy)) {
      ElMessage.warning(`警告：该药品可能引发过敏反应（${petInfo.allergy}）`)
    }
  }
}

// 服务操作
const addService = () => {
  serviceList.value.push({
    serviceId: null,
    serviceName: '',
    quantity: 1,
    price: 0
  })
}

const removeService = (index) => {
  serviceList.value.splice(index, 1)
}

const handleServiceChange = (val, index) => {
  const service = serviceOptions.value.find(s => s.serviceId === val)
  if (service) {
    serviceList.value[index].serviceName = service.serviceName
    serviceList.value[index].price = service.price
  }
}

// 保存草稿
const handleSave = async () => {
  try {
    const data = {
      ...formData,
      drugs: drugList.value,
      services: serviceList.value,
      totalAmount: totalAmount.value
    }
    await prescriptionModule.createPrescription(data)
    ElMessage.success('保存成功')
    showForm.value = false
    fetchList()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 提交处方
const handleSubmitPrescription = async () => {
  if (!formData.diagnosis) {
    ElMessage.warning('请输入诊断结果')
    return
  }
  if (drugList.value.length === 0 && serviceList.value.length === 0) {
    ElMessage.warning('请至少添加一项药品或服务')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要提交处方吗？', '提示')
    await handleSave()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const resetForm = () => {
  formData.registerId = ''
  formData.petId = ''
  formData.diagnosis = ''
  formData.remark = ''
  drugList.value = []
  serviceList.value = []
}

onMounted(() => {
  if (route.query.action === 'create') {
    formData.petId = route.query.petId
    formData.registerId = route.query.registerId
    handleCreate()
  } else {
    fetchList()
  }
})
</script>

<style scoped lang="scss">
.prescription-page {
  .search-card {
    border-radius: 16px;
    margin-bottom: 20px;
  }
  
  .list-card {
    border-radius: 16px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title {
        font-size: 18px;
        font-weight: 600;
      }
    }
    
    .prescription-table {
      margin-top: 20px;
      
      .prescription-no {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #3b82f6;
        font-family: monospace;
        font-weight: 500;
        
        .el-icon {
          color: #94a3b8;
        }
      }
      
      .amount {
        font-weight: 600;
        color: #f59e0b;
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
    }
    
    .info-section {
      margin-bottom: 20px;
    }
    
    .allergy-alert {
      margin-bottom: 20px;
    }
    
    .section-title {
      font-size: 16px;
      font-weight: bold;
      margin: 20px 0 10px;
      padding-left: 10px;
      border-left: 4px solid #409EFF;
      color: #334155;
    }
    
    .detail-table {
      margin-bottom: 10px;
    }
    
    .add-btn {
      margin-bottom: 20px;
    }
    
    .total-section {
      text-align: right;
      padding: 20px;
      background-color: #f5f7fa;
      margin: 20px 0;
      border-radius: 8px;
      
      .label {
        font-size: 16px;
        color: #606266;
      }
      
      .amount {
        font-size: 24px;
        color: #f56c6c;
        font-weight: bold;
        margin-left: 10px;
      }
    }
  }
}
</style>