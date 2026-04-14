import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import request from '@/utils/request'

// 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const app = createApp(App)

// ========== 关键修改：先 Pinia，再 Router ==========
app.use(createPinia())  
app.use(router)         // 后注册，确保路由守卫能使用 store

// 全局挂载 Element Plus（中文）
app.use(ElementPlus, { locale: zhCn })

// 全局挂载 axios（四端共用）
app.config.globalProperties.$request = request

// 挂载到页面
app.mount('#app')