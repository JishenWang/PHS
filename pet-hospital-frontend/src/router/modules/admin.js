import AdminLayout from '@/layout/AdminLayout.vue'

export default [
  {
    path: '/admin',
    component: AdminLayout,  // 添加布局组件
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/index.vue'),
        meta: { title: '数据看板', requireAuth: true, role: 'admin' }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/user/index.vue'),
        meta: { title: '用户管理', requireAuth: true, role: 'admin' }
      },
      {
        path: 'doctor',
        name: 'AdminDoctor',
        component: () => import('@/views/admin/doctor/index.vue'),
        meta: { title: '医生管理', requireAuth: true, role: 'admin' }
      },
      {
        path: 'pet',
        name: 'AdminPet',
        component: () => import('@/views/admin/pet/index.vue'),
        meta: { title: '宠物管理', requireAuth: true, role: 'admin' }
      },
      {
        path: 'system',
        name: 'AdminSystem',
        component: () => import('@/views/admin/system/index.vue'),
        meta: { title: '系统配置', requireAuth: true, role: 'admin' }
      }
    ]
  }
]