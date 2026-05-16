<template>
    <div class="prescription-page">
      <!-- 列表视图 -->
      <template v-if="!showForm">
        <el-card class="search-card" shadow="hover">
          <el-form :model="searchForm" inline>
            <el-form-item :label="$t('prescription.petName')">
              <el-input v-model="searchForm.petName" :placeholder="$t('prescription.placeholderEnter')" clearable>
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item :label="$t('prescription.prescriptionNo')">
              <el-input v-model="searchForm.prescriptionNo" :placeholder="$t('prescription.placeholderEnter')" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>{{ $t('common.search') }}
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshRight /></el-icon>{{ $t('common.reset') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" class="list-card">
          <template #header>
            <div class="card-header">
              <span class="title">{{ $t('prescription.prescriptionList') }}</span>
              <el-button type="primary" @click="handleCreate">
                <el-icon><Plus /></el-icon>{{ $t('prescription.newPrescription') }}
              </el-button>
            </div>
          </template>

          <el-table :data="tableData" stripe v-loading="loading" class="prescription-table">
            <el-table-column prop="prescriptionNo" :label="$t('prescription.prescriptionNo')" width="170">
              <template #default="{ row }">
                <div class="prescription-no">
                  <el-icon><Tickets /></el-icon>
                  <span>{{ row.prescriptionNo }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="petName" :label="$t('prescription.pet')" width="120" />
            <el-table-column prop="diagnosis" :label="$t('prescription.diagnosis')" min-width="200" show-overflow-tooltip />
            <el-table-column prop="totalAmount" :label="$t('prescription.amount')" width="120" align="right">
              <template #default="{ row }">
                <span class="amount">¥{{ row.totalAmount?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" :label="$t('common.status')" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" effect="light" round size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" :label="$t('prescription.createTime')" width="170" />
            <el-table-column :label="$t('common.operation')" fixed="right" width="250" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">{{ $t('common.view') }}</el-button>
                <el-button 
                  v-if="row.status === 0" 
                  type="success" 
                  link 
                  @click="handleSubmitPrescription(row)"
                >
                  {{ $t('common.submit') }}
                </el-button>
                <el-button type="warning" link @click="handlePrint(row)">{{ $t('prescription.print') }}</el-button>
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
                <el-button @click="handleBack">
                  <el-icon><ArrowLeft /></el-icon>{{ $t('common.back') }}
                </el-button>
                <span class="title">{{ $t('prescription.createPrescription') }}</span>
              </div>
              <div class="form-actions">
                <el-button @click="handleSubmit(0)" :icon="Document">{{ $t('prescription.saveDraft') }}</el-button>
                <el-button type="primary" @click="handleSubmit(1)" :icon="Check">{{ $t('prescription.submitPrescription') }}</el-button>
              </div>
            </div>
          </template>

          <!-- 宠物信息 -->
          <el-descriptions :column="3" border class="info-section">
            <el-descriptions-item :label="$t('prescription.petName')">{{ petInfo.name }}</el-descriptions-item>
            <el-descriptions-item :label="$t('prescription.petType')">{{ displayPetType }}</el-descriptions-item>
            <el-descriptions-item :label="$t('prescription.diagnosisResult')">
              <el-input v-model="formData.diagnosis" :placeholder="$t('prescription.placeholderDiagnosisResult')" />
            </el-descriptions-item>
          </el-descriptions>

          <!-- 过敏警告 -->
          <el-alert
            v-if="petInfo.allergy"
            :title="$t('prescription.allergyAlert', { allergy: petInfo.allergy })"
            type="error"
            :closable="false"
            show-icon
            class="allergy-alert"
          />

          <!-- 药品列表 -->
          <div class="section-title">{{ $t('prescription.drugDetails') }}</div>
          <el-table :data="drugList" border class="detail-table">
            <el-table-column type="index" width="50" />
            <el-table-column :label="$t('prescription.drugName')" width="220">
              <template #default="{ row, $index }">
                <el-select 
                  v-model="row.drugId" 
                  filterable 
                  :placeholder="$t('prescription.selectDrug')"
                  @change="(val) => handleDrugChange(val, $index)"
                >
                  <el-option
                    v-for="drug in drugOptions"
                    :key="drug.drugId"
                    :value="drug.drugId"
                  >
                    <span style="float: left">{{ drug.drugName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 12px">
                      {{ $t('prescription.stock') }}: {{ drug.stockQty }}
                      <el-tag v-if="drug.stockQty <= drug.warningStockQty" type="danger" size="small" style="margin-left:4px">{{ $t('prescription.outOfStock') }}</el-tag>
                    </span>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="specification" :label="$t('prescription.specification')" width="120" />
            <el-table-column :label="$t('prescription.stock')" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.stockQty != null" :type="row.stockQty <= (row.warningStockQty || 0) ? 'danger' : 'success'" size="small">
                  {{ row.stockQty }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.quantity')" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :max="999" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.dosage')" width="120">
              <template #default="{ row }">
                <el-input v-model="row.dosage" :placeholder="$t('prescription.placeholderDosage')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.usage')" width="120">
              <template #default="{ row }">
                <el-select v-model="row.usage" :placeholder="$t('common.select')">
                  <el-option :label="$t('prescription.usageOral')" value="Oral" />
                  <el-option :label="$t('prescription.usageExternal')" value="External use" />
                  <el-option :label="$t('prescription.usageInjection')" value="Injection" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.frequency')" width="130">
              <template #default="{ row }">
                <el-select v-model="row.frequency" :placeholder="$t('common.select')">
                  <el-option :label="$t('prescription.frequencyOnceDaily')" value="Once daily" />
                  <el-option :label="$t('prescription.frequencyTwiceDaily')" value="Twice daily" />
                  <el-option :label="$t('prescription.frequencyThreeTimesDaily')" value="Three times daily" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.days')" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.days" :min="1" :max="30" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.unitPrice')" width="100">
              <template #default="{ row }">
                ¥{{ row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.amount')" width="100">
              <template #default="{ row }">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="80">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeDrug($index)">{{ $t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-button type="primary" class="add-btn" @click="addDrug">
            <el-icon><Plus /></el-icon>{{ $t('prescription.addDrug') }}
          </el-button>

          <!-- 服务项目 -->
          <div class="section-title">{{ $t('prescription.serviceItems') }}</div>
          <el-table :data="serviceList" border class="detail-table">
            <el-table-column type="index" width="50" />
            <el-table-column :label="$t('prescription.itemName')" min-width="200">
              <template #default="{ row, $index }">
                <el-select 
                  v-model="row.serviceId" 
                  :placeholder="$t('prescription.selectService')"
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
            <el-table-column :label="$t('prescription.quantity')" width="100">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="1" :max="99" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.unitPrice')" width="120">
              <template #default="{ row }">
                ¥{{ row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('prescription.amount')" width="120">
              <template #default="{ row }">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="80">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeService($index)">{{ $t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-button type="primary" class="add-btn" @click="addService">
            <el-icon><Plus /></el-icon>{{ $t('prescription.addService') }}
          </el-button>

          <!-- 合计 -->
          <div class="total-section">
            <span class="label">{{ $t('prescription.totalAmountLabel') }}</span>
            <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
          </div>

          <!-- 备注 -->
          <el-form label-width="80px">
            <el-form-item :label="$t('prescription.remark')">
              <el-input 
                v-model="formData.remark" 
                type="textarea" 
                :rows="2"
                :placeholder="$t('prescription.placeholderRemark')"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </template>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, RefreshRight, Plus, ArrowLeft, Tickets, Document, Check
} from '@element-plus/icons-vue'
import { prescriptionModule, acceptModule } from '@/api/doctor/doctor'
import { useUserStore } from '@/store/user'
import { useSettingsStore } from '@/store/settings'

const userStore = useUserStore()
const settingsStore = useSettingsStore()

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

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
  name: 'DouDou',
  type: 'Dog',
  breed: 'Golden Retriever',
  allergy: 'Penicillin'
})

const petTypeMap = {
  'Cat': 'dashboard.typeCat',
  'Dog': 'dashboard.typeDog',
  'Rabbit': 'dashboard.typeRabbit',
  'cat': 'dashboard.typeCat',
  'dog': 'dashboard.typeDog',
  'rabbit': 'dashboard.typeRabbit',
  '猫': 'dashboard.typeCat',
  '狗': 'dashboard.typeDog',
  '兔': 'dashboard.typeRabbit',
}
const displayPetType = computed(() => {
  const type = petInfo.type
  if (!type || type === '-') return '-'
  const key = petTypeMap[type]
  return key ? t(key) : type
})

const formData = reactive({
  registerId: '',
  recordId: '',
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


// 计算总金额的函数
const calculateTotal = () => {
  const drugTotal = drugList.value.reduce((sum, item) => {
    return sum + (item.price || 0) * (item.quantity || 0)
  }, 0)
  const serviceTotal = serviceList.value.reduce((sum, item) => {
    return sum + (item.price || 0) * (item.quantity || 0)
  }, 0)
  return drugTotal + serviceTotal
}

// 获取列表
// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    // 构建搜索关键词：组合宠物名称和处方号
    let keyword = ''
    if (searchForm.petName) {
      keyword = searchForm.petName
    }
    if (searchForm.prescriptionNo) {
      keyword = searchForm.prescriptionNo
    }
    // 如果两个都有，用空格连接
    if (searchForm.petName && searchForm.prescriptionNo) {
      keyword = `${searchForm.petName} ${searchForm.prescriptionNo}`
    }
    
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: keyword,  // 使用 keyword 参数
      petId: null        // 可选，如果需要按宠物筛选
    }
    
    console.log('请求参数:', params)
    const res = await prescriptionModule.getPrescriptionList(params)
    console.log('处方列表API返回:', res)
    
    if (res.code === 200 && res.data) {
      let list = []
      let total = 0
      
      const pageData = res.data
      
      // 兼容多种返回格式
      if (pageData.data && Array.isArray(pageData.data)) {
        list = pageData.data
        total = pageData.total || list.length
      } else if (pageData.list && Array.isArray(pageData.list)) {
        list = pageData.list
        total = pageData.total || list.length
      } else if (pageData.records && Array.isArray(pageData.records)) {
        list = pageData.records
        total = pageData.total || list.length
      } else if (Array.isArray(pageData)) {
        list = pageData
        total = list.length
      }
      
      // 如果后端没有返回总数，但前端有宠物名称筛选，需要前端过滤
      if (searchForm.petName && total > 0) {
        list = list.filter(item => item.petName && item.petName.includes(searchForm.petName))
        total = list.length
      }
      if (searchForm.prescriptionNo && total > 0) {
        list = list.filter(item => item.prescriptionNo && item.prescriptionNo.includes(searchForm.prescriptionNo))
        total = list.length
      }
      
      if (total === 0 && list.length > 0) {
        total = list.length
      }
      
      // 处方支付/提交提醒
      const notifiedKey = 'notified_prescriptions'
      let notifiedIds = JSON.parse(localStorage.getItem(notifiedKey) || '[]')
      const newPaid = list.filter(item => item.status === 1 && !notifiedIds.includes(item.prescriptionId))
      if (newPaid.length > 0 && settingsStore.shouldNotify('prescriptionPaid')) {
        settingsStore.sendDesktopNotification(
          t('prescription.prescriptionPaymentAlertTitle'),
          t('prescription.prescriptionPaymentAlertBody', { count: newPaid.length })
        )
        settingsStore.playNotificationSound()
        notifiedIds = [...notifiedIds, ...newPaid.map(i => i.prescriptionId)]
        localStorage.setItem(notifiedKey, JSON.stringify(notifiedIds.slice(-100))) // 保留最近100条
      }
      
      tableData.value = list
      pagination.total = total
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取处方列表失败:', error)
    tableData.value = []
    ElMessage.error(t('prescription.failedFetchList'))
  } finally {
    loading.value = false
  }
}

// 获取药品/服务选项
// 获取药品/服务选项
const fetchOptions = async () => {
  try {
    console.log('开始获取药品和服务选项...')
    
    // 分别调用，便于调试
    console.log('调用药品接口...')
    const drugRes = await prescriptionModule.getDrugList()
    console.log('药品接口返回:', drugRes)
    
    console.log('调用服务接口...')
    const serviceRes = await prescriptionModule.getServiceList()
    console.log('服务接口返回:', serviceRes)
    
    // 处理药品数据
    let drugs = []
    if (drugRes.code === 200) {
      if (Array.isArray(drugRes.data)) {
        drugs = drugRes.data
        console.log('药品数据是数组，长度:', drugs.length)
      } else {
        console.error('药品数据不是数组:', drugRes.data)
      }
    } else {
      console.error('药品接口返回错误:', drugRes.msg)
    }
    
    // 处理服务数据
    let services = []
    if (serviceRes.code === 200) {
      if (Array.isArray(serviceRes.data)) {
        services = serviceRes.data
        console.log('服务数据是数组，长度:', services.length)
      } else {
        console.error('服务数据不是数组:', serviceRes.data)
      }
    } else {
      console.error('服务接口返回错误:', serviceRes.msg)
    }
    
    drugOptions.value = drugs
    serviceOptions.value = services
    
    console.log('最终药品选项:', drugOptions.value)
    console.log('最终服务选项:', serviceOptions.value)
    
    if (drugOptions.value.length === 0) {
      ElMessage.warning(t('prescription.noDrugData'))
    }
    if (serviceOptions.value.length === 0) {
      ElMessage.warning(t('prescription.noServiceData'))
    }
  } catch (error) {
    console.error('获取选项失败:', error)
    ElMessage.error(t('prescription.failedFetchDrugService') + (error.message ? ': ' + error.message : ''))
  }
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: t('prescription.statusDraft'), 1: t('prescription.statusSubmitted'), 2: t('prescription.statusCancelled') }
  return map[status] || t('prescription.statusUnknown')
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchList()
}

const handleBack = () => {
  showForm.value = false
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
  // 尝试恢复草稿
  if (settingsStore.autoSavePrescription) {
    const hasDraft = restoreDraft()
    if (hasDraft) {
      ElMessage.success(t('prescription.autoRestoredDraft'))
    }
    startAutoSave()
  }
}

// 查看
const handleView = (row) => {
  router.push({
    path: '/doctor/prescription/detail',
    query: {
      prescriptionId: row.prescriptionId || row.id,
      prescriptionNo: row.prescriptionNo || '',
      petName: row.petName || '',
      diagnosis: row.diagnosis || '',
      totalAmount: row.totalAmount || 0,
      status: row.status ?? '',
      statusDesc: row.statusDesc || '',
      createTime: row.createTime || ''
    }
  })
}

// 提交处方
// status: 0=保存草稿, 1=提交处方
const handleSubmit = async (status = 1) => {
  // 1. 过滤有效的药品和服务（已选择的）
  const validDrugs = drugList.value.filter(item => item.drugId)
  const validServices = serviceList.value.filter(item => item.serviceId)
  
  // 2. 验证：至少有一项药品或服务
  if (validDrugs.length === 0 && validServices.length === 0) {
    ElMessage.warning(t('prescription.pleaseAddDrugOrService'))
    return
  }
  
  // 3. 验证：诊断结果不能为空
  if (!formData.diagnosis || formData.diagnosis.trim() === '') {
    ElMessage.warning(t('prescription.pleaseEnterDiagnosisResult'))
    return
  }
  
  // 4. 计算总金额
  const totalAmount = calculateTotal()
  
  // 5. 获取医生ID
  const doctorId = userStore.userInfo?.doctorId || userStore.userInfo?.id || 2006
  
  // 6. 验证必要字段
  if (!formData.registerId) {
    ElMessage.error(t('prescription.registrationIdRequired'))
    return
  }
  if (!formData.petId) {
    ElMessage.error(t('prescription.petIdRequired'))
    return
  }
  
  // 7. 构建提交数据（匹配后端 PrescriptionCreateDto）
  const submitData = {
    registerId: Number(formData.registerId),
    recordId: formData.recordId ? Number(formData.recordId) : null,
    petId: Number(formData.petId),
    doctorId: Number(doctorId),
    prescriptionType: 1,
    diagnosis: formData.diagnosis.trim(),
    totalAmount: totalAmount,
    remark: formData.remark || '',
    drugs: validDrugs.map(item => ({
      drugId: item.drugId,
      drugName: item.drugName,
      quantity: item.quantity || 1,
      dosage: item.dosage || '',
      usage: item.usage || '',
      frequency: item.frequency || '',
      days: item.days || 1,
      price: item.price || 0,
      amount: (item.price || 0) * (item.quantity || 1)
    })),
    services: validServices.map(item => ({
      serviceId: item.serviceId,
      serviceName: item.serviceName,
      quantity: item.quantity || 1,
      price: item.price || 0,
      amount: (item.price || 0) * (item.quantity || 1)
    }))
  }
  
  console.log('提交数据:', JSON.stringify(submitData, null, 2))
  
  try {
    if (status === 0) {
      // ========== 保存草稿：只创建，不提交 ==========
      const res = await prescriptionModule.createPrescription(submitData)
      console.log('保存草稿返回:', res)
      
      if (res.code === 200) {
        ElMessage.success(t('prescription.draftSavedSuccess'))
        showForm.value = false
        fetchList()  // 刷新列表
      } else {
        ElMessage.error(res.msg || t('prescription.failedSaveDraft'))
      }
    } else {
      // ========== 提交处方：创建并立即提交 ==========
      // 第一步：创建处方
      const createRes = await prescriptionModule.createPrescription(submitData)
      console.log('创建处方返回:', createRes)
      
      if (createRes.code !== 200) {
        ElMessage.error(createRes.msg || t('prescription.failedCreatePrescription'))
        return
      }
      
      const prescriptionId = createRes.data?.prescriptionId
      if (!prescriptionId) {
        ElMessage.error(t('prescription.createdButNoId'))
        return
      }
      
      // 第二步：提交处方（更新状态为已提交）
      const submitRes = await prescriptionModule.submitPrescription(prescriptionId)
      console.log('提交处方返回:', submitRes)
      
      if (submitRes.code === 200) {
        ElMessage.success(t('prescription.prescriptionSubmittedSuccess'))
        showForm.value = false
        fetchList()  // 刷新列表
      } else {
        ElMessage.error(submitRes.msg || t('prescription.failedSubmitPrescription'))
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    const errorMsg = error.response?.data?.msg || error.message || t('prescription.operationFailed')
    ElMessage.error(errorMsg)
  }
}

// 打印
const handlePrint = (row) => {
  ElMessage.success(t('prescription.printingStarted'))
}

// 药品操作
const addDrug = () => {
  drugList.value.push({
    drugId: null,      // 用于选择
    drugName: '',      // 用于显示
    specification: '',
    quantity: 1,
    dosage: '',
    usage: '',
    frequency: '',
    days: 1,
    price: 0,
    id: null           // 可以删除或统一使用 drugId
  })
}

const removeDrug = (index) => {
  drugList.value.splice(index, 1)
}

const handleDrugChange = (val, index) => {
  console.log('药品选择变化:', val, index)
  console.log('当前药品选项:', drugOptions.value)
  
  const drug = drugOptions.value.find(d => d.drugId === val)
  console.log('找到的药品:', drug)
  
  if (drug) {
    drugList.value[index].drugName = drug.drugName
    drugList.value[index].specification = drug.specification || ''
    drugList.value[index].unit = drug.unit || ''
    drugList.value[index].price = drug.price || 0
    drugList.value[index].stockQty = drug.stockQty || 0
    drugList.value[index].warningStockQty = drug.warningStockQty || 0
    drugList.value[index].id = drug.drugId
    
    console.log('更新后的药品项:', drugList.value[index])
    
    if (petInfo.allergy && drug.drugName && drug.drugName.includes(petInfo.allergy)) {
      ElMessage.warning(t('prescription.allergicReactionWarning', { allergy: petInfo.allergy }))
    }
    
    if (drug.stockQty != null && drug.stockQty <= (drug.warningStockQty || 0)) {
      ElMessage.warning(t('prescription.drugOutOfStockWarning', { drugName: drug.drugName, stockQty: drug.stockQty }))
    }
  } else {
    console.warn('未找到药品, drugId:', val)
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
    ElMessage.success(t('prescription.savedSuccessfully'))
    showForm.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(t('prescription.saveFailed'))
  }
}

// 提交处方
// 提交列表中已有的处方（用于状态为草稿的处方）
const handleSubmitPrescription = async (row) => {
  try {
    await ElMessageBox.confirm(t('prescription.confirmSubmitPrescription'), t('common.notice'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    
    const res = await prescriptionModule.submitPrescription(row.prescriptionId)
    console.log('提交处方返回:', res)
    
    if (res.code === 200) {
      ElMessage.success(t('prescription.submittedSuccessfully'))
      fetchList()  // 刷新列表
    } else {
      ElMessage.error(res.msg || t('prescription.submissionFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(t('prescription.submissionFailed'))
    }
  }
}

const resetForm = () => {
  formData.registerId = ''
  formData.recordId = ''
  formData.petId = ''
  formData.diagnosis = ''
  formData.remark = ''
  drugList.value = []
  serviceList.value = []
  
  // 重置宠物信息为默认值
  petInfo.name = ''
  petInfo.type = ''
  petInfo.breed = ''
  petInfo.allergy = ''
  
  // 清除自动保存的草稿
  localStorage.removeItem('prescription_draft')
}

// 自动保存草稿
let autoSaveTimer = null
const startAutoSave = () => {
  if (!settingsStore.autoSavePrescription) return
  if (autoSaveTimer) clearInterval(autoSaveTimer)
  autoSaveTimer = setInterval(() => {
    if (!showForm.value) return
    const draft = {
      formData: { ...formData },
      drugList: drugList.value,
      serviceList: serviceList.value,
      petInfo: { ...petInfo },
      saveTime: new Date().toISOString()
    }
    localStorage.setItem('prescription_draft', JSON.stringify(draft))
  }, 30000) // 每30秒自动保存一次
}

const stopAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}

const restoreDraft = (expectedPetId) => {
  const draftStr = localStorage.getItem('prescription_draft')
  if (!draftStr) return false
  try {
    const draft = JSON.parse(draftStr)
    // 如果指定了期望的宠物ID，检查草稿是否匹配；不匹配则清除旧草稿
    if (expectedPetId !== undefined) {
      const draftPetId = draft.formData?.petId
      if (draftPetId && Number(draftPetId) !== Number(expectedPetId)) {
        console.log(`草稿宠物ID(${draftPetId})与当前宠物ID(${expectedPetId})不匹配，清除旧草稿`)
        localStorage.removeItem('prescription_draft')
        return false
      }
    }
    if (draft.formData) {
      formData.registerId = draft.formData.registerId || ''
      formData.recordId = draft.formData.recordId || ''
      formData.petId = draft.formData.petId || ''
      formData.diagnosis = draft.formData.diagnosis || ''
      formData.remark = draft.formData.remark || ''
    }
    if (draft.drugList) drugList.value = draft.drugList
    if (draft.serviceList) serviceList.value = draft.serviceList
    if (draft.petInfo) {
      if (draft.petInfo.name) petInfo.name = draft.petInfo.name
      if (draft.petInfo.type) petInfo.type = draft.petInfo.type
      if (draft.petInfo.breed) petInfo.breed = draft.petInfo.breed
      if (draft.petInfo.allergy) petInfo.allergy = draft.petInfo.allergy
    }
    return true
  } catch (e) {
    console.error('恢复草稿失败', e)
    return false
  }
}

// ========== 新增：获取宠物详情 ==========
const fetchPetDetail = async (petId) => {
  try {
    console.log('获取宠物详情, petId:', petId)
    const res = await acceptModule.getPetDetail(petId)
    console.log('宠物详情返回:', res)
    
    if (res.code === 200 && res.data) {
      const data = res.data
      // 更新宠物信息显示
      petInfo.name = data.name || t('common.unknown')
      petInfo.type = data.type || '-'
      petInfo.breed = data.breed || '-'
      petInfo.allergy = data.allergies || data.allergy || ''
      
      console.log('宠物信息已更新:', petInfo)
    } else {
      // 如果接口失败，使用URL传来的名称
      if (route.query.petName) {
        petInfo.name = route.query.petName
      }
    }
  } catch (error) {
    console.error('获取宠物详情失败:', error)
    // 使用URL传来的名称作为兜底
    if (route.query.petName) {
      petInfo.name = route.query.petName
    }
  }
}

onMounted(async () => {
  console.log('=== 处方页面启动 ===')
  console.log('路由参数:', route.query)
  console.log('userStore.userInfo:', userStore.userInfo)
  
  if (route.query.action === 'create') {
    const petId = route.query.petId
    const registerId = route.query.registerId
    const recordId = route.query.recordId
    const petName = route.query.petName
    
    console.log('接收到的参数:', { petId, registerId, recordId, petName })
    
    if (petId) {
      formData.petId = Number(petId)
    }
    if (registerId) {
      formData.registerId = Number(registerId)
    }
    if (recordId) {
      formData.recordId = Number(recordId)
    }
    
    console.log('设置后的 formData:', { 
      petId: formData.petId, 
      registerId: formData.registerId,
      recordId: formData.recordId
    })
    
    // 先显示表单
    showForm.value = true
    
    // 自动恢复草稿（仅当草稿宠物ID与当前一致时才恢复）
    if (settingsStore.autoSavePrescription) {
      const hasDraft = restoreDraft(formData.petId)
      if (hasDraft) {
        ElMessage.success(t('prescription.autoRestoredDraft'))
      }
      startAutoSave()
    }
    
    // 并行获取数据和宠物详情（API数据会覆盖草稿中的空值）
    await Promise.all([
      fetchOptions(),
      formData.petId ? fetchPetDetail(formData.petId) : Promise.resolve()
    ])
    
    console.log('数据加载完成，药品选项:', drugOptions.value.length)
  } else {
    fetchList()
  }
})

onUnmounted(() => {
  stopAutoSave()
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