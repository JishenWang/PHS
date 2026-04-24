<template>
  <el-container class="desk-layout">
    <!-- 左侧边栏 -->
    <el-aside class="desk-layout__aside" width="240px">
      <div class="desk-layout__brand">
        <div class="desk-layout__brandTitle">宠物医院系统</div>
        <div class="desk-layout__brandSub">前台收银端</div>
      </div>

      <el-menu class="desk-layout__menu" :default-active="activeMenu" router>
        <el-menu-item index="/desk/customer">
          <el-icon><User /></el-icon>
          <span>客户查询</span>
        </el-menu-item>
        <el-menu-item index="/desk/register">
          <el-icon><Calendar /></el-icon>
          <span>挂号管理</span>
        </el-menu-item>
        <el-menu-item index="/desk/charge">
          <el-icon><Money /></el-icon>
          <span>收费管理</span>
        </el-menu-item>
      </el-menu>

      <!-- 医生状态面板 -->
      <div class="desk-layout__doctorBox">
        <div class="desk-layout__sectionTitle">医生接诊状态</div>
        <div v-for="d in doctors" :key="d.id" class="desk-layout__doctorItem">
          <span>{{ d.name }}</span>
          <el-tag size="small" :type="doctorTagType(d.status)">
            {{ doctorStatusLabel(d.status) }}
          </el-tag>
        </div>
      </div>
    </el-aside>

    <!-- 右侧主内容 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="desk-layout__header">
        <div>
          <div class="desk-layout__pageTitle">{{ pageTitle }}</div>
          <div class="desk-layout__sub">
            今日运营：挂号 {{ stats.registerCount }} / 接诊 {{ stats.doneCount }} / 收费 ¥{{ stats.chargeTotal?.toFixed(2) || '0.00' }}
          </div>
        </div>

        <div class="desk-layout__headerRight">
          <el-badge :value="unreadCount" :hidden="!unreadCount">
            <el-button text @click="messageVisible = true">
              <el-icon><Bell /></el-icon> 消息提醒
            </el-button>
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <span class="desk-layout__user">
              <el-icon><User /></el-icon>
              {{ userStore.username || '前台账号' }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="desk-layout__main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 消息抽屉 -->
    <el-drawer v-model="messageVisible" title="消息提醒" size="420px">
      <el-empty v-if="messages.length === 0" description="暂无消息" />
      <div 
        v-for="m in messages" 
        :key="m.id" 
        class="desk-layout__messageItem" 
        :class="{ unread: !m.read }"
      >
        <div class="desk-layout__messageText">{{ m.content }}</div>
        <div class="desk-layout__messageMeta">
          <span>{{ formatTime(m.createdAt) }}</span>
          <el-button v-if="!m.read" type="primary" link @click="markRead(m.id)">
            标记已读
          </el-button>
        </div>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Calendar, Money, Bell, ArrowDown,
  Lock, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 模拟数据（实际应从 API 获取）
const stats = ref({ registerCount: 0, doneCount: 0, chargeTotal: 0 })
const doctors = ref([
  { id: 1, name: '张医生', status: 'FREE' },
  { id: 2, name: '李医生', status: 'BUSY' },
  { id: 3, name: '王医生', status: 'REST' }
])
const messages = ref([])
const messageVisible = ref(false)
let dashboardTimer = null

const activeMenu = computed(() => {
  const p = route.path || ''
  if (p.startsWith('/desk/customer')) return '/desk/customer'
  if (p.startsWith('/desk/register')) return '/desk/register'
  if (p.startsWith('/desk/charge')) return '/desk/charge'
  return '/desk/customer'
})

const pageTitle = computed(() => route.meta?.title || '前台工作台')
const unreadCount = computed(() => messages.value.filter(x => !x.read).length)

// 医生状态映射
const doctorStatusMap = {
  'FREE': { label: '空闲', type: 'success' },
  'BUSY': { label: '接诊中', type: 'warning' },
  'REST': { label: '休息', type: 'info' }
}

const doctorStatusLabel = (status) => doctorStatusMap[status]?.label || '未知'
const doctorTagType = (status) => doctorStatusMap[status]?.type || 'info'

const formatTime = (v) => {
  if (!v) return '-'
  const d = new Date(v)
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleString()
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.success('个人中心')
      break
    case 'password':
      ElMessage.success('修改密码')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
  }).catch(() => {})
}

const markRead = (id) => {
  const msg = messages.value.find(m => m.id === id)
  if (msg) msg.read = true
}

// 加载数据
const loadDashboard = async () => {
  // 实际项目中调用 API
  // const [s, d, m] = await Promise.all([getTodayStats(), getDoctorStatusList(), getMessages()])
}

onMounted(() => {
  loadDashboard()
  dashboardTimer = window.setInterval(loadDashboard, 30000) // 30秒刷新
})

onUnmounted(() => {
  if (dashboardTimer) {
    clearInterval(dashboardTimer)
    dashboardTimer = null
  }
})
</script>

<style scoped lang="scss">
.desk-layout {
  height: 100vh;
  min-height: 100vh;
  background: #f4f6fb;
}

.desk-layout__aside {
  background: #111827;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.desk-layout__brand {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.desk-layout__brandTitle {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.desk-layout__brandSub {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.8;
  color: #9ca3af;
}

.desk-layout__menu {
  border-right: none;
  background: transparent;
  flex: 1;
}

.desk-layout__menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.86);
  height: 50px;
  line-height: 50px;
  margin: 4px 10px;
  border-radius: 6px;
}

.desk-layout__menu :deep(.el-menu-item.is-active) {
  background: rgba(59, 130, 246, 0.25);
  color: #fff;
}

.desk-layout__menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
}

.desk-layout__menu :deep(.el-icon) {
  margin-right: 10px;
}

.desk-layout__doctorBox {
  margin-top: auto;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.desk-layout__sectionTitle {
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 12px;
  color: #9ca3af;
}

.desk-layout__doctorItem {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0;
  font-size: 14px;
}

.desk-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  height: 64px;
}

.desk-layout__pageTitle {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.desk-layout__sub {
  margin-top: 4px;
  font-size: 13px;
  color: #6b7280;
}

.desk-layout__headerRight {
  display: flex;
  align-items: center;
  gap: 20px;
}

.desk-layout__user {
  cursor: pointer;
  color: #2563eb;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.desk-layout__user:hover {
  background: #eff6ff;
}

.desk-layout__main {
  padding: 20px;
}

.desk-layout__messageItem {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.desk-layout__messageItem.unread {
  border-color: #93c5fd;
  background: #eff6ff;
}

.desk-layout__messageText {
  color: #111827;
  line-height: 1.5;
  font-size: 14px;
}

.desk-layout__messageMeta {
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>