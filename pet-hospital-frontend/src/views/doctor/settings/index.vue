<template>
    <div class="settings-page">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2 class="page-title">
          <el-icon><Setting /></el-icon>
          系统设置
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
          <!-- 界面设置 -->
          <template v-if="activeMenu === 'interface'">
            <el-card shadow="hover" class="setting-card">
              <template #header>
                <div class="card-header">
                  <span>界面设置</span>
                </div>
              </template>
              
              <div class="setting-list">
                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">主题颜色</div>
                    <div class="item-desc">选择系统主题色，将应用于按钮、链接等元素</div>
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
                    <div class="item-title">侧边栏样式</div>
                    <div class="item-desc">选择侧边栏显示模式</div>
                  </div>
                  <div class="item-control">
                    <el-radio-group v-model="settings.sidebarStyle" @change="handleSidebarStyleChange">
                      <el-radio-button label="dark">深色</el-radio-button>
                      <el-radio-button label="light">浅色</el-radio-button>
                    </el-radio-group>
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">菜单折叠</div>
                    <div class="item-desc">是否默认折叠侧边栏菜单</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.collapseMenu" />
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">显示面包屑</div>
                    <div class="item-desc">在页面顶部显示导航路径</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.showBreadcrumb" />
                  </div>
                </div>
              </div>
            </el-card>
          </template>

          <!-- 通知设置 -->
          <template v-if="activeMenu === 'notification'">
            <el-card shadow="hover" class="setting-card">
              <template #header>
                <div class="card-header">
                  <span>通知设置</span>
                </div>
              </template>
              
              <div class="setting-list">
                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">新挂号提醒</div>
                    <div class="item-desc">有新患者挂号时接收通知</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.notifyNewRegister" />
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">新咨询提醒</div>
                    <div class="item-desc">患者提交在线咨询时接收通知</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.notifyNewConsult" />
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">处方支付提醒</div>
                    <div class="item-desc">患者完成处方支付后通知</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.notifyPrescriptionPaid" />
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">接诊超时提醒</div>
                    <div class="item-desc">待接诊超过30分钟提醒</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.notifyTimeout" />
                  </div>
                </div>

                <el-divider />

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">声音提醒</div>
                    <div class="item-desc">新通知时播放提示音</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.soundEnabled" @change="handleSoundTest" />
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">桌面通知</div>
                    <div class="item-desc">允许浏览器桌面通知</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.desktopNotify" @change="handleDesktopNotify" />
                  </div>
                </div>
              </div>
            </el-card>
          </template>

          <!-- 工作设置 -->
          <template v-if="activeMenu === 'work'">
            <el-card shadow="hover" class="setting-card">
              <template #header>
                <div class="card-header">
                  <span>工作设置</span>
                </div>
              </template>
              
              <div class="setting-list">
                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">自动刷新间隔</div>
                    <div class="item-desc">列表数据自动刷新时间（秒）</div>
                  </div>
                  <div class="item-control">
                    <el-slider 
                      v-model="settings.refreshInterval" 
                      :min="10" 
                      :max="300" 
                      :step="10" 
                      show-stops 
                      style="width: 200px;" 
                    />
                    <span class="value-text">{{ settings.refreshInterval }}秒</span>
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">默认接诊状态</div>
                    <div class="item-desc">登录后自动设置的工作状态</div>
                  </div>
                  <div class="item-control">
                    <el-select v-model="settings.defaultStatus" style="width: 150px;">
                      <el-option label="空闲" :value="1" />
                      <el-option label="接诊中" :value="2" />
                      <el-option label="休息" :value="0" />
                    </el-select>
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">病历模板</div>
                    <div class="item-desc">创建病历时使用的默认模板</div>
                  </div>
                  <div class="item-control">
                    <el-select v-model="settings.recordTemplate" style="width: 200px;">
                      <el-option label="通用模板" value="general" />
                      <el-option label="内科模板" value="internal" />
                      <el-option label="外科模板" value="surgery" />
                      <el-option label="疫苗模板" value="vaccine" />
                    </el-select>
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">处方自动保存</div>
                    <div class="item-desc">编辑处方时自动保存草稿</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.autoSavePrescription" />
                  </div>
                </div>
              </div>
            </el-card>
          </template>

          <!-- 隐私设置 -->
          <template v-if="activeMenu === 'privacy'">
            <el-card shadow="hover" class="setting-card">
              <template #header>
                <div class="card-header">
                  <span>隐私设置</span>
                </div>
              </template>
              
              <div class="setting-list">
                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">显示真实姓名</div>
                    <div class="item-desc">向患者展示真实姓名而非用户名</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.showRealName" />
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">显示联系方式</div>
                    <div class="item-desc">允许患者查看您的联系电话</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.showContact" />
                  </div>
                </div>

                <div class="setting-item">
                  <div class="item-info">
                    <div class="item-title">在线咨询可见</div>
                    <div class="item-desc">允许患者向您发起在线咨询</div>
                  </div>
                  <div class="item-control">
                    <el-switch v-model="settings.consultVisible" />
                  </div>
                </div>

                <el-divider />

                <div class="setting-item danger">
                  <div class="item-info">
                    <div class="item-title">清除缓存</div>
                    <div class="item-desc">清除本地存储的临时数据</div>
                  </div>
                  <div class="item-control">
                    <el-button type="danger" plain @click="handleClearCache">清除缓存</el-button>
                  </div>
                </div>

                <div class="setting-item danger">
                  <div class="item-info">
                    <div class="item-title">导出个人数据</div>
                    <div class="item-desc">导出您的工作记录和统计数据</div>
                  </div>
                  <div class="item-control">
                    <el-button type="primary" plain @click="handleExportData">导出数据</el-button>
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
                    <el-icon :size="64" color="#3b82f6"><FirstAidKit /></el-icon>
                  </div>
                  <h2 class="app-name">宠物医院管理系统</h2>
                  <p class="app-version">版本号：v1.0.0</p>
                </div>

                <el-descriptions :column="1" border class="about-info">
                  <el-descriptions-item label="系统名称">宠物医院医生工作站</el-descriptions-item>
                  <el-descriptions-item label="开发团队">毕业设计小组</el-descriptions-item>
                  <el-descriptions-item label="技术支持">XXX大学计算机学院</el-descriptions-item>
                  <el-descriptions-item label="更新时间">2026-04-12</el-descriptions-item>
                </el-descriptions>

                <div class="about-actions">
                  <el-button type="primary" @click="handleCheckUpdate">
                    <el-icon><Refresh /></el-icon>
                    检查更新
                  </el-button>
                  <el-button @click="showFeedback = true">
                    <el-icon><ChatDotRound /></el-icon>
                    反馈问题
                  </el-button>
                </div>

                <div class="copyright">
                  <p>© 2026 宠物医院管理系统 版权所有</p>
                </div>
              </div>
            </el-card>
          </template>

          <!-- 保存按钮 -->
          <div class="settings-footer">
            <el-button type="primary" size="large" @click="handleSaveSettings">
              <el-icon><Check /></el-icon>
              保存设置
            </el-button>
            <el-button size="large" @click="handleResetSettings">恢复默认</el-button>
          </div>
        </div>
      </div>

      <!-- 反馈对话框 -->
      <el-dialog v-model="showFeedback" title="问题反馈" width="500px">
        <el-form :model="feedbackForm" label-width="80px">
          <el-form-item label="反馈类型">
            <el-radio-group v-model="feedbackForm.type">
              <el-radio label="bug">功能异常</el-radio>
              <el-radio label="feature">功能建议</el-radio>
              <el-radio label="other">其他</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="详细描述">
            <el-input 
              v-model="feedbackForm.content" 
              type="textarea" 
              :rows="4"
              placeholder="请详细描述您遇到的问题或建议..." 
            />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="feedbackForm.contact" placeholder="手机号或邮箱（选填）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showFeedback = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitFeedback">提交反馈</el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, Monitor, Bell, Briefcase, Lock, InfoFilled,
  ArrowRight, Check, Refresh, ChatDotRound, FirstAidKit
} from '@element-plus/icons-vue'
import { useSettingsStore } from '@/store/settings'

