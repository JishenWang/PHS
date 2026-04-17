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
                  @click="handleSubmitPrescription(row)"
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
              <div class="form-actions">
                <el-button @click="handleSubmit(0)" :icon="Document">保存草稿</el-button>
                <el-button type="primary" @click="handleSubmit(1)" :icon="Check">提交处方</el-button>
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
  Search, RefreshRight, Plus, ArrowLeft, Tickets, Document, Check
} from '@element-plus/icons-vue'
import { prescriptionModule, acceptModule } from '@/api/doctor/doctor'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

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
      
      tableData.value = list
      pagination.total = total
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取处方列表失败:', error)
    tableData.value = []
    ElMessage.error('获取列表失败')
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
      ElMessage.warning('没有获取到药品数据，请检查数据库')
    }
    if (serviceOptions.value.length === 0) {
      ElMessage.warning('没有获取到服务数据，请检查数据库')
    }
  } catch (error) {
    console.error('获取选项失败:', error)
    ElMessage.error('获取药品/服务数据失败: ' + (error.message || '未知错误'))
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
// status: 0=保存草稿, 1=提交处方
const handleSubmit = async (status = 1) => {
  // 1. 过滤有效的药品和服务（已选择的）
  const validDrugs = drugList.value.filter(item => item.drugId)
  const validServices = serviceList.value.filter(item => item.serviceId)
  
  // 2. 验证：至少有一项药品或服务
  if (validDrugs.length === 0 && validServices.length === 0) {
    ElMessage.warning('请至少添加一项药品或服务')
    return
  }
  
  // 3. 验证：诊断结果不能为空
  if (!formData.diagnosis || formData.diagnosis.trim() === '') {
    ElMessage.warning('请输入诊断结果')
    return
  }
  
  // 4. 计算总金额
  const totalAmount = calculateTotal()
  
  // 5. 获取医生ID
  const doctorId = userStore.userInfo?.doctorId || userStore.userInfo?.id || 2006
  
  // 6. 验证必要字段
  if (!formData.registerId) {
    ElMessage.error('挂号ID不能为空，请从接诊列表重新进入')
    return
  }
  if (!formData.petId) {
    ElMessage.error('宠物ID不能为空，请从接诊列表重新进入')
    return
  }
  
  // 7. 构建提交数据（匹配后端 PrescriptionCreateDto）
  const submitData = {
    registerId: Number(formData.registerId),
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
        ElMessage.success('草稿保存成功')
        showForm.value = false
        fetchList()  // 刷新列表
      } else {
        ElMessage.error(res.msg || '草稿保存失败')
      }
    } else {
      // ========== 提交处方：创建并立即提交 ==========
      // 第一步：创建处方
      const createRes = await prescriptionModule.createPrescription(submitData)
      console.log('创建处方返回:', createRes)
      
      if (createRes.code !== 200) {
        ElMessage.error(createRes.msg || '创建处方失败')
        return
      }
      
      const prescriptionId = createRes.data?.prescriptionId
      if (!prescriptionId) {
        ElMessage.error('创建处方成功但未获取到处方ID')
        return
      }
      
      // 第二步：提交处方（更新状态为已提交）
      const submitRes = await prescriptionModule.submitPrescription(prescriptionId)
      console.log('提交处方返回:', submitRes)
      
      if (submitRes.code === 200) {
        ElMessage.success('处方提交成功')
        showForm.value = false
        fetchList()  // 刷新列表
      } else {
        ElMessage.error(submitRes.msg || '处方提交失败')
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    const errorMsg = error.response?.data?.msg || error.message || '操作失败'
    ElMessage.error(errorMsg)
  }
}

// 打印
const handlePrint = (row) => {
  ElMessage.success('开始打印')
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
    drugList.value[index].price = drug.price || 0
    drugList.value[index].id = drug.drugId
    
    console.log('更新后的药品项:', drugList.value[index])
    
    if (petInfo.allergy && drug.drugName && drug.drugName.includes(petInfo.allergy)) {
      ElMessage.warning(`警告：该药品可能引发过敏反应（${petInfo.allergy}）`)
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
    ElMessage.success('保存成功')
    showForm.value = false
    fetchList()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 提交处方
// 提交列表中已有的处方（用于状态为草稿的处方）
const handleSubmitPrescription = async (row) => {
  try {
    await ElMessageBox.confirm('确定要提交该处方吗？提交后不可修改。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await prescriptionModule.submitPrescription(row.prescriptionId)
    console.log('提交处方返回:', res)
    
    if (res.code === 200) {
      ElMessage.success('提交成功')
      fetchList()  // 刷新列表
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
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
  
  // 重置宠物信息为默认值
  petInfo.name = ''
  petInfo.type = ''
  petInfo.breed = ''
  petInfo.allergy = ''
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
      petInfo.name = data.name || '未知'
      petInfo.type = data.type || '-'
      petInfo.breed = data.breed || '-'
      petInfo.allergy = data.allergy || ''
      
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
    const petName = route.query.petName
    
    console.log('接收到的参数:', { petId, registerId, petName })
    
    if (petId) {
      formData.petId = Number(petId)
    }
    if (registerId) {
      formData.registerId = Number(registerId)
    }
    
    console.log('设置后的 formData:', { 
      petId: formData.petId, 
      registerId: formData.registerId 
    })
    
    // 先显示表单
    showForm.value = true
    
    // 并行获取数据和宠物详情
    await Promise.all([
      fetchOptions(),
      formData.petId ? fetchPetDetail(formData.petId) : Promise.resolve()
    ])
    
    console.log('数据加载完成，药品选项:', drugOptions.value.length)
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