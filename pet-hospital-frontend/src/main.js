import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import request from '@/utils/request'

const app = createApp(App)

// 全局挂载路由、Pinia
app.use(router)
app.use(createPinia())

// 全局挂载 axios（四端共用）
app.config.globalProperties.$request = request

// 挂载到页面
app.mount('#app')