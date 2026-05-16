<template>
  <div class="record-detail-page" v-loading="loading">
    <!-- 顶部返回栏 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="back-btn" @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          <span>{{ $t('common.back') }}</span>
        </el-button>
        <div class="header-title">
          <div class="title-icon-wrap">
            <el-icon><Document /></el-icon>
          </div>
          <div class="title-text">
            <span class="main">{{ $t('record.recordDetail') }}</span>
            <span class="sub">{{ detail.recordNo || $t('record.noNumber') }}</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-tag :type="detail.status === 1 ? 'success' : 'warning'" effect="light" round size="large">
          {{ detail.status === 1 ? $t('record.statusCompleted') : $t('record.statusDraft') }}
        </el-tag>
      </div>
    </div>

    <!-- 宠物信息卡片 -->
    <div class="info-card pet-summary-card">
      <div class="card-title">
        <div class="title-icon-wrap icon-pet">
          <el-icon><FirstAidKit /></el-icon>
        </div>
        <span>{{ $t('record.petAndVisitInfo') }}</span>
      </div>
      <div class="info-grid">
        <div class="info-item">
          <div class="info-label">{{ $t('record.petName') }}</div>
          <div class="info-value highlight">{{ detail.petName || $t('common.unknown') }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('record.petType') }}</div>
          <div class="info-value">{{ detail.petType || $t('common.unknown') }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('record.doctor') }}</div>
          <div class="info-value">
            <el-icon><User /></el-icon>
            {{ detail.doctorName || $t('common.unknown') }}
          </div>
        </div>
        <div class="info-item">
          <div class="info-label">{{ $t('record.createTime') }}</div>
          <div class="info-value">{{ detail.createTime || $t('common.unknown') }}</div>
        </div>
      </div>
    </div>

    <!-- 病历内容区域 -->
    <div class="detail-grid">
      <!-- Chief Complaint与症状 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-icon-wrap icon-symptom">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <span>{{ $t('record.chiefComplaintAndSymptoms') }}</span>
          </div>
        </template>
        <div class="detail-section">
          <div class="section-row">
            <div class="row-label">{{ $t('record.chiefComplaint') }}</div>
            <div class="row-content">{{ detail.chiefComplaint || $t('record.noRecord') }}</div>
          </div>
          <div class="section-row">
            <div class="row-label">{{ $t('record.symptoms') }}</div>
            <div class="row-content">{{ detail.symptoms || $t('record.noRecord') }}</div>
          </div>
        </div>
      </el-card>

      <!-- 病史 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-icon-wrap icon-history">
              <el-icon><Clock /></el-icon>
            </div>
            <span>{{ $t('record.medicalHistory') }}</span>
          </div>
        </template>
        <div class="detail-section">
          <div class="section-row">
            <div class="row-label">{{ $t('record.presentIllness') }}</div>
            <div class="row-content">{{ detail.presentIllness || $t('record.noRecord') }}</div>
          </div>
          <div class="section-row">
            <div class="row-label">{{ $t('record.pastHistory') }}</div>
            <div class="row-content">{{ detail.pastHistory || $t('record.noRecord') }}</div>
          </div>
        </div>
      </el-card>

      <!-- 检查 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-icon-wrap icon-exam">
              <el-icon><Search /></el-icon>
            </div>
            <span>{{ $t('record.examinationInfo') }}</span>
          </div>
        </template>
        <div class="detail-section">
          <div class="section-row">
            <div class="row-label">{{ $t('record.physicalExam') }}</div>
            <div class="row-content">{{ detail.physicalExam || $t('record.noRecord') }}</div>
          </div>
          <div class="section-row">
            <div class="row-label">{{ $t('record.auxiliaryExam') }}</div>
            <div class="row-content">{{ detail.auxiliaryExam || $t('record.noRecord') }}</div>
          </div>
        </div>
      </el-card>

      <!-- 诊断与治疗 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-icon-wrap icon-diagnosis">
              <el-icon><FirstAidKit /></el-icon>
            </div>
            <span>{{ $t('record.diagnosisTreatment') }}</span>
          </div>
        </template>
        <div class="detail-section">
          <div class="section-row">
            <div class="row-label">{{ $t('record.diagnosis') }}</div>
            <div class="row-content highlight">{{ detail.diagnosis || $t('record.noRecord') }}</div>
          </div>
          <div class="section-row">
            <div class="row-label">{{ $t('record.treatmentPlan') }}</div>
            <div class="row-content">{{ detail.treatmentPlan || $t('record.noRecord') }}</div>
          </div>
          <div class="section-row">
            <div class="row-label">{{ $t('record.doctorsAdvice') }}</div>
            <div class="row-content">{{ detail.doctorAdvice || $t('record.noRecord') }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 备注 -->
    <el-card class="remark-card" shadow="hover" v-if="detail.remark">
      <template #header>
        <div class="card-header">
          <div class="header-icon-wrap icon-remark">
            <el-icon><Memo /></el-icon>
          </div>
          <span>{{ $t('record.remarks') }}</span>
        </div>
      </template>
      <div class="remark-content">{{ detail.remark }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Document, FirstAidKit, User,
  ChatDotRound, Clock, Search, Memo
} from '@element-plus/icons-vue'
import { recordModule, acceptModule } from '@/api/doctor/doctor'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const detail = ref({})

const fetchDetail = async () => {
  const recordId = route.query.recordId || route.params.recordId
  if (!recordId) {
    ElMessage.error(t('record.missingRecordId'))
    return
  }

  loading.value = true
  try {
    const res = await recordModule.getMedicalRecordDetail(recordId)
    console.log('病历详情API返回:', res)

    if (res.code === 200 && res.data) {
      // 兼容多种返回结构
      let data = res.data
      if (res.data.data && typeof res.data.data === 'object' && !Array.isArray(res.data.data)) {
        data = res.data.data
      } else if (res.data.record && typeof res.data.record === 'object') {
        data = res.data.record
      }

      console.log('原始data对象键:', Object.keys(data))
      console.log('原始data完整内容:', JSON.parse(JSON.stringify(data)))

      // 提取嵌套宠物信息
      const pet = data.pet || data.petInfo || {}
      // 提取嵌套医生信息
      const doctor = data.doctor || data.doctorInfo || {}

      // 从URL query获取列表页传递的兜底数据
      const q = route.query

      // 字段映射（兼容可能的命名差异及嵌套对象，query参数作为兜底）
      detail.value = {
        recordId: data.recordId || data.id || data.record_id || recordId,
        recordNo: data.recordNo || data.record_no || data.no || data.recordNumber || q.recordNo || '',
        petName: data.petName || data.pet_name || pet.name || pet.petName || pet.pet_name || q.petName || '',
        petType: data.petType || data.pet_type || data.type || pet.type || pet.petType || q.petType || '',
        doctorName: data.doctorName || data.doctor_name || data.doctor || doctor.name || doctor.realName || doctor.doctorName || q.doctorName || '',
        status: data.status ?? data.statusCode ?? data.state ?? Number(q.status) ?? 0,
        createTime: data.createTime || data.create_time || data.createdAt || data.created_at || q.createTime || '',
        chiefComplaint: data.chiefComplaint || data.chief_complaint || data.chiefComplaintText || '',
        symptoms: data.symptoms || data.symptom || '',
        presentIllness: data.presentIllness || data.present_illness || '',
        pastHistory: data.pastHistory || data.past_history || '',
        physicalExam: data.physicalExam || data.physical_exam || data.physicalExamination || '',
        auxiliaryExam: data.auxiliaryExam || data.auxiliary_exam || data.auxiliaryExamination || '',
        diagnosis: data.diagnosis || data.diagnosisResult || data.diagnosis_result || q.diagnosis || '',
        treatmentPlan: data.treatmentPlan || data.treatment_plan || data.treatment || '',
        doctorAdvice: data.doctorAdvice || data.doctor_advice || data.advice || '',
        remark: data.remark || data.note || data.notes || ''
      }

      console.log('解析后的病历详情:', JSON.parse(JSON.stringify(detail.value)))

      // 如果宠物信息为空，但通过 petId 可以获取，则补充宠物信息
      const rawPetId = data.petId || data.pet_id || pet.petId || pet.pet_id
      if (rawPetId && (!detail.value.petName || !detail.value.petType)) {
        try {
          const petRes = await acceptModule.getPetDetail(rawPetId)
          console.log('补充查询宠物详情:', petRes)
          if (petRes.code === 200 && petRes.data) {
            const pd = petRes.data
            detail.value.petName = detail.value.petName || pd.name || pd.petName || ''
            detail.value.petType = detail.value.petType || pd.type || pd.petType || ''
          }
        } catch (e) {
          console.warn('补充查询宠物详情失败:', e)
        }
      }
    } else {
      ElMessage.error(res.msg || t('record.failedFetchRecordDetail'))
    }
  } catch (error) {
    console.error('获取病历详情失败:', error)
    ElMessage.error(t('record.failedFetchRecordDetail'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped lang="scss">
.record-detail-page {
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
          background: linear-gradient(135deg, #3b82f6, #2563eb);
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 4px 12px -2px rgba(59, 130, 246, 0.25);

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

        &.icon-pet {
          background: linear-gradient(135deg, #3b82f6, #2563eb);
        }
      }
    }
  }

  .pet-summary-card {
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
          display: flex;
          align-items: center;
          gap: 6px;

          .el-icon {
            color: var(--primary-blue);
            font-size: 14px;
          }

          &.highlight {
            color: var(--primary-blue);
            font-size: 17px;
          }
        }
      }
    }
  }

  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .detail-card {
      border-radius: 16px;
      transition: all 0.3s ease;
      border: 1px solid rgba(226, 232, 240, 0.6);
      overflow: hidden;

      &:hover {
        box-shadow: 0 12px 32px -8px rgba(0, 0, 0, 0.08);
        transform: translateY(-1px);
      }

      :deep(.el-card__header) {
        padding: 18px 24px;
        border-bottom: 1px solid #f1f5f9;
        background: linear-gradient(135deg, rgba(59, 130, 246, 0.03), rgba(59, 130, 246, 0.01));
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

          &.icon-symptom {
            background: linear-gradient(135deg, #f59e0b, #d97706);
          }

          &.icon-history {
            background: linear-gradient(135deg, #8b5cf6, #7c3aed);
          }

          &.icon-exam {
            background: linear-gradient(135deg, #06b6d4, #0891b2);
          }

          &.icon-diagnosis {
            background: linear-gradient(135deg, #ef4444, #dc2626);
          }
        }
      }

      .detail-section {
        .section-row {
          padding: 14px 0;
          border-bottom: 1px solid #f8fafc;

          &:last-child {
            border-bottom: none;
            padding-bottom: 0;
          }

          &:first-child {
            padding-top: 0;
          }

          .row-label {
            font-size: 12px;
            color: var(--text-muted);
            font-weight: 600;
            margin-bottom: 6px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
          }

          .row-content {
            font-size: 14px;
            color: var(--text-secondary);
            line-height: 1.7;

            &.highlight {
              color: var(--text-primary);
              font-weight: 600;
              font-size: 15px;
            }
          }
        }
      }
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
  .record-detail-page {
    .pet-summary-card .info-grid {
      grid-template-columns: repeat(2, 1fr);
    }

    .detail-grid {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 640px) {
  .record-detail-page {
    .page-header {
      flex-direction: column;
      gap: 16px;
      align-items: flex-start;
    }

    .pet-summary-card .info-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
