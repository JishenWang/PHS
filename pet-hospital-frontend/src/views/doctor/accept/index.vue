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
            <div class="label">待接诊</div>
          </div>
          <div class="trend up">
            <el-icon><ArrowUp /></el-icon>
            <span>+{{ stats.waitingNew }} 新增</span>
          </div>
        </div>
        
        <div class="stat-card processing">
          <div class="stat-icon">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="number">{{ stats.processing }}</div>
            <div class="label">接诊中</div>
          </div>
        </div>
        
        <div class="stat-card completed">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="number">{{ stats.completed }}</div>
            <div class="label">今日已完成</div>
          </div>
        </div>
        
        <div class="stat-card avg-time">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="number">{{ stats.avgTime }}min</div>
            <div class="label">平均接诊时长</div>
          </div>
        </div>
      </div>

      <!-- 搜索筛选区 -->
      <el-card class="search-card" shadow="hover">
        <div class="search-header">
          <div class="search-title">
            <el-icon><Search /></el-icon>
            <span>筛选查询</span>
          </div>
          <el-button type="primary" text @click="toggleAdvanced">
            {{ showAdvanced ? '收起' : '高级筛选' }}
            <el-icon class="arrow" :class="{ rotate: showAdvanced }"><ArrowDown /></el-icon>
          </el-button>
        </div>
        
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item label="挂号状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable class="status-select">
              <el-option label="待接诊" :value="0">
                <el-tag size="small" type="warning">待接诊</el-tag>
              </el-option>
              <el-option label="接诊中" :value="1">
                <el-tag size="small" type="primary">接诊中</el-tag>
              </el-option>
              <el-option label="已完成" :value="2">
                <el-tag size="small" type="success">已完成</el-tag>
              </el-option>
              <el-option label="已取消" :value="3">
                <el-tag size="small" type="info">已取消</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="宠物名称">
            <el-input 
              v-model="searchForm.petName" 
              placeholder="请输入宠物名称" 
              clearable 
              class="search-input"
              :prefix-icon="Search"
            />
          </el-form-item>
          
          <el-form-item label="客户手机号">
            <el-input 
              v-model="searchForm.ownerPhone" 
              placeholder="请输入手机号" 
              clearable 
              class="search-input"
            />
          </el-form-item>
          
          <template v-if="showAdvanced">
            <el-form-item label="挂号时间">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                class="date-picker"
              />
            </el-form-item>
          </template>
          
          <el-form-item class="search-btns">
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
            <el-button @click="handleReset" :icon="RefreshRight">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 数据表格 -->
      <el-card class="table-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="title">接诊列表</span>
              <el-tag type="info" effect="plain" round>共 {{ pagination.total }} 条</el-tag>
            </div>
            <div class="header-actions">
              <el-switch
                v-model="autoRefresh"
                active-text="自动刷新"
                inline-prompt
                :active-icon="Check"
                :inactive-icon="Close"
                @change="handleAutoRefreshChange"
              />
              <el-divider direction="vertical" />
              <el-button type="primary" @click="handleRefresh" :loading="loading" :icon="Refresh">
                刷新
              </el-button>
            </div>
          </div>
        </template>

          <!-- 数据表格 -->
          <el-table
            v-loading="loading"
            :data="tableDataList"
            stripe
            class="data-table"
            @selection-change="handleSelectionChange"
            :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: 600 }"
            key="accept-table"
          >
            <el-table-column type="selection" width="55" align="center" />
            
            <el-table-column prop="registerNo" label="挂号单号" width="160">
              <template #default="{ row }">
                <div class="register-no">
                  <el-icon><Document /></el-icon>
                  <span>{{ row?.registerNo || '无单号' }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="宠物信息" min-width="200">
              <template #default="{ row }">
                <div class="pet-info">
                  <div class="pet-name">{{ row?.petName || '未知宠物' }}</div>
                  <div class="pet-detail">
                    {{ row?.petType || '-' }} · {{ row?.breed || '-' }} · {{ row?.age || '-' }}岁
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="主人信息" min-width="180">
              <template #default="{ row }">
                <div class="owner-info">
                  <div class="owner-name">{{ row?.ownerName || '未知' }}</div>
                  <div class="owner-phone">
                    <el-icon><Phone /></el-icon>
                    {{ row?.ownerPhone || '-' }}
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="serviceType" label="服务类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getServiceType(row?.serviceType)" effect="light" round>
                  {{ row?.serviceType || '普通诊疗' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <div class="status-wrapper">
                  <el-tag :type="getStatusType(row?.status)" effect="dark" round size="small">
                    {{ getStatusText(row?.status) }}
                  </el-tag>
                  <div v-if="row?.status === 0" class="wait-time">{{ row?.waitTime }}分钟</div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="registerTime" label="挂号时间" width="170">
              <template #default="{ row }">
                <div class="time-info">
                  <div class="date">{{ formatDate(row?.registerTime) }}</div>
                  <div class="time">{{ formatTime(row?.registerTime) }}</div>
                </div>
              </template>
            </el-table-column>
            
            <!-- 操作列 - 添加开具处方按钮 -->
            <el-table-column label="操作" fixed="right" width="320" align="center">
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
                    接诊
                  </el-button>
                  
                  <!-- 继续按钮（接诊中状态显示） -->
                  <el-button 
                    v-else-if="row.status === 1" 
                    type="warning" 
                    size="small"
                    round
                    @click="handleContinue(row)"
                  >
                    继续
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
                    病历
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
                    处方
                  </el-button>
                  
                  <!-- 详情按钮 -->
                  <el-button type="info" size="small" text @click="handleViewDetail(row)">
                    详情
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

const router = useRouter()

// 响应式数据
const loading = ref(false)
const autoRefresh = ref(false)
const showAdvanced = ref(false)
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
  ownerPhone: '',
  dateRange: []
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
      ElMessage.warning(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('获取列表失败:', error)
    tableData.value = []
    pagination.total = 0
    ElMessage.error('获取列表失败: ' + (error.message || '未知错误'))
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
  if (status === 0) return '待接诊'
  if (status === 1) return '接诊中'
  if (status === 2) return '已完成'
  if (status === 3) return '已取消'
  return '未知'
}

const getServiceType = (type) => {
  const map = {
    '疫苗接种': 'success',
    '疾病诊疗': 'danger',
    '健康体检': 'primary',
    '绝育手术': 'warning',
    '美容洗护': 'info',
    '普通诊疗': 'primary'
  }
  return map[type] || 'info'
}

// 格式化时间
const formatDate = (datetime) => {
  if (!datetime) return '-'
  if (typeof datetime === 'string') {
    return datetime.split(' ')[0] || '-'
  }
  return '-'
}

const formatTime = (datetime) => {
  if (!datetime) return ''
  if (typeof datetime === 'string') {
    return datetime.split(' ')[1] || ''
  }
  return ''
}

// 搜索重置
const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value
}

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
  searchForm.dateRange = []
  handleSearch()
}

// 刷新
const handleRefresh = () => {
  fetchList()
  ElMessage.success('数据已刷新')
}

// 自动刷新
const handleAutoRefreshChange = (val) => {
  if (val) {
    refreshTimer = setInterval(fetchList, 30000)
    ElMessage.success('已开启自动刷新（30秒）')
  } else {
    clearInterval(refreshTimer)
  }
}

// 接诊准备
const handleAccept = async (row) => {
  if (!row) return

  const petId = row.petId || row.pet_id || row.id
  const registerId = row.registerId || row.register_id || row.id
  
  if (!petId) {
    ElMessage.error('无法获取宠物ID，请刷新重试')
    console.error('row对象:', row)
    return
  }

  try {
    await ElMessageBox.confirm(
      `<div style="text-align: center;">
        <div style="font-size: 48px; margin-bottom: 15px;">🐾</div>
        <div style="font-size: 18px; font-weight: bold; margin-bottom: 10px;">开始接诊</div>
        <div style="color: #666;">宠物：${row.petName || '未知'}（${row.breed || '-'}）</div>
        <div style="color: #666; margin-top: 5px;">主人：${row.ownerName || '-'}</div>
      </div>`,
      '接诊确认',
      {
        confirmButtonText: '确认接诊',
        cancelButtonText: '取消',
        dangerouslyUseHTMLString: true,
        center: true
      }
    )
    
    await acceptModule.updateAcceptStatus({
      registerId: registerId,
      status: 1
    })
    
    ElMessage.success('接诊成功，正在跳转...')
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
      ElMessage.error('接诊失败')
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
    ElMessage.error('数据异常，无法创建病历')
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
    ElMessage.error('无法获取宠物或挂号信息，请刷新后重试')
    console.error('row数据:', row)
    return
  }
  
  // 确保是数字类型
  const numericPetId = Number(petId)
  const numericRegisterId = Number(registerId)
  
  if (isNaN(numericPetId) || isNaN(numericRegisterId)) {
    ElMessage.error('数据格式错误，请联系管理员')
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
    ElMessage.error('数据异常，无法开具处方')
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
    ElMessage.error('无法获取宠物或挂号信息，请刷新后重试')
    console.error('row数据:', row)
    return
  }
  
  // 确保是数字类型
  const numericPetId = Number(petId)
  const numericRegisterId = Number(registerId)
  
  if (isNaN(numericPetId) || isNaN(numericRegisterId)) {
    ElMessage.error('数据格式错误，请联系管理员')
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
  const petId = row.petId || row.pet_id || row.id
  if (!petId) {
    ElMessage.error('无法获取宠物ID')
    return
  }
  router.push({
    path: '/doctor/pet',
    query: { petId: petId }
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