export default [
  {
    path: '/admin',
    redirect: '/admin/dashboard'
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/dashboard/index.vue'),
    meta: { title: '数据看板' }
  },
  {
    path: '/admin/user',
    name: 'AdminUser',
    component: () => import('@/views/admin/user/index.vue'),
    meta: { title: '用户管理' }
  },
  {
    path: '/admin/doctor',
    name: 'AdminDoctor',
    component: () => import('@/views/admin/doctor/index.vue'),
    meta: { title: '医生管理' }
  },
  {
    path: '/admin/pet',
    name: 'AdminPet',
    component: () => import('@/views/admin/pet/index.vue'),
    meta: { title: '宠物管理' }
  },
  {
    path: '/admin/system',
    name: 'AdminSystem',
    component: () => import('@/views/admin/system/index.vue'),
    meta: { title: '系统配置' }
  }
]