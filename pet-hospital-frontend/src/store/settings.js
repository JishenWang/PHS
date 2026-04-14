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

  // ========== 计算属性 ==========
  
  // 获取状态文本
  const defaultStatusText = computed(() => {
    const map = { 0: '休息中', 1: '空闲', 2: '接诊中' }
    return map[defaultStatus.value] || '空闲'
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
        chiefComplaint: '食欲不振、精神萎靡',
        symptoms: '宠物出现食欲减退、精神不振、活动减少等症状',
        presentIllness: '近2-3天出现食欲下降，精神状态不佳',
        pastHistory: '无特殊病史',
        physicalExam: '体温：38.5℃，心率：正常，呼吸：正常',
        auxiliaryExam: '建议进行血常规检查',
        diagnosis: '初步诊断：消化系统功能紊乱',
        treatmentPlan: '1. 口服益生菌调节肠道菌群\n2. 清淡饮食\n3. 观察3-5天',
        doctorAdvice: '注意观察食欲恢复情况，如无改善请及时复诊'
      },
      surgery: {
        chiefComplaint: '外伤、跛行',
        symptoms: '局部肿胀、疼痛、活动受限',
        presentIllness: '今日发现跛行，检查发现局部外伤',
        pastHistory: '无手术史',
        physicalExam: '患处局部肿胀，触痛明显',
        auxiliaryExam: '建议进行X光检查排除骨折',
        diagnosis: '初步诊断：软组织挫伤',
        treatmentPlan: '1. 局部冷敷\n2. 限制活动\n3. 口服消炎止痛药',
        doctorAdvice: '限制活动3-5天，如症状加重请及时复诊'
      },
      vaccine: {
        chiefComplaint: '常规疫苗接种',
        symptoms: '无异常症状，精神状态良好',
        presentIllness: '健康，无不适表现',
        pastHistory: '疫苗接种史完整',
        physicalExam: '体温：38.2℃，心率：正常，呼吸：正常，体表检查无异常',
        auxiliaryExam: '无需辅助检查',
        diagnosis: '健康状况良好，适合接种疫苗',
        treatmentPlan: '接种狂犬疫苗/四联疫苗',
        doctorAdvice: '接种后观察30分钟，注意是否有过敏反应'
      }
    }
    return templates[templateType] || templates.general
  }

  // ========== 方法 ==========
  
  // 初始化：从 localStorage 加载
  const initSettings = () => {
    const saved = localStorage.getItem('doctor_settings')
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
        
        // 应用主题色
        applyThemeColor(themeColor.value)
      } catch (e) {
        console.error('加载设置失败', e)
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
      consultVisible: consultVisible.value
    }
    localStorage.setItem('doctor_settings', JSON.stringify(data))
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
    
    applyThemeColor(themeColor.value)
    saveSettings()
  }
  
  // 应用主题色（修改 CSS 变量）
  const applyThemeColor = (color) => {
    document.documentElement.style.setProperty('--el-color-primary', color)
    // 生成不同透明度的变体
    document.documentElement.style.setProperty('--el-color-primary-light-3', color + '80')
    document.documentElement.style.setProperty('--el-color-primary-light-5', color + '40')
    document.documentElement.style.setProperty('--el-color-primary-light-7', color + '20')
    document.documentElement.style.setProperty('--el-color-primary-light-8', color + '10')
    document.documentElement.style.setProperty('--el-color-primary-light-9', color + '08')
    
    // 同时设置一些自定义变量供布局使用
    document.documentElement.style.setProperty('--primary-color', color)
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
    autoSavePrescription, showRealName, showContact, consultVisible
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