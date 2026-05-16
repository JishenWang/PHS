<template>
  <div class="pet-page" v-loading="loading">
    <!-- 未选择宠物：引导页面 -->
    <div v-if="!hasPetId" class="empty-state">
      <div class="empty-content">
        <!-- 图标 -->
        <div class="empty-icon">
          <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="60" cy="60" r="52" fill="#f0f9ff" />
            <ellipse cx="42" cy="55" rx="9" ry="12" fill="#bae6fd" transform="rotate(-20 42 55)" />
            <ellipse cx="78" cy="55" rx="9" ry="12" fill="#bae6fd" transform="rotate(20 78 55)" />
            <ellipse cx="32" cy="42" rx="7" ry="10" fill="#bae6fd" transform="rotate(-35 32 42)" />
            <ellipse cx="88" cy="42" rx="7" ry="10" fill="#bae6fd" transform="rotate(35 88 42)" />
            <ellipse cx="60" cy="68" rx="20" ry="18" fill="#7dd3fc" />
            <ellipse cx="52" cy="62" rx="3" ry="4" fill="#0284c7" />
            <ellipse cx="68" cy="62" rx="3" ry="4" fill="#0284c7" />
            <path d="M56 72 Q60 76 64 72" stroke="#0284c7" stroke-width="2" fill="none" stroke-linecap="round" />
          </svg>
        </div>

        <h2 class="empty-title">{{ $t('doctorPet.noPetProfileYet') }}</h2>
        <p class="empty-desc">{{ $t('doctorPet.noPetProfileDesc') }}</p>

        <!-- 步骤引导 -->
        <div class="steps-guide">
          <div class="step-item">
            <div class="step-icon">
              <el-icon :size="24"><List /></el-icon>
            </div>
            <div class="step-num">1</div>
            <div class="step-text">{{ $t('doctorPet.step1') }}</div>
          </div>
          <div class="step-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
          <div class="step-item">
            <div class="step-icon">
              <el-icon :size="24"><FirstAidKit /></el-icon>
            </div>
            <div class="step-num">2</div>
            <div class="step-text">{{ $t('doctorPet.step2') }}</div>
          </div>
          <div class="step-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
          <div class="step-item">
            <div class="step-icon">
              <el-icon :size="24"><DocumentChecked /></el-icon>
            </div>
            <div class="step-num">3</div>
            <div class="step-text">{{ $t('doctorPet.step3') }}</div>
          </div>
        </div>

        <el-button type="primary" size="large" class="goto-btn" @click="goToAcceptList">
          <el-icon><ArrowRight /></el-icon>
          {{ $t('doctorPet.goToReceptionList') }}
        </el-button>
      </div>
    </div>

    <!-- 已选择宠物：档案详情 -->
    <template v-else>
      <!-- 顶部操作栏 -->
      <div class="page-header">
        <div></div>
        <div class="header-actions">
          <el-button type="primary" @click="handleCreateRecord">
            <el-icon><DocumentAdd /></el-icon>
            {{ $t('doctorPet.createMedicalRecord') }}
          </el-button>
          <el-button type="success" @click="handleCreatePrescription">
            <el-icon><FirstAidKit /></el-icon>
            {{ $t('doctorPet.writePrescription') }}
          </el-button>
          <el-button @click="handleViewHistory">
            <el-icon><Clock /></el-icon>
            {{ $t('doctorPet.history') }}
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
                {{ petInfo.status === 1 ? $t('doctorPet.healthy') : $t('doctorPet.underObservation') }}
              </div>
            </div>
            <div class="pet-basic">
              <h2 class="pet-name">{{ petInfo.name || $t('doctorPet.unnamed') }}</h2>
              <div class="pet-tags">
                <el-tag type="primary" effect="light" round>{{ petInfo.type || $t('doctorPet.unknown') }}</el-tag>
                <el-tag type="warning" effect="light" round>{{ petInfo.breed || $t('doctorPet.unknownBreed') }}</el-tag>
                <el-tag type="success" effect="light" round>{{ petInfo.gender === 1 ? $t('doctorPet.male') : $t('doctorPet.female') }}</el-tag>
              </div>
            </div>
          </div>

          <div class="pet-quick-stats">
            <div class="quick-stat">
              <div class="label">{{ $t('doctorPet.age') }}</div>
              <div class="value">{{ petInfo.age || 0 }}<span class="unit">{{ $t('doctorPet.yr') }}</span></div>
            </div>
            <div class="quick-stat">
              <div class="label">{{ $t('doctorPet.weight') }}</div>
              <div class="value">{{ petInfo.weight || 0 }}<span class="unit">kg</span></div>
            </div>
            <div class="quick-stat">
              <div class="label">{{ $t('doctorPet.coatColor') }}</div>
              <div class="value color-value">
                <span class="color-dot" :style="{ background: petInfo.color || '#D4A574' }"></span>
                {{ petInfo.color || $t('doctorPet.golden') }}
              </div>
            </div>
            <div class="quick-stat">
              <div class="label">{{ $t('doctorPet.chipNo') }}</div>
              <div class="value chip">{{ petInfo.chipNumber || $t('doctorPet.notImplanted') }}</div>
            </div>
          </div>
        </div>

        <!-- 主人信息卡片 -->
        <div class="owner-card">
          <div class="card-title">
            <el-icon><User /></el-icon>
            {{ $t('doctorPet.ownerInfo') }}
          </div>
          <div class="owner-content">
            <div class="owner-header">
              <el-avatar :size="48" :src="petInfo.ownerAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="owner-meta">
                <div class="name">{{ petInfo.ownerName || $t('doctorPet.unknown') }}</div>
                <div class="phone">
                  <el-icon><Phone /></el-icon>
                  {{ petInfo.ownerPhone || $t('doctorPet.notProvided') }}
                </div>
              </div>
            </div>
            <div class="owner-address" v-if="petInfo.ownerAddress">
              <el-icon><Location /></el-icon>
              {{ petInfo.ownerAddress }}
            </div>
            <div class="owner-address" v-else>
              <el-icon><Location /></el-icon>
              {{ $t('doctorPet.noAddressProvided') }}
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
              <span>{{ $t('doctorPet.healthInfo') }}</span>
            </div>
          </template>
          <div class="detail-list">
            <div class="detail-item">
              <span class="label">{{ $t('doctorPet.allergyHistory') }}</span>
              <span class="value">
                <el-tag v-if="petInfo.allergies || petInfo.allergy" type="danger" effect="light">
                  <el-icon><Warning /></el-icon>
                  {{ petInfo.allergies || petInfo.allergy }}
                </el-tag>
                <span v-else class="text-muted">{{ $t('doctorPet.noAllergyRecords') }}</span>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">{{ $t('doctorPet.vaccineStatus') }}</span>
              <span class="value">
                <el-tag type="success" effect="light">{{ petInfo.vaccineStatus || $t('doctorPet.vaccineStatus') }}</el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">{{ $t('doctorPet.neuterStatus') }}</span>
              <span class="value">{{ petInfo.neutered ? $t('doctorPet.neutered') : $t('doctorPet.notNeutered') }}</span>
            </div>
            <div class="detail-item">
              <span class="label">{{ $t('doctorPet.lastCheckup') }}</span>
              <span class="value">{{ petInfo.lastCheckup || $t('doctorPet.noRecords') }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="detail-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ $t('doctorPet.basicInfo') }}</span>
            </div>
          </template>
          <div class="detail-list">
            <div class="detail-item">
              <span class="label">{{ $t('doctorPet.dateOfBirth') }}</span>
              <span class="value">{{ petInfo.birthday || $t('doctorPet.unknown') }}</span>
            </div>
            <div class="detail-item">
              <span class="label">{{ $t('doctorPet.registrationTime') }}</span>
              <span class="value">{{ petInfo.createTime || $t('doctorPet.unknown') }}</span>
            </div>
            <div class="detail-item">
              <span class="label">{{ $t('doctorPet.source') }}</span>
              <span class="value">{{ petInfo.source || $t('doctorPet.outpatientRegistration') }}</span>
            </div>
            <div class="detail-item">
              <span class="label">{{ $t('doctorPet.remarks') }}</span>
              <span class="value text-wrap">{{ petInfo.remark || $t('doctorPet.noSpecialRemarks') }}</span>
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
              <span>{{ $t('doctorPet.healthRecordTimeline') }}</span>
            </div>
            <el-radio-group v-model="timelineFilter" size="small">
              <el-radio-button label="all">{{ $t('doctorPet.all') }}</el-radio-button>
              <el-radio-button label="medical">{{ $t('doctorPet.medicalRecord') }}</el-radio-button>
              <el-radio-button label="vaccine">{{ $t('doctorPet.vaccine') }}</el-radio-button>
              <el-radio-button label="exam">{{ $t('doctorPet.exam') }}</el-radio-button>
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
                  <span class="doctor">{{ record.doctor }}</span>
                  <el-button v-if="record.hasDetail" type="primary" link size="small" @click.stop="viewRecordDetail(record)">
                    {{ $t('doctorPet.viewDetails') }} <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>

          <el-empty v-else :description="$t('doctorPet.noHealthRecords')">
            <el-button type="primary" @click="handleCreateRecord">{{ $t('doctorPet.createFirstRecord') }}</el-button>
          </el-empty>
        </div>
      </el-card>
    </template>


  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import {
  DocumentAdd, FirstAidKit, Clock, User,
  UserFilled, Phone, Location, Warning, InfoFilled,
  Calendar, ArrowRight, List, DocumentChecked
} from '@element-plus/icons-vue'
import { acceptModule, recordModule } from '@/api/doctor/doctor'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const petId = ref(route.query.petId)
const registerId = ref(route.query.registerId)
const timelineFilter = ref('all')

// 是否有宠物ID
const hasPetId = computed(() => !!petId.value)

// 宠物信息
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
  avatar: '',
  ownerName: '',
  ownerPhone: '',
  ownerAddress: '',
  ownerAvatar: '',
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

// 跳转到接诊列表
const goToAcceptList = () => {
  router.push('/doctor/accept')
}

// 获取宠物详情
const fetchPetDetail = async () => {
  if (!petId.value) {
    // 未选择宠物，不加载模拟数据，显示引导页面
    return
  }

  loading.value = true
  try {
    const res = await acceptModule.getPetDetail(petId.value)
    if (res.data) {
      petInfo.value = res.data
    }
    await fetchHealthRecords()
  } catch (error) {
    console.error(t('doctorPet.failedToGetPetInfo'), error)
    loadMockData()
  } finally {
    loading.value = false
  }
}

// 获取健康记录（病历列表）
const fetchHealthRecords = async () => {
  try {
    const res = await recordModule.getMedicalRecordList({
      petId: petId.value,
      pageNum: 1,
      pageSize: 50
    })
    if (res.code === 200 && res.data) {
      // 适配多种返回结构：res.data.list / res.data.data / res.data
      const list = res.data.list || res.data.data || res.data.records || res.data
      if (Array.isArray(list)) {
        healthRecords.value = list.map(record => ({
          title: record.diagnosis || record.title || t('doctorPet.medicalRecord'),
          content: record.chiefComplaint || record.symptoms || record.content || '',
          time: record.createTime || record.visitTime || '',
          type: getRecordType(record.type),
          doctor: record.doctorName || t('doctorPet.unknown'),
          hasDetail: true,
          recordId: record.id || record.recordId
        }))
      }
    }
  } catch (error) {
    console.error(t('doctorPet.failedToGetPetInfo'), error)
    loadMockHealthRecords()
  }
}

// 将后端病历类型映射到时间轴类型
const getRecordType = (type) => {
  const typeMap = {
    '门诊': 'danger',
    '疫苗': 'success',
    '体检': 'primary',
    '手术': 'warning',
    '治疗': 'warning',
    '复诊': 'danger',
    '初诊': 'danger',
    'medical': 'danger',
    'vaccine': 'success',
    'exam': 'primary',
    'surgery': 'warning',
    'treatment': 'warning'
  }
  return typeMap[type] || 'danger'
}

// 加载模拟数据
const loadMockData = () => {
  petInfo.value = {
    petId: petId.value || '1',
    name: t('doctorPet.unnamed'),
    type: t('doctorPet.unknown'),
    breed: t('doctorPet.unknownBreed'),
    gender: 1,
    age: 3,
    birthday: '2023-04-15',
    weight: 28.5,
    color: '#D4A574',
    chipNumber: '900123456789012',
    allergy: t('doctorPet.noAllergyRecords'),
    vaccineStatus: t('doctorPet.vaccineStatus'),
    neutered: true,
    lastCheckup: '2026-02-20',
    remark: t('doctorPet.noSpecialRemarks'),
    status: 1,
    avatar: '',
    ownerName: t('doctorPet.unknown'),
    ownerPhone: '138****8888',
    ownerAddress: '',
    ownerAvatar: '',
    createTime: '2025-03-15',
    source: t('doctorPet.outpatientRegistration')
  }
  loadMockHealthRecords()
}

const loadMockHealthRecords = () => {
  healthRecords.value = [
    {
      title: t('doctorPet.medicalRecord'),
      content: t('doctorPet.noHealthRecords'),
      time: '2026-04-10 14:30',
      type: 'danger',
      doctor: t('doctorPet.unknown'),
      hasDetail: true
    },
    {
      title: t('doctorPet.vaccine'),
      content: t('doctorPet.noHealthRecords'),
      time: '2026-04-05 10:00',
      type: 'success',
      doctor: t('doctorPet.unknown'),
      hasDetail: true
    },
    {
      title: t('doctorPet.exam'),
      content: t('doctorPet.noHealthRecords'),
      time: '2026-02-20 09:30',
      type: 'primary',
      doctor: t('doctorPet.unknown'),
      hasDetail: true
    }
  ]
}

// 时间轴样式
const getTimelineColor = (type) => {
  const map = { danger: '#f56c6c', success: '#67c23a', primary: '#409eff', warning: '#e6a23c' }
  return map[type] || '#909399'
}

const getRecordTypeText = (type) => {
  const map = {
    danger: t('doctorPet.medicalRecord'),
    success: t('doctorPet.vaccine'),
    primary: t('doctorPet.exam'),
    warning: t('doctorPet.treatment')
  }
  return map[type] || t('doctorPet.other')
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
  const recordId = record.recordId || record.id
  if (!recordId) {
    ElMessage.warning(t('doctorPet.noRecordId'))
    return
  }
  router.push({
    path: '/doctor/record/detail',
    query: { recordId }
  })
}

onMounted(() => {
  fetchPetDetail()
})
</script>

<style scoped lang="scss">
.pet-page {
  min-height: 100%;

  // 空状态引导页
  .empty-state {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 600px;
    padding: 40px 20px;

    .empty-content {
      text-align: center;
      max-width: 600px;
      width: 100%;
    }

    .empty-icon {
      width: 160px;
      height: 160px;
      margin: 0 auto 30px;

      svg {
        width: 100%;
        height: 100%;
      }
    }

    .empty-title {
      font-size: 26px;
      font-weight: 600;
      color: #1e293b;
      margin-bottom: 12px;
    }

    .empty-desc {
      font-size: 15px;
      color: #64748b;
      margin-bottom: 40px;
      line-height: 1.6;
    }

    .steps-guide {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      margin-bottom: 40px;

      .step-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;
        padding: 20px 16px;
        background: white;
        border-radius: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        min-width: 120px;
        position: relative;

        .step-icon {
          width: 48px;
          height: 48px;
          border-radius: 14px;
          background: #eff6ff;
          color: #3b82f6;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .step-num {
          position: absolute;
          top: -8px;
          right: -8px;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #3b82f6;
          color: white;
          font-size: 12px;
          font-weight: 600;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .step-text {
          font-size: 13px;
          color: #475569;
          font-weight: 500;
        }
      }

      .step-arrow {
        color: #cbd5e1;
        font-size: 20px;
        flex-shrink: 0;
      }
    }

    .goto-btn {
      padding: 12px 36px;
      font-size: 16px;
      border-radius: 10px;

      .el-icon {
        margin-right: 6px;
      }
    }
  }

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
