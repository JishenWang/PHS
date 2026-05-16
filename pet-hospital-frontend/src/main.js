import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import request from '@/utils/request'
import i18n, { getLocale } from '@/locales'

// 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'

const app = createApp(App)

// ========== 关键修改：先 Pinia，再 Router ==========
app.use(createPinia())
app.use(router)

// Element Plus 动态 locale
const elLocaleMap = {
  zh: zhCn,
  en: en,
}
app.use(ElementPlus, { locale: elLocaleMap[getLocale()] || zhCn })

// i18n
app.use(i18n)

// 全局挂载 axios（四端共用）
app.config.globalProperties.$request = request

// 挂载到页面
app.mount('#app')
