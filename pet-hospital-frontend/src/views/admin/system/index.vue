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
                {{ Math.abs(item.change) }}% 较上月
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
              <span>预约趋势</span>
              <el-radio-group v-model="trendPeriod" size="small" @change="handlePeriodChange">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">全年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-placeholder" v-loading="trendLoading">
            <div class="mock-chart">
              <div v-for="i in 7" :key="i" class="bar" :style="{ height: `${Math.random() * 60 + 20}%` }"></div>
            </div>
            <div class="chart-labels">
              <span v-for="day in ['周一', '周二', '周三', '周四', '周五', '周六', '周日']" :key="day">{{ day }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>宠物种类分布</span>
            </div>
          </template>
          <div class="pie-chart" v-loading="pieLoading">
            <div class="pie-item" v-for="(item, index) in petTypes" :key="index">
              <div class="pie-color" :style="{ background: item.color }"></div>
              <span class="pie-label">{{ item.name }}</span>
              <span class="pie-value">{{ item.value }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口和最近活动 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>快捷入口</span>
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
              <span class="action-name">{{ action.name }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>最近活动</span>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, FirstAidKit, Calendar, TrendCharts, ArrowUp, ArrowDown, Plus, Edit, Search, Setting } from '@element-plus/icons-vue'
import { getDashboardData, getTrendData, getPetTypeDistribution, getRecentActivities } from '@/api/admin/admin'

const trendPeriod = ref('week')
const trendLoading = ref(false)
const pieLoading = ref(false)
const activityLoading = ref(false)

const statistics = ref([
  { title: '总用户数', value: 0, change: 0, type: 'primary', icon: 'User' },
  { title: '医生数量', value: 0, change: 0, type: 'success', icon: 'FirstAidKit' },
  { title: '宠物档案', value: 0, change: 0, type: 'warning', icon: 'Calendar' },
  { title: '今日预约', value: 0, change: 0, type: 'danger', icon: 'TrendCharts' }
])

const petTypes = ref([
  { name: '犬类', value: 45, color: '#409EFF' },
  { name: '猫类', value: 35, color: '#67C23A' },
  { name: '其他', value: 20, color: '#E6A23C' }
])

const activities = ref([])

const quickActions = ref([
  { name: '新增用户', path: '/admin/user', icon: 'Plus', color: '#409EFF' },
  { name: '编辑医生', path: '/admin/doctor', icon: 'Edit', color: '#67C23A' },
  { name: '查询宠物', path: '/admin/pet', icon: 'Search', color: '#E6A23C' },
  { name: '系统设置', path: '/admin/system', icon: 'Setting', color: '#909399' }
])

const fetchDashboardData = async () => {
  try {
    const res = await getDashboardData()
    if (res.code === 200) {
      const data = res.data
      statistics.value[0].value = data.userCount
      statistics.value[0].change = data.userChange || 0
      statistics.value[1].value = data.doctorCount
      statistics.value[1].change = data.doctorChange || 0
      statistics.value[2].value = data.petCount
      statistics.value[2].change = data.petChange || 0
      statistics.value[3].value = data.todayReserve
      statistics.value[3].change = data.reserveChange || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchTrendData = async () => {
  trendLoading.value = true
  try {
    const res = await getTrendData(trendPeriod.value)
    if (res.code === 200) {
      console.log('趋势数据:', res.data)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  } finally {
    trendLoading.value = false
  }
}

const fetchPetDistribution = async () => {
  pieLoading.value = true
  try {
    const res = await getPetTypeDistribution()
    if (res.code === 200) {
      petTypes.value = res.data
    }
  } catch (error) {
    console.error('获取宠物分布失败:', error)
  } finally {
    pieLoading.value = false
  }
}

const fetchActivities = async () => {
  activityLoading.value = true
  try {
    const res = await getRecentActivities()
    if (res.code === 200) {
      activities.value = res.data
    }
  } catch (error) {
    console.error('获取活动记录失败:', error)
  } finally {
    activityLoading.value = false
  }
}

const handlePeriodChange = () => {
  fetchTrendData()
}

onMounted(() => {
  fetchDashboardData()
  fetchTrendData()
  fetchPetDistribution()
  fetchActivities()
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
  padding: 24px;
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
  width: 64px;
  height: 64px;
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #fff;
}

.stat-primary .stat-icon { background: var(--primary-color); }
.stat-success .stat-icon { background: var(--success-color); }
.stat-warning .stat-icon { background: var(--warning-color); }
.stat-danger .stat-icon { background: var(--danger-color); }

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
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

.chart-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 20px;
}

.mock-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  gap: 20px;
}

.bar {
  width: 40px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--primary-light) 100%);
  border-radius: 4px 4px 0 0;
  transition: all 0.3s;
  animation: grow 1s ease-out;
}

@keyframes grow {
  from { height: 0 !important; }
}

.bar:hover {
  background: var(--primary-dark);
}

.chart-labels {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
  color: var(--text-secondary);
  font-size: 12px;
}

.pie-chart {
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
  padding: 20px;
}

.pie-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pie-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.pie-label {
  flex: 1;
  color: var(--text-regular);
}

.pie-value {
  font-weight: 600;
  color: var(--text-primary);
}

.bottom-row {
  margin-bottom: 20px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 10px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
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
  width: 48px;
  height: 48px;
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.action-name {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}
</style>