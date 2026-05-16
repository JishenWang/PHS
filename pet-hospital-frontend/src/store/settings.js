import { defineStore } from 'pinia'
import { ref, watch, computed } from 'vue'

export const useSettingsStore = defineStore('settings', () => {
  // ========== 状态定义 ==========
  
  // 界面设置
  const themeColor = ref('#3b82f6')
  const sidebarStyle = ref('dark') // 'dark' | 'light'
  const collapseMenu = ref(false)
  const showBreadcrumb = ref(true)
  
  // 通知设置
  const notifyNewRegister = ref(true)
  const notifyNewConsult = ref(true)
  const notifyPrescriptionPaid = ref(true)
  const notifyTimeout = ref(true)
  const soundEnabled = ref(true)
  const desktopNotify = ref(false)
  
  // 工作设置
  const refreshInterval = ref(30) // 秒
  const defaultStatus = ref(1) // 1-空闲, 2-接诊中, 0-休息
  const recordTemplate = ref('general')
  const autoSavePrescription = ref(true)
  
  // 隐私设置
  const showRealName = ref(true)
  const showContact = ref(false)
  const consultVisible = ref(true)
  
  // 数据设置（管理端）
  const pageSize = ref(20)

  // ========== 计算属性 ==========
  
  // 获取状态文本
  const defaultStatusText = computed(() => {
    const map = { 0: 'Resting', 1: 'Free', 2: 'In Progress' }
    return map[defaultStatus.value] || 'Free'
  })
  
  // 获取模板默认内容
  const getTemplateContent = (templateType) => {
    const templates = {
      general: {
        chiefComplaint: '',
        symptoms: '',
        presentIllness: '',
        pastHistory: '',
        physicalExam: '',
        auxiliaryExam: '',
        diagnosis: '',
        treatmentPlan: '',
        doctorAdvice: ''
      },
      internal: {
        chiefComplaint: 'Loss of appetite, lethargy',
        symptoms: 'Pet shows decreased appetite, low spirits, reduced activity',
        presentIllness: 'Appetite decline in the last 2-3 days, poor mental state',
        pastHistory: 'No special medical history',
        physicalExam: 'Temperature: 38.5°C, heart rate: normal, respiration: normal',
        auxiliaryExam: 'Recommend blood routine examination',
        diagnosis: 'Preliminary diagnosis: digestive system dysfunction',
        treatmentPlan: '1. Oral probiotics to regulate intestinal flora\n2. Light diet\n3. Observe for 3-5 days',
        doctorAdvice: 'Monitor appetite recovery; if no improvement, please revisit promptly'
      },
      surgery: {
        chiefComplaint: 'Trauma, lameness',
        symptoms: 'Local swelling, pain, limited mobility',
        presentIllness: 'Lameness found today, local trauma detected upon examination',
        pastHistory: 'No surgical history',
        physicalExam: 'Local swelling at the affected area, obvious tenderness',
        auxiliaryExam: 'Recommend X-ray to rule out fracture',
        diagnosis: 'Preliminary diagnosis: soft tissue contusion',
        treatmentPlan: '1. Local cold compress\n2. Restrict activity\n3. Oral anti-inflammatory painkillers',
        doctorAdvice: 'Restrict activity for 3-5 days; if symptoms worsen, please revisit promptly'
      },
      vaccine: {
        chiefComplaint: 'Routine vaccination',
        symptoms: 'No abnormal symptoms, good mental state',
        presentIllness: 'Healthy, no discomfort',
        pastHistory: 'Complete vaccination history',
        physicalExam: 'Temperature: 38.2°C, heart rate: normal, respiration: normal, no abnormalities on body surface examination',
        auxiliaryExam: 'No auxiliary examination needed',
        diagnosis: 'Good health condition, suitable for vaccination',
        treatmentPlan: 'Administer rabies vaccine / quadruple vaccine',
        doctorAdvice: 'Observe for 30 minutes after vaccination; watch for allergic reactions'
      }
    }
    return templates[templateType] || templates.general
  }

  // ========== 方法 ==========
  
  // 根据当前路径获取存储 key
  const getStorageKey = () => {
    const path = window.location.pathname
    if (path.startsWith('/admin')) return 'admin_settings'
    if (path.startsWith('/doctor')) return 'doctor_settings'
    if (path.startsWith('/desk')) return 'desk_settings'
    if (path.startsWith('/owner')) return 'owner_settings'
    return 'doctor_settings'
  }
  
  // 初始化：从 localStorage 加载
  const initSettings = () => {
    const saved = localStorage.getItem(getStorageKey())
    if (saved) {
      try {
        const data = JSON.parse(saved)
        themeColor.value = data.themeColor || '#3b82f6'
        sidebarStyle.value = data.sidebarStyle || 'dark'
        collapseMenu.value = data.collapseMenu || false
        showBreadcrumb.value = data.showBreadcrumb !== false
        
        notifyNewRegister.value = data.notifyNewRegister !== false
        notifyNewConsult.value = data.notifyNewConsult !== false
        notifyPrescriptionPaid.value = data.notifyPrescriptionPaid !== false
        notifyTimeout.value = data.notifyTimeout !== false
        soundEnabled.value = data.soundEnabled !== false
        desktopNotify.value = data.desktopNotify || false
        
        refreshInterval.value = data.refreshInterval || 30
        defaultStatus.value = data.defaultStatus ?? 1
        recordTemplate.value = data.recordTemplate || 'general'
        autoSavePrescription.value = data.autoSavePrescription !== false
        
        showRealName.value = data.showRealName !== false
        showContact.value = data.showContact || false
        consultVisible.value = data.consultVisible !== false
        
        pageSize.value = data.pageSize || 20
        
        // 应用主题色
        applyThemeColor(themeColor.value)
      } catch (e) {
        console.error('Load settings failed', e)
      }
    }
  }
  
  // 保存到 localStorage
  const saveSettings = () => {
    const data = {
      themeColor: themeColor.value,
      sidebarStyle: sidebarStyle.value,
      collapseMenu: collapseMenu.value,
      showBreadcrumb: showBreadcrumb.value,
      notifyNewRegister: notifyNewRegister.value,
      notifyNewConsult: notifyNewConsult.value,
      notifyPrescriptionPaid: notifyPrescriptionPaid.value,
      notifyTimeout: notifyTimeout.value,
      soundEnabled: soundEnabled.value,
      desktopNotify: desktopNotify.value,
      refreshInterval: refreshInterval.value,
      defaultStatus: defaultStatus.value,
      recordTemplate: recordTemplate.value,
      autoSavePrescription: autoSavePrescription.value,
      showRealName: showRealName.value,
      showContact: showContact.value,
      consultVisible: consultVisible.value,
      pageSize: pageSize.value
    }
    localStorage.setItem(getStorageKey(), JSON.stringify(data))
  }
  
  // 恢复默认
  const resetSettings = () => {
    themeColor.value = '#3b82f6'
    sidebarStyle.value = 'dark'
    collapseMenu.value = false
    showBreadcrumb.value = true
    
    notifyNewRegister.value = true
    notifyNewConsult.value = true
    notifyPrescriptionPaid.value = true
    notifyTimeout.value = true
    soundEnabled.value = true
    desktopNotify.value = false
    
    refreshInterval.value = 30
    defaultStatus.value = 1
    recordTemplate.value = 'general'
    autoSavePrescription.value = true
    
    showRealName.value = true
    showContact.value = false
    consultVisible.value = true
    
    pageSize.value = 20
    
    applyThemeColor(themeColor.value)
    saveSettings()
  }
  
  // HEX 转 RGBA
  const hexToRgba = (hex, alpha) => {
    const r = parseInt(hex.slice(1, 3), 16)
    const g = parseInt(hex.slice(3, 5), 16)
    const b = parseInt(hex.slice(5, 7), 16)
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
  }

  // 应用主题色（修改 CSS 变量）
  const applyThemeColor = (color) => {
    document.documentElement.style.setProperty('--el-color-primary', color)
    // 生成不同透明度的变体（使用 RGBA 格式兼容性更好）
    document.documentElement.style.setProperty('--el-color-primary-light-3', hexToRgba(color, 0.5))
    document.documentElement.style.setProperty('--el-color-primary-light-5', hexToRgba(color, 0.25))
    document.documentElement.style.setProperty('--el-color-primary-light-7', hexToRgba(color, 0.125))
    document.documentElement.style.setProperty('--el-color-primary-light-8', hexToRgba(color, 0.0625))
    document.documentElement.style.setProperty('--el-color-primary-light-9', hexToRgba(color, 0.05))
    
    // 同时设置一些自定义变量供布局使用
    document.documentElement.style.setProperty('--primary-color', color)
    document.documentElement.style.setProperty('--primary-color-light-bg', hexToRgba(color, 0.06))
    document.documentElement.style.setProperty('--primary-color-hover-text', color)
  }
  
  // 播放提示音
  const playNotificationSound = () => {
    if (!soundEnabled.value) return
    // 使用 Web Audio API 生成简单的提示音，避免依赖外部文件
    try {
      const audioContext = new (window.AudioContext || window.webkitAudioContext)()
      const oscillator = audioContext.createOscillator()
      const gainNode = audioContext.createGain()
      
      oscillator.connect(gainNode)
      gainNode.connect(audioContext.destination)
      
      oscillator.frequency.value = 800
      oscillator.type = 'sine'
      
      gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.2)
      
      oscillator.start()
      oscillator.stop(audioContext.currentTime + 0.2)
    } catch (e) {
      // 自动播放被阻止，静默处理
    }
  }
  
  // 发送桌面通知
  const sendDesktopNotification = (title, body) => {
    if (!desktopNotify.value) return
    if (!('Notification' in window)) return
    if (Notification.permission !== 'granted') return
    
    new Notification(title, {
      body,
      icon: '/favicon.svg'
    })
  }
  
  // 请求桌面通知权限
  const requestNotificationPermission = async () => {
    if (!('Notification' in window)) return false
    if (Notification.permission === 'granted') return true
    
    const permission = await Notification.requestPermission()
    return permission === 'granted'
  }
  
  // 检查是否应该显示某类通知
  const shouldNotify = (type) => {
    switch (type) {
      case 'newRegister': return notifyNewRegister.value
      case 'newConsult': return notifyNewConsult.value
      case 'prescriptionPaid': return notifyPrescriptionPaid.value
      case 'timeout': return notifyTimeout.value
      default: return true
    }
  }

  // ========== 监听变化自动保存 ==========
  
  watch([
    themeColor, sidebarStyle, collapseMenu, showBreadcrumb,
    notifyNewRegister, notifyNewConsult, notifyPrescriptionPaid, notifyTimeout,
    soundEnabled, desktopNotify, refreshInterval, defaultStatus, recordTemplate,
    autoSavePrescription, showRealName, showContact, consultVisible, pageSize
  ], () => {
    saveSettings()
  }, { deep: true })

  return {
    // 状态
    themeColor,
    sidebarStyle,
    collapseMenu,
    showBreadcrumb,
    notifyNewRegister,
    notifyNewConsult,
    notifyPrescriptionPaid,
    notifyTimeout,
    soundEnabled,
    desktopNotify,
    refreshInterval,
    defaultStatus,
    recordTemplate,
    autoSavePrescription,
    showRealName,
    showContact,
    consultVisible,
    pageSize,
    
    // 计算属性
    defaultStatusText,
    
    // 方法
    initSettings,
    saveSettings,
    resetSettings,
    applyThemeColor,
    playNotificationSound,
    sendDesktopNotification,
    requestNotificationPermission,
    shouldNotify,
    getTemplateContent
  }
})
