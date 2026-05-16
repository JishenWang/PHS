<template>
  <div class="system-config">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('system.title') }}</h2>
      <p class="subtitle">{{ $t('system.subtitle') }}</p>
    </div>

    <!-- 配置标签页 -->
    <el-tabs v-model="activeTab" type="border-card" class="config-tabs">
      <!-- 基础配置 -->
      <el-tab-pane :label="$t('system.basicConfig')" name="basic" key="basic">
        <el-form 
          :model="basicForm" 
          :rules="basicRules" 
          ref="basicFormRef"
          label-width="140px"
          class="config-form"
        >
          <el-divider content-position="left">{{ $t('system.organizationInfo') }}</el-divider>
          
          <el-form-item :label="$t('system.hospitalName')" prop="hospitalName">
            <el-input v-model="basicForm.hospitalName" :placeholder="$t('system.hospitalNamePlaceholder')" />
          </el-form-item>
          
          <el-form-item :label="$t('system.contactPhone')" prop="contactPhone">
            <el-input v-model="basicForm.contactPhone" :placeholder="$t('system.contactPhonePlaceholder')" />
          </el-form-item>
          
          <el-form-item :label="$t('system.address')" prop="address">
            <el-input 
              v-model="basicForm.address" 
              type="textarea" 
              :rows="2" 
              :placeholder="$t('system.addressPlaceholder')"
            />
          </el-form-item>

          <el-divider content-position="left">{{ $t('system.systemParameters') }}</el-divider>
          
          <el-form-item :label="$t('system.maxAdvanceBookingDays')" prop="maxAdvanceDays">
            <el-slider v-model="basicForm.maxAdvanceDays" :max="30" show-stops />
            <span class="form-hint">{{ $t('system.canBookUpToDays', { days: basicForm.maxAdvanceDays }) }}</span>
          </el-form-item>
          
          <el-form-item :label="$t('system.dailyBookingLimit')" prop="dailyLimit">
            <el-input-number v-model="basicForm.dailyLimit" :min="10" :max="200" />
          </el-form-item>
          
          <el-form-item :label="$t('system.systemAnnouncement')" prop="announcement">
            <el-input 
              v-model="basicForm.announcement" 
              type="textarea" 
              :rows="3" 
              :placeholder="$t('system.announcementPlaceholder')"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveBasicConfig" :loading="saving.basic">
              <el-icon><Check /></el-icon> {{ $t('system.saveBasicConfig') }}
            </el-button>
            <el-button @click="resetBasicForm">{{ $t('system.reset') }}</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 业务配置 -->
      <el-tab-pane :label="$t('system.businessConfig')" name="business" key="business">
        <el-form 
          :model="businessForm" 
          :rules="businessRules" 
          ref="businessFormRef"
          label-width="160px"
          class="config-form"
        >
          <el-divider content-position="left">{{ $t('system.registrationFees') }}</el-divider>
          
          <el-form-item :label="$t('system.normalRegistrationFee')" prop="normalFee">
            <el-input-number v-model="businessForm.normalFee" :min="0" :precision="2" :step="5" />
          </el-form-item>
          
          <el-form-item :label="$t('system.expertRegistrationFee')" prop="expertFee">
            <el-input-number v-model="businessForm.expertFee" :min="0" :precision="2" :step="10" />
          </el-form-item>

          <el-divider content-position="left">{{ $t('system.businessHours') }}</el-divider>
          
          <el-form-item :label="$t('system.morningBusinessHours')" prop="morningTime">
            <el-time-picker
              v-model="businessForm.morningTime"
              is-range
              range-separator="-"
              :start-placeholder="$t('system.startTime')"
              :end-placeholder="$t('system.endTime')"
              format="HH:mm"
            />
          </el-form-item>
          
          <el-form-item :label="$t('system.afternoonBusinessHours')" prop="afternoonTime">
            <el-time-picker
              v-model="businessForm.afternoonTime"
              is-range
              range-separator="-"
              :start-placeholder="$t('system.startTime')"
              :end-placeholder="$t('system.endTime')"
              format="HH:mm"
            />
          </el-form-item>

          <el-divider content-position="left">{{ $t('system.consultationRules') }}</el-divider>
          
          <el-form-item :label="$t('system.consultationDuration')" prop="consultDuration">
            <el-radio-group v-model="businessForm.consultDuration">
              <el-radio-button :value="15">{{ $t('system.minutes15') }}</el-radio-button>
              <el-radio-button :value="20">{{ $t('system.minutes20') }}</el-radio-button>
              <el-radio-button :value="30">{{ $t('system.minutes30') }}</el-radio-button>
              <el-radio-button :value="45">{{ $t('system.minutes45') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item :label="$t('system.refundDeadline')" prop="refundDeadline">
            <el-select v-model="businessForm.refundDeadline">
              <el-option :label="$t('system.hours2BeforeVisit')" :value="2" />
              <el-option :label="$t('system.hours4BeforeVisit')" :value="4" />
              <el-option :label="$t('system.hours8BeforeVisit')" :value="8" />
              <el-option :label="$t('system.hours24BeforeVisit')" :value="24" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveBusinessConfig" :loading="saving.business">
              <el-icon><Check /></el-icon> {{ $t('system.saveBusinessConfig') }}
            </el-button>
            <el-button @click="resetBusinessForm">{{ $t('system.reset') }}</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 通知配置 -->
      <el-tab-pane :label="$t('system.notificationConfig')" name="notification" key="notification">
        <el-form 
          :model="notifyForm" 
          ref="notifyFormRef"
          label-width="160px"
          class="config-form"
        >
          <el-divider content-position="left">{{ $t('system.smsNotification') }}</el-divider>
          
          <el-form-item :label="$t('system.bookingSuccessNotification')">
            <el-switch v-model="notifyForm.smsReserveSuccess" :active-text="$t('system.on')" :inactive-text="$t('system.off')" />
          </el-form-item>
          
          <el-form-item :label="$t('system.preVisitReminder')">
            <el-switch v-model="notifyForm.smsRemind" :active-text="$t('system.on')" :inactive-text="$t('system.off')" />
          </el-form-item>
          
          <el-form-item :label="$t('system.reminderLeadTime')" v-if="notifyForm.smsRemind">
            <el-select v-model="notifyForm.remindTime">
              <el-option :label="$t('system.minutes30Ahead')" :value="30" />
              <el-option :label="$t('system.hour1Ahead')" :value="60" />
              <el-option :label="$t('system.hours2Ahead')" :value="120" />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">{{ $t('system.emailNotification') }}</el-divider>
          
          <el-form-item :label="$t('system.systemExceptionAlert')">
            <el-switch v-model="notifyForm.emailErrorAlert" :active-text="$t('system.on')" :inactive-text="$t('system.off')" />
          </el-form-item>
          
          <el-form-item :label="$t('system.receivingEmail')" v-if="notifyForm.emailErrorAlert">
            <el-input v-model="notifyForm.alertEmail" :placeholder="$t('system.emailPlaceholder')" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveNotifyConfig" :loading="saving.notification">
              <el-icon><Check /></el-icon> {{ $t('system.saveNotificationConfig') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 数据维护 -->
      <el-tab-pane :label="$t('system.dataMaintenance')" name="maintenance" key="maintenance">
        <div class="maintenance-section">
          <el-divider content-position="left">{{ $t('system.dataBackup') }}</el-divider>
          
          <div class="maint-item">
            <div class="maint-info">
              <h4>{{ $t('system.manualBackup') }}</h4>
              <p>{{ $t('system.manualBackupDesc') }}</p>
            </div>
            <el-button type="primary" @click="handleBackup" :loading="loading.backup">
              <el-icon><Download /></el-icon> {{ $t('system.backupNow') }}
            </el-button>
          </div>
          
          <div class="maint-item">
            <div class="maint-info">
              <h4>{{ $t('system.autoBackup') }}</h4>
              <p>{{ $t('system.autoBackupDesc') }}</p>
            </div>
            <el-switch v-model="autoBackup" :active-text="$t('system.enabled')" :inactive-text="$t('system.disabled')" />
          </div>

          <el-divider content-position="left">{{ $t('system.cacheManagement') }}</el-divider>
          
          <div class="maint-item">
            <div class="maint-info">
              <h4>{{ $t('system.clearSystemCache') }}</h4>
              <p>{{ $t('system.clearCacheDesc') }}</p>
            </div>
            <el-button type="warning" @click="handleClearCache" :loading="loading.cache">
              <el-icon><Delete /></el-icon> {{ $t('system.clearCache') }}
            </el-button>
          </div>

          <el-divider content-position="left">{{ $t('system.dangerousOperations') }}</el-divider>
          
          <div class="maint-item danger">
            <div class="maint-info">
              <h4>{{ $t('system.resetSystem') }}</h4>
              <p class="danger-text">{{ $t('system.resetSystemDesc') }}</p>
            </div>
            <el-button type="danger" @click="handleResetSystem" :loading="loading.reset">
              <el-icon><Warning /></el-icon> {{ $t('system.resetSystem') }}
            </el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 操作日志 -->
    <el-card class="log-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ $t('system.configOperationLog') }}</span>
          <el-button link @click="refreshLogs">
            <el-icon><Refresh /></el-icon> {{ $t('system.refresh') }}
          </el-button>
        </div>
      </template>
      
      <el-table :data="operationLogs" v-loading="logLoading" size="small">
        <el-table-column prop="time" :label="$t('system.time')" width="160" />
        <el-table-column prop="user" :label="$t('system.operator')" width="100" />
        <el-table-column prop="action" :label="$t('system.operationType')" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" :label="$t('system.details')" />
        <el-table-column prop="ip" :label="$t('system.ipAddress')" width="120" />
      </el-table>
      
      <el-pagination
        v-model:current-page="logPage"
        v-model:page-size="logPageSize"
        :total="logTotal"
        layout="prev, pager, next, jumper"
        class="pagination"
        @change="fetchLogs"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Check, 
  Download, 
  Delete, 
  Warning, 
  Refresh 
} from '@element-plus/icons-vue'
import { 
  getBasicConfig, 
  saveBasicConfig as apiSaveBasic,
  getBusinessConfig,
  saveBusinessConfig as apiSaveBusiness,
  getNotificationConfig,
  saveNotificationConfig as apiSaveNotification,
  backupData,
  clearCache,
  resetSystem,
  getOperationLogs
} from '@/api/admin/admin'

const { t } = useI18n()
const activeTab = ref('basic')
const saving = reactive({ basic: false, business: false, notification: false })
const loading = reactive({ backup: false, cache: false, reset: false })
const logLoading = ref(false)
const logPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)
const autoBackup = ref(true)

// 基础配置表单
const basicFormRef = ref(null)
const basicForm = reactive({
  hospitalName: '',
  contactPhone: '',
  address: '',
  maxAdvanceDays: 7,
  dailyLimit: 50,
  announcement: ''
})

const basicRules = {
  hospitalName: [{ required: true, message: t('system.pleaseEnterOrganizationName'), trigger: 'blur' }],
  contactPhone: [{ required: true, message: t('system.pleaseEnterContactPhone'), trigger: 'blur' }],
  address: [{ required: true, message: t('system.pleaseEnterAddress'), trigger: 'blur' }]
}

// 业务配置表单
const businessFormRef = ref(null)
const businessForm = reactive({
  normalFee: 10.00,
  expertFee: 30.00,
  morningTime: [new Date(2024, 0, 1, 8, 0), new Date(2024, 0, 1, 12, 0)],
  afternoonTime: [new Date(2024, 0, 1, 14, 0), new Date(2024, 0, 1, 18, 0)],
  consultDuration: 30,
  refundDeadline: 4
})

const businessRules = {
  normalFee: [{ required: true, message: t('system.pleaseSetNormalFee'), trigger: 'change' }],
  expertFee: [{ required: true, message: t('system.pleaseSetExpertFee'), trigger: 'change' }]
}

// 通知配置表单
const notifyFormRef = ref(null)
const notifyForm = reactive({
  smsReserveSuccess: true,
  smsRemind: true,
  remindTime: 60,
  emailErrorAlert: false,
  alertEmail: ''
})

// 操作日志
const operationLogs = ref([])

// 获取基础配置
const fetchBasicConfig = async () => {
  try {
    const res = await getBasicConfig()
    if (res.code === 200 && res.data) {
      Object.assign(basicForm, res.data)
    }
  } catch (error) {
    console.error('获取基础配置失败:', error)
  }
}

// 保存基础配置
const saveBasicConfig = async () => {
  try {
    await basicFormRef.value.validate()
    saving.basic = true
    const res = await apiSaveBasic(basicForm)
    if (res.code === 200) {
      ElMessage.success(t('system.basicConfigSaved'))
      fetchLogs()
    }
  } catch (error) {
    ElMessage.error(t('system.saveFailed'))
  } finally {
    saving.basic = false
  }
}

const resetBasicForm = () => {
  basicFormRef.value?.resetFields()
  fetchBasicConfig()
}

// 保存业务配置
const formatTime = (date) => {
  if (!date || !(date instanceof Date)) return '00:00'
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

const saveBusinessConfig = async () => {
  try {
    await businessFormRef.value.validate()
    saving.business = true
    const payload = {
      normalFee: businessForm.normalFee,
      expertFee: businessForm.expertFee,
      morningTime: businessForm.morningTime.map(formatTime),
      afternoonTime: businessForm.afternoonTime.map(formatTime),
      consultDuration: businessForm.consultDuration,
      refundDeadline: businessForm.refundDeadline
    }
    const res = await apiSaveBusiness(payload)
    if (res.code === 200) {
      ElMessage.success(t('system.businessConfigSaved'))
      fetchLogs()
    }
  } catch (error) {
    ElMessage.error(t('system.saveFailed'))
  } finally {
    saving.business = false
  }
}

const resetBusinessForm = () => {
  businessFormRef.value?.resetFields()
}

// 解析时间字符串为 Date（兼容 ISO 格式和 HH:mm 格式）
const parseTimeStr = (str) => {
  if (!str) return null
  if (typeof str !== 'string') return null
  // 如果是完整 ISO 时间字符串（如 2024-01-01T00:00:00.000Z）
  if (str.includes('T')) {
    const d = new Date(str)
    if (!isNaN(d.getTime())) return d
  }
  // 否则假设是 HH:mm 格式
  const d = new Date(`2024-01-01T${str}`)
  if (!isNaN(d.getTime())) return d
  return null
}

// 获取业务配置
const fetchBusinessConfig = async () => {
  try {
    const res = await getBusinessConfig()
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.normalFee != null) businessForm.normalFee = Number(data.normalFee)
      if (data.expertFee != null) businessForm.expertFee = Number(data.expertFee)
      if (data.morningTime && Array.isArray(data.morningTime) && data.morningTime.length >= 2) {
        const s = parseTimeStr(data.morningTime[0])
        const e = parseTimeStr(data.morningTime[1])
        if (s && e) businessForm.morningTime = [s, e]
      }
      if (data.afternoonTime && Array.isArray(data.afternoonTime) && data.afternoonTime.length >= 2) {
        const s = parseTimeStr(data.afternoonTime[0])
        const e = parseTimeStr(data.afternoonTime[1])
        if (s && e) businessForm.afternoonTime = [s, e]
      }
      if (data.consultDuration != null) businessForm.consultDuration = Number(data.consultDuration)
      if (data.refundDeadline != null) businessForm.refundDeadline = Number(data.refundDeadline)
    }
  } catch (error) {
    console.error('获取业务配置失败:', error)
  }
}

// 获取通知配置
const fetchNotificationConfig = async () => {
  try {
    const res = await getNotificationConfig()
    if (res.code === 200 && res.data) {
      const data = res.data
      notifyForm.smsReserveSuccess = data.smsReserveSuccess !== false
      notifyForm.smsRemind = data.smsRemind !== false
      notifyForm.remindTime = data.remindTime || 60
      notifyForm.emailErrorAlert = data.emailErrorAlert === true
      notifyForm.alertEmail = data.alertEmail || ''
    }
  } catch (error) {
    console.error('获取通知配置失败:', error)
  }
}

// 保存通知配置
const saveNotifyConfig = async () => {
  saving.notification = true
  try {
    const res = await apiSaveNotification({
      smsReserveSuccess: notifyForm.smsReserveSuccess,
      smsRemind: notifyForm.smsRemind,
      remindTime: notifyForm.remindTime,
      emailErrorAlert: notifyForm.emailErrorAlert,
      alertEmail: notifyForm.alertEmail
    })
    if (res.code === 200) {
      ElMessage.success(t('system.notificationConfigSaved'))
      fetchLogs()
    } else {
      ElMessage.error(t('system.saveFailed'))
    }
  } catch (error) {
    ElMessage.error(t('system.saveFailed'))
  } finally {
    saving.notification = false
  }
}

// 数据备份
const handleBackup = async () => {
  loading.backup = true
  try {
    const res = await backupData()
    if (res.code === 200) {
      ElMessage.success(t('system.backupSuccessful'))
    }
  } catch (error) {
    ElMessage.error(t('system.saveFailed'))
  } finally {
    loading.backup = false
  }
}

// 清除缓存
const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm(t('system.confirmClearCache'), t('system.prompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    loading.cache = true
    const res = await clearCache()
    if (res.code === 200) {
      ElMessage.success(t('system.cacheCleared'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('system.operationFailed'))
    }
  } finally {
    loading.cache = false
  }
}

// 重置系统
const handleResetSystem = async () => {
  try {
    await ElMessageBox.confirm(
      t('system.confirmResetSystem'),
      t('system.dangerousOperationConfirm'),
      {
        confirmButtonText: t('system.understandRiskConfirmReset'),
        cancelButtonText: t('common.cancel'),
        type: 'danger',
        confirmButtonClass: 'el-button--danger'
      }
    )
    loading.reset = true
    const res = await resetSystem()
    if (res.code === 200) {
      ElMessage.success(t('system.systemReset'))
      // 跳转到登录页
      window.location.href = '/login'
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('system.operationFailed'))
    }
  } finally {
    loading.reset = false
  }
}

// 获取操作日志
const fetchLogs = async () => {
  logLoading.value = true
  try {
    const res = await getOperationLogs({ page: logPage.value, size: logPageSize.value })
    if (res.code === 200) {
      operationLogs.value = res.data.list
      logTotal.value = res.data.total
    }
  } catch (error) {
    console.error('获取日志失败:', error)
  } finally {
    logLoading.value = false
  }
}

const refreshLogs = () => {
  logPage.value = 1
  fetchLogs()
}

onMounted(() => {
  fetchBasicConfig()
  fetchBusinessConfig()
  fetchNotificationConfig()
  fetchLogs()
})
</script>

<style scoped>
.system-config {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: var(--text-primary);
}

.subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.config-tabs {
  margin-bottom: 20px;
}

.config-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.config-form {
  padding: 20px;
  max-width: 700px;
}

.form-hint {
  margin-left: 12px;
  color: var(--text-secondary);
  font-size: 12px;
}

/* 数据维护区域样式 */
.maintenance-section {
  padding: 20px;
}

.maint-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  margin-bottom: 16px;
  background: var(--bg-color);
  border-radius: var(--radius-base);
  border: 1px solid var(--border-lighter);
}

.maint-item.danger {
  border-color: var(--danger-color);
  background: rgba(245, 108, 108, 0.05);
}

.maint-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: var(--text-primary);
}

.maint-info p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.maint-info .danger-text {
  color: var(--danger-color);
  font-weight: 500;
}

/* 日志卡片 */
.log-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .config-form {
    max-width: 100%;
  }
  
  .maint-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>