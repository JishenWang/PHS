<template>
  <el-container class="desk-layout">
    <el-aside class="desk-layout__aside" width="240px">
      <div class="desk-layout__brand">
        <div class="desk-layout__brandTitle">宠物医院系统</div>
        <div class="desk-layout__brandSub">前台收银端</div>
      </div>

      <el-menu class="desk-layout__menu" :default-active="activeMenu" router>
        <el-menu-item index="/desk/customer">客户查询</el-menu-item>
        <el-menu-item index="/desk/register">挂号管理</el-menu-item>
        <el-menu-item index="/desk/charge">收费管理</el-menu-item>
      </el-menu>

      <div class="desk-layout__doctorBox">
        <div class="desk-layout__sectionTitle">医生接诊状态</div>
        <div v-for="d in doctors" :key="d.id" class="desk-layout__doctorItem">
          <span>{{ d.name }}</span>
          <el-tag size="small" :type="doctorTagType(d.status)">{{ doctorStatusLabel(d.status) }}</el-tag>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="desk-layout__header">
        <div>
          <div class="desk-layout__pageTitle">{{ pageTitle }}</div>
          <div class="desk-layout__sub">今日运营：挂号 {{ stats.registerCount }} / 接诊 {{ stats.doneCount }} / 收费 ¥{{ stats.chargeTotal.toFixed(2) }}</div>
        </div>

        <div class="desk-layout__headerRight">
          <el-badge :value="unreadCount" :hidden="!unreadCount">
            <el-button text @click="messageVisible = true">消息提醒</el-button>
          </el-badge>

          <el-dropdown>
            <span class="desk-layout__user">前台账号</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="showProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="goLogin">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="desk-layout__main">
        <router-view />
      </el-main>
    </el-container>

    <el-drawer v-model="messageVisible" title="消息提醒" size="420px">
      <el-empty v-if="messages.length === 0" description="暂无消息" />
      <div v-for="m in messages" :key="m.id" class="desk-layout__messageItem" :class="{ unread: !m.read }">
        <div class="desk-layout__messageText">{{ m.content }}</div>
        <div class="desk-layout__messageMeta">
          <span>{{ formatTime(m.createdAt) }}</span>
          <el-button v-if="!m.read" type="primary" link @click="markRead(m.id)">标记已读</el-button>
        </div>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getDoctorStatusList, getMessages, getTodayStats, getDeskEnums, markMessageRead } from '@/api/desk/desk'

const route = useRoute()
const router = useRouter()

const enums = getDeskEnums()
const stats = ref({ registerCount: 0, doneCount: 0, chargeTotal: 0 })
const doctors = ref([])
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

function doctorStatusLabel(status) {
  if (status === enums.DoctorStatus.FREE) return '空闲'
  if (status === enums.DoctorStatus.BUSY) return '接诊中'
  if (status === enums.DoctorStatus.REST) return '休息'
  return '未知'
}

function doctorTagType(status) {
  if (status === enums.DoctorStatus.FREE) return 'success'
  if (status === enums.DoctorStatus.BUSY) return 'warning'
  if (status === enums.DoctorStatus.REST) return 'info'
  return 'info'
}

function formatTime(v) {
  if (!v) return '-'
  const d = new Date(v)
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleString()
}

async function loadDashboard() {
  const [s, d, m] = await Promise.all([getTodayStats(), getDoctorStatusList(), getMessages()])
  stats.value = s.data || stats.value
  doctors.value = d.data || []
  messages.value = m.data || []
}

async function markRead(id) {
  await markMessageRead(id)
  await loadDashboard()
}

function showProfile() {
  ElMessage.success('个人中心：可查看个人操作记录（挂号/收费）')
}

function changePassword() {
  ElMessage.success('密码修改入口已预留')
}

function goLogin() {
  router.push('/login')
}

onMounted(() => {
  loadDashboard()
  dashboardTimer = window.setInterval(loadDashboard, 10000)
})

onUnmounted(() => {
  if (dashboardTimer) {
    window.clearInterval(dashboardTimer)
    dashboardTimer = null
  }
})
</script>

<style scoped lang="scss">
.desk-layout { height: 100vh; min-height: 100vh; background: #f4f6fb; }
.desk-layout__aside { background: #111827; color: #fff; display: flex; flex-direction: column; }
.desk-layout__brand { padding: 16px; border-bottom: 1px solid rgba(255,255,255,.12); }
.desk-layout__brandTitle { font-size: 18px; font-weight: 700; }
.desk-layout__brandSub { margin-top: 4px; font-size: 12px; opacity: .8; }
.desk-layout__menu { border-right: none; background: transparent; }
.desk-layout__menu :deep(.el-menu-item) { color: rgba(255,255,255,.86); }
.desk-layout__menu :deep(.el-menu-item.is-active) { background: rgba(59,130,246,.25); color: #fff; }
.desk-layout__doctorBox { margin-top: auto; padding: 12px 16px 16px; border-top: 1px solid rgba(255,255,255,.12); }
.desk-layout__sectionTitle { font-size: 12px; opacity: .9; margin-bottom: 8px; }
.desk-layout__doctorItem { display: flex; align-items: center; justify-content: space-between; margin: 6px 0; }
.desk-layout__header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #ebeef5; }
.desk-layout__pageTitle { font-size: 16px; font-weight: 700; color: #111827; }
.desk-layout__sub { margin-top: 2px; font-size: 12px; color: #6b7280; }
.desk-layout__headerRight { display: flex; align-items: center; gap: 14px; }
.desk-layout__user { cursor: pointer; color: #2563eb; }
.desk-layout__main { padding: 16px; }
.desk-layout__messageItem { border: 1px solid #e5e7eb; border-radius: 8px; padding: 10px; margin-bottom: 10px; }
.desk-layout__messageItem.unread { border-color: #93c5fd; background: #eff6ff; }
.desk-layout__messageText { color: #111827; line-height: 1.5; }
.desk-layout__messageMeta { margin-top: 6px; font-size: 12px; color: #6b7280; display: flex; justify-content: space-between; }
</style>
