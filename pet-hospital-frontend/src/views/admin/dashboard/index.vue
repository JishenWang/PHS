<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="(item, index) in statistics" :key="index">
        <el-card class="stat-card" :body-style="{ padding: '0' }" shadow="hover">
          <div class="stat-content" :class="`stat-${item.type}`">
            <div class="stat-icon">
              <el-icon :size="40">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-change" :class="item.change > 0 ? 'up' : 'down'">
                <el-icon><ArrowUp v-if="item.change > 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(item.change) }}% {{ $t('dashboard.vsLastMonth') }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.appointmentTrend') }}</span>
              <el-radio-group v-model="trendPeriod" size="small" @change="handlePeriodChange">
                <el-radio-button label="week">{{ $t('dashboard.thisWeek') }}</el-radio-button>
                <el-radio-button label="month">{{ $t('dashboard.thisMonth') }}</el-radio-button>
                <el-radio-button label="year">{{ $t('dashboard.thisYear') }}</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container" v-loading="trendLoading"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.petTypeDistribution') }}</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container" v-loading="pieLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.deptDistribution') }}</span>
            </div>
          </template>
          <div ref="deptChartRef" class="chart-container" v-loading="deptLoading"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.appointmentStatusDistribution') }}</span>
            </div>
          </template>
          <div ref="statusChartRef" class="chart-container" v-loading="statusLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口和最近活动 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>{{ $t('dashboard.quickActions') }}</span>
          </template>
          <div class="quick-actions">
            <div 
              v-for="action in quickActions" 
              :key="action.path"
              class="action-item"
              @click="$router.push(action.path)"
            >
              <div class="action-icon" :style="{ background: action.color }">
                <el-icon :size="24"><component :is="action.icon" /></el-icon>
              </div>
              <div class="action-info">
                <span class="action-name">{{ action.name }}</span>
                <span class="action-desc">{{ action.desc }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.recentActivities') }}</span>
              <el-link type="primary" :underline="false" @click="$router.push('/admin/system')">{{ $t('dashboard.viewMore') }}</el-link>
            </div>
          </template>
          <el-timeline v-loading="activityLoading">
            <el-timeline-item 
              v-for="(activity, index) in activities" 
              :key="index"
              :type="activity.type"
              :timestamp="activity.time"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部表格和公告 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.todayPendingAppointments') }}</span>
              <el-button type="primary" size="small" text @click="$router.push('/admin/doctor')">{{ $t('dashboard.viewAll') }}</el-button>
            </div>
          </template>
          <el-table :data="todayReservations" size="small" stripe>
            <el-table-column prop="id" :label="$t('dashboard.appointmentNo')" width="80" />
            <el-table-column prop="petName" :label="$t('dashboard.petName')" width="100" />
            <el-table-column prop="ownerName" :label="$t('dashboard.owner')" width="100" />
            <el-table-column prop="doctorName" :label="$t('dashboard.doctor')" width="100" />
            <el-table-column prop="type" :label="$t('common.type')" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.type === $t('dashboard.firstVisit') ? 'primary' : 'success'">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" :label="$t('dashboard.appointmentTime')" width="120" />
            <el-table-column prop="status" :label="$t('common.status')" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="getStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="100" fixed="right">
              <template #default>
                <el-button link type="primary" size="small">{{ $t('common.process') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.systemNotice') }}</span>
              <el-tag type="warning" size="small">{{ notices.length }}{{ $t('dashboard.newMessages') }}</el-tag>
            </div>
          </template>
          <div class="notice-list">
            <div 
              v-for="(notice, index) in notices" 
              :key="index"
              class="notice-item"
              :class="{ unread: !notice.read }"
            >
              <div class="notice-dot" v-if="!notice.read"></div>
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-time">{{ notice.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  User, FirstAidKit, Calendar, TrendCharts, ArrowUp, ArrowDown,
  Plus, Edit, Search, Setting, Money, Warning, Box, UserFilled,
  Bell, Document, Timer, FirstAidKit as MedicineIcon
} from '@element-plus/icons-vue'
import {
  getDashboardData, getTrendData, getPetTypeDistribution, getRecentActivities,
  getDeptDistribution, getAppointmentStatusDistribution, getTodayAppointments, getNotices
} from '@/api/admin/admin'
import * as echarts from 'echarts'

const { t } = useI18n()

const trendPeriod = ref('week')
const trendLoading = ref(false)
const pieLoading = ref(false)
const activityLoading = ref(false)
const deptLoading = ref(false)
const statusLoading = ref(false)
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const deptChartRef = ref(null)
const statusChartRef = ref(null)
let trendChart = null
let pieChart = null
let deptChart = null
let statusChart = null

const statistics = ref([
  { title: t('dashboard.totalUsers'), value: 0, change: 0, type: 'primary', icon: 'User' },
  { title: t('dashboard.doctorCount'), value: 0, change: 0, type: 'success', icon: 'FirstAidKit' },
  { title: t('dashboard.petRecords'), value: 0, change: 0, type: 'warning', icon: 'Calendar' },
  { title: t('dashboard.todayReserve'), value: 0, change: 0, type: 'danger', icon: 'TrendCharts' },
  { title: t('dashboard.monthlyIncome'), value: '¥0', change: 0, type: 'primary', icon: 'Money' },
  { title: t('dashboard.pendingReserve'), value: 0, change: 0, type: 'warning', icon: 'Timer' },
  { title: t('dashboard.medicineStock'), value: 0, change: 0, type: 'success', icon: 'Box' },
  { title: t('dashboard.newUsersThisMonth'), value: 0, change: 0, type: 'danger', icon: 'UserFilled' }
])

const activities = ref([])

const quickActions = computed(() => [
  { name: t('common.add') + t('用户管理'), path: '/admin/user', icon: 'Plus', color: '#409EFF', desc: t('dashboard.quickActions') },
  { name: t('common.edit') + t('医生管理'), path: '/admin/doctor', icon: 'Edit', color: '#67C23A', desc: t('dashboard.quickActions') },
  { name: t('common.search') + t('宠物管理'), path: '/admin/pet', icon: 'Search', color: '#E6A23C', desc: t('dashboard.quickActions') },
  { name: t('药品管理'), path: '/admin/medicine', icon: 'MedicineIcon', color: '#F56C6C', desc: t('dashboard.quickActions') },
  { name: t('系统配置'), path: '/admin/system', icon: 'Setting', color: '#909399', desc: t('dashboard.quickActions') },
  { name: t('common.operation') + t('common.detail'), path: '/admin/system', icon: 'Document', color: '#8E44AD', desc: t('dashboard.quickActions') }
])

const todayReservations = ref([])
const notices = ref([])

const getStatusType = (status) => {
  const map = {
    [t('dashboard.statusPending')]: 'warning',
    [t('dashboard.statusConfirmed')]: 'primary',
    [t('dashboard.statusCompleted')]: 'success',
    [t('dashboard.statusCancelled')]: 'info'
  }
  return map[status] || 'info'
}

// 初始化趋势图表
const initTrendChart = (data) => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date),
      axisLine: { lineStyle: { color: '#ccc' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#eee' } }
    },
    series: [{
      data: data.map(item => item.count),
      type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#a0cfff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  }
  trendChart.setOption(option)
}

// 初始化饼图
const initPieChart = (data) => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data: data.map(item => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: item.color }
      }))
    }]
  }
  pieChart.setOption(option)
}

