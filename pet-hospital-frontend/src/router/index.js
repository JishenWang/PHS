import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  // 首页自动跳转到登录页
  {
    path: '/',
    redirect: '/login'
  },

  // =============== 登录页（共用）===============
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },

  // =============== 1. 管理端（admin 目录）===============
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/dashboard/index.vue')
  },
  {
    path: '/admin/user',
    name: 'AdminUser',
    component: () => import('@/views/admin/user/index.vue')
  },
  {
    path: '/admin/doctor',
    name: 'AdminDoctor',
    component: () => import('@/views/admin/doctor/index.vue')
  },
  {
    path: '/admin/pet',
    name: 'AdminPet',
    component: () => import('@/views/admin/pet/index.vue')
  },
  {
    path: '/admin/system',
    name: 'AdminSystem',
    component: () => import('@/views/admin/system/index.vue')
  },

  // =============== 2. 前台端（desk 目录）===============
  {
    path: '/desk/customer',
    name: 'DeskCustomer',
    component: () => import('@/views/desk/customer/index.vue')
  },
  {
    path: '/desk/register',
    name: 'DeskRegister',
    component: () => import('@/views/desk/register/index.vue')
  },
  {
    path: '/desk/charge',
    name: 'DeskCharge',
    component: () => import('@/views/desk/charge/index.vue')
  },

  // =============== 3. 医生端（doctor 目录）===============
  {
    path: '/doctor/accept',
    name: 'DoctorAccept',
    component: () => import('@/views/doctor/accept/index.vue')
  },
  {
    path: '/doctor/pet',
    name: 'DoctorPet',
    component: () => import('@/views/doctor/pet/index.vue')
  },
  {
    path: '/doctor/record',
    name: 'DoctorRecord',
    component: () => import('@/views/doctor/record/index.vue')
  },
  {
    path: '/doctor/prescription',
    name: 'DoctorPrescription',
    component: () => import('@/views/doctor/prescription/index.vue')
  },
  {
    path: '/doctor/consult',
    name: 'DoctorConsult',
    component: () => import('@/views/doctor/consult/index.vue')
  },

  // =============== 4. 用户端（owner 目录）===============
  {
    path: '/owner/pet',
    name: 'OwnerPet',
    component: () => import('@/views/owner/pet/index.vue')
  },
  {
    path: '/owner/health',
    name: 'OwnerHealth',
    component: () => import('@/views/owner/health/index.vue')
  },
  {
    path: '/owner/reserve',
    name: 'OwnerReserve',
    component: () => import('@/views/owner/reserve/index.vue')
  },
  {
    path: '/owner/consult',
    name: 'OwnerConsult',
    component: () => import('@/views/owner/consult/index.vue')
  },

  // =============== 全局错误页 ===============
  {
    path: '/403',
    component: () => import('@/views/403/index.vue')
  },
  {
    path: '/404',
    component: () => import('@/views/404/index.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router