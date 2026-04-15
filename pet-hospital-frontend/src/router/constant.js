

// ==================== 常量路由（所有人可访问）====================
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    hidden: true,
    meta: { title: '登录' }
  },
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/403/index.vue'),
    hidden: true,
    meta: { title: '无权限' }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404/index.vue'),
    hidden: true,
    meta: { title: '页面不存在' }
  },
  {
    path: '/',
    redirect: '/login',
    hidden: true
  },
  // 404通配符路由放在常量路由最后
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/404',
    hidden: true
  }
]

// ==================== 异步路由（根据角色动态加载）====================
export const asyncRoutes = [
  // 管理员路由
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { title: '管理端', roles: ['admin'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'dashboard' }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/user/index.vue'),
        meta: { title: '用户管理', icon: 'user' }
      },
      {
        path: 'doctor',
        name: 'AdminDoctor',
        component: () => import('@/views/admin/doctor/index.vue'),
        meta: { title: '医生管理', icon: 'doctor' }
      },
      {
        path: 'pet',
        name: 'AdminPet',
        component: () => import('@/views/admin/pet/index.vue'),
        meta: { title: '宠物管理', icon: 'pet' }
      },
      {
        path: 'system',
        name: 'AdminSystem',
        component: () => import('@/views/admin/system/index.vue'),
        meta: { title: '系统配置', icon: 'setting' }
      }
    ]
  },
  
  // ==================== 客户自助端路由 ====================
  {
    path: '/owner',
    name: 'Owner',
    component: () => import('@/layout/OwnerLayout.vue'),
    redirect: '/owner/pet',
    meta: { title: '客户自助端', roles: ['owner'] },
    children: [
      {
        path: 'pet',
        name: 'OwnerPet',
        component: () => import('@/views/owner/pet/index.vue'),
        meta: { title: '我的宠物', icon: 'pet' }
      },
      {
        path: 'pet/:id',
        name: 'OwnerPetDetail',
        component: () => import('@/views/owner/pet/detail.vue'),
        meta: { title: '宠物详情', hidden: true }
      },
      {
        path: 'health',
        name: 'OwnerHealth',
        component: () => import('@/views/owner/health/index.vue'),
        meta: { title: '健康记录', icon: 'health' }
      },
      {
        path: 'reserve',
        name: 'OwnerReserve',
        component: () => import('@/views/owner/reserve/index.vue'),
        meta: { title: '预约申请', icon: 'reserve' }
      },
      {
        path: 'reserve/:id',
        name: 'OwnerReserveDetail',
        component: () => import('@/views/owner/reserve/detail.vue'),
        meta: { title: '预约详情', hidden: true }
      },
      {
        path: 'reserve/create',
        name: 'OwnerReserveCreate',
        component: () => import('../views/owner/reserve/create.vue'),
        meta: { title: '创建预约', hidden: true }
      },
      {
        path: 'consult',
        name: 'OwnerConsult',
        component: () => import('@/views/owner/consult/index.vue'),
        meta: { title: '在线咨询', icon: 'consult' }
      },
      {
        path: 'consult/:id',
        name: 'OwnerConsultDetail',
        component: () => import('@/views/owner/consult/detail.vue'),
        meta: { title: '咨询详情', hidden: true }
      },
      {
        path: 'profile',
        name: 'OwnerProfile',
        component: () => import('@/views/owner/profile/index.vue'),
        meta: { title: '个人中心', hidden: true }
      },
      {
        path: 'orders',
        name: 'OwnerOrders',
        component: () => import('@/views/owner/order/index.vue'),
        meta: { title: '我的订单', hidden: true }
      },
      {
        path: 'order/:id',
        name: 'OwnerOrderDetail',
        component: () => import('@/views/owner/order/detail.vue'),
        meta: { title: '订单详情', hidden: true }
      }
    ]
  },
  
  // ==================== 前台收银端路由 ====================
  {
    path: '/desk',
    name: 'Desk',
    component: () => import('@/layout/DeskLayout.vue'),
    redirect: '/desk/register',
    meta: { title: '前台收银端', roles: ['desk'] },
    children: [
      {
        path: 'customer',
        name: 'DeskCustomer',
        component: () => import('../views/desk/customer/index.vue'),
        meta: { title: '客户查询', icon: 'customer' }
      },
      {
        path: 'register',
        name: 'DeskRegister',
        component: () => import('@/views/desk/register/index.vue'),
        meta: { title: '挂号管理', icon: 'register' }
      },
      {
        path: 'charge',
        name: 'DeskCharge',
        component: () => import('@/views/desk/charge/index.vue'),
        meta: { title: '收费管理', icon: 'charge' }
      },
      {
        path: 'customer',
        name: 'DeskCustomer',
        component: () => import('@/views/desk/customer/index.vue'),
        meta: { title: '客户管理', icon: 'customer' }
      }
    ]
  },
  
  // ==================== 医生端路由 ====================
  {
    path: '/doctor',
    name: 'Doctor',
    component: () => import('@/layout/DoctorLayout.vue'),
    redirect: '/doctor/accept',
    meta: { title: '医生端', roles: ['doctor'] },
    children: [
      {
        path: 'accept',
        name: 'DoctorAccept',
        component: () => import('@/views/doctor/accept/index.vue'),
        meta: { title: '接诊管理', icon: 'accept' }
      },
      {
        path: 'record',
        name: 'DoctorRecord',
        component: () => import('@/views/doctor/record/index.vue'),
        meta: { title: '病历管理', icon: 'record' }
      },
      {
        path: 'prescription',
        name: 'DoctorPrescription',
        component: () => import('@/views/doctor/prescription/index.vue'),
        meta: { title: '处方管理', icon: 'prescription' }
      },
      {
        path: 'consult',
        name: 'DoctorConsult',
        component: () => import('@/views/doctor/consult/index.vue'),
        meta: { title: '在线咨询', icon: 'consult' }
      },
      {
        path: 'pet',
        name: 'DoctorPet',
        component: () => import('@/views/doctor/pet/index.vue'),
        meta: { title: '宠物档案', icon: 'pet' }
      },
      {
        path: 'record',
        name: 'DoctorRecord',
        component: () => import('../views/doctor/record/index.vue'),
        meta: { title: '病历记录', icon: 'record' }
      },
      {
        path: 'prescription',
        name: 'DoctorPrescription',
        component: () => import('../views/doctor/prescription/index.vue'),
        meta: { title: '处方开具', icon: 'prescription' }
      },
      {
        path: 'consult',
        name: 'DoctorConsult',
        component: () => import('../views/doctor/consult/index.vue'),
        meta: { title: '咨询回复', icon: 'consult' }
      },
      {
        path: 'profile',
        name: 'DoctorProfile',
        component: () => import('../views/doctor/profile/index.vue'),
        meta: { title: '个人中心', hidden: true }
      }
    ]
  }
]

