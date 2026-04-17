<template>
    <div class="pet-page" v-loading="loading">
      <!-- 顶部操作栏 -->
      <div class="page-header">
        <div></div> <!-- 占位，保持 flex 布局 -->
        <div class="header-actions">
          <el-button type="primary" @click="handleCreateRecord">
            <el-icon><DocumentAdd /></el-icon>
            创建病历
          </el-button>
          <el-button type="success" @click="handleCreatePrescription">
            <el-icon><FirstAidKit /></el-icon>
            开具处方
          </el-button>
          <el-button @click="handleViewHistory">
            <el-icon><Clock /></el-icon>
            历史记录
          </el-button>
        </div>
      </div>

      <!-- 宠物档案主卡片 -->
      <div class="profile-section">
        <div class="pet-profile-card">
          <div class="pet-avatar-section">
            <div class="pet-avatar">
              <el-avatar :size="120" :src="petInfo.avatar" class="avatar-img">
                <el-icon :size="60"><FirstAidKit /></el-icon>
              </el-avatar>
              <div class="status-badge" :class="petInfo.status === 1 ? 'active' : 'inactive'">
                {{ petInfo.status === 1 ? '健康' : '观察中' }}
              </div>
            </div>
            <div class="pet-basic">
              <h2 class="pet-name">{{ petInfo.name || '未命名' }}</h2>
              <div class="pet-tags">
                <el-tag type="primary" effect="light" round>{{ petInfo.type || '未知' }}</el-tag>
                <el-tag type="warning" effect="light" round>{{ petInfo.breed || '未知品种' }}</el-tag>
                <el-tag type="success" effect="light" round>{{ petInfo.gender === 1 ? '公' : '母' }}</el-tag>
              </div>
            </div>
          </div>
          
          <div class="pet-quick-stats">
            <div class="quick-stat">
              <div class="label">年龄</div>
              <div class="value">{{ petInfo.age || 0 }}<span class="unit">岁</span></div>
            </div>
            <div class="quick-stat">
              <div class="label">体重</div>
              <div class="value">{{ petInfo.weight || 0 }}<span class="unit">kg</span></div>
            </div>
            <div class="quick-stat">
              <div class="label">毛色</div>
              <div class="value color-value">
                <span class="color-dot" :style="{ background: petInfo.color || '#D4A574' }"></span>
                {{ petInfo.color || '金色' }}
              </div>
            </div>
            <div class="quick-stat">
              <div class="label">芯片号</div>
              <div class="value chip">{{ petInfo.chipNumber || '未植入' }}</div>
            </div>
          </div>
        </div>

        <!-- 主人信息卡片 -->
        <div class="owner-card">
          <div class="card-title">
            <el-icon><User /></el-icon>
            主人信息
          </div>
          <div class="owner-content">
            <div class="owner-header">
              <el-avatar :size="48" :src="petInfo.ownerAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="owner-meta">
                <div class="name">{{ petInfo.ownerName || '未知' }}</div>
                <div class="phone">
                  <el-icon><Phone /></el-icon>
                  {{ petInfo.ownerPhone || '未填写' }}
                </div>
              </div>
            </div>
            <div class="owner-address" v-if="petInfo.ownerAddress">
              <el-icon><Location /></el-icon>
              {{ petInfo.ownerAddress }}
            </div>
            <div class="owner-address" v-else>
              <el-icon><Location /></el-icon>
              未填写地址
            </div>
          </div>
        </div>
      </div>

      <!-- 详细信息网格 -->
      <div class="detail-grid">
        <el-card class="detail-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><FirstAidKit /></el-icon>
              <span>健康信息</span>
            </div>
          </template>
          <div class="detail-list">
            <div class="detail-item">
              <span class="label">过敏史</span>
              <span class="value">
                <el-tag v-if="petInfo.allergy" type="danger" effect="light">
                  <el-icon><Warning /></el-icon>
                  {{ petInfo.allergy }}
                </el-tag>
                <span v-else class="text-muted">无过敏记录</span>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">疫苗状态</span>
              <span class="value">
                <el-tag type="success" effect="light">{{ petInfo.vaccineStatus || '已接种狂犬疫苗' }}</el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">绝育状态</span>
              <span class="value">{{ petInfo.neutered ? '已绝育' : '未绝育' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">上次体检</span>
              <span class="value">{{ petInfo.lastCheckup || '暂无记录' }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="detail-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
          </template>
          <div class="detail-list">
            <div class="detail-item">
              <span class="label">出生日期</span>
              <span class="value">{{ petInfo.birthday || '未知' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">登记时间</span>
              <span class="value">{{ petInfo.createTime || '未知' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">来源渠道</span>
              <span class="value">{{ petInfo.source || '门诊登记' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">备注</span>
              <span class="value text-wrap">{{ petInfo.remark || '无特殊备注' }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 健康记录时间轴 -->
      <el-card class="timeline-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <el-icon><Calendar /></el-icon>
              <span>健康档案时间轴</span>
            </div>
            <el-radio-group v-model="timelineFilter" size="small">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="medical">病历</el-radio-button>
              <el-radio-button label="vaccine">疫苗</el-radio-button>
              <el-radio-button label="exam">体检</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        
        <div class="timeline-wrapper">
          <el-timeline v-if="filteredRecords.length > 0">
            <el-timeline-item
              v-for="(record, index) in filteredRecords"
              :key="index"
              :type="record.type"
              :color="getTimelineColor(record.type)"
              :timestamp="record.time"
              placement="top"
              size="large"
            >
              <div class="timeline-content" @click="viewRecordDetail(record)">
                <div class="timeline-header">
                  <h4 class="title">{{ record.title }}</h4>
                  <el-tag :type="record.type" size="small" effect="light" round>
                    {{ getRecordTypeText(record.type) }}
                  </el-tag>
                </div>
                <p class="description">{{ record.content }}</p>
                <div class="timeline-footer">
                  <span class="doctor">👨‍⚕️ {{ record.doctor }}</span>
                  <el-button v-if="record.hasDetail" type="primary" link size="small">
                    查看详情 <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          
          <el-empty v-else description="暂无健康记录">
            <el-button type="primary" @click="handleCreateRecord">创建第一条记录</el-button>
          </el-empty>
        </div>
      </el-card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  DocumentAdd, FirstAidKit, Clock, User, 
  UserFilled, Phone, Location, Warning, InfoFilled, 
  Calendar, ArrowRight
} from '@element-plus/icons-vue'
import { acceptModule, recordModule } from '@/api/doctor/doctor'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const timelineFilter = ref('all')

// 关键修复：从路由参数获取，并添加调试
const petId = ref(route.query.petId)
const registerId = ref(route.query.registerId)

// 调试输出
console.log('路由参数:', route.query)
console.log('petId:', petId.value)
console.log('registerId:', registerId.value)

// 宠物信息 - 默认空值
const petInfo = ref({
  petId: '',
  name: '',
  type: '',
  breed: '',
  gender: 1,
  age: 0,
  birthday: '',
  weight: 0,
  color: '',
  chipNumber: '',
  allergy: '',
  vaccineStatus: '',
  neutered: false,
  lastCheckup: '',
  remark: '',
  status: 1,
  ownerName: '',
  ownerPhone: '',
  ownerAddress: '',
  createTime: '',
  source: ''
})

// 健康记录
const healthRecords = ref([])

// 过滤后的记录
const filteredRecords = computed(() => {
  if (timelineFilter.value === 'all') return healthRecords.value
  const typeMap = {
    medical: 'danger',
    vaccine: 'success',
    exam: 'primary'
  }
  return healthRecords.value.filter(r => r.type === typeMap[timelineFilter.value])
})

// 获取宠物详情 - 关键修复
const fetchPetDetail = async () => {
  // 关键检查：必须有 petId
  if (!petId.value) {
    ElMessage.error('缺少宠物ID参数，请从接诊列表点击进入')
    console.error('错误：缺少 petId 参数，当前路由:', route.fullPath)
    return
  }
  
  loading.value = true
  console.log('开始查询宠物详情, petId:', petId.value)
  
  try {
    const res = await acceptModule.getPetDetail(petId.value)
    console.log('宠物详情API返回:', res)
    
    if (res.code === 200 && res.data) {
      const data = res.data
      
      // 关键修复：映射后端字段到前端字段，添加更多默认值处理
      petInfo.value = {
        petId: data.petId || petId.value,
        name: data.name || '未命名',
        type: data.type || '未知',
        breed: data.breed || '未知品种',
        gender: data.gender === 2 ? 2 : 1,  // 明确判断：只有2是母，其他都是公
        age: data.age || 0,
        birthday: data.birthday || '',
        weight: data.weight || 0,
        color: data.color || '#D4A574',
        chipNumber: data.chipNumber || '未植入',
        allergy: data.allergy || '',
        vaccineStatus: data.vaccineStatus || '暂无记录',
        neutered: data.neutered || false,
        lastCheckup: data.lastCheckup || '暂无记录',
        remark: data.description || data.remark || '无特殊备注',
        status: data.status === 1 ? 1 : 0,
        ownerName: data.ownerName || '未知',
        ownerPhone: data.ownerPhone || '未填写',
        ownerAddress: data.ownerAddress || '未填写地址',
        createTime: data.createTime || '',
        source: data.source || '门诊登记'
      }
      
      console.log('宠物信息加载成功:', petInfo.value)
      
    } else {
      ElMessage.error(res.msg || '获取宠物信息失败')
      console.error('API返回错误:', res)
    }
  } catch (error) {
    console.error('获取宠物信息失败', error)
    ElMessage.error('获取宠物信息失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取健康记录（病历/疫苗/体检）
const fetchHealthRecords = async () => {
  if (!petId.value) return
  
  try {
    // 调用病历列表API获取该宠物的病历
    const res = await recordModule.getMedicalRecordList({
      petId: petId.value,
      pageNum: 1,
      pageSize: 50
    })
    
    console.log('健康记录API返回:', res)
    
    // 关键修复：后端返回的 total 是 0 但 data 有数据，需要特殊处理
    if (res.code === 200 && res.data) {
      let list = []
      
      // 方式1：从 res.data.data 获取
      if (res.data.data && Array.isArray(res.data.data)) {
        list = res.data.data
      }
      // 方式2：从 res.data.list 获取
      else if (res.data.list && Array.isArray(res.data.list)) {
        list = res.data.list
      }
      // 方式3：从 res.data.records 获取
      else if (res.data.records && Array.isArray(res.data.records)) {
        list = res.data.records
      }
      // 方式4：res.data 本身就是数组
      else if (Array.isArray(res.data)) {
        list = res.data
      }
      
      console.log('解析出的列表:', list)
      
      // 转换为时间轴格式
      if (list.length > 0) {
        healthRecords.value = list.map(record => ({
          type: getRecordType(record.type || '门诊'),
          title: record.title || record.diagnosis || '就诊记录',
          content: record.chiefComplaint || record.symptoms || record.diagnosis || '',
          time: record.createTime ? record.createTime.split('T')[0] : '',
          doctor: record.doctorName || '医生',
          hasDetail: true,
          recordId: record.recordId || record.id
        }))
      } else {
        // 如果没有病历记录，显示空数组
        healthRecords.value = []
      }
      
      console.log('转换后的健康记录:', healthRecords.value)
    } else {
      healthRecords.value = []
    }
  } catch (error) {
    console.error('获取健康记录失败:', error)
    healthRecords.value = []
  }
}

// 时间轴样式
const getTimelineColor = (type) => {
  const map = { danger: '#f56c6c', success: '#67c23a', primary: '#409eff', warning: '#e6a23c' }
  return map[type] || '#909399'
}

const getRecordTypeText = (type) => {
  const map = { danger: '病历', success: '疫苗', primary: '体检', warning: '治疗' }
  return map[type] || '其他'
}

const getRecordType = (type) => {
  const typeMap = {
    '门诊': 'danger',
    '疫苗': 'success',
    '体检': 'primary',
    '手术': 'warning',
    '复查': 'info'
  }
  return typeMap[type] || 'danger'
}

// 创建病历
const handleCreateRecord = () => {
  router.push({
    path: '/doctor/record',
    query: {
      petId: petId.value,
      registerId: registerId.value,
      action: 'create'
    }
  })
}

// 开具处方
const handleCreatePrescription = () => {
  router.push({
    path: '/doctor/prescription',
    query: {
      petId: petId.value,
      registerId: registerId.value,
      action: 'create'
    }
  })
}

// 查看历史记录
const handleViewHistory = () => {
  router.push({
    path: '/doctor/record',
    query: { petId: petId.value }
  })
}

// 查看记录详情
const viewRecordDetail = (record) => {
  ElMessage.info(`查看${record.title}详情`)
}

onMounted(() => {
  console.log('宠物档案页面加载，完整路由:', route.fullPath)
  console.log('route.query:', route.query)
  console.log('route.params:', route.params)
  
  // 尝试多种方式获取 petId
  petId.value = route.query.petId || route.params.petId || route.query.id
  registerId.value = route.query.registerId || route.params.registerId
  
  console.log('解析后的 petId:', petId.value)
  console.log('解析后的 registerId:', registerId.value)
  
  if (!petId.value) {
    console.log('未传递宠物ID,显示默认空状态')
  } else {
    fetchPetDetail()
    fetchHealthRecords()
  }
})
</script>

<style scoped lang="scss">
.pet-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
    
    .header-actions {
      display: flex;
      gap: 12px;
      margin-left: auto;
    }
  }
  
  .profile-section {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 20px;
    margin-bottom: 25px;
    
    .pet-profile-card {
      background: white;
      border-radius: 20px;
      padding: 30px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
      
      .pet-avatar-section {
        display: flex;
        align-items: center;
        gap: 24px;
        margin-bottom: 30px;
        padding-bottom: 30px;
        border-bottom: 1px solid #f1f5f9;
        
        .pet-avatar {
          position: relative;
          flex-shrink: 0;
          
          .avatar-img {
            border: 4px solid #e0f2fe;
            
            :deep(img) {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
            
            :deep(.el-icon) {
              font-size: 60px;
              color: #94a3b8;
            }
          }
          
          .status-badge {
            position: absolute;
            bottom: -5px;
            left: 50%;
            transform: translateX(-50%);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
            color: white;
            white-space: nowrap;
            
            &.active {
              background: #10b981;
            }
            
            &.inactive {
              background: #f59e0b;
            }
          }
        }
        
        .pet-basic {
          flex: 1;
          
          .pet-name {
            font-size: 32px;
            font-weight: 700;
            color: #1e293b;
            margin-bottom: 12px;
          }
          
          .pet-tags {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
          }
        }
      }
      
      .pet-quick-stats {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 20px;
        
        .quick-stat {
          text-align: center;
          padding: 15px;
          background: #f8fafc;
          border-radius: 12px;
          
          .label {
            font-size: 13px;
            color: #64748b;
            margin-bottom: 8px;
          }
          
          .value {
            font-size: 24px;
            font-weight: 700;
            color: #1e293b;
            
            .unit {
              font-size: 14px;
              color: #94a3b8;
              margin-left: 4px;
              font-weight: 400;
            }
            
            &.color-value {
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 6px;
              font-size: 16px;
              
              .color-dot {
                width: 16px;
                height: 16px;
                border-radius: 50%;
              }
            }
            
            &.chip {
              font-family: monospace;
              font-size: 14px;
              color: #475569;
            }
          }
        }
      }
    }
    
    .owner-card {
      background: white;
      border-radius: 20px;
      padding: 24px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
      
      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: #334155;
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 1px solid #f1f5f9;
        
        .el-icon {
          color: #3b82f6;
        }
      }
      
      .owner-content {
        .owner-header {
          display: flex;
          align-items: center;
          gap: 15px;
          margin-bottom: 20px;
          
          .owner-meta {
            .name {
              font-size: 18px;
              font-weight: 600;
              color: #1e293b;
              margin-bottom: 6px;
            }
            
            .phone {
              display: flex;
              align-items: center;
              gap: 6px;
              color: #3b82f6;
              font-size: 15px;
              
              .el-icon {
                font-size: 14px;
              }
            }
          }
        }
        
        .owner-address {
          display: flex;
          align-items: flex-start;
          gap: 8px;
          padding: 15px;
          background: #f8fafc;
          border-radius: 10px;
          color: #64748b;
          font-size: 14px;
          line-height: 1.6;
          
          .el-icon {
            color: #94a3b8;
            margin-top: 2px;
            flex-shrink: 0;
          }
        }
      }
    }
  }
  
  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 25px;
    
    .detail-card {
      border-radius: 16px;
      
      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        
        .el-icon {
          color: #3b82f6;
        }
      }
      
      .detail-list {
        .detail-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px 0;
          border-bottom: 1px solid #f1f5f9;
          
          &:last-child {
            border-bottom: none;
            padding-bottom: 0;
          }
          
          &:first-child {
            padding-top: 0;
          }
          
          .label {
            color: #64748b;
            font-size: 14px;
          }
          
          .value {
            color: #334155;
            font-weight: 500;
            
            &.text-muted {
              color: #94a3b8;
            }
            
            &.text-wrap {
              max-width: 200px;
              text-align: right;
              line-height: 1.5;
            }
          }
        }
      }
    }
  }
  
  .timeline-card {
    border-radius: 16px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        
        .el-icon {
          color: #3b82f6;
        }
      }
    }
    
    .timeline-wrapper {
      padding: 20px;
      
      .el-timeline {
        padding-left: 10px;
      }
      
      .timeline-content {
        background: #f8fafc;
        border-radius: 12px;
        padding: 20px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #f1f5f9;
          transform: translateX(5px);
        }
        
        .timeline-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
          
          .title {
            font-size: 16px;
            font-weight: 600;
            color: #1e293b;
          }
        }
        
        .description {
          color: #64748b;
          line-height: 1.6;
          margin-bottom: 15px;
        }
        
        .timeline-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .doctor {
            color: #94a3b8;
            font-size: 13px;
          }
        }
      }
    }
  }
}
</style>