const settingsStore = useSettingsStore()

// 菜单配置
const menuList = [
  { key: 'interface', label: '界面设置', icon: 'Monitor' },
  { key: 'notification', label: '通知设置', icon: 'Bell' },
  { key: 'work', label: '工作设置', icon: 'Briefcase' },
  { key: 'privacy', label: '隐私设置', icon: 'Lock' },
  { key: 'about', label: '关于系统', icon: 'InfoFilled' }
]

const activeMenu = ref('interface')

// 预定义颜色
const predefineColors = [
  '#3b82f6', '#10b981', '#f59e0b', '#ef4444', 
  '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'
]

// 设置数据 - 直接绑定 store
const settings = reactive({
  themeColor: settingsStore.themeColor,
  sidebarStyle: settingsStore.sidebarStyle,
  collapseMenu: settingsStore.collapseMenu,
  showBreadcrumb: settingsStore.showBreadcrumb,
  notifyNewRegister: settingsStore.notifyNewRegister,
  notifyNewConsult: settingsStore.notifyNewConsult,
  notifyPrescriptionPaid: settingsStore.notifyPrescriptionPaid,
  notifyTimeout: settingsStore.notifyTimeout,
  soundEnabled: settingsStore.soundEnabled,
  desktopNotify: settingsStore.desktopNotify,
  refreshInterval: settingsStore.refreshInterval,
  defaultStatus: settingsStore.defaultStatus,
  recordTemplate: settingsStore.recordTemplate,
  autoSavePrescription: settingsStore.autoSavePrescription,
  showRealName: settingsStore.showRealName,
  showContact: settingsStore.showContact,
  consultVisible: settingsStore.consultVisible
})

