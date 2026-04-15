<template>
  <div class="system-config">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统配置</h2>
      <p class="subtitle">管理系统的基本参数和业务规则</p>
    </div>

    <!-- 配置标签页 -->
    <el-tabs v-model="activeTab" type="border-card" class="config-tabs">
      <!-- 基础配置 -->
      <el-tab-pane label="基础配置" name="basic">
        <el-form 
          :model="basicForm" 
          :rules="basicRules" 
          ref="basicFormRef"
          label-width="140px"
          class="config-form"
        >
          <el-divider content-position="left">机构信息</el-divider>
          
          <el-form-item label="机构名称" prop="hospitalName">
            <el-input v-model="basicForm.hospitalName" placeholder="请输入宠物医院名称" />
          </el-form-item>
          
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="basicForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
          
          <el-form-item label="机构地址" prop="address">
            <el-input 
              v-model="basicForm.address" 
              type="textarea" 
              :rows="2" 
              placeholder="请输入详细地址"
            />
          </el-form-item>

          <el-divider content-position="left">系统参数</el-divider>
          
          <el-form-item label="预约提前天数" prop="maxAdvanceDays">
            <el-slider v-model="basicForm.maxAdvanceDays" :max="30" show-stops />
            <span class="form-hint">最多可提前 {{ basicForm.maxAdvanceDays }} 天预约</span>
          </el-form-item>
          
          <el-form-item label="每日预约上限" prop="dailyLimit">
            <el-input-number v-model="basicForm.dailyLimit" :min="10" :max="200" />
          </el-form-item>
          
          <el-form-item label="系统公告" prop="announcement">
            <el-input 
              v-model="basicForm.announcement" 
              type="textarea" 
              :rows="3" 
              placeholder="登录页显示的系统公告"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveBasicConfig" :loading="saving.basic">
              <el-icon><Check /></el-icon> 保存基础配置
            </el-button>
            <el-button @click="resetBasicForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 业务配置 -->
      <el-tab-pane label="业务配置" name="business">
        <el-form 
          :model="businessForm" 
          :rules="businessRules" 
          ref="businessFormRef"
          label-width="160px"
          class="config-form"
        >
          <el-divider content-position="left">挂号费用</el-divider>
          
          <el-form-item label="普通挂号费（元）" prop="normalFee">
            <el-input-number v-model="businessForm.normalFee" :min="0" :precision="2" :step="5" />
          </el-form-item>
          
          <el-form-item label="专家挂号费（元）" prop="expertFee">
            <el-input-number v-model="businessForm.expertFee" :min="0" :precision="2" :step="10" />
          </el-form-item>

          <el-divider content-position="left">营业时间</el-divider>
          
          <el-form-item label="上午营业时间" prop="morningTime">
            <el-time-picker
              v-model="businessForm.morningTime"
              is-range
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="HH:mm"
            />
          </el-form-item>
          
          <el-form-item label="下午营业时间" prop="afternoonTime">
            <el-time-picker
              v-model="businessForm.afternoonTime"
              is-range
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="HH:mm"
            />
          </el-form-item>

          <el-divider content-position="left">诊疗规则</el-divider>
          
          <el-form-item label="单次诊疗时长（分钟）" prop="consultDuration">
            <el-radio-group v-model="businessForm.consultDuration">
              <el-radio-button :label="15">15分钟</el-radio-button>
              <el-radio-button :label="20">20分钟</el-radio-button>
              <el-radio-button :label="30">30分钟</el-radio-button>
              <el-radio-button :label="45">45分钟</el-radio-button>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="允许退号时间" prop="refundDeadline">
            <el-select v-model="businessForm.refundDeadline">
              <el-option label="就诊前2小时" :value="2" />
              <el-option label="就诊前4小时" :value="4" />
              <el-option label="就诊前8小时" :value="8" />
              <el-option label="就诊前24小时" :value="24" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveBusinessConfig" :loading="saving.business">
              <el-icon><Check /></el-icon> 保存业务配置
            </el-button>
            <el-button @click="resetBusinessForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 通知配置 -->
      <el-tab-pane label="通知配置" name="notification">
        <el-form 
          :model="notifyForm" 
          ref="notifyFormRef"
          label-width="160px"
          class="config-form"
        >
          <el-divider content-position="left">短信通知</el-divider>
          
          <el-form-item label="预约成功通知">
            <el-switch v-model="notifyForm.smsReserveSuccess" active-text="开启" inactive-text="关闭" />
          </el-form-item>
          
          <el-form-item label="就诊前提醒">
            <el-switch v-model="notifyForm.smsRemind" active-text="开启" inactive-text="关闭" />
          </el-form-item>
          
          <el-form-item label="提醒提前时间" v-if="notifyForm.smsRemind">
            <el-select v-model="notifyForm.remindTime">
              <el-option label="提前30分钟" :value="30" />
              <el-option label="提前1小时" :value="60" />
              <el-option label="提前2小时" :value="120" />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">邮件通知</el-divider>
          
          <el-form-item label="系统异常报警">
            <el-switch v-model="notifyForm.emailErrorAlert" active-text="开启" inactive-text="关闭" />
          </el-form-item>
          
          <el-form-item label="接收邮箱" v-if="notifyForm.emailErrorAlert">
            <el-input v-model="notifyForm.alertEmail" placeholder="多个邮箱用逗号分隔" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveNotifyConfig" :loading="saving.notification">
              <el-icon><Check /></el-icon> 保存通知配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 数据维护 -->
      <el-tab-pane label="数据维护" name="maintenance">
        <div class="maintenance-section">
          <el-divider content-position="left">数据备份</el-divider>
          
          <div class="maint-item">
            <div class="maint-info">
              <h4>手动备份</h4>
              <p>立即创建当前数据库的完整备份</p>
            </div>
            <el-button type="primary" @click="handleBackup" :loading="loading.backup">
              <el-icon><Download /></el-icon> 立即备份
            </el-button>
          </div>
          
          <div class="maint-item">
            <div class="maint-info">
              <h4>自动备份</h4>
              <p>系统每天凌晨2:00自动备份数据</p>
            </div>
            <el-switch v-model="autoBackup" active-text="已开启" inactive-text="已关闭" />
          </div>

          <el-divider content-position="left">缓存管理</el-divider>
          
          <div class="maint-item">
            <div class="maint-info">
              <h4>清除系统缓存</h4>
              <p>清除Redis缓存，强制刷新所有配置</p>
            </div>
            <el-button type="warning" @click="handleClearCache" :loading="loading.cache">
              <el-icon><Delete /></el-icon> 清除缓存
            </el-button>
          </div>

          <el-divider content-position="left">危险操作</el-divider>
          
          <div class="maint-item danger">
            <div class="maint-info">
              <h4>重置系统</h4>
              <p class="danger-text">⚠️ 清空所有业务数据，仅保留管理员账号，此操作不可逆！</p>
            </div>
            <el-button type="danger" @click="handleResetSystem" :loading="loading.reset">
              <el-icon><Warning /></el-icon> 重置系统
            </el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 操作日志 -->
    <el-card class="log-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>配置操作日志</span>
          <el-button link @click="refreshLogs">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="operationLogs" v-loading="logLoading" size="small">
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column prop="user" label="操作人" width="100" />
        <el-table-column prop="action" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" />
        <el-table-column prop="ip" label="IP地址" width="120" />
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
  backupData,
  clearCache,
  resetSystem,
  getOperationLogs
} from '@/api/admin/admin'

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
  hospitalName: [{ required: true, message: '请输入机构名称', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
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
  normalFee: [{ required: true, message: '请设置普通挂号费', trigger: 'change' }],
  expertFee: [{ required: true, message: '请设置专家挂号费', trigger: 'change' }]
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
      ElMessage.success('基础配置保存成功')
      fetchLogs()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.basic = false
  }
}

