<template>
    <div class="accept-page">
      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card waiting">
          <div class="stat-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="number">{{ stats.waiting }}</div>
            <div class="label">{{ $t('dashboard.pendingConfirm') }}</div>
          </div>
          <div class="trend up">
            <el-icon><ArrowUp /></el-icon>
            <span>+{{ stats.waitingNew }} {{ $t('common.add') }}</span>
          </div>
        </div>
        
        <div class="stat-card processing">
          <div class="stat-icon">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="number">{{ stats.processing }}</div>
            <div class="label">{{ $t('layout.statusBusy') }}</div>
          </div>
        </div>
        
        <div class="stat-card completed">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="number">{{ stats.completed }}</div>
            <div class="label">{{ $t('dashboard.completed') }}</div>
          </div>
        </div>
        
        <div class="stat-card avg-time">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="number">{{ stats.avgTime }}{{ $t('accept.minutes') }}</div>
            <div class="label">{{ $t('accept.avgTime') }}</div>
          </div>
        </div>
      </div>

      <!-- 搜索筛选区 -->
      <el-card class="search-card" shadow="hover">
        <div class="search-header">
          <div class="search-title">
            <el-icon><Search /></el-icon>
            <span>{{ $t('common.search') }}</span>
          </div>

        </div>
        
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item :label="$t('common.status')">
            <el-select v-model="searchForm.status" :placeholder="$t('common.status')" clearable class="status-select">
              <el-option :label="$t('dashboard.pendingConfirm')" :value="0">
                <el-tag size="small" type="warning">{{ $t('dashboard.pendingConfirm') }}</el-tag>
              </el-option>
              <el-option :label="$t('layout.statusBusy')" :value="1">
                <el-tag size="small" type="primary">{{ $t('layout.statusBusy') }}</el-tag>
              </el-option>
              <el-option :label="$t('dashboard.completed')" :value="2">
                <el-tag size="small" type="success">{{ $t('dashboard.completed') }}</el-tag>
              </el-option>
              <el-option :label="$t('dashboard.cancelled')" :value="3">
                <el-tag size="small" type="info">{{ $t('dashboard.cancelled') }}</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item :label="$t('dashboard.petName')">
            <el-input
              v-model="searchForm.petName"
              :placeholder="$t('dashboard.petName')" 
              clearable 
              class="search-input"
              :prefix-icon="Search"
            />
          </el-form-item>
          
          <el-form-item :label="$t('common.phone')">
            <el-input
              v-model="searchForm.ownerPhone"
              :placeholder="$t('common.phone')" 
              clearable 
              class="search-input"
            />
          </el-form-item>

          
          <el-form-item class="search-btns">
            <el-button type="primary" @click="handleSearch" :icon="Search">{{ $t('common.search') }}</el-button>
            <el-button @click="handleReset" :icon="RefreshRight">{{ $t('common.reset') }}</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 数据表格 -->
      <el-card class="table-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="title">{{ $t('接诊列表') }}</span>
              <el-tag type="info" effect="plain" round>{{ pagination.total }}</el-tag>
            </div>
            <div class="header-actions">
              <el-switch
                v-model="autoRefresh"
                :active-text="$t('layout.refreshData')"
                inline-prompt
                :active-icon="Check"
                :inactive-icon="Close"
                @change="handleAutoRefreshChange"
              />
              <el-divider direction="vertical" />
              <el-button type="primary" @click="handleRefresh" :loading="loading" :icon="Refresh">
                {{ $t('layout.refreshData') }}
              </el-button>
            </div>
          </div>
        </template>

          <!-- 数据表格 -->
          <el-table
            v-loading="loading"
            :data="tableDataList"
            stripe
            style="width: 100%"
            class="data-table"
            @selection-change="handleSelectionChange"
            :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: 600 }"
            key="accept-table"
          >
            <el-table-column type="selection" width="55" align="center" />
            
            <el-table-column prop="registerNo" :label="$t('dashboard.appointmentNo')" width="160">
              <template #default="{ row }">
                <div class="register-no">
                  <el-icon><Document /></el-icon>
                  <span>{{ row?.registerNo || '-' }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column :label="$t('dashboard.petName')" min-width="200">
              <template #default="{ row }">
                <div class="pet-info">
                  <div class="pet-name">{{ row?.petName || '-' }}</div>
                  <div class="pet-detail">
                    {{ getSpeciesLabel(row?.petType) }} · {{ row?.breed || '-' }} · {{ row?.age || '-' }}
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column :label="$t('dashboard.owner')" min-width="180">
              <template #default="{ row }">
                <div class="owner-info">
                  <div class="owner-name">{{ row?.ownerName || '-' }}</div>
                  <div class="owner-phone">
                    <el-icon><Phone /></el-icon>
                    {{ row?.ownerPhone || '-' }}
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="serviceType" :label="$t('common.type')" width="120">
              <template #default="{ row }">
                <el-tag :type="getServiceType(row?.serviceType)" effect="light" round>
                  {{ getServiceTypeText(row?.serviceType) || '-' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="status" :label="$t('common.status')" width="100" align="center">
              <template #default="{ row }">
                <div class="status-wrapper">
                  <el-tag :type="getStatusType(row?.status)" effect="dark" round size="small">
                    {{ getStatusText(row?.status) }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="registerTime" :label="$t('dashboard.appointmentTime')" width="210">
              <template #default="{ row }">
                <div class="time-info">
                  <div class="date">{{ formatDate(row?.registerTime) }}</div>
                  <div class="time">{{ formatTime(row?.registerTime) }}</div>
                </div>
              </template>
            </el-table-column>
            
            <!-- 操作列 - 添加开具处方按钮 -->
            <el-table-column :label="$t('accept.action')" fixed="right" width="320" align="center">
              <template #default="{ row }">
                <div class="action-btns" v-if="row">
                  <!-- 接诊按钮（待接诊状态显示） -->
                  <el-button 
                    v-if="row.status === 0" 
                    type="primary" 
                    size="small"
                    round
                    class="accept-btn"
                    @click="handleAccept(row)"
                  >
                    <el-icon><Right /></el-icon>
                    {{ $t('accept.accept') }}
                  </el-button>
                  
                  <!-- 继续按钮（接诊中状态显示） -->
                  <el-button 
                    v-else-if="row.status === 1" 
                    type="warning" 
                    size="small"
                    round
                    @click="handleContinue(row)"
                  >
                    {{ $t('accept.continue') }}
                  </el-button>
                  
                  <!-- 创建病历按钮 -->
                  <el-button 
                    type="success" 
                    size="small"
                    round
                    :disabled="row.status === 3"
                    @click="handleCreateRecord(row)"
                  >
                    <el-icon><Document /></el-icon>
                    {{ $t('accept.record') }}
                  </el-button>
                  
                  <!-- 开具处方按钮 -->
                  <el-button 
                    type="primary" 
                    size="small"
                    round
                    :disabled="row.status === 3"
                    @click="handleCreatePrescription(row)"
                  >
                    <el-icon><FirstAidKit /></el-icon>
                    {{ $t('accept.prescription') }}
                  </el-button>
                  
                  <!-- 详情按钮 -->
                  <el-button type="info" size="small" text @click="handleViewDetail(row)">
                    {{ $t('accept.detail') }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
// 在 handleSearch 和 fetchList 之间添加一个监听
import { watch } from 'vue'

import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, RefreshRight, Timer, FirstAidKit, 
  CircleCheck, Clock, ArrowUp, ArrowDown, Check, Close,
  Right, Document, Phone
} from '@element-plus/icons-vue'
import { acceptModule } from '@/api/doctor/doctor'
import { useSettingsStore } from '@/store/settings'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const settingsStore = useSettingsStore()
const { t } = useI18n()

// 响应式数据
const loading = ref(false)
const autoRefresh = ref(false)
const tableData = ref([])
const selectedRows = ref([])
let refreshTimer = null

// 统计数据
const stats = reactive({
  waiting: 5,
  waitingNew: 2,
  processing: 3,
  completed: 28,
  avgTime: 25
})

const searchForm = reactive({
  status: null,  // 改为 null
  petName: '',
  ownerPhone: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// ========== 计算属性确保始终是数组 ==========
const tableDataList = computed(() => {
  if (!tableData.value) return []
  if (!Array.isArray(tableData.value)) {
    console.warn('tableData 不是数组:', tableData.value)
    return []
  }
  return tableData.value
})

// 获取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    // 确保 status 是数字，如果是 null 或 undefined 则不传
    const statusParam = searchForm.status !== null && searchForm.status !== undefined 
      ? searchForm.status 
      : undefined
    
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      status: statusParam,
      keyword: searchForm.petName || searchForm.ownerPhone || undefined
    }
    
    console.log('请求参数:', params)
    
    const res = await acceptModule.getWaitAcceptList(params)
    console.log('后端返回:', res)
    
    if (res.code === 200 && res.data) {
      const pageData = res.data
      let list = pageData.data || pageData.list || pageData.records || []
      
      if (!Array.isArray(list)) {
        list = []
      }
      
      // 关键：确保 status 是数字类型
      tableData.value = list.map(item => {
        let statusNum = item.status
        // 如果是字符串，转换为数字
        if (typeof statusNum === 'string') {
          statusNum = parseInt(statusNum, 10)
        }
        // 如果是 null 或 undefined，默认为 0
        if (isNaN(statusNum)) {
          statusNum = 0
        }
        
        console.log(`宠物 ${item.petName} 的原始状态: ${item.status}, 转换后: ${statusNum}`)
        
        return {
          ...item,
          status: statusNum,
          waitTime: Math.floor(Math.random() * 30) + 5
        }
      })
      
      console.log('处理后的 tableData:', tableData.value.map(i => ({ petName: i.petName, status: i.status, statusType: typeof i.status })))
      
      pagination.total = pageData.total || 0
      pagination.current = Number(pageData.current) || 1
      pagination.size = Number(pageData.size) || 10
      
      updateStats(tableData.value)
    } else {
      tableData.value = []
      pagination.total = 0
      ElMessage.warning(res.msg || t('accept.loadFailed'))
    }
  } catch (error) {
    console.error('获取列表失败:', error)
    tableData.value = []
    pagination.total = 0
    ElMessage.error(t('accept.loadFailed') + ': ' + (error.message || t('accept.unknownError')))
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = (list) => {
  if (!Array.isArray(list)) return
  stats.waiting = list.filter(item => item?.status === 0).length
  stats.processing = list.filter(item => item?.status === 1).length
  stats.completed = list.filter(item => item?.status === 2).length
}

// 状态处理
// 状态处理 - 处理数字状态
const getStatusType = (status) => {
  // status 是数字 0,1,2,3
  if (status === 0) return 'warning'
  if (status === 1) return 'primary'
  if (status === 2) return 'success'
  if (status === 3) return 'info'
  return 'info'
}

const getStatusText = (status) => {
  console.log('getStatusText 接收到的 status:', status, '类型:', typeof status)
  // status 是数字 0,1,2,3
  if (status === 0) return t('accept.statusWaiting')
  if (status === 1) return t('accept.statusInProgress')
  if (status === 2) return t('accept.statusDone')
  if (status === 3) return t('accept.statusCanceled')
  return t('accept.statusUnknown')
}

// 宠物种类翻译
const getSpeciesLabel = (species) => {
  const key = String(species || '').toLowerCase().trim()
  const map = {
    dog: 'dashboard.typeDog',
    cat: 'dashboard.typeCat',
    rabbit: 'dashboard.typeRabbit',
    '狗': 'dashboard.typeDog',
    '猫': 'dashboard.typeCat',
    '兔': 'dashboard.typeRabbit',
  }
  return map[key] ? t(map[key]) : (species || '-')
}

const getServiceTypeText = (type) => {
  const key = String(type || '').toLowerCase().trim()
  const map = {
    '普通门诊': t('accept.serviceGeneralClinic'),
    '口腔基础检查': t('accept.serviceDentalBasicCheck'),
    '疫苗接种': t('accept.serviceVaccination'),
    '疾病诊疗': t('accept.serviceDiseaseTreatment'),
    '健康体检': t('accept.serviceHealthCheck'),
    '绝育手术': t('accept.serviceSpayNeuter'),
    '美容洗护': t('accept.serviceGrooming'),
    'vaccination': t('accept.serviceVaccination'),
    'disease treatment': t('accept.serviceDiseaseTreatment'),
    'health check': t('accept.serviceHealthCheck'),
    'spay/neuter': t('accept.serviceSpayNeuter'),
    'grooming': t('accept.serviceGrooming'),
    'general clinic': t('accept.serviceGeneralClinic'),
    'general': t('accept.serviceGeneralClinic'),
    'consultation': t('accept.serviceGeneralClinic'),
    'vaccine': t('accept.serviceVaccination'),
    'exam': t('accept.serviceHealthCheck'),
    'bath grooming': t('accept.serviceGrooming'),
  }
  if (map[key]) return map[key]
  // 模糊匹配
  if (key.includes('consult') || key.includes('门诊') || key.includes('clinic')) return t('accept.serviceGeneralClinic')
  if (key.includes('vaccine') || key.includes('疫苗')) return t('accept.serviceVaccination')
  if (key.includes('exam') || key.includes('体检') || key.includes('check')) return t('accept.serviceHealthCheck')
  if (key.includes('groom') || key.includes('美容') || key.includes('bath') || key.includes('洗澡')) return t('accept.serviceGrooming')
  return type || '-'
}

const getServiceType = (type) => {
  const key = String(type || '').toLowerCase().trim()
  const map = {
    '普通门诊': 'success',
    '疫苗接种': 'success',
    'vaccination': 'success',
    'vaccine': 'success',
    '疾病诊疗': 'danger',
    'disease treatment': 'danger',
    '健康体检': 'primary',
    'health check': 'primary',
    'exam': 'primary',
    '绝育手术': 'warning',
    'spay/neuter': 'warning',
    '美容洗护': 'info',
    'grooming': 'info',
    'general': 'primary',
    'general clinic': 'primary',
    'consultation': 'primary'
  }
  return map[key] || 'info'
}

// 格式化时间（兼容空格分隔和 ISO 8601 T 分隔格式）
const formatDate = (datetime) => {
  if (!datetime) return '-'
  if (typeof datetime === 'string') {
    // 先按空格分割，再按 T 分割，兼容两种格式
    const part = datetime.split(' ')[0] || datetime.split('T')[0]
    return part || '-'
  }
  return '-'
}

const formatTime = (datetime) => {
  if (!datetime) return ''
  if (typeof datetime === 'string') {
    // 兼容空格分隔和 ISO 8601 T 分隔格式
    let part = datetime.split(' ')[1]
    if (!part && datetime.includes('T')) {
      part = datetime.split('T')[1]
    }
    return part || ''
  }
  return ''
}

// 搜索重置
const handleSearch = () => {
  console.log('搜索前的 searchForm.status:', searchForm.status)
  pagination.current = 1
  fetchList()
}

// 监听 searchForm.status 变化（可选，用于调试）
watch(() => searchForm.status, (newVal) => {
  console.log('挂号状态变化:', newVal, '类型:', typeof newVal)
})


const handleReset = () => {
  searchForm.status = undefined
  searchForm.petName = ''
  searchForm.ownerPhone = ''
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  fetchList()
  ElMessage.success(t('accept.refreshed'))
}

// 自动刷新
const handleAutoRefreshChange = (val) => {
  if (val) {
    const interval = (settingsStore.refreshInterval || 30) * 1000
    refreshTimer = setInterval(fetchList, interval)
    ElMessage.success(t('accept.autoRefreshOn', { interval: settingsStore.refreshInterval || 30 }))
  } else {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 监听刷新间隔变化，自动更新定时器
watch(() => settingsStore.refreshInterval, (newVal) => {
  if (autoRefresh.value && refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = setInterval(fetchList, (newVal || 30) * 1000)
    console.log('自动刷新间隔已更新为', newVal, '秒')
  }
})

// 接诊准备
const handleAccept = async (row) => {
  if (!row) return

  const petId = row.petId || row.pet_id || row.id
  const registerId = row.registerId || row.register_id || row.id
  
  if (!petId) {
    ElMessage.error(t('accept.cannotGetPetId'))
    console.error('row对象:', row)
    return
  }

  try {
    await ElMessageBox.confirm(
      `<div style="text-align: center;">
        <div style="font-size: 48px; margin-bottom: 15px;">🐾</div>
        <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px;">${t('accept.startAccept')}</div>
        <div style="color: #666;">${t('accept.petLabel', { name: row.petName || t('common.unknown'), breed: row.breed || '-' })}</div>
        <div style="color: #666; margin-top: 5px;">${t('accept.ownerLabel', { name: row.ownerName || '-' })}</div>
      </div>`,
      t('accept.confirmAccept'),
      {
        confirmButtonText: t('accept.confirm'),
        cancelButtonText: t('accept.cancel'),
        dangerouslyUseHTMLString: true,
        center: true
      }
    )
    
    await acceptModule.updateAcceptStatus({
      registerId: registerId,
      status: 1
    })
    
    // 通知提醒
    if (settingsStore.shouldNotify('newRegister')) {
      settingsStore.sendDesktopNotification(t('accept.newRegistration'), t('accept.petAccepted', { name: row.petName || t('common.unknown') }))
      settingsStore.playNotificationSound()
    }
    
    ElMessage.success(t('accept.acceptedRedirecting'))
    router.push({
      path: '/doctor/pet',
      query: { 
        petId: petId,
        registerId: registerId
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接诊失败:', error)
      ElMessage.error(t('accept.acceptFailed'))
    }
  }
}

// 继续接诊
const handleContinue = (row) => {
  if (!row) return
  const petId = row.petId || row.pet_id || row.id
  const registerId = row.registerId || row.register_id || row.id
  router.push({
    path: '/doctor/pet',
    query: { 
      petId: petId,
      registerId: registerId
    }
  })
}

// ========== 关键修复：创建病历跳转函数 ==========
// ========== 关键修复：创建病历跳转函数 ==========
const handleCreateRecord = (row) => {
  if (!row) {
    ElMessage.error(t('accept.dataErrorCannotCreateRecord'))
    return
  }
  
  // 关键修复：获取数字类型的 ID
  // registerId 是数字ID（如 11），registerNo 是字符串编号（如 "RG2026041101"）
  const petId = row.petId || row.pet_id
  const registerId = row.registerId || row.register_id || row.id
  
  console.log('创建病历 - 原始数据:', {
    petId,
    registerId,
    registerNo: row.registerNo,
    petName: row.petName
  })
  
  if (!petId || !registerId) {
    ElMessage.error(t('accept.cannotGetPetOrRegInfo'))
    console.error('row数据:', row)
    return
  }
  
  // 确保是数字类型
  const numericPetId = Number(petId)
  const numericRegisterId = Number(registerId)
  
  if (isNaN(numericPetId) || isNaN(numericRegisterId)) {
    ElMessage.error(t('accept.dataFormatErrorContactAdmin'))
    return
  }
  
  console.log('跳转到病历创建页面，参数:', { 
    petId: numericPetId, 
    registerId: numericRegisterId, 
    petName: row.petName 
  })
  
  router.push({
    path: '/doctor/record',
    query: {
      action: 'create',
      petId: numericPetId,
      registerId: numericRegisterId,
      petName: row.petName || ''
    }
  })
}

// ========== 新增：开具处方跳转函数 ==========
// ========== 开具处方跳转函数 ==========
const handleCreatePrescription = (row) => {
  if (!row) {
    ElMessage.error(t('accept.dataErrorCannotCreatePrescription'))
    return
  }
  
  // 获取数字类型的 ID
  const petId = row.petId || row.pet_id
  const registerId = row.registerId || row.register_id || row.id
  
  console.log('开具处方 - 原始数据:', {
    petId,
    registerId,
    registerNo: row.registerNo,
    petName: row.petName
  })
  
  if (!petId || !registerId) {
    ElMessage.error(t('accept.cannotGetPetOrRegInfo'))
    console.error('row数据:', row)
    return
  }
  
  // 确保是数字类型
  const numericPetId = Number(petId)
  const numericRegisterId = Number(registerId)
  
  if (isNaN(numericPetId) || isNaN(numericRegisterId)) {
    ElMessage.error(t('accept.dataFormatErrorContactAdmin'))
    return
  }
  
  console.log('跳转到处方开具页面，参数:', { 
    petId: numericPetId, 
    registerId: numericRegisterId, 
    petName: row.petName 
  })
  
  // 跳转到处方开具页面
  router.push({
    path: '/doctor/prescription',
    query: {
      action: 'create',
      petId: numericPetId,
      registerId: numericRegisterId,
      petName: row.petName || ''
    }
  })
}


// 查看详情
const handleViewDetail = (row) => {
  if (!row) return
  const petId = row.petId || row.pet_id || row.petId
  const registerId = row.registerId || row.id || row.register_id
  if (!petId) {
    ElMessage.error(t('accept.cannotGetPetId'))
    return
  }
  router.push({
    path: '/doctor/pet',
    query: { 
      petId: petId,
      registerId: registerId
    }
  })
}

// 表格选择
const handleSelectionChange = (val) => {
  selectedRows.value = val || []
}

// 分页事件
const handleSizeChange = (val) => {
  pagination.size = val
  fetchList()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  fetchList()
}

onMounted(() => {
  tableData.value = []
  fetchList()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped lang="scss">
.accept-page {
  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 25px;
    
    .stat-card {
      background: white;
      border-radius: 16px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 4px;
        height: 100%;
      }
      
      &.waiting::before { background: #f59e0b; }
      &.processing::before { background: #3b82f6; }
      &.completed::before { background: #10b981; }
      &.avg-time::before { background: #8b5cf6; }
      
      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        
        .el-icon {
          color: white;
        }
      }
      
      &.waiting .stat-icon { background: linear-gradient(135deg, #f59e0b, #d97706); }
      &.processing .stat-icon { background: linear-gradient(135deg, #3b82f6, #2563eb); }
      &.completed .stat-icon { background: linear-gradient(135deg, #10b981, #059669); }
      &.avg-time .stat-icon { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
      
      .stat-info {
        flex: 1;
        
        .number {
          font-size: 32px;
          font-weight: 700;
          color: #1e293b;
          line-height: 1;
        }
        
        .label {
          font-size: 14px;
          color: #64748b;
          margin-top: 6px;
        }
      }
      
      .trend {
        position: absolute;
        top: 20px;
        right: 20px;
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        padding: 4px 8px;
        border-radius: 20px;
        
        &.up {
          color: #10b981;
          background: #d1fae5;
        }
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
  
  .search-card {
    border-radius: 16px;
    margin-bottom: 20px;
    
    .search-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #f1f5f9;
      
      .search-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: #334155;
        
        .el-icon {
          color: #3b82f6;
        }
      }
      
      .arrow {
        transition: transform 0.3s;
        
        &.rotate {
          transform: rotate(180deg);
        }
      }
    }
    
    .search-form {
      .el-form-item {
        margin-bottom: 0;
        margin-right: 20px;
      }
      
      .status-select {
        width: 140px;
      }
      
      .search-input {
        width: 200px;
      }
      
      .date-picker {
        width: 320px;
      }
      
      .search-btns {
        margin-left: auto;
        margin-right: 0;
      }
    }
  }
  
  .table-card {
    border-radius: 16px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .title {
          font-size: 18px;
          font-weight: 600;
          color: #1e293b;
        }
      }
      
      .header-actions {
        display: flex;
        align-items: center;
        gap: 15px;
      }
    }
    
    .data-table {
      margin-top: 20px;
      
      .register-no {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #3b82f6;
        font-weight: 500;
        font-family: monospace;
        
        .el-icon {
          color: #94a3b8;
        }
      }
      
      .pet-info {
        .pet-name {
          font-weight: 600;
          color: #1e293b;
          font-size: 15px;
        }
        
        .pet-detail {
          color: #64748b;
          font-size: 13px;
          margin-top: 4px;
        }
      }
      
      .owner-info {
        .owner-name {
          font-weight: 500;
          color: #334155;
        }
        
        .owner-phone {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #64748b;
          font-size: 13px;
          margin-top: 4px;
          
          .el-icon {
            font-size: 12px;
          }
        }
      }
      
      .status-wrapper {
        text-align: center;
        
        .wait-time {
          font-size: 12px;
          color: #f59e0b;
          margin-top: 4px;
        }
      }
      
      .time-info {
        .date {
          color: #334155;
          font-weight: 500;
        }
        
        .time {
          color: #94a3b8;
          font-size: 13px;
          margin-top: 2px;
        }
      }
      
      .action-btns {
        display: flex;
        gap: 8px;
        justify-content: center;
        flex-wrap: wrap;
        
        .accept-btn {
          background: linear-gradient(135deg, #3b82f6, #2563eb);
          border: none;
          
          &:hover {
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
          }
        }
      }
    }
    
    .pagination-wrapper {
      margin-top: 25px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>