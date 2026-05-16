<template>
  <div class="settings-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Setting /></el-icon>
        {{ $t('settings.systemSettings') }}
      </h2>
    </div>

    <div class="settings-container">
      <!-- 左侧菜单 -->
      <div class="settings-menu">
        <div
          v-for="item in menuList"
          :key="item.key"
          class="menu-item"
          :class="{ active: activeMenu === item.key }"
          @click="activeMenu = item.key"
        >
          <el-icon :size="20">
            <component :is="item.icon" />
          </el-icon>
          <span class="menu-text">{{ item.label }}</span>
          <el-icon class="arrow" v-if="activeMenu === item.key"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="settings-content">
        <!-- Interface Settings -->
        <template v-if="activeMenu === 'interface'">
          <el-card shadow="hover" class="setting-card">
            <template #header>
              <div class="card-header">
                <span>{{ $t('settings.interfaceSettings') }}</span>
              </div>
            </template>

            <div class="setting-list">
              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.themeColor') }}</div>
                  <div class="item-desc">{{ $t('settings.themeColorDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-color-picker
                    v-model="settings.themeColor"
                    :predefine="predefineColors"
                    @change="handleThemeChange"
                  />
                </div>
              </div>

              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.sidebarStyle') }}</div>
                  <div class="item-desc">{{ $t('settings.sidebarStyleDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-radio-group v-model="settings.sidebarStyle" @change="handleSidebarStyleChange">
                    <el-radio-button label="dark">{{ $t('settings.dark') }}</el-radio-button>
                    <el-radio-button label="light">{{ $t('settings.light') }}</el-radio-button>
                  </el-radio-group>
                </div>
              </div>

              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.collapseMenu') }}</div>
                  <div class="item-desc">{{ $t('settings.collapseMenuDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-switch v-model="settings.collapseMenu" @change="val => settingsStore.collapseMenu = val" />
                </div>
              </div>

              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.showBreadcrumb') }}</div>
                  <div class="item-desc">{{ $t('settings.showBreadcrumbDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-switch v-model="settings.showBreadcrumb" @change="val => settingsStore.showBreadcrumb = val" />
                </div>
              </div>
            </div>
          </el-card>
        </template>

        <!-- Notification Settings -->
        <template v-if="activeMenu === 'notification'">
          <el-card shadow="hover" class="setting-card">
            <template #header>
              <div class="card-header">
                <span>{{ $t('settings.notificationSettings') }}</span>
              </div>
            </template>

            <div class="setting-list">
              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.newRegistrationReminder') }}</div>
                  <div class="item-desc">{{ $t('settings.newRegistrationDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-switch v-model="settings.notifyNewRegister" @change="val => settingsStore.notifyNewRegister = val" />
                </div>
              </div>

              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.newChargeReminder') }}</div>
                  <div class="item-desc">{{ $t('settings.newChargeDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-switch v-model="settings.notifyNewConsult" @change="val => settingsStore.notifyNewConsult = val" />
                </div>
              </div>

              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.refundApplicationReminder') }}</div>
                  <div class="item-desc">{{ $t('settings.refundApplicationDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-switch v-model="settings.notifyTimeout" @change="val => settingsStore.notifyTimeout = val" />
                </div>
              </div>

              <el-divider />

              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.soundReminder') }}</div>
                  <div class="item-desc">{{ $t('settings.soundReminderDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-switch v-model="settings.soundEnabled" @change="handleSoundTest" />
                </div>
              </div>

              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.desktopNotification') }}</div>
                  <div class="item-desc">{{ $t('settings.desktopNotificationDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-switch v-model="settings.desktopNotify" @change="handleDesktopNotify" />
                </div>
              </div>
            </div>
          </el-card>
        </template>

        <!-- Data Settings -->
        <template v-if="activeMenu === 'data'">
          <el-card shadow="hover" class="setting-card">
            <template #header>
              <div class="card-header">
                <span>{{ $t('settings.dataSettings') }}</span>
              </div>
            </template>

            <div class="setting-list">
              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.autoRefreshInterval') }}</div>
                  <div class="item-desc">{{ $t('settings.autoRefreshDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-slider
                    v-model="settings.refreshInterval"
                    :min="10"
                    :max="300"
                    :step="10"
                    show-stops
                    style="width: 200px;"
                    @change="val => settingsStore.refreshInterval = val"
                  />
                  <span class="value-text">{{ settings.refreshInterval }}s</span>
                </div>
              </div>

              <div class="setting-item">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.pageSize') }}</div>
                  <div class="item-desc">{{ $t('settings.pageSizeDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-select v-model="settings.pageSize" style="width: 150px;" @change="val => settingsStore.pageSize = val">
                    <el-option :label="'10 ' + $t('settings.items')" :value="10" />
                    <el-option :label="'20 ' + $t('settings.items')" :value="20" />
                    <el-option :label="'50 ' + $t('settings.items')" :value="50" />
                    <el-option :label="'100 ' + $t('settings.items')" :value="100" />
                  </el-select>
                </div>
              </div>

              <el-divider />

              <div class="setting-item danger">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.clearCache') }}</div>
                  <div class="item-desc">{{ $t('settings.clearCacheDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-button type="danger" plain @click="handleClearCache">{{ $t('settings.clearCache') }}</el-button>
                </div>
              </div>

              <div class="setting-item danger">
                <div class="item-info">
                  <div class="item-title">{{ $t('settings.exportPersonalData') }}</div>
                  <div class="item-desc">{{ $t('settings.exportDataDesc') }}</div>
                </div>
                <div class="item-control">
                  <el-button type="primary" plain @click="handleExportData">{{ $t('settings.exportPersonalData') }}</el-button>
                </div>
              </div>
            </div>
          </el-card>
        </template>

        <!-- 关于系统 -->
        <template v-if="activeMenu === 'about'">
          <el-card shadow="hover" class="setting-card about-card">
            <div class="about-content">
              <div class="logo-section">
                <div class="app-logo">
                  <el-icon :size="64" color="#059669"><FirstAidKit /></el-icon>
                </div>
                <h2 class="app-name">{{ $t('settings.appName') }}</h2>
                <p class="app-version">{{ $t('settings.appVersion', { version: versionInfo.version || 'v1.0.0' }) }}</p>
              </div>

              <el-descriptions :column="1" border class="about-info">
                <el-descriptions-item :label="$t('settings.systemName')">{{ $t('settings.systemNameDesk') }}</el-descriptions-item>
                <el-descriptions-item :label="$t('settings.updateTime')">{{ versionInfo.buildDate || '2026-04-12' }}</el-descriptions-item>
              </el-descriptions>

              <div class="about-actions">
                <el-button type="primary" @click="handleCheckUpdate" :loading="checkingUpdate">
                  <el-icon><Refresh /></el-icon>
                  {{ $t('settings.checkUpdate') }}
                </el-button>
                <el-button @click="showFeedback = true">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ $t('settings.feedback') }}
                </el-button>
              </div>

              <div class="copyright">
                <p>{{ $t('settings.copyright') }}</p>
              </div>
            </div>
          </el-card>
        </template>

        <!-- 保存按钮 -->
        <div class="settings-footer" v-if="activeMenu !== 'about'">
          <el-button type="primary" size="large" @click="handleSaveSettings">
            <el-icon><Check /></el-icon>
            {{ $t('settings.saveSettings') }}
          </el-button>
          <el-button size="large" @click="handleResetSettings">{{ $t('settings.restoreDefault') }}</el-button>
        </div>
      </div>
    </div>

    <!-- 反馈对话框 -->
    <el-dialog v-model="showFeedback" :title="$t('settings.feedback')" width="500px">
      <el-form :model="feedbackForm" label-width="80px">
        <el-form-item :label="$t('settings.feedbackType')">
          <el-radio-group v-model="feedbackForm.type">
            <el-radio label="bug">{{ $t('settings.bug') }}</el-radio>
            <el-radio label="feature">{{ $t('settings.featureRequest') }}</el-radio>
            <el-radio label="other">{{ $t('settings.other') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('settings.detailedDescription')">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="4"
            :placeholder="$t('settings.feedbackPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="$t('settings.contact')">
          <el-input v-model="feedbackForm.contact" :placeholder="$t('settings.contactPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFeedback = false">{{ $t('settings.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmitFeedback" :loading="submittingFeedback">{{ $t('settings.submitFeedback') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, Monitor, Bell, DataLine, Lock, InfoFilled,
  ArrowRight, Check, Refresh, ChatDotRound, FirstAidKit
} from '@element-plus/icons-vue'
import { useSettingsStore } from '@/store/settings'
import { getDeskVersion, submitDeskFeedback } from '@/api/desk/desk'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const settingsStore = useSettingsStore()

// 菜单配置
const menuList = [
  { key: 'interface', label: t('settings.interfaceSettings'), icon: 'Monitor' },
  { key: 'notification', label: t('settings.notificationSettings'), icon: 'Bell' },
  { key: 'data', label: t('settings.dataSettings'), icon: 'DataLine' },
  { key: 'about', label: t('settings.aboutSystem'), icon: 'InfoFilled' }
]

const activeMenu = ref('interface')

// 预定义颜色
const predefineColors = [
  '#3b82f6', '#10b981', '#f59e0b', '#ef4444',
  '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'
]

// 设置数据
const settings = reactive({
  themeColor: settingsStore.themeColor,
  sidebarStyle: settingsStore.sidebarStyle,
  collapseMenu: settingsStore.collapseMenu,
  showBreadcrumb: settingsStore.showBreadcrumb,
  notifyNewRegister: settingsStore.notifyNewRegister,
  notifyNewConsult: settingsStore.notifyNewConsult,
  notifyTimeout: settingsStore.notifyTimeout,
  soundEnabled: settingsStore.soundEnabled,
  desktopNotify: settingsStore.desktopNotify,
  refreshInterval: settingsStore.refreshInterval,
  pageSize: settingsStore.pageSize
})

// 同步 store 到本地 reactive
watch(() => settingsStore.$state, (newState) => {
  Object.keys(settings).forEach(key => {
    if (key in newState) {
      settings[key] = newState[key]
    }
  })
}, { deep: true })

// 反馈
const showFeedback = ref(false)
const submittingFeedback = ref(false)
const feedbackForm = reactive({
  type: 'bug',
  content: '',
  contact: ''
})

// 版本信息
const versionInfo = reactive({
  version: 'v1.0.0',
  buildDate: '2026-04-12',
  forceUpdate: false,
  updateLog: ''
})
const checkingUpdate = ref(false)

// 方法
const handleThemeChange = (color) => {
  settingsStore.themeColor = color
  settingsStore.applyThemeColor(color)
  ElMessage.success(t('settings.themeApplied'))
}

const handleSidebarStyleChange = (style) => {
  settingsStore.sidebarStyle = style
  ElMessage.success(t('settings.sidebarSwitched'))
}

const handleDesktopNotify = async (val) => {
  if (val) {
    const granted = await settingsStore.requestNotificationPermission()
    if (!granted) {
      ElMessage.warning(t('settings.allowNotification'))
      settings.desktopNotify = false
      settingsStore.desktopNotify = false
    } else {
      settingsStore.desktopNotify = true
      ElMessage.success(t('settings.desktopEnabled'))
    }
  } else {
    settingsStore.desktopNotify = false
  }
}

const handleSoundTest = (val) => {
  settingsStore.soundEnabled = val
  if (val) {
    settingsStore.playNotificationSound()
    ElMessage.success(t('settings.soundEnabled'))
  }
}

const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm(t('settings.clearCacheConfirm'), t('settings.confirmClear'), {
      type: 'warning'
    })
    localStorage.clear()
    ElMessage.success(t('settings.cacheCleared'))
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (e) {
    // 用户取消
  }
}

const handleExportData = () => {
  const data = JSON.stringify(settingsStore.$state, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `desk_settings_${new Date().getTime()}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(t('settings.dataExported'))
}

const handleCheckUpdate = async () => {
  checkingUpdate.value = true
  try {
    const res = await getDeskVersion()
    if (res.code === 200 && res.data) {
      Object.assign(versionInfo, res.data)
      if (res.data.forceUpdate) {
        ElMessageBox.alert(res.data.updateLog || t('settings.alreadyLatest'), t('settings.versionUpdate'), {
          confirmButtonText: t('settings.gotIt'),
          type: 'warning'
        })
      } else {
        ElMessage.success(res.data.updateLog || t('settings.alreadyLatest'))
      }
    } else {
      ElMessage.success(t('settings.alreadyLatest'))
    }
  } catch (error) {
    ElMessage.error(t('settings.checkUpdate'))
  } finally {
    checkingUpdate.value = false
  }
}

const handleSubmitFeedback = async () => {
  if (!feedbackForm.content.trim()) {
    ElMessage.warning(t('settings.pleaseEnterFeedback'))
    return
  }
  submittingFeedback.value = true
  try {
    const res = await submitDeskFeedback({
      type: feedbackForm.type,
      content: feedbackForm.content,
      contact: feedbackForm.contact
    })
    if (res.code === 200) {
      ElMessage.success(t('settings.feedbackSubmitted'))
      showFeedback.value = false
      feedbackForm.content = ''
      feedbackForm.contact = ''
    } else {
      ElMessage.error(res.message || t('settings.feedbackFailed'))
    }
  } catch (error) {
    ElMessage.error(t('settings.feedbackFailed'))
  } finally {
    submittingFeedback.value = false
  }
}

const handleSaveSettings = async () => {
  Object.keys(settings).forEach(key => {
    if (key in settingsStore) {
      settingsStore[key] = settings[key]
    }
  })
  settingsStore.saveSettings()
  ElMessage.success(t('settings.settingsSaved'))
}

const handleResetSettings = async () => {
  try {
    await ElMessageBox.confirm(t('settings.restoreConfirm'), t('settings.confirmRestore'), {
      type: 'warning'
    })
    settingsStore.resetSettings()
    Object.assign(settings, settingsStore.$state)
    settings.pageSize = 20
    ElMessage.success(t('settings.defaultRestored'))
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  settingsStore.initSettings()
  Object.assign(settings, settingsStore.$state)
})
</script>

<style scoped lang="scss">
.settings-page {
  .page-header {
    margin-bottom: 25px;

    .page-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 24px;
      font-weight: 600;
      color: #1e293b;

      .el-icon {
        color: #059669;
      }
    }
  }

  .settings-container {
    display: grid;
    grid-template-columns: 260px 1fr;
    gap: 25px;

    .settings-menu {
      background: white;
      border-radius: 16px;
      padding: 15px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
      height: fit-content;

      .menu-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 15px 20px;
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s;
        color: #64748b;
        margin-bottom: 5px;

        &:hover {
          background: #f8fafc;
          color: #334155;
        }

        &.active {
          background: #059669;
          color: white;

          .arrow {
            opacity: 1;
          }
        }

        .menu-text {
          flex: 1;
          font-weight: 500;
        }

        .arrow {
          opacity: 0;
          transition: opacity 0.3s;
        }
      }
    }

    .settings-content {
      .setting-card {
        border-radius: 16px;
        margin-bottom: 20px;

        .card-header {
          font-size: 18px;
          font-weight: 600;
          color: #1e293b;
        }

        .setting-list {
          .setting-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 24px 0;
            border-bottom: 1px solid #f1f5f9;

            &:last-child {
              border-bottom: none;
              padding-bottom: 0;
            }

            &:first-child {
              padding-top: 0;
            }

            &.danger {
              .item-title {
                color: #ef4444;
              }
            }

            .item-info {
              .item-title {
                font-size: 16px;
                font-weight: 500;
                color: #334155;
                margin-bottom: 6px;
              }

              .item-desc {
                font-size: 13px;
                color: #94a3b8;
              }
            }

            .item-control {
              display: flex;
              align-items: center;
              gap: 15px;

              .value-text {
                color: #64748b;
                font-size: 14px;
                min-width: 50px;
              }
            }
          }
        }

        &.about-card {
          .about-content {
            text-align: center;
            padding: 40px;

            .logo-section {
              margin-bottom: 40px;

              .app-logo {
                width: 120px;
                height: 120px;
                background: #f0fdf4;
                border-radius: 24px;
                display: flex;
                align-items: center;
                justify-content: center;
                margin: 0 auto 20px;
              }

              .app-name {
                font-size: 24px;
                font-weight: 600;
                color: #1e293b;
                margin-bottom: 10px;
              }

              .app-version {
                color: #94a3b8;
                font-size: 14px;
              }
            }

            .about-info {
              max-width: 500px;
              margin: 0 auto 40px;
              text-align: left;
            }

            .about-actions {
              display: flex;
              gap: 15px;
              justify-content: center;
              margin-bottom: 40px;
            }

            .copyright {
              color: #94a3b8;
              font-size: 13px;
            }
          }
        }
      }

      .settings-footer {
        display: flex;
        gap: 15px;
        justify-content: center;
        padding: 20px;
      }
    }
  }
}
</style>