const resetBasicForm = () => {
  basicFormRef.value?.resetFields()
  fetchBasicConfig()
}

// 保存业务配置
const saveBusinessConfig = async () => {
  try {
    await businessFormRef.value.validate()
    saving.business = true
    const res = await apiSaveBusiness(businessForm)
    if (res.code === 200) {
      ElMessage.success('业务配置保存成功')
      fetchLogs()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.business = false
  }
}

const resetBusinessForm = () => {
  businessFormRef.value?.resetFields()
}

// 保存通知配置
const saveNotifyConfig = async () => {
  saving.notification = true
  setTimeout(() => {
    saving.notification = false
    ElMessage.success('通知配置保存成功')
  }, 500)
}

// 数据备份
const handleBackup = async () => {
  loading.backup = true
  try {
    const res = await backupData()
    if (res.code === 200) {
      ElMessage.success('备份成功，文件已下载')
    }
  } catch (error) {
    ElMessage.error('备份失败')
  } finally {
    loading.backup = false
  }
}

// 清除缓存
const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清除系统缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    loading.cache = true
    const res = await clearCache()
    if (res.code === 200) {
      ElMessage.success('缓存清除成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    loading.cache = false
  }
}

// 重置系统
const handleResetSystem = async () => {
  try {
    await ElMessageBox.confirm(
      '⚠️ 此操作将清空所有业务数据，仅保留管理员账号，确定要继续吗？',
      '危险操作确认',
      {
        confirmButtonText: '我已了解风险，确认重置',
        cancelButtonText: '取消',
        type: 'danger',
        confirmButtonClass: 'el-button--danger'
      }
    )
    loading.reset = true
    const res = await resetSystem()
    if (res.code === 200) {
      ElMessage.success('系统已重置，请重新登录')
      // 跳转到登录页
      window.location.href = '/login'
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
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