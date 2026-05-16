<template>
  <div class="prescription-detail-page" v-loading="loading">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          <span>{{ $t('common.back') }}</span>
        </el-button>
        <div class="header-title">
          <div class="title-icon-wrap">
            <el-icon><Tickets /></el-icon>
          </div>
          <div class="title-text">
            <span class="main">{{ $t('prescription.prescriptionDetails') }}</span>
            <span class="sub">{{ detail.prescriptionNo || $t('prescription.noNumber') }}</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-tag :type="getStatusType(detail.status)" effect="light" round size="large">
          {{ detail.statusDesc || getStatusText(detail.status) }}
        </el-tag>
      </div>
    </div>

    <!-- 处方概要卡片 -->
    <div class="info-card summary-card">
      <div class="card-title">
        <div class="title-icon-wrap icon-summary">
          <el-icon><FirstAidKit /></el-icon>
        </div>
        <span>{{ $t('prescription.prescriptionSummary') }}</span>
      </div>
      <div class="info-grid">
        <div class="info-item">
          <div class="info-label">{{ $t('prescription.petName') }}</div>
          <div class="info-value highlight">{{ detail.petName || $t('common.unknown') }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('prescription.diagnosisResult') }}</div>
          <div class="info-value">{{ detail.diagnosis || $t('prescription.none') }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('prescription.prescriptionAmount') }}</div>
          <div class="info-value amount">¥{{ (detail.totalAmount || 0).toFixed(2) }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('prescription.createTime') }}</div>
          <div class="info-value">{{ detail.createTime || $t('common.unknown') }}</div>
        </div>
      </div>
    </div>

    <!-- 药品明细 -->
    <el-card class="detail-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-icon-wrap icon-drug">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <span>{{ $t('prescription.drugDetails') }}</span>
          <el-tag type="primary" effect="light" round size="small" class="count-tag">
            {{ $t('prescription.itemCount', { count: (detail.drugs || []).length }) }}
          </el-tag>
        </div>
      </template>

      <el-table :data="detail.drugs || []" border class="detail-table" v-if="(detail.drugs || []).length > 0">
        <el-table-column type="index" :label="$t('prescription.serialNo')" width="60" align="center" />
        <el-table-column prop="drugName" :label="$t('prescription.drugName')" min-width="160" />
        <el-table-column prop="specification" :label="$t('prescription.specification')" width="120" />
        <el-table-column prop="quantity" :label="$t('prescription.quantity')" width="80" align="center" />
        <el-table-column prop="dosage" :label="$t('prescription.dosage')" width="100" />
        <el-table-column prop="usage" :label="$t('prescription.usage')" width="100">
          <template #default="{ row }">{{ translateUsage(row.usage) }}</template>
        </el-table-column>
        <el-table-column prop="frequency" :label="$t('prescription.frequency')" width="100">
          <template #default="{ row }">{{ translateFrequency(row.frequency) }}</template>
        </el-table-column>
        <el-table-column prop="days" :label="$t('prescription.days')" width="80" align="center" />
        <el-table-column prop="price" :label="$t('prescription.unitPrice')" width="100" align="right">
          <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="amount" :label="$t('prescription.amount')" width="100" align="right">
          <template #default="{ row }">¥{{ (row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>

      <el-empty v-else :description="$t('prescription.noDrugDetails')" :image-size="80">
        <template #image>
          <el-icon :size="48" color="#cbd5e1"><FirstAidKit /></el-icon>
        </template>
      </el-empty>
    </el-card>

    <!-- 服务明细 -->
    <el-card class="detail-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-icon-wrap icon-service">
            <el-icon><Service /></el-icon>
          </div>
          <span>{{ $t('prescription.serviceDetails') }}</span>
          <el-tag type="success" effect="light" round size="small" class="count-tag">
            {{ $t('prescription.itemCount', { count: (detail.services || []).length }) }}
          </el-tag>
        </div>
      </template>

      <el-table :data="detail.services || []" border class="detail-table" v-if="(detail.services || []).length > 0">
        <el-table-column type="index" :label="$t('prescription.serialNo')" width="60" align="center" />
        <el-table-column prop="serviceName" :label="$t('prescription.serviceItem')" min-width="200" />
        <el-table-column prop="quantity" :label="$t('prescription.quantity')" width="80" align="center" />
        <el-table-column prop="price" :label="$t('prescription.unitPrice')" width="120" align="right">
          <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="amount" :label="$t('prescription.amount')" width="120" align="right">
          <template #default="{ row }">¥{{ (row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>

      <el-empty v-else :description="$t('prescription.noServiceDetails')" :image-size="80">
        <template #image>
          <el-icon :size="48" color="#cbd5e1"><Service /></el-icon>
        </template>
      </el-empty>
    </el-card>

    <!-- 费用合计 -->
    <div class="total-bar">
      <div class="total-item">
        <span class="label">{{ $t('prescription.drugTotal') }}</span>
        <span class="value">¥{{ drugTotal.toFixed(2) }}</span>
      </div>
      <div class="total-divider">|</div>
      <div class="total-item">
        <span class="label">{{ $t('prescription.serviceTotal') }}</span>
        <span class="value">¥{{ serviceTotal.toFixed(2) }}</span>
      </div>
      <div class="total-divider">|</div>
      <div class="total-item grand">
        <span class="label">{{ $t('prescription.totalAmountLabel') }}</span>
        <span class="value">¥{{ (detail.totalAmount || 0).toFixed(2) }}</span>
      </div>
    </div>

    <!-- 备注 -->
    <el-card class="remark-card" shadow="hover" v-if="detail.remark">
      <template #header>
        <div class="card-header">
          <div class="header-icon-wrap icon-remark">
            <el-icon><Memo /></el-icon>
          </div>
          <span>{{ $t('prescription.remark') }}</span>
        </div>
      </template>
      <div class="remark-content">{{ detail.remark }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Tickets, FirstAidKit, Service, Memo
} from '@element-plus/icons-vue'
import { prescriptionModule, acceptModule } from '@/api/doctor/doctor'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const detail = ref({})