// 初始化科室分布图
const initDeptChart = (data) => {
  if (!deptChartRef.value) return
  deptChart = echarts.init(deptChartRef.value)
  const colors = ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C', '#909399', '#8E44AD', '#00BCD4']
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name),
      axisLine: { lineStyle: { color: '#ccc' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#eee' } }
    },
    series: [{
      data: data.map((item, index) => ({
        value: item.value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors[index % colors.length] },
            { offset: 1, color: colors[index % colors.length] + '80' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      })),
      type: 'bar'
    }]
  }
  deptChart.setOption(option)
}

// 初始化预约状态分布图
const initStatusChart = (data) => {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)
  const colorMap = {
    [t('dashboard.statusPending')]: '#E6A23C',
    [t('dashboard.statusConfirmed')]: '#409EFF',
    [t('dashboard.statusCompleted')]: '#67C23A',
    [t('dashboard.statusCancelled')]: '#909399'
  }
  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: data.map(item => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: colorMap[item.name] || '#909399' }
      }))
    }]
  }
  statusChart.setOption(option)
}

// 获取统计数据
const fetchDashboardData = async () => {
  try {
    const res = await getDashboardData()
    if (res.code === 200) {
      const data = res.data
      statistics.value = [
        { title: t('dashboard.totalUsers'), value: data.userCount || 0, change: data.userChange || 0, type: 'primary', icon: 'User' },
        { title: t('dashboard.doctorCount'), value: data.doctorCount || 0, change: data.doctorChange || 0, type: 'success', icon: 'FirstAidKit' },
        { title: t('dashboard.petRecords'), value: data.petCount || 0, change: data.petChange || 0, type: 'warning', icon: 'Calendar' },
        { title: t('dashboard.todayReserve'), value: data.todayReserve || 0, change: data.reserveChange || 0, type: 'danger', icon: 'TrendCharts' },
        { title: t('dashboard.monthlyIncome'), value: '¥' + Math.round(data.income || 0).toLocaleString(), change: data.incomeChange || 0, type: 'primary', icon: 'Money' },
        { title: t('dashboard.pendingReserve'), value: data.pendingReserve || 0, change: data.pendingChange || 0, type: 'warning', icon: 'Timer' },
        { title: t('dashboard.medicineStock'), value: data.medicineCount || 0, change: data.medicineChange || 0, type: 'success', icon: 'Box' },
        { title: t('dashboard.newUsersThisMonth'), value: data.newUserThisMonth || 0, change: data.newUserChange || 0, type: 'danger', icon: 'UserFilled' }
      ]
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取趋势数据
const fetchTrendData = async () => {
  trendLoading.value = true
  try {
    const res = await getTrendData(trendPeriod.value)
    if (res.code === 200 && res.data && res.data.length > 0) {
      initTrendChart(res.data)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  } finally {
    trendLoading.value = false
  }
}

// 获取宠物分布数据
const fetchPetDistribution = async () => {
  pieLoading.value = true
  try {
    const res = await getPetTypeDistribution()
    if (res.code === 200 && res.data && res.data.length > 0) {
      const colors = ['#5470c6', '#91cc75', '#5c6b7a', '#fac858', '#ee6666', '#73c0de']
      const nameMap = {
        '猫': t('dashboard.petTypeCat'),
        '狗': t('dashboard.petTypeDog'),
        '兔': t('dashboard.petTypeRabbit'),
        '未知': t('common.unknown'),
        'cat': t('dashboard.typeCat'),
        'dog': t('dashboard.typeDog'),
        'rabbit': t('dashboard.typeRabbit'),
        'Unknown': t('dashboard.typeUnknown')
      }
      const data = res.data.map((item, index) => ({
        name: nameMap[item.name] || item.name,
        value: item.value,
        color: colors[index % colors.length]
      }))
      initPieChart(data)
    }
  } catch (error) {
    console.error('获取宠物分布失败:', error)
  } finally {
    pieLoading.value = false
  }
}

// 获取最近活动
const fetchActivities = async () => {
  activityLoading.value = true
  try {
    const res = await getRecentActivities()
    if (res.code === 200 && res.data) {
      activities.value = res.data.map(item => ({
        ...item,
        content: String(item.content || '').replace(/^System\s+/, t('dashboard.activitySystem') + ' ')
      }))
    }
  } catch (error) {
    console.error('获取活动记录失败:', error)
  } finally {
    activityLoading.value = false
  }
}

// 获取科室分布
const fetchDeptDistribution = async () => {
  deptLoading.value = true
  try {
    const res = await getDeptDistribution()
    if (res.code === 200 && res.data && res.data.length > 0) {
      const deptMap = {
        '内科': t('dashboard.deptInternalMedicine'),
        '外科': t('dashboard.deptSurgery'),
        '全科医疗部': t('dashboard.deptGeneralPractice'),
        '口腔科': t('dashboard.deptDentistry'),
        '皮肤科': t('dashboard.deptDermatology'),
        '眼科': t('dashboard.deptOphthalmology'),
        '影像科': t('dashboard.deptImaging'),
        '牙科': t('dashboard.deptDentistry')
      }
      initDeptChart(res.data.map(d => ({ ...d, name: deptMap[d.name] || d.name })))
    }
  } catch (error) {
    console.error('获取科室分布失败:', error)
  } finally {
    deptLoading.value = false
  }
}

// 获取预约状态分布
const fetchAppointmentStatus = async () => {
  statusLoading.value = true
  try {
    const res = await getAppointmentStatusDistribution()
    if (res.code === 200 && res.data && res.data.length > 0) {
      const statusMap = {
        'pending': t('dashboard.statusPending'),
        'confirmed': t('dashboard.statusConfirmed'),
        'completed': t('dashboard.statusCompleted'),
        'cancelled': t('dashboard.statusCancelled'),
        'Pending': t('dashboard.statusPending'),
        'Confirmed': t('dashboard.statusConfirmed'),
        'Done': t('dashboard.statusCompleted'),
        'Canceled': t('dashboard.statusCancelled')
      }
      const data = res.data.map(item => ({
        ...item,
        name: statusMap[item.name] || item.name
      }))
      initStatusChart(data)
    }
  } catch (error) {
    console.error('获取预约状态分布失败:', error)
  } finally {
    statusLoading.value = false
  }
}

// 获取今日预约
const fetchTodayAppointments = async () => {
  try {
    const res = await getTodayAppointments()
    if (res.code === 200 && res.data) {
      const statusMap = {
        'pending': t('dashboard.statusPending'),
        'confirmed': t('dashboard.statusConfirmed'),
        'completed': t('dashboard.statusCompleted'),
        'cancelled': t('dashboard.statusCancelled')
      }
      const typeMap = {
        'General': t('dashboard.serviceTypeGeneral'),
        'general': t('dashboard.serviceTypeGeneral')
      }
      todayReservations.value = res.data.map(row => ({
        ...row,
        status: statusMap[row.status] || row.status,
        type: typeMap[row.type] || row.type
      }))
    }
  } catch (error) {
    console.error('获取今日预约失败:', error)
  }
}

// 获取公告
const fetchNotices = async () => {
  try {
    const res = await getNotices()
    if (res.code === 200 && res.data) {
      notices.value = res.data.map(item => {
        const typeMap = {
          lowStock: () => t('dashboard.noticeLowStock', item.params || {}),
          pendingAppointments: () => t('dashboard.noticePendingAppointments', item.params || {}),
          newUsers: () => t('dashboard.noticeNewUsers', item.params || {}),
          systemRunning: () => t('dashboard.noticeSystemRunning')
        }
        const timeMap = {
          justNow: t('dashboard.noticeJustNow'),
          today: t('dashboard.noticeToday'),
          thisWeek: t('dashboard.noticeThisWeek'),
          system: t('dashboard.noticeSystem')
        }
        return {
          title: (typeMap[item.type] || (() => item.type))(),
          time: timeMap[item.timeType] || item.timeType,
          read: item.read
        }
      })
    }
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

const handlePeriodChange = () => {
  fetchTrendData()
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
  deptChart?.resize()
  statusChart?.resize()
}

onMounted(() => {
  fetchDashboardData()
  fetchTrendData()
  fetchPetDistribution()
  fetchActivities()
  fetchDeptDistribution()
  fetchAppointmentStatus()
  fetchTodayAppointments()
  fetchNotices()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
  deptChart?.dispose()
  statusChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: var(--radius-large);
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-dark);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.stat-content::before {
  content: '';
  position: absolute;
  right: -20px;
  top: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.1;
}

.stat-primary::before { background: var(--primary-color); }
.stat-success::before { background: var(--success-color); }
.stat-warning::before { background: var(--warning-color); }
.stat-danger::before { background: var(--danger-color); }

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  color: #fff;
  flex-shrink: 0;
}

.stat-primary .stat-icon { background: var(--primary-color); }
.stat-success .stat-icon { background: var(--success-color); }
.stat-warning .stat-icon { background: var(--warning-color); }
.stat-danger .stat-icon { background: var(--danger-color); }

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.stat-change {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-change.up { color: var(--success-color); }
.stat-change.down { color: var(--danger-color); }

.chart-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.chart-container {
  height: 300px;
}

.bottom-row {
  margin-bottom: 20px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 6px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: var(--radius-base);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid var(--border-lighter);
}

.action-item:hover {
  background: var(--bg-color);
  border-color: var(--primary-color);
  transform: translateX(4px);
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.action-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.action-name {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.action-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-base);
  transition: all 0.3s;
  cursor: pointer;
}

.notice-item:hover {
  background: var(--bg-color);
}

.notice-item.unread {
  background: rgba(64, 158, 255, 0.05);
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--danger-color);
  margin-top: 6px;
  flex-shrink: 0;
}

.notice-content {
  flex: 1;
  min-width: 0;
}

.notice-title {
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.5;
  margin-bottom: 4px;
}

.notice-time {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