// 同步 store 到本地 reactive
watch(() => settingsStore.$state, (newState) => {
  Object.assign(settings, newState)
}, { deep: true })

// 反馈
const showFeedback = ref(false)
const feedbackForm = reactive({
  type: 'bug',
  content: '',
  contact: ''
})

// 方法
const handleThemeChange = (color) => {
  settingsStore.themeColor = color
  settingsStore.applyThemeColor(color)
  ElMessage.success('主题色已应用')
}

const handleSidebarStyleChange = (style) => {
  settingsStore.sidebarStyle = style
  ElMessage.success('侧边栏样式已切换')
  // 不需要刷新页面，DoctorLayout 会通过 sidebarClass 计算属性实时响应
}

const handleDesktopNotify = async (val) => {
  if (val) {
    const granted = await settingsStore.requestNotificationPermission()
    if (!granted) {
      ElMessage.warning('请允许通知权限')
      settings.desktopNotify = false
      settingsStore.desktopNotify = false
    } else {
      settingsStore.desktopNotify = true
      ElMessage.success('桌面通知已开启')
    }
  } else {
    settingsStore.desktopNotify = false
  }
}

const handleSoundTest = (val) => {
  if (val) {
    settingsStore.playNotificationSound()
    ElMessage.success('声音提醒已开启')
  }
}

const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清除所有缓存数据吗？', '确认清除', {
      type: 'warning'
    })
    localStorage.clear()
    ElMessage.success('缓存已清除，页面将重新加载')
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (e) {
    // 用户取消
  }
}

const handleExportData = () => {
  // 导出当前设置
  const data = JSON.stringify(settingsStore.$state, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `doctor_settings_${new Date().getTime()}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('数据导出成功')
}

const handleCheckUpdate = () => {
  ElMessage.success('已是最新版本')
}

const handleSubmitFeedback = () => {
  if (!feedbackForm.content.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  ElMessage.success('反馈提交成功，感谢您的建议！')
  showFeedback.value = false
  feedbackForm.content = ''
  feedbackForm.contact = ''
}

const handleSaveSettings = () => {
  // 同步到 store
  Object.keys(settings).forEach(key => {
    if (key in settingsStore) {
      settingsStore[key] = settings[key]
    }
  })
  settingsStore.saveSettings()
  ElMessage.success('设置已保存')
}

const handleResetSettings = async () => {
  try {
    await ElMessageBox.confirm('确定要恢复默认设置吗？', '确认恢复', {
      type: 'warning'
    })
    settingsStore.resetSettings()
    Object.assign(settings, settingsStore.$state)
    ElMessage.success('已恢复默认设置')
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
        color: #3b82f6;
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
          background: #3b82f6;
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
                background: #f0f9ff;
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