const drugTotal = computed(() => {
  return (detail.value.drugs || []).reduce((sum, item) => sum + (item.amount || 0), 0)
})

const serviceTotal = computed(() => {
  return (detail.value.services || []).reduce((sum, item) => sum + (item.amount || 0), 0)
})

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: t('prescription.statusDraft'), 1: t('prescription.statusSubmitted'), 2: t('prescription.statusCancelled') }
  return map[status] || t('prescription.statusUnknown')
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

const fetchDetail = async () => {
  const prescriptionId = route.query.prescriptionId || route.params.prescriptionId
  if (!prescriptionId) {
    ElMessage.error(t('prescription.missingPrescriptionId'))
    return
  }

  loading.value = true
  try {
    const res = await prescriptionModule.getPrescriptionDetail(prescriptionId)
    console.log('处方详情API返回:', res)

    if (res.code === 200 && res.data) {
      // 兼容多种返回结构
      let data = res.data
      if (res.data.data && typeof res.data.data === 'object' && !Array.isArray(res.data.data)) {
        data = res.data.data
      } else if (res.data.prescription && typeof res.data.prescription === 'object') {
        data = res.data.prescription
      }

      console.log('原始data对象键:', Object.keys(data))
      console.log('原始data完整内容:', JSON.parse(JSON.stringify(data)))
      console.log('原始data.drugs:', data.drugs)
      console.log('原始data.drugList:', data.drugList)
      console.log('原始data.prescriptionDrugs:', data.prescriptionDrugs)
      console.log('原始data.services:', data.services)
      console.log('原始data.serviceList:', data.serviceList)
      console.log('原始data.prescriptionServices:', data.prescriptionServices)

      // 提取嵌套宠物信息
      const pet = data.pet || data.petInfo || {}
      // 提取嵌套病历/诊断信息
      const record = data.record || data.medicalRecord || data.medical_record || {}

      // 从URL query获取列表页传递的兜底数据
      const q = route.query

      // 字段映射（兼容可能的命名差异及嵌套对象，query参数作为兜底）
      detail.value = {
        prescriptionId: data.prescriptionId || data.id || data.prescription_id || prescriptionId,
        prescriptionNo: data.prescriptionNo || data.prescription_no || data.no || data.prescriptionNumber || q.prescriptionNo || '',
        petName: data.petName || data.pet_name || pet.name || pet.petName || pet.pet_name || q.petName || '',
        diagnosis: data.diagnosis || record.diagnosis || record.diagnosis_result || q.diagnosis || '',
        totalAmount: data.totalAmount || data.total_amount || data.amount || data.totalPrice || Number(q.totalAmount) || 0,
        status: data.status ?? data.statusCode ?? data.state ?? Number(q.status) ?? 0,
        statusDesc: data.statusDesc || data.status_desc || data.statusText || data.stateText || q.statusDesc || getStatusText(data.status ?? Number(q.status) ?? 0),
        createTime: data.createTime || data.create_time || data.createdAt || data.created_at || q.createTime || '',
        remark: data.remark || data.note || data.notes || '',
        drugs: Array.isArray(data.drugs) ? data.drugs :
               Array.isArray(data.drugList) ? data.drugList :
               Array.isArray(data.prescriptionDrugs) ? data.prescriptionDrugs :
               Array.isArray(data.drugItems) ? data.drugItems : [],
        services: Array.isArray(data.services) ? data.services :
                  Array.isArray(data.serviceList) ? data.serviceList :
                  Array.isArray(data.prescriptionServices) ? data.prescriptionServices : []
      }

      console.log('解析后的处方详情:', JSON.parse(JSON.stringify(detail.value)))

      // 如果宠物信息为空，但通过 petId 可以获取，则补充宠物信息
      const rawPetId = data.petId || data.pet_id || pet.petId || pet.pet_id
      if (rawPetId && !detail.value.petName) {
        try {
          const petRes = await acceptModule.getPetDetail(rawPetId)
          console.log('补充查询宠物详情:', petRes)
          if (petRes.code === 200 && petRes.data) {
            const pd = petRes.data
            detail.value.petName = detail.value.petName || pd.name || pd.petName || ''
          }
        } catch (e) {
          console.warn('补充查询宠物详情失败:', e)
        }
      }
    } else {
      ElMessage.error(res.msg || t('prescription.failedGetPrescriptionDetails'))
    }
  } catch (error) {
    console.error('获取处方详情失败:', error)
    ElMessage.error(t('prescription.failedGetPrescriptionDetails'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped lang="scss">
.prescription-detail-page {
  --primary-blue: #3b82f6;
  --primary-green: #10b981;
  --primary-orange: #f59e0b;
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --text-muted: #94a3b8;
  --bg-subtle: #f8fafc;
  --border-light: #e2e8f0;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 20px;

      .back-btn {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: 10px;
        border: 1px solid #e2e8f0;
        background: white;
        color: #475569;
        font-weight: 500;
        transition: all 0.2s;

        &:hover {
          background: #f8fafc;
          border-color: #cbd5e1;
          color: #1e293b;
        }

        .el-icon {
          font-size: 16px;
        }
      }

      .header-title {
        display: flex;
        align-items: center;
        gap: 12px;

        .title-icon-wrap {
          width: 40px;
          height: 40px;
          border-radius: 12px;
          background: linear-gradient(135deg, #10b981, #059669);
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 12px -2px rgba(16, 185, 129, 0.25);

          .el-icon {
            color: white;
            font-size: 20px;
          }
        }

        .title-text {
          display: flex;
          flex-direction: column;

          .main {
            font-size: 20px;
            font-weight: 700;
            color: var(--text-primary);
          }

          .sub {
            font-size: 13px;
            color: var(--text-muted);
            font-family: monospace;
            margin-top: 2px;
          }
        }
      }
    }

    .header-actions {
      .el-tag {
        font-size: 14px;
        padding: 6px 16px;
        font-weight: 600;
      }
    }
  }

  .info-card {
    background: white;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.06);
    border: 1px solid rgba(226, 232, 240, 0.6);
    margin-bottom: 20px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 12px 32px -8px rgba(0, 0, 0, 0.08);
    }

    .card-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 16px;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f1f5f9;

      .title-icon-wrap {
        width: 32px;
        height: 32px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;

        .el-icon {
          color: white;
          font-size: 16px;
        }

        &.icon-summary {
          background: linear-gradient(135deg, #10b981, #059669);
        }
      }
    }
  }

  .summary-card {
    .info-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;

      .info-item {
        padding: 16px;
        background: #f8fafc;
        border-radius: 12px;
        border: 1px solid #f1f5f9;
        transition: all 0.2s;

        &:hover {
          background: white;
          box-shadow: 0 4px 12px -2px rgba(0, 0, 0, 0.06);
        }

        .info-label {
          font-size: 12px;
          color: var(--text-muted);
          margin-bottom: 8px;
          font-weight: 500;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .info-value {
          font-size: 15px;
          color: var(--text-primary);
          font-weight: 600;

          &.highlight {
            color: var(--primary-blue);
            font-size: 17px;
          }

          &.amount {
            color: var(--primary-orange);
            font-size: 18px;
            font-weight: 700;
          }
        }
      }
    }
  }

  .detail-card {
    border-radius: 16px;
    margin-bottom: 20px;
    transition: all 0.3s ease;
    border: 1px solid rgba(226, 232, 240, 0.6);
    overflow: hidden;

    &:hover {
      box-shadow: 0 12px 32px -8px rgba(0, 0, 0, 0.08);
    }

    :deep(.el-card__header) {
      padding: 18px 24px;
      border-bottom: 1px solid #f1f5f9;
    }

    :deep(.el-card__body) {
      padding: 20px 24px;
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 700;
      font-size: 15px;
      color: var(--text-primary);

      .header-icon-wrap {
        width: 32px;
        height: 32px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;

        .el-icon {
          color: white;
          font-size: 16px;
        }

        &.icon-drug {
          background: linear-gradient(135deg, #3b82f6, #2563eb);
        }

        &.icon-service {
          background: linear-gradient(135deg, #8b5cf6, #7c3aed);
        }
      }

      .count-tag {
        margin-left: auto;
        font-weight: 600;
      }
    }

    .detail-table {
      :deep(.el-table__header) {
        th.el-table__cell {
          background: #f8fafc;
          color: #475569;
          font-weight: 600;
          font-size: 13px;
        }
      }

      :deep(.el-table__row) {
        td.el-table__cell {
          font-size: 13px;
          color: #334155;
        }
      }
    }
  }

  .total-bar {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 20px;
    padding: 20px 24px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.06);
    border: 1px solid rgba(226, 232, 240, 0.6);
    margin-bottom: 20px;

    .total-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .label {
        font-size: 13px;
        color: var(--text-muted);
        font-weight: 500;
      }

      .value {
        font-size: 15px;
        color: var(--text-primary);
        font-weight: 600;
      }

      &.grand {
        .label {
          font-size: 14px;
          color: var(--text-secondary);
          font-weight: 600;
        }

        .value {
          font-size: 22px;
          color: var(--primary-orange);
          font-weight: 700;
        }
      }
    }

    .total-divider {
      color: #e2e8f0;
      font-size: 14px;
    }
  }

  .remark-card {
    border-radius: 16px;
    transition: all 0.3s ease;
    border: 1px solid rgba(226, 232, 240, 0.6);
    overflow: hidden;

    &:hover {
      box-shadow: 0 12px 32px -8px rgba(0, 0, 0, 0.08);
    }

    :deep(.el-card__header) {
      padding: 18px 24px;
      border-bottom: 1px solid #f1f5f9;
      background: linear-gradient(135deg, rgba(100, 116, 139, 0.03), rgba(100, 116, 139, 0.01));
    }

    :deep(.el-card__body) {
      padding: 20px 24px;
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 700;
      font-size: 15px;
      color: var(--text-primary);

      .header-icon-wrap {
        width: 32px;
        height: 32px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;

        .el-icon {
          color: white;
          font-size: 16px;
        }

        &.icon-remark {
          background: linear-gradient(135deg, #64748b, #475569);
        }
      }
    }

    .remark-content {
      font-size: 14px;
      color: var(--text-secondary);
      line-height: 1.7;
    }
  }
}

@media (max-width: 1024px) {
  .prescription-detail-page {
    .summary-card .info-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}

@media (max-width: 768px) {
  .prescription-detail-page {
    .page-header {
      flex-direction: column;
      gap: 16px;
      align-items: flex-start;
    }

    .summary-card .info-grid {
      grid-template-columns: 1fr;
    }

    .total-bar {
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;

      .total-divider {
        display: none;
      }
    }
  }
}
</style>
