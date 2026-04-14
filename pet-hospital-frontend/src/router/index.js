import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/pet' },
  { path: '/pet', component: () => import('@/views/owner/pet/index.vue') },
  { path: '/health', component: () => import('@/views/owner/health/index.vue') },
  { path: '/reserve', component: () => import('@/views/owner/reserve/index.vue') },
  { path: '/consult', component: () => import('@/views/owner/consult/index.vue') },
  { path: '/profile', component: () => import('@/views/owner/profile/index.vue') },
  { path: '/orders', component: () => import('@/views/owner/order/index.vue') },
  { path: '/order/:id', component: () => import('@/views/owner/order/detail.